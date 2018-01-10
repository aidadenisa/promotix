package blog.aida.promotixproject;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.FirebaseDatabase;

import android.location.Geocoder;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import blog.aida.promotixproject.model.Promotion;
import blog.aida.promotixproject.model.Store;
import blog.aida.promotixproject.model.User;
import blog.aida.promotixproject.util.DatePickerFragment;
import blog.aida.promotixproject.util.FontManager;

import static blog.aida.promotixproject.NearbyPromotionsDisplayActivity.ANONYMOUS;

public class AddPromotionActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{


    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = NearbyPromotionsDisplayActivity.class.getSimpleName();

    private FirebaseDatabase database;
    private DatabaseReference promotionsReference;
    private DatabaseReference storeReference;
    private ChildEventListener databasePromotionsEventListener;
    private ChildEventListener databaseStoresEventListener;

    //store
    private String storeAddress;
    private String storeName;
    private double storeLat;
    private double storeLng;
    private String storeId;
    private String storeCategories;

    //promotion
    private int promotionBadVotes;
    private int promotionGoodVotes;
    private User promotionAuthor;
    private String promotionDescription;
    private String promotionCuantum;
    private long promotionEndDate;
    private String promotionCategory;
    private String promotionUser;


    private String userName;


    private ArrayList<Store> stores = new ArrayList<Store>();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    private LatLng latLng;
    private Marker marker;
    Geocoder geocoder;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private List<AuthUI.IdpConfig> providersForSignIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userName = ANONYMOUS;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_promotion);

        database = FirebaseDatabase.getInstance();
        promotionsReference = database.getReference().child("promotions");
        storeReference = database.getReference().child("stores");




        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        firebaseAuth = FirebaseAuth.getInstance();

        setUpMapIfNeeded();

        TextView promotionEndDate = (TextView) findViewById(R.id.add_promotion_date_picker);
        promotionEndDate.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //user is signed in
                    onSingnedInInitialize(user.getDisplayName());
                } else {
                    //user is signed out

                    providersForSignIn.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
                    providersForSignIn.add(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
                    onSignedOutcleanup();

                }
            }

            ;
        };


    }
    //........................


    private void onSingnedInInitialize(String displayName) {
        if(displayName != null) {
            userName = displayName;
        } else {
            userName = "Hunter";
        }
        setMenuItemsVisibility(true);
        setHeaderMessage(userName);
    }

    private void setHeaderMessage(String displayName) {

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        TextView headerMenuUserNameView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.username_drawer_header);
        if(!displayName.equals(ANONYMOUS)) {
            headerMenuUserNameView.setVisibility(View.VISIBLE);
            headerMenuUserNameView.setText(displayName);
        } else {
            headerMenuUserNameView.setVisibility(View.GONE);
        }
    }

    private void setMenuItemsVisibility(boolean loggedIn) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        Menu menu = navigationView.getMenu();

        if(loggedIn) {
            menu.findItem(R.id.login_menu_item).setVisible(false);
            menu.findItem(R.id.logout_menu_item).setVisible(true);
        } else {
            menu.findItem(R.id.login_menu_item).setVisible(true);
            menu.findItem(R.id.logout_menu_item).setVisible(false);
        }
    }

    private void onSignedOutcleanup() {
        userName = ANONYMOUS;
        setMenuItemsVisibility(false);
        setHeaderMessage(ANONYMOUS);
    }

    public void addPromo(View view) {
        Store store = new Store();
        Promotion promotion = new Promotion();

        Spinner categorySpinner = (Spinner) findViewById(R.id.add_promotion_category_spinner);
        int selectedCategoryId = categorySpinner.getSelectedItemPosition();

       // Log.i("123cat", selectedCategoryId + "");
      /*  String category = store.getCategories();
        category="000000111";
        //
        getCategory(selectedCategoryId, category);
        */
        //store
        store.setAddress(storeAddress);
        store.setLat(storeLat);
        store.setLng(storeLng);
        store.setName(storeName);
        store.setId(storeId);
       // store.setCategories(storeCategories);

        //promotion
        EditText description = (EditText) findViewById(R.id.add_promotion_description);
        promotionDescription = description.getText().toString();


        EditText cuantum = (EditText) findViewById(R.id.add_promotion_cuantum);
        promotionCuantum = cuantum.getText().toString();



        promotionUser=firebaseAuth.getCurrentUser().getUid();

        promotion.setName(promotionDescription);
        promotion.setAuthor(promotionUser);
        promotion.setStoreId(storeId);
        promotion.setPromoEndDate(promotionEndDate);
        promotion.setCuantum(promotionCuantum);
        //promotion.setCategory(promotionCategory);
        promotion.setPromoEndDate(promotionEndDate);


        storeReference.push().setValue(store);
        promotionsReference.push().setValue(promotion);


        Log.i("DB push", "A pus");
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFrag.getMapAsync(this);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void handleNewLocation(Location location) {
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.setMyLocationEnabled(true);


        if (location != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }


    }



    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        @SuppressLint("MissingPermission") Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
        }
        else {
            handleNewLocation(location);
        }
    }


//-----------------




    public void showDatePickerDialog(View v) {
        final DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");

        final TextView dateTextView = (TextView) findViewById(R.id.add_promotion_show_date);
        TextWatcher watch = new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String endDate = dateTextView.getText().toString();


                DateFormat df = new SimpleDateFormat("dd-mm-yyyy");
                try {
                    Date parseDate;
                    parseDate = df.parse(endDate);
                    promotionEndDate = parseDate.getTime();

                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void afterTextChanged(Editable s) {



            }
        };
        dateTextView.addTextChangedListener(watch);



    }




    //............................................
    private void setUpMap() {

        /*if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    LocationService.MY_PERMISSION_ACCESS_COURSE_LOCATION );
        }
        mMap.setMyLocationEnabled(true);*/
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }
    //............................................



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
        mGoogleApiClient.connect();


    }

    private static final int PLACE_PICKER_REQUEST = 1;

    public void getPlaceFromDatePicker(View v) {

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {

            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            Log.i("Place Picker ",PlacePicker.getPlace(getBaseContext(),builder.build(this)).toString());
        } catch (Exception e){
            Log.w("exception",e.toString());
        }


    }

    public void getCategory(int position, String category){

        char x = category.charAt(position);
        if (!(x>'0')){
            StringBuilder cat = new StringBuilder(category);
            cat.setCharAt(position, '1');
        }
        Log.i("categ", category+"000" );

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                LatLng latlng = place.getLatLng();
                if (marker!=null) {
                    marker.remove();
                    marker = null;
                }
                marker = mMap.addMarker(new MarkerOptions().position(latlng).title(place.getName()+ "")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                TextView promotionAddress2 = (TextView) findViewById(R.id.add_promotion_address);
                promotionAddress2.setText(place.getName());
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();

                storeAddress = place.getAddress().toString();
                storeName = place.getName().toString();
                storeId = place.getId();
                LatLng latitudexlongitude = place.getLatLng();
                storeLat = latitudexlongitude.latitude;
                storeLng = latitudexlongitude.longitude;


            }
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

}
