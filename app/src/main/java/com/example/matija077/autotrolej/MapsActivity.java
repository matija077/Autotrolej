package com.example.matija077.autotrolej;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        autotrolej.asyncResponse {

    private GoogleMap mMap;
    private static final String TAG = MapsActivity.class.getSimpleName();
    autotrolej autotrolej = null;
    List<String> data = new ArrayList<String>();
    autotrolej.jsonTask asyncTask = null;
    List<String> urlList = null;
    static final String urlLinije = "http://e-usluge2.rijeka.hr/OpenData/ATstanice.json";
    static final String urlStanice = "http://e-usluge2.rijeka.hr/OpenData/ATlinije.json";
    databaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        urlList = new ArrayList<String>();
        urlList.add(urlLinije);
        urlList.add(urlStanice);

        // database helper instance
        db = new databaseHelper(getApplicationContext());

        //db.dropTable(databaseHelper.TABLE_STATION);

        //Station instance
        Station station1 = new Station("Kudeji", 23.45, 23.56, (short) 2);

        long station1_id = db.createStation(station1);
        List<Station> stations = new ArrayList<Station>();
        stations = db.getAllStations();
        Log.d("Station", stations.get(0).getName());
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        autotrolej = new autotrolej();
        asyncTask = new autotrolej.jsonTask(urlList);
        asyncTask.delegate = this;
        asyncTask.execute(urlList);
    }

    @Override
    public void processfinish(List params) {
        Log.d(TAG, String.valueOf(params));
    }

    //trying database

}

