package com.example.hw2;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener {

    public static List<Marker> markerList;
    private String MARKER_LIST_JSON="markerList.json";
    private static final int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 101;
    private  GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback locationCallback;
    Marker gpsMarker = null;
    private SensorManager sensorManager;
    private Sensor sensor;
    private boolean isAccWorking=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            restoreMarkerList();
            //Log.e("restore","restore worked");
        } catch (IOException e) {
            e.printStackTrace();
            //Log.e("restore",e.toString());
        }

        if (markerList == null) {
            markerList = new ArrayList<>();
        }
        FloatingActionButton circle =findViewById(R.id.circleButton);
        FloatingActionButton cross =findViewById(R.id.crossButton);
        TextView accelerationTextView=findViewById(R.id.accellerationTextView);
        circle.setVisibility(View.INVISIBLE);
        cross.setVisibility(View.INVISIBLE);
        accelerationTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        String json = new Gson().toJson(markerList);
        saveMarkerList(json);
    }

    private void restoreMarkerList() throws IOException {
        FileInputStream inputStream = null;
        int DEFAULT_BUFFER_SIZE = 10000;
        Gson gson = new Gson();
        String readJson;


        inputStream = openFileInput(MARKER_LIST_JSON);
        FileReader reader = null;

        reader = new FileReader(inputStream.getFD());

        char[] buf = new char[DEFAULT_BUFFER_SIZE];
        int n = 0;
        StringBuilder builder = new StringBuilder();
        while (true) {
            if (!((n = reader.read(buf)) >= 0)) break;
            String tmp = String.valueOf(buf);
            String substring = (n < DEFAULT_BUFFER_SIZE) ? tmp.substring(0, n) : tmp;
            builder.append(substring);
        }

        reader.close();

        readJson = builder.toString();
        Type collectionType = new TypeToken<List<Marker>>() {
        }.getType();
        List<Marker> o = gson.fromJson(readJson, collectionType);

        if (o != null) {
            for (Marker marker : o) {
                markerList.add(marker);
            }
        }


    }

    private void saveMarkerList(String json){
        FileOutputStream outputStream;
        try {
            outputStream=openFileOutput(MARKER_LIST_JSON,MODE_PRIVATE);
            FileWriter writer = new FileWriter(outputStream.getFD());
            writer.write(json);
            writer.close();
            //Log.e("save","saved!");
        } catch (FileNotFoundException e) {
            //Log.e("save",e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            //Log.e("save",e.toString());
            e.printStackTrace();
        }


    }

    public void zoomInClick(View v) {
        if (mMap != null) mMap.moveCamera(CameraUpdateFactory.zoomIn());
    }

    public void zoomOutClick(View v) {
        if (mMap != null) mMap.moveCamera(CameraUpdateFactory.zoomOut());
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
        mMap.setOnMapLoadedCallback(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);

    }


    @Override
    public void onMapLoaded() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }



    @Override
    public void onMapLongClick(LatLng latLng) {
        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).alpha(0.8f).title("Marker"));
        markerList.add(marker);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        FloatingActionButton circle =findViewById(R.id.circleButton);
        FloatingActionButton cross =findViewById(R.id.crossButton);

        circle.setVisibility(View.VISIBLE);
        ObjectAnimator animatorCircle = ObjectAnimator.ofFloat(circle, "alpha", 0f, 1f);
        animatorCircle.setDuration(1000);
        animatorCircle.start();

        cross.setVisibility(View.VISIBLE);
        ObjectAnimator animatorCross = ObjectAnimator.ofFloat(cross, "alpha", 0f, 1f);
        animatorCross.setDuration(1000);
        animatorCross.start();
        return false;
    }

    public void clearMemory(View view) {
        mMap.clear();
        markerList = null;
        markerList = new ArrayList<>();
    }

    public void hideButtons(View view) {
        FloatingActionButton circle =findViewById(R.id.circleButton);
        FloatingActionButton cross =findViewById(R.id.crossButton);


        ObjectAnimator animatorCircle = ObjectAnimator.ofFloat(circle, "alpha", 1f, 0f);
        animatorCircle.setDuration(1000);
        animatorCircle.start();


        ObjectAnimator animatorCross = ObjectAnimator.ofFloat(cross, "alpha", 1f, 0f);
        animatorCross.setDuration(1000);
        animatorCross.start();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        FloatingActionButton circle =findViewById(R.id.circleButton);
                        FloatingActionButton cross =findViewById(R.id.crossButton);
                        cross.setVisibility(View.INVISIBLE);
                        circle.setVisibility(View.INVISIBLE);
                    }
                },
                1000);
    }

    public void toggleAcc(View view) {
        if (isAccWorking == false) {
            isAccWorking = true;
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            sensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    TextView accelerationTextView = findViewById(R.id.accellerationTextView);
                    accelerationTextView.setText(String.format("Acceleration:\nx:%s y:%s", event.values[0], event.values[1]));
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }

            }, sensor, SensorManager.SENSOR_DELAY_FASTEST);


            TextView accelerationTextView = findViewById(R.id.accellerationTextView);
            accelerationTextView.setVisibility(View.VISIBLE);
        } else {
            isAccWorking = false;
            TextView accelerationTextView = findViewById(R.id.accellerationTextView);
            accelerationTextView.setVisibility(View.INVISIBLE);
            sensorManager = null;
            sensor = null;
        }

    }
}
