package com.ensipoly.events.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ensipoly.events.FirebaseUtils;
import com.ensipoly.events.R;
import com.ensipoly.events.Utils;
import com.ensipoly.events.fragments.GroupFragment;
import com.ensipoly.events.models.Group;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.HashMap;
import java.util.Map;


public class GroupsActivity extends AppCompatActivity {

    private DatabaseReference mGroupsDBReference;
    private BroadcastReceiver mBatteryReceiver;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if a user has been created
        String userId = Utils.getUserID(getApplicationContext());
        if (userId == null) {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_groups);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new GroupAdapter(getSupportFragmentManager()));

        mGroupsDBReference = FirebaseUtils.getGroupDBReference();
        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.add_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = GroupsActivity.this.getLayoutInflater();
                View layout = inflater.inflate(R.layout.dialog_group_add, null);
                final EditText groupInput = (EditText) layout.findViewById(R.id.input_group);
                AlertDialog.Builder dialog = new AlertDialog.Builder(GroupsActivity.this);

                dialog.setTitle("Add Group");
                dialog.setView(layout);
                dialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String user = Utils.getUserID(GroupsActivity.this);
                        addGroupToDatabase(groupInput.getText().toString(), user);
                    }
                });
                dialog.setNegativeButton("Cancel", null);
                dialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Show dialog when battery becomes too low
        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_LOW);
        mBatteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                lowBattery();
            }
        };

        this.registerReceiver(mBatteryReceiver, batteryFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.unregisterReceiver(mBatteryReceiver);
    }

    /**
     * @param name group's name. Note: groups are index by their name
     * @param user user's ID
     */
    public void addGroupToDatabase(final String name, final String user) {
        // We use a transaction in case 2 users create the same group at the same time.
        mGroupsDBReference.child(name).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Group group = mutableData.getValue(Group.class);
                Map<String, Boolean> map;
                if (group == null) {
                    // Group didn't exist
                    group = new Group();
                    group.setOrganizer(user);
                    map = new HashMap<>();
                    group.setMembers(map);
                } else {
                    // Group existed
                    map = group.getMembers();
                }

                // Add user to group
                map.put(user, false);

                mutableData.setValue(group);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null)
                    Toast.makeText(GroupsActivity.this, "An error occurred, please retry", Toast.LENGTH_SHORT).show();
                else {
                    if (dataSnapshot.getValue(Group.class).getNbUsers() == 1)
                        Toast.makeText(GroupsActivity.this, "Group created", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(GroupsActivity.this, "Added to group", Toast.LENGTH_SHORT).show();

                    // Add group to user's data
                    FirebaseUtils.getUserDBReference().child(user).child("groups").child(dataSnapshot.getKey()).setValue(true);

                    startMapsActivity(dataSnapshot.getKey());
                }
            }
        });
    }

    private void startMapsActivity(String groupID) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(MapsActivity.GROUP_ID, groupID);
        startActivity(intent);
    }

    private void lowBattery() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Low battery detected")
                .setMessage("Do you wish to set location update frequency to 30min?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(GroupsActivity.this);
                        p.edit().putString("pref_locationFrequency", 30 * 60 * 1000 + "").commit();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Adapter for tabs.
     */
    private class GroupAdapter extends FragmentPagerAdapter {


        GroupAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putInt(GroupFragment.POSITION, position);
            GroupFragment fragment = new GroupFragment();
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return GroupFragment.SIZE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return GroupFragment.getPageTitle(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

}
