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
package com.cloud.projects.dao;

import java.util.List;

import com.cloud.projects.ProjectInvitation.State;
import com.cloud.projects.ProjectInvitationVO;
import com.cloud.utils.db.GenericDao;

public interface ProjectInvitationDao extends GenericDao<ProjectInvitationVO, Long> {
    ProjectInvitationVO findByAccountIdProjectId(long accountId, long projectId, State... inviteState);

    List<ProjectInvitationVO> listExpiredInvitations();

    boolean expirePendingInvitations(long timeOut);

    boolean isActive(long id, long timeout);

    ProjectInvitationVO findByEmailAndProjectId(String email, long projectId, State... inviteState);

    ProjectInvitationVO findPendingByTokenAndProjectId(String token, long projectId, State... inviteState);

    void cleanupInvitations(long projectId);

    ProjectInvitationVO findPendingById(long id);

    List<ProjectInvitationVO> listInvitationsToExpire(long timeOut);

}
