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
package com.cloud.resource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.configuration.Config;
import com.cloud.configuration.dao.ConfigurationDao;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.network.NetworkManager;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;
import com.cloud.utils.net.UrlUtil;

public abstract class DiscovererBase implements Discoverer {
    protected String _name;
    protected Map<String, String> _params;
    private static final Logger s_logger = Logger.getLogger(DiscovererBase.class);
    @Inject protected ClusterDao _clusterDao;
    @Inject protected ConfigurationDao _configDao;
    @Inject protected NetworkManager _networkMgr;
    @Inject protected HostDao _hostDao;
    
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        ConfigurationDao dao = ComponentLocator.getCurrentLocator().getDao(ConfigurationDao.class);
        _params = dao.getConfiguration(params);
        _name = name;
        
        return true;
    }
    
    protected Map<String, String> resolveInputParameters(URL url) {
        Map<String, String> params = UrlUtil.parseQueryParameters(url);
        
        return null;
    }
    
    @Override
    public void putParam(Map<String, String> params) {
    	if (_params == null) {
    		_params = new HashMap<String, String>();
    	}
    	_params.putAll(params);
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }
    
    protected ServerResource getResource(String resourceName){
        ServerResource resource = null;
        try {
            Class<?> clazz = Class.forName(resourceName);
            Constructor constructor = clazz.getConstructor();
            resource = (ServerResource) constructor.newInstance();
        } catch (ClassNotFoundException e) {
            s_logger.warn("Unable to find class " + resourceName, e);
        } catch (InstantiationException e) {
            s_logger.warn("Unablet to instantiate class " + resourceName, e);
        } catch (IllegalAccessException e) {
            s_logger.warn("Illegal access " + resourceName, e);
        } catch (SecurityException e) {
            s_logger.warn("Security error on " + resourceName, e);
        } catch (NoSuchMethodException e) {
            s_logger.warn("NoSuchMethodException error on " + resourceName, e);
        } catch (IllegalArgumentException e) {
            s_logger.warn("IllegalArgumentException error on " + resourceName, e);
        } catch (InvocationTargetException e) {
            s_logger.warn("InvocationTargetException error on " + resourceName, e);
        }
        
        return resource;
    }
    
    protected HashMap<String, Object> buildConfigParams(HostVO host){
        HashMap<String, Object> params = new HashMap<String, Object>(host.getDetails().size() + 5);
        params.putAll(host.getDetails());

        params.put("guid", host.getGuid());
        params.put("zone", Long.toString(host.getDataCenterId()));
        if (host.getPodId() != null) {
            params.put("pod", Long.toString(host.getPodId()));
        }
        if (host.getClusterId() != null) {
            params.put("cluster", Long.toString(host.getClusterId()));
            String guid = null;
            ClusterVO cluster = _clusterDao.findById(host.getClusterId());
            if (cluster.getGuid() == null) {
                guid = host.getDetail("pool");
            } else {
                guid = cluster.getGuid();
            }
            if (guid != null && !guid.isEmpty()) {
                params.put("pool", guid);
            }
        }

        params.put("ipaddress", host.getPrivateIpAddress());
        params.put("secondary.storage.vm", "false");
        params.put("max.template.iso.size", _configDao.getValue(Config.MaxTemplateAndIsoSize.toString()));
        params.put("migratewait", _configDao.getValue(Config.MigrateWait.toString()));
        return params;

    }
    
    @Override
    public ServerResource reloadResource(HostVO host) {
        String resourceName = host.getResource();
        ServerResource resource = getResource(resourceName);
        
        if(resource != null){
            _hostDao.loadDetails(host);
            updateNetworkLabels(host);
            
            HashMap<String, Object> params = buildConfigParams(host);
            try {
                resource.configure(host.getName(), params);
            } catch (ConfigurationException e) {
                s_logger.warn("Unable to configure resource due to " + e.getMessage());
                return null;
            }
            if (!resource.start()) {
                s_logger.warn("Unable to start the resource");
                return null;
            }
        }
        return resource;
    }
    
    private void updateNetworkLabels(HostVO host){
        //check if networkLabels need to be updated in details
        //we send only private and storage network label to the resource.
        String privateNetworkLabel = _networkMgr.getDefaultManagementTrafficLabel(host.getDataCenterId(), host.getHypervisorType());
        String storageNetworkLabel = _networkMgr.getDefaultStorageTrafficLabel(host.getDataCenterId(), host.getHypervisorType());
        
        String privateDevice = host.getDetail("private.network.device");
        String storageDevice = host.getDetail("storage.network.device1");
        
        boolean update = false;
        
        if(privateNetworkLabel != null && !privateNetworkLabel.equalsIgnoreCase(privateDevice)){
            host.setDetail("private.network.device", privateNetworkLabel);
            update = true;
        }
        if(storageNetworkLabel != null && !storageNetworkLabel.equalsIgnoreCase(storageDevice)){
            host.setDetail("storage.network.device1", storageNetworkLabel);
            update = true;
        }
        if(update){
            _hostDao.saveDetails(host);
        }
    }

}
