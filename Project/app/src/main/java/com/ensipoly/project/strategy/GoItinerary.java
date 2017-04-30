package com.ensipoly.project.strategy;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ensipoly.project.R;
import com.ensipoly.project.models.Itinerary;
import com.ensipoly.project.utils.FirebaseUtils;
import com.ensipoly.project.utils.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class GoItinerary extends ShowItinerary {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Photo currPhoto;
    private int state = SELECTING;
    private static final int SELECTING = 0;
    private static final int NOT_START = 1;
    private static final int STARTED = 2;
    private int numberOfPhotosTaken = 0;

    private float batteryLevelAtStart;

    public static class Photo {
        public byte[] photoData;
        private String fileName;
        private String absolutePath;
        public Marker marker;
    }

    private static class ItineraryViewHolder extends RecyclerView.ViewHolder {
        TextView itineraryTextView;
        View v;

        public ItineraryViewHolder(View v) {
            super(v);
            itineraryTextView = (TextView) itemView.findViewById(R.id.itinerary);
            this.v = v;
        }

        void setOnClickListener(View.OnClickListener listener) {
            v.setOnClickListener(listener);
        }
    }

    public GoItinerary(StrategyParameters params) {
        super(params);

        FirebaseRecyclerAdapter<Itinerary, ItineraryViewHolder> adapter =
                new FirebaseRecyclerAdapter<Itinerary, ItineraryViewHolder>(
                        Itinerary.class,
                        R.layout.item_itinerary,
                        ItineraryViewHolder.class,
                        FirebaseUtils.getItinerariesDBReference()
                ) {
                    @Override
                    protected void populateViewHolder(ItineraryViewHolder viewHolder, final Itinerary model, int position) {
                        model.setId(getRef(position).getKey());
                        viewHolder.itineraryTextView.setText("Itinerary " + position);
                        viewHolder.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                select(view, model);
                                showItinerary();
                            }
                        });

                    }
                };
        recyclerView.setAdapter(adapter);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state == SELECTING) {
                    state = NOT_START;
                    mBottomSheetBehavior1.setHideable(true);
                    mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);
                    fab.setImageResource(R.drawable.ic_timer_white_24dp);
                } else if (state == NOT_START) {
                    state = STARTED;
                    batteryLevelAtStart = activity.getBatteryLevel();
                    fab.setImageResource(R.drawable.ic_timer_off_white_24dp);
                } else {
                    fab.hide(true);
                    Toast.makeText(activity, "Uploading...", Toast.LENGTH_SHORT).show();
                    StorageReference storage = FirebaseStorage.getInstance().getReference("photos");
                    final SparseArray<String> map = new SparseArray<>();

                    for (int i = 0; i < pictures.size(); i++) {
                        Marker picture = pictures.get(i);
                        if (picture.getTag() == null)
                            continue;
                        Photo photo = (Photo) picture.getTag();
                        StorageReference photoRef = storage.child(photo.fileName);
                        final int finalI = i;
                        photoRef.putBytes(photo.photoData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @SuppressWarnings("VisibleForTests")
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                synchronized (this) {
                                    map.append(finalI, downloadUrl.toString());
                                    if (map.size() == numberOfPhotosTaken) {
                                        numberOfPhotosTaken = 0;
                                        DatabaseReference history = FirebaseUtils.getHistoryDBReference();
                                        String key = history.push().getKey();
                                        Map<String, Object> children = new HashMap<>();
                                        children.put("users/" + Utils.getUserID(activity) + "/history/" + key, false);
                                        for (int i = 0; i < map.size(); i++) {
                                            int id = map.indexOfKey(i);
                                            String url = map.valueAt(i);
                                            children.put("history/" + key + "/" + id, url);
                                        }
                                        children.put("history/" + key + "/itinerary", ((Itinerary) mSelectedView.getTag()).getId());
                                        FirebaseUtils.getDatabase().getReference().updateChildren(children).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                state = NOT_START;
                                                fab.show(true);
                                                onBackPressed();
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }


    @Override
    public boolean onBackPressed() {
        if (state == SELECTING)
            return false;
        if (state == STARTED)
            return true;
        state = SELECTING;
        fab.setImageResource(R.drawable.ic_done_white_24dp);
        fab.hide(true);
        cleanupMap();
        mBottomSheetBehavior1.setHideable(false);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mSelectedView.setBackgroundColor(Color.WHITE);
        return true;
    }


    private File createImageFile() throws IOException {
        currPhoto = new Photo();
        // Create an image file name
        currPhoto.fileName = UUID.randomUUID().toString();
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                currPhoto.fileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currPhoto.absolutePath = image.getAbsolutePath();
        return image;
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        if (state != STARTED)
            return false;
        if (marker.equals(first) || marker.equals(last))
            return false;
        if (marker.getTag() != null) {
            activity.zoomImage((Photo) marker.getTag());
            return true;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                // Error occurred while creating the File
                e.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                currPhoto.marker = marker;
                Uri photoURI = FileProvider.getUriForFile(activity,
                        "com.ensipoly.project.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Get photo data and show it
            Bitmap imageBitmap = BitmapFactory.decodeFile(currPhoto.absolutePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            currPhoto.photoData = stream.toByteArray();
            currPhoto.marker.setTag(currPhoto);
            currPhoto = null;
            updateNumberOfPhotoTaken();
        }
    }

    private void updateNumberOfPhotoTaken() {
        numberOfPhotosTaken++;
        if (numberOfPhotosTaken == pictures.size()) {
            float newBatteryLevel = activity.getBatteryLevel();
            int diff = (int) Math.ceil((batteryLevelAtStart - newBatteryLevel) * 100.0);
            String extra_message = "";
            if (diff < 0) {
                extra_message = activity.getString(R.string.battery_level_negative);
            } else if (diff >= 10) {
                extra_message = activity.getString(R.string.battery_level_positive);
            }
            Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.battery_level) + " " + diff + "% " + extra_message, Toast.LENGTH_LONG).show();
        }
    }

}
