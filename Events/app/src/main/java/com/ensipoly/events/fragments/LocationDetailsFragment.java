package com.ensipoly.events.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.squareup.picasso.Target;

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
    private Bitmap mCurrentLoadedImage = null;

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration;

    public static LocationDetailsFragment getInstance(Location location, String groupID, String userID, boolean canCreateEvent) {
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
        mVotes = ((MapsActivity) getActivity()).getVotes();
        if (mLocation.getPhotoURL() != null) {
            final CircleImageView imageView = (CircleImageView) v.findViewById(location_photo);
            imageView.setVisibility(View.VISIBLE);
            Picasso.with(getContext()).load(mLocation.getPhotoURL()).into(new Target() {
                @Override
                public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                    /* Save the bitmap or do something with it here */
                    mCurrentLoadedImage = bitmap.copy(bitmap.getConfig(), false);
                    //Set it in the ImageView
                    imageView.setImageBitmap(bitmap);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }
            });
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomImageFromThumb(v);
                }
            });
        }

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);


        TextView name = (TextView) v.findViewById(R.id.location_name);
        final RatingBar ratingBar = (RatingBar) v.findViewById(vote);
        name.setText(mLocation.getName());
        final FloatingActionButton mFAB = (FloatingActionButton) getActivity().findViewById(R.id.fab_done);
        if (mLocation.getVotes() != null && mLocation.getVotes().containsKey(mUserId)) {
            // Has already voted
            float vote = mLocation.getVotes().get(mUserId);
            v.findViewById(R.id.average).setVisibility(View.VISIBLE);
            RatingBar average = (RatingBar) v.findViewById(R.id.average_rb);
            average.setRating(mLocation.getAverage());
            ratingBar.setRating(vote);
            ratingBar.setIsIndicator(true);
            if (mCanCreateEvent) {
                mFAB.setImageResource(R.drawable.ic_event_white_24dp);
                mFAB.show(true);
                mFAB.setOnClickListener(new View.OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        ((MapsActivity) getActivity()).showCreateEvent(mLocation);
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
                                                ((MapsActivity) getActivity()).hideBottomSheet();
                                                Toast.makeText(getContext(), "Votes saved", Toast.LENGTH_SHORT).show();
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

    // Adapted from https://developer.android.com/training/animation/zoom.html
    private void zoomImageFromThumb(final View thumbView) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) (getActivity().findViewById(R.id.image_zoom));
        if (mCurrentLoadedImage != null)
            expandedImageView.setImageBitmap(mCurrentLoadedImage);
        else
            Picasso.with(getContext()).load(mLocation.getPhotoURL()).into(expandedImageView);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        final FrameLayout image_wrapper = (FrameLayout) getActivity().findViewById(R.id.layout_image_zoom);
        image_wrapper.getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        final FrameLayout background_image = (FrameLayout) getActivity().findViewById(R.id.background_image_zoom);
        image_wrapper.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        image_wrapper.setPivotX(0f);
        image_wrapper.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(image_wrapper, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(image_wrapper, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(image_wrapper, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(image_wrapper,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                background_image.setVisibility(View.VISIBLE);
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        image_wrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(image_wrapper, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(image_wrapper,
                                        View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(image_wrapper,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(image_wrapper,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        image_wrapper.setVisibility(View.GONE);
                        background_image.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        image_wrapper.setVisibility(View.GONE);
                        background_image.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

}
