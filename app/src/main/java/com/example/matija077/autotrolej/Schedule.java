package com.example.matija077.autotrolej;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;


/**
 * Created by Matija077 on 4/15/2017.
 */

@DatabaseTable(tableName = "schedule")
public class Schedule {
    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(canBeNull = false, foreign = true)
    private long station_line_id;
	//You can also specify the dataType field to the @DatabaseField annotation as a
	// DataType.DATE_STRING in which case the date will be stored as a string in
	// yyyy-MM-dd HH:mm:ss.SSSSSS format.
	//setting format because default adds milliseconds.
    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING,
			format = " yyyy-MM-dd HH:mm:ss")
    private Date date;
	//Persisted as SQL type TIMESTAMP
    @DatabaseField(canBeNull = false, dataType = DataType.DATE)
    private Date time;

    public Schedule() {

    }

    public Schedule(long station_line_id, Date date, Date time) {
        this.station_line_id = station_line_id;
        this.date = date;
        this.time = time;
    }
}
