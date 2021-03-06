package com.ensipoly.project;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ensipoly.project.strategy.CreateItinerary;
import com.ensipoly.project.strategy.Default;
import com.ensipoly.project.strategy.GoItinerary;
import com.ensipoly.project.strategy.HistoryItinerary;
import com.ensipoly.project.strategy.Strategy;
import com.ensipoly.project.utils.CheckConnection;
import com.ensipoly.project.utils.CheckLocation;
import com.ensipoly.project.utils.FirebaseUtils;
import com.ensipoly.project.utils.Utils;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import static com.ensipoly.project.R.id.map;
import static com.ensipoly.project.utils.Utils.USER_ID_KEY_PREFERENCE;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private GoogleMap mMap;
    private Strategy strategy;
    private Strategy.StrategyParameters params;
    private StepsCounter stepsCounter;

    public static final int DEFAULT_STRATEGY = 0;
    public static final int CREATE_STRATEGY = 1;
    public static final int GO_STRATEGY = 2;
    public static final int HISTORY_STRATEGY = 3;

    private BottomSheetBehavior mBottomSheetBehavior1;
    private NestedScrollView mNestedScrollView;
    private RecyclerView recyclerView;
    private CheckConnection mCheckConnection;
    private CheckLocation mCheckLocation;
    private TextView mConnectionTextView;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration = 300;

    private static final int REQUEST_LOCATION_ON_MAP_READY = 0;
    private static final int REQUEST_LOCATION_START_LOCATION_UPDATES = 1;
    private List<com.ensipoly.project.utils.LatLng> locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildGoogleApiClient();

        setContentView(R.layout.activity_maps);
        TextView infoView = (TextView) findViewById(R.id.info_text_view);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        FloatingActionMenu menu = (FloatingActionMenu) findViewById(R.id.fab_menu);
        FloatingActionButton undo = (FloatingActionButton) findViewById(R.id.undo);
        FloatingActionButton cancel = (FloatingActionButton) findViewById(R.id.cancel);
        FloatingActionButton done = (FloatingActionButton) findViewById(R.id.done);
        FloatingActionButton go = (FloatingActionButton) findViewById(R.id.go);
        FloatingActionButton create = (FloatingActionButton) findViewById(R.id.create);
        FloatingActionButton history = (FloatingActionButton) findViewById(R.id.history);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_done);

        mNestedScrollView = (NestedScrollView) findViewById(R.id.bottom_sheet1);
        mBottomSheetBehavior1 = BottomSheetBehavior.from(mNestedScrollView);
        mBottomSheetBehavior1.setHideable(true);
        mBottomSheetBehavior1.setPeekHeight(300);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);

        fab.hide(false);
        recyclerView = (RecyclerView) findViewById(R.id.myList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        params = new Strategy.StrategyParameters();
        params.infoView = infoView;
        params.menu = menu;
        params.undo = undo;
        params.cancel = cancel;
        params.done = done;
        params.go = go;
        params.create = create;
        params.history = history;
        params.activity = this;
        params.mBottomSheetBehavior1 = mBottomSheetBehavior1;
        params.recyclerView = recyclerView;
        params.fab = fab;
        stepsCounter = new StepsCounter(this);

        String userID = Utils.getUserID(this);
        if (userID == null) {
            DatabaseReference userDB = FirebaseUtils.getUserDBReference();
            DatabaseReference user = userDB.push();
            userID = user.getKey();
            user.setValue(true);
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(USER_ID_KEY_PREFERENCE, userID);
            editor.commit();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected() && locations != null) {
            startLocationUpdates();
        }

        stepsCounter.registerListener();
        mConnectionTextView = (TextView) findViewById(R.id.connection_text_view);
        Utils.ConnectionInfoManager manager = new Utils.ConnectionInfoManager(mConnectionTextView);
        mCheckConnection = new CheckConnection(manager);
        mCheckLocation = new CheckLocation(manager);
        IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        IntentFilter gpsFilter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(mCheckConnection, networkFilter);
        registerReceiver(mCheckLocation, gpsFilter);

        // Initial GPS status is not given by the BroadcastReceiver. We query it ourselves.
        if (!(mCheckLocation.isGPSConnected(getApplicationContext())))
            manager.onLocationChanged(false);
        else
            manager.onLocationChanged(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected() && locations != null) {
            stopLocationUpdates(false);
        }

        stepsCounter.unregisterListener();
        unregisterReceiver(mCheckConnection);
        unregisterReceiver(mCheckLocation);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Get frequency from preferences
        long freq = 5000;

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(freq);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(freq / 2);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        params.map = mMap;
        // Add a marker in Sydney and move the camera
        LatLng montreal = new LatLng(45.5016889, -73.56725599999999);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(montreal).zoom(18).build()));
        strategy = new Default(params);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_START_LOCATION_UPDATES);
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length == 0)
            return;
        switch (requestCode) {
            case REQUEST_LOCATION_ON_MAP_READY:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    onMapReady(mMap);
                break;
            case REQUEST_LOCATION_START_LOCATION_UPDATES:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    startLocationUpdates();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        if (!strategy.onBackPressed()) {
            if (strategy instanceof Default)
                super.onBackPressed();
            else
                switchStrategy(DEFAULT_STRATEGY);
        }
    }

    public void switchStrategy(int strategy) {
        this.strategy.cleanup();
        switch (strategy) {
            case DEFAULT_STRATEGY:
                this.strategy = new Default(params);
                return;
            case CREATE_STRATEGY:
                this.strategy = new CreateItinerary(params);
                return;
            case GO_STRATEGY:
                this.strategy = new GoItinerary(params);
                return;
            case HISTORY_STRATEGY:
                this.strategy = new HistoryItinerary(params);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        strategy.onActivityResult(requestCode, resultCode, data);
    }

    public void zoomImage(GoItinerary.Photo photo) {
        zoomImage(photo.photoData, photo.marker);
    }

    // Adapted from https://developer.android.com/training/animation/zoom.html
    public void zoomImage(byte[] data, Marker marker) {

        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);

        // Set the start point at the marker position
        Projection projection = mMap.getProjection();
        LatLng markerLocation = marker.getPosition();
        Point screenPosition = projection.toScreenLocation(markerLocation);

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) (findViewById(R.id.image_zoom));
        expandedImageView.setImageBitmap(image);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect(screenPosition.x, screenPosition.y, screenPosition.x + 1, screenPosition.y + 1);
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        final FrameLayout image_wrapper = (FrameLayout) findViewById(R.id.layout_image_zoom);
        //final FrameLayout start_bounds = (FrameLayout) findViewById(R.id.start_bounds);
        //start_bounds.getGlobalVisibleRect(startBounds);
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
        final FrameLayout background_image = (FrameLayout) findViewById(R.id.background_image_zoom);
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
                        image_wrapper.setVisibility(View.GONE);
                        background_image.setVisibility(View.GONE);
                        // Don't forget to set the wrapper bounds to its originals
                        image_wrapper.setX(finalBounds.left);
                        image_wrapper.setY(finalBounds.top);
                        image_wrapper.setScaleX(1f);
                        image_wrapper.setScaleY(1f);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        image_wrapper.setVisibility(View.GONE);
                        background_image.setVisibility(View.GONE);
                        // Don't forget to set the wrapper bounds to its originals
                        image_wrapper.setX(finalBounds.left);
                        image_wrapper.setY(finalBounds.top);
                        image_wrapper.setScaleX(1f);
                        image_wrapper.setScaleY(1f);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

    public float getBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return level / (float) scale;
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_START_LOCATION_UPDATES);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    public void startLocationUpdates(List<com.ensipoly.project.utils.LatLng> list) {
        locations = list;
        startLocationUpdates();
    }

    public void stopLocationUpdates() {
        stopLocationUpdates(true);
    }

    private void stopLocationUpdates(boolean reset) {
        if (reset)
            locations = null;
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(locations != null)
            locations.add(new com.ensipoly.project.utils.LatLng(location));
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

}
