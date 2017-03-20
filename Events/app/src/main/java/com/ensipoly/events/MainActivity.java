package com.ensipoly.events;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ensipoly.events.fragments.GroupsFragment;
import com.ensipoly.events.fragments.MapsFragments;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private BroadcastReceiver mCheckConnection;
    private BroadcastReceiver mCheckLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String userId = Utils.getUserID(getApplicationContext());
        if ( userId == null) {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Register to Broadcast Receivers
        mCheckConnection = new CheckConnection();
        mCheckLocation = new CheckLocation();
        IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        IntentFilter gpsFilter =  new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        this.registerReceiver(mCheckConnection, networkFilter);
        this.registerReceiver(mCheckLocation, gpsFilter);

        setContentView(R.layout.activity_main);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView.setNavigationItemSelectedListener(this);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DatabaseReference userDB = FirebaseUtils.getUserDBReference().child(userId);
        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Picasso.with(MainActivity.this).load(user.photoUrl).into((ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.profile_image));
                ((TextView) mNavigationView.getHeaderView(0).findViewById(R.id.username)).setText(user.username);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle
        // If it returns true, then it has handled
        // the nav drawer indicator touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mNavigationView.setCheckedItem(item.getItemId());
        Fragment fragment=null;
        switch (item.getItemId()) {
            case R.id.nav_map:
                MapsFragments mapsFragments = new MapsFragments();
                mapsFragments.getMapAsync(mapsFragments);
                fragment = mapsFragments;


                break;
            case R.id.nav_groups:
                fragment = new GroupsFragment();
                break;
        }
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        mDrawerLayout.closeDrawers();

        return true;
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }


}
