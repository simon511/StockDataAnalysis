package controllers;

import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

import java.io.BufferedReader;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import models.OptionalStock;
import models.StockDataService;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import services.StockHistoryDataUtil;
import services.StockQueue;

/**
 * Created by qding on 6/15/2017.
 */
public class DashBoardController {


    private final StockDataService stockDataService;
    private final FormFactory formFactory;
    private final HttpExecutionContext ec;
    private final StockQueue stockQueue ;

    @Inject
    public DashBoardController(FormFactory formFactory, StockDataService stockDataService,StockQueue stockQueue, HttpExecutionContext ec) {
        this.stockDataService = stockDataService;
        this.formFactory = formFactory;
        this.ec = ec;
        this.stockQueue = stockQueue;
    }

    public Result index() {
        String csvFile = "D:\\stockHistoryData\\000651.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

//        StockHistoryDataUtil r = new StockHistoryDataUtil(stockQueue);

        for(int i=0; i<5;i++) {
            StockHistoryDataUtil r = new StockHistoryDataUtil(stockQueue,stockDataService);
            Thread t = new Thread(r);//创建线程
            System.out.println("Start new Thread "+ i +" *******");
            t.start(); //线程开启
        }
        System.out.println("end Thread ******");

//        try {
//
//            br = new BufferedReader(new FileReader(csvFile));
//            int i=0;
//            while ((line = br.readLine()) != null) {
//
//                if(i>0) {
//                    StockDailyTrade s = new StockDailyTrade();
//                    String[] data = line.split(cvsSplitBy);
//
//                    s.id=data[0].replace("-","")+data[1].substring(1);
//                    System.out.println(data[0]);
//                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");//小写的mm表示的是分钟
//
//                    try{
//                        s.publishDate= sdf.parse(data[0]);
//                    }
//                    catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                    s.stockCode=data[1].substring(1);
//                    s.stockName=data[2];
//                    s.closePrice= new BigDecimal(data[3]);
//                    s.highPrice= new BigDecimal(data[4]);
//                    s.lowPrice= new BigDecimal(data[5]);
//                    s.openPrice= new BigDecimal(data[6]);
//                    s.lastClosedPrice= new BigDecimal(data[7]);
//                    if(data[6].equals("0.0")){
//                        s.priceChange = new BigDecimal("0");
//                        s.percentPriceChange = new BigDecimal("0");
//                    }
//                    else {
//                        s.priceChange = new BigDecimal(data[8]);
//                        s.percentPriceChange = new BigDecimal(data[9]);
//                    }
//                    s.turnoverRate= new BigDecimal(data[10]);
//                    s.volume= new BigDecimal(data[11]);
//                    s.turnover= new BigDecimal(data[12]);
//                    s.totalMarketValue= new BigDecimal(data[13]);
//                    s.circulationMarketValue= new BigDecimal(data[14]);
//
//                    stockDataRepository.insertDailytrade(s);
//                }
//                i++;
//            }
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        stockDataService.calMa("000651");
//        stockDataService.initStockHistoryData();
//          stockDataService.initStockList();
        return ok(views.html.dashboard.render());
    }



    public Result addOptionalStock() {
        OptionalStock optionalStock = formFactory.form(OptionalStock.class).bindFromRequest().get();
        stockDataService.saveOptionalStock(optionalStock);
        return redirect(routes.DashBoardController.index());
    }

    public Result getStockTrend(String stockCode){
        return ok(Json.toJson(stockDataService.trendAnalysis(stockCode)));
    }
    public Result getDailyTradeList(){
        List tradeList  = stockDataService.findDailyTradeList();
        return ok(Json.toJson(tradeList));
    }

    public Result calStageChange(String stockCode,String date){
        Map map  = stockDataService.calStageChange(stockCode,date);
        return ok(Json.toJson(map));
    }
}
