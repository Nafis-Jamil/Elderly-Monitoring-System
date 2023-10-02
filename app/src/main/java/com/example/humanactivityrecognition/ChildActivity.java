package com.example.humanactivityrecognition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChildActivity extends AppCompatActivity implements SensorEventListener {
    private final String locUrl = MainActivity.postUrl + "update_location";
    private final String callUrl = MainActivity.postUrl + "get_number";
    private final String sensorUrl= MainActivity.postUrl + "activity";
    public final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    FusedLocationProviderClient fusedLocationProviderClient;
    ImageView emergencycall;
    private final static int REQUEST_CODE = 100;
    static int PERMISSION_CODE = 100;
    private String username;
    //...................................................
    private SensorManager sensorManager;
    private static Sensor acc, lin_acc, gyro;
    private static final String[] activities = {"Walking", "stairs", "Jogging", "Sitting", "Standing", "stairs", "Walking"};
    private int count[] = new int[7];
    private static final int N_SAMPLES = 100;
    private static int index = 0;
    private int counter=0;
    private String current_activity;

    private static List<Float> ax;
    private static List<Float> ay;
    private static List<Float> az;
    private static List<Float> lx;
    private static List<Float> ly;
    private static List<Float> lz;
    private static List<Float> gx;
    private static List<Float> gy;
    private static List<Float> gz;
    //.........................................................
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        getSupportActionBar().hide();

        username = getIntent().getStringExtra("username");


        emergencycall = findViewById(R.id.buttonCall);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        emergencycall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    call();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //...........................................................

        ax = new ArrayList<>();
        ay = new ArrayList<>();
        az = new ArrayList<>();
        lx = new ArrayList<>();
        ly = new ArrayList<>();
        lz = new ArrayList<>();
        gx = new ArrayList<>();
        gy = new ArrayList<>();
        gz = new ArrayList<>();


        for (int i = 0; i < count.length; i++) {
            count[i] = 0;
        }
        counter = 0;
        current_activity="null";


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lin_acc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        setupSensors();

        getContinuousLocation();

    }

    //....................................................................



    void setupSensors() {
        sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, lin_acc, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);
    }
    void sensorPostRequest(String postUrl, String postBody) throws IOException {

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON, postBody);

        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String rec_data = response.body().string();

                try {
                    JSONObject object = new JSONObject(rec_data);


                    index = object.getInt("max");
                    count[index]++;
                    counter++;


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        activity_prediction();

        Sensor sensor = sensorEvent.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            ax.add(sensorEvent.values[0]);
            ay.add(sensorEvent.values[1]);
            az.add(sensorEvent.values[2]);

        } else if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            lx.add(sensorEvent.values[0]);
            ly.add(sensorEvent.values[1]);
            lz.add(sensorEvent.values[2]);

        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gx.add(sensorEvent.values[0]);
            gy.add(sensorEvent.values[1]);
            gz.add(sensorEvent.values[2]);

        }
    }

    private void activity_prediction() {
        if (counter == 10) {
            int maxAt = 0;

            for (int i = 0; i < count.length; i++) {
                maxAt = count[i] > count[maxAt] ? i : maxAt;
            }

            current_activity=activities[maxAt];
            System.out.println(current_activity);

            counter = 0;
            for (int i = 0; i < count.length; i++) {
                count[i] = 0;
            }
        }
        List<Float> data = new ArrayList<>();

        if (ax.size() >= N_SAMPLES && ay.size() >= N_SAMPLES && az.size() >= N_SAMPLES
                && lx.size() >= N_SAMPLES && ly.size() >= N_SAMPLES && lz.size() >= N_SAMPLES
                && gx.size() >= N_SAMPLES && gy.size() >= N_SAMPLES && gz.size() >= N_SAMPLES
        ) {



            data.addAll(ax.subList(0, N_SAMPLES));
            data.addAll(ay.subList(0, N_SAMPLES));
            data.addAll(az.subList(0, N_SAMPLES));

            data.addAll(lx.subList(0, N_SAMPLES));
            data.addAll(ly.subList(0, N_SAMPLES));
            data.addAll(lz.subList(0, N_SAMPLES));

            data.addAll(gx.subList(0, N_SAMPLES));
            data.addAll(gy.subList(0, N_SAMPLES));
            data.addAll(gz.subList(0, N_SAMPLES));



            JSONArray jsonArray = new JSONArray(data);
            final String jsondata = jsonArray.toString();
            try {
                sensorPostRequest(sensorUrl, jsondata);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ax.clear();
            ay.clear();
            az.clear();
            lx.clear();
            ly.clear();
            lz.clear();
            gx.clear();
            gy.clear();
            gz.clear();

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

//...........................................................................

    public void getContinuousLocation() {
        getLastLocation();
        refresh(5000);
    }

    private void refresh(int milliseconds) {

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getContinuousLocation();
            }
        };

        handler.postDelayed(runnable, milliseconds);
    }

    private void call() throws JSONException {

        JSONObject jo = new JSONObject();
        jo.put("username", username);

        try {
            callPostRequest(callUrl, jo.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void getLastLocation() {


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                Geocoder geocoder = new Geocoder(ChildActivity.this, Locale.getDefault());
                                List<Address> addresses = null;
                                try {

                                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                                    String s_address, s_city, s_country;
                                    Double s_longitude, s_latitude;

                                    s_latitude = addresses.get(0).getLatitude();
                                    s_longitude = addresses.get(0).getLongitude();
                                    s_address = addresses.get(0).getAddressLine(0);
                                    s_city = addresses.get(0).getLocality();
                                    s_country = addresses.get(0).getCountryName();
                                    JSONObject jo = new JSONObject();
                                    try {
                                        jo.put("username", username);
                                        jo.put("latitude", s_latitude);
                                        jo.put("longitude", s_longitude);
                                        jo.put("address", s_address);
                                        jo.put("city", s_city);
                                        jo.put("country", s_country);
                                        jo.put("activity", current_activity);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        locPostRequest(locUrl, jo.toString());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }


                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(ChildActivity.this, "Permission Required", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        } else {
            askPermission();
        }
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(ChildActivity.this, new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Required Permission", Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    void locPostRequest(String postUrl, String postBody) throws IOException {

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON, postBody);

        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                System.out.println("error happened");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TAG", response.body().string());
            }
        });
    }

    void callPostRequest(String postUrl, String postBody) throws IOException {

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON, postBody);

        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                System.out.println("error happened");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String number = response.body().string();
                Log.i("Number", number);
                String phone = "tel:" + number;
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(phone));

                if (ContextCompat.checkSelfPermission(ChildActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChildActivity.this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);
                }
                startActivity(intent);
            }
        });
    }

    //......................................................................

    @Override
    protected void onResume() {
        super.onResume();
        setupSensors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
}