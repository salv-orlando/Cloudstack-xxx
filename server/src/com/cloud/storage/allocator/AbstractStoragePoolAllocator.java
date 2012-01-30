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

package com.cloud.storage.allocator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.host.Host;
import com.cloud.server.StatsCollector;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StorageManager;
import com.cloud.storage.StoragePool;
import com.cloud.storage.StoragePoolStatus;
import com.cloud.storage.StoragePoolVO;
import com.cloud.storage.StorageStats;
import com.cloud.storage.VMTemplateHostVO;
import com.cloud.storage.VMTemplateStoragePoolVO;
import com.cloud.storage.VMTemplateStorageResourceAssoc;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.storage.VMTemplateSwiftVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.Volume.Type;
import com.cloud.storage.dao.StoragePoolDao;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplateHostDao;
import com.cloud.storage.dao.VMTemplatePoolDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.swift.SwiftManager;
import com.cloud.template.TemplateManager;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.component.Inject;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;

public abstract class AbstractStoragePoolAllocator extends AdapterBase implements StoragePoolAllocator {
	private static final Logger s_logger = Logger.getLogger(AbstractStoragePoolAllocator.class);
    @Inject TemplateManager _tmpltMgr;
    @Inject StorageManager _storageMgr;
    @Inject StoragePoolDao _storagePoolDao;
    @Inject VMTemplateHostDao _templateHostDao;
    @Inject VMTemplatePoolDao _templatePoolDao;
    @Inject VMTemplateDao _templateDao;
    @Inject VolumeDao _volumeDao;
    @Inject StoragePoolHostDao _poolHostDao;
    @Inject ConfigurationDao _configDao;
    @Inject ClusterDao _clusterDao;
    @Inject
    SwiftManager _swiftMgr;
    protected BigDecimal _storageOverprovisioningFactor = new BigDecimal(1);    
    long _extraBytesPerVolume = 0;
    Random _rand;
    boolean _dontMatter;
    double _storageUsedThreshold = 1.0d;
    double _storageAllocatedThreshold = 1.0d;
    
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        
        Map<String, String> configs = _configDao.getConfiguration(null, params);
        
        String globalStorageOverprovisioningFactor = configs.get("storage.overprovisioning.factor");
        _storageOverprovisioningFactor = new BigDecimal(NumbersUtil.parseFloat(globalStorageOverprovisioningFactor, 2.0f));
        
        _extraBytesPerVolume = 0;
        
        
        String storageUsedThreshold = _configDao.getValue(Config.StorageCapacityDisableThreshold.key());        
        if (storageUsedThreshold != null) {
            _storageUsedThreshold = Double.parseDouble(storageUsedThreshold);
        }

        String storageAllocatedThreshold = _configDao.getValue(Config.StorageAllocatedCapacityDisableThreshold.key());
        if (storageAllocatedThreshold != null) {
            _storageAllocatedThreshold = Double.parseDouble(storageAllocatedThreshold);
        }
        
        _rand = new Random(System.currentTimeMillis());
        
        _dontMatter = Boolean.parseBoolean(configs.get("storage.overwrite.provisioning"));
        
