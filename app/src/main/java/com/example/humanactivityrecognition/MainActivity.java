package com.example.humanactivityrecognition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
//    public static String postUrl= "http://192.168.0.106:5000/";
    public static String postUrl= "http://192.168.64.49:5000/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        Button parent, child;
        parent = findViewById(R.id.buttonParent);
        child = findViewById(R.id.buttonChild);

        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ParentLogin.class);
                startActivity(intent);
            }
        });

        child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChildLogin.class);
                startActivity(intent);
            }
        });

//        int SDK_INT = android.os.Build.VERSION.SDK_INT;
//        if (SDK_INT > 8)
//        {
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
//                    .permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//            //your codes here
//            super.onCreate(savedInstanceState);
//            setContentView(R.layout.activity_main);
//
//            OkHttpClient okHttpClient=new OkHttpClient();
//
//            RequestBody formbody=new FormBody.Builder().add("username", "aaa").add("password", "123").build();
//            RequestBody formbody2=new FormBody.Builder().add("username", "aaa").build();
//
//            Request request = new Request.Builder().url("http://192.169.1.119:5000/delete").post(formbody2).build();
//            okHttpClient.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(@NonNull Call call, @NonNull IOException e) {
////                Toast.makeText(MainActivity.this, "network not found", Toast.LENGTH_LONG).show();
//                }
//
//                @Override
//                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                    final TextView textView=findViewById(R.id.textview);
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                textView.setText(response.body().string());
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//
//                }
//            });
//        }


    }
}