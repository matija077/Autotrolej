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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        autotrolej.asyncResponse {

    private GoogleMap mMap;
    private static final String TAG = MapsActivity.class.getSimpleName();
    autotrolej autotrolej = null;
    List<String> data = new ArrayList<String>();
    autotrolej.jsonTask asyncTask = null;
    List<String> urlList = null;
    static final String urlStanice = "http://e-usluge2.rijeka.hr/OpenData/ATstanice.json";
    static final String urlLinije = "http://e-usluge2.rijeka.hr/OpenData/ATlinije.json";
	//trying to stupidly use enum here
	/*public enum Days {
		RADNI_DAN, SUBOTA, NEDELJA
	}

	public enum Category {
		GRADSKI, PRIGRADSKI, NOCNI
	}
	Days days;
	Category category;*/
    //databaseHelper db;
    OrmLiteDatabaseHelper db;

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
		urlList.add(urlStanice);
        urlList.add(urlLinije);
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
    public void processfinish(List<Station> newStations, List<Route> newRoutes, List<Station_route>
							  newStationRoutes) {
        Log.d(TAG, String.valueOf(newStations));
		db = new OrmLiteDatabaseHelper(getApplicationContext());
		db.clear();

		for (int i = 0; i < newStations.size(); i++) {
			db.insertStation((Station) newStations.get(i));
		}

		List<Station> allStations = new ArrayList<Station>();
		allStations = db.getAllStations();
		Log.d(TAG, String.valueOf(allStations));

		for (int i = 0; i < newRoutes.size(); i++) {
			db.insertRoute((Route) newRoutes.get(i));
		}

		List<Route> allRoutes = new ArrayList<Route>();
		allRoutes = db.getAllRoutes();
		Log.d(TAG, String.valueOf(allRoutes));

		db.close();
    }



    //trying database

	public void doDatabase() {
		//databasePart - just for now

		db = new OrmLiteDatabaseHelper(getApplicationContext());

		Station station1 = new Station("1", "Kudeji", "23.45", "23.56", "2");
		db.insertStation(station1);
		List<Station> stations = db.getAllStations();
		Log.d("Stations", String.valueOf(stations));

		//trying to insert and get routes
		Route route1 = new Route("2", "Zamet", "Trsat", "Gradski");
		db.insertRoute(route1);
		List<Route> routes = db.getAllRoutes();
		Log.d("Routes", String.valueOf(routes));

		//trying to insert and get schedules

		//trying to insert and get station_routes
		//in case of no table records we get null
		Station tempStation = null;
		Route tempRoute = null;
		Station_route station_route1 = null;

		//get id for foreign keys
		tempStation = db.getStationById(stations.get(0).getId());
		tempRoute = db.getRouteById(routes.get(0).getId());

		if ((tempStation != null) && (tempRoute != null)) {
			station_route1 = new Station_route(tempStation, tempRoute,
					(char) 'A', Boolean.TRUE, "1");
			db.insertStation_route(station_route1);
		}

		List<Station_route> station_routes = db.getAllStation_routes();
		Log.d("Station_routes", String.valueOf(station_routes));

		//schedule
		//same routine for but now we fetch station_route
		Station_route tempStation_route = null;
		Schedule schedule1 = null;
		//Date constructor is depricated so we use Calendar
		Calendar currentTime = Calendar.getInstance();
		//trying to convert Calendar to Date
		Date dateDate = currentTime.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss");
		String date = null;
		try {
			date = sdf.format(dateDate);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		tempStation_route = db.getStation_routeById(stations.get(0).getId());

		if (tempStation_route != null) {
			schedule1 = new Schedule(tempStation_route, date, "RADNI DAN");
			db.insertSchedule(schedule1);
		}

		List<Schedule> schedules = db.getAllSchedules();
		Log.d("schedules", String.valueOf(schedules));

		//close connections and release DAO objects.
		db.close();
	}
}

