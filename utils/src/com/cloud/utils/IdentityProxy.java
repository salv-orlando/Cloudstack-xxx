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
package com.cloud.utils;

public class IdentityProxy {
    private String _tableName;
    private Long _value;
    private String _idFieldName;

    public IdentityProxy() {
    }

    public IdentityProxy(String tableName) {
        _tableName = tableName;
    }

    public IdentityProxy(String tableName, Long id, String fieldName) {
    	_tableName = tableName;
    	_value = id;
    	_idFieldName = fieldName;
    }
    
    public String getTableName() {
        return _tableName;
    }

    public void setTableName(String tableName) {
        _tableName = tableName;
    }

    public Long getValue() {
        return _value;
    }

    public void setValue(Long value) {
        _value = value;
    }
    
    public void setidFieldName(String value) {
    	_idFieldName = value;
    }
    
    public String getidFieldName() {
    	return _idFieldName;
    }
}
