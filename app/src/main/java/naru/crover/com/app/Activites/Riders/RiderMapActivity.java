package naru.crover.com.app.Activites.Riders;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
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
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import naru.crover.com.app.Activites.EmailLogInActivity;
import naru.crover.com.app.Activites.HistoryActivity;
import naru.crover.com.app.Activites.Settings;
import naru.crover.com.app.Adapter.PlaceAutocompleteAdapter;
import naru.crover.com.app.Models.PlaceInfo;
import naru.crover.com.app.R;
import com.google.android.gms.location.places.PlaceDetectionClient;

public class RiderMapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, RoutingListener{

    private GoogleMap mMap;
    private GoogleApiClient client, mGoogleApiClient;
    private Location myLocation;
    private LocationRequest myLocationRequest;
    private Button pickUpRequest, btnCancel;
    private boolean isRider = false;
    private LatLng pickupLocation;
    private double radius = 0.01;
    private boolean driverFound = false;
    private String foundedDriverID;
    private Marker pickupMarker, destinationMarker;
    private boolean requestBoolean = false;
    private DatabaseReference driverLocationRef, databaseReference, ref, reference, ref2, ride, callingDataRef;
    private ValueEventListener driverLocationRefListener, riderStatusListener, driverServiceValueEventListener, va1, val2, val3, val4;
    private GeoFire geoFire;
    private GeoQuery geoQuery;
    private Marker driverMarker;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    private NavigationView navigationView;
    private TextView userName,userPhones, driverName, driverPhone, ambulanceCost, cngCost;
    private ImageView iv, driverImage;
    private FirebaseAuth mAuth;
    private String rName, rPhone, myProfileUrl, destination, service = "ambulance", userID, driverId , ambulance, cng;
    private SupportMapFragment mapFragment;
    final int LOCATION_REQUEST_CODE = 1;
    private AutoCompleteTextView searchText;
    private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    protected GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private PlaceInfo mPlace;
    private LinearLayout customLinearLayout, serviceLayout, optionalLayout;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorWhite,R.color.colorButton,R.color.colorYellow,R.color.colorDimBlack};
    private boolean destinationLocation = false;
    private LatLng destinationLatLng;
    private String driverPhoneNumber;
    private boolean isDriverCall = false;
    private boolean isDriverPhoneReceived = false;
    private final int PHONE_STATE = 3;

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));

    private ImageView btnCng, btnAmbulance;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_rider_map);

        //Service & Optional Layout
        serviceLayout = findViewById(R.id.layoutService);
        optionalLayout = findViewById(R.id.optionalLayout);

        // Place Auto Complete Text Box
        searchText = findViewById(R.id.searchTB);
        mGeoDataClient = Places.getGeoDataClient(this, null);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        searchText.setOnItemClickListener(mAutocompleteClickListener);
        placeAutocompleteAdapter = new PlaceAutocompleteAdapter( this, mGoogleApiClient,BOUNDS_GREATER_SYDNEY, null );
        searchText.setAdapter(placeAutocompleteAdapter);

        //FireBase Authentication for user id & Database
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child("Profile").child(userID);

        //Display Driver Profile
        customLinearLayout = findViewById(R.id.driverProfile);
        driverName = findViewById(R.id.userNameTv);
        driverPhone = findViewById(R.id.userPhoneNumberTv);
        driverImage = findViewById(R.id.driverImageV);
