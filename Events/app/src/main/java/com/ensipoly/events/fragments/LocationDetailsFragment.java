package com.ensipoly.events.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ensipoly.events.FirebaseUtils;
import com.ensipoly.events.R;
import com.ensipoly.events.activities.MapsActivity;
import com.ensipoly.events.models.Location;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.ensipoly.events.R.id.location_photo;
import static com.ensipoly.events.R.id.vote;

public class LocationDetailsFragment extends Fragment {

    private static final String GROUP_ID = "groupID";
    private static final String USER_ID = "userID";
    private static final String CAN_CREATE = "canCreateEvent";

    private Location mLocation;
    private String mGroupID;
    private String mUserId;
    private boolean mCanCreateEvent;
    private MapsActivity.Votes mVotes;

    public static LocationDetailsFragment getInstance(Location location, String groupID, String userID, boolean canCreateEvent){
        LocationDetailsFragment fragment = new LocationDetailsFragment();
        Bundle bundle = new Bundle();
        location.addArguments(bundle);
        bundle.putString(GROUP_ID, groupID);
        bundle.putString(USER_ID, userID);
        bundle.putBoolean(CAN_CREATE, canCreateEvent);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_location_details, container, false);
        mLocation = Location.getLocationFromBundle(getArguments());
        mGroupID = getArguments().getString(GROUP_ID);
        mUserId = getArguments().getString(USER_ID);
        mCanCreateEvent = getArguments().getBoolean(CAN_CREATE);
        mVotes = ((MapsActivity)getActivity()).getVotes();
        if(mLocation.getPhotoURL()!=null){
            CircleImageView imageView = (CircleImageView) v.findViewById(location_photo);
            imageView.setVisibility(View.VISIBLE);
            Picasso.with(getContext()).load(mLocation.getPhotoURL()).into(imageView);
        }
        TextView name = (TextView) v.findViewById(R.id.location_name);
        final RatingBar ratingBar = (RatingBar) v.findViewById(vote);
        name.setText(mLocation.getName());
        final FloatingActionButton mFAB = (FloatingActionButton) getActivity().findViewById(R.id.fab_done);
        if (mLocation.getVotes() != null && mLocation.getVotes().containsKey(mUserId)) {
            float vote = mLocation.getVotes().get(mUserId);
            v.findViewById(R.id.average).setVisibility(View.VISIBLE);
            RatingBar average = (RatingBar) v.findViewById(R.id.average_rb);
            average.setRating(mLocation.getAverage());
            ratingBar.setRating(vote);
            if (mCanCreateEvent) {
                mFAB.setImageResource(R.drawable.ic_event_white_24dp);
                mFAB.show(true);
                mFAB.setOnClickListener(new View.OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        ((MapsActivity)getActivity()).showCreateEvent(mLocation);
                    }
                });
            }
        } else {
            if (mVotes.getNbVotes() >= 2) {
                mFAB.setImageResource(R.drawable.ic_done_all_white_24dp);

            }
            float vote = mVotes.getVote(mLocation.getId());
            if (vote >= 0)
                ratingBar.setRating(vote);
            mFAB.show(true);
            mFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    float vote = ratingBar.getRating();
                    mVotes.addVote(mLocation.getId(), vote);

                    if (mVotes.getNbVotes() == 3) {
                        // TODO R.string
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                        dialog.setMessage("Save votes? You won't be able to modify your votes after.")
                                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Iterator<Map.Entry<String, Float>> it = mVotes.getIterator();
                                        Map<String, Object> children = new HashMap<>();
                                        while (it.hasNext()) {
                                            Map.Entry<String, Float> entry = it.next();
                                            children.put("/locations/" + entry.getKey() + "/votes/" + mUserId, entry.getValue());
                                        }
                                        children.put("/groups/" + mGroupID + "/members/" + mUserId, true);
                                        FirebaseUtils.getDatabase().getReference().updateChildren(children).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                ((MapsActivity)getActivity()).hideBottomSheet();
                                                Toast.makeText(getContext(), "Location added", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show()
                        ;
                    } else {
                        Toast.makeText(getContext(), "Vote saved locally", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        return v;
    }


}
