package com.example.matija077.autotrolej;

/**
 * Created by Matija077 on 4/15/2017.
 */

public class Station {

    private String name;
    private double gpsx;
    private double gpsy;
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
