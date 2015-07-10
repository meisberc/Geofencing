package lbs.de.geofencing;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.DbAdapter;
import database.Point;
import geofence.Constants;
import geofence.GeofenceErrorMessages;
import geofence.GeofenceTransitionsIntentService;
import gpstracker.GPSTracker;
import path.HttpConnection;
import path.PathJSONParser;

public class MapsActivity extends FragmentActivity implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status>, LocationListener, GpsStatus.Listener {
    protected static final String TAG = "monitoring-geofences";
    public static final String POINT = "triggering-point";

    private String tourName;

    private final BroadcastReceiver startReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive()");
            if (intent.getAction().equals(GeofenceTransitionsIntentService.STARTPOI)) {
                String point = intent.getExtras().getString(MapsActivity.POINT);
                Intent i = new Intent(getApplicationContext(), POI2Activity.class);
                i.putExtra(POINT, point);
                i.putExtra(MainActivity.TOURNAME, tourName);
                Log.i(TAG, "startActivityForResult()");


                LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getGeofencePendingIntent());
                mGeofenceList.remove(0);
                startReceiver.abortBroadcast();
                startActivityForResult(i, 1);

            }
        }
    };

    //     Provides the entry point to Google Play services.
    protected GoogleApiClient mGoogleApiClient;

    //     The list of geofences used in this sample
    protected ArrayList<Geofence> mGeofenceList;

    /**
     * Used to keep track of whether geofences were added.
     */
    private boolean mGeofencesAdded;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ArrayList<Point> points;
    private DbAdapter dbAdapter = MainActivity.getDbAdapter();
    private boolean cameraMoved;
    private Location myLocation;
    private int aktPointNr = 0;
    private Point newPoint;
    private boolean comeFromResult;
    private boolean firstStart = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Log.i(TAG, "on Create()");

        setUpMapIfNeeded();
        buildGoogleApiClient();
        /**
         *Prüfung, ob Google Play Services installiert sind, muss noch implementiert werden,
         * um direkte Crashes der App zu vermeiden.
         * Wenn Play Services nicht installiert sind, wird ein Dialog geöffnet und die App beendet
         */
        if (!isGooglePlayServiceAvailable()) {
            showPlayServiceAlert();
        }

        tourName = getIntent().getExtras().getString(MainActivity.TOURNAME);
        dbAdapter.openRead();
        points = dbAdapter.getPoints(tourName);

        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<>();

        populateGeofenceList();

        setUpListener();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!cameraMoved) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng
                    (location.getLatitude(), location.getLongitude()), 18));
            myLocation = location;
            if (firstStart) {
                drawNewLine();
                firstStart = false;
            }
        }
    }

    private void drawNewLine() {
        mMap.clear();
//        addMarkers();
        addNextMarker();
        drawLineBetweenNextPoints();
    }

    private void showPlayServiceAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle(R.string.googlePlay);

        // Setting Dialog Message
        alertDialog.setMessage(R.string.googlePlayError);

        // on pressing cancel button
        alertDialog.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void drawLineBetweenNextPoints() {
        Point oldPoint;
        if (aktPointNr == 0) {
            oldPoint = new Point("My Location", myLocation.getLatitude(), myLocation.getLongitude());
            newPoint = points.get(aktPointNr);
        } else {
            oldPoint = newPoint;
            newPoint = points.get(aktPointNr);
        }
        LatLng newLatLng = new LatLng(newPoint.getLatitude(), newPoint.getLongitude());
        LatLng oldLatLng = new LatLng(oldPoint.getLatitude(), oldPoint.getLongitude());

        String url = getDirectionsUrl(oldLatLng, newLatLng);
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);
    }

    private void setUpListener() {

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (!cameraMoved) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng
                            (location.getLatitude(), location.getLongitude()), 18));
                    myLocation = location;
                    if (firstStart) {
                        drawNewLine();
                        firstStart = false;
                    }
                }
            }
        });

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                cameraMoved = true;
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (mMap.getMyLocation() != null) {
                    cameraMoved = false;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng
                            (mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude()), 18));
                }
                return false;
            }
        });


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });
    }

    public void centerMap(Location location) {
        if (location != null) {
            CameraUpdate center =
                    CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),
                            location.getLongitude()));

            mMap.moveCamera(center);
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        //Mode walking
        String mode = "mode=walking";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    private boolean isGooglePlayServiceAvailable() {
        int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        return errorCode == ConnectionResult.SUCCESS;
    }

    public void addNextMarker() {
        Point p = points.get(aktPointNr);
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(p.getLatitude(), p.getLongitude())).title(p.getName()));
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();

            }
        }

    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_NORMAL)
                .compassEnabled(true)
                .rotateGesturesEnabled(true)
                .tiltGesturesEnabled(false)
                .mapType(GoogleMap.MAP_TYPE_NORMAL)
                .mapToolbarEnabled(false);
        mMap.setMyLocationEnabled(true);


    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle(R.string.gps_settings);

        // Setting Dialog Message
        alertDialog.setMessage(R.string.gps_not_enabled);

        // On pressing Settings button
        alertDialog.setPositiveButton(R.string.action_settings, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    public void populateGeofenceList() {
        HashMap<String, LatLng> geo = new HashMap<>();
        for (int i = aktPointNr; i < points.size(); i++) {
            Point p = points.get(i);
            geo.put(p.getName(), new LatLng(p.getLatitude(), p.getLongitude()));
        }

        for (Map.Entry<String, LatLng> entry : geo.entrySet()) {

            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())

                            // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )

                            // Set the expiration duration of the geofence. This geofence gets automatically
                            // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                            // Set the transition types of interest. Alerts are only generated for these
                            // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                            // Create the geofence.
                    .build());
        }

    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(this);
        if (comeFromResult) {
            drawNewLine();
            comeFromResult = false;
        }

        startLocationUpdates();
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        Log.i(TAG, "Connection suspended");

        // onConnected() will be called again automatically when the service reconnects
    }


    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Update state and save in shared preferences.
            mGeofencesAdded = !mGeofencesAdded;

            // Update the UI. Adding geofences enables the Remove Geofences button, and removing
            // geofences enables the Add Geofences button.

            Toast.makeText(
                    this,
                    mGeofencesAdded ? "added" :
                            "removed",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
        dbAdapter.close();
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }

        unregisterReceiver(startReceiver);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");

        dbAdapter.openRead();
        setUpMapIfNeeded();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        } else {
            buildGoogleApiClient();
        }
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
        checkGPS();
        registerBroadcastReceiver();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        intent.putExtra(MainActivity.TOURNAME, tourName);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        Log.i(TAG, "onActivityResult()");
        if (resultCode == RESULT_OK && requestCode == 1) {
            if (mGoogleApiClient != null) {
                Log.i(TAG, "mGoogleApiClient.connect()");
                mGoogleApiClient.connect();
            } else {
                Log.i(TAG, " buildGoogleApiClient()");
                buildGoogleApiClient();
            }
//            String name = intent.getExtras().getString(MapsActivity.POINT);
            tourName = intent.getExtras().getString(MainActivity.TOURNAME);
            dbAdapter.openWrite();
            points = dbAdapter.getPoints(tourName);
            comeFromResult = true;
            aktPointNr++;

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        Log.i(TAG, "onSaveInstanceState()");
        state.putString(MainActivity.TOURNAME, tourName);

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle state) {
        super.onRestoreInstanceState(state);
        Log.i(TAG, "onRestoreInstanceState()");

        tourName = state.getString(MainActivity.TOURNAME);
        dbAdapter.openRead();
        points = dbAdapter.getPoints(tourName);
        setUpMapIfNeeded();
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle(R.string.exiting);

        // Setting Dialog Message
        alertDialog.setMessage(R.string.exitError);

        // on pressing cancel button
        alertDialog.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getGeofencePendingIntent());
                MapsActivity.this.finish();
            }
        });

        alertDialog.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(GeofenceTransitionsIntentService.STARTPOI);
        registerReceiver(startReceiver, filter);
    }

    public void checkGPS() {
        GPSTracker tmpTracker = new GPSTracker(this);
        tmpTracker.getLocation();
        if (!tmpTracker.canGetLocation()) {
            showSettingsAlert();
        } else {
            Location location = tmpTracker.getLocation();
            myLocation = location;
            centerMap(location);
            drawNewLine();
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    public void onGpsStatusChanged(int event) {
        if (event == GpsStatus.GPS_EVENT_STOPPED) {
            showSettingsAlert();
        }
    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(8);
                polyLineOptions.color(Color.BLUE);
            }

            mMap.addPolyline(polyLineOptions);

        }

    }


}