        return true;
    }
    
    abstract boolean allocatorIsCorrectType(DiskProfile dskCh);
    
	protected boolean templateAvailable(long templateId, long poolId) {
    	VMTemplateStorageResourceAssoc thvo = _templatePoolDao.findByPoolTemplate(poolId, templateId);
    	if (thvo != null) {
    		if (s_logger.isDebugEnabled()) {
    			s_logger.debug("Template id : " + templateId + " status : " + thvo.getDownloadState().toString());
    		}
    		return (thvo.getDownloadState()==Status.DOWNLOADED);
    	} else {
    		return false;
    	}
    }
	
	protected boolean localStorageAllocationNeeded(DiskProfile dskCh) {
	    return dskCh.useLocalStorage();
	}
	
	protected boolean poolIsCorrectType(DiskProfile dskCh, StoragePool pool) {
		boolean localStorageAllocationNeeded = localStorageAllocationNeeded(dskCh);
		if (s_logger.isDebugEnabled()) {
            s_logger.debug("Is localStorageAllocationNeeded? "+ localStorageAllocationNeeded);
            s_logger.debug("Is storage pool shared? "+ pool.getPoolType().isShared());
        }
		
		return ((!localStorageAllocationNeeded && pool.getPoolType().isShared()) || (localStorageAllocationNeeded && !pool.getPoolType().isShared()));
	}
	
	protected boolean checkPool(ExcludeList avoid, StoragePoolVO pool, DiskProfile dskCh, VMTemplateVO template, List<VMTemplateStoragePoolVO> templatesInPool, 
			StatsCollector sc, DeploymentPlan plan) {
		
		if (s_logger.isDebugEnabled()) {
            s_logger.debug("Checking if storage pool is suitable, name: " + pool.getName()+ " ,poolId: "+ pool.getId());
        }
		
		if (avoid.shouldAvoid(pool)) {
			if (s_logger.isDebugEnabled()) {
                s_logger.debug("StoragePool is in avoid set, skipping this pool");
            }			
			return false;
		}
        if(dskCh.getType().equals(Type.ROOT) && pool.getPoolType().equals(StoragePoolType.Iscsi)){
    		if (s_logger.isDebugEnabled()) {
                s_logger.debug("Disk needed for ROOT volume, but StoragePoolType is Iscsi, skipping this and trying other available pools");
            }	
            return false;
        }
        
        //by default, all pools are up when successfully added
		//don't return the pool if not up (if in maintenance/prepareformaintenance/errorinmaintenance)
        if(!pool.getStatus().equals(StoragePoolStatus.Up)){
    		if (s_logger.isDebugEnabled()) {
                s_logger.debug("StoragePool status is not UP, status is: "+pool.getStatus().name()+", skipping this pool");
            }
        	return false;
        }
        
		// Check that the pool type is correct
		if (!poolIsCorrectType(dskCh, pool)) {
    		if (s_logger.isDebugEnabled()) {
                s_logger.debug("StoragePool is not of correct type, skipping this pool");
            }
			return false;
		}
		
		/*hypervisor type is correct*/
		// TODO : when creating a standalone volume, offering is passed as NULL, need to 
		// refine the logic of checking hypervisorType based on offering info
		Long clusterId = pool.getClusterId();
		ClusterVO cluster = _clusterDao.findById(clusterId);
		if (!(cluster.getHypervisorType() == dskCh.getHypersorType())) {
    		if (s_logger.isDebugEnabled()) {
                s_logger.debug("StoragePool's Cluster does not have required hypervisorType, skipping this pool");
            }
			return false;
		}

		// check the used size against the total size, skip this host if it's greater than the configured
		// capacity check "storage.capacity.threshold"
		if (sc != null) {
			long totalSize = pool.getCapacityBytes();
			StorageStats stats = sc.getStoragePoolStats(pool.getId());
			if(stats == null){
				stats = sc.getStorageStats(pool.getId());
			}
			if (stats != null) {
				double usedPercentage = ((double)stats.getByteUsed() / (double)totalSize);
				if (s_logger.isDebugEnabled()) {
					s_logger.debug("Attempting to look for pool " + pool.getId() + " for storage, totalSize: " + pool.getCapacityBytes() + ", usedBytes: " + stats.getByteUsed() + ", usedPct: " + usedPercentage + ", disable threshold: " + _storageUsedThreshold);
				}
				if (usedPercentage >= _storageUsedThreshold) {
					if (s_logger.isDebugEnabled()) {
						s_logger.debug("Cannot allocate this pool " + pool.getId() + " for storage since its usage percentage: " +usedPercentage + " has crossed the pool.storage.capacity.disablethreshold: " + _storageUsedThreshold + ", skipping this pool");
					}
					return false;
				}
			}
		}

		Pair<Long, Long> sizes = _volumeDao.getCountAndTotalByPool(pool.getId());
		
		long totalAllocatedSize = sizes.second() + sizes.first() * _extraBytesPerVolume;

		// Iterate through all templates on this storage pool
		boolean tmpinstalled = false;
		List<VMTemplateStoragePoolVO> templatePoolVOs;
		if (templatesInPool != null) {
			templatePoolVOs = templatesInPool;
		} else {
			templatePoolVOs = _templatePoolDao.listByPoolId(pool.getId());
		}

		for (VMTemplateStoragePoolVO templatePoolVO : templatePoolVOs) {
			if ((template != null) && !tmpinstalled && (templatePoolVO.getTemplateId() == template.getId())) {
				tmpinstalled = true;
			}
			
			long templateSize = templatePoolVO.getTemplateSize();
			totalAllocatedSize += templateSize + _extraBytesPerVolume;
		}

		if ((template != null) && !tmpinstalled) {
		    // If the template that was passed into this allocator is not installed in the storage pool,
		    // add 3 * (template size on secondary storage) to the running total
		    VMTemplateHostVO templateHostVO = _storageMgr.findVmTemplateHost(template.getId(), pool);

		    if (templateHostVO == null) {
                VMTemplateSwiftVO templateSwiftVO = _swiftMgr.findByTmpltId(template.getId());
                if (templateSwiftVO == null) {
                    s_logger.error("Did not find template downloaded on secondary hosts in zone " + plan.getDataCenterId());
                    return false;
                }
                long templateSize = templateSwiftVO.getPhysicalSize();
                if (templateSize == 0) {
                    templateSize = templateSwiftVO.getSize();
                }
                totalAllocatedSize += (templateSize + _extraBytesPerVolume);
		    } else {
		        long templateSize = templateHostVO.getPhysicalSize();
		        if ( templateSize == 0 ){
		            templateSize = templateHostVO.getSize();
		        }
		        totalAllocatedSize +=  (templateSize + _extraBytesPerVolume);
		    }
		}

		long askingSize = dskCh.getSize();
		
		long totalOverProvCapacity;
        if (pool.getPoolType() == StoragePoolType.NetworkFilesystem) {
            totalOverProvCapacity = _storageOverprovisioningFactor.multiply(new BigDecimal(pool.getCapacityBytes())).longValue();// All this for the inaccuracy of floats for big number multiplication.
        }else {
            totalOverProvCapacity = pool.getCapacityBytes();
        }

		if (s_logger.isDebugEnabled()) {
			s_logger.debug("Attempting to look for pool " + pool.getId() + " for storage, maxSize : " + totalOverProvCapacity + ", totalAllocatedSize : " + totalAllocatedSize + ", askingSize : " + askingSize + ", allocated disable threshold: " + _storageAllocatedThreshold);
		}

		double usedPercentage = (totalAllocatedSize + askingSize) / (double)(totalOverProvCapacity);
		if (usedPercentage > _storageAllocatedThreshold){
			if (s_logger.isDebugEnabled()) {
				s_logger.debug("Cannot allocate this pool " + pool.getId() + " for storage since its allocated percentage: " +usedPercentage + " has crossed the allocated pool.storage.allocated.capacity.disablethreshold: " + _storageAllocatedThreshold + ", skipping this pool");
			}
			return false;
		}

		if (totalOverProvCapacity < (totalAllocatedSize + askingSize)) {
			if (s_logger.isDebugEnabled()) {
				s_logger.debug("Cannot allocate this pool " + pool.getId() + " for storage, not enough storage, maxSize : " + totalOverProvCapacity + ", totalAllocatedSize : " + totalAllocatedSize + ", askingSize : " + askingSize);
			}

			return false;
		}

		return true;
	}
	
	@Override
	public String chooseStorageIp(VirtualMachine vm, Host host, Host storage) {
		return storage.getStorageIpAddress();
	}
	
	
	@Override
	public List<StoragePool> allocateToPool(DiskProfile dskCh, VirtualMachineProfile<? extends VirtualMachine> vmProfile, long dcId, long podId, Long clusterId, Set<? extends StoragePool> avoids, int returnUpTo) {
	    
	    ExcludeList avoid = new ExcludeList();
	    for(StoragePool pool : avoids){
	    	avoid.addPool(pool.getId());
	    }
	    
	    DataCenterDeployment plan = new DataCenterDeployment(dcId, podId, clusterId, null, null, null);
	    return allocateToPool(dskCh, vmProfile, plan, avoid, returnUpTo);
	}

}
