package services;

import java.util.List;
import javax.inject.Inject;
import models.Stock;
import models.StockDataService;

/**
 * Created by qding on 6/30/2017.
 */
public class StockHistoryDataUtil implements Runnable {

    private StockQueue stockQueue;
    private StockDataService stockDataService;

    @Inject
    public StockHistoryDataUtil(StockQueue stockQueue,StockDataService stockDataService) {
        this.stockQueue = stockQueue;
        this.stockDataService = stockDataService;

    }
    @Override
    public void run() {

        while (Boolean.TRUE) {
            List<Stock> stockList = stockQueue.findAllStock();
            if(stockList.isEmpty()){
                break;
            }
            try {

                Stock stock = stockQueue.popStock();

                System.out.println("Start insert History " + stock.stockCode);
                stockDataService.insertHistoryData(stock.stockCode);
                stockQueue.complete(stock.stockCode);
                System.out.println("end insert History " + stock.stockCode);
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }

    }
}
