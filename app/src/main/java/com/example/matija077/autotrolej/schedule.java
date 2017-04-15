package com.example.matija077.autotrolej;

/**
 * Created by Matija077 on 4/15/2017.
 */

public class schedule {
    private long station_line_id;
    private String date;
    private long time;

    public schedule(long station_line_id, String date, long time) {
        this.station_line_id = station_line_id;
        this.date = date;
        this.time = time;
    }
}
