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
import java.util.List;
import javax.inject.Inject;
import play.Logger;

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
        return stockDataRepository.findDailyTradeList();
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
                        s.priceChange = new BigDecimal(data[8]);
                        s.percentPriceChange = new BigDecimal(data[9]);
                        s.turnoverRate= new BigDecimal(data[10]);
                    }

                    s.volume= new BigDecimal(data[11]);
                    s.turnover= new BigDecimal(data[12]);
                    s.totalMarketValue= new BigDecimal(data[13]);
                    s.circulationMarketValue= new BigDecimal(data[14]);
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
