package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by qding on 6/30/2017.
 */
@Entity
public class Stock {
    @Id
    public String stockCode;
    public String stockName;
    public String status; // Delisting , activity
    public Date quotedDate;
    public Date exitDate;
    public String dataStatus; // OUTSTANDING PROCESSING COMPLETE

}
