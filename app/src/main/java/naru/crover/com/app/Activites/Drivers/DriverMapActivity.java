package naru.crover.com.app.Activites.Drivers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import naru.crover.com.app.Activites.DriveCompletedActivity;
import naru.crover.com.app.Activites.EmailLogInActivity;
import naru.crover.com.app.Activites.HistoryActivity;
import naru.crover.com.app.Activites.Riders.RiderMapActivity;
import naru.crover.com.app.Activites.Settings;
import naru.crover.com.app.R;

public class DriverMapActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, RoutingListener {

    private GoogleMap mMap;
    private GoogleApiClient client;
    private Location myLocation;
    private LocationRequest myLocationRequest;
    private boolean isDisconnected = false;
    private boolean cameraPositions = false;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar toolbar;
    private PlaceAutocompleteFragment placeAutoComplete;
    private String riderID = "";
    private NavigationView navigationView;
    private Marker pickupLocationMarker, driverLocation;
    private DatabaseReference riderPickUpLocationRef, riderRequestRef, databaseReference, reference, rideStatusRef;
    private ValueEventListener riderPickUpLocationRefListener, riderStatusValueEventListener, valueEventListener, driverServiceValueEventListener;
    private TextView userName, userPhones, riderName, riderPhone, riderDestination;
    private ImageView iv, riderIv, phoneBtn;
    private FirebaseAuth mAuth;
    private String userID;
    private String dName;
    private String dPhone;
    private String myProfileUrl;
    private LinearLayout customLinearLayout;
    private SupportMapFragment mapFragment;
    final int LOCATION_REQUEST_CODE = 1;
    final int CALL_REQUEST = 2;
    final int PHONE_STATE = 3;
    private boolean available = false;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorWhite, R.color.colorButton, R.color.colorYellow, R.color.colorDimBlack};
    private TextView btnAccept;
    private Button btnCall, btnRideComplete;
    private boolean isDriverOnService = false;
    private double cost = 0;


    // create Driver Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_driver_map);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child("Profile").child(userID);

        btnAccept = findViewById(R.id.btnAccept);
        btnCall = findViewById(R.id.btnCall);
        btnRideComplete = findViewById(R.id.btnRideCompleted);

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                driverWorking();
                btnAccept.setBackgroundColor(Color.parseColor("#262622"));
                btnAccept.setEnabled(false);

            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                if (!isDriverOnService) {

                    Toast.makeText(DriverMapActivity.this, "Calling to rider", Toast.LENGTH_SHORT).show();
                    makePhoneCall();

                    reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child("Activity").child(riderID).child("riderStatus").child("rideStarted");
                    riderStatusValueEventListener = reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {
                                String data = dataSnapshot.getValue().toString();
                                if (data.equals("0")) {
                                    Toast.makeText(DriverMapActivity.this, "Due to inactivity of the rider this ride has been canceled !!", Toast.LENGTH_SHORT).show();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            cancelRider();
                                        }
                                    }, 200);


                                } else if (data.equals("1")) {
                                    isDriverOnService = true;
                                    btnCall.setText("Pick Your Client");
                                }
                            } else {

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {


                        }
                    });

                } else {
                    isDriverOnService = false;
                    btnCall.setVisibility(View.GONE);
                    btnRideComplete.setVisibility(View.VISIBLE);
                }

            }
        });

        btnRideComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeRider();
                startActivity(new Intent(DriverMapActivity.this, DriveCompletedActivity.class).putExtra("cost", ""+cost));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


        // Display Rider Information

        customLinearLayout = findViewById(R.id.riderProfile);
        riderName = findViewById(R.id.userNameTv);
        riderPhone = findViewById(R.id.userPhoneNumberTv);
        riderIv = findViewById(R.id.riderImageIv);
