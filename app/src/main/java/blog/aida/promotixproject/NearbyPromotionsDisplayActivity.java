package blog.aida.promotixproject;

import android.content.Intent;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import blog.aida.promotixproject.adapters.PromotionAdapter;
import blog.aida.promotixproject.model.Promotion;
import blog.aida.promotixproject.model.Store;
import blog.aida.promotixproject.model.User;
import blog.aida.promotixproject.util.FontManager;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.firebase.ui.auth.AuthUI;

public class NearbyPromotionsDisplayActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = NearbyPromotionsDisplayActivity.class.getSimpleName();

    public static final String ANONYMOUS = "anonymousUSER";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int SIGN_IN_REQUEST = 1000;

    private LocationRequest mLocationRequest;

    private String userName;
    private int badVotes;
    private int goodVotes;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private List<AuthUI.IdpConfig> providersForSignIn;

    private FirebaseDatabase database;
    private DatabaseReference promotionsReference;
    private DatabaseReference storeReference;
    private DatabaseReference userReference;

    private ListView promotionListView;
    private PromotionAdapter promotionAdapter;
    private PromotionAdapter promotionAdapter2;

    private ChildEventListener databasePromotionsEventListener;
    private ChildEventListener databaseStoresEventListener;
    private ChildEventListener databaseUsersEventListener;

    private ArrayList<Store> stores = new ArrayList<Store>();
    private User loggedUser;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    private ArrayList<Promotion> pPromotions = new ArrayList<Promotion>();
    private ArrayList<Marker> markers = new ArrayList<Marker>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userName = ANONYMOUS;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        setContentView(R.layout.activity_nearby_promotions_display);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

        setUpMapIfNeeded();


        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        promotionsReference = database.getReference().child("promotions");
        storeReference = database.getReference().child("stores");
        userReference = database.getReference().child("users");

        attachPromotionsDatabaseReadListener();
        attachStoresDatabaseReadListener();
