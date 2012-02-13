package com.cloud.projects.dao;

import java.util.List;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.projects.Project;
import com.cloud.projects.ProjectVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.Transaction;

@Local(value = { ProjectDao.class })
public class ProjectDaoImpl extends GenericDaoBase<ProjectVO, Long> implements ProjectDao {
    private static final Logger s_logger = Logger.getLogger(ProjectDaoImpl.class);
    protected final SearchBuilder<ProjectVO> AllFieldsSearch;
    protected GenericSearchBuilder<ProjectVO, Long> CountByDomain;
    protected GenericSearchBuilder<ProjectVO, Long> ProjectAccountSearch;

    protected ProjectDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("name", AllFieldsSearch.entity().getName(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("domainId", AllFieldsSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("projectAccountId", AllFieldsSearch.entity().getProjectAccountId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        CountByDomain = createSearchBuilder(Long.class);
        CountByDomain.select(null, Func.COUNT, null);
        CountByDomain.and("domainId", CountByDomain.entity().getDomainId(), SearchCriteria.Op.EQ);
        CountByDomain.done();
    }

    @Override
    public ProjectVO findByNameAndDomain(String name, long domainId) {
        SearchCriteria<ProjectVO> sc = AllFieldsSearch.create();
        sc.setParameters("name", name);
        sc.setParameters("domainId", domainId);

        return findOneBy(sc);
    }

    @Override
    @DB
    public boolean remove(Long projectId) {
        boolean result = false;
        Transaction txn = Transaction.currentTxn();
        txn.start();
        ProjectVO projectToRemove = findById(projectId);
        projectToRemove.setName(null);
        if (!update(projectId, projectToRemove)) {
            s_logger.warn("Failed to reset name for the project id=" + projectId + " as a part of project remove");
            return false;
        } else {

        }
        result = super.remove(projectId);
        txn.commit();

        return result;

    }

    @Override
    public Long countProjectsForDomain(long domainId) {
        SearchCriteria<Long> sc = CountByDomain.create();
        sc.setParameters("domainId", domainId);
        return customSearch(sc, null).get(0);
    }

    @Override
    public ProjectVO findByProjectAccountId(long projectAccountId) {
        SearchCriteria<ProjectVO> sc = AllFieldsSearch.create();
        sc.setParameters("projectAccountId", projectAccountId);

        return findOneBy(sc);
    }

    @Override
    public List<ProjectVO> listByState(Project.State state) {
        SearchCriteria<ProjectVO> sc = AllFieldsSearch.create();
        sc.setParameters("state", state);
        return listBy(sc);
    }

}
