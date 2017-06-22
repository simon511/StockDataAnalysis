package models;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import play.Logger;
import play.libs.Json;

/**
 * Created by qding on 6/19/2017.
 */
public class StockDataService {

    private final StockDataRepository stockDataRepository;

    @Inject
    public StockDataService(StockDataRepository stockDataRepository) {
        this.stockDataRepository = stockDataRepository;
    }

    public List<StockDailyTrade> findDailyTradeList(){
        List<StockDailyTrade> stockDailyTrades = stockDataRepository.findDailyTradeList();
        for (StockDailyTrade s : stockDailyTrades){
            s.trend=Json.toJson(trendAnalysis(s.stockCode)).toString();
        }
        return stockDailyTrades;
    }

    public OptionalStock saveOptionalStock(OptionalStock optionalStock){
        if(downloadHistoryData(optionalStock.stockCode)){
            insertHistoryData(optionalStock.stockCode);
            calMa(optionalStock.stockCode,5);
            calMa(optionalStock.stockCode,60);
            calMa(optionalStock.stockCode,250);
        }
        optionalStock.addDate=new Date();
        OptionalStock orgOptionalStock = stockDataRepository.findOptionalStock(optionalStock.stockCode);
        if(orgOptionalStock==null) {
            stockDataRepository.insertOptionalStock(optionalStock);
        }
        else{
            orgOptionalStock.addDate=new Date();
            orgOptionalStock.comment=optionalStock.comment;
            stockDataRepository.saveorUpdateOptionalStock(orgOptionalStock);
        }

        return optionalStock;
    }

    // caluate stage change
    public Map calStageChange(String stockCode,String startDate){
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        List stockList = stockDataRepository.findStageChange(stockCode,startDate);
        Map stockStageChange = new HashMap();
        stockStageChange.put("stockCode",stockCode);
        stockStageChange.put("startDate",startDate);
        BigDecimal d1_low = BigDecimal.ZERO;
        BigDecimal d1_high = BigDecimal.ZERO;
        BigDecimal d1_close = BigDecimal.ZERO;
        BigDecimal d3_low = BigDecimal.ZERO;
        BigDecimal d3_high = BigDecimal.ZERO;
        BigDecimal d3_close = BigDecimal.ZERO;
        BigDecimal d5_low = BigDecimal.ZERO;
        BigDecimal d5_high = BigDecimal.ZERO;
        BigDecimal d5_close = BigDecimal.ZERO;
        BigDecimal d0_close = BigDecimal.ZERO;

        for(int i=0;i<stockList.size();i++){
            Object[] obj = (Object[]) stockList.get(i);
            if(i==0){
                d0_close=(BigDecimal)obj[3];
            }
            if(i==1){
             d1_low = (BigDecimal)obj[1];
             d1_high = (BigDecimal)obj[2];
             d1_close = (BigDecimal)obj[3];
            }
            if(i<=3) {
                if (d3_low.compareTo((BigDecimal) obj[1]) < 0){
                    d3_low = (BigDecimal) obj[1];
                }
                if (d3_low.compareTo((BigDecimal) obj[2]) > 0) {
                    d3_high = (BigDecimal) obj[2];
                }
                d3_close = (BigDecimal)obj[3];
            }
            if(i<=5) {
                if (d5_low.compareTo((BigDecimal) obj[1]) < 0){
                    d5_low = (BigDecimal) obj[1];
                }
                if (d5_low.compareTo((BigDecimal) obj[2]) > 0) {
                    d5_high = (BigDecimal) obj[2];
                }
                d5_close = (BigDecimal)obj[3];
            }
        }
        stockStageChange.put("d0_close",d0_close);
        stockStageChange.put("d1_low",d1_low);
        stockStageChange.put("d1_high",d1_high);
        stockStageChange.put("d1_close",d1_close);
        stockStageChange.put("d3_low",d3_low);
        stockStageChange.put("d3_high",d3_high);
        stockStageChange.put("d3_close",d3_close);
        stockStageChange.put("d5_low",d5_low);
        stockStageChange.put("d5_high",d5_high);
        return stockStageChange;
    }

    public Map trendAnalysis(String stockCode){
        Map result= new HashMap();
//        result.put("stockCode",stockCode);
        List list = stockDataRepository.findTop2StockDailyTradeList(stockCode);
        Object[] t1 = (Object[])list.get(0);
        Object[] t2 = (Object[])list.get(1);
        if(((BigDecimal)t1[2]).compareTo((BigDecimal)t2[2])>0) {
            result.put("s", trend((BigDecimal) t1[2], (BigDecimal) t2[2]));
            result.put("m", trend((BigDecimal) t1[3], (BigDecimal) t2[3]));
            result.put("l", trend((BigDecimal) t1[4], (BigDecimal) t2[4]));
        }
        return result;
    }

    private String trend(BigDecimal o,BigDecimal r){
        String str="";
        if(o!=null && r!=null){
            if(o.compareTo(r)>0){
                str="up";
            }
            else if(o.compareTo(r)==0){
                str="=";
            }
            else{
                str="down";
            }
        }
        return str;
    }

