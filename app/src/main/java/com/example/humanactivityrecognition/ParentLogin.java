package com.example.humanactivityrecognition;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ParentLogin extends AppCompatActivity {
    private final String postUrl= MainActivity.postUrl+"check_username_password";
    public final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private EditText username, password;
    private Button login, signup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_login);

        getSupportActionBar().hide();

        username = findViewById(R.id.edittextUsername);
        password = findViewById(R.id.passwordPassword);
        login = findViewById(R.id.Login);
        signup = findViewById(R.id.buttonSignup);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ParentLogin.this, ParentSignup.class);
                startActivity(intent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String user = username.getText().toString();
                String pass = password.getText().toString();

                if (TextUtils.isEmpty(user)) {
                    username.setError("Required");
                    return;
                }
                if (TextUtils.isEmpty(pass)) {
                    password.setError("Required");
                    return;
                }
                JSONObject jo = new JSONObject();
                try {
                    jo.put("username", user);
                    jo.put("password", pass);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    postRequest(postUrl,jo.toString());
                    Intent intent= new Intent(ParentLogin.this,ParentActivity.class);
                    intent.putExtra("username", user);
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });
    }

    void postRequest(String postUrl,String postBody) throws IOException {

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
                Log.d("TAG",response.body().string());
            }
        });
    }
}