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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import blog.aida.promotixproject.adapters.PromotionAdapter;
import blog.aida.promotixproject.model.Promotion;

public class UserPromotionsActivity extends AppCompatActivity {

    public static final String ANONYMOUS = "anonymousUSER";
    private final static int SIGN_IN_REQUEST = 1000;

    private FirebaseDatabase database;
    private DatabaseReference promotionsReference;
    private DatabaseReference storeReference;

    private ListView promotionListView;
    private PromotionAdapter promotionAdapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private List<AuthUI.IdpConfig> providersForSignIn;

    private ChildEventListener databasePromotionsEventListener;
    private ChildEventListener databaseStoresEventListener;

    private String userName;

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


        // Initialize promotion ListView and its adapter
        List<Promotion> promotions = new ArrayList<>();
        promotionListView = (ListView) findViewById(R.id.user_promotions_list_view);
        Promotion p1 = new Promotion();
        p1.setAuthor(firebaseAuth.getCurrentUser().getUid());
        p1.setName("Promotie la pantofi");
        p1.setPromoEndDate((new Date(2018,11,12)).getTime());
        p1.setStoreId("s1");

        Promotion p2 = new Promotion();
        p2.setAuthor(firebaseAuth.getCurrentUser().getUid());
        p2.setName("Promotie la lemne");
        p2.setPromoEndDate((new Date(2018,12,12)).getTime());
        p2.setStoreId("s1");

        promotions.add(p1);
        promotions.add(p2);

        promotionAdapter = new PromotionAdapter(this, R.layout.promotion_item, promotions);
        promotionListView.setAdapter(promotionAdapter);

    }

    //cand m-am logat
    private void onSingnedInInitialize(String displayName) {
        if(displayName != null) {
            userName = displayName;
        } else {
            userName = "Hunter";
        }

        promotionAdapter.setUserLoggedIn(true);
        promotionAdapter.setLoggedInUserId(firebaseAuth.getUid());
    }

    //cand m-am delogat
    private void onSignedOutcleanup() {
        userName = ANONYMOUS;
    }
}
