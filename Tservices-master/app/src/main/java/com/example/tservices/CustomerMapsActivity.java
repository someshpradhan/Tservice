package com.example.tservices;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap,googleMap;

    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;
    Button customermaplogout,customermaprequestride,settingsbutton;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    boolean Transporterfound=false,currentcustomerlogoutstatus=false,requesttype=false,rev=false;
    String Transporterfoundid;
    String customerid;
    LatLng customerpickuplocation;
    int radius=1,r=1;
    GeoQuery geoQuery;
    LatLng delivery,transp;
    Marker transportermarker,Pickupmarker,ppmarker;
    DatabaseReference customerdatabaserf,Transporterdatabaserf;
    DatabaseReference transporterrf,transporterlocationrf;
    ValueEventListener Transporterlocationrflistner;
    TextView txtphone,txtname,vtype;
    
    CircleImageView profilepic;
    RelativeLayout relativeLayout;
    ImageView callbtn;
    String k="",lasttransporterid="";
    Button cancelride;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        settingsbutton =(Button) findViewById(R.id.customermapsettings);
        customermaplogout = (Button) findViewById(R.id.customermaplogout);
        customermaprequestride=(Button) findViewById(R.id.customermaprequestride);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        customerid= currentUser.getUid();
        customerdatabaserf = FirebaseDatabase.getInstance().getReference().child("Customer Request");
        Transporterdatabaserf=FirebaseDatabase.getInstance().getReference().child("Transporter Available");
        transporterlocationrf=FirebaseDatabase.getInstance().getReference().child("Transporters Working");


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        txtname = (TextView) findViewById(R.id.name_transporter);
        txtphone = (TextView) findViewById(R.id.phone_transporter);
        profilepic = (CircleImageView) findViewById(R.id.profile_image_customermap);
        relativeLayout = (RelativeLayout) findViewById(R.id.tel1);
        vtype = (TextView) findViewById(R.id.vehicle_transporter);
        callbtn = (ImageView) findViewById(R.id.callbtn);
        cancelride = (Button) findViewById(R.id.customermapcancel);
        searchView = (SearchView) findViewById(R.id.sv_location);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {



            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addressList=null;
                if(location!=null || !location.equals("")){
                    Geocoder geocoder = new Geocoder(CustomerMapsActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location,1);

                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    assert addressList != null;
                    Address address=addressList.get( 0 );
                    LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                    if(ppmarker!=null){
                        ppmarker.remove();
                    }
                    delivery=latLng;
                    ppmarker = mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                    cancelride.setVisibility(View.GONE);
                    DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Delivery Address").child(Transporterfoundid);
                    GeoFire geoFire = new GeoFire(db);
                    geoFire.setLocation(currentUser.getUid(),new GeoLocation(latLng.latitude,latLng.longitude));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });




        settingsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CustomerMapsActivity.this,SettingsActivity.class);
                i.putExtra("type","Customers");
                startActivity(i);

            }
        });

        customermaplogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              currentcustomerlogoutstatus=true;
                DisconnectCustomer();
                mAuth.signOut();
                LogoutCustomer();
            }
        });
        customermaprequestride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (requesttype) {

                } else {
                    requesttype = true;
                    String customeriD = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    GeoFire geoFire = new GeoFire(customerdatabaserf);
                    geoFire.setLocation(customeriD, new GeoLocation(lastlocation.getLatitude(), lastlocation.getLongitude()), (key, error) -> Log.d("location saved", "location saved"));
                    customerpickuplocation = new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude());
                    cancelride.setVisibility(View.VISIBLE);
                    Pickupmarker = mMap.addMarker(new MarkerOptions().position(customerpickuplocation).title("My location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));
                    customermaprequestride.setText("Arranging vehicle");
                    GetclosestTransporter();
                }


            }




        });

        cancelride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(requesttype){

                    cancelride.setVisibility(View.GONE);
                    cancelride.setText("Cancel Booking");
                    requesttype = false;
                    geoQuery.removeAllListeners();

                    if(Transporterlocationrflistner!=null) {
                        transporterlocationrf.removeEventListener(Transporterlocationrflistner);
                    }

                    DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Delivery Address").child(Transporterfoundid);
                    GeoFire geoFire1 = new GeoFire(db);
                    geoFire1.removeLocation(currentUser.getUid(),(key, error) -> { });
                    if (Transporterfound) {

                        transporterrf = FirebaseDatabase.getInstance().getReference().child("Users").child("Transporters").child(Transporterfoundid).child("CustomerRideID");
                        transporterrf.removeValue();
                        Transporterfoundid = null;
                    }

                    Transporterfound = false;
                    radius = 1;
                    String customeriD = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                    GeoFire geoFire = new GeoFire(customerdatabaserf);
                    geoFire.removeLocation(customeriD, (key, error) -> { });


                    if (Pickupmarker != null) {
                        Pickupmarker.remove();
                    }
                    if(ppmarker!=null)
                    {
                        ppmarker.remove();
                    }
                    searchView.setVisibility(View.GONE);
                    if (transportermarker != null){
                        transportermarker.remove();
                    }
                    transp=null;
                    delivery=null;
                    customermaprequestride.setText("Request");
                    relativeLayout.setVisibility(View.GONE);
                    searchView.setVisibility(View.GONE);
                }
            }
        });


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



    private void GetclosestTransporter() {
        GeoFire geoFire = new GeoFire(Transporterdatabaserf);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(lastlocation.getLatitude(),lastlocation.getLongitude()),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                    if(!Transporterfound && requesttype) {
                        Transporterfound = true;
                        Transporterfoundid = key;

                        if(!Transporterfoundid.equals(lasttransporterid)){
                        transporterrf = FirebaseDatabase.getInstance().getReference().child("Users").child("Transporters").child(Transporterfoundid);

                        HashMap<String, Object> Transportermap = new HashMap<String, Object>();
                        Transportermap.put("CustomerRideID", customerid);
                        transporterrf.updateChildren(Transportermap);

                        customermaprequestride.setText("Finding");
                        Gettingtransporterlocation();
                        }

                    }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!Transporterfound){
                    radius=radius+1;
                    GetclosestTransporter();

                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void Gettingtransporterlocation() {
     Transporterlocationrflistner = transporterlocationrf.child(Transporterfoundid).child("l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && requesttype   ){
                    List<Object> transporterlocationmap = (List<Object>) snapshot.getValue();

                    double locationlat= 0;
                    double locationlon= 0;
                    customermaprequestride.setText("Found");

                    relativeLayout.setVisibility(View.VISIBLE);
                    GetAssignedTransporterInformation();
                    assert transporterlocationmap != null;
                    if(transporterlocationmap.get(0)!=null){
                            locationlat = Double.parseDouble(transporterlocationmap.get(0).toString());

                    }
                    if(transporterlocationmap.get(1)!=null){

                        locationlon = Double.parseDouble(transporterlocationmap.get(1).toString());

                    }

                LatLng  transporterlatlng = new LatLng(locationlat,locationlon);
                    transp = transporterlatlng;

                    if(transportermarker!=null){
                        transportermarker.remove();

                    }

                    Location location1 = new Location("");
                    location1.setLatitude(customerpickuplocation.latitude);
                    location1.setLongitude(customerpickuplocation.longitude);

                    Location location2 = new Location("");
                    location2.setLatitude(transporterlatlng.latitude);
                    location2.setLongitude(transporterlatlng.longitude);

                    float Distance = location1.distanceTo(location2);
                    if(Distance<90)
                    {
                        customermaprequestride.setText("Transporter reached");
                        searchView.setVisibility(View.VISIBLE);
                    }else {
                       // customermaprequestride.setText(String.valueOf(Distance));
                        customermaprequestride.setText(String.valueOf(Distance) + "m away");
                    }

                    transportermarker= mMap.addMarker(new MarkerOptions().position(transporterlatlng).title("Transporter Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.truckformap1)));



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
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
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
            customermaprequestride.setText("suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        customermaprequestride.setText("failed");
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lastlocation=location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));



        if(delivery!=null && transp!=null) {
            Location location1 = new Location("");
            location1.setLatitude(delivery.latitude);
            location1.setLongitude(delivery.longitude);

            Location location2 = new Location("");
            location2.setLatitude(transp.latitude);
            location2.setLongitude(transp.longitude);

            float Distance = location1.distanceTo(location2);
            if (Distance < 90) {
                customermaprequestride.setText("Confirm Delivery");
                cancelride.setVisibility(View.VISIBLE);
                cancelride.setText("Confirm Delivery");
            }
        }
//        customermaprequestride.setText(String.valueOf(r++));
        if(transporterrf!=null) {
            transporterrf.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists() && snapshot.getChildrenCount()>0) {
                        if(snapshot.hasChild("CustomerRideID"))
                            k=snapshot.child("CustomerRideID").getValue().toString();
                        else
                            k="";
                        if (!k.equals(customerid)) {
                            if (requesttype) {
                                cancelride.setVisibility(View.GONE);
                                requesttype = false;
                    //            geoQuery.removeAllListeners();

                                if (Transporterlocationrflistner != null) {
                                    transporterlocationrf.removeEventListener(Transporterlocationrflistner);
                                }

                                if (Transporterfound) {
                                    lasttransporterid=Transporterfoundid;
                                    transporterrf = FirebaseDatabase.getInstance().getReference().child("Users").child("Transporters").child(Transporterfoundid).child("CustomerRideID");
                                    transporterrf.removeValue();
                                    Transporterfoundid = null;
                                }

                                Transporterfound = false;
                                radius = 1;
                                String customeriD = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                                GeoFire geoFire = new GeoFire(customerdatabaserf);
                                geoFire.removeLocation(customeriD, (key, error) -> {
                                });
                                if (Pickupmarker != null) {
                                    Pickupmarker.remove();
                                }

                                if (transportermarker != null) {
                                    transportermarker.remove();
                                }

                                if(ppmarker!=null){
                                    ppmarker.remove();
                                }
                                searchView.setVisibility(View.GONE);
                                customermaprequestride.setText("Request");
                                relativeLayout.setVisibility(View.GONE);
                            }
                        }





                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });




        }
    }
    protected synchronized void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        googleApiClient.connect();

    }


    @Override
    protected void onStop() {
        super.onStop();

        if(!currentcustomerlogoutstatus){
            DisconnectCustomer();
        }
    }


    private void DisconnectCustomer() {
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customerdatabaserf = FirebaseDatabase.getInstance().getReference().child("Customer Request");
        GeoFire geoFire = new GeoFire(customerdatabaserf);
        geoFire.removeLocation(userid, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                Log.d("location removed","location removed");
            }
        });

}
        // LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);

    private void LogoutCustomer() {
        Intent welcomeIntent = new Intent(CustomerMapsActivity.this,WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeIntent);
        finish();
    }
    private void GetAssignedTransporterInformation(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Transporters").child(Transporterfoundid);




        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists() && snapshot.getChildrenCount()>0){
                        String namv = snapshot.child("name").getValue().toString();
                        String phonv = snapshot.child("phone").getValue().toString();
                        txtname.setText(namv);
                        txtphone.setText(phonv);

                            String vehiclev = snapshot.child("Vehicle").getValue().toString();
                            vtype.setText(vehiclev);

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


}