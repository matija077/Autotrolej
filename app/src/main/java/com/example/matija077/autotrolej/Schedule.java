package com.example.matija077.autotrolej;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Matija077 on 4/15/2017.
 */

@DatabaseTable(tableName = "schedule")
public class Schedule {
    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(canBeNull = false, foreign = true)
    private long station_line_id;
    @DatabaseField(canBeNull = false)
    private String date;
    @DatabaseField(canBeNull = false)
    private long time;

    public Schedule() {

    }

    public Schedule(long station_line_id, String date, long time) {
        this.station_line_id = station_line_id;
        this.date = date;
        this.time = time;
    }
}
