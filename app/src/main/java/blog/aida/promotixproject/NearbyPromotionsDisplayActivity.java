package blog.aida.promotixproject;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.transition.ChangeBounds;
import android.transition.TransitionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import blog.aida.promotixproject.adapters.PromotionAdapter;
import blog.aida.promotixproject.model.Promotion;
import blog.aida.promotixproject.model.Store;

public class NearbyPromotionsDisplayActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private FirebaseDatabase database;
    private DatabaseReference promotionsReference;
    private DatabaseReference storeReference;

    Geocoder geocoder;
    private LatLng latLng;
    private Marker marker;

    private ListView promotionListView;
    private PromotionAdapter promotionAdapter;

    private ChildEventListener databasePromotionsEventListener;
    private ChildEventListener databaseStoresEventListener;

    private ArrayList<Store> stores = new ArrayList<Store>();

    private ArrayList<String> AssociatedArrayServicesCentres = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_promotions_display);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        database = FirebaseDatabase.getInstance();
        promotionsReference = database.getReference().child("promotions");
        storeReference = database.getReference().child("stores");

        attachPromotionsDatabaseReadListener();
        attachStoresDatabaseReadListener();

        // Initialize promotion ListView and its adapter
        List<Promotion> promotions = new ArrayList<>();
        promotionListView = (ListView) findViewById(R.id.promotions_list_view);

        promotionAdapter = new PromotionAdapter(this, R.layout.promotion_item, promotions);
        promotionListView.setAdapter(promotionAdapter);


    }

    private void attachPromotionsDatabaseReadListener() {
        if(databasePromotionsEventListener == null) {
            databasePromotionsEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Promotion promotion = dataSnapshot.getValue(Promotion.class);
                    promotionAdapter.add(promotion);
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };

            promotionsReference.addChildEventListener(databasePromotionsEventListener);
        }
    }

    private void attachStoresDatabaseReadListener() {
        if(databaseStoresEventListener == null) {
            databaseStoresEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Store store = dataSnapshot.getValue(Store.class);
                    stores.add(store);
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };

            storeReference.addChildEventListener(databaseStoresEventListener);
        }

        storeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(mMap != null){
                    addStoresOnMap();
                }
            }
            public void onCancelled(DatabaseError firebaseError) { }
        });
    }

    @TargetApi(19)
    public void expandListItem(View view) {

        // Initialize a new ChangeBounds transition instance
        ChangeBounds changeBounds = new ChangeBounds();

        // Set the transition start delay
        changeBounds.setStartDelay(300);

        // Set the transition interpolator
        changeBounds.setInterpolator(new AnticipateOvershootInterpolator());

        // Specify the transition duration
        changeBounds.setDuration(1000);

        // Begin the delayed transition
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.promotion_item);
        TransitionManager.beginDelayedTransition(linearLayout, changeBounds);

        // Toggle the button size
        toggleSize(view);
    }

    public void toggleSize(View view) {

        String isExpanded = "isExpanded";

        Log.i("clicked", " on item which is expanded " + view.getTag());

        if(!view.getTag().equals(isExpanded)) {
            view.getLayoutParams().height = (int)view.getLayoutParams().height + view.getLayoutParams().height*50/100;
            view.setTag("isExpanded");
        } else {
            view.getLayoutParams().height = (int)view.getLayoutParams().height * 2 / 3;
            view.setTag("isNotExpanded");
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

        // Add a marker in Sydney and move the camera


        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        addStoresOnMap();
    }

    public void addStoresOnMap() {

        if (stores != null && !stores.isEmpty()) {

            for (int i = 0; i < stores.size(); i++) {

               /* String[] latitudelongitude =  stores.get(i).getLatLng().split(",");
                double latitude = Double.parseDouble(latitudelongitude[0]);
                double longitude = Double.parseDouble(latitudelongitude[1]);
                LatLng location = new LatLng(latitude, longitude);

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .title(stores.get(i).getName()));
                            */
                LatLng location = new LatLng(stores.get(i).getLat(), stores.get(i).getLng());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(stores.get(i).getLat(), stores.get(i).getLng()))
                        .title(stores.get(i).getName()));
            }
           // Log.i("nearbypromotions","stors from db" + stores.get(1).getName()+ stores.get(1).getLat());
        }
    }
}