package com.example.cartracking;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class ControlActivity extends AppCompatActivity {

    private static int REQUEST_ENABLE_BT = 1;
    private static String TAG = "ControlActivity";
    private static String PHONE_NUMBER = "0020111";
    private static String MESSAGE = "message";

    private Button authButton;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BluetoothSocket socket;

    private String time;
    private String key;
    private String ip;
    private String port;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        authButton = findViewById(R.id.authButton);
        socket = null;
        // pair here first and notify the user with the result
        // if connected enable the auth button

        if(connectToKit()){
            authButton.setEnabled(true);
            Toast.makeText(ControlActivity.this, "Connected via Bluetooth", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(ControlActivity.this, "Not Connected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }

    }

    private boolean connectToKit() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress();

                        // check the wanted device if found then begin connect
                        ParcelUuid[] uuids = device.getUuids();
                        try {
                            socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                            socket.connect();
                            outputStream = socket.getOutputStream();
                            inputStream = socket.getInputStream();
                            return true;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }

                        // else tell user to pair with the device first
                    }
                } else {
                    Log.e(TAG, "no paired devices");
                    // no paired devices
                    // enable discovery
                }

            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else {
            Log.e(TAG, "Device doesn't support Bluetooth");
        }
        return false;
    }

    private boolean write(String s) {
        try {
            outputStream.write(s.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                connectToKit();
            } else {
                // user say no or error in the bluetooth itself
                Log.e(TAG, "the user say no");
            }
        }
    }

    public void viewCarSMS(View view) {
        // send the sms
        // view the maps activity
    }

    public void viewCar(View view) {
        // view the maps activity
    }

    public void authenticate(View view) {
        String value = "";
        // get the time and key
        // xor them
        // send the value via bluetooth
        if(write(value)){
            Toast.makeText(ControlActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(ControlActivity.this, "Error in sending message", Toast.LENGTH_SHORT).show();
        }
    }

    public void setConfig(View view) {
        // update the setting value of key and time;
    }
}
