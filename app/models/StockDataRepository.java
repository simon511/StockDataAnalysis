package models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import play.db.jpa.JPAApi;

/**
 * Created by qding on 6/16/2017.
 */
public class StockDataRepository {
    private final JPAApi jpaApi;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public StockDataRepository(JPAApi jpaApi, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.executionContext = executionContext;
    }

    public void insertOptionalStock(OptionalStock optionalStock){
        wrap(em -> saveOptionalStock(em,optionalStock));
    }

    public void insertDailytrade(StockDailyTrade stockDailyTrade){
        wrap(em -> insert(em,stockDailyTrade));
    }

    public void saveorUpdateDailytrade(StockDailyTrade stockDailyTrade){
        wrap(em -> saveOrUpdateDailyTrade(em,stockDailyTrade));
    }

    public void saveorUpdateOptionalStock(OptionalStock optionalStock){
        wrap(em -> saveOrUpdateOptionalStock(em,optionalStock));
    }
    public OptionalStock findOptionalStock(String stockCode){
        String sql="select s from OptionalStock s where s.stockCode ='%s' ";
        List  results = jpaApi.em("default").createQuery(String.format(sql,stockCode), OptionalStock.class).getResultList();
        if (results.isEmpty()) {
            return null;
        } else if (results.size() == 1) {
            return (OptionalStock) results.get(0);
        }

        return null;
    }

    public List findTop2StockDailyTradeList(String stockCode){
        String sql="select s.stockCode,s.closePrice,s.ma5,s.ma60,s.ma250 from Stockdailytrade  s where s.stockcode='%s' and s.openPrice <>0.00 order by s.publishdate desc limit 2";
        List results = jpaApi.em("default").createNativeQuery(String.format(sql,stockCode)).getResultList();
        return results;
    }
    public StockDailyTrade findLatestDailyTradeList(String stockCode){

        String sql="select s from StockDailyTrade s where s.publishDate in (select max(publishDate) from StockDailyTrade sdt) and s.stockCode ='%s' ";
        List  results = jpaApi.em("default").createQuery(String.format(sql,stockCode), StockDailyTrade.class).getResultList();
        if (results.isEmpty()) {
            return null;
        } else if (results.size() == 1) {
            return (StockDailyTrade) results.get(0);
        }

        return null;
    }

    public List findStageChange(String stockCode,String startDate){
        String sql = "select stockCode,lowPrice,highPrice,closePrice,publishdate from stockdailytrade where publishdate>'%s' and stockCode='%s' and openPrice<>0.0 order  by publishdate asc limit 6";

        List result = jpaApi.em("default").createNativeQuery(String.format(sql,startDate,stockCode)).getResultList();

        return result;
    }
    public List findDailyTradeList(Map params){
        String sql = "select * from stockDailyTrade";
        List result = jpaApi.em("default").createNativeQuery(sql).getResultList();

        return result;
    }

    public List<StockDailyTrade> findDailyTradeList() {
        String sql="select s from StockDailyTrade s where s.stockCode in (select stockCode from OptionalStock) and s.publishDate ="
            + "( select Max(publishDate) from StockDailyTrade sdt) ";

        List<StockDailyTrade> persons = jpaApi.em("default").createQuery(sql, StockDailyTrade.class)
            .getResultList();
        return persons;
    }
    public List<StockDailyTrade> findStockDailyTradeList(String code,String orderField,String orderBy) {
        String sql="select s from StockDailyTrade s where s.stockCode=%s order by %s %s";

        List<StockDailyTrade> persons = jpaApi.em("default").createQuery(String.format(sql, code,orderField,orderBy), StockDailyTrade.class)
            .getResultList();
        return persons;
    }
    private <T> T wrap(Function<EntityManager, T> function) {
        return jpaApi.withTransaction(function);
    }

    private StockDailyTrade insert(EntityManager em, StockDailyTrade dailyTrade) {
        em.persist(dailyTrade);
        return dailyTrade;
    }

    private StockDailyTrade saveOrUpdateDailyTrade(EntityManager em, StockDailyTrade dailyTrade) {
        em.merge(dailyTrade);
        return dailyTrade;
    }

    private OptionalStock saveOrUpdateOptionalStock(EntityManager em, OptionalStock optionalStock) {
        em.merge(optionalStock);
        return optionalStock;
    }

    private OptionalStock saveOptionalStock(EntityManager em,OptionalStock optionalStock){
        em.persist(optionalStock);
        return optionalStock;
    }
}
