package com.example.matija077.autotrolej;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Matija077 on 4/15/2017.
 */

@DatabaseTable(tableName = "station_route")
public class Station_route {

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(canBeNull = false, foreign = true, columnName = "station_id")
    private Station station;
    @DatabaseField(canBeNull = false, foreign = true, columnName = "route_id")
    private Route route;
    @DatabaseField(canBeNull = false)
    private char direction;
    @DatabaseField(canBeNull = false)
    private Boolean turnAroundStation;
    @DatabaseField(canBeNull = false)
    private Short stationNumber;

    public Station_route() {

    }


    public Station_route(Station station, Route route, char direction,
                         Boolean turnAroundStation, Short stationNumber) {
        this.station = station;
        this.route = route;
        this.direction = direction;
        this.turnAroundStation = turnAroundStation;
        this.stationNumber = stationNumber;
    }

    public Station getStation() {
        return this.station;
    }

    public Route getRoute() {
        return this.route;
    }
}
