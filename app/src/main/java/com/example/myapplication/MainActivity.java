package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap map;

    private DatabaseReference databaseReference;


    Location myL;

    private LocationManager manager;

    private final int MIN_TIME=1000;
    private final int MIN_DISTANCE=1;

    Marker myMarker;

    //Button startB,stopB;


    public static int LOCATION_REQUEST_CODE = 101;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(this,"Started Working",Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this,locationTracker.class);
        startService(intent);


       databaseReference = FirebaseDatabase.getInstance().getReference().child("Online Users");

       FirebaseDatabase.getInstance().getReference().setValue("Locations");

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.MapView);

        supportMapFragment.getMapAsync(this);

        getLocationUpdates();
        readChanges();
    }
    private void readChanges() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    MyLocation location = snapshot.getValue(MyLocation.class);

                    if(location!=null){
                        myMarker.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (manager != null) {
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

            } else if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            } else {
                Toast.makeText(this, "No Provider Enabled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
            }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==LOCATION_REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getLocationUpdates();
            }else {
                Toast.makeText(this,"Permissions Requested",Toast.LENGTH_SHORT).show();
            }
        }
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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        map=googleMap;

        LatLng Sydney = new LatLng(22,75);

        myMarker=map.addMarker(new MarkerOptions().position(Sydney).title("Your Location"));
        map.moveCamera(CameraUpdateFactory.newLatLng(Sydney));
    }
}