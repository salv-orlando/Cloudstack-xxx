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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.server.StatsCollector;
import com.cloud.storage.StoragePool;
import com.cloud.storage.StoragePoolVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.user.Account;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;

@Local(value=StoragePoolAllocator.class)
public class FirstFitStoragePoolAllocator extends AbstractStoragePoolAllocator {
    private static final Logger s_logger = Logger.getLogger(FirstFitStoragePoolAllocator.class);
    protected String _allocationAlgorithm = "random";
    
    @Override
    public boolean allocatorIsCorrectType(DiskProfile dskCh) {
    	return !localStorageAllocationNeeded(dskCh);
    }

    @Override
	public List<StoragePool> allocateToPool(DiskProfile dskCh, VirtualMachineProfile<? extends VirtualMachine> vmProfile, DeploymentPlan plan, ExcludeList avoid, int returnUpTo) {

 
	    VMTemplateVO template = (VMTemplateVO)vmProfile.getTemplate();
	    Account account = null;
	    if(vmProfile.getVirtualMachine() != null){
	    	account = vmProfile.getOwner();
	    }
	    
    	List<StoragePool> suitablePools = new ArrayList<StoragePool>();

    	// Check that the allocator type is correct
        if (!allocatorIsCorrectType(dskCh)) {
        	return suitablePools;
        }
		long dcId = plan.getDataCenterId();
		Long podId = plan.getPodId();
		Long clusterId = plan.getClusterId();

        if(dskCh.getTags() != null && dskCh.getTags().length != 0){
        	s_logger.debug("Looking for pools in dc: " + dcId + "  pod:" + podId + "  cluster:" + clusterId + " having tags:" + Arrays.toString(dskCh.getTags()));
        }else{
        	s_logger.debug("Looking for pools in dc: " + dcId + "  pod:" + podId + "  cluster:" + clusterId);
        }

		List<StoragePoolVO> pools = _storagePoolDao.findPoolsByTags(dcId, podId, clusterId, dskCh.getTags(), null);
        if (pools.size() == 0) {
    		if (s_logger.isDebugEnabled()) {
    			s_logger.debug("No storage pools available for allocation, returning");
    		}
            return suitablePools;
        }
        
        StatsCollector sc = StatsCollector.getInstance();

        //FixMe: We are ignoring userdispersing algorithm when account is null. Find a way to get account ID when VMprofile is null
        if(_allocationAlgorithm.equals("random") || _allocationAlgorithm.equals("userconcentratedpod_random") || (account == null)) {
            // Shuffle this so that we don't check the pools in the same order.
            Collections.shuffle(pools);
        }else if(_allocationAlgorithm.equals("userdispersing")){
            pools = reorderPoolsByNumberOfVolumes(plan, pools, account);
        }
        
    	if (s_logger.isDebugEnabled()) {
            s_logger.debug("FirstFitStoragePoolAllocator has " + pools.size() + " pools to check for allocation");
        }
    	
        for (StoragePoolVO pool: pools) {
        	if(suitablePools.size() == returnUpTo){
        		break;
        	}
        	if (checkPool(avoid, pool, dskCh, template, null, sc, plan)) {
        		suitablePools.add(pool);
        	}
        }
        
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("FirstFitStoragePoolAllocator returning "+suitablePools.size() +" suitable storage pools");
        }
        
        return suitablePools;
	}
    
    private List<StoragePoolVO> reorderPoolsByNumberOfVolumes(DeploymentPlan plan, List<StoragePoolVO> pools, Account account) {
        if(account == null){
            return pools;
        }
        long dcId = plan.getDataCenterId();
        Long podId = plan.getPodId();
        Long clusterId = plan.getClusterId();
            
        List<Long> poolIdsByVolCount = _volumeDao.listPoolIdsByVolumeCount(dcId, podId, clusterId, account.getAccountId());
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("List of pools in ascending order of number of volumes for account id: "+ account.getAccountId() + " is: "+ poolIdsByVolCount);
        }
            
        //now filter the given list of Pools by this ordered list
        Map<Long, StoragePoolVO> poolMap = new HashMap<Long, StoragePoolVO>();        
        for (StoragePoolVO pool : pools) {
            poolMap.put(pool.getId(), pool);
        }
        List<Long> matchingPoolIds = new ArrayList<Long>(poolMap.keySet());
        
        poolIdsByVolCount.retainAll(matchingPoolIds);
        
        List<StoragePoolVO> reorderedPools = new ArrayList<StoragePoolVO>();
        for(Long id: poolIdsByVolCount){
            reorderedPools.add(poolMap.get(id));
        }
        
        return reorderedPools;
    }

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        if (_configDao != null) {
            Map<String, String> configs = _configDao.getConfiguration(params);
            String allocationAlgorithm = configs.get("vm.allocation.algorithm");
            if (allocationAlgorithm != null) {
                _allocationAlgorithm = allocationAlgorithm;
            }
        }
        return true;
    }
}
