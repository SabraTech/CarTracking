package com.example.cartracking;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private Marker now;
    private int interval;
    private Handler locationHandler;
    private Runnable runnable;
    private String serverIp;
    private int serverPort;
    private boolean oneTimeRead, isFirst;
    private TCPClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        isFirst = true;
        Intent data = getIntent();
        serverIp = data.getStringExtra(StaticConfig.STR_EXTRA_IP);
        oneTimeRead = Boolean.parseBoolean(data.getStringExtra(StaticConfig.STR_EXTRA_STATIC));
        interval = Integer.parseInt(data.getStringExtra(StaticConfig.STR_EXTRA_TIME));
        serverPort = Integer.parseInt(data.getStringExtra(StaticConfig.STR_EXTRA_PORT));


        client = new TCPClient();

        locationHandler = new Handler();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(oneTimeRead){
            updateLocation();
        }else{
            scheduleUpdateLocation();
        }
    }

    @Override
    public void onBackPressed() {
        client.Disconnect();
        locationHandler.removeCallbacks(runnable);
        super.onBackPressed();
    }

    private void scheduleUpdateLocation() {
        runnable = new Runnable() {
            @Override
            public void run() {
                updateLocation();
                locationHandler.postDelayed(this, interval);
            }
        };
        locationHandler.postDelayed(runnable, interval);
    }

    private void updateLocation() {
        // send request to server and get the location
        client.Connect(serverIp, serverPort);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!client.isConnected()){
            Log.e(TAG, "Not connected to server");
            Toast.makeText(MapsActivity.this, "Connection Lost!", Toast.LENGTH_SHORT).show();
        }else{
            String response = client.ReceiveData(); // "31$12.3822$N@29$55.5140$S" || "NODATA";
            client.Disconnect();
            if(response == null){
                Toast.makeText(MapsActivity.this, "Server Down!", Toast.LENGTH_SHORT).show();
            }else if(response.trim().equalsIgnoreCase("NODATA")){
                if(isFirst){
                    Toast.makeText(MapsActivity.this, "No Location Data on Server view Default!", Toast.LENGTH_SHORT).show();
                    double lat = 31.205753, lng = 29.924526;
                    updateMarker(lat, lng);
                }else{
                    Toast.makeText(MapsActivity.this, "Location Static No Change!", Toast.LENGTH_SHORT).show();
                }
            } else {
                String parts[] = response.split("@");
                String latDms = parts[0].trim();
                String lngDms = parts[1].trim();
                double lat = getDegreeLocation(latDms);
                double lng = getDegreeLocation(lngDms);
                updateMarker(lat, lng);
            }
        }

    }

    private void updateMarker(double lat, double lng) {
        LatLng location = new LatLng(lat, lng);
        if (now == null) {
            now = mMap.addMarker(new MarkerOptions().position(location).title("My Car"));
        } else {
            now.setPosition(location);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    private double getDegreeLocation(String dms){
        String[] parts = dms.split("\\$");
        if(parts.length != 3){
            throw new RuntimeException("String DMS not in right format");
        }
        double degree = Double.parseDouble(parts[0].trim());
        double minutes = Double.parseDouble(parts[1].trim());

        double value = degree + (minutes / 60);

        if(parts[2].trim().equalsIgnoreCase("S") || parts[2].trim().equalsIgnoreCase("W")){
            value = value * -1;
        }

        return value;
    }

}
