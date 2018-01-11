package blog.aida.promotixproject.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blog.aida.promotixproject.R;
import blog.aida.promotixproject.model.Promotion;
import blog.aida.promotixproject.model.Store;
import blog.aida.promotixproject.util.FontManager;

/**
 * Created by aida on 06-Jan-18.
 */

public class PromotionAdapter extends ArrayAdapter<Promotion> {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private ValueEventListener databaseEventListener;
    private Store store;
    private Promotion promotion;
    private boolean isUserLoggedIn = false;
    private String loggedInUserId;
    private DatabaseReference promotionReference;
    private String author;
    private String storeName;

    public void setStoreDetailsFromDB(Store store) {
        this.store = store;
        this.notifyDataSetChanged();
    }

    public void setLoggedInUserId(String loggedInUserId){

        this.loggedInUserId = loggedInUserId;
        this.notifyDataSetChanged();
    }


    public PromotionAdapter(Context context, int resource, List<Promotion> objects) {
        super(context,resource,objects);
    }

    private void attachDatabaseReadListener(final PromotionAdapter self, DatabaseReference storeReference) {
        if (databaseEventListener == null) {
            databaseEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Store store = dataSnapshot.getValue(Store.class);
                    self.setStoreDetailsFromDB(store);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };

            storeReference.addValueEventListener(databaseEventListener);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.promotion_item, parent, false);
        }

        final TextView promotionName = (TextView) convertView.findViewById(R.id.promotion_name);
        final TextView promotionShopName = (TextView) convertView.findViewById(R.id.promotion_shop_name);
        final TextView promotionShopAddress = (TextView) convertView.findViewById(R.id.promotion_shop_address);
        final TextView promotionCheckedVotes = (TextView) convertView.findViewById(R.id.promotion_checked_votes);
        final TextView promotionCheckedIcon = (TextView) convertView.findViewById(R.id.promotion_checked_icon);
        final TextView promotionFakeVotes = (TextView) convertView.findViewById(R.id.promotion_fake_votes);
        TextView promotionFakeIcon = (TextView) convertView.findViewById(R.id.promotion_fake_icon);
        TextView deleteItemIcon = (TextView) convertView.findViewById(R.id.delete_item_icon);

        promotionCheckedIcon.setTypeface(FontManager.getTypeface(getContext(),FontManager.FONTAWESOME));
        promotionFakeIcon.setTypeface(FontManager.getTypeface(getContext(),FontManager.FONTAWESOME));
        deleteItemIcon.setTypeface(FontManager.getTypeface(getContext(),FontManager.FONTAWESOME));

        final Promotion promotion = getItem(position);

        deleteItemIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PromotionAdapter.this.remove(getItem(position));
                            PromotionAdapter.this.notifyDataSetChanged();

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
                int goodVotes = promotion.getGoodVotes();
                goodVotes ++;
                promotion.setGoodVotes(goodVotes);
                if(promotion.getUniqueId() != null) {
                    promotionReference = database.getReference().child("promotions").child(promotion.getUniqueId());
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
                promotion.setBadVotes(badVotes);
                if(promotion.getUniqueId() != null) {
                    promotionReference = database.getReference().child("promotions").child(promotion.getUniqueId());
                    promotionReference.child("badVotes").setValue(badVotes);
                }
            }
        });


        DatabaseReference storeReference = database.getReference().child("stores").child(promotion.getStoreId());

        attachDatabaseReadListener(this, storeReference);




        if(store != null) {
            promotionShopName.setText(store.getName());
            promotionShopAddress.setText(store.getAddress());
        }

        if(promotion != null) {
            promotionName.setText(promotion.getName());
            promotionCheckedVotes.setText(promotion.getGoodVotes() + "");
            promotionFakeVotes.setText(promotion.getBadVotes() + "");
        }


        return convertView;
    }

    public void setUserLoggedIn(boolean isUserLoggedIn){
        this.isUserLoggedIn = isUserLoggedIn;
    }

}

