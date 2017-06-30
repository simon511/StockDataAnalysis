package utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by qding on 6/22/2017.
 */
public class CSVFileUtil {

    public static void readCSVFile() throws IOException {
        CSVReader reader = new CSVReader(new FileReader("yourfile.csv"));
        String [] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            // nextLine[] is an array of values from the line
            System.out.println(nextLine[0] + nextLine[1] + "etc...");
        }

    }

    public static void saveToCSVFile(List<String[]> content,String filePath) throws IOException {
        CSVWriter writer = new CSVWriter(new FileWriter(filePath), ',');
        // feed in your array (or convert your data to an array)

        writer.writeAll(content);
        writer.close();
    }

}
