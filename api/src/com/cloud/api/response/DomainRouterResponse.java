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
import com.cloud.vm.VirtualMachine.State;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class DomainRouterResponse extends BaseResponse implements ControlledEntityResponse{
    @SerializedName(ApiConstants.ID) @Param(description="the id of the router")
    private IdentityProxy id = new IdentityProxy("vm_instance");
 
    @SerializedName(ApiConstants.ZONE_ID) @Param(description="the Zone ID for the router")
    private IdentityProxy zoneId = new IdentityProxy("data_center");

    @SerializedName(ApiConstants.ZONE_NAME) @Param(description="the Zone name for the router")
    private String zoneName; 

    @SerializedName(ApiConstants.DNS1) @Param(description="the first DNS for the router")
    private String dns1;

    @SerializedName(ApiConstants.DNS2) @Param(description="the second DNS for the router")
    private String dns2;

    @SerializedName("networkdomain") @Param(description="the network domain for the router")
    private String networkDomain;

    @SerializedName(ApiConstants.GATEWAY) @Param(description="the gateway for the router")
    private String gateway;

    @SerializedName(ApiConstants.NAME) @Param(description="the name of the router")
    private String name;

    @SerializedName(ApiConstants.POD_ID) @Param(description="the Pod ID for the router")
    private IdentityProxy podId = new IdentityProxy("host_pod_ref");

    @SerializedName(ApiConstants.HOST_ID) @Param(description="the host ID for the router")
    private IdentityProxy hostId = new IdentityProxy("host");

    @SerializedName("hostname") @Param(description="the hostname for the router")
    private String hostName;

    @SerializedName(ApiConstants.LINK_LOCAL_IP) @Param(description="the link local IP address for the router")
    private String linkLocalIp;

    @SerializedName(ApiConstants.LINK_LOCAL_MAC_ADDRESS) @Param(description="the link local MAC address for the router")
    private String linkLocalMacAddress;

    @SerializedName(ApiConstants.LINK_LOCAL_MAC_NETMASK) @Param(description="the link local netmask for the router")
    private String linkLocalNetmask;
    
    @SerializedName(ApiConstants.LINK_LOCAL_NETWORK_ID) @Param(description="the ID of the corresponding link local network")
    private IdentityProxy linkLocalNetworkId = new IdentityProxy("networks");

    @SerializedName(ApiConstants.PUBLIC_IP) @Param(description="the public IP address for the router")
    private String publicIp;

    @SerializedName("publicmacaddress") @Param(description="the public MAC address for the router")
    private String publicMacAddress;

    @SerializedName("publicnetmask") @Param(description="the public netmask for the router")
    private String publicNetmask;
    
    @SerializedName("publicnetworkid") @Param(description="the ID of the corresponding public network")
    private IdentityProxy publicNetworkId = new IdentityProxy("networks");

    @SerializedName("guestipaddress") @Param(description="the guest IP address for the router")
    private String guestIpAddress;

    @SerializedName("guestmacaddress") @Param(description="the guest MAC address for the router")
    private String guestMacAddress;

    @SerializedName("guestnetmask") @Param(description="the guest netmask for the router")
    private String guestNetmask;
    
    @SerializedName("guestnetworkid") @Param(description="the ID of the corresponding guest network")
    private IdentityProxy guestNetworkId = new IdentityProxy("networks");

    @SerializedName(ApiConstants.TEMPLATE_ID) @Param(description="the template ID for the router")
    private IdentityProxy templateId = new IdentityProxy("vm_template");

    @SerializedName(ApiConstants.CREATED) @Param(description="the date and time the router was created")
    private Date created;

    @SerializedName(ApiConstants.STATE) @Param(description="the state of the router")
    private State state;

    @SerializedName(ApiConstants.ACCOUNT) @Param(description="the account associated with the router")
    private String accountName;
    
    @SerializedName(ApiConstants.PROJECT_ID) @Param(description="the project id of the ipaddress")
    private IdentityProxy projectId = new IdentityProxy("projects");
    
    @SerializedName(ApiConstants.PROJECT) @Param(description="the project name of the address")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID) @Param(description="the domain ID associated with the router")
    private IdentityProxy domainId = new IdentityProxy("domain");

    @SerializedName(ApiConstants.DOMAIN) @Param(description="the domain associated with the router")
    private String domainName;
    
    @SerializedName(ApiConstants.SERVICE_OFFERING_ID) @Param(description="the ID of the service offering of the virtual machine")
    private IdentityProxy serviceOfferingId = new IdentityProxy("disk_offering");

    @SerializedName("serviceofferingname") @Param(description="the name of the service offering of the virtual machine")
    private String serviceOfferingName;
    
    @SerializedName("isredundantrouter") @Param(description="if this router is an redundant virtual router")
    private boolean isRedundantRouter;
    
    @SerializedName("redundantstate") @Param(description="the state of redundant virtual router")
    private String redundantState;
    
    @SerializedName("templateversion") @Param(description="the version of template")
    private String templateVersion;
    
    @SerializedName("scriptsversion") @Param(description="the version of scripts")
    private String scriptsVersion;
    
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

    public void setZoneId(Long zoneId) {
        this.zoneId.setValue(zoneId);
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public void setDns1(String dns1) {
        this.dns1 = dns1;
    }

    public void setDns2(String dns2) {
        this.dns2 = dns2;
    }

    public void setNetworkDomain(String networkDomain) {
        this.networkDomain = networkDomain;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPodId(Long podId) {
        this.podId.setValue(podId);
    }

    public void setHostId(Long hostId) {
        this.hostId.setValue(hostId);
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public void setPublicMacAddress(String publicMacAddress) {
        this.publicMacAddress = publicMacAddress;
    }

    public void setPublicNetmask(String publicNetmask) {
        this.publicNetmask = publicNetmask;
    }

    public void setGuestIpAddress(String guestIpAddress) {
        this.guestIpAddress = guestIpAddress;
    }

    public void setGuestMacAddress(String guestMacAddress) {
        this.guestMacAddress = guestMacAddress;
    }

    public void setGuestNetmask(String guestNetmask) {
        this.guestNetmask = guestNetmask;
    }

    public void setTemplateId(Long templateId) {
        this.templateId.setValue(templateId);
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @Override
    public void setDomainId(Long domainId) {
        this.domainId.setValue(domainId);
    }

    @Override
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public void setPublicNetworkId(Long publicNetworkId) {
        this.publicNetworkId.setValue(publicNetworkId);
    }

    public void setGuestNetworkId(Long guestNetworkId) {
        this.guestNetworkId.setValue(guestNetworkId);
    }

    public void setLinkLocalIp(String linkLocalIp) {
        this.linkLocalIp = linkLocalIp;
    }
    
    public void setLinkLocalMacAddress(String linkLocalMacAddress) {
        this.linkLocalMacAddress = linkLocalMacAddress;
    }

    public void setLinkLocalNetmask(String linkLocalNetmask) {
        this.linkLocalNetmask = linkLocalNetmask;
    }

    public void setLinkLocalNetworkId(Long linkLocalNetworkId) {
        this.linkLocalNetworkId.setValue(linkLocalNetworkId);
    }

    public void setServiceOfferingId(Long serviceOfferingId) {
        this.serviceOfferingId.setValue(serviceOfferingId);
    }

    public void setServiceOfferingName(String serviceOfferingName) {
        this.serviceOfferingName = serviceOfferingName;
    }

    public void setRedundantState(String redundantState) {
        this.redundantState = redundantState;
    }

    public void setIsRedundantRouter(boolean isRedundantRouter) {
        this.isRedundantRouter = isRedundantRouter;
    }

    public String getTemplateVersion() {
        return this.templateVersion;
    }
    
    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }
    
    public String getScriptsVersion() {
        return this.scriptsVersion;
    }
    
    public void setScriptsVersion(String scriptsVersion) {
        this.scriptsVersion = scriptsVersion;
    }
    @Override
    public void setProjectId(Long projectId) {
        this.projectId.setValue(projectId);
    }

    @Override
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
