package com.example.tservices;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class Transporterloginactivity extends AppCompatActivity {
    private Button transloginlogin,transloginregister;
    private TextView transloginnotaccount,Transloginstatus;
    private EditText transloginemail,transloginpassword;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;
    String onlinetransporterid;
    DatabaseReference transporterregisterdatabaserf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transporterloginactivity);
        mAuth=FirebaseAuth.getInstance();



        transloginlogin = (Button) findViewById(R.id.transloginlogin);
        transloginregister=(Button) findViewById(R.id.transloginregister);
        transloginnotaccount=(TextView) findViewById(R.id.transloginnotaccount);
        Transloginstatus=(TextView) findViewById(R.id.transloginstatus);
        transloginemail=(EditText) findViewById(R.id.transloginemail);
        transloginpassword=(EditText) findViewById(R.id.transloginpassword);
        loadingbar = new ProgressDialog(this);

        transloginregister.setVisibility(View.INVISIBLE);
        transloginregister.setEnabled(false);

        transloginnotaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transloginlogin.setVisibility(View.INVISIBLE);
                transloginlogin.setEnabled(false);
                transloginnotaccount.setVisibility(View.INVISIBLE);
                transloginnotaccount.setEnabled(false);
                Transloginstatus.setText("Transporter Register");

                transloginregister.setVisibility(View.VISIBLE);
                transloginregister.setEnabled(true);
            }
        });

        transloginregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String transemail = transloginemail.getText().toString();
                String transpassword=transloginpassword.getText().toString();

                RegisterTransporter(transemail,transpassword);
            }
        });

        transloginlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String transemail=transloginemail.getText().toString();
                String transpassword=transloginpassword.getText().toString();
             TransporterLogin(transemail,transpassword);
            }
        });



    }

    private void TransporterLogin(String transemail, String transpassword) {
        if(TextUtils.isEmpty(transemail)){
            Toast.makeText(Transporterloginactivity.this,"Please Write Email",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(transpassword)){
            Toast.makeText(Transporterloginactivity.this,"Please Write Password",Toast.LENGTH_SHORT).show();
        }else{
            loadingbar.setTitle("Transporter login");
            loadingbar.setMessage("loging you");
            loadingbar.show();

            mAuth.signInWithEmailAndPassword(transemail,transpassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(Transporterloginactivity.this,"Login Successful",Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();

                        Intent Transmapintent = new Intent(Transporterloginactivity.this,TransporterMapsActivity.class);
                        startActivity(Transmapintent);
                    }else{
                        Toast.makeText(Transporterloginactivity.this,"Wrong Email or Password",Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                }
            });

        }
    }


    private void RegisterTransporter(String transemail, String transpassword) {
        if(TextUtils.isEmpty(transemail)){
             Toast.makeText(Transporterloginactivity.this,"Please Write Email",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(transpassword)){
            Toast.makeText(Transporterloginactivity.this,"Please Write Password",Toast.LENGTH_SHORT).show();
        }else{
            loadingbar.setTitle("Transporter Registration");
            loadingbar.setMessage("Registering you");
            loadingbar.show();
            mAuth.createUserWithEmailAndPassword(transemail,transpassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        onlinetransporterid=mAuth.getCurrentUser().getUid();
                        transporterregisterdatabaserf = FirebaseDatabase.getInstance().getReference().child("Users").child("Transporters").child(onlinetransporterid);
                        transporterregisterdatabaserf.setValue(true);


                        Toast.makeText(Transporterloginactivity.this,"Transporter Regisration Successful",Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();

                        Intent Transmapintent = new Intent(Transporterloginactivity.this,TransporterMapsActivity.class);
                        startActivity(Transmapintent);

                    }else{
                        Toast.makeText(Transporterloginactivity.this,"Error on Registering try again",Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }

                }
            });
        }
    }
}