package com.example.matija077.autotrolej;

/**
 * Created by Matija077 on 4/15/2017.
 */

public class station_route {

    private long station_id;
    private long route_id;
    private char direction;
    private Boolean turnAroundStation;
    private Short stationNumber;


    public station_route(long station_id, long route_id, char direction,
                         Boolean turnAroundStation, Short stationNumber) {
        this.station_id = station_id;
        this.route_id = route_id;
        this.direction = direction;
        this.turnAroundStation = turnAroundStation;
        this.stationNumber = stationNumber;
    }
}
