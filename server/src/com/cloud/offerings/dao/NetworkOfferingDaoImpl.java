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

/**
 * 
 */
package com.cloud.offerings.dao;

import java.util.List;

import javax.ejb.Local;
import javax.persistence.EntityExistsException;

import com.cloud.network.Network;
import com.cloud.network.Networks.TrafficType;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Availability;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.Transaction;

@Local(value = NetworkOfferingDao.class)
@DB(txn = false)
public class NetworkOfferingDaoImpl extends GenericDaoBase<NetworkOfferingVO, Long> implements NetworkOfferingDao {
    final SearchBuilder<NetworkOfferingVO> NameSearch;
    final SearchBuilder<NetworkOfferingVO> SystemOfferingSearch;
    final SearchBuilder<NetworkOfferingVO> AvailabilitySearch;
    final SearchBuilder<NetworkOfferingVO> AllFieldsSearch;
    private final GenericSearchBuilder<NetworkOfferingVO, Long> UpgradeSearch;

    protected NetworkOfferingDaoImpl() {
        super();

        NameSearch = createSearchBuilder();
        NameSearch.and("name", NameSearch.entity().getName(), SearchCriteria.Op.EQ);
        NameSearch.and("uniqueName", NameSearch.entity().getUniqueName(), SearchCriteria.Op.EQ);
        NameSearch.done();

        SystemOfferingSearch = createSearchBuilder();
        SystemOfferingSearch.and("system", SystemOfferingSearch.entity().isSystemOnly(), SearchCriteria.Op.EQ);
        SystemOfferingSearch.done();

        AvailabilitySearch = createSearchBuilder();
        AvailabilitySearch.and("availability", AvailabilitySearch.entity().getAvailability(), SearchCriteria.Op.EQ);
        AvailabilitySearch.and("isSystem", AvailabilitySearch.entity().isSystemOnly(), SearchCriteria.Op.EQ);
        AvailabilitySearch.done();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("trafficType", AllFieldsSearch.entity().getTrafficType(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("guestType", AllFieldsSearch.entity().getGuestType(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("isSystem", AllFieldsSearch.entity().isSystemOnly(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        UpgradeSearch = createSearchBuilder(Long.class);
        UpgradeSearch.selectField(UpgradeSearch.entity().getId());
        UpgradeSearch.and("physicalNetworkId", UpgradeSearch.entity().getId(), Op.NEQ);
        UpgradeSearch.and("physicalNetworkId", UpgradeSearch.entity().isSystemOnly(), Op.EQ);
        UpgradeSearch.and("trafficType", UpgradeSearch.entity().getTrafficType(), Op.EQ);
        UpgradeSearch.and("guestType", UpgradeSearch.entity().getGuestType(), Op.EQ);
        UpgradeSearch.and("state", UpgradeSearch.entity().getState(), Op.EQ);
        UpgradeSearch.done();
    }

    @Override
    public NetworkOfferingVO findByUniqueName(String uniqueName) {
        SearchCriteria<NetworkOfferingVO> sc = NameSearch.create();

        sc.setParameters("uniqueName", uniqueName);

        return findOneBy(sc);

    }

    @Override
    public NetworkOfferingVO persistDefaultNetworkOffering(NetworkOfferingVO offering) {
        assert offering.getUniqueName() != null : "how are you going to find this later if you don't set it?";
        NetworkOfferingVO vo = findByUniqueName(offering.getUniqueName());
        if (vo != null) {
            return vo;
        }
        try {
            vo = persist(offering);
            return vo;
        } catch (EntityExistsException e) {
            // Assume it's conflict on unique name from two different management servers.
            return findByUniqueName(offering.getName());
        }
    }

    @Override
    public List<NetworkOfferingVO> listSystemNetworkOfferings() {
        SearchCriteria<NetworkOfferingVO> sc = SystemOfferingSearch.create();
        sc.setParameters("system", true);
        return this.listIncludingRemovedBy(sc, null);
    }

    @Override
    public List<NetworkOfferingVO> listByAvailability(Availability availability, boolean isSystem) {
        SearchCriteria<NetworkOfferingVO> sc = AvailabilitySearch.create();
        sc.setParameters("availability", availability);
        sc.setParameters("isSystem", isSystem);
        return listBy(sc, null);
    }

    @Override
    @DB
    public boolean remove(Long networkOfferingId) {
        Transaction txn = Transaction.currentTxn();
        txn.start();
        NetworkOfferingVO offering = findById(networkOfferingId);
        offering.setUniqueName(null);
        update(networkOfferingId, offering);
        boolean result = super.remove(networkOfferingId);
        txn.commit();
        return result;
    }

    @Override
    public List<Long> getOfferingIdsToUpgradeFrom(NetworkOffering originalOffering) {
        SearchCriteria<Long> sc = UpgradeSearch.create();
        // exclude original offering
        sc.addAnd("id", SearchCriteria.Op.NEQ, originalOffering.getId());

        // list only non-system offerings
        sc.addAnd("systemOnly", SearchCriteria.Op.EQ, false);

        // Type of the network should be the same
        sc.addAnd("guestType", SearchCriteria.Op.EQ, originalOffering.getGuestType());

        // Traffic types should be the same
        sc.addAnd("trafficType", SearchCriteria.Op.EQ, originalOffering.getTrafficType());

        sc.addAnd("state", SearchCriteria.Op.EQ, NetworkOffering.State.Enabled);

        return customSearch(sc, null);
    }

    @Override
    public List<NetworkOfferingVO> listByTrafficTypeGuestTypeAndState(NetworkOffering.State state, TrafficType trafficType, Network.GuestType type) {
        SearchCriteria<NetworkOfferingVO> sc = AllFieldsSearch.create();
        sc.setParameters("trafficType", trafficType);
        sc.setParameters("guestType", type);
        sc.setParameters("state", state);
        return listBy(sc, null);
    }

}
