package blog.aida.promotixproject;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

public class NearbyPromotionsDisplayActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseDatabase database;
    private DatabaseReference promotionsReference;
    private DatabaseReference storeReference;

    private ListView promotionListView;
    private PromotionAdapter promotionAdapter;

    private ChildEventListener databasePromotionsEventListener;
    private ChildEventListener databaseStoresEventListener;

    private ArrayList<Store> stores = new ArrayList<Store>();


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

    public void addStoresOnMap(){
        //mMap
               // stores//for pe stores
        if(stores != null && !stores.isEmpty()){
//            Log.i("nearbypromotions","stors from db" + stores.get(0).getName());.


        }

    }
}