//        phoneBtn = findViewById(R.id.phoneBtn);

        //Toolbar, DrawerLayout & NavigationView
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        drawerLayout = findViewById(R.id.riderDrawer);
        mToggle = new ActionBarDrawerToggle(this, drawerLayout, mToolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        navigationView = findViewById(R.id.riderNavView);

        //Display Rider Profile at the top of the NavigationView
        View headerView = navigationView.getHeaderView(0);
        userName = headerView.findViewById(R.id.riderName);
        userPhones = headerView.findViewById(R.id.riderPhone);
        iv = headerView.findViewById(R.id.riderImage);

        //User Information for Edit & Display
        getUserInfo();

        //NavigationView Item Click Listener
        navigationView.setNavigationItemSelectedListener(this);

        //Bind Map Fragment To this Activity
        initMap();

        //Rider Request for pickup
        pickUpRequest = findViewById(R.id.btnFind);
        pickUpRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    requestBoolean = true;
                    serviceLayout.setVisibility(View.GONE);
                    btnCancel.setVisibility(View.VISIBLE);
                    pickUpRequest.setEnabled(false);

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("riderRequest");

                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(myLocation.getLatitude(), myLocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
                    pickupLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    pickupMarker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.user_location_icon))
                            .position(pickupLocation).title("My Location"));

                    pickUpRequest.setText("Getting your driver....");
                    gettingClosestDriver();
            }
        });

        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelRequestActivity();
            }

        });

        getRiderStatus();


        if (ContextCompat.checkSelfPermission(RiderMapActivity.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(RiderMapActivity.this,
                    Manifest.permission.READ_PHONE_STATE)){
                ActivityCompat.requestPermissions(RiderMapActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, PHONE_STATE);
            }else {
                ActivityCompat.requestPermissions(RiderMapActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, PHONE_STATE);
            }
        }else {
            //do nothing
        }


        btnCng = findViewById(R.id.btnCng);
        btnAmbulance = findViewById(R.id.btnAmbulance);

        btnCng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                service = "cng";

                btnCng.setBackground(getResources().getDrawable(R.drawable.round_button_service));
                btnAmbulance.setBackground(getResources().getDrawable(R.drawable.round_button));
            }
        });
        btnAmbulance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                service = "ambulance";

                btnCng.setBackground(getResources().getDrawable(R.drawable.round_button));
                btnAmbulance.setBackground(getResources().getDrawable(R.drawable.round_button_service));
            }
        });

        ambulanceCost = findViewById(R.id.tvAmbulanceCost);
        cngCost = findViewById(R.id.tvCngCost);

        onPhoneCall();

    }

    //For Driver Rider Calling

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onPhoneCall();
            }
        }, 2000);

    }
    @Override
    protected void onPause() {
        super.onPause();
        onPhoneCall();
    }
    private void onPhoneCall(){

        callingDataRef = FirebaseDatabase.getInstance().getReference().child("callStatus").child(userID);
        val3 = callingDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() >= 2){

                    Map<String, Object> map =(Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("number") != null){
                        String number = map.get("number").toString();
                        onCallRinging(number);
                    }

                    if (map.get("received") != null){
                       onCallReceived();
                    }

                    if (map.get("end") != null){
                        onCallEnd();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public void onCallRinging( String number){

        this.driverPhoneNumber = number;
        ref2 = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child("Activity").child(userID).child("foundDriver").child("driverID");
        va1 = ref2.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    driverId = dataSnapshot.getValue().toString();
                    ride = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child("Profile").child(driverId).child("dPhone");
                    val2 = ride.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()){
                                    isDriverCall = true;
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
    public void onCallReceived(){
        if (isDriverCall){
            isDriverPhoneReceived = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onRiderStatus(1);
                }
            }, 3000);
        }
    }
    public void onCallEnd(){
        if (isDriverCall && !isDriverPhoneReceived){
            onRiderStatus(0);
        }
    }

    // if ride is over this activity is called
    private void getRiderStatus(){
        String riderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child("Activity").child(riderId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() == 0){
                    cancelRequestActivity();
                }else {}
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    //Getting Driver Information
    private void gettingDriverInformation(){

        String riderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child("Activity").child(riderId).child("riderStatus").child("rideStarted");
        riderStatusListener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    String status = dataSnapshot.getValue().toString();
                    if (status.equals("0")){
                        Toast.makeText(RiderMapActivity.this, "Because of your in activity your request has been canceled... !!", Toast.LENGTH_SHORT).show();
                        cancelRequestActivity();
                    }else if (status.equals("1")){
                        getDriverLocation();
                    }

                }else {}

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    //Search For Getting Closest Driver
    private void gettingClosestDriver(){
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("driverAvailable");
        geoFire = new GeoFire(driverRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if (!driverFound && requestBoolean){

                    foundedDriverID = key;

                    reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child("Profile").child(key).child("dService");
                    driverServiceValueEventListener = reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()){

                                startCountDown();

                                String driverService = dataSnapshot.getValue().toString();
                                if (service.equals(driverService)){

                                    driverFound = true;
                                    driverId = foundedDriverID;
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child("Activity").child(foundedDriverID).child("riderRequest");
                                    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                    HashMap m = new HashMap();
                                    m.put("riderId", userID);
                                    m.put("destination", destination);
                                    m.put("desLat", destinationLatLng.latitude );
                                    m.put("desLang", destinationLatLng.longitude);

                                    if (service.equals("ambulance")){
                                        m.put("cost",  ambulance);
                                    }else if (service.equals("cng")){
                                        m.put("cost", cng);
                                    }

                                    ref.updateChildren(m);
                                    gettingDriverInformation();
                                    btnCancel.setVisibility(View.GONE);
                                    pickUpRequest.setText("We Found Driver For You");

                                }

                            }

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
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

                if (!driverFound && requestBoolean){

                    if (radius <= 5){
                        radius+= 0.01;
                        gettingClosestDriver();
                    }else {
                        AlertDialog.Builder builder;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(RiderMapActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                        } else {
                            builder = new AlertDialog.Builder(RiderMapActivity.this);
                        }
                        builder.setTitle("Sorry No Driver Found At that moment!")
                                .setMessage("Are you sure you want to search again?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        pickUpRequest.setText("Getting your driver....");
                                        gettingClosestDriver();

                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        cancelRequestActivity();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                }
            }
            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }
    //Getting Closest Driver Location
    private void getDriverLocation(){
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driverWorking").child(foundedDriverID).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && requestBoolean){
                    List<Object> driverList = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;

                    if (driverList.get(0) != null){
                        locationLat = Double.parseDouble(driverList.get(0).toString());
                    }
                    if (driverList.get(1) != null){
                        locationLng = Double.parseDouble(driverList.get(1).toString());
                    }

                    LatLng driverLatLng = new LatLng(locationLat, locationLng);

                    if (driverMarker!= null){
                        driverMarker.remove();
                    }

                    if (destinationMarker != null){
                        destinationMarker.remove();
                    }
                    Location riderLocation = new Location("");
                    riderLocation.setLatitude(pickupLocation.latitude);
                    riderLocation.setLongitude(pickupLocation.longitude);

                    Location driverLocation = new Location("");
                    driverLocation.setLatitude(driverLatLng.latitude);
                    driverLocation.setLongitude(driverLatLng.longitude);

                    float distance = riderLocation.distanceTo(driverLocation);

                    if (distance < 100){
                        pickUpRequest.setText("Driver is nearby");
                        getDriverInformation();
                    }else {
                        pickUpRequest.setText("Driver is: " + distance/1000 + "km away");
                        getDriverInformation();
                    }
                    driverMarker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon))
                            .position(driverLatLng).title("Your Driver"));

                    erasePolyline();
                    getRouteToMarker(driverLatLng);

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void startCountDown(){

        new CountDownTimer(30000, 10000){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child("Activity").child(userID).child("foundDriver").child("driverID");
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            pickUpRequest.setText("We Found Driver For You");
                        }else {
                            Toast.makeText(RiderMapActivity.this, "Due to unavoidable circumstance yor request has been canceled ", Toast.LENGTH_SHORT).show();
                            cancelRequestActivity();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }.start();
    }
    //Initial Map Fragment
    public void initMap() {
        polylines = new ArrayList<>();
         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RiderMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }else {
            mapFragment.getMapAsync(this);
        }

    }
    //Get Rider Destination from Place AutoComplete
    private void getDestination(){
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER){
                    // execute method for search
                    geoLocate();
                }

                return false;
            }
        });
    }
    //Getting Geo Location From Place AutoComplete
    private void geoLocate(){
        String searchString = searchText.getText().toString();

        Geocoder geocoder = new Geocoder(RiderMapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.d("TER", "ERROR: "+ e.getMessage());
        }
        if (list.size() > 0){
            Address address = list.get(0);
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
            Log.d("ADDRESS", "PLACE: "+ address.toString());

            onDestinationLocation(new LatLng(address.getLatitude(), address.getLongitude()), 15, address.getLocality());
            hideSoftKeyBoard();

        }
    }
    //Place AutoComplete
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = placeAutocompleteAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    //Place AutoComplete Result Callback
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {

            if (!places.getStatus().isSuccess()){

                Log.d("Error", "Unsuccessful " + places.getStatus().getStatusMessage());
                places.release();
                return;
            }else {
                final Place place = places.get(0);

                try{

                    mPlace = new PlaceInfo();
                    mPlace.setName(place.getName().toString());
                    mPlace.setAddress(place.getAddress().toString());
                    mPlace.setAttribution(place.getAttributions().toString());
                    mPlace.setId(place.getId());
                    mPlace.setLatLng(place.getLatLng());
                    mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                    mPlace.setRating(place.getRating());
                    mPlace.setWebSiteUri(place.getWebsiteUri());

                }catch (NullPointerException e){
                    Log.d("ERROR", "Message: "+ mPlace.toString());
                }
                onDestinationLocation(new LatLng(place.getViewport().getCenter().latitude,
                        place.getViewport().getCenter().longitude), 15, mPlace.getName());
                places.release();
            }

        }
    };
    //Implement Map Styling and other Necessary things
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
            ActivityCompat.requestPermissions(RiderMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        builtGoogleApiClient();
        getDestination();
        mMap.setMyLocationEnabled(true);
    }
    //Synchronized Task for Google Api Client
    protected synchronized void builtGoogleApiClient(){

        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();

    }
    //Getting My Current Location
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        myLocationRequest = new LocationRequest();
        myLocationRequest.setInterval(1000);
        myLocationRequest.setFastestInterval(1000);
        myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RiderMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client, myLocationRequest, this);
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    //Update My Current Location
    @Override
    public void onLocationChanged(Location location) {

        if (!destinationLocation){
            myLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16), 2000, null);
        }
    }
    //Move Camera to my Destination
    public void onDestinationLocation(LatLng latLng, float zoom, String title){

        Location location = new Location("");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);

        double cost = myLocation.distanceTo(location) /1000;

        destination = title;
        destinationLocation = true;
        destinationLatLng = new LatLng(latLng.latitude, latLng.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom), 2000, null);

        destinationMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.dest))
                .position(destinationLatLng).title(title));

        searchText.setVisibility(View.GONE);
        getRouteToMarker(latLng);
        displayServiceLayout(cost);
        hideSoftKeyBoard();
    }
    @SuppressLint("SetTextI18n")
    private void displayServiceLayout(Double cost) {
        optionalLayout.setVisibility(View.VISIBLE);
        serviceLayout.setVisibility(View.VISIBLE);

        ambulance = new DecimalFormat("##").format(cost * 25);
        cng = new DecimalFormat("##").format(cost * 50);

        ambulanceCost.setText("BDT " + ambulance + "/-");
        cngCost.setText("BDT " + cng + "/-");

    }
    private void getRouteToMarker(LatLng latLng){

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), latLng)
                .build();
        routing.execute();

    }
    //If Rider is Disconnected
    private void riderDisconnected(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("rider");

        GeoFire geoFire = new GeoFire(reference);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
    }
    //If The Device is Stop
    @Override
    protected void onStop() {
        super.onStop();

        if (!isRider){
            riderDisconnected();
        }
    }
    //BackPressed
    @Override
    public void onBackPressed() {
        cancelRequestActivity();
    }
    //For Navigation Item Selected Listener
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id){
            case R.id.profile:
                startActivity(new Intent(RiderMapActivity.this, RiderProfileActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case R.id.riderHistory:
                startActivity(new Intent(RiderMapActivity.this, HistoryActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case R.id.setting:
                startActivity(new Intent(RiderMapActivity.this, Settings.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case R.id.logOut:
                isRider = true;
                riderDisconnected();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(RiderMapActivity.this, EmailLogInActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
                break;
        }

        return false;
    }
    //Getting User Info From Database
    private void getUserInfo(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("rName") != null){
                        rName = map.get("rName").toString();
                        userName.setText(rName);
                    }
                    if (map.get("rPhone") != null){
                        rPhone = map.get("rPhone").toString();
                        userPhones.setText(rPhone);
                    }
                    if (map.get("profileImage") != null){
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
    //Getting Driver Info From Database
    public void getDriverInformation(){

            customLinearLayout.setVisibility(View.VISIBLE);

            DatabaseReference riderDatabaseReferences = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child("Profile").child(driverId);

            riderDatabaseReferences.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){

                        Map<String, Object> maps = (Map<String, Object>)dataSnapshot.getValue();

                        if (maps.get("dName") != null){
                            driverName.setText(maps.get("dName").toString());
                        }
                        if (maps.get("dPhone") != null){
                            driverPhone.setText(maps.get("dPhone").toString());
                        }
                        if (maps.get("profileImage") != null){
                            Glide.with(getApplication()).load(maps.get("profileImage").toString()).into(driverImage);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }
    //Request Permission For Different Version of Android Phone
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
            case PHONE_STATE:{
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    if (ContextCompat.checkSelfPermission(RiderMapActivity.this,
                            Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
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
    public void onRoutingStart() {

    }
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
    private void cancelRequestActivity(){
        requestBoolean = false;
        destinationLocation = false;
        erasePolyline();
        searchText.setVisibility(View.VISIBLE);
        searchText.setText("");
        serviceLayout.setVisibility(View.GONE);
        optionalLayout.setVisibility(View.GONE);
        customLinearLayout.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        service = "bike";
        cng = "";
        ambulance = "";

        if (callingDataRef != null){
            callingDataRef.removeEventListener(val3);
        }

        if (geoQuery != null){
            geoQuery.removeAllListeners();
        }
        if (driverLocationRef != null){
            driverLocationRef.removeEventListener(driverLocationRefListener);
        }
        if (foundedDriverID != null){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child("Activity").child(foundedDriverID);
            ref.setValue(true);
        }

        foundedDriverID = null;

        if (reference != null){
            reference.removeEventListener(driverServiceValueEventListener);
        }

        if (ref != null){
            ref.removeEventListener(riderStatusListener);
        }

        if (ref2 != null){
            ref2.removeEventListener(va1);
        }

        if (ride != null){
            ride.removeEventListener(val2);
        }

        if (callingDataRef != null){
            callingDataRef.removeEventListener(val3);
        }
        isDriverCall = false;
        driverFound = false;
        isDriverPhoneReceived = false;
        radius = 0.01;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("riderRequest");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
            }
        });

        if (pickupMarker!= null){
            pickupMarker.remove();
        }

        if (destinationMarker != null){
            destinationMarker.remove();
        }

        if (driverMarker != null){
            driverMarker.remove();
        }
        pickUpRequest.setText("Find Driver");
        pickUpRequest.setEnabled(true);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child("Activity").child(userID);
        databaseReference.setValue(true);

    }
    private void hideSoftKeyBoard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void onRiderStatus(int i){
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child("Activity").child(userID).child("riderStatus");
        HashMap map = new HashMap();
        map.put("rideStarted", i);
        ref2.updateChildren(map);
    }
}
