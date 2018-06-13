package com.example.cartracking;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class ControlActivity extends AppCompatActivity {

    private static String TAG = "ControlActivity";
    private static String PHONE_NUMBER = "0020111";
    private static String MESSAGE = "message";
    private String time;
    private String key;
    private String ip;
    private String port;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        // pair here first and notify the user with the result
        // if connected enable the auth button
    }

    public void viewCarSMS(View view) {
        // send the sms
        // view the maps activity
    }

    public void viewCar(View view) {
        // view the maps activity
    }

    public void authenticate(View view) {
        // get the time and key
        // xor them
        // send the value via bluetooth
    }

    public void setConfig(View view) {
        // update the setting value of key and time;
    }
}
