 package com.example.tservices;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

 public class TransporterMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,TaskLoadedCallback {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;
    Button Transportermapsignout, Transportermapssettings;
    FirebaseAuth mAuth;
    Marker Pickupmarker,deliverymarker;
    int r = 1;
    LatLng delivery;
    Bundle bundlef;
    FirebaseUser currentUser;
    boolean currenttransporterlogoutstatus = false,rev=false;
    DatabaseReference Assignedcustomerrf, Assignedcustomerpickuprf;
    private String transporterid, customerid = "";
    ValueEventListener Assignedcustomerpickuprflistner;

     TextView txtphone,txtname;
     CircleImageView profilepic;
     RelativeLayout relativeLayout;
     ImageView callbtn;
     private Polyline currentPolyline;
     private MarkerOptions place1, place2;

     Button cancelrequest,acceptrequest;
     boolean cancel=false;
     String lastcustomerid="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transporter_maps);

        Transportermapssettings = (Button) findViewById(R.id.Transportermapssettings);
        Transportermapsignout = (Button) findViewById(R.id.Transportermapsignout);
        mAuth = FirebaseAuth.getInstance();
        transporterid = mAuth.getCurrentUser().getUid();
        currentUser = mAuth.getCurrentUser();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        txtname = (TextView) findViewById(R.id.name_customer);
        txtphone = (TextView) findViewById(R.id.phone_customer);
        profilepic = (CircleImageView) findViewById(R.id.profile_image_transportermap);
        relativeLayout = (RelativeLayout) findViewById(R.id.tel2);
        acceptrequest = (Button) findViewById(R.id.Transportermapsaccept);
        callbtn = (ImageView) findViewById(R.id.callbtn2);
        cancelrequest = (Button) findViewById(R.id.Transportermapscancel);




        Transportermapssettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TransporterMapsActivity.this,SettingsActivity.class);
                i.putExtra("type","Transporters");
                startActivity(i);

            }
        });

        Transportermapsignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currenttransporterlogoutstatus = true;
                DisconnecttheTransporter();
                mAuth.signOut();
                LogoutTransporter();
            }
        });

        GetAssignedcustomerrequest();

        callbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone_number
                        = txtphone.getText().toString();

                // Getting instance of Intent
                // with action as ACTION_CALL
                Intent phone_intent
                        = new Intent(Intent.ACTION_CALL);

                // Set data of Intent through Uri
                // by parsing phone number
                phone_intent
                        .setData(Uri.parse("tel:"
                                + phone_number));

                // start Intent
                startActivity(phone_intent);
            }
        });




    }

    private void GetAssignedcustomerrequest() {
        Assignedcustomerrf = FirebaseDatabase.getInstance().getReference().child("Users").child("Transporters").child(transporterid).child("CustomerRideID");


            Assignedcustomerrf.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        customerid = snapshot.getValue().toString();

                        if(!customerid.equals(lastcustomerid)) {
                            cancelrequest.setVisibility(View.VISIBLE);
                            acceptrequest.setVisibility(View.VISIBLE);

                            cancelrequest.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    cancel=true;
                                    Assignedcustomerrf.removeValue();
                                    cancelrequest.setVisibility(View.GONE);
                                    acceptrequest.setVisibility(View.GONE);
                                    lastcustomerid = customerid;
                                    delivery=null;
                                }
                            });
                            acceptrequest.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    cancel =false;
                                    lastcustomerid="";
                                    acceptrequest.setVisibility(View.GONE);
                                    cancelrequest.setVisibility(View.GONE);
                                    Log.d("getassignedcustomere", "getfunc");
                                    GetAssignedcustomerpickuplocation();
                                    relativeLayout.setVisibility(View.VISIBLE);
                                    GetAssignedCustomerInformation();

                                }
                            });
                        }


                    } else {

                        customerid = "";
                        if (Pickupmarker != null) {
                            Pickupmarker.remove();
                        }
                        /*if(Pickupmarker2!=null)
                            Pickupmarker2.remove();*/
                        if(deliverymarker != null)
                        {
                            delivery=null;
                            deliverymarker.remove();
                        }

                        if (Assignedcustomerpickuprflistner != null) {
                            Assignedcustomerpickuprf.removeEventListener(Assignedcustomerpickuprflistner);
                        }
                        relativeLayout.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

    }

    private void GetAssignedcustomerpickuplocation() {
        Assignedcustomerpickuprf = FirebaseDatabase.getInstance().getReference().child("Customer Request").child(customerid).child("l");
       Assignedcustomerpickuprflistner = Assignedcustomerpickuprf.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Object> customerlocationmap = (List<Object>) snapshot.getValue();
                    double locationlat = 0;
                    double locationlon = 0;


                    assert customerlocationmap != null;
                    if (customerlocationmap.get(0) != null) {
                        locationlat = Double.parseDouble(customerlocationmap.get(0).toString());

                    }
                    if (customerlocationmap.get(1) != null) {

                        locationlon = Double.parseDouble(customerlocationmap.get(1).toString());

                    }
                    LatLng transporterlatlng = new LatLng(locationlat, locationlon);
                    Pickupmarker=mMap.addMarker(new MarkerOptions().position(transporterlatlng).title("Customer Pickup Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));



                    place1 = new MarkerOptions().position(transporterlatlng);
                    place2 = new MarkerOptions().position(new LatLng(lastlocation.getLatitude(),lastlocation.getLongitude())).title("My Location");

                   // Pickupmarker2 = mMap.addMarker(place2);

                    new FetchURL(TransporterMapsActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "driving"), "driving");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        // Add a marker in Sydney and move the camera

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
            bundlef=bundle;
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {





        if (getApplicationContext() != null) {
            if(!customerid.equals("")){
                DatabaseReference dt = FirebaseDatabase.getInstance().getReference().child("Delivery Address").child(transporterid).child(customerid).child("l");
                if(dt!=null) {
                    dt.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.getChildrenCount() > 0) {


                                List<Object> deliverylocationmap = (List<Object>) snapshot.getValue();
                                double locationla = 0;
                                double locationlo = 0;
                                assert deliverylocationmap != null;
                                if (deliverylocationmap.get(0) != null) {
                                    locationla = Double.parseDouble(deliverylocationmap.get(0).toString());

                                }
                                if (deliverylocationmap.get(1) != null) {

                                    locationlo = Double.parseDouble(deliverylocationmap.get(1).toString());

                                }
                                LatLng deliverylatlng = new LatLng(locationla, locationlo);
                                delivery=deliverylatlng;
                                if(deliverymarker!=null)
                                {
                                    deliverymarker.remove();
                                }

                                deliverymarker = mMap.addMarker(new MarkerOptions().position(deliverylatlng).title("delivery location"));

                            }

                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } }else if(deliverymarker!=null){
                deliverymarker.remove();
            }












            lastlocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

            if(!currenttransporterlogoutstatus) {
//            Transportermapssettings.setText(String.valueOf(r++));
                String useri = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                DatabaseReference Transporteravailabilityrf = FirebaseDatabase.getInstance().getReference().child("Transporter Available");
                GeoFire geoFireAvailable = new GeoFire(Transporteravailabilityrf);
                DatabaseReference Tansporterworkingrf = FirebaseDatabase.getInstance().getReference().child("Transporters Working");
                GeoFire geoFireworking = new GeoFire(Tansporterworkingrf);


                if (customerid.equals("")) {
                    geoFireworking.removeLocation(useri, (key, error) -> {
                    });
                    delivery=null;
                    geoFireAvailable.setLocation(useri, new GeoLocation(location.getLatitude(), location.getLongitude()), (key, error) -> Log.d("location saved", "location saved"));

                } else {

                    geoFireAvailable.removeLocation(useri, (key, error) -> {
                    });

                    geoFireworking.setLocation(useri, new GeoLocation(location.getLatitude(), location.getLongitude()), (key, error) -> Log.d("location saved", "location saved"));


                }
            }

            if(delivery!=null) {
                Location location1 = new Location("");
                location1.setLatitude(latLng.latitude);
                location1.setLongitude(latLng.longitude);

                Location location2 = new Location("");
                location2.setLatitude(delivery.latitude);
                location2.setLongitude(delivery.longitude);

                float Distance = location1.distanceTo(location2);
                if (Distance < 90) {
                    Toast.makeText(TransporterMapsActivity.this, "Destination Reached,Confirming delivery from customer", Toast.LENGTH_SHORT).show();

                }

            }

        }
    }
    protected synchronized void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        googleApiClient.connect();

    }

    

    @Override
    protected void onStop() {
        super.onStop();
       if(!currenttransporterlogoutstatus){
           DisconnecttheTransporter();}
    }



     private void DisconnecttheTransporter() {
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference Transporteravailabilityrf = FirebaseDatabase.getInstance().getReference().child("Transporter Available");
        GeoFire geoFire = new GeoFire(Transporteravailabilityrf);
        geoFire.removeLocation(userid, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                Log.d("location removed","location removed");
            }
        });



        //LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
    }
    private void LogoutTransporter() {
        Intent welcomeIntent = new Intent(TransporterMapsActivity.this,WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeIntent);
        finish();
    }
     private void GetAssignedCustomerInformation(){
         DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerid);




         reference.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 if(snapshot.exists() && snapshot.getChildrenCount()>0){
                     String namv = snapshot.child("name").getValue().toString();
                     String phonv = snapshot.child("phone").getValue().toString();
                     txtname.setText(namv);
                     txtphone.setText(phonv);



                     if(snapshot.hasChild("image")){
                         String imagev = snapshot.child("image").getValue().toString();
                         Picasso.get().load(imagev).into(profilepic);
                     }



                 }

             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });


     }
     private String getUrl(LatLng origin, LatLng dest, String directionMode) {
         // Origin of route
         String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
         // Destination of route
         String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
         // Mode
         String mode = "mode=" + directionMode;
         // Building the parameters to the web service
         String parameters = str_origin + "&" + str_dest + "&" + mode;
         // Output format
         String output = "json";
         // Building the url to the web service
         String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
         return url;
     }

     @Override
     public void onTaskDone(Object... values) {
         if (currentPolyline != null)
             currentPolyline.remove();
         currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
     }

 }