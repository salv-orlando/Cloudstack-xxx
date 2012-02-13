/**
 *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved.
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

package com.cloud.deploy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.utils.Pair;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;

@Local(value=DeploymentPlanner.class)
public class UserConcentratedPodPlanner extends FirstFitPlanner implements DeploymentPlanner {

    private static final Logger s_logger = Logger.getLogger(UserConcentratedPodPlanner.class);
    
    /**
     * This method should reorder the given list of Cluster Ids by applying any necessary heuristic 
     * for this planner
     * For UserConcentratedPodPlanner we need to order the clusters in a zone across pods, by considering those pods first which have more number of VMs for this account
     * This reordering is not done incase the clusters within single pod are passed when the allocation is applied at pod-level.
     * @return List<Long> ordered list of Cluster Ids
     */
    @Override
    protected List<Long> reorderClusters(long id, boolean isZone, Pair<List<Long>, Map<Long, Double>> clusterCapacityInfo, VirtualMachineProfile<? extends VirtualMachine> vmProfile, DeploymentPlan plan){
        List<Long> clusterIdsByCapacity = clusterCapacityInfo.first();
        if(vmProfile.getOwner() == null || !isZone){
            return clusterIdsByCapacity;
        }
        return applyUserConcentrationPodHeuristicToClusters(id, clusterIdsByCapacity, vmProfile.getOwner().getAccountId());
    }
    
    private List<Long> applyUserConcentrationPodHeuristicToClusters(long zoneId, List<Long> prioritizedClusterIds, long accountId){
        //user has VMs in certain pods. - prioritize those pods first
        //UserConcentratedPod strategy
        List<Long> clusterList = new ArrayList<Long>();
        List<Long> podIds = listPodsByUserConcentration(zoneId, accountId);
        if(!podIds.isEmpty()){
            clusterList = reorderClustersByPods(prioritizedClusterIds, podIds);
        }else{
            clusterList = prioritizedClusterIds;
        }
        return clusterList;
    }    
    
    private List<Long> reorderClustersByPods(List<Long> clusterIds, List<Long> podIds) {

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Reordering cluster list as per pods ordered by user concentration");
        }

        Map<Long, List<Long>> podClusterMap = _clusterDao.getPodClusterIdMap(clusterIds);

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Pod To cluster Map is: "+podClusterMap );
        }

        List<Long> reorderedClusters = new ArrayList<Long>();
        for (Long pod : podIds){
            if(podClusterMap.containsKey(pod)){
                List<Long> clustersOfThisPod = podClusterMap.get(pod);
                if(clustersOfThisPod != null){
                    for(Long clusterId : clusterIds){
                        if(clustersOfThisPod.contains(clusterId)){
                            reorderedClusters.add(clusterId);
                        }
                    }
                    clusterIds.removeAll(clustersOfThisPod);
                }
            }
        }
        reorderedClusters.addAll(clusterIds);

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Reordered cluster list: " + reorderedClusters);
        }
        return reorderedClusters;
    }

    protected List<Long> listPodsByUserConcentration(long zoneId, long accountId){

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Applying UserConcentratedPod heuristic for account: "+ accountId);
        }

        List<Long> prioritizedPods = _vmDao.listPodIdsHavingVmsforAccount(zoneId, accountId);

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("List of pods to be considered, after applying UserConcentratedPod heuristic: "+ prioritizedPods);
        }

        return prioritizedPods;
    }
    
    /**
     * This method should reorder the given list of Pod Ids by applying any necessary heuristic 
     * for this planner
     * For UserConcentratedPodPlanner we need to order the pods by considering those pods first which have more number of VMs for this account 
     * @return List<Long> ordered list of Pod Ids
     */
    @Override
    protected List<Long> reorderPods(Pair<List<Long>, Map<Long, Double>> podCapacityInfo, VirtualMachineProfile<? extends VirtualMachine> vmProfile, DeploymentPlan plan){
        List<Long> podIdsByCapacity = podCapacityInfo.first();
        if(vmProfile.getOwner() == null){
            return podIdsByCapacity;
        }
        long accountId = vmProfile.getOwner().getAccountId(); 

        //user has VMs in certain pods. - prioritize those pods first
        //UserConcentratedPod strategy
        List<Long> podIds = listPodsByUserConcentration(plan.getDataCenterId(), accountId);
        if(!podIds.isEmpty()){
            //remove pods that dont have capacity for this vm
            podIds.retainAll(podIdsByCapacity);
            podIdsByCapacity.removeAll(podIds);
            podIds.addAll(podIdsByCapacity);
            return podIds;
        }else{
            return podIdsByCapacity;
        }
        
    }

    @Override
    public boolean canHandle(VirtualMachineProfile<? extends VirtualMachine> vm, DeploymentPlan plan, ExcludeList avoid) {
        if(vm.getHypervisorType() != HypervisorType.BareMetal){
            //check the allocation strategy
            if (_allocationAlgorithm != null && (_allocationAlgorithm.equals(AllocationAlgorithm.userconcentratedpod_random.toString()) || _allocationAlgorithm.equals(AllocationAlgorithm.userconcentratedpod_firstfit.toString()))){
                return true;
            }
        }
        return false;
    }

}
