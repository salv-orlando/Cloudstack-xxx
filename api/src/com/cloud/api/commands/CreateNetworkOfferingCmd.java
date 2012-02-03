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

package com.cloud.api.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.NetworkOfferingResponse;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.Service;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Availability;
import com.cloud.user.Account;

@Implementation(description="Creates a network offering.", responseObject=NetworkOfferingResponse.class, since="3.0.0")
public class CreateNetworkOfferingCmd extends BaseCmd {
    public static final Logger s_logger = Logger.getLogger(CreateNetworkOfferingCmd.class.getName());
    private static final String _name = "createnetworkofferingresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name=ApiConstants.NAME, type=CommandType.STRING, required=true, description="the name of the network offering")
    private String networkOfferingName;
    
    @Parameter(name=ApiConstants.DISPLAY_TEXT, type=CommandType.STRING, required=true, description="the display text of the network offering")
    private String displayText;
    
    @Parameter(name=ApiConstants.TRAFFIC_TYPE, type=CommandType.STRING, required=true, description="the traffic type for the network offering. Supported type in current release is GUEST only")
    private String traffictype;
    
    @Parameter(name=ApiConstants.TAGS, type=CommandType.STRING, description="the tags for the network offering.", length=4096)
    private String tags; 
    
    @Parameter(name=ApiConstants.SPECIFY_VLAN, type=CommandType.BOOLEAN, description="true if network offering supports vlans")
    private Boolean specifyVlan; 
    
    @Parameter(name=ApiConstants.AVAILABILITY, type=CommandType.STRING, description="the availability of network offering. Default value is Optional")
    private String availability; 
    
    @Parameter(name=ApiConstants.NETWORKRATE, type=CommandType.INTEGER, description="data transfer rate in megabits per second allowed")
    private Integer networkRate;
    
    @Parameter(name=ApiConstants.CONSERVE_MODE, type=CommandType.BOOLEAN, description="true if the network offering is IP conserve mode enabled")
    private Boolean conserveMode;
    
    @IdentityMapper(entityTableName="disk_offering")
    @Parameter(name=ApiConstants.SERVICE_OFFERING_ID, type=CommandType.LONG, description="the service offering ID used by virtual router provider")
    private Long serviceOfferingId;
    
    @Parameter(name=ApiConstants.GUEST_IP_TYPE, type=CommandType.STRING, required=true, description="guest type of the network offering: Shared or Isolated")
    private String guestIptype;
    
    @Parameter(name=ApiConstants.SUPPORTED_SERVICES, type=CommandType.LIST, required=true, collectionType=CommandType.STRING, description="services supported by the network offering")
    private List<String> supportedServices;
    
    @Parameter(name = ApiConstants.SERVICE_PROVIDER_LIST, type = CommandType.MAP, description = "provider to service mapping. If not specified, the provider for the service will be mapped to the default provider on the physical network")
    private Map serviceProviderList;

    @Parameter(name = ApiConstants.SERVICE_CAPABILITY_LIST, type = CommandType.MAP, description = "desired service capabilities as part of network offering")
    private Map serviceCapabilitystList;
    
    @Parameter(name=ApiConstants.SPECIFY_IP_RANGES, type=CommandType.BOOLEAN, description="true if network offering supports specifying ip ranges; defaulted to false if not specified")
    private Boolean specifyIpRanges;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    
    public String getNetworkOfferingName() {
        return networkOfferingName;
    }
    
    public String getDisplayText() {
        return displayText;
    }

    public String getTags() {
        return tags;
    }

    public String getTraffictype() {
        return traffictype;
    }
    
    public Boolean getSpecifyVlan() {
        return specifyVlan == null ? false : specifyVlan;
    }

    public String getAvailability() {
        return availability == null ? Availability.Optional.toString() : availability;
    }
    
    public Integer getNetworkRate() {
        return networkRate;
    }

    public static String getName() {
        return _name;
    }

    public Long getServiceOfferingId() {
        return serviceOfferingId;
    }
    
    public List<String> getSupportedServices() {
		return supportedServices;
	}

	public String getGuestIpType() {
        return guestIptype;
    }
	
	public Boolean getSpecifyIpRanges() {
		return specifyIpRanges == null ? false : specifyIpRanges;
	}

    public Boolean getConserveMode() {
        if (conserveMode == null) {
            return true;
        }
        return conserveMode;
    }

    public Map<String, List<String>> getServiceProviders() {
        Map<String, List<String>> serviceProviderMap = null;
        if (serviceProviderList != null && !serviceProviderList.isEmpty()) {
            serviceProviderMap = new HashMap<String, List<String>>();
            Collection servicesCollection = serviceProviderList.values();
            Iterator iter = servicesCollection.iterator();
            while (iter.hasNext()) {
                HashMap<String, String> services = (HashMap<String, String>) iter.next();
                String service = services.get("service");
                String provider = services.get("provider");
                List<String> providerList = null;
                if (serviceProviderMap.containsKey(service)) {
                    providerList = serviceProviderMap.get(service);
                } else {
                    providerList = new ArrayList<String>();
                }
                providerList.add(provider);
                serviceProviderMap.put(service, providerList);
            }
        }
        
        return serviceProviderMap;
    }

    public Map<Capability, String> getServiceCapabilities(Service service) {
        Map<Capability, String> capabilityMap = null;

        if (serviceCapabilitystList != null && !serviceCapabilitystList.isEmpty()) {
            capabilityMap = new HashMap <Capability, String>();
            Collection serviceCapabilityCollection = serviceCapabilitystList.values();
            Iterator iter = serviceCapabilityCollection.iterator();
            while (iter.hasNext()) {
                HashMap<String, String> svcCapabilityMap = (HashMap<String, String>) iter.next();
                Capability capability = null;
                String svc = (String) svcCapabilityMap.get("service");
                String capabilityName = (String) svcCapabilityMap.get("capabilitytype");
                String capabilityValue = (String) svcCapabilityMap.get("capabilityvalue");

                if (capabilityName != null) {
                    capability = Capability.getCapability(capabilityName);
                }

                if ((capability == null) || (capabilityName == null) || (capabilityValue == null) ) {
                    throw new InvalidParameterValueException("Invalid capability:" + capabilityName + " capability value:" + capabilityValue);
                }

                if (svc.equalsIgnoreCase(service.getName())) {
                    capabilityMap.put(capability, capabilityValue);
                } else {
                	//throw new InvalidParameterValueException("Service is not equal ")
                }
            }
        }

        return capabilityMap;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return _name;
    }
    
    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public void execute(){
        NetworkOffering result = _configService.createNetworkOffering(this);
        if (result != null) {
            NetworkOfferingResponse response = _responseGenerator.createNetworkOfferingResponse(result);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to create network offering");
        }
    }
}
