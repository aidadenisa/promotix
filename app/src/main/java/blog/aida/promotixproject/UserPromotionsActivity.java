package blog.aida.promotixproject;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import blog.aida.promotixproject.adapters.PromotionAdapter;
import blog.aida.promotixproject.model.Promotion;
import blog.aida.promotixproject.model.User;

public class UserPromotionsActivity extends AppCompatActivity {

    public static final String ANONYMOUS = "anonymousUSER";
    private final static int SIGN_IN_REQUEST = 1000;

    private FirebaseDatabase database;
    private DatabaseReference promotionsReference;
    private DatabaseReference storeReference;
    private DatabaseReference userReference;

    private ListView promotionListView;
    private PromotionAdapter promotionAdapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private List<AuthUI.IdpConfig> providersForSignIn;

    private ChildEventListener databasePromotionsEventListener;
    private ChildEventListener databaseStoresEventListener;
    private ChildEventListener databaseUsersEventListener;

    private String userName;
    private User loggedUser;

    private void attachPromotionsDatabaseReadListener() {
        if (databasePromotionsEventListener == null) {
            databasePromotionsEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Promotion promotion = dataSnapshot.getValue(Promotion.class);
                    if (firebaseAuth.getCurrentUser().getUid().equals(promotion.getAuthor())) {
                        promotion.setId(dataSnapshot.getKey());
                        promotionAdapter.add(promotion);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    promotionAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };

            promotionsReference.addChildEventListener(databasePromotionsEventListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_promotions);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

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
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(providersForSignIn)
                                    .build(),
                            SIGN_IN_REQUEST);

                }
            }
        };

//        if(firebaseAuth.getCurrentUser() != null){
//            promotionAdapter.setUserLoggedIn(true);
//            promotionAdapter.setLoggedInUserId(firebaseAuth.getCurrentUser().getUid());
//        }



        // Initialize promotion ListView and its adapter
        List<Promotion> promotions = new ArrayList<>();
        promotionListView = (ListView) findViewById(R.id.user_promotions_list_view);

        promotionsReference = database.getReference().child("promotions");
        userReference = database.getReference().child("users");

        attachPromotionsDatabaseReadListener();

        promotionAdapter = new PromotionAdapter(this, R.layout.promotion_item, promotions);
        promotionAdapter.setUserLoggedIn(true);
//        promotionAdapter.setLoggedInUserId(firebaseAuth.getUid());
        promotionListView.setAdapter(promotionAdapter);


    }

    //cand m-am logat
    private void onSingnedInInitialize(String displayName) {
        if(displayName != null) {
            userName = displayName;
        } else {
            userName = "Hunter";
        }

        attachUsersDatabaseReadListener();

        promotionAdapter.setUserLoggedIn(true);
//        promotionAdapter.setLoggedInUserId(firebaseAuth.getUid());
        promotionAdapter.setLoggedInUser(loggedUser);
    }

    //cand m-am delogat
    private void onSignedOutcleanup() {
        userName = ANONYMOUS;
    }

    private void attachUsersDatabaseReadListener() {
        if (databaseUsersEventListener == null) {
            databaseUsersEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    User user = dataSnapshot.getValue(User.class);
                    if(user.getId().equals(firebaseAuth.getCurrentUser().getUid())) {
                        loggedUser = user;
                        promotionAdapter.setLoggedInUser(loggedUser);
                        return;
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
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

                        userReference.push().setValue(newUser);
                    }

                }
                public void onCancelled(DatabaseError firebaseError) { }
            });
        }
    }
}
