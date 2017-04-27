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
import android.view.View;
import android.widget.TextView;

import com.ensipoly.project.R;
import com.ensipoly.project.models.Itinerary;
import com.ensipoly.project.utils.FirebaseUtils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class GoItinerary extends Strategy{

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private byte[] mPhotoData;
    private String mFileName;
    private String mAbsolutePath;
    private Marker currMarker;

    boolean selecting = true;
    private List<LatLng> waypoints;
    private List<Marker> pictures;
    private List<Circle> circles;
    private Marker first;
    private Marker last;
    private Polyline line;

    private static class ItineraryViewHolder extends RecyclerView.ViewHolder {
        TextView itineraryTextView;
        View v;
        public ItineraryViewHolder(View v) {
            super(v);
            itineraryTextView = (TextView) itemView.findViewById(R.id.itinerary);
            this.v = v;
        }

        public void setOnClickListener(View.OnClickListener listener){
            v.setOnClickListener(listener);
        }
    }

    public GoItinerary(StrategyParameters params) {
        super(params);
        mBottomSheetBehavior1.setHideable(false);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);

        FirebaseRecyclerAdapter<Itinerary,ItineraryViewHolder> adapter =
                new FirebaseRecyclerAdapter<Itinerary, ItineraryViewHolder>(
                        Itinerary.class,
                        R.layout.item_itinerary,
                        ItineraryViewHolder.class,
                        FirebaseUtils.getItinerariesDBReference()
                ) {
                    @Override
                    protected void populateViewHolder(ItineraryViewHolder viewHolder, final Itinerary model, int position) {
                        viewHolder.itineraryTextView.setText("Itinerary " + position);
                        viewHolder.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showItinerary(model);
                            }
                        });
                    }
                };
        recyclerView.setAdapter(adapter);
    }

    private void showItinerary(Itinerary itinerary){
        selecting = false;
        mBottomSheetBehavior1.setHideable(true);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);
        waypoints = itinerary.getGMapsWaypoints();
        List<LatLng> picturesLatLng = itinerary.getGMapsPictures();
        first = mMap.addMarker(new MarkerOptions().position(waypoints.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        last = mMap.addMarker(new MarkerOptions().position(waypoints.get(waypoints.size()-1)));
        line = mMap.addPolyline(new PolylineOptions().color(Color.RED).jointType(JointType.ROUND));
        line.setPoints(waypoints);
        circles = new ArrayList<>();
        for(int i=1;i<waypoints.size()-1;i++)
            circles.add(mMap.addCircle(new CircleOptions().center(waypoints.get(i)).radius(0.5).zIndex(1)));
        if(picturesLatLng!=null){
            pictures = new ArrayList<>();
            for(LatLng picture : picturesLatLng)
                pictures.add(mMap.addMarker(new MarkerOptions().position(picture).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));
        }
    }

    @Override
    public boolean onBackPressed() {
        if(selecting)
            return false;
        selecting = true;
        cleanupMap();
        mBottomSheetBehavior1.setHideable(false);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);
        return true;
    }

    @Override
    protected int initiallyShownButtons() {
        return 0;
    }

    @Override
    public void cleanup() {
        mBottomSheetBehavior1.setHideable(true);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);

        cleanupMap();
    }

    private void cleanupMap(){
        if(first!=null)
            first.remove();
        if(last!=null)
            last.remove();
        if(line != null)
            line.remove();
        if(pictures!=null)
        for(Marker marker : pictures)
            marker.remove();
        if(circles!=null)
        for(Circle circle : circles)
            circle.remove();
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        mFileName = UUID.randomUUID().toString();
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                mFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mAbsolutePath = image.getAbsolutePath();
        return image;
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker == first || marker == last)
            return false;
        if(marker.getTag() != null){
            // TODO Show picture
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
                currMarker = marker;
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
            Bitmap imageBitmap = BitmapFactory.decodeFile(mAbsolutePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            mPhotoData = stream.toByteArray();
            currMarker.setTag(mPhotoData);
            currMarker = null;
        }
    }

}
