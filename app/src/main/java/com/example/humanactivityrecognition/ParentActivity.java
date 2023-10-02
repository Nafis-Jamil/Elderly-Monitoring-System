package com.example.humanactivityrecognition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ParentActivity extends AppCompatActivity {

    private String username, f_longitude, f_latitude, res_body;
    TextView country, city, address,actView;
    ImageView location;
    private final String postUrl = MainActivity.postUrl + "get_location";
    public final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);

        getSupportActionBar().hide();

        username = getIntent().getStringExtra("username");

        country = findViewById(R.id.country);
        city = findViewById(R.id.city);
        address = findViewById(R.id.address);
        location = findViewById(R.id.imageViewLocation);
        actView= findViewById(R.id.activity);

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });


            getCntLocation();
    }

    private void openMap() {
        String s = "geo:0, 0?q=" + f_latitude + "," + f_longitude;
        Uri uri = Uri.parse(s);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    public void getCntLocation()  {
        getLstLocation();
        refresh(5000);
    }

    private void refresh(int milliseconds) {

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {

                    getCntLocation();

            }
        };

        handler.postDelayed(runnable, milliseconds);
    }

    private void getLstLocation()  {

        JSONObject jo = new JSONObject();
        try {
            jo.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            postRequest(postUrl, jo.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void postRequest(String postUrl, String postBody) throws IOException {

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
               res_body= response.body().string();
                JSONObject jsonObject = null;

                try {
                    String s_address, s_city, s_country, s_longitude, s_latitude,activity;
                    jsonObject = new JSONObject(res_body);
                    s_address = jsonObject.getString("address");
                    s_city = jsonObject.getString("city");
                    s_country = jsonObject.getString("country");
                    s_longitude = jsonObject.getString("longitude");
                    s_latitude = jsonObject.getString("latitude");
                    activity=jsonObject.getString("activity");
                    f_longitude = s_longitude;
                    f_latitude = s_latitude;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            address.setText(s_address);
                            city.setText(s_city);
                            country.setText(s_country);
                            actView.setText(activity);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}