package com.example.cartracking;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

public class ControlActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "ControlActivity";

    private static final String MESSAGE = "LOC";
    private static final String KIT_NAME = "name";
    private static final String KIT_MAC = "00:06:66:87:C7:A8";

    private String phoneNumber = "01068435908";
    private String xorNum = "1111";
    private String key = "5060";
    private String ip = "156.218.50.52";
    private String port = "999";
    private String serverInterval = "5000";

    private BluetoothSocket socket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        socket = null;
        // pair here first and notify the user with the result
        // if connected enable the auth button

        if (connectToKit()) {
            Toast.makeText(ControlActivity.this, "Connected via Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ControlActivity.this, "Not Connected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
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
                        if (deviceHardwareAddress.equals(KIT_MAC)) {
                            ParcelUuid[] uuids = device.getUuids();
                            bluetoothAdapter.cancelDiscovery();
                            try {
                                Class<?> clazz = device.getClass();
                                Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};

                                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                                Object[] params = new Object[] {Integer.valueOf(1)};

                                socket = (BluetoothSocket) m.invoke(device, params);
                                socket.connect();
                                return true;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return false;
                            }
                        }
                    }
                    // else tell user to pair with the device first
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

    public void viewCarSMS(View view) {
        // send the sms
        sendSMSMessage();
        // view the maps activity
        Intent intent = new Intent(ControlActivity.this, MapsActivity.class);
        intent.putExtra(StaticConfig.STR_EXTRA_IP, ip);
        intent.putExtra(StaticConfig.STR_EXTRA_PORT, port);
        intent.putExtra(StaticConfig.STR_EXTRA_TIME, serverInterval);
        intent.putExtra(StaticConfig.STR_EXTRA_STATIC, true);
        startActivity(intent);
    }

    protected void sendSMSMessage() {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, MESSAGE, null, null);
        Toast.makeText(getApplicationContext(), "SMS sent", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNumber, null, MESSAGE, null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void viewCar(View view) {
        // view the maps activity
        Intent intent = new Intent(ControlActivity.this, MapsActivity.class);
        intent.putExtra(StaticConfig.STR_EXTRA_IP, ip);
        intent.putExtra(StaticConfig.STR_EXTRA_PORT, port);
        intent.putExtra(StaticConfig.STR_EXTRA_TIME, serverInterval);
        intent.putExtra(StaticConfig.STR_EXTRA_STATIC, false);
        startActivity(intent);
    }

    public void authenticate(View view) {
        // xor the two values
        String value = makeXor(xorNum.getBytes(), key.getBytes());

        if(socket.isConnected()){
            try {
                socket.getOutputStream().write(value.getBytes());
                socket.getOutputStream().flush();
                Toast.makeText(ControlActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(ControlActivity.this, "Error in sending message", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(ControlActivity.this, "Bluetooth connection lost!", Toast.LENGTH_SHORT).show();
        }


    }

    private String makeXor(byte[] time, byte[] key) {
        byte[] out = new byte[time.length];
        for (int i = 0; i < time.length; i++) {
            out[i] = (byte) (time[i] ^ key[i % key.length]);
        }
        return new String(out);
    }

    public void setConfig(View view) {
        // update the setting value of key and time;
        startActivityForResult(new Intent(this, SettingsActivity.class), StaticConfig.REQUEST_CODE_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT: {
                if (resultCode == RESULT_OK) {
                    connectToKit();
                } else {
                    // user say no or error in the bluetooth itself
                    Log.e(TAG, "the user say no");
                }
                break;
            }
            case StaticConfig.REQUEST_CODE_SETTINGS: {
                // get the data from the intent and set to the new values;
                if(resultCode == RESULT_CANCELED){
                    Log.d(TAG, "Back button pressed");
                }else if(resultCode == RESULT_OK){
                    String admin = data.getStringExtra(StaticConfig.STR_EXTRA_ADMIN);
                    if(admin.equals("true")){
                        String tmp;
                        tmp = data.getStringExtra(StaticConfig.STR_EXTRA_IP);
                        if(tmp.equals("")){
                            ip = ip;
                        }else{
                            ip = tmp;
                        }

                        tmp = data.getStringExtra(StaticConfig.STR_EXTRA_PORT);
                        if(tmp.equals("")){
                            port = port;
                        }else{
                            port = tmp;
                        }

                        tmp = data.getStringExtra(StaticConfig.STR_EXTRA_PHONE);
                        if(tmp.equals("")){
                            phoneNumber = phoneNumber;
                        }else{
                            phoneNumber = tmp;
                        }

                        tmp = data.getStringExtra(StaticConfig.STR_EXTRA_KEY);
                        if(tmp.equals("")){
                            key = key;
                        }else{
                            key = tmp;
                        }

                        tmp = data.getStringExtra(StaticConfig.STR_EXTRA_XOR);
                        if(tmp.equals("")){
                            xorNum = xorNum;
                        }else{
                            xorNum = tmp;
                        }

                        tmp = data.getStringExtra(StaticConfig.STR_EXTRA_TIME);
                        if(tmp.equals("")){
                            serverInterval = serverInterval;
                        }else{
                            serverInterval = tmp;
                        }
                    } else {
                        String tmp;
                        tmp = data.getStringExtra(StaticConfig.STR_EXTRA_KEY);
                        if (tmp.equals("")) {
                            key = key;
                        } else {
                            key = tmp;
                        }

                        tmp = data.getStringExtra(StaticConfig.STR_EXTRA_XOR);
                        if (tmp.equals("")) {
                            xorNum = xorNum;
                        } else {
                            xorNum = tmp;
                        }

                        tmp = data.getStringExtra(StaticConfig.STR_EXTRA_TIME);
                        if (tmp.equals("")) {
                            serverInterval = serverInterval;
                        } else {
                            serverInterval = tmp;
                        }
                    }
                }
                break;

            }
        }
    }

}
