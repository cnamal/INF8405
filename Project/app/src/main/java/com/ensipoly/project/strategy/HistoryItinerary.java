package com.ensipoly.project.strategy;

import android.graphics.Color;
import android.support.design.widget.BottomSheetBehavior;
import android.view.View;
import android.widget.Toast;

import com.ensipoly.project.R;
import com.ensipoly.project.models.History;
import com.ensipoly.project.models.Itinerary;
import com.ensipoly.project.utils.FirebaseUtils;
import com.ensipoly.project.utils.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Map;

public class HistoryItinerary extends ShowItinerary {

    private boolean selecting = true;

    public HistoryItinerary(StrategyParameters params) {
        super(params);
        FirebaseRecyclerAdapter<History, ViewHolder> adapter =
                new FirebaseRecyclerAdapter<History, ViewHolder>(
                        History.class,
                        R.layout.item_itinerary,
                        ViewHolder.class,
                        FirebaseUtils.getHistoryDBReference().orderByChild("user").equalTo(Utils.getUserID(activity))
                ) {
                    @Override
                    protected void populateViewHolder(ViewHolder viewHolder, final History model, int position) {
                        viewHolder.textView.setText("Run " + position);
                        viewHolder.descView.setVisibility(View.VISIBLE);
                        viewHolder.descView.setText(model.getStartTime() + "-" + model.getEndTime());
                        viewHolder.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                select(view, model);
                                DatabaseReference itineraryDB = FirebaseUtils.getItinerariesDBReference();
                                itineraryDB.child(model.getItinerary()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (mSelectedView.getTag() instanceof History) {
                                            History history = (History) mSelectedView.getTag();
                                            if (history.getItinerary().equals(model.getItinerary())) {
                                                Itinerary itinerary = dataSnapshot.getValue(Itinerary.class);
                                                showItinerary(itinerary);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                    }
                };
        recyclerView.setAdapter(adapter);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selecting = false;
                fab.hide(true);
                mBottomSheetBehavior1.setHideable(true);
                mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);
                History history = (History) mSelectedView.getTag();
                if (history.getPictures() != null) {
                    for (Map.Entry<String, String> picture : history.getPictures().entrySet()) {
                        Marker marker = pictures.get(idToInt(picture.getKey()));
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        marker.setTag(picture.getValue());
                    }
                }
            }
        });
    }

    private int idToInt(String id) {
        return Integer.parseInt(id.substring(2));
    }

    @Override
    public boolean onBackPressed() {
        if (selecting)
            return false;
        selecting = true;
        cleanupMap();
        mBottomSheetBehavior1.setHideable(false);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mSelectedView.setBackgroundColor(Color.WHITE);
        return true;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        if (selecting)
            return false;
        if (marker.equals(first) || marker.equals(last))
            return false;
        if (marker.getTag() != null) {
            Toast.makeText(activity,"Loading image...",Toast.LENGTH_SHORT).show();
            FirebaseStorage.getInstance().getReference("photos").child((String) marker.getTag()).getBytes(20*(1<<20)).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    activity.zoomImage(bytes, marker);
                }
            });
            return true;
        }
        return false;
    }
}
