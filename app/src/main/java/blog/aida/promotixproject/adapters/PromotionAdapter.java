package blog.aida.promotixproject.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blog.aida.promotixproject.NearbyPromotionsDisplayActivity;
import blog.aida.promotixproject.R;
import blog.aida.promotixproject.model.Promotion;
import blog.aida.promotixproject.model.Store;
import blog.aida.promotixproject.util.FontManager;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;

/**
 * Created by aida on 06-Jan-18.
 */

public class PromotionAdapter extends ArrayAdapter<Promotion> {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private ChildEventListener databaseEventListener;
    private ArrayList<Store> stores = new ArrayList<>();
    private boolean isUserLoggedIn = false;
    private String loggedInUserId;
    private DatabaseReference promotionReference;
    private String author;
    private Promotion promotion;

    private TextView deleteItemIcon;

    public void setLoggedInUserId(String loggedInUserId){

        this.loggedInUserId = loggedInUserId;
        this.notifyDataSetChanged();
    }

    public PromotionAdapter(Context context, int resource, List<Promotion> objects) {
        super(context,resource,objects);
    }

    private void attachDatabaseReadListener(final PromotionAdapter self, DatabaseReference storeReference) {
        if (databaseEventListener == null) {
            databaseEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Store store = dataSnapshot.getValue(Store.class);

                    if(store.getId().equals(promotion.getPlaceId())) {
                        promotion.setStoreId(dataSnapshot.getKey());
                        promotionReference = database.getReference().child("promotions").child(promotion.getId());
                        promotionReference.child("storeId").setValue(dataSnapshot.getKey());

                    }

                    stores.add(store);

                    PromotionAdapter.this.notifyDataSetChanged();

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

//                    PromotionAdapter.this.notifyDataSetChanged();

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

            storeReference.addChildEventListener(databaseEventListener);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.promotion_item, parent, false);
        }

        promotion = getItem(position);

        final TextView promotionName = (TextView) convertView.findViewById(R.id.promotion_name);
        final TextView promotionCheckedVotes = (TextView) convertView.findViewById(R.id.promotion_checked_votes);
        final TextView promotionCheckedIcon = (TextView) convertView.findViewById(R.id.promotion_checked_icon);
        final TextView promotionFakeVotes = (TextView) convertView.findViewById(R.id.promotion_fake_votes);
        TextView promotionFakeIcon = (TextView) convertView.findViewById(R.id.promotion_fake_icon);
        TextView promotionCuantum = convertView.findViewById(R.id.promotion_cuantum);
        TextView promotionShopName = (TextView) convertView.findViewById(R.id.promotion_shop_name);
        TextView promotionShopAddress = (TextView) convertView.findViewById(R.id.promotion_shop_address);

        deleteItemIcon = (TextView) convertView.findViewById(R.id.delete_item_icon);

        promotionCheckedIcon.setTypeface(FontManager.getTypeface(getContext(),FontManager.FONTAWESOME));
        promotionFakeIcon.setTypeface(FontManager.getTypeface(getContext(),FontManager.FONTAWESOME));
        deleteItemIcon.setTypeface(FontManager.getTypeface(getContext(),FontManager.FONTAWESOME));

        if (loggedInUserId!= null && loggedInUserId.equals(promotion.getAuthor())) {
            deleteItemIcon.setVisibility(View.VISIBLE);
        } else {
            deleteItemIcon.setVisibility(View.GONE);
        }

        deleteItemIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (loggedInUserId.equals(promotion.getAuthor())) {
                                PromotionAdapter.this.remove(getItem(position));
                                promotionReference = database.getReference().child("promotions").child(promotion.getId());
                                promotionReference.removeValue();
                                Toast.makeText(getContext(),"The promotion has been deleted",Toast.LENGTH_SHORT).show();
                                PromotionAdapter.this.notifyDataSetChanged();

                            } else {
                                v.setVisibility(View.GONE);
                            }
                        }
                    });


        promotionCheckedIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PromotionAdapter.this.notifyDataSetChanged();

                if(!isUserLoggedIn){
                    Toast.makeText(getContext(),"You need to be logged in in order to grade promotions.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(promotion.getAuthor().equals(loggedInUserId)){
                    Toast.makeText(getContext(),"You can't grade your promotions.", Toast.LENGTH_SHORT).show();
                    return;
                }

                ((TextView)v).setTextColor(GREEN);

                int goodVotes = promotion.getGoodVotes();
                goodVotes ++;
                promotion.setGoodVotes(goodVotes);
                v.setTag("voted");
                if(promotion.getId() != null) {
                    promotionReference = database.getReference().child("promotions").child(promotion.getId());
                    promotionReference.child("goodVotes").setValue(goodVotes);
                }
            }
        });

        promotionFakeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PromotionAdapter.this.notifyDataSetChanged();

                if(!isUserLoggedIn){
                    Toast.makeText(getContext(),"You need to be logged in in order to grade promotions.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(promotion.getAuthor().equals(loggedInUserId)){
                    Toast.makeText(getContext(),"You can't grade your promotions.", Toast.LENGTH_SHORT).show();
                    return;
                }
                int badVotes = promotion.getBadVotes();
                badVotes ++;
                ((TextView)v).setTextColor(RED);
                promotion.setBadVotes(badVotes);
                if(promotion.getId() != null) {
                    promotionReference = database.getReference().child("promotions").child(promotion.getId());
                    promotionReference.child("badVotes").setValue(badVotes);
                }
            }
        });



        DatabaseReference storeReference = database.getReference().child("stores");
        attachDatabaseReadListener(this, storeReference);


        if(promotion != null) {
            promotionName.setText(promotion.getName());
            promotionCheckedVotes.setText(promotion.getGoodVotes() + "");
            promotionFakeVotes.setText(promotion.getBadVotes() + "");
            promotionCuantum.setText(promotion.getCuantum());

            if(stores != null && !stores.isEmpty()) {

                for(int i=0; i< stores.size();i++ ) {
                    Store store = stores.get(i);
                    if(promotion.getPlaceId().equals(store.getId())) {
                        promotionShopAddress.setText(store.getAddress());
                        promotionShopName.setText(store.getName());
                    }
                }

            }


        }


        return convertView;
    }

    public void setUserLoggedIn(boolean isUserLoggedIn){
        this.isUserLoggedIn = isUserLoggedIn;
    }

}