//        attachUsersDatabaseReadListener();

        // Initialize promotion ListView and its adapter
        List<Promotion> promotions = new ArrayList<>();
        promotionListView = (ListView) findViewById(R.id.promotions_list_view);

        promotionAdapter = new PromotionAdapter(this, R.layout.promotion_item, promotions);
        promotionListView.setAdapter(promotionAdapter);

        Button addPromotionButton = (Button) findViewById(R.id.add_promotion_button);
        addPromotionButton.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));

        Button backToAllPromotionsButton = (Button) findViewById(R.id.add_back_button);
        backToAllPromotionsButton.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.closed);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }


        providersForSignIn = new ArrayList<>();

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

        if(firebaseAuth.getCurrentUser() != null){
            promotionAdapter.setUserLoggedIn(true);
//            promotionAdapter.setLoggedInUserId(firebaseAuth.getCurrentUser().getUid());
            promotionAdapter.setLoggedInUser(loggedUser);
        }
    }


    //cand m-am logat
    private void onSingnedInInitialize(String displayName) {
        if(displayName != null) {
            userName = displayName;
        } else {
            userName = "Hunter";
        }

        attachUsersDatabaseReadListener();

        setMenuItemsVisibility(true);
        setHeaderMessage(userName);
        promotionAdapter.setUserLoggedIn(true);
//        promotionAdapter.setLoggedInUserId(firebaseAuth.getUid());


    }

    //cand m-am delogat
    private void onSignedOutcleanup() {
        userName = ANONYMOUS;
        setMenuItemsVisibility(false);
        setHeaderMessage(ANONYMOUS);
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
            menu.findItem(R.id.user_promotions_menu_item).setVisible(true);
        } else {
            menu.findItem(R.id.login_menu_item).setVisible(true);
            menu.findItem(R.id.logout_menu_item).setVisible(false);
            menu.findItem(R.id.user_promotions_menu_item).setVisible(false);
        }
    }

    public void openViewMyPromotionsActivity() {
        Intent goToViewMyPromotionsActivity = new Intent(this, UserPromotionsActivity.class);
        startActivity(goToViewMyPromotionsActivity);
    }

    public void backToPromotionsActivity(View v) {
        promotionListView.setAdapter(promotionAdapter);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        drawerLayout.closeDrawers();

        switch(id) {
            case R.id.login_menu_item:
                onSignedOutcleanup();
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .setProviders(providersForSignIn)
                                .build(),
                        SIGN_IN_REQUEST);
                break;

            case R.id.logout_menu_item:
                AuthUI.getInstance().signOut(this);
                onSignedOutcleanup();
                Toast.makeText(this, "You have been signed out", Toast.LENGTH_SHORT);
                return true;

            case R.id.user_promotions_menu_item:

                openViewMyPromotionsActivity();

            case R.id.about_menu_item:
                break;

            default:
                return true;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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


//    @Override
//    protected void onResume() {
//        super.onResume();
//        setUpMapIfNeeded();
//        mGoogleApiClient.connect();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (mGoogleApiClient.isConnected()) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
//            mGoogleApiClient.disconnect();
//        }
//    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }


    private void attachPromotionsDatabaseReadListener() {
        if (databasePromotionsEventListener == null) {
            databasePromotionsEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Promotion promotion = dataSnapshot.getValue(Promotion.class);
                    promotion.setId(dataSnapshot.getKey());
                    pPromotions.add(promotion);
                    promotionAdapter.add(promotion);
                }



                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    promotionAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    promotionAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    promotionAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };


           // pPromotions.size();
            promotionsReference.addChildEventListener(databasePromotionsEventListener);
        }
    }

    private void attachStoresDatabaseReadListener() {
        if (databaseStoresEventListener == null) {
            databaseStoresEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Store store = dataSnapshot.getValue(Store.class);
                    stores.add(store);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    promotionAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    promotionAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    promotionAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };

            storeReference.addChildEventListener(databaseStoresEventListener);

        }

        storeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mMap != null) {
                    addStoresOnMap();
                }
            }

            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    private void attachUsersDatabaseReadListener() {
        if (databaseUsersEventListener == null) {
            databaseUsersEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    User user = dataSnapshot.getValue(User.class);
                    if( user != null && user.getId() != null && user.getId().equals(firebaseAuth.getCurrentUser().getUid())) {
                        loggedUser = user;
                        loggedUser.setDatabaseReferenceId(dataSnapshot.getKey());
                        promotionAdapter.setLoggedInUser(loggedUser);
                        return;
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    promotionAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    promotionAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    promotionAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };



            userReference.addChildEventListener(databaseUsersEventListener);

            userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(loggedUser == null) {
                        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                        User newUser = new User();

                        newUser.setId(currentUser.getUid());
                        newUser.setBlocked(false);
                        newUser.setEmail(currentUser.getEmail());
                        newUser.setFirstName(currentUser.getDisplayName());

                        promotionAdapter.setLoggedInUser(newUser);

                        String key = userReference.push().getKey();

                        userReference.push().child(key).setValue(newUser);

                        loggedUser = newUser;
                        loggedUser.setDatabaseReferenceId(key);
                    }



                }
                public void onCancelled(DatabaseError firebaseError) { }
            });
        }
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
        mGoogleApiClient.connect();

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
                    .zoom(14)                   // Sets the zoom
                    //.bearing(90)                // Sets the orientation of the camera to east
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
            try{
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
            } catch(Exception e) {
                Toast.makeText(this,R.string.location_service_error, Toast.LENGTH_SHORT).show();

            }
        }
        else {
            handleNewLocation(location);
        }
    }

    public void addStoresOnMap() {
        if (mMap != null) { //prevent crashing if the map doesn't exist yet (eg. on starting activity)
            mMap.clear();

            ArrayList<Promotion> promotions = new ArrayList<>();
            promotionAdapter2 = new PromotionAdapter(this, R.layout.promotion_item, promotions);

            if (stores != null && !stores.isEmpty()) {

                //for (int i=0;i<pPromotions.size)
                mMap.setOnMarkerClickListener(this);
                for (int i = 0; i < stores.size(); i++) {

                        for (int k = 0; k < pPromotions.size(); k++) {
                            if ((pPromotions.get(k).getPlaceId()).equalsIgnoreCase(stores.get(i).getId())) {
                                Promotion promo = pPromotions.get(k);
                                promotionAdapter2.add(promo);
                            }

                        }
                    if (!promotionAdapter2.isEmpty()) {
                        Marker mMarker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(stores.get(i).getLat(), stores.get(i).getLng()))
                                .title(stores.get(i).getName()));

                        if ((mMarker.getTitle()).equalsIgnoreCase(stores.get(i).getName())) {
                            promotionAdapter2.clear();
                            promotionAdapter2.notifyDataSetChanged();
                        }

                    }

                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    public void openAddPromotionActivity(View view) {
        if (userName!=null && !userName.equals(ANONYMOUS)) {
            Intent goToAddPromotionActivity = new Intent(this, AddPromotionActivity.class);
            startActivity(goToAddPromotionActivity);
        }
        else{
            Toast.makeText(this, "You have to login to add a promotion.",Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public boolean onMarkerClick(Marker marker) {
        ArrayList<Promotion> promotions = new ArrayList<>();
        promotionAdapter2 = new PromotionAdapter(this, R.layout.promotion_item, promotions);
        promotionListView.setAdapter(null);
        promotionListView.setAdapter(promotionAdapter2);

        int j,k;
            for (j=0;j<stores.size();j++){

                promotionListView.setAdapter(promotionAdapter2);
                if ((marker.getTitle()).equalsIgnoreCase(stores.get(j).getName())){
                    promotionAdapter2.clear();
                    promotionAdapter2.notifyDataSetChanged();
                    for (k=0;k<pPromotions.size();k++){
                        if((pPromotions.get(k).getPlaceId()).equalsIgnoreCase(stores.get(j).getId())){
                            Promotion promo = pPromotions.get(k);
                            promotionAdapter2.add(promo);
                        }
                    }

                }
            }
        return true;

    }
}