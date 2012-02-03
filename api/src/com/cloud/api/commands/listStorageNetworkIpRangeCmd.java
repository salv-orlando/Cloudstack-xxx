package com.cloud.api.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseCmd;
import com.cloud.api.BaseListCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.StorageNetworkIpRangeResponse;
import com.cloud.dc.StorageNetworkIpRange;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;

@Implementation(description="List a storage network IP range.", responseObject=StorageNetworkIpRangeResponse.class, since="3.0.0")
public class listStorageNetworkIpRangeCmd extends BaseListCmd {
	public static final Logger s_logger = Logger.getLogger(listStorageNetworkIpRangeCmd.class);
	
	String s_name = "liststoragenetworkiprangeresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////   
	
    @IdentityMapper(entityTableName="dc_storage_network_ip_range")
    @Parameter(name=ApiConstants.ID, type=CommandType.LONG, description="optional parameter. Storaget network IP range uuid, if specicied, using it to search the range.")
    private Long rangeId;
    
    @IdentityMapper(entityTableName="host_pod_ref")
    @Parameter(name=ApiConstants.POD_ID, type=CommandType.LONG, description="optional parameter. Pod uuid, if specicied and range uuid is absent, using it to search the range.")
    private Long podId;
    
    @IdentityMapper(entityTableName="data_center")
    @Parameter(name=ApiConstants.ZONE_ID, type=CommandType.LONG, description="optional parameter. Zone uuid, if specicied and both pod uuid and range uuid are absent, using it to search the range.")
    private Long zoneId;
    
    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    
    public Long getRangeId() {
    	return rangeId;
    }
    
    public Long getPodId() {
    	return podId;
    }
    
    public Long getZoneId() {
    	return zoneId;
    }

	@Override
	public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
	        ResourceAllocationException {
		try {
			List<StorageNetworkIpRange> results = _storageNetworkService.listIpRange(this);
			ListResponse<StorageNetworkIpRangeResponse> response = new ListResponse<StorageNetworkIpRangeResponse>();
			List<StorageNetworkIpRangeResponse> resList = new ArrayList<StorageNetworkIpRangeResponse>(results.size());
			for (StorageNetworkIpRange r : results) {
				StorageNetworkIpRangeResponse resp = _responseGenerator.createStorageNetworkIpRangeResponse(r);
				resList.add(resp);
			}
			response.setResponses(resList);
			response.setObjectName(getCommandName());
			this.setResponseObject(response);
		} catch (Exception e) {
			s_logger.warn("Failed to list storage network ip range for rangeId=" + getRangeId() + " podId=" + getPodId() + " zoneId=" + getZoneId());
			throw new ServerApiException(BaseCmd.INTERNAL_ERROR, e.getMessage());
		}
	}

	@Override
	public String getCommandName() {
		return s_name;
	}

	@Override
	public long getEntityOwnerId() {
		return Account.ACCOUNT_ID_SYSTEM;
	}

}
