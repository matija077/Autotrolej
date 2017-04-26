package com.example.matija077.autotrolej;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Matija077 on 4/15/2017.
 */

@DatabaseTable(tableName = "station")
public class Station {

    @DatabaseField(generatedId = false)
    private Integer id;
    @DatabaseField(canBeNull = false)
    private String name;
    @DatabaseField
    private double gpsx;
    @DatabaseField
    private double gpsy;
    @DatabaseField
    private short zone;

    public Station() {

    }

    //everythign is string because it is much easier to work with strings and
    //strings are used in JSON

    public Station(String id, String name, String gpsx, String gpsy, String zone) {
        this.id = Integer.valueOf(id);
        this.name = name;
        this.gpsx = Double.valueOf(gpsx);
        this.gpsy = Double.valueOf(gpsy);
        this.zone = Short.valueOf(zone);
    }

    public Integer getId() { return this.id;}

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getGpsx() {
        return this.gpsx;
    }

    public  void setGpsx(double gpsx) {
        this.gpsx = gpsx;
    }

    public double getGpsy() {
        return this.gpsy;
    }

    public  void setGpsy(double gpsy) {
        this.gpsy = gpsy;
    }

    public  short getZone() {
        return this.zone;
    }

    public void setZone(short zone) {
        this.zone = zone;
    }

}
