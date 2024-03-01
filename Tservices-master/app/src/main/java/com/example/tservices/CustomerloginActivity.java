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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerloginActivity extends AppCompatActivity {
    private Button custloginlogin,custloginregister;
    private TextView custloginnotaccount,custloginstatus;
    private EditText custloginemail,custloginpassword;
    private FirebaseAuth mAuth;
    ProgressDialog loadingbar;
    String onlinecustomerid;
    DatabaseReference customerregisterdatabaserf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customerlogin);
    mAuth=FirebaseAuth.getInstance();


        custloginlogin = (Button) findViewById(R.id.custloginlogin);
        custloginregister=(Button) findViewById(R.id.custloginregister);
        custloginnotaccount=(TextView) findViewById(R.id.custloginnotaccount);
        custloginstatus=(TextView) findViewById(R.id.custloginstatus);
        custloginemail=(EditText) findViewById(R.id.custloginemail);
        custloginpassword=(EditText) findViewById(R.id.custloginpassword);
        loadingbar=new ProgressDialog(this);

        custloginregister.setVisibility(View.INVISIBLE);
        custloginregister.setEnabled(false);

        custloginnotaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              custloginlogin.setVisibility(View.INVISIBLE);
                custloginlogin.setEnabled(false);
                custloginnotaccount.setVisibility(View.INVISIBLE);
                custloginnotaccount.setEnabled(false);
              custloginstatus.setText("Customer Register");

                custloginregister.setVisibility(View.VISIBLE);
                custloginregister.setEnabled(true);
            }
        });

        custloginregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String custemail = custloginemail.getText().toString();
                String custpassword=custloginpassword.getText().toString();

                RegisterCustomer(custemail,custpassword);
            }
        });

        custloginlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String custemail = custloginemail.getText().toString();
                String custpassword=custloginpassword.getText().toString();

                CustomerLogin(custemail,custpassword);
            }
        });


    }

    private void CustomerLogin(String custemail, String custpassword) {
        if(TextUtils.isEmpty(custemail)){
            Toast.makeText(CustomerloginActivity.this,"Please Write Email",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(custpassword)){
            Toast.makeText(CustomerloginActivity.this,"Please Write Password",Toast.LENGTH_SHORT).show();
        }else{
            loadingbar.setTitle("Customer Login");
            loadingbar.setMessage("Loging you");
            loadingbar.show();
            mAuth.signInWithEmailAndPassword(custemail,custpassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(CustomerloginActivity.this,"Customer login Successful",Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                        Intent customermapintent = new Intent(CustomerloginActivity.this,CustomerMapsActivity.class);
                        startActivity(customermapintent);
                    }else{
                        Toast.makeText(CustomerloginActivity.this,"wrong email or password",Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }

                }
            });
        }
    }

    private void RegisterCustomer(String custemail, String custpassword) {
        if(TextUtils.isEmpty(custemail)){
            Toast.makeText(CustomerloginActivity.this,"Please Write Email",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(custpassword)){
            Toast.makeText(CustomerloginActivity.this,"Please Write Password",Toast.LENGTH_SHORT).show();
        }else{
            loadingbar.setTitle("Customer Registration");
            loadingbar.setMessage("Registering you");
            loadingbar.show();
            mAuth.createUserWithEmailAndPassword(custemail,custpassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        onlinecustomerid = mAuth.getCurrentUser().getUid();
                        customerregisterdatabaserf = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(onlinecustomerid);

                        customerregisterdatabaserf.setValue(true);


                        Toast.makeText(CustomerloginActivity.this,"Customer Regisration Successful",Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                        Intent customermapintent = new Intent(CustomerloginActivity.this,CustomerMapsActivity.class);
                        startActivity(customermapintent);


                    }else{
                        Toast.makeText(CustomerloginActivity.this,"Error on Registering try again",Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }

                }
            });
        }
    }
}