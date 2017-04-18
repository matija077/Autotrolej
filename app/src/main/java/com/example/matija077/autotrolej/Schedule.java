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
    private Station_route station_route;
	//You can also specify the dataType field to the @DatabaseField annotation as a
	// DataType.DATE_STRING in which case the date will be stored as a string in
	// yyyy-MM-dd HH:mm:ss.SSSSSS format.
	//setting format because default adds milliseconds.
	//we don't care about format in base, for all we know it can be String.
	//String makes this easy
    /*@DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING,
			format = " yyyy-MM-dd HH:mm:ss")
    private Date date;*/
    @DatabaseField(canBeNull = false)
	private String date;
    @DatabaseField(canBeNull = false)
    private String day;

    public Schedule() {

    }

    public Schedule(Station_route station_route, String date, String day) {
        this.station_route = station_route;
        this.date = date;
        this.day = day;
    }

    public Station_route getStation_route() {
		return this.station_route;
	}
}
