package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

/**
 * Created by qding on 6/20/2017.
 */
@Entity
public class OptionalStock {
    @javax.persistence.Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Long Id;
    public String stockCode;
    public String comment;
    public Date addDate;

}
