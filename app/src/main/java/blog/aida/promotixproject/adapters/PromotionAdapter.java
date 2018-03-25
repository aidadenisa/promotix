package blog.aida.promotixproject.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
import blog.aida.promotixproject.model.User;
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
    private DatabaseReference promotionReference;
    private DatabaseReference userReference;
    private String authorId;
    private Promotion promotion;
    private User loggedInUser;

    private TextView deleteItemIcon;

    public void setLoggedInUserId(String id) {
        authorId = id;
        this.notifyDataSetChanged();
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
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
                    PromotionAdapter.this.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    PromotionAdapter.this.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    PromotionAdapter.this.notifyDataSetChanged();
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

        promotionCheckedIcon.setTag( position);
        promotionFakeIcon.setTag(position);
        deleteItemIcon.setTag( position);

        promotionCheckedIcon.setTypeface(FontManager.getTypeface(getContext(),FontManager.FONTAWESOME));
        promotionFakeIcon.setTypeface(FontManager.getTypeface(getContext(),FontManager.FONTAWESOME));
        deleteItemIcon.setTypeface(FontManager.getTypeface(getContext(),FontManager.FONTAWESOME));

        if ((loggedInUser!= null && loggedInUser.getId().equals(promotion.getAuthor())) || (authorId!=null && authorId.equals(promotion.getAuthor()))) {
//            deleteItemIcon.setVisibility(View.VISIBLE);
        } else {
            deleteItemIcon.setVisibility(View.GONE);

        }

        if(loggedInUser!= null && loggedInUser.getLikedPromotions() != null && loggedInUser.getLikedPromotions().containsValue(promotion.getId())) {
            promotionCheckedIcon.setTextColor(GREEN);
        }

        if(loggedInUser!= null && loggedInUser.getDislikedPromotions() != null && loggedInUser.getDislikedPromotions().containsValue(promotion.getId())) {
            promotionFakeIcon.setTextColor(RED);
        }

        deleteItemIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (loggedInUser!= null && loggedInUser.getId().equals(promotion.getAuthor())) {
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

                Promotion clickedPromotion = getItem(Integer.parseInt(v.getTag().toString()));

                if(!isUserLoggedIn){
                    Toast.makeText(getContext(),"You need to be logged in in order to grade promotions.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(loggedInUser != null && clickedPromotion.getAuthor().equals(loggedInUser.getId())){
                    Toast.makeText(getContext(),"You can't grade your promotions.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String,String> likedPromotions = loggedInUser.getLikedPromotions();

                if(likedPromotions.size() == 0) {

                    //DELETE FROM DISLIKED PROMOTIONS IF NEEDED
                    if(!loggedInUser.getDislikedPromotions().isEmpty()
                            && loggedInUser.getDislikedPromotions().containsValue(clickedPromotion.getId())
                            && clickedPromotion.getBadVotes() > 0) {
                        removeFromDislikedPromotions(clickedPromotion, v, loggedInUser.getLikedPromotions());
                    }
                    //////

                    userReference = database.getReference().child("users").child(loggedInUser.getDatabaseReferenceId());
                    String key = userReference.child("likedPromotions").push().getKey();
                    userReference.child("likedPromotions").child(key).setValue(clickedPromotion.getId());

                    likedPromotions.put(key, clickedPromotion.getId());

                    ((TextView)v).setTextColor(GREEN);

                    int goodVotes = clickedPromotion.getGoodVotes();
                    goodVotes ++;
                    clickedPromotion.setGoodVotes(goodVotes);
                    if(clickedPromotion.getId() != null) {
                        promotionReference = database.getReference().child("promotions").child(clickedPromotion.getId());
                        promotionReference.child("goodVotes").setValue(goodVotes);
                    }

                } else {
                    if(likedPromotions.containsValue(clickedPromotion.getId())) {

                        //has been voted => substract the vote

                        String key = new String();

                        for (Map.Entry<String, String> entry : likedPromotions.entrySet()) {
                            if (entry.getValue().equals(clickedPromotion.getId())) {
                                key = entry.getKey();

                                userReference = database.getReference().child("users").child(loggedInUser.getDatabaseReferenceId());
                                userReference.child("likedPromotions").child(key).removeValue();
                                break;
                            }
                        }

                        ((TextView)v).setTextColor(Color.parseColor("#757575"));

                        int goodVotes = clickedPromotion.getGoodVotes();
                        goodVotes --;
                        clickedPromotion.setGoodVotes(goodVotes);

                        if(clickedPromotion.getId() != null) {
                            promotionReference = database.getReference().child("promotions").child(clickedPromotion.getId());
                            promotionReference.child("goodVotes").setValue(goodVotes);
                        }

                        likedPromotions.remove(key);

                    } else {

                        //DELETE FROM DISLIKED PROMOTIONS IF NEEDED
                        if(!loggedInUser.getDislikedPromotions().isEmpty()
                                && loggedInUser.getDislikedPromotions().containsValue(clickedPromotion.getId())
                                && clickedPromotion.getBadVotes() > 0) {
                            removeFromDislikedPromotions(clickedPromotion, v, loggedInUser.getDislikedPromotions());
                        }
                        //////

                        //hasn t been voted => vote for it
                        userReference = database.getReference().child("users").child(loggedInUser.getDatabaseReferenceId());
                        String key = userReference.child("likedPromotions").push().getKey();
                        userReference.child("likedPromotions").child(key).setValue(clickedPromotion.getId());

                        likedPromotions.put(key, clickedPromotion.getId());

                        ((TextView)v).setTextColor(GREEN);

                        int goodVotes = clickedPromotion.getGoodVotes();
                        goodVotes ++;
                        clickedPromotion.setGoodVotes(goodVotes);
                        if(clickedPromotion.getId() != null) {
                            promotionReference = database.getReference().child("promotions").child(clickedPromotion.getId());
                            promotionReference.child("goodVotes").setValue(goodVotes);
                        }
                    }
                }

                loggedInUser.setLikedPromotions(likedPromotions);

                PromotionAdapter.this.notifyDataSetChanged();
            }

            private void removeFromDislikedPromotions(Promotion clickedPromotion, View v, Map<String,String> dislikedPromotions) {
                String key = new String();

                for (Map.Entry<String, String> entry : dislikedPromotions.entrySet()) {
                    if (entry.getValue().equals(clickedPromotion.getId())) {
                        key = entry.getKey();

                        userReference = database.getReference().child("users").child(loggedInUser.getDatabaseReferenceId());
                        userReference.child("dislikedPromotions").child(key).removeValue();
                        break;
                    }
                }

                ((TextView) ((ViewGroup)v.getParent()).getChildAt(3)).setTextColor(Color.parseColor("#757575"));

                int badVotes = clickedPromotion.getBadVotes();
                badVotes --;
                clickedPromotion.setBadVotes(badVotes);

                if(clickedPromotion.getId() != null) {
                    promotionReference = database.getReference().child("promotions").child(clickedPromotion.getId());
                    promotionReference.child("badVotes").setValue(badVotes);
                }

                dislikedPromotions.remove(key);
            }
        });

        promotionFakeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Promotion clickedPromotion = getItem(Integer.parseInt(v.getTag().toString()));

                if(!isUserLoggedIn){
                    Toast.makeText(getContext(),"You need to be logged in in order to grade promotions.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(clickedPromotion.getAuthor().equals(loggedInUser.getId())){
                    Toast.makeText(getContext(),"You can't grade your promotions.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String,String> dislikedPromotions = loggedInUser.getDislikedPromotions();

                if(dislikedPromotions.size() == 0) {

                    //DELETE FROM LIKED PROMOTIONS IF NEEDED
                    if(!loggedInUser.getLikedPromotions().isEmpty()
                            && loggedInUser.getLikedPromotions().containsValue(clickedPromotion.getId())
                            && clickedPromotion.getGoodVotes() > 0) {
                        removeFromLikedPromotions(clickedPromotion, v, loggedInUser.getLikedPromotions());
                    }
                    //////

                    userReference = database.getReference().child("users").child(loggedInUser.getDatabaseReferenceId());
                    String key = userReference.child("dislikedPromotions").push().getKey();
                    userReference.child("dislikedPromotions").child(key).setValue(clickedPromotion.getId());

                    dislikedPromotions.put(key, clickedPromotion.getId());

                    ((TextView)v).setTextColor(RED);

                    int badVotes = clickedPromotion.getBadVotes();
                    badVotes ++;
                    clickedPromotion.setBadVotes(badVotes);
                    if(clickedPromotion.getId() != null) {
                        promotionReference = database.getReference().child("promotions").child(clickedPromotion.getId());
                        promotionReference.child("badVotes").setValue(badVotes);
                    }

                } else {
                    if(dislikedPromotions.containsValue(clickedPromotion.getId())) {

                        //has been voted => substract the vote

                        String key = new String();

                        for (Map.Entry<String, String> entry : dislikedPromotions.entrySet()) {
                            if (entry.getValue().equals(clickedPromotion.getId())) {
                                key = entry.getKey();

                                userReference = database.getReference().child("users").child(loggedInUser.getDatabaseReferenceId());
                                userReference.child("dislikedPromotions").child(key).removeValue();
                                break;
                            }
                        }

                        ((TextView)v).setTextColor(Color.parseColor("#757575"));

                        int badVotes = clickedPromotion.getBadVotes();
                        badVotes --;
                        clickedPromotion.setBadVotes(badVotes);

                        if(clickedPromotion.getId() != null) {
                            promotionReference = database.getReference().child("promotions").child(clickedPromotion.getId());
                            promotionReference.child("badVotes").setValue(badVotes);
                        }

                        dislikedPromotions.remove(key);

                    } else {

                        //hasn t been voted => vote for it

                        //DELETE FROM LIKED PROMOTIONS IF NEEDED
                        if(!loggedInUser.getLikedPromotions().isEmpty()
                                && loggedInUser.getLikedPromotions().containsValue(clickedPromotion.getId())
                                && clickedPromotion.getGoodVotes() > 0) {
                            removeFromLikedPromotions(clickedPromotion, v, loggedInUser.getLikedPromotions());
                        }
                        //////

                        userReference = database.getReference().child("users").child(loggedInUser.getDatabaseReferenceId());
                        String key = userReference.child("dislikedPromotions").push().getKey();
                        userReference.child("dislikedPromotions").child(key).setValue(clickedPromotion.getId());

                        dislikedPromotions.put(key, clickedPromotion.getId());

                        ((TextView)v).setTextColor(RED);

                        int badVotes = clickedPromotion.getBadVotes();
                        badVotes ++;
                        clickedPromotion.setBadVotes(badVotes);
                        if(clickedPromotion.getId() != null) {
                            promotionReference = database.getReference().child("promotions").child(clickedPromotion.getId());
                            promotionReference.child("badVotes").setValue(badVotes);
                        }
                    }
                }

                loggedInUser.setDislikedPromotions(dislikedPromotions);

                PromotionAdapter.this.notifyDataSetChanged();
            }

            private void removeFromLikedPromotions(Promotion clickedPromotion, View v, Map<String,String> likedPromotions) {
                String key = new String();

                for (Map.Entry<String, String> entry : likedPromotions.entrySet()) {
                    if (entry.getValue().equals(clickedPromotion.getId())) {
                        key = entry.getKey();

                        userReference = database.getReference().child("users").child(loggedInUser.getDatabaseReferenceId());
                        userReference.child("likedPromotions").child(key).removeValue();
                        break;
                    }
                }

                ((TextView) ((ViewGroup)v.getParent()).getChildAt(1)).setTextColor(Color.parseColor("#757575"));

                int goodVotes = clickedPromotion.getGoodVotes();
                goodVotes --;
                clickedPromotion.setGoodVotes(goodVotes);

                if(clickedPromotion.getId() != null) {
                    promotionReference = database.getReference().child("promotions").child(clickedPromotion.getId());
                    promotionReference.child("goodVotes").setValue(goodVotes);
                }

                likedPromotions.remove(key);
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

