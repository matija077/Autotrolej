package com.example.matija077.autotrolej;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Matija077 on 4/15/2017.
 */

@DatabaseTable(tableName = "station")
public class Station {

    @DatabaseField(generatedId = true)
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

    public Station(String name, double gpsx, double gpsy, short zone) {
        this.name = name;
        this.gpsx = gpsx;
        this.gpsy = gpsy;
        this.zone = zone;
    }

    //for implementing our own wrapper for dealing with SQLite
    public String getClassName() {
        return this.getClass().getSuperclass().getName();
    }

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
