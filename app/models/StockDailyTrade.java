package models;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by qding on 6/16/2017.
 */
@Entity
public class StockDailyTrade {
    @Id
    public String id;
    public Date publishDate;
    public String stockCode;
    public String stockName;
    public BigDecimal closePrice;
    public BigDecimal highPrice;
    public BigDecimal lowPrice;
    public BigDecimal openPrice;
    public BigDecimal lastClosedPrice;
    public BigDecimal priceChange;
    public BigDecimal percentPriceChange;
    public BigDecimal turnoverRate;
    public BigDecimal volume;
    public BigDecimal turnover;
    public BigDecimal totalMarketValue;
    public BigDecimal circulationMarketValue;

    public BigDecimal ma5;
    public BigDecimal ma60;
    public BigDecimal ma250;


}
