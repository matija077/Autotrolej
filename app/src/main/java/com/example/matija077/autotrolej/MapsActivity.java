package com.example.matija077.autotrolej;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Map;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
		GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
		autotrolej.asyncResponse{

	long startTime = System.currentTimeMillis();
    private GoogleMap mMap;
	private GoogleApiClient mGoogleApiClient;
    private static final String TAG = MapsActivity.class.getSimpleName();
	/*
    autotrolej autotrolej = null;
    List<String> data = new ArrayList<String>();
    autotrolej.jsonTask asyncTask = null;
    */
	ArrayList<MarkerOptions> markerOptionsList = new ArrayList<MarkerOptions>();
    List<String> urlList = null;
    static final String urlStanice = "http://e-usluge2.rijeka.hr/OpenData/ATstanice.json";
    static final String urlLinije = "http://e-usluge2.rijeka.hr/OpenData/ATlinije.json";
	static final String urlRadniDan = "http://e-usluge2.rijeka.hr/OpenData/ATvoznired-tjedan.json";
	static  final  String urlSubota = "http://e-usluge2.rijeka.hr/OpenData/ATvoznired-subota.json";
	static final String urlNedelja = "http://e-usluge2.rijeka.hr/OpenData/ATvoznired-nedjelja.json";
	static final String preferenceName = "com.example.autotrolej.PREFERENCE_FILE_KEY";
	static final String linesExpireKey = "com.example.autotrolej.lineExpire";
	private final static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
	private final static int DEFAULT_ZOOM = 16;
	private static final String KEY_CAMERA_POSITION = "camera_position";
	private static final String KEY_LOCATION = "location";
    OrmLiteDatabaseHelper db;
	Boolean mLocationPermissionGranted = false;
	private Location mLastKnownLocation;
	private CameraPosition mCameraPosition;
	private Location mCurrentLocation;
	private Boolean stateReset = FALSE;
	private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener;
	private Marker lastOpened = null;
	/*
	log TAG constant and Log switch
	*/
	public static final Boolean DebugOn = TRUE;
	private static final String TAG_onMapReady2 = "OnMapReady-writing part";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
		/*
			if saveInstanceState is not equal to null then we need to rebuild our last known
			location and markers. Also we need to know if state was reset in drawStations() method
			so we use stateReset variable and clean all existing data that we will be fetching from
			savedInstanceState.
		*/
		mCurrentLocation = null;
		mCameraPosition = null;
		markerOptionsList.clear();
		if (savedInstanceState != null) {
			mCurrentLocation = savedInstanceState.getParcelable("location");
			mCameraPosition = savedInstanceState.getParcelable("camera_position");
			markerOptionsList = savedInstanceState.getParcelableArrayList("stations");
			stateReset = TRUE;
		}
        //mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);

		// Build the Play services client for use by the Fused Location Provider and the Places API.
		// Use the addApi() method to request the Google Places API and the Fused Location Provider.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
				.addApi(LocationServices.API)
				.addApi(Places.GEO_DATA_API)
				.addApi(Places.PLACE_DETECTION_API)
				.build();
		mGoogleApiClient.connect();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        urlList = new ArrayList<String>();
		urlList.add(urlRadniDan);
      	//urlList.add(urlSubota);
		//urlList.add(urlNedelja);
		/*urlList.add(urlStanice);
		urlList.add(urlLinije);*/
    }

	@Override
	protected void onStart() {
		super.onStart();
	}


    @Override
	protected void onStop(){
		super.onStop();
		mGoogleApiClient.disconnect();
	}


	/**
	 * Builds the map when the Google Play services client is successfully connected.
	 */

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
		/*
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
		*/
        //autotrolej = new autotrolej();
        //asyncTask = new autotrolej.jsonTask(urlList);
       //asyncTask.delegate = this;
        //asyncTask.execute(urlList);
		//doDatabase();

		/*OrmLiteDatabaseHelper db = new OrmLiteDatabaseHelper(getApplicationContext());
		db.dumpDatabase2(getApplicationContext());*/
		db = new OrmLiteDatabaseHelper(getApplicationContext());
		//List<Route> routes = db.getAllRoutes();
		//Station_route station_route = db.queryStation_route_specific1("1795", "1-0", 'B', TRUE);
		//List<Station_route> station_routes = db.queryStation_route_specific2("1-0");
		//List<Station_route> station_routes2 = db.queryStation_route_specific2("21-7");



		/*
		should we get all stations, routes and station routes? if we do we clear db.
		*/
		//if (shouldWeParse()) {
		/*if (TRUE) {
			db.clear();
			Intent intent = new Intent(this, parseDataIntentService.class);
			intent.putStringArrayListExtra("urlList", (ArrayList<String>) urlList);
			startService(intent);
		}*/

		/*
			schedule here
		*/

		//db.clear();
		/*Intent intent = new Intent(this, parseScheduleDataIntentService.class);
		intent.putStringArrayListExtra("urlList", (ArrayList<String>) urlList);
		startService(intent);*/

		/*try {
			List<Schedule> schedules = db.getAllSchedules();
		} catch (Exception e) {
			e.printStackTrace();
		}*/


		/*
			override default behaviour of centering map whenever user clicks marker.
			Return True means that we suppress default behaviour so it doesn't happen. Also we
			need to handle opening info windows now.
		*/
		mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				// is there na open window
				if (lastOpened != null) {
					// if there is closed it
					lastOpened.hideInfoWindow();

					// check if this marker is the same that was already open
					if (lastOpened.equals(marker)) {
						//	if it is we don't want to open it again
						lastOpened = null;
						return true;
					}
				}

				marker.showInfoWindow();
				lastOpened = marker;
				return  true;
			}
		});

		// Turn on the My Location layer and the related control on the map.
		updateLocationUI();

		// Get the current location of the device and set the position of the map.
		getDeviceLocation();

		/*
			map draw part - for test purposes.
		*/
		if ((db.getWritableDatabase() != null) && (mLastKnownLocation != null)) {
			drawStations();
			mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
				@Override
				public void onCameraIdle() {
					mMap.clear();
					if (mMap.getCameraPosition().zoom > 14) {
						drawStations();
					} else {
						/*
							TODO: tell the user that the zoom level is to low
						*/
					}
				}
			});
		} else {
			Log.i(TAG_onMapReady2, String.valueOf(db.getWritableDatabase()).concat
					(String.valueOf(mLastKnownLocation)));
		}

		/*
		long endTime   = System.currentTimeMillis()/1000;
		long totalTime = endTime - startTime;
		Log.i(TAG, String.valueOf(totalTime));
		*/
    }

    @Override
    public void processfinish(List<String> data) {

		/*
		Intent intent = new Intent(this, parseDataIntentService.class);
		intent.putStringArrayListExtra("data", (ArrayList<String>) data);
		startService(intent);
		*/
        /*
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
		*/
    }



    /*
    	checks if parsing new data is necessary

    	here we check for date value saved in shared preferences to see if it is time to update.
    	key is LinesExpireKey and if doesn't exist we should probably parse.
    	if exists then compare current time with time aved in shared preferences. Also we include
    	the check for database. if database is non existing we fetch new data. Also network check
    	should be here, but NETWORK EXCEPTION COMES IF YOU DO ANYTHING REGARDING NETWORKS IN MAIN
    	THREAD, SO IT IS WAS MOVED TO parseDataIntentService.
   	*/

	private Boolean shouldWeParse() {
		Boolean shouldParse = true;
		SharedPreferences preferences = getApplicationContext().getSharedPreferences(preferenceName,
				Context.MODE_PRIVATE);
		String date = "";

		try {
			date = preferences.getString(linesExpireKey, null);
			Map<String, ?> list = preferences.getAll();
		} catch (Exception e) {
			Log.e("error", "error getting preference", e);
		}
		if (date == null || date == "") {
			/*
				TODO: what to do if shared preferences is lost?
			*/
		} else {
			DateFormat dateFormat = new SimpleDateFormat("dd MM yyyy, HH:mm");
			Calendar currentCalendar = Calendar.getInstance();
			Calendar dateCalendar = Calendar.getInstance();
			Date dateCalendarDate = null;
			try {
				dateCalendarDate = dateFormat.parse(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			dateCalendar.setTime(dateCalendarDate);
			if (currentCalendar.getTimeInMillis() < dateCalendar.getTimeInMillis() &&
					db.getWritableDatabase() != null) {
						return shouldParse = false;
			}

		}


		return shouldParse;
	}

	private void drawStations() {
		if ((markerOptionsList.size() >= 1) && (stateReset)) {
			for (int i = 0; i < markerOptionsList.size(); i++) {
				mMap.addMarker(markerOptionsList.get(i));
			}
			/*
				this needs to be here or it will never go down this code.
			*/
			stateReset = FALSE;
			return;
		}
		/*
			TODOFIXED: fix the bug with resize or changing configuration options.
			clearing marker options before adding any more when window was not resized.
		 */
		markerOptionsList.clear();
		Projection projection = null;
		try {
			projection = mMap.getProjection();
		} catch (Exception e) {
			Log.i(TAG_onMapReady2, String.valueOf(e));
		}
		if (projection != null) {
			VisibleRegion visibleRegion = projection.getVisibleRegion();
			if (DebugOn) Log.i(TAG_onMapReady2, String.valueOf(visibleRegion));
			mMap.addCircle(new CircleOptions().center(visibleRegion.farLeft).radius(150));
			mMap.addCircle(new CircleOptions().center(visibleRegion.farRight).radius(150));
			mMap.addCircle(new CircleOptions().center(visibleRegion.nearLeft).radius(150));
			mMap.addCircle(new CircleOptions().center(visibleRegion.nearRight).radius(150));
			List<Station> stations = db.queryStation_specific2(visibleRegion);
			if (DebugOn) Log.i(TAG_onMapReady2, String.valueOf(stations));

			List<List<Station_route>> listOfStation_routeLists = new ArrayList<List<Station_route>>();
			for (int i = 0; i < stations.size(); i++){
				try {
					listOfStation_routeLists.add(db.queryStation_route_specific3(stations.get(i)));
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (stations != null) {
				for (int i=0; i < stations.size(); i++) {
					String allRouteMarks = "-";
					for (Station_route station_route : listOfStation_routeLists.get(i)) {
						try {
							allRouteMarks = allRouteMarks + station_route.getRoute().getRouteMark()
									.concat(" | ");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					LatLng latLng = new LatLng(stations.get(i).getGpsy(),
							stations.get(i).getGpsx());
					String stationName = stations.get(i).getName();
					MarkerOptions markerOptions = new MarkerOptions().position(latLng)
							.title(allRouteMarks).icon(BitmapDescriptorFactory.fromBitmap(
									resizeMapIcon("bus_station", 100, 100)));
					mMap.addMarker(markerOptions);
					markerOptionsList.add(markerOptions);
				}
			}
		} else {
			Log.i(TAG_onMapReady2, "projection is null");
		}

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
					(char) 'A', TRUE, "1");
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

	private void updateLocationUI() {
		if (mMap == null) {
			return;
		}

		 /*
		 * Request location permission, so that we can get the location of the
		 * device. The result of the permission request is handled by a callback,
		 * onRequestPermissionsResult.
		 */
		 Log.i("updateLocationUI", String.valueOf(PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION));
		if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
				android.Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			mLocationPermissionGranted = true;
		} else if (Build.VERSION.SDK_INT > 23) {requestPermissions(new String[]{android.Manifest.permission
						.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
		} else {
			//	TODO: if aap under 23 SDK version and permission not granted what to do?
		}

		if (mLocationPermissionGranted) {
			mMap.setMyLocationEnabled(true);
			mMap.getUiSettings().setMyLocationButtonEnabled(true);
		} else {
			mMap.setMyLocationEnabled(false);
			mMap.getUiSettings().setMyLocationButtonEnabled(false);
		}

	}


	@Override
	public void onRequestPermissionsResult(int requestCode,
										   @NonNull String permissions[],
										   @NonNull int[] grantResults) {
		mLocationPermissionGranted = false;
		switch (requestCode) {
			case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					mLocationPermissionGranted = true;
				} else {
					Log.w("errorPermission", "couldn't obtian permission :".concat
							(String.valueOf(grantResults)));
				}
			}
		}
		updateLocationUI();
	}

	private void getDeviceLocation() {

		 /*
		 * Before getting the device location, you must check location
		 * permission, Then: Get the best and most recent location of the device,
		 * which may be null in rare cases when a location is not available.
		 */
		if (mLocationPermissionGranted) {
			if (mCurrentLocation != null) {
				mLastKnownLocation = mCurrentLocation;
			} else {
				mLastKnownLocation = LocationServices.FusedLocationApi
						.getLastLocation(mGoogleApiClient);
			}
		}

		// Set the map's camera position to the current location of the device.
		if (mCameraPosition != null) {
			mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
		} else if (mLastKnownLocation != null) {
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
					new LatLng(mLastKnownLocation.getLatitude(),
							mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
		} else {
			/*
				CAN'T GET CURRENT LOCATION
			 */
			Log.w(TAG, "Current location is null..");
		}
	}


	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Log.w("ConnectioNfailed", "Connection failed :".concat(String.valueOf(connectionResult)));
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void  onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList("stations", markerOptionsList);
		outState.putParcelable("location", mLastKnownLocation);
		outState.putParcelable("camera_position", mMap.getCameraPosition());
	}

	/*
		resize of an icon.  getResource.getIdentifier returns a resource identifier for the given
		resource name. A little bit of a different way of getting a resource. It is discouraged.
	*/
	private Bitmap resizeMapIcon(String iconName, int width, int height) {
		Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources()
				.getIdentifier(iconName, "drawable", getPackageName()));
		Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
		return resizedBitmap;
	}

}

