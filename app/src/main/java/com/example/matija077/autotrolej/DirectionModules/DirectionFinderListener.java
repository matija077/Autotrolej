package com.example.matija077.autotrolej.DirectionModules;

import java.util.List;

public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<DirRoute> dirRoute);
}