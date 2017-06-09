package com.example.matija077.autotrolej;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ExpandableListView;
import android.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        autotrolej.asyncResponse {

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

    MyInfoWindowAdapter infoWindow;

    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataHeader;
    private List<LatLng> listLatLngStations;
    private HashMap<String, List<String>> listDataChild;
    private String[] ruteArray = {"2 - Srdoči - 5 min","5 - Drenova - 7 min","7 - Pehlin - 10 min","1 - Bivio - 15 min"};
    private boolean searching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

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
                    searching = false;
                    prepareListData();
                    listAdapter.setListDataHeader(listDataHeader);
                    listAdapter.setListDataChild(listDataChild);
                    listAdapter.notifyDataSetChanged();
                }
                else if(searching){
                    return true;
                }
                return false;
            }
        });

        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

                if(!searching) mMap.animateCamera(CameraUpdateFactory.newLatLng(listLatLngStations.get(groupPosition)));

            }
        });

        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {

                mMap.animateCamera(CameraUpdateFactory.newLatLng(listLatLngStations.get(groupPosition)));

            }
        });
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {

        listDataHeader = new ArrayList<String>();
        listLatLngStations = new ArrayList<LatLng>();
        listDataChild = new HashMap<String, List<String>>();
        int j = 0;

        if(!searching){
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
        }else{
            listDataHeader.add("PONIŠTI PRETRAGU");
            listDataHeader.add("odstanice - linija - dostanice");

            for(String header : listDataHeader){
                expListView.expandGroup(j);
                j++;

                List<String> child = new ArrayList<String>();

                if(header == listDataHeader.get(0)) {
                    child.add(" ");
                }else{
                    child.add("vrijeme potrebno, ili neki drugi podatci");
                }

                listDataChild.put(header, child);
            }
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

        LatLng okretiste = new LatLng(45.35, 14.39);
        googleMap.addMarker(new MarkerOptions().position(okretiste).title("Turkovo")
                .icon(BitmapDescriptorFactory.fromBitmap(
                        resizeMapIcon("bus_station", 100, 100))));

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
                mMap.setPadding(0, 400, 0, 0);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                mMap.setPadding(0, 0, 0, 0);
                marker.showInfoWindow();
                lastOpened = marker;
                return  true;
            }
        });

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

/*        if ((db.getWritableDatabase() != null) && (mLastKnownLocation != null)) {
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
/*					}
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
            mMap.addCircle(new CircleOptions().center(visibleRegion.farLeft).radius(150));
            mMap.addCircle(new CircleOptions().center(visibleRegion.farRight).radius(150));
            mMap.addCircle(new CircleOptions().center(visibleRegion.nearLeft).radius(150));
            mMap.addCircle(new CircleOptions().center(visibleRegion.nearRight).radius(150));
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
//            for(int i=0;i<addresses.size();i++){
//                Address address = (Address) addresses.get(i);
            Address address = (Address) addresses.get(0);
            // Creating an instance of GeoPoint, to display in Google Map
            latLngSearch = new LatLng(address.getLatitude(), address.getLongitude());

            String addressText = String.format("%s, %s",
                    address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                    address.getCountryName());

            markerOptionsSerach = new MarkerOptions();
            markerOptionsSerach.position(latLngSearch);
            markerOptionsSerach.title(addressText);
            infoWindow.setRuteArray(null);
            markerSearch = mMap.addMarker(markerOptionsSerach);

            searching = true;
            //TODO:pozovi algoritam
            prepareListData();
            listAdapter.setListDataHeader(listDataHeader);
            listAdapter.setListDataChild(listDataChild);
            listAdapter.notifyDataSetChanged();
//            }
        }
    }
}
