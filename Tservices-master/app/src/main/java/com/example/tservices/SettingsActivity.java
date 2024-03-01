package com.example.tservices;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private String getType;
    TextView changeprofilesettings;
    EditText name,phone,vehicletype;
    ImageView closebtn,savebtn,profileimg;

    String checker="";
    Uri imageuri;
    String myurl="";
DatabaseReference databaseReference;
FirebaseAuth mAuth;
    private StorageTask uploadTask;
    private StorageReference storageprofilepicrf;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);




    getType= getIntent().getStringExtra("type");
    Toast.makeText(this,getType,Toast.LENGTH_SHORT).show();
mAuth = FirebaseAuth.getInstance();
databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(getType);
storageprofilepicrf = FirebaseStorage.getInstance().getReference().child("Profile Pictures");

    changeprofilesettings = (TextView) findViewById(R.id.change_profile_btn);
    name = (EditText) findViewById(R.id.settingsname);
    phone = (EditText) findViewById(R.id.phonenumber);
    vehicletype = (EditText) findViewById(R.id.vehicletype);
    if(getType.equals("Transporters"))
    {
        vehicletype.setVisibility(View.VISIBLE);
    }
    closebtn = (ImageView) findViewById(R.id.closesettings);
    savebtn = (ImageView) findViewById(R.id.savesettings);
    profileimg = (ImageView) findViewById(R.id.profile_image);


closebtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if(getType.equals("Transporters")){
            Intent i = new Intent(SettingsActivity.this,TransporterMapsActivity.class);
            startActivity(i);

        }else{

            Intent i = new Intent(SettingsActivity.this,CustomerMapsActivity.class);
            startActivity(i);
        }



    }
});

savebtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if(checker.equals("clicked")){
            validatecontrollers();
        }else{
            validateandsaveonlyinformation();
        }
    }
});

changeprofilesettings.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        checker="clicked";
        CropImage.activity().setAspectRatio(1,1).start(SettingsActivity.this);
    }
});

getuserInformation();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK && data!=null){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
         imageuri = result.getUri();
            profileimg.setImageURI(imageuri);
        }else{
            if(getType.equals("Transporters")){
                startActivity(new Intent(SettingsActivity.this, TransporterMapsActivity.class));
            }else {
                startActivity(new Intent(SettingsActivity.this, CustomerMapsActivity.class));
            }
            Toast.makeText(this,"Error,Try again",Toast.LENGTH_SHORT);

        }
    }
    private void validatecontrollers(){

        if(TextUtils.isEmpty(name.getText().toString())){
            Toast.makeText(this,"Please enter your name",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(phone.getText().toString())){
            Toast.makeText(this,"Please enter your phone no",Toast.LENGTH_SHORT).show();

        }else if(getType.equals("Transporters") && TextUtils.isEmpty(vehicletype.getText().toString())){
            Toast.makeText(this,"mention your vehicle",Toast.LENGTH_SHORT).show();
        }else if(checker.equals("clicked")){
            uploadProfilePicture();

        }
    }

    private void uploadProfilePicture() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
       progressDialog.setTitle("Setting Account Information");
       progressDialog.setTitle("Please wait while we are setting your account information");
       progressDialog.show();
    if(imageuri!=null){
          final StorageReference fileref = storageprofilepicrf.child(mAuth.getCurrentUser().getUid()+".jpg");
        uploadTask = fileref.putFile(imageuri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return fileref.getDownloadUrl();
            }
        })
     .addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadurl = task.getResult();
                        myurl = downloadurl.toString();

                        HashMap<String,Object> Usermap = new HashMap<>();
                        Usermap.put("uid",mAuth.getCurrentUser().getUid());
                        Usermap.put("name",name.getText().toString());
                        Usermap.put("phone",phone.getText().toString());
                        Usermap.put("image",myurl);

                    if(getType.equals("Transporters")){
                        Usermap.put("Vehicle",vehicletype.getText().toString());
                    }
                    databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(Usermap);
                    progressDialog.dismiss();

                        if(getType.equals("Transporters")){
                            startActivity(new Intent(SettingsActivity.this, TransporterMapsActivity.class));
                        }else {
                            startActivity(new Intent(SettingsActivity.this, CustomerMapsActivity.class));
                        }

                    }
            }
        });
    }else{
Toast.makeText(this,"Image is not selected",Toast.LENGTH_SHORT);
    }

    }
    private void validateandsaveonlyinformation() {
        if(TextUtils.isEmpty(name.getText().toString())){
            Toast.makeText(this,"Please enter your name",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(phone.getText().toString())){
            Toast.makeText(this,"Please enter your phone no",Toast.LENGTH_SHORT).show();

        }else if(getType.equals("Transporters") && TextUtils.isEmpty(vehicletype.getText().toString())){
            Toast.makeText(this,"mention your vehicle",Toast.LENGTH_SHORT).show();
        }else{

        HashMap<String,Object> Usermap = new HashMap<>();
        Usermap.put("uid",mAuth.getCurrentUser().getUid());
        Usermap.put("name",name.getText().toString());
        Usermap.put("phone",phone.getText().toString());

        if(getType.equals("Transporters")){
            Usermap.put("Vehicle",vehicletype.getText().toString());
        }
        databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(Usermap);


        if(getType.equals("Transporters")){
            startActivity(new Intent(SettingsActivity.this, TransporterMapsActivity.class));
        }else {
            startActivity(new Intent(SettingsActivity.this, CustomerMapsActivity.class));
        }
    }
    }

    private void getuserInformation(){

        databaseReference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists() && snapshot.getChildrenCount()>0){
                    String namv = snapshot.child("name").getValue().toString();
                    String phonv = snapshot.child("phone").getValue().toString();
                    name.setText(namv);
                    phone.setText(phonv);
                    if(getType.equals("Transporters")) {
                        String vehiclev = snapshot.child("Vehicle").getValue().toString();
                        vehicletype.setText(vehiclev);
                    }
                    if(snapshot.hasChild("image")){
                        String imagev = snapshot.child("image").getValue().toString();
                        Picasso.get().load(imagev).into(profileimg);
                    }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}