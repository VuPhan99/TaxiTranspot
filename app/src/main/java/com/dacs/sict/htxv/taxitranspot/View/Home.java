package com.dacs.sict.htxv.taxitranspot.View;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dacs.sict.htxv.taxitranspot.Common.Common;
import com.dacs.sict.htxv.taxitranspot.Common.SessionManager;
import com.dacs.sict.htxv.taxitranspot.Model.InformationUser;
import com.dacs.sict.htxv.taxitranspot.Model.Notification;
import com.dacs.sict.htxv.taxitranspot.Model.Token;
import com.dacs.sict.htxv.taxitranspot.R;
import com.dacs.sict.htxv.taxitranspot.Remote.IGoogleApi;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;


import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private GoogleMap mMap;

    //Play Services
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUETS = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference drivers;
    GeoFire geoFire;

    Marker mCurrent;

    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;

    //Car animation
    private List<LatLng> polyLineList;
    private Marker carMarker;
    private float v;
    private double lat, lng;
    private Handler handler;
    private LatLng startPosition, endPosition, currentPosition;
    private int index, next;
    //private Button btnGo;
    private PlaceAutocompleteFragment places;
    private String destination;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyline;

    private IGoogleApi mService;

    private SessionManager mSessionManager;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mFirebaseUser;

    //presence system
    DatabaseReference onlineRef, currentUserRef;


    /* private String TAG = "FRAGMENT_AUTOCOMPLETE";*/
    private AutocompleteSupportFragment autocompleteSupportFragment;

    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {
            if (index < polyLineList.size() - 1) {
                index++;
                next = index + 1;
            }
            if (index < polyLineList.size() - 1) {
                startPosition = polyLineList.get(index);
                endPosition = polyLineList.get(next);
            }

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    v = valueAnimator.getAnimatedFraction();
                    lng = v * endPosition.longitude + (1-v) * startPosition.longitude;
                    lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;
                    LatLng newPos = new LatLng(lat, lng);
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f, 0.5f);
                    carMarker.setRotation(getBearing(startPosition, newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(newPos)
                                    .zoom(15.5f)
                                    .build()
                    ));
                }
            });
            valueAnimator.start();
            handler.postDelayed(this, 3000);
        }
    };

    private float getBearing(LatLng startPosition, LatLng endPosition) {
        double lat = Math.abs(startPosition.latitude - endPosition.latitude);
        double lng = Math.abs(startPosition.longitude - endPosition.longitude);

        if (startPosition.latitude < endPosition.latitude && startPosition.longitude < endPosition.longitude) {
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        } else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude < endPosition.longitude) {
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        } else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude >= endPosition.longitude) {
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        } else if (startPosition.latitude < endPosition.latitude && startPosition.longitude >= endPosition.longitude) {
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        }
        return -1;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_home );
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );


        try {
            mSessionManager = new SessionManager( getApplicationContext() );
            String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            if (!mSessionManager.isLogin() && mSessionManager.checkUserId() != uID) {
                Toast.makeText( this, "Please login to countinue", Toast.LENGTH_SHORT ).show();
                Intent intent = new Intent( this, SPFlashScreen.class );
                intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                this.startActivity( intent );
            }
        } catch (NullPointerException e) {
        }


        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
        drawer.addDrawerListener( toggle );
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById( R.id.nav_view );
        navigationView.setNavigationItemSelectedListener( this );

        setInfoHeaderNavBar();


        //map
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById( R.id.map );
        mapFragment.getMapAsync( this );


        //Presense System
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.driver_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //We will remove value from Driver tbl when driver disconnected

                currentUserRef.onDisconnect().removeValue();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e( "email_", "Failed for read data from Firebase, errors: " + databaseError );

            }
        });
        //Init View
        location_switch = findViewById(R.id.location_switch);
        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if (isOnline) {

                    // set connected when switch to on
                    FirebaseDatabase.getInstance().goOnline();

                    startLocationUpdates();
                    displayLocation();
                    Snackbar.make(mapFragment.getView(), "You are online", Snackbar.LENGTH_SHORT)
                            .show();

                } else {

                    // set disconnected when switch to off
                    FirebaseDatabase.getInstance().goOffline();

                    stopLocationUpdates();
                    mCurrent.remove();
                    mMap.clear();
                    if (handler != null) handler.removeCallbacks( drawPathRunnable );
                    mMap.setMyLocationEnabled( false );
                    /*handler.removeCallbacks(drawPathRunnable);*/
                    Snackbar.make(mapFragment.getView(), "You are offline", Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });




        polyLineList = new ArrayList<>();

        //Places API
        places = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (location_switch.isChecked()) {
                    destination = place.getAddress().toString();
                    destination = destination.replace(" ", "+");

                    getDirection();

                } else {
                    Toast.makeText(Home.this, "Please change your status to ONLINE", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(Home.this, "" + status.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        // PLACE
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }*/

       /* polyLineList = new ArrayList<>();
        Places.initialize( getApplicationContext(), "AIzaSyD2wAtMMw3lGT6Rivl9lJjbtdGPmA7OS9E" );

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient( this );


        // Initialize the AutocompleteSupportFragment.
        autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById( R.id.autocomplete_fragment );


        // Specify the types of place data to return.
        autocompleteSupportFragment.setPlaceFields( Arrays.asList( com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME ) );

        // Set up a PlaceSelectionListener to handle the response.

        autocompleteSupportFragment.setOnPlaceSelectedListener( new com.google.android.libraries.places.widget.listener.PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull com.google.android.libraries.places.api.model.Place place) {
                // TODO: Get info about the selected place.
                if (location_switch.isChecked()) {
                    destination = place.getAddress().toString();
                    destination = destination.replace( "", "+" );

                    getDirection();

                } else {
                    Toast.makeText( Home.this, "Please change your status to ONLINE", Toast.LENGTH_SHORT ).show();
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i( TAG, "An error occurred: " + status );
            }
        } );*/
        //geo fire
        drivers = FirebaseDatabase.getInstance().getReference( Common.driver_tbl );
        geoFire = new GeoFire( drivers );
        setUpLocation();

        mService = Common.getGoogleApi();

        updateFirebaseToken();
    }
    private void setInfoHeaderNavBar() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference( Common.user_driver_tbl ).child( mFirebaseAuth.getInstance().getCurrentUser().getUid() );

        mDatabaseReference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                InformationUser uInfo = new InformationUser();
//                InformationUser uInfo = dataSnapshot.getValue(InformationUser.class);
                uInfo.setmEmail( dataSnapshot.child( "mEmail" ).getValue( String.class ) );
                uInfo.setmName( dataSnapshot.child( "mName" ).getValue( String.class ) );


                NavigationView navigationView = (NavigationView) findViewById( R.id.nav_view );
                View headerView = navigationView.getHeaderView( 0 );
                TextView txtHeaderNavHomeName = headerView.findViewById( R.id.txt_nav_header_name );
                TextView txtHeaderNavHomeEmail = headerView.findViewById( R.id.txt_nav_header_email );
                txtHeaderNavHomeName.setText( uInfo.getmName() );
                txtHeaderNavHomeEmail.setText( uInfo.getmEmail() );


                Log.v( "email_2", uInfo.getmEmail() + uInfo.getmName() );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e( "email_", "Failed for read data from Firebase, errors: " + databaseError );
            }
        } );
    }

    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference( Common.token_tbl );

        Token token = new Token( FirebaseInstanceId.getInstance().getToken() );
        tokens.child( FirebaseAuth.getInstance().getCurrentUser().getUid() ).setValue( token );
    }

    private void getDirection() {
        currentPosition = new LatLng( Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude() );

        String requestApi = null;

        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" + "mode=driving&" + "transit_routing_preference=less_driving&" + "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" + "destination=" + destination + "&" + "key=" + getResources().getString( R.string.google_direction_api );

            Log.d( "DEV", requestApi ); // Print URL for debug

            mService.getPath( requestApi ).enqueue( new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    try {
                        JSONObject jsonObject = new JSONObject( response.body().toString() );
                        JSONArray jsonArray = jsonObject.getJSONArray( "routes" );
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject route = jsonArray.getJSONObject( i );
                            JSONObject poly = route.getJSONObject( "overview_polyline" );
                            String polyline = poly.getString( "points" );
                            polyLineList = decodePoly( polyline );
                        }

                        //Adjusting bounds
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng latLng : polyLineList) {
                            builder.include( latLng );
                        }
                        LatLngBounds bounds = builder.build();
                        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds( bounds, 2 );
                        mMap.animateCamera( mCameraUpdate );

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color( Color.GRAY );
                        polylineOptions.width( 5 );
                        polylineOptions.startCap( new SquareCap() );
                        polylineOptions.endCap( new SquareCap() );
                        polylineOptions.jointType( JointType.ROUND );
                        polylineOptions.addAll( polyLineList );
                        greyPolyline = mMap.addPolyline( polylineOptions );

                        blackPolylineOptions = new PolylineOptions();
                        blackPolylineOptions.color( Color.BLACK );
                        blackPolylineOptions.width( 5 );
                        blackPolylineOptions.startCap( new SquareCap() );
                        blackPolylineOptions.endCap( new SquareCap() );
                        blackPolylineOptions.jointType( JointType.ROUND );
                        blackPolylineOptions.addAll( polyLineList );
                        blackPolyline = mMap.addPolyline( blackPolylineOptions );

                        mMap.addMarker( new MarkerOptions().position( polyLineList.get( polyLineList.size() - 1 ) ).title( "Pickup Location" ) );

                        //Animation
                        ValueAnimator polyLineAnimator = ValueAnimator.ofInt( 0, 100 );
                        polyLineAnimator.setDuration( 2000 );
                        polyLineAnimator.setInterpolator( new LinearInterpolator() );
                        polyLineAnimator.addUpdateListener( new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                List<LatLng> points = greyPolyline.getPoints();
                                int percentValue = (int) valueAnimator.getAnimatedValue();
                                int size = points.size();
                                int newPoints = (int) (size * (percentValue / 100.0f));
                                List<LatLng> p = points.subList( 0, newPoints );
                                blackPolyline.setPoints( p );
                            }
                        } );
                        polyLineAnimator.start();

                        carMarker = mMap.addMarker( new MarkerOptions().position( currentPosition ).flat( true ).icon( BitmapDescriptorFactory.fromResource( R.drawable.car ) ) );

                        handler = new Handler();
                        index = -1;
                        next = 1;
                        handler.postDelayed( drawPathRunnable, 3000 );

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText( Home.this, "" + t.getMessage(), Toast.LENGTH_SHORT ).show();
                    t.printStackTrace();
                }
            } );

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt( index++ ) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt( index++ ) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)), (((double) lng / 1E5)) );
            poly.add( p );
        }

        return poly;
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        if (location_switch.isChecked()) displayLocation();
                    }

                }
        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            //request runtime permission
            ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE );
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                if (location_switch.isChecked()) displayLocation();
            }
        }
    }


    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval( UPDATE_INTERVAL );
        mLocationRequest.setFastestInterval( FATEST_INTERVAL );
        mLocationRequest.setPriority( LocationRequest.PRIORITY_HIGH_ACCURACY );
        mLocationRequest.setSmallestDisplacement( DISPLACEMENT );

    }


    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder( this ).addConnectionCallbacks( this ).addOnConnectionFailedListener( this ).addApi( LocationServices.API ).build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable( this );
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError( resultCode ))
                GooglePlayServicesUtil.getErrorDialog( resultCode, this, PLAY_SERVICE_RES_REQUETS ).show();
            else {
                Toast.makeText( this, "This device not supported", Toast.LENGTH_SHORT ).show();
                finish();
            }
            return false;


        }
        return true;
    }


    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates( mGoogleApiClient, this );
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Common.mLastLocation = LocationServices.FusedLocationApi.getLastLocation( mGoogleApiClient );
        if (Common.mLastLocation != null) {
            if (location_switch.isChecked()) {
                final double latitude = Common.mLastLocation.getLatitude();
                final double longitude = Common.mLastLocation.getLongitude();
                //update to firebase
                geoFire.setLocation( FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation( latitude, longitude ), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        // add marker
                        if (mCurrent != null) mCurrent.remove();//remove allready marker
                        mCurrent = mMap.addMarker( new MarkerOptions().position( new LatLng( latitude, longitude ) ).title( "Your Location" ) );

                        //move camera this potition



                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById( R.id.map );
                        int ZoomControl_id = 0x1;

                        // Find ZoomControl view
                        View zoomControls = mapFragment.getView().findViewById( ZoomControl_id );

                        if (zoomControls != null && zoomControls.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                            // ZoomControl is inside of RelativeLayout
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) zoomControls.getLayoutParams();

                            // Update margins, set to 10dp
                            final int margin = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics() );
                            params.setMargins( margin, margin, margin, margin + 220 );
                        }

                        View _mapView = mapFragment.getView();

                        // Get map views
                        View location_button = _mapView.findViewWithTag( "GoogleMapMyLocationButton" );
                        View zoom_in_button = _mapView.findViewWithTag( "GoogleMapZoomInButton" );

                        View zoom_layout = (View) zoom_in_button.getParent();

                        // adjust location button layout params above the zoom layout
                        RelativeLayout.LayoutParams location_layout = (RelativeLayout.LayoutParams) location_button.getLayoutParams();
                        location_layout.setMargins( 0, 0, 0, 20 );

                        location_layout.addRule( RelativeLayout.ALIGN_PARENT_TOP, 0 );
                        location_layout.addRule( RelativeLayout.ABOVE, zoom_layout.getId() );

                        mMap.animateCamera( CameraUpdateFactory.newLatLngZoom( new LatLng( latitude, longitude ), 15.0f ) );
                        mMap.setMyLocationEnabled( true );
                        mMap.getUiSettings().setMyLocationButtonEnabled( true );


                    }
                } );
            }
        } else {
            Log.d( "ERROR", "Cannot get your location" );
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates( mGoogleApiClient, mLocationRequest, this );
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        if (drawer.isDrawerOpen( GravityCompat.START )) {
            drawer.closeDrawer( GravityCompat.START );
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.home, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_myaccount) {
            Intent intentMyAccount = new Intent( this, MyAccount.class );
            startActivity( intentMyAccount );
            // Handle the camera action
        } else if (id == R.id.nav_notifications) {
            Intent intentNotifications = new Intent( this, Notifications.class );
            startActivity( intentNotifications );

        } else if (id == R.id.nav_history) {

        } else if (id == R.id.nav_support) {
            Intent intentSuport = new Intent( this, Support.class );
            startActivity( intentSuport );

        } else if (id == R.id.nav_logout) {

            mFirebaseAuth.getInstance().signOut();
            /*FileUtils.deleteQuietly( getApplicationContext().getCacheDir() );
            FileUtils.deleteQuietly( getApplicationContext().getExternalCacheDir() );
            FileUtils.deleteQuietly( getApplicationContext().getCodeCacheDir() );
            FileUtils.deleteQuietly( getApplicationContext().getDataDir() );*/

            Intent intent = new Intent( this, LoginActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            startActivity( intent );

        }
        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        drawer.closeDrawer( GravityCompat.START );
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Common.mLastLocation = location;
        displayLocation();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType( GoogleMap.MAP_TYPE_NORMAL );
        mMap.setTrafficEnabled( false );
        mMap.setIndoorEnabled( false );
        mMap.setBuildingsEnabled( false );
        mMap.getUiSettings().setZoomControlsEnabled( true );
        mMap.getUiSettings().setZoomGesturesEnabled( true );
    }
}
