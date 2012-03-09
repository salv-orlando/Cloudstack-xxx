/**
 * Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
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
package com.cloud.exception;

import com.cloud.utils.IdentityProxy;
import java.util.ArrayList;
import com.cloud.utils.exception.CSExceptionErrorCode;

/**
 * CloudException is a generic exception class that has an IdentityProxy
 * object in it to enable on the fly conversion of database ids to uuids
 * by the API response serializer. Any exceptions that are thrown by
 * command invocations must extend this class, or the RuntimeCloudException
 * class, which extends RuntimeException instead of Exception like this
 * class does. 
 */

public class CloudException extends Exception {
    
	// This holds a list of uuids and their names. Add uuid:fieldname pairs
	// if required, to this list before throwing an exception, using addProxyObject().
	protected ArrayList<IdentityProxy> idList = new ArrayList<IdentityProxy>();
	
	// This holds an error code. Set this before throwing an exception, if applicable.
	protected Integer csErrorCode;	
		
	public CloudException(String message) {
		super(message);
		setCSErrorCode(CSExceptionErrorCode.getCSErrCode(this.getClass().getName()));
	}

    public CloudException(String message, Throwable cause) {
        super(message, cause);
        setCSErrorCode(CSExceptionErrorCode.getCSErrCode(this.getClass().getName()));
    }
    
	public CloudException() {
		super();
		setCSErrorCode(CSExceptionErrorCode.getCSErrCode(this.getClass().getName()));
	}
	
	public void addProxyObject(String tableName, Long id, String idFieldName) {
		idList.add(new IdentityProxy(tableName, id, idFieldName));
		return;
	}
	
	public ArrayList<IdentityProxy> getIdProxyList() {
		return idList;
	}
	
	public void setCSErrorCode(int cserrcode) {
		this.csErrorCode = cserrcode;
	}
	
	public int getCSErrorCode() {
		return this.csErrorCode;
	}
}