//        phoneBtn = findViewById(R.id.phoneBtn);
        riderDestination = findViewById(R.id.riderDestinationTv);

        // Activity Toolbar And Navigation

        toolbar = findViewById(R.id.toolbarDriver);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawerLayoutId);
        mToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        navigationView = findViewById(R.id.navView);

        View headerView = navigationView.getHeaderView(0);
        userName = headerView.findViewById(R.id.driverName);
        userPhones = headerView.findViewById(R.id.driverPhone);
        iv = headerView.findViewById(R.id.driverImage);



        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        //Display Driver Info From Database
        getUserInfo();

        //Initialize Driver Map
        initMap();
        //Get Rider Request
        getAssignedRider();


    }

    private void driverWorking() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child("Activity").child(riderID).child("foundDriver");
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        HashMap m = new HashMap();
        m.put("driverID", userID);
        ref.updateChildren(m);
        btnCall.setEnabled(true);
        btnCall.setBackgroundColor(Color.parseColor("#480721"));

    }

    public void initMap() {

        polylines = new ArrayList<>();

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            mapFragment.getMapAsync(this);
        }

    }

    private void getAssignedRider() {

        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        riderRequestRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child("Activity").child(driverId).child("riderRequest").child("riderId");
        valueEventListener = riderRequestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    riderID = dataSnapshot.getValue().toString();
                    // Getting Rider Location
                    getAssignRiderPickupLocation();
                    getAssignRiderDestination();
                    getRiderInformation();
                } else {
                     rideStatusRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child("Activity").child(riderID).child("riderStatus").child("rideStarted");
                     riderRequestRef.removeEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String data = dataSnapshot.getValue().toString();
                                if (data.equals("1")){
                                }else {
                                    Toast.makeText(DriverMapActivity.this, "This ride has been canceled due to inactivity..!", Toast.LENGTH_SHORT).show();
                                    cancelRider();
                                }
                            } else {
                                clearDriverProfile();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Rider Location
    private void getAssignRiderPickupLocation() {
        riderPickUpLocationRef = FirebaseDatabase.getInstance().getReference().child("riderRequest").child(riderID).child("l");
        riderPickUpLocationRefListener = riderPickUpLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !riderID.equals("")) {

                    List<Object> riderList = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;

                    if (riderList.get(0) != null) {
                        locationLat = Double.parseDouble(riderList.get(0).toString());
                    }
                    if (riderList.get(1) != null) {
                        locationLng = Double.parseDouble(riderList.get(1).toString());
                    }

                    LatLng riderLatLng = new LatLng(locationLat, locationLng);
                    pickupLocationMarker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.user_location_icon))
                            .position(riderLatLng).title("Pick Me"));
                    getRouteToMarker(riderLatLng);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getRouteToMarker(LatLng riderLatLng) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), riderLatLng)
                .build();
        routing.execute();
    }

    private void getAssignRiderDestination() {

        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child("Activity").child(driverId).child("riderRequest");
        ref.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("destination") != null) {
                        riderDestination.setText(map.get("destination").toString());
                    } else {
                        riderDestination.setText("Destination: --");
                    }

                    if (map.get("cost") != null) {
                        cost = Double.parseDouble(map.get("cost").toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_styling));
            if (!success) {
                Log.e("MapError: ", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapError: ", "Can't find style. Error: ", e);
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        builtGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void builtGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        myLocationRequest = new LocationRequest();
        myLocationRequest.setInterval(1000);
        myLocationRequest.setFastestInterval(1000);
        myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client, myLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {

        if (getApplicationContext() != null) {

            if (driverLocation != null) {
                driverLocation.remove();
            }

            if (available) {
                myLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                driverLocation = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon))
                        .position(latLng)
                        .flat(true)
                        .rotation(245));
                CameraPosition cameraPosition = CameraPosition.builder()
                        .target(latLng)
                        .zoom(16)
                        .bearing(60)
                        .build();
                cameraPositions = true;
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);
            }

            if (!isDisconnected) {

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference DriverAvlRef = FirebaseDatabase.getInstance().getReference("driverAvailable");
                DatabaseReference DriverWorkRef = FirebaseDatabase.getInstance().getReference("driverWorking");

                GeoFire geoFireAvl = new GeoFire(DriverAvlRef);
                GeoFire geoFireWork = new GeoFire(DriverWorkRef);

                switch (riderID) {

                    case "":
                        geoFireWork.removeLocation(userId, new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {

                            }
                        });
                        geoFireAvl.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                available = true;
                            }
                        });
                        break;
                    default:
                        geoFireAvl.removeLocation(userId, new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {

                            }
                        });
                        geoFireWork.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                available = false;
                            }
                        });
                        break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        driverDisconnected();
        finishAffinity();
    }

    private void driverDisconnected() {
        isDisconnected = true;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("driverAvailable");
        GeoFire geoFire = new GeoFire(reference);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.driverProfile:
                startActivity(new Intent(DriverMapActivity.this, DriverProfile.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case R.id.driverHistory:
                startActivity(new Intent(DriverMapActivity.this, HistoryActivity.class).putExtra("riderOrDriver", "driver"));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case R.id.driverSetting:
                startActivity(new Intent(DriverMapActivity.this, Settings.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case R.id.driverLogOut:
                driverDisconnected();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(DriverMapActivity.this, EmailLogInActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
                break;
        }
        return false;
    }

    private void getUserInfo() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("dName") != null) {
                        dName = map.get("dName").toString();
                        userName.setText(dName);
                    }
                    if (map.get("dPhone") != null) {
                        dPhone = map.get("dPhone").toString();
                        userPhones.setText(dPhone);
                    }
                    if (map.get("profileImage") != null) {
                        myProfileUrl = map.get("profileImage").toString();
                        Glide.with(getApplication()).load(myProfileUrl).into(iv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getRiderInformation() {

        customLinearLayout.setVisibility(View.VISIBLE);

        DatabaseReference riderDatabaseReferences = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child("Profile").child(riderID);

        riderDatabaseReferences.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                    Map<String, Object> maps = (Map<String, Object>) dataSnapshot.getValue();

                    if (maps.get("rName") != null) {
                        riderName.setText(maps.get("rName").toString());
                    }
                    if (maps.get("rPhone") != null) {
                        riderPhone.setText(maps.get("rPhone").toString());
                    }
                    if (maps.get("profileImage") != null) {
                        Glide.with(getApplication()).load(maps.get("profileImage").toString()).into(riderIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void makePhoneCall() {
        String number = riderPhone.getText().toString();
        Intent i = new Intent(Intent.ACTION_CALL);
        i.setData(Uri.parse("tel:" + number));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.CALL_PHONE}, CALL_REQUEST);
        }
        startActivity(i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mapFragment.getMapAsync(this);
                }else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case CALL_REQUEST:{
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    makePhoneCall();
                }else {

                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_SHORT).show();
                }
            }
                break;
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {

        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() { }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() { }

    private void erasePolyline(){
        for (Polyline polyline: polylines){
            polyline.remove();
        }
        polylines.clear();
    }

    @SuppressLint("SetTextI18n")
    private void clearDriverProfile(){
        erasePolyline();
        riderID = "";
        if (pickupLocationMarker!= null){
            pickupLocationMarker.remove();
        }

        if (riderPickUpLocationRef != null){
            riderPickUpLocationRef.removeEventListener(riderPickUpLocationRefListener);
        }

        customLinearLayout.setVisibility(View.GONE);
        riderName.setText("");
        riderPhone.setText("");
        riderDestination.setText("Destination: --");
        riderIv.setImageResource(R.drawable.profile);
    }

    @SuppressLint("SetTextI18n")
    private void removeRider(){

        if (riderID != null) {

            DatabaseReference driverReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child("History").child(userID);
            DatabaseReference riderReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child("History").child(riderID);
            DatabaseReference history = FirebaseDatabase.getInstance().getReference().child("History");

            String historyId = history.push().getKey();

            if (historyId != null) {
                driverReference.child(historyId).setValue(true);
                riderReference.child(historyId).setValue(true);

                HashMap map = new HashMap();
                map.put("driverId", userID);
                map.put("riderId", riderID);
                map.put("destination", riderDestination.getText().toString());
                map.put("driverRating", 0);
                map.put("cost", cost);
                history.child(historyId).updateChildren(map);
            }
            cancelRider();
        }
    }

    @SuppressLint("SetTextI18n")
    public void cancelRider(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child("Activity").child(userID);
        DatabaseReference riderRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child("Activity").child(riderID);

        riderRef.setValue(true);
        ref.setValue(true);
        riderID = null;


        if (reference != null){
            reference.removeEventListener(riderStatusValueEventListener);
        }

        customLinearLayout.setVisibility(View.GONE);
        btnRideComplete.setVisibility(View.GONE);
        btnCall.setVisibility(View.VISIBLE);
        btnCall.setText("Call");
        clearDriverProfile();
    }
}
