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

import com.cloud.api.ApiConstants;
import com.cloud.utils.IdentityProxy;
import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

public class UpgradeVmResponse extends BaseResponse {
	@SerializedName("id")
    private IdentityProxy id = new IdentityProxy("vm_instance");

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Long getDomainId() {
        return domainId.getValue();
    }

    public void setDomainId(Long domainId) {
        this.domainId.setValue(domainId);
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isHaEnable() {
        return haEnable;
    }

    public void setHaEnable(boolean haEnable) {
        this.haEnable = haEnable;
    }

    public Long getZoneId() {
        return zoneId.getValue();
    }

    public void setZoneId(Long zoneId) {
        this.zoneId.setValue(zoneId);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public Long getHostId() {
        return hostId.getValue();
    }

    public void setHostId(Long hostId) {
        this.hostId.setValue(hostId);
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Long getTemplateId() {
        return templateId.getValue();
    }

    public void setTemplateId(Long templateId) {
        this.templateId.setValue(templateId);
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateDisplayText() {
        return templateDisplayText;
    }

    public void setTemplateDisplayText(String templateDisplayText) {
        this.templateDisplayText = templateDisplayText;
    }

    public boolean isPasswordEnabled() {
        return passwordEnabled;
    }

    public void setPasswordEnabled(boolean passwordEnabled) {
        this.passwordEnabled = passwordEnabled;
    }

    public Long getServiceOfferingId() {
        return serviceOfferingId.getValue();
    }

    public void setServiceOfferingId(Long serviceOfferingId) {
        this.serviceOfferingId.setValue(serviceOfferingId);
    }

    public String getServiceOfferingName() {
        return serviceOfferingName;
    }

    public void setServiceOfferingName(String serviceOfferingName) {
        this.serviceOfferingName = serviceOfferingName;
    }

    public long getCpuSpeed() {
        return cpuSpeed;
    }

    public void setCpuSpeed(long cpuSpeed) {
        this.cpuSpeed = cpuSpeed;
    }

    public long getMemory() {
        return memory;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public long getCpuUsed() {
        return cpuUsed;
    }

    public void setCpuUsed(long cpuUsed) {
        this.cpuUsed = cpuUsed;
    }

    public long getNetworkKbsRead() {
        return networkKbsRead;
    }

    public void setNetworkKbsRead(long networkKbsRead) {
        this.networkKbsRead = networkKbsRead;
    }

    public long getNetworkKbsWrite() {
        return networkKbsWrite;
    }

    public void setNetworkKbsWrite(long networkKbsWrite) {
        this.networkKbsWrite = networkKbsWrite;
    }

    public Long isId() {
        return id.getValue();
    }

    @SerializedName("name") @Param(description="the ID of the virtual machine")
    private String name;

    @SerializedName("created") @Param(description="the date when this virtual machine was created")
    private Date created;

    @SerializedName("ipaddress") @Param(description="the ip address of the virtual machine")
    private String ipAddress;

    @SerializedName("state") @Param(description="the state of the virtual machine")
    private String state;

    @SerializedName("account") @Param(description="the account associated with the virtual machine")
    private String account;

    @SerializedName("domainid") @Param(description="the ID of the domain in which the virtual machine exists")
    private IdentityProxy domainId = new IdentityProxy("domain");

    @SerializedName("domain") @Param(description="the name of the domain in which the virtual machine exists")
    private String domain;

    @SerializedName("haenable") @Param(description="true if high-availability is enabled, false otherwise")
    private boolean haEnable;

    @SerializedName("zoneid") @Param(description="the ID of the availablility zone for the virtual machine")
    private IdentityProxy zoneId = new IdentityProxy("data_center");

    @SerializedName("displayname") @Param(description="user generated name. The name of the virtual machine is returned if no displayname exists.")
    private String displayName;

    @SerializedName(ApiConstants.ZONE_NAME) @Param(description="the name of the availability zone for the virtual machine")
    private String zoneName;

    @SerializedName("hostid") @Param(description="the ID of the host for the virtual machine")
    private IdentityProxy hostId = new IdentityProxy("host");

    @SerializedName("hostname") @Param(description="the name of the host for the virtual machine")
    private String hostName;

    @SerializedName("templateid") @Param(description="the ID of the template for the virtual machine. A -1 is returned if the virtual machine was created from an ISO file.")
    private IdentityProxy templateId = new IdentityProxy("vm_template");

    @SerializedName("templatename") @Param(description="the name of the template for the virtual machine")
    private String templateName;

    @SerializedName("templatedisplaytext") @Param(description="	an alternate display text of the template for the virtual machine")
    private String templateDisplayText;

    @SerializedName("passwordenabled") @Param(description="true if the password rest feature is enabled, false otherwise")
    private boolean passwordEnabled;

    @SerializedName("serviceofferingid") @Param(description="the ID of the service offering of the virtual machine")
    private IdentityProxy serviceOfferingId = new IdentityProxy("disk_offering");

    @SerializedName("serviceofferingname") @Param(description="the name of the service offering of the virtual machine")
    private String serviceOfferingName;

    @SerializedName("cpunumber") @Param(description="the number of cpu this virtual machine is running with")
    private long cpuSpeed;

    @SerializedName("memory")  @Param(description="the memory allocated for the virtual machine")
    private long memory;

    @SerializedName("cpuused") @Param(description="the amount of the vm's CPU currently used")
    private long cpuUsed;

    @SerializedName("networkkbsread") @Param(description="the incoming network traffic on the vm")
    private long networkKbsRead;

    @SerializedName("networkkbswrite") @Param(description="the outgoing network traffic on the host")
    private long networkKbsWrite;
}