    private Boolean downloadHistoryData(String stockCode) {
        Boolean saveSuccess=true;
        String marketCode= stockCode.substring(0,1).equals("6")?"0":"1";
        String historyDataUrl="http://quotes.money.163.com/service/chddata.html?code="
        + marketCode + stockCode + "&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;TURNOVER;VOTURNOVER;VATURNOVER;TCAP;MCAP";
        try {
            URL url = new URL(historyDataUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3 * 1000);
            conn.setRequestProperty("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            InputStream inputStream = conn.getInputStream();
            byte[] getData = readInputStream(inputStream);

            String savePath = "d:\\stockHistoryData_dev";
            File saveDir = new File(savePath);
            if (!saveDir.exists()) {
                saveDir.mkdir();
            }
            File file = new File(saveDir + File.separator + stockCode + ".csv");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(getData);
            if(fos!=null){
                fos.close();
            }
            if(inputStream!=null){
                inputStream.close();
            }
        }
        catch (Exception ex){
            saveSuccess=Boolean.FALSE;
            ex.printStackTrace();
        }
        Logger.info("info:"+stockCode+" download success");
        return saveSuccess;
    }

    public void initStockHistoryData(){
        File root = new File("D:\\stockHistoryData\\");
        File[] files = root.listFiles();
        for(File file:files){
            String fileName= file.getName();
            Logger.info("Start insert "+fileName+" history trade data");
            insertHistoryData(fileName.substring(0,fileName.length()-4));
            Logger.info("End insert "+fileName+" history trade data");
        }
    }

    private void insertHistoryData(String stockCode){
        Logger.info("Start insert "+stockCode+" history trade data");
        String csvFile = "D:\\stockHistoryData\\" + stockCode + ".csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        StockDailyTrade latestStockDailyTrade = stockDataRepository.findLatestDailyTradeList(stockCode);
        try {
            br = new BufferedReader(new FileReader(csvFile));
            int i=0;
            while ((line = br.readLine()) != null) {

                if(i>0) {
                    StockDailyTrade s = new StockDailyTrade();
                    String[] data = line.split(cvsSplitBy);
                    s.id=data[0].replace("-","")+data[1].substring(1);
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");//小写的mm表示的是分钟

                    try{
                        s.publishDate= sdf.parse(data[0]);
                    }
                    catch (ParseException e) {
                        e.printStackTrace();
                    }
                    s.stockCode=data[1].substring(1);
                    s.stockName=data[2];
                    s.closePrice= new BigDecimal(data[3]);
                    s.highPrice= new BigDecimal(data[4]);
                    s.lowPrice= new BigDecimal(data[5]);
                    s.openPrice= new BigDecimal(data[6]);
                    s.lastClosedPrice= new BigDecimal(data[7]);
                    if(data[6].equals("0.0")){
                        s.priceChange = new BigDecimal("0");
                        s.percentPriceChange = new BigDecimal("0");
                        s.turnoverRate= new BigDecimal("0");
                    }
                    else {

                        s.priceChange = new BigDecimal(changeNone(data[8]));
                        s.percentPriceChange = new BigDecimal(changeNone(data[9]));
                        s.turnoverRate= new BigDecimal(changeNone(data[10]));
                    }
                    if(data.length>11) {
                        s.volume = new BigDecimal(data[11]);
                        s.turnover = new BigDecimal(data[12]);
                        s.totalMarketValue = new BigDecimal(data[13]);
                        s.circulationMarketValue = new BigDecimal(data[14]);
                    }
                    if(latestStockDailyTrade==null || s.publishDate.after(latestStockDailyTrade.publishDate)) {
                        stockDataRepository.insertDailytrade(s);
                    }
                }
                i++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Logger.info("End insert "+stockCode+" history trade data Success");
    }

    private String changeNone(String price){
        return price.isEmpty() || price.equals("None") ? "0.0":price;
    }


    private static  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    public void calMa(String code,int num){
        Logger.info("Start Caluate MA"+num);
        List<StockDailyTrade> tradeList  = stockDataRepository.findStockDailyTradeList(code,"publishDate","asc");
        // ma5
        List<StockDailyTrade> clearedList = new ArrayList<StockDailyTrade>();
        for(int i=0;i<tradeList.size();i++){
            StockDailyTrade s = tradeList.get(i);
            if(s.openPrice.compareTo(BigDecimal.valueOf(0.0))!=0){
                clearedList.add(s);
            }
        }
        for( int i=0;i<clearedList.size();i++){
            if (i>num){
                BigDecimal m = clearedList.get(i).closePrice;
                for(int j=1;j<num;j++) {
                    m = m.add(clearedList.get(i - j).closePrice);
                }
                if(num==5) {
                    clearedList.get(i).ma5 = m
                        .divide(BigDecimal.valueOf(num), 2, BigDecimal.ROUND_HALF_UP);
                }
                else if(num==60){
                    clearedList.get(i).ma60 = m
                        .divide(BigDecimal.valueOf(num), 2, BigDecimal.ROUND_HALF_UP);
                }
                else if(num==250){
                    clearedList.get(i).ma250 = m
                        .divide(BigDecimal.valueOf(num), 2, BigDecimal.ROUND_HALF_UP);
                }
                stockDataRepository.saveorUpdateDailytrade(clearedList.get(i));
            }
        }
        Logger.info("End Caluate MA"+num);
    }


}
