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

package com.cloud.storage.upload;

import org.apache.log4j.Level;

import com.cloud.agent.api.storage.UploadAnswer;
import com.cloud.agent.api.storage.UploadProgressCommand.RequestType;
import com.cloud.storage.Upload.Status;

public class UploadErrorState extends UploadInactiveState {

	public UploadErrorState(UploadListener ul) {
		super(ul);
	}

	@Override
	public String handleAnswer(UploadAnswer answer) {
		switch (answer.getUploadStatus()) {
		case UPLOAD_IN_PROGRESS:
			getUploadListener().scheduleStatusCheck(RequestType.GET_STATUS);
			return Status.UPLOAD_IN_PROGRESS.toString();
		case UPLOADED:
			getUploadListener().scheduleImmediateStatusCheck(RequestType.PURGE);
			getUploadListener().cancelTimeoutTask();
			return Status.UPLOADED.toString();
		case NOT_UPLOADED:
			getUploadListener().scheduleStatusCheck(RequestType.GET_STATUS);
			return Status.NOT_UPLOADED.toString();
		case UPLOAD_ERROR:
			getUploadListener().cancelStatusTask();
			getUploadListener().cancelTimeoutTask();
			return Status.UPLOAD_ERROR.toString();
		case UNKNOWN:
			getUploadListener().cancelStatusTask();
			getUploadListener().cancelTimeoutTask();
			return Status.UPLOAD_ERROR.toString();
		default:
			return null;
		}
	}



	@Override
	public String handleAbort() {
		return Status.ABANDONED.toString();
	}


	@Override
	public String getName() {
		return Status.UPLOAD_ERROR.toString();
	}


	@Override
	public void onEntry(String prevState, UploadEvent event, Object evtObj) {
		super.onEntry(prevState, event, evtObj);
		if (event==UploadEvent.DISCONNECT){
			getUploadListener().logDisconnect();
			getUploadListener().cancelStatusTask();
			getUploadListener().cancelTimeoutTask();
			getUploadListener().updateDatabase(Status.UPLOAD_ERROR, "Storage agent or storage VM disconnected");  
			getUploadListener().log("Entering upload error state because the storage host disconnected", Level.WARN);
		} else if (event==UploadEvent.TIMEOUT_CHECK){
			getUploadListener().updateDatabase(Status.UPLOAD_ERROR, "Timeout waiting for response from storage host");
			getUploadListener().log("Entering upload error state: timeout waiting for response from storage host", Level.WARN);
		}
		getUploadListener().setUploadInactive(Status.UPLOAD_ERROR);
	}



}
