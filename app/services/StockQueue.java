package services;

import java.util.List;
import javax.inject.Inject;
import models.Stock;
import models.StockDataRepository;

/**
 * Created by qding on 6/30/2017.
 */
public class StockQueue{

    private final StockDataRepository stockDataRepository;

    @Inject
    public StockQueue(StockDataRepository stockDataRepository) {
        this.stockDataRepository = stockDataRepository;
    }

    public List<Stock> findAllStock(){
       return stockDataRepository.findActivityStockList();
    }

    public synchronized  Stock popStock(){
        List<Stock> stocks = stockDataRepository.findActivityStockList();

        Stock st = stockDataRepository.findStock(stocks.get(0).stockCode);
        st.dataStatus="PROCESSING";
        stockDataRepository.saveorUpdateStock(st);
        return st;
    }

    public void complete(String stockCode){
        Stock st = stockDataRepository.findStock(stockCode);
        st.dataStatus="COMPLETE";
        stockDataRepository.saveorUpdateStock(st);
    }


}
