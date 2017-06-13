package com.example.matija077.autotrolej;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.ExpandableListView;
import android.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.matija077.autotrolej.DirectionModules.DirRoute;
import com.example.matija077.autotrolej.DirectionModules.DirectionFinder;
import com.example.matija077.autotrolej.DirectionModules.DirectionFinderListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        autotrolej.asyncResponse, DirectionFinderListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = MapsActivity.class.getSimpleName();

    ArrayList<MarkerOptions> markerOptionsList = new ArrayList<MarkerOptions>();
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

    public static final Boolean DebugOn = TRUE;
    private static final String TAG_onMapReady2 = "OnMapReady-writing part";

    LatLng latLngSearch;
    MarkerOptions markerOptionsSerach;
    Marker markerSearch;
    private final static int RADIUS = 500;
    private List<List<MarkerOptions>> allRouteMarkers = new ArrayList<>();
    private List<Marker> drawnMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private Context mContext;
    private String mode;
    private List<List<DirRoute>> allDirRoutesSearch;
    private String searchAddressText;
    private List<List<Station_route>> matchingStationRoutes = new ArrayList<List<Station_route>>();
    private HashMap<String, Double> chosenRouteDist = new HashMap<String, Double>();
    private HashMap<String, String> chosenRouteTime = new HashMap<String, String>();

    MyInfoWindowAdapter infoWindow;

    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataHeader;
    private List<LatLng> listLatLngStations;
    private HashMap<String, List<String>> listDataChild;
    private String[] ruteArray = {"2 - Srdoči - 5 min","5 - Drenova - 7 min","7 - Pehlin - 10 min","1 - Bivio - 15 min"};
    private boolean searching = false;
    private boolean routeChosen = false;
    private boolean wasSearching = false;
    private int chosen = 0;

    private String mDate;
    private String mDay;

    private LocationManager locationManager;
    private LocationUpdateListener locationListener;
    private List<LatLng> locationStations;

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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mContext = this;

        ImageButton btnBack = (ImageButton) findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();

        final SearchView searchPlace = (SearchView) findViewById(R.id.searchStreet);

        searchPlace.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                progressDialog = ProgressDialog.show(mContext, "Molimo pričekajte.",
                        "Pretraživanje puteva u tijeku..!", true);

                new GeocoderTask().execute(query.toString());
                searchPlace.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.expList);

        // preparing list data
        //TODO: svaku min update podataka
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
        // Listview Group click listener
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                if(searching && groupPosition == 0){
                    if(routeChosen){
                        prepareListDataRoutes();
                        listAdapter.setNewItems(listDataHeader, listDataChild);
                        routeChosen = false;

                        int j = 0;
                        for(String header : listDataHeader){
                            expListView.expandGroup(j);
                            j++;
                        }

                        locationManager.removeUpdates(locationListener);
                    }else{
                        searching = false;
                        prepareListData();
                        listAdapter.setNewItems(listDataHeader, listDataChild);
                    }

                    if (polylinePaths != null) {
                        for (Polyline polyline:polylinePaths ) {
                            polyline.remove();
                        }
                    }

                    if (drawnMarkers != null) {
                        for (Marker marker : drawnMarkers) {
                            marker.remove();
                        }
                    }

                    if (markerSearch != null) {
                        markerSearch.remove();
                    }
                }
                else if(searching){
                    if(routeChosen){
                        drawRoute(chosen, true);
                    }else{
                        drawRoute(groupPosition - 1, true);
                    }
                    return true;
                }
                return false;
            }
        });

        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

                if(!searching && !wasSearching){
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(listLatLngStations.get(groupPosition)));
                    wasSearching = false;
                }

            }
        });

        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {

                if(!searching && !wasSearching){
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(listLatLngStations.get(groupPosition)));
                    wasSearching = false;
                }

            }
        });

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if(childPosition == 3 && searching){
                    prepareListDataChosen(matchingStationRoutes.get(groupPosition - 1));
                    listAdapter.setNewItems(listDataHeader, listDataChild);
                    drawRoute(groupPosition - 1, false);
                    chosen = groupPosition - 1;

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    routeChosen = true;
                }
                return false;
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationUpdateListener();
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {

        listDataHeader = new ArrayList<String>();
        listLatLngStations = new ArrayList<LatLng>();
        listDataChild = new HashMap<String, List<String>>();
        int j = 0;

        //TODO: dodat podatke iz baze
        // Adding child data
        listDataHeader.add("Turkovo");
        listLatLngStations.add(new LatLng(45.35, 14.39));
        listDataHeader.add("Pehlin");
        listLatLngStations.add(new LatLng(45.357, 14.391));

        for(String header : listDataHeader){

            List<String> child = new ArrayList<String>();

            for(int i = 0; i < ruteArray.length; i++){
                child.add(ruteArray[i]);
            }

            listDataChild.put(header, child);
        }

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        infoWindow = new MyInfoWindowAdapter(this.getLayoutInflater());
        infoWindow.setRuteArray(ruteArray);

        mMap.setInfoWindowAdapter(infoWindow);

/*        LatLng okretiste = new LatLng(45.35, 14.39);
        googleMap.addMarker(new MarkerOptions().position(okretiste).title("Turkovo")
                .icon(BitmapDescriptorFactory.fromBitmap(
                        resizeMapIcon("bus_station", 100, 100))));*/

        db = new OrmLiteDatabaseHelper(getApplicationContext());
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
                //mMap.setPadding(0, 400, 0, 0);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                //mMap.setPadding(0, 0, 0, 0);
                marker.showInfoWindow();
                lastOpened = marker;
                return  true;
            }
        });

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        /*if ((db.getWritableDatabase() != null) && (mLastKnownLocation != null)) {
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
			/*		}

				}
			});
		} else {
			Log.i(TAG_onMapReady2, String.valueOf(db.getWritableDatabase()).concat
					(String.valueOf(mLastKnownLocation)));
		}*/
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
            /*mMap.addCircle(new CircleOptions().center(visibleRegion.farLeft).radius(150));
            mMap.addCircle(new CircleOptions().center(visibleRegion.farRight).radius(150));
            mMap.addCircle(new CircleOptions().center(visibleRegion.nearLeft).radius(150));
            mMap.addCircle(new CircleOptions().center(visibleRegion.nearRight).radius(150));*/
            List<Station> stations = db.queryStation_specific2(visibleRegion);
            if (DebugOn) Log.i(TAG_onMapReady2, String.valueOf(stations));

            List<List<Station_route>> listOfStation_routeLists = new ArrayList<List<Station_route>>();
            for (int i = 0; i < stations.size(); i++) {
                try {
                    listOfStation_routeLists.add(db.queryStation_route_specific3(stations.get(i)));
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (stations != null) {
                for (int i = 0; i < stations.size(); i++) {
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

                    //TODO: napraviti ruteArray u formatu: br.linije - naziv smjera - za koliko min dolazi
                    //infoWindow.setRuteArray(ruteArray);

                    mMap.addMarker(markerOptions);
                    markerOptionsList.add(markerOptions);
                }
            }
        } else {
            Log.i(TAG_onMapReady2, "projection is null");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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
        } else  {requestPermissions(new String[]{android.Manifest.permission
                .ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
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
    public void processfinish(List<String> data) {

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

    private Bitmap resizeMapIcon(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources()
                .getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    //*** An AsyncTask Background Process
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {


        @Override
        protected List<Address> doInBackground(String... locationName) {
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

            if (polylinePaths != null) {
                for (Polyline polyline:polylinePaths ) {
                    polyline.remove();
                }
            }

            if (drawnMarkers != null) {
                for (Marker marker : drawnMarkers) {
                    marker.remove();
                }
            }

            // Clears last search marker on the map
            if (markerSearch != null) {
                markerSearch.remove();
            }

            if(addresses==null || addresses.size()==0){
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
                return;
            }

            SearchView searchPlace = (SearchView) findViewById(R.id.searchStreet);
            searchPlace.setQuery("", false);

            // Adding Markers on Google Map for each matching address
            //  for(int i=0;i<addresses.size();i++){
            //      Address address = (Address) addresses.get(i);
            Address address = (Address) addresses.get(0);
            // Creating an instance of GeoPoint, to display in Google Map
            latLngSearch = new LatLng(address.getLatitude(), address.getLongitude());

            searchAddressText = String.format("%s, %s",
                    address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                    address.getCountryName());

            markerOptionsSerach = new MarkerOptions();
            markerOptionsSerach.position(latLngSearch);
            markerOptionsSerach.title(searchAddressText);

            String[] empty = {" "};
            infoWindow.setRuteArray(empty);

            markerSearch = mMap.addMarker(markerOptionsSerach);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(markerSearch.getPosition());
            builder.include(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
            LatLngBounds bounds = builder.build();

            CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 100);
            mMap.moveCamera(update);
            mMap.animateCamera(update);

            searching = true;
            wasSearching = true;

            Location endPoint=new Location("");
            endPoint.setLatitude(address.getLatitude());
            endPoint.setLongitude(address.getLongitude());

            //priprema podatke za pokretanje algoritma
            prepareForAlgorithm(endPoint);
        }
    }

    private void prepareForAlgorithm(Location endPoint){

        //lista stanica u blizini nase lokacije
        List<Station> stationsStart = db.queryStation_specific3(mLastKnownLocation, RADIUS);

        List<List<Station_route>> stationRoutesStart = new ArrayList<List<Station_route>>();
        for (int i = 0; i < stationsStart.size(); i++) {
            try {
                stationRoutesStart.add(db.queryStation_route_specific3(stationsStart.get(i)));
            } catch (SQLException e) {
                Log.e("stanice", e.toString());
                e.printStackTrace();
            } catch (Exception e) {
                Log.e("stanice", "nes je " + e.toString());
                e.printStackTrace();
            }
        }

        //lista stanica u blizini trazene lokacije
        List<Station> stationsEnd = db.queryStation_specific3(endPoint, RADIUS);

        List<List<Station_route>> stationRoutesEnd = new ArrayList<List<Station_route>>();

        for (int i = 0; i < stationsEnd.size(); i++) {
            try {
                stationRoutesEnd.add(db.queryStation_route_specific3(stationsEnd.get(i)));
            } catch (SQLException e) {
                e.printStackTrace();
                Log.e("stanice", e.toString());
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("stanice", "nes je " + e.toString());
            }
        }

        //pronalazimo stanice iste rute istog smjera od naše lokacije do tražene
        matchingStationRoutes = new ArrayList<List<Station_route>>();
        chosenRouteDist = new HashMap<String, Double>();
        chosenRouteTime = new HashMap<String, String>();

        Date now = new Date();
        mDate = DateFormat.format("HH:mm:ss", now).toString();
        mDay = formatDay(DateFormat.format("EEE", now).toString());

        //Log.d("stanice", mDate + " " + mDay);

        if(stationsEnd != null || stationsStart != null) {
            for (int i = 0; i < stationsStart.size(); i++) {
                for (Station_route routeSrc : stationRoutesStart.get(i)) {
                    try {
                        for (int j = 0; j < stationsEnd.size(); j++) {
                            for (Station_route routeDst : stationRoutesEnd.get(j)) {
                                try {
                                    if (routeSrc.getRoute().getRouteMark().equals(routeDst.getRoute().getRouteMark()) && routeSrc.getDirection().equals(routeDst.getDirection())) {

                                        if (routeDst.getStationNumber() - routeSrc.getStationNumber() > 0) {

                                            //TODO: ne zračna udaljenost!

                                            Location srcStation = new Location("");
                                            srcStation.setLatitude(routeSrc.getStation().getGpsy());
                                            srcStation.setLongitude(routeSrc.getStation().getGpsx());

                                            Location dstStation = new Location("");
                                            dstStation.setLatitude(routeDst.getStation().getGpsy());
                                            dstStation.setLongitude(routeDst.getStation().getGpsx());

                                            double dist = mLastKnownLocation.distanceTo(srcStation) + endPoint.distanceTo(dstStation);
                                            String time = db.querySchedule_specificMapTime(routeSrc, mDate, mDay);

                                            String tempRouteMark = routeSrc.getRoute().getRouteMark().split("-")[0];

                                            if (chosenRouteDist.containsKey(tempRouteMark)) {

                                                //Integer test = time.compareTo(chosenRouteTime.get(tempRouteMark));
                                                //Log.d("stanice", test.toString() + "  NOVO: " + time + "  STARO:" + chosenRouteTime.get(tempRouteMark) + "   lin: " + tempRouteMark);

                                                if (dist < chosenRouteDist.get(tempRouteMark) && !time.equals("KASNO") && time.compareTo(chosenRouteTime.get(tempRouteMark)) < 0) {
                                                    for (List<Station_route> tempRoute : matchingStationRoutes) {
                                                        String tempMark = tempRoute.get(0).getRoute().getRouteMark().split("-")[0];
                                                        if (tempMark.equals(tempRouteMark)) {
                                                            matchingStationRoutes.remove(tempRoute);
                                                            break;
                                                        }
                                                    }

                                                    List<Station_route> tempList = new ArrayList<Station_route>();
                                                    tempList.add(routeSrc);
                                                    tempList.add(routeDst);
                                                    matchingStationRoutes.add(tempList);
                                                    chosenRouteDist.put(tempRouteMark, dist);
                                                    chosenRouteTime.put(tempRouteMark, time);
                                                }
                                            } else if(!time.equals("KASNO")){

                                                List<Station_route> tempList = new ArrayList<Station_route>();
                                                tempList.add(routeSrc);
                                                tempList.add(routeDst);
                                                matchingStationRoutes.add(tempList);
                                                chosenRouteDist.put(tempRouteMark, dist);
                                                chosenRouteTime.put(tempRouteMark, time);
                                            }
                                        }

                                    }
                                } catch (Exception e) {
                                    Log.e("stanice", e.toString());
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("stanice", e.toString());
                        e.printStackTrace();
                    }
                }
            }
        }else{
            //TODO: ispisati da nema stanica u blizini
            Log.d("stanice", "NEMA PUTA");
        }

        prepareListDataRoutes();
        listAdapter.setNewItems(listDataHeader, listDataChild);
        int j = 0;

        for(String header : listDataHeader){
            expListView.expandGroup(j);
            j++;
        }

        doAlgorithm(matchingStationRoutes, endPoint);
    }

    private void prepareListDataRoutes() {

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        listDataHeader.add("PONIŠTI PRETRAGU");

        String stationSrc = "";
        String stationDst = "";

        for (List<Station_route> tempList : matchingStationRoutes) {

            Station_route tempStationRoute = tempList.get(0);
            String tempStationName;

            if(tempStationRoute.getDirection() == "A")
                tempStationName = tempStationRoute.getRoute().getDirectionA();
            else
                tempStationName = tempStationRoute.getRoute().getDirectionB();

            listDataHeader.add(tempStationRoute.getRoute().getRouteMark().split("-")[0] + "  " + tempStationName);

            stationSrc = tempStationRoute.getStation().getName();
            stationDst = tempList.get(1).getStation().getName();
        }

        for(String header : listDataHeader){

            List<String> child = new ArrayList<String>();

            if(header == listDataHeader.get(0)) {
                child.add("ODREDIŠTE: " + searchAddressText);
            }else{
                child.add("  POČETNA STANICA: " + stationSrc);
                child.add("ODREDIŠNA STANICA: " + stationDst);
                child.add("          POLAZAK: " + chosenRouteTime.get(header.split(" ")[0]));
                child.add("ODABERI");
            }

            listDataChild.put(header, child);
        }
    }

    private void prepareListDataChosen(List<Station_route> route){
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        listDataHeader.add("NATRAG");
        String routeName;

        if(route.get(0).getDirection() == "A")
            routeName = route.get(0).getRoute().getDirectionA();
        else
            routeName = route.get(0).getRoute().getDirectionB();

        listDataHeader.add(route.get(0).getRoute().getRouteMark().split("-")[0] + " " + routeName);

        for(String header : listDataHeader){

            List<String> child = new ArrayList<String>();

            if(header == listDataHeader.get(0)) {
                child.add("ODREDIŠTE: " + searchAddressText);
            }else{
                child.add("  POČETNA STANICA: " + route.get(0).getStation().getName());
                child.add("ODREDIŠNA STANICA: " + route.get(1).getStation().getName());
                child.add("          POLAZAK: " + chosenRouteTime.get(header.split(" ")[0]));
            }

            listDataChild.put(header, child);
        }
    }

    private void doAlgorithm(List<List<Station_route>> matchingStationRoutes, Location endPoint){

        allDirRoutesSearch = new ArrayList<>();
        allRouteMarkers = new ArrayList<>();

        double waypointLat;
        double waypointLng;
        String waypointsStanice = "";


        for (List<Station_route> tempList : matchingStationRoutes) {

            waypointsStanice = "";

            Station_route tempStationRouteSrc = tempList.get(0);
            Station_route tempStationRouteDst = tempList.get(1);

            List<MarkerOptions> tempMarkers = new ArrayList<>();

            waypointLat = tempStationRouteSrc.getStation().getGpsy();
            waypointLng = tempStationRouteSrc.getStation().getGpsx();
            waypointsStanice += waypointLat + "," + waypointLng;

            tempMarkers.add(new MarkerOptions()
                    .title( tempStationRouteSrc.getStation().getName())
                    .position(new LatLng(waypointLat,waypointLng)).icon(BitmapDescriptorFactory.fromBitmap(
                            resizeMapIcon("bus_station", 100, 100))));
            try {
                List<Station_route> tempStationRoutes = db.queryStation_route_specific4(tempStationRouteSrc.getStationNumber(),
                                                                                        tempStationRouteDst.getStationNumber(),
                                                                                        tempStationRouteSrc.getRoute().getRouteMark(),
                                                                                        tempStationRouteSrc.getDirectionChar());

                for(Station_route station_route : tempStationRoutes){

                    waypointLat = station_route.getStation().getGpsy();
                    waypointLng = station_route.getStation().getGpsx();
                    waypointsStanice += "|" + waypointLat + "," + waypointLng;

                    tempMarkers.add((new MarkerOptions()
                            .title( station_route.getStation().getName())
                            .position(new LatLng(waypointLat,waypointLng)).icon(BitmapDescriptorFactory.fromBitmap(
                                    resizeMapIcon("bus_station", 100, 100)))));
                }

            } catch (SQLException e) {
                e.printStackTrace();
                Log.e("markeri", e.toString());
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("markeri", "nes je " + e.toString());
            }

            /*if(locTemp != null){
                locationStations.add(locTemp);
            }
            else{*/
            //    locationStations.add(locSrc);
            //}

            waypointLat = tempStationRouteDst.getStation().getGpsy();
            waypointLng = tempStationRouteDst.getStation().getGpsx();
            waypointsStanice += "|" + waypointLat + "," + waypointLng;

            //locationStations.add(locDst);

            tempMarkers.add((new MarkerOptions()
                    .title( tempStationRouteDst.getStation().getName())
                    .position(new LatLng(waypointLat,waypointLng)).icon(BitmapDescriptorFactory.fromBitmap(
                            resizeMapIcon("bus_station", 100, 100)))));

            allRouteMarkers.add(tempMarkers);

            mode = "walking";

            try {
                new DirectionFinder((DirectionFinderListener) mContext, mLastKnownLocation, endPoint, waypointsStanice, mode).execute();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        progressDialog.dismiss();

    }

    @Override
    public void onDirectionFinderStart() {

    }

    @Override
    public void onDirectionFinderSuccess(List<DirRoute> dirRoutes) {

        allDirRoutesSearch.add(dirRoutes);

    }

    private void drawRoute(int clickedGroup, boolean zoomOut){

        //polylinePaths = new ArrayList<>();
        List<DirRoute> dirRoutes = new ArrayList<>();

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }

        if (drawnMarkers != null) {
            for (Marker marker : drawnMarkers) {
                marker.remove();
            }
        }

        //ovisno koja je linija stisnuta
        dirRoutes = allDirRoutesSearch.get(clickedGroup);
        Log.d("putanja", dirRoutes.get(0).distance.text);

        for(MarkerOptions marker : allRouteMarkers.get(clickedGroup)){
            drawnMarkers.add(mMap.addMarker(marker));
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if(zoomOut){
            builder.include(markerSearch.getPosition());
            builder.include(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
            for (Marker marker : drawnMarkers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();

            CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 50);

            mMap.moveCamera(update);
            mMap.animateCamera(update);
        }else{
            builder.include(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
            builder.include(drawnMarkers.get(0).getPosition());
            LatLngBounds bounds = builder.build();
            CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 50);

            mMap.moveCamera(update);
            mMap.animateCamera(update);
        }

        for (DirRoute route : dirRoutes) {

            Log.d("putanja", "  Udaljenost: " + route.distance.text);
            Log.d("putanja", "  Trajanje: " + route.duration.text);

            int lineColor = Color.BLUE;
            float lineWidth = 5;

            /*if(mode.equals("walking")){
                lineColor = Color.BLUE;
                lineWidth = 5;
            }else{
                lineColor = Color.RED;
                lineWidth = 7;
            }*/

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(lineColor).
                    width(lineWidth);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

    private String formatDay(String day){
        if(day.equals("sub"))
            day = "subota";
        else if(day.equals("ned"))
            day = "nedjelja";
        else
            day = "radni dan";

        return day;
    }

    class LocationUpdateListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            //if(location.distanceTo(locationStations.get(0)) <= 10){
                Log.d("promjena", "na sljedecoj izlazis");
            //}else if(location.distanceTo(locationStations.get(1)) <= 10){
                Log.d("promjena", "sada izlazis");
            //}
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }

}
