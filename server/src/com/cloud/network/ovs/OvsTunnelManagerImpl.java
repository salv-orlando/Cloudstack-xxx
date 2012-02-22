/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.cloud.network.ovs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.ejb.Local;
import javax.naming.ConfigurationException;
import javax.persistence.EntityExistsException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.manager.Commands;
import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.network.Network;
import com.cloud.network.ovs.dao.OvsTunnelAccountDao;
import com.cloud.network.ovs.dao.OvsTunnelAccountVO;
import com.cloud.network.ovs.dao.OvsTunnelDao;
import com.cloud.utils.Pair;
import com.cloud.utils.component.Inject;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.DB;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.NicVO;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.UserVmDao;

@Local(value={OvsTunnelManager.class})
public class OvsTunnelManagerImpl implements OvsTunnelManager {
	public static final Logger s_logger = Logger.getLogger(OvsTunnelManagerImpl.class.getName());
	
	String _name;
	boolean _isEnabled;
	ScheduledExecutorService _executorPool;
    ScheduledExecutorService _cleanupExecutor;
    OvsTunnelListener _listener;
    
	@Inject ConfigurationDao _configDao;
	@Inject NicDao _nicDao;
	@Inject HostDao _hostDao;
	@Inject UserVmDao _userVmDao;
	@Inject DomainRouterDao _routerDao;
	@Inject OvsTunnelDao _tunnelDao;
	@Inject OvsTunnelAccountDao _tunnelAccountDao;
	@Inject AgentManager _agentMgr;
	
	@Override
	public boolean configure(String name, Map<String, Object> params)
			throws ConfigurationException {
		_name = name;
		_isEnabled = Boolean.parseBoolean(_configDao.getValue(Config.OvsTunnelNetwork.key()));
		
		if (_isEnabled) {
			_executorPool = Executors.newScheduledThreadPool(10, new NamedThreadFactory("OVS"));
			_cleanupExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("OVS-Cleanup"));
			_listener = new OvsTunnelListener(_tunnelDao, _hostDao);
			_agentMgr.registerForHostEvents(_listener, true, true, true);
		}
		
