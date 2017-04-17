package com.example.matija077.autotrolej;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Matija077 on 4/15/2017.
 */

@DatabaseTable(tableName = "route")
public class Route {

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(canBeNull = false)
    private String routeMark;
    @DatabaseField
    private String directionA;
    @DatabaseField
    private String directionB;
    @DatabaseField(canBeNull = false)
    private String categoy;

    public final String CITY = "city";
    public final String SUBURB = "suburb";
    public final String NIGHT = "night";

    public Route() {
    }

    public Route(String routeMark, String directionA, String directionB, String categoy) {
        this.routeMark = routeMark;
        this.directionA = directionA;
        this.directionB = directionB;
        this.categoy = categoy;
    }
}
