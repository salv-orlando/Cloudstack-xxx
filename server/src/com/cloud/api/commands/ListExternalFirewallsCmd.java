/**
 * *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
*
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
import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.PlugService;
import com.cloud.api.response.ListResponse;
import com.cloud.host.Host;
import com.cloud.network.element.JuniperSRXFirewallElementService;
import com.cloud.server.api.response.ExternalFirewallResponse;

@Implementation(description="List external firewall appliances.", responseObject = ExternalFirewallResponse.class)
public class ListExternalFirewallsCmd extends BaseListCmd {
	public static final Logger s_logger = Logger.getLogger(ListServiceOfferingsCmd.class.getName());
    private static final String s_name = "listexternalfirewallsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @IdentityMapper(entityTableName="data_center")
    @Parameter(name=ApiConstants.ZONE_ID, type=CommandType.LONG, required = true, description="zone Id")
    private long zoneId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public long getZoneId() {
        return zoneId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @PlugService JuniperSRXFirewallElementService _srxElementService;

    @Override
    public String getCommandName() {
        return s_name;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(){

    	List<? extends Host> externalFirewalls = _srxElementService.listExternalFirewalls(this);

        ListResponse<ExternalFirewallResponse> listResponse = new ListResponse<ExternalFirewallResponse>();
        List<ExternalFirewallResponse> responses = new ArrayList<ExternalFirewallResponse>();
        for (Host externalFirewall : externalFirewalls) {
        	ExternalFirewallResponse response = _srxElementService.createExternalFirewallResponse(externalFirewall);
        	response.setObjectName("externalfirewall");
        	response.setResponseName(getCommandName());
        	responses.add(response);
        }

        listResponse.setResponses(responses);
        listResponse.setResponseName(getCommandName());
        this.setResponseObject(listResponse);
    }
}
