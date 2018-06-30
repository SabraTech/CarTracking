package com.example.cartracking;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    private EditText ipEditText, portEditText, phoneEditText, keyEditText, xorEditText, timeEditText;
    private String pass;

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent data = new Intent();
        setResult(RESULT_CANCELED, data);
        finish();
    }

    public void submit(View view){
        final String ip = ipEditText.getText().toString().trim();
        final String port = portEditText.getText().toString().trim();
        final String phone = phoneEditText.getText().toString().trim();
        final String key = keyEditText.getText().toString().trim();
        final String xor = xorEditText.getText().toString().trim();
        final String time = timeEditText.getText().toString().trim();

        if(checkAdminData(ip, port, phone)){
            // view input dialog with password
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Admin Password");

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    pass = input.getText().toString();
                    if(pass.equals("1234")){
                        Intent data = new Intent();
                        data.putExtra(StaticConfig.STR_EXTRA_IP, ip);
                        data.putExtra(StaticConfig.STR_EXTRA_PORT, port);
                        data.putExtra(StaticConfig.STR_EXTRA_PHONE, phone);
                        data.putExtra(StaticConfig.STR_EXTRA_KEY, key);
                        data.putExtra(StaticConfig.STR_EXTRA_XOR, xor);
                        data.putExtra(StaticConfig.STR_EXTRA_TIME, time);
                        data.putExtra(StaticConfig.STR_EXTRA_ADMIN, "true");
                        setResult(RESULT_OK, data);
                        finish();
                    } else {
                        Toast.makeText(SettingsActivity.this, "Incorrect Password!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        } else {
            // no data for admin entered
            Intent data = new Intent();
            data.putExtra(StaticConfig.STR_EXTRA_KEY, key);
            data.putExtra(StaticConfig.STR_EXTRA_XOR, xor);
            data.putExtra(StaticConfig.STR_EXTRA_TIME, time);
            data.putExtra(StaticConfig.STR_EXTRA_ADMIN, "false");
            setResult(RESULT_OK, data);
            finish();
        }
    }

    public boolean checkAdminData(String ip, String port, String phone){
        return ip.length() != 0 || port.length() != 0 || phone.length() != 0 ;
    }
}
