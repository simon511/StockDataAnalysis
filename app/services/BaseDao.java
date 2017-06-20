package services;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import models.DatabaseExecutionContext;
import play.db.jpa.JPAApi;

/**
 * Created by qding on 6/15/2017.
 */
public class BaseDao {
    private final JPAApi jpaApi;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public BaseDao(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }

    public List findBySql(String sql, Map params){
        return jpaApi.em("default").createNativeQuery(sql).getResultList();
    }

}
