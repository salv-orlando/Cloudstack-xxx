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

package com.cloud.test.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class SignEC2 {
	public static String url;
	public static String secretkey;
	public static String host;
	public static String port;
	public static String command;
	public static String accessPoint;
	public static final Logger s_logger = Logger
	.getLogger(SignRequest.class.getName());
	
	
	public static void main (String[] args) {
		// Parameters
		List<String> argsList = Arrays.asList(args);
		Iterator<String> iter = argsList.iterator();
		while (iter.hasNext()) {
			String arg = iter.next();

			if (arg.equals("-u")) {
				url = iter.next();
			}
		}
		
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream("../conf/tool.properties"));
		} catch (IOException ex) {
			s_logger.error("Error reading from ../conf/tool.properties", ex);
			System.exit(2);
		}
		
		host = prop.getProperty("host");
		secretkey = prop.getProperty("secretkey");
		port = prop.getProperty("port");
		
		
		if (host == null) {
			s_logger.info("Please set host in tool.properties file");
			System.exit(1);
		}
		
		if (port == null) {
			s_logger.info("Please set port in tool.properties file");
			System.exit(1);
		}
		
		if (url == null) {
			s_logger.info("Please specify url with -u option");
			System.exit(1);
		}
		
		if (secretkey == null) {
			s_logger.info("Please set secretkey in tool.properties file");
			System.exit(1);
		}
		
		if (prop.get("apikey") == null) {
			s_logger.info("Please set apikey in tool.properties file");
			System.exit(1);
		}
		
		if (prop.get("accesspoint") == null) {
			s_logger.info("Please set apikey in tool.properties file");
			System.exit(1);
		}
		
		
		
		TreeMap<String, String> param = new TreeMap<String, String>();
		
		String req = "GET\n" + host + ":" + prop.getProperty("port") + "\n/" + prop.getProperty("accesspoint") + "\n";
		String temp = "";
		param.put("AWSAccessKeyId", prop.getProperty("apikey"));
		param.put("Expires",prop.getProperty("expires"));
		param.put("SignatureMethod", "HmacSHA1");
		param.put("SignatureVersion", "2");
		param.put("Version", prop.getProperty("version"));
		param.put("id", "1");
		
		StringTokenizer str1 = new StringTokenizer (url, "&");
		while(str1.hasMoreTokens()) {
			String newEl = str1.nextToken();
			StringTokenizer str2 = new StringTokenizer(newEl, "=");
			String name = str2.nextToken();
			String value= str2.nextToken();
			param.put(name, value);
		}
		
		//sort url hash map by key
		Set c = param.entrySet();
		Iterator it = c.iterator();
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry)it.next();
			String key = (String) me.getKey();
			String value = (String) me.getValue();
			try {
				temp = temp + key + "=" + URLEncoder.encode(value, "UTF-8") + "&";
			} catch (Exception ex) {
				s_logger.error("Unable to set parameter " + value + " for the command " + param.get("command"));
			}
			
		}
		temp = temp.substring(0, temp.length()-1 );
		String requestToSign = req + temp;
		String signature = UtilsForTest.signRequest(requestToSign, secretkey);
		String encodedSignature = "";
		try {
			encodedSignature = URLEncoder.encode(signature, "UTF-8");
		} catch (Exception ex) {
			s_logger.error(ex);
		}
		String url = "http://" + host + ":" + prop.getProperty("port") + "/" + prop.getProperty("accesspoint") + "?" + temp + "&Signature=" + encodedSignature;
		s_logger.info("Url is " + url);
		
	}
}
