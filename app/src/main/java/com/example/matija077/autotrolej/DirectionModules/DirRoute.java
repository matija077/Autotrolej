package com.example.matija077.autotrolej.DirectionModules;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class DirRoute {
    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public List<LatLng> points;
}
