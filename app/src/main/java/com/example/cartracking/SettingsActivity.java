package com.example.cartracking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {
    private EditText ipEditText, portEditText, phoneEditText, keyEditText, xorEditText, timeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ipEditText = findViewById(R.id.edit_text_ip);
        portEditText = findViewById(R.id.edit_text_port);
        phoneEditText = findViewById(R.id.edit_text_phone_number);
        keyEditText = findViewById(R.id.edit_text_key);
        xorEditText = findViewById(R.id.edit_text_xor_num);
        timeEditText = findViewById(R.id.edit_text_time_interval);
    }

    public void submit(View view){
        String ip = ipEditText.getText().toString();
        String port = portEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String key = keyEditText.getText().toString();
        String xor = xorEditText.getText().toString();
        String time = timeEditText.getText().toString();

        if(checkAdminData(ip, port, phone)){
            // view input dialog with password
            // if true create the intent
            Intent data = new Intent();
            data.putExtra(StaticConfig.STR_EXTRA_IP, ip);
            data.putExtra(StaticConfig.STR_EXTRA_PORT, Integer.parseInt(port));
            data.putExtra(StaticConfig.STR_EXTRA_PHONE, phone);
            data.putExtra(StaticConfig.STR_EXTRA_KEY, key);
            data.putExtra(StaticConfig.STR_EXTRA_XOR, xor);
            data.putExtra(StaticConfig.STR_EXTRA_TIME, Integer.parseInt(time));
            data.putExtra(StaticConfig.STR_EXTRA_ADMIN, true);
            setResult(RESULT_OK, data);
            finish();
            // if false error message
        } else {
            // no data for admin entered
            Intent data = new Intent();
            data.putExtra(StaticConfig.STR_EXTRA_KEY, key);
            data.putExtra(StaticConfig.STR_EXTRA_XOR, xor);
            data.putExtra(StaticConfig.STR_EXTRA_TIME, Integer.parseInt(time));
            data.putExtra(StaticConfig.STR_EXTRA_ADMIN, false);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    public boolean checkAdminData(String ip, String port, String phone){
        return ip.length() != 0 && port.length() != 0 && phone.length() != 0 ;
    }
}
