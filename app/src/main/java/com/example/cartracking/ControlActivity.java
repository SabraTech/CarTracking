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
    private static final String KIT_NAME = "Dr. Farid Sabra";
    private static final String KIT_MAC = "50:01:D9:93:7E:C6";

    private String phoneNumber = "01068435908";
    private String xorNum = "1111";
    private String key = "5060";
    private String ip = "0.0.0.0";
    private int port = 999;
    private int serverInterval = 5000;

    private Button authButton;
    private DataOutputStream outputStream;
    private InputStream inputStream;
    private BluetoothSocket socket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        authButton = findViewById(R.id.authButton);
        authButton.setEnabled(false);
        socket = null;
        // pair here first and notify the user with the result
        // if connected enable the auth button

        if (connectToKit()) {
            authButton.setEnabled(true);
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
                        if (deviceName.equals(KIT_NAME) && deviceHardwareAddress.equals(KIT_MAC)) {
                            ParcelUuid[] uuids = device.getUuids();
                            bluetoothAdapter.cancelDiscovery();
                            try {
//                                Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
//                                socket = (BluetoothSocket) m.invoke(device, 1);                                // socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                                socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());

                                socket.connect();
                                outputStream = new DataOutputStream(socket.getOutputStream());
                                inputStream = socket.getInputStream();
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

    private boolean write(String s) {
        try {
            outputStream.write(s.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
////        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
//
//            } else {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
//            }
//        }
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

                    // or
//                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
//
//                    smsIntent.setData(Uri.parse("smsto:"));
//                    smsIntent.setType("vnd.android-dir/mms-sms");
//                    smsIntent.putExtra("address"  , PHONE_NUMBER);
//                    smsIntent.putExtra("sms_body"  , MESSAGE);
//
//                    try {
//                        startActivity(smsIntent);
//                        finish();
//                        Log.i("Finished sending SMS...", "");
//                    } catch (android.content.ActivityNotFoundException ex) {
//                        Toast.makeText(MainActivity.this,
//                                "SMS faild, please try again later.", Toast.LENGTH_SHORT).show();
//                    }

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

        // send the value via bluetooth
        if (write(value)) {
            Toast.makeText(ControlActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ControlActivity.this, "Error in sending message", Toast.LENGTH_SHORT).show();
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
            }
            case StaticConfig.REQUEST_CODE_SETTINGS: {
                // get the data from the intent and set to the new values;
                boolean admin = Boolean.parseBoolean(data.getStringExtra(StaticConfig.STR_EXTRA_ADMIN));
                if(admin){
                    ip = data.getStringExtra(StaticConfig.STR_EXTRA_IP);
                    port = Integer.parseInt(data.getStringExtra(StaticConfig.STR_EXTRA_PORT));
                    phoneNumber = data.getStringExtra(StaticConfig.STR_EXTRA_PHONE);
                    key = data.getStringExtra(StaticConfig.STR_EXTRA_KEY);
                    xorNum = data.getStringExtra(StaticConfig.STR_EXTRA_XOR);
                    serverInterval = Integer.parseInt(data.getStringExtra(StaticConfig.STR_EXTRA_TIME));
                } else {
                    key = data.getStringExtra(StaticConfig.STR_EXTRA_KEY);
                    xorNum = data.getStringExtra(StaticConfig.STR_EXTRA_XOR);
                    serverInterval = Integer.parseInt(data.getStringExtra(StaticConfig.STR_EXTRA_TIME));
                }
            }
        }
    }

}
