package com.example.matija077.autotrolej;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Matija077 on 4/15/2017.
 */

@DatabaseTable(tableName = "station_route")
public class station_route {

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(canBeNull = false, foreign = true)
    private long station_id;
    @DatabaseField(canBeNull = false, foreign = true)
    private long route_id;
    @DatabaseField(canBeNull = false)
    private char direction;
    @DatabaseField(canBeNull = false)
    private Boolean turnAroundStation;
    @DatabaseField(canBeNull = false)
    private Short stationNumber;

    public station_route() {

    }


    public station_route(long station_id, long route_id, char direction,
                         Boolean turnAroundStation, Short stationNumber) {
        this.station_id = station_id;
        this.route_id = route_id;
        this.direction = direction;
        this.turnAroundStation = turnAroundStation;
        this.stationNumber = stationNumber;
    }
}
