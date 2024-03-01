package com.example.tservices;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity {

    private Button welcometransporter;
    private Button welcomecustomer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        welcometransporter =(Button) findViewById(R.id.welcometransporter);
        welcometransporter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent transloginintent = new Intent(WelcomeActivity.this,Transporterloginactivity.class);
                startActivity(transloginintent);
            }
        });

         welcomecustomer =(Button) findViewById(R.id.welcomecustomer);
        welcomecustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent custloginintent = new Intent(WelcomeActivity.this,CustomerloginActivity.class);
                startActivity(custloginintent);
            }
        });

    }
}