		return true;
	}

	protected OvsTunnelAccountVO createTunnelRecord(long from, long to, long networkId) {
		OvsTunnelAccountVO ta = null;
		
		try {
			// Use the network id as a Key
			// FIXME: networkid is long, key must be integer
			// This is a horrible cast, and it must be removed and replace with something better
			// before committing into master
			ta = new OvsTunnelAccountVO(from, to, (int)networkId, networkId);
			OvsTunnelAccountVO lock = _tunnelAccountDao.acquireInLockTable(Long.valueOf(1));
			if (lock == null) {
			    s_logger.warn("Cannot lock table ovs_tunnel_account");
			    return null;
			}
			_tunnelAccountDao.persist(ta);
			_tunnelAccountDao.releaseFromLockTable(lock.getId());
		} catch (EntityExistsException e) {
			s_logger.debug("A record for the tunnel from " + from + " to " + to + " already exists");
		}
		
		return ta;
	}

	private void handleCreateTunnelAnswer(Answer[] answers){
		OvsCreateTunnelAnswer r = (OvsCreateTunnelAnswer) answers[0];
		String s = String.format(
				"(hostIP:%1$s, remoteIP:%2$s, bridge:%3$s, greKey:%4$s, portName:%5$s)",
				r.getFromIp(), r.getToIp(), r.getBridge(), r.getKey(), r.getInPortName());
		Long from = r.getFrom();
		Long to = r.getTo();
		long networkId = r.getNetworkId();
		OvsTunnelAccountVO ta = _tunnelAccountDao.getByFromToAccount(from, to, networkId);
		if (ta == null) {
            throw new CloudRuntimeException(String.format("Unable find tunnelAccount record(from=%1$s, to=%2$s, account=%3$s", from, to, networkId));
		}
		s_logger.debug("Result:" + r.getResult());
		if (!r.getResult()) {
		    ta.setState("FAILED");
			s_logger.warn("Create GRE tunnel failed due to " + r.getDetails() + s);
		} else {
		    ta.setState("SUCCESS");
		    ta.setPortName(r.getInPortName());
		    s_logger.warn("Create GRE tunnel " + r.getDetails() + s);
		}
		_tunnelAccountDao.update(ta.getId(), ta);
	}
	
	@DB
    protected void CheckAndCreateTunnel(VirtualMachine instance, Network nw, DeployDestination dest) {
		if (!_isEnabled) {
			return;
		}
		
		s_logger.debug("Creating tunnels with OVS tunnel manager");
		if (instance.getType() != VirtualMachine.Type.User
				&& instance.getType() != VirtualMachine.Type.DomainRouter) {
			s_logger.debug("Will not work if you're not an instance or a virtual router");
			return;
		}
		
		long hostId = dest.getHost().getId();
		// Find active (i.e.: not shut off) VMs with a NIC on the target network
		List<UserVmVO> vms = _userVmDao.listByNetworkIdAndStates(nw.getId(), State.Running, State.Starting,
								State.Stopping, State.Unknown, State.Migrating);
		// Find routers for the network
		List<DomainRouterVO> routers = _routerDao.findByNetwork(nw.getId());
		List<VMInstanceVO>ins = new ArrayList<VMInstanceVO>();
		if (vms != null) {
			ins.addAll(vms);
		}
		if (routers.size() != 0) {
			ins.addAll(routers);
		}
		s_logger.debug("### Virtual Machines:" + vms.size());
		s_logger.debug("### Virtual Routers:" + routers.size());
		List<Long> toHostIds = new ArrayList<Long>();
		List<Long> fromHostIds = new ArrayList<Long>();
		
        for (VMInstanceVO v : ins) {
            Long rh = v.getHostId();
            if (rh == null || rh.longValue() == hostId) {
                continue;
            }
            //FIXME: Still using 'TunnelAccount name' - but should actually be tunnelNetwork or something like that
            OvsTunnelAccountVO ta = _tunnelAccountDao.getByFromToAccount(hostId, rh.longValue(), nw.getId());
            // Try and create the tunnel even if a previous attempt failed
            if (ta == null || ta.getState().equals("FAILED")) {
            	s_logger.debug("Attempting to create tunnel from:" + hostId + " to:" + rh.longValue());
            	if (ta == null) {
            		this.createTunnelRecord(hostId, rh.longValue(), nw.getId());
            	}
                if (!toHostIds.contains(rh)) {
                    toHostIds.add(rh);
                } 
            }

            ta = _tunnelAccountDao.getByFromToAccount(rh.longValue(), hostId, nw.getId());
            // Try and create the tunnel even if a previous attempt failed            
            if (ta == null || ta.getState().equals("FAILED")) {
            	s_logger.debug("Attempting to create tunnel from:" + rh.longValue() + " to:" + hostId);
            	if (ta == null) {
            		this.createTunnelRecord(rh.longValue(), hostId, nw.getId());
            	} 
                if (!fromHostIds.contains(rh)) {
                    fromHostIds.add(rh);
                }
            }
        }
		
		try {
			String myIp = dest.getHost().getPrivateIpAddress();
			for (Long i : toHostIds) {
				HostVO rHost = _hostDao.findById(i);
				Commands cmds = new Commands(
						new OvsCreateTunnelCommand(rHost.getPrivateIpAddress(), String.valueOf(nw.getId()),
								Long.valueOf(hostId), i, nw.getId(), myIp));
				s_logger.debug("Ask host " + hostId + " to create gre tunnel to " + i);
				Answer[] answers = _agentMgr.send(hostId, cmds);
				handleCreateTunnelAnswer(answers);
			}
			
			for (Long i : fromHostIds) {
			    HostVO rHost = _hostDao.findById(i);
				Commands cmd2s = new Commands(
				        new OvsCreateTunnelCommand(myIp, String.valueOf(nw.getId()),
				        		i, Long.valueOf(hostId), nw.getId(), rHost.getPrivateIpAddress()));
				s_logger.debug("Ask host " + i + " to create gre tunnel to " + hostId);
				Answer[] answers = _agentMgr.send(i, cmd2s);
				handleCreateTunnelAnswer(answers);
			}
		} catch (Exception e) {
		    s_logger.debug("Ovs Tunnel network created tunnel failed", e);
		}	
	}
	
	@Override
	public boolean start() {
		return true;
	}

	@Override
	public boolean stop() {
		return true;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public boolean isOvsTunnelEnabled() {
		return _isEnabled;
	}

    @Override
    public void VmCheckAndCreateTunnel(VirtualMachineProfile<? extends VirtualMachine> vm, Network nw, DeployDestination dest) {
        CheckAndCreateTunnel(vm.getVirtualMachine(), nw, dest);    
    }

    private void handleDestroyTunnelAnswer(Answer ans, long from, long to, long account) {
        String toStr = (to == 0 ? "all peers" : Long.toString(to));
        
        if (ans.getResult()) {
            OvsTunnelAccountVO lock = _tunnelAccountDao.acquireInLockTable(Long.valueOf(1));
            if (lock == null) {
                s_logger.warn(String.format("failed to lock ovs_tunnel_account, remove record of tunnel(from=%1$s, to=%2$s account=%3$s) failed", from, to, account));
                return;
            }

            if (to == 0) {
                _tunnelAccountDao.removeByFromAccount(from, account);
            } else {
                _tunnelAccountDao.removeByFromToAccount(from, to, account);
            }
            _tunnelAccountDao.releaseFromLockTable(lock.getId());
            
            s_logger.debug(String.format("Destroy tunnel(account:%1$s, from:%2$s, to:%3$s) successful", account, from, toStr)); 
        } else {
            s_logger.debug(String.format("Destroy tunnel(account:%1$s, from:%2$s, to:%3$s) failed", account, from, toStr));
        }
    }
    
    @Override
    public void CheckAndDestroyTunnel(VirtualMachine vm) {
        if (!_isEnabled) {
            return;
        }
        
        List<UserVmVO> userVms = _userVmDao.listByAccountIdAndHostId(vm.getAccountId(), vm.getHostId());
        if (vm.getType() == VirtualMachine.Type.User) {
            if (userVms.size() > 1) {
                return;
            }
            
            List<DomainRouterVO> routers = _routerDao.findBy(vm.getAccountId(), vm.getDataCenterIdToDeployIn());
            for (DomainRouterVO router : routers) {
                if (router.getHostId() == vm.getHostId()) {
                    return;
                }
            }
        } else if (vm.getType() == VirtualMachine.Type.DomainRouter && userVms.size() != 0) {
                return;
        }
        
        try {
            /* Now we are last one on host, destroy all tunnels of my account */
            Command cmd = new OvsDestroyTunnelCommand(vm.getAccountId(), "[]");
            Answer ans = _agentMgr.send(vm.getHostId(), cmd);
            handleDestroyTunnelAnswer(ans, vm.getHostId(), 0, vm.getAccountId());
            
            /* Then ask hosts have peer tunnel with me to destroy them */
            List<OvsTunnelAccountVO> peers = _tunnelAccountDao.listByToAccount(vm.getHostId(), vm.getAccountId());
            for (OvsTunnelAccountVO p : peers) {
                cmd = new OvsDestroyTunnelCommand(p.getAccount(), p.getPortName());
                ans = _agentMgr.send(p.getFrom(), cmd);
                handleDestroyTunnelAnswer(ans, p.getFrom(), p.getTo(), p.getAccount());
            }
        } catch (Exception e) {
            s_logger.warn(String.format("Destroy tunnel(account:%1$s, hostId:%2$s) failed", vm.getAccountId(), vm.getHostId()), e);
        }
        
    }

}
