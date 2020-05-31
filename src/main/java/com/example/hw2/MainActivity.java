package com.example.hw2;

import android.os.Bundle;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static List<Marker> markerList;
    private String MARKER_LIST_JSON="markerList.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            restoreMarkerList();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void zoomInClick(View view) {

    }
}
