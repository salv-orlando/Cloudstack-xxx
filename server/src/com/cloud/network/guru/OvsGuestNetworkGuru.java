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

package com.cloud.network.guru;

import java.util.List;

import javax.ejb.Local;

import com.cloud.dc.DataCenterVnetVO;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.exception.InsufficientVirtualNetworkCapcityException;
import com.cloud.network.Network;
import com.cloud.network.NetworkManager;
import com.cloud.network.NetworkVO;
import com.cloud.network.ovs.OvsNetworkManager;
import com.cloud.network.ovs.OvsTunnelManager;
import com.cloud.offering.NetworkOffering;
import com.cloud.user.Account;
import com.cloud.utils.component.Inject;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.ReservationContext;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Network.State;

@Local(value=NetworkGuru.class)
public class OvsGuestNetworkGuru extends GuestNetworkGuru {
	@Inject OvsNetworkManager _ovsNetworkMgr;
	@Inject NetworkManager _externalNetworkManager;
	@Inject OvsTunnelManager _ovsTunnelMgr;
	
	@Override
    public Network design(NetworkOffering offering, DeploymentPlan plan, Network userSpecified, Account owner) {
      
		if (!_ovsNetworkMgr.isOvsNetworkEnabled() && !_ovsTunnelMgr.isOvsTunnelEnabled()) {
			return null;
		}
		
        NetworkVO config = (NetworkVO) super.design(offering, plan, userSpecified, owner); 
        if (config == null) {
        	return null;
        }
        
        config.setBroadcastDomainType(BroadcastDomainType.Vswitch);
        
        return config;
	}
	
	@Override
	 public Network implement(Network config, NetworkOffering offering, DeployDestination dest, ReservationContext context) throws InsufficientVirtualNetworkCapcityException {
		 assert (config.getState() == State.Implementing) : "Why are we implementing " + config;
		 if (!_ovsNetworkMgr.isOvsNetworkEnabled()&& !_ovsTunnelMgr.isOvsTunnelEnabled()) {
			 return null;
		 }
		 NetworkVO implemented = (NetworkVO)super.implement(config, offering, dest, context);		 
		 // Overrides operations performed in the base class
		 // - Set broadcast URI for network
		 String uri = null;
		 if (_ovsNetworkMgr.isOvsNetworkEnabled()) {
		     uri = "vlan";
		 } else if (_ovsTunnelMgr.isOvsTunnelEnabled()) {
		     uri = Long.toString(config.getAccountId());
		 }
		 if (!config.isSpecifiedCidr()) {
			 // - Set cidr and gateway if not specified
			 // a vnet should have been allocated - can use it for calculating the CIDR
			 long dcId = dest.getDataCenter().getId();
			 long physicalNetworkId = _networkMgr.findPhysicalNetworkId(dcId, offering.getTags());
			 List<DataCenterVnetVO> vnetVOs = _dcDao.listAllocatedVnets(physicalNetworkId);
			 String vnet = null;
			 for (DataCenterVnetVO vnetVO:vnetVOs) {
				 if (vnetVO.getReservationId().equals(implemented.getReservationId())) {
					 vnet = vnetVO.getVnet();
				 }
			}
	        if (vnet == null) {
	            throw new CloudRuntimeException("Unable to retrieve vnet allocation for network: " +
	            								implemented.getId() + "(reservation:" +
	            								implemented.getReservationId() + ")");
	        }
			int vnetId = Integer.parseInt(vnet);

			// Procedure is the same as external network guru, but tag is a fake
			// FIXME: This is just awful, provide a better approach
			// Determine the offset from the lowest vlan tag
	        int offset = getVlanOffset(config.getPhysicalNetworkId(), vnetId);
	        // Determine the new gateway and CIDR
	        int cidrSize = getGloballyConfiguredCidrSize();
	        // If the offset has more bits than there is room for, return null
	        long bitsInOffset = 32 - Integer.numberOfLeadingZeros(offset);
	        if (bitsInOffset > (cidrSize - 8)) {
	            throw new CloudRuntimeException("The offset " + offset + " needs " + bitsInOffset + " bits, but only have " + (cidrSize - 8) + " bits to work with.");
	        }
	        // Use 10.1.1.1 which is reserved for private address
	        long newCidrAddress = (NetUtils.ip2Long("10.1.1.1") & 0xff000000) | (offset << (32 - cidrSize));
	        implemented.setGateway(NetUtils.long2Ip(newCidrAddress + 1));
	        implemented.setCidr(NetUtils.long2Ip(newCidrAddress) + "/" + cidrSize);
	        implemented.setState(State.Implemented);
			 // TODO: - Reconfigure IP addresses for NICs
		 }
		 implemented.setBroadcastUri(BroadcastDomainType.Vswitch.toUri(uri));
         return implemented;
	}
	
}
