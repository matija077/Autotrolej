package com.example.matija077.autotrolej;

import android.provider.BaseColumns;

/**
 * Created by Matija077 on 4/14/2017.
 */

public final class database {

    //we don't want someone to be albe to instanting this class

    private database() {}

    public static class stanica implements BaseColumns {
        public static final String TABLE_NAME = "station";
        public static final String NAME = "name";
        public  static  final String GPSX = "gpsx";
        public static final String GPSY = "gpsy";
        public  static final  String ZONE = "zone";
    }
}


