package com.cloud.hypervisor.xen.resource;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import java.util.concurrent.ConcurrentHashMap;
import com.cloud.utils.Pair;
import com.cloud.vm.VirtualMachine.State;

 
public class XenServerPoolVms {
    private static final Logger s_logger = Logger.getLogger(XenServerPoolVms.class);
    private final Map<String/* clusterId */, HashMap<String/* vm name */, Pair<String/* host uuid */, State/* vm state */>>> _cluster_vms =
         new ConcurrentHashMap<String, HashMap<String, Pair<String, State>>>();


    public HashMap<String, Pair<String, State>> getClusterVmState(String clusterId){
        HashMap<String, Pair<String, State>> _vms= _cluster_vms.get(clusterId);
        if (_vms==null) {
            HashMap<String, Pair<String, State>> vmStates =  new HashMap<String, Pair<String, State>>();
            _cluster_vms.put(clusterId, vmStates);
            return vmStates;
        }
        else return _vms;
    }
    
    public void clear(String clusterId){
        HashMap<String, Pair<String, State>> _vms= getClusterVmState(clusterId);
        _vms.clear();
    }
    
    public State getState(String clusterId, String name){
        HashMap<String, Pair<String, State>> vms = getClusterVmState(clusterId);
        Pair<String, State> pv = vms.get(name);
        return pv == null ? State.Stopped : pv.second(); // if a VM is absent on the cluster, it is effectively in stopped state.
    }

    public void put(String clusterId, String hostUuid, String name, State state){
        HashMap<String, Pair<String, State>> vms= getClusterVmState(clusterId);
        vms.put(name, new Pair<String, State>(hostUuid, state));
    }
    
    public void remove(String clusterId, String hostUuid, String name){
        HashMap<String, Pair<String, State>> vms= getClusterVmState(clusterId);
        vms.remove(name);
    }
    
    public void putAll(String clusterId, HashMap<String, Pair<String, State>> new_vms){
        HashMap<String, Pair<String, State>> vms= getClusterVmState(clusterId);
        vms.putAll(new_vms);
    }
    
    public int size(String clusterId){
        HashMap<String, Pair<String, State>> vms= getClusterVmState(clusterId);
        return vms.size();
    }
    
    @Override
    public String toString(){
        StringBuilder sbuf = new StringBuilder("PoolVms=");
        for (HashMap<String/* vm name */, Pair<String/* host uuid */, State/* vm state */>>  clusterVM: _cluster_vms.values()){
            for (String vmname: clusterVM.keySet()){
                sbuf.append(vmname).append("-").append(clusterVM.get(vmname).second()).append(",");
            }
        }
        return sbuf.toString();
    }
    
}

