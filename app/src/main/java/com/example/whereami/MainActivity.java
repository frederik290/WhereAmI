package com.example.whereami;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static com.google.android.gms.common.api.GoogleApiClient.*;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    private GoogleApiClient googleAPIClient;
    private Location lastKnownLocation;
    private boolean requestingUpdates;
    private TextView longitureView;
    private TextView latitudeView;
    private double currentLongitude = -1;
    private double currentLatitude = -1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestingUpdates = false;

        if(savedInstanceState != null){
            requestingUpdates = savedInstanceState.getBoolean("requestingUpdates", false);
            currentLongitude = savedInstanceState.getDouble("currentLongitude", 0);
            currentLatitude = savedInstanceState.getDouble("currentLatitude", 0);
        }

        requestLocationPermissionIfNeeded();
        longitureView = (TextView) findViewById(R.id.longitude_display);
        latitudeView = (TextView) findViewById(R.id.latitude_display);

        if (googleAPIClient == null) {
            googleAPIClient = new Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleAPIClient.connect();

    }

    @Override
    protected void onStop() {
        googleAPIClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause(){
        if(requestingUpdates){
            stopLocationUpdates();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (googleAPIClient.isConnected() && requestingUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("onConnected", "requestingUpdates: " + requestingUpdates);
        if (requestingUpdates) {
            startLocationUpdates();
            displayLastKnownLocation();

        }
    }

    //basically from: https://developer.android.com/training/permissions/requesting.html
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // task you need to do.
                } else {
                    // we will display a message to the user, telling them that the app will close since
                    // it is worth nothing without location!

                    showToast("Location denied, closing app!");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.finish();
                        }
                    }, 3200);
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "Location Changed!", Toast.LENGTH_SHORT).show();
        currentLongitude = location.getLongitude();
        currentLatitude = location.getLatitude();
        displayLastKnownLocation();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean("requestingUpdates", requestingUpdates);
        outState.putDouble("currentLongitude", currentLongitude);
        outState.putDouble("currentLatitude", currentLatitude);
        Log.d("onSaveInstanceState", "currentLongitude:" + currentLongitude);
    }

    private void requestLocationPermissionIfNeeded() {
        int permissionRequest = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions , PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void displayLastKnownLocation() {
        if(currentLongitude != -1){
            longitureView.setText(currentLongitude + "");
            latitudeView.setText(currentLatitude + "");
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(googleAPIClient, locationRequest, this);

        } else {
            requestLocationPermissionIfNeeded();
        }

    }

    private void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(googleAPIClient, this);
    }

    public void startTrackingLocationPressed(View view){
        requestingUpdates = true;
        startLocationUpdates();
    }

    public void stopTrackingLocationPressed(View view){
        if(requestingUpdates){
            stopLocationUpdates();
            requestingUpdates = false;
        }
    }

    private void showToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}


