package com.example.cartracking;

import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker now;
    private static final int INTERVAL = 5000;
    private Handler locationHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationHandler = new Handler();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        scheduleUpdateLocation();
    }

    @Override
    public void onBackPressed() {
        locationHandler.removeCallbacksAndMessages(null);
        super.onBackPressed();
    }

    private void scheduleUpdateLocation() {
        locationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLocation();
                locationHandler.postDelayed(this, INTERVAL);
            }
        }, INTERVAL);
    }

    private void updateLocation() {
        // send request to server and get the location
        double lat = 31.205753, lng = 29.924526;
        updateMarker(lat, lng);
    }

    private void updateMarker(double lat, double lng) {
        LatLng location = new LatLng(lat, lng);
        if (now == null) {
            now = mMap.addMarker(new MarkerOptions().position(location).title("My Car"));
        } else {
            now.setPosition(location);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
    }
}
