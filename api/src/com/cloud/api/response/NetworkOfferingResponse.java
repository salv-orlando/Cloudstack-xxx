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

package com.cloud.api.response;

import java.util.Date;
import java.util.List;

import com.cloud.api.ApiConstants;
import com.cloud.utils.IdentityProxy;
import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class NetworkOfferingResponse extends BaseResponse{
    @SerializedName("id") @Param(description="the id of the network offering")
    private final IdentityProxy id = new IdentityProxy("network_offerings");

    @SerializedName(ApiConstants.NAME) @Param(description="the name of the network offering")
    private String name;
    
    @SerializedName(ApiConstants.DISPLAY_TEXT) @Param(description="an alternate display text of the network offering.")
    private String displayText;
    
    @SerializedName(ApiConstants.TAGS) @Param(description="the tags for the network offering")
    private String tags;
    
    @SerializedName(ApiConstants.CREATED) @Param(description="the date this network offering was created")
    private Date created;
    
    @SerializedName(ApiConstants.TRAFFIC_TYPE) @Param(description="the traffic type for the network offering, supported types are Public, Management, Control, Guest, Vlan or Storage.")
    private String trafficType;
    
    @SerializedName(ApiConstants.IS_DEFAULT) @Param(description="true if network offering is default, false otherwise")
    private Boolean isDefault;
   
    @SerializedName(ApiConstants.SPECIFY_VLAN) @Param(description="true if network offering supports vlans, false otherwise")
    private Boolean specifyVlan;
    
    @SerializedName(ApiConstants.CONSERVE_MODE) @Param(description="true if network offering is ip conserve mode enabled")
    private Boolean conserveMode;

    @SerializedName(ApiConstants.SPECIFY_IP_RANGES) @Param(description="true if network offering supports specifying ip ranges, false otherwise")
    private Boolean specifyIpRanges;
    
    @SerializedName(ApiConstants.AVAILABILITY) @Param(description="availability of the network offering")
    private String availability;
    
    @SerializedName(ApiConstants.NETWORKRATE) @Param(description="data transfer rate in megabits per second allowed.")
    private Integer networkRate;

    @SerializedName(ApiConstants.STATE) @Param(description="state of the network offering. Can be Disabled/Enabled/Inactive")
    private String state;
    
    @SerializedName(ApiConstants.GUEST_IP_TYPE) @Param(description="guest type of the network offering, can be Shared or Isolated")
    private String guestIpType;
    
    @SerializedName(ApiConstants.SERVICE_OFFERING_ID) @Param(description="the ID of the service offering used by virtual router provider")
    private IdentityProxy serviceOfferingId = new IdentityProxy("disk_offering");
   
    @SerializedName(ApiConstants.SERVICE) @Param(description="the list of supported services", responseObject = ServiceResponse.class)
    private List<ServiceResponse> services;
    

    public void setId(Long id) {
        this.id.setValue(id);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setTrafficType(String trafficType) {
        this.trafficType = trafficType;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setSpecifyVlan(Boolean specifyVlan) {
        this.specifyVlan = specifyVlan;
    }

    public void setConserveMode(Boolean conserveMode) {
        this.conserveMode = conserveMode;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public void setNetworkRate(Integer networkRate) {
        this.networkRate = networkRate;
    }

    public void setServices(List<ServiceResponse> services) {
        this.services = services;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setGuestIpType(String type) {
        this.guestIpType = type;
    }

    public void setServiceOfferingId(Long serviceOfferingId) {
        this.serviceOfferingId.setValue(serviceOfferingId);
    }

	public void setServiceOfferingId(IdentityProxy serviceOfferingId) {
		this.serviceOfferingId = serviceOfferingId;
	}

	public void setSpecifyIpRanges(Boolean specifyIpRanges) {
		this.specifyIpRanges = specifyIpRanges;
	}
}
