package com.example.matija077.autotrolej;

/**
 * Created by Matija077 on 4/15/2017.
 */

public class Route {

    private String routeMark;
    private String directionA;
    private String directionB;
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
