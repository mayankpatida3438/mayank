package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.database.DatabaseReference;

import java.util.Timer;
import java.util.TimerTask;


public class locationTracker extends Service implements LocationListener{

    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    DatabaseReference databaseReference;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LocationTracker(context);
        startLocationUpdates();
        stopLocationUpdates();

        return super.onStartCommand(intent, flags, startId);
    }

    public void LocationTracker(Context context) {
        this.context = context;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        Log.d(TAG, "Latitude: " + location.getLatitude() +
                                ", Longitude: " + location.getLongitude());
                        saveLocationToFirebase(location.getLatitude(), location.getLongitude());
                    }
                }
            }
        };
    }

    public void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3600000); // 1 hour in milliseconds
        locationRequest.setFastestInterval(3600000); // 1 hour in milliseconds

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, null);
    }

    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void saveLocationToFirebase(double latitude, double longitude) {
// Save the location to Firebase
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        if(location != null){
            saveLocation(location);
        }else {
            Toast.makeText(this,"No Location",Toast.LENGTH_SHORT).show();
        }
    }
    private void saveLocation(Location location) {
        databaseReference.setValue(location);
        //  Intent intent = new Intent(this,LocationService.class);
    }
}
