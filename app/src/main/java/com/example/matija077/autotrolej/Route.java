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
    private String category;

    public Route() {
    }

    public Route(String routeMark, String directionA, String directionB, String category) {
        this.routeMark = routeMark;
        this.directionA = directionA;
        this.directionB = directionB;
        this.category = category;
    }

    public Integer getId() { return this.id;}

    public  String getRouteMark() {
        return this.routeMark;
    }

    public String getDirectionA() {
        return this.directionA;
    }

    public void setDirectionA(String A) {
        this.directionA = A;
    }

    public String getDirectionB() {
        return this.directionB;
    }

    public void setDirectionB(String B) {
        this.directionB = B;
    }
}
