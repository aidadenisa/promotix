package blog.aida.promotixproject.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import blog.aida.promotixproject.R;
import blog.aida.promotixproject.model.Promotion;
import blog.aida.promotixproject.model.Store;

/**
 * Created by aida on 06-Jan-18.
 */

public class PromotionAdapter extends ArrayAdapter<Promotion> {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private ValueEventListener databaseEventListener;
    private Store store;
    private Promotion promotion;

    public void setStoreDetailsFromDB(Store store) {
        this.store = store;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.promotion_item, parent, false);
        }

        TextView promotionName = (TextView) convertView.findViewById(R.id.promotion_name);
        TextView promotionShopName = (TextView) convertView.findViewById(R.id.promotion_shop_name);
        TextView promotionShopAddress = (TextView) convertView.findViewById(R.id.promotion_shop_address);
        TextView promotionCheckedVotes = (TextView) convertView.findViewById(R.id.promotion_checked_votes);
        TextView promotionFakeVotes = (TextView) convertView.findViewById(R.id.promotion_fake_votes);

        Promotion promotion = getItem(position);

        DatabaseReference storeReference = database.getReference().child("stores").child(promotion.getStoreId());

        attachDatabaseReadListener(this, storeReference);

        if(store != null) {
            promotionShopName.setText(store.getName());
            promotionShopAddress.setText(store.getAddress());
        }

        if(promotion != null) {
            promotionName.setText(promotion.getName());
            promotionCheckedVotes.setText(promotion.getGoodVotes() + "");
            promotionCheckedVotes.setText(promotion.getBadVotes() + "");
        }


        return convertView;
    }
}

