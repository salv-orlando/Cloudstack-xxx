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

import com.cloud.api.ApiConstants;
import com.cloud.utils.IdentityProxy;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

public class HypervisorCapabilitiesResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID) @Param(description="the ID of the hypervisor capabilities row")
    private IdentityProxy id = new IdentityProxy("hypervisor_capabilities");

    @SerializedName(ApiConstants.HYPERVISOR_VERSION) @Param(description="the hypervisor version")
    private String hypervisorVersion;

    @SerializedName(ApiConstants.HYPERVISOR) @Param(description="the hypervisor type")
    private HypervisorType hypervisor;

    @SerializedName(ApiConstants.MAX_GUESTS_LIMIT) @Param(description="the maximum number of guest vms recommended for this hypervisor")
    private Long maxGuestsLimit;

    @SerializedName(ApiConstants.SECURITY_GROUP_EANBLED) @Param(description="true if security group is supported")
    private boolean isSecurityGroupEnabled;

    @Override
    public Long getObjectId() {
        return getId();
    }

    public Long getId() {
        return id.getValue();
    }

    public void setId(Long id) {
        this.id.setValue(id);
    }


    public String getHypervisorVersion() {
        return hypervisorVersion;
    }

    public void setHypervisorVersion(String hypervisorVersion) {
        this.hypervisorVersion = hypervisorVersion;
    }

    public HypervisorType getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(HypervisorType hypervisor) {
        this.hypervisor = hypervisor;
    }

    public Long getMaxGuestsLimit() {
        return maxGuestsLimit;
    }

    public void setMaxGuestsLimit(Long maxGuestsLimit) {
        this.maxGuestsLimit = maxGuestsLimit;
    }

    public Boolean getIsSecurityGroupEnabled() {
        return this.isSecurityGroupEnabled;
    }

    public void setIsSecurityGroupEnabled(Boolean sgEnabled) {
        this.isSecurityGroupEnabled = sgEnabled;
    }
}
