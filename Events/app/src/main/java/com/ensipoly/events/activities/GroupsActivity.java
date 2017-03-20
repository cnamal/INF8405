package com.ensipoly.events.activities;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ensipoly.events.FirebaseUtils;
import com.ensipoly.events.models.Group;
import com.ensipoly.events.R;
import com.ensipoly.events.Utils;
import com.ensipoly.events.fragments.GroupFragment;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsActivity extends AppCompatActivity {

    private DatabaseReference mGroupsDBReference;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userId = Utils.getUserID(getApplicationContext());
        if ( userId == null) {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_groups);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        mGroupsDBReference = FirebaseUtils.getGroupDBReference();
        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.add_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = GroupsActivity.this.getLayoutInflater();
                View layout = inflater.inflate(R.layout.dialog_group_add,null);
                final EditText groupInput = (EditText) layout.findViewById(R.id.input_group);
                AlertDialog.Builder dialog = new AlertDialog.Builder(GroupsActivity.this);

                dialog.setTitle("Add Group"); // TODO R.string
                dialog.setView(layout);
                dialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mGroupsDBReference.child(groupInput.getText().toString()).runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                Group group = mutableData.getValue(Group.class);
                                Map<String,Boolean> map;
                                String user = Utils.getUserID(GroupsActivity.this);
                                if(group==null){
                                    group = new Group();
                                    group.setOrganizer(user);
                                    map = new HashMap<>();
                                    group.setMembers(map);
                                }else
                                    map = group.getMembers();

                                map.put(user,false);
                                mutableData.setValue(group);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                if(databaseError!=null){
                                    // TODO R.string
                                    Toast.makeText(GroupsActivity.this,"An error occurred, please retry", Toast.LENGTH_SHORT).show();
                                }else{
                                    if(dataSnapshot.getChildrenCount()==0)
                                        Toast.makeText(GroupsActivity.this,"Group created", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(GroupsActivity.this,"Added to group", Toast.LENGTH_SHORT).show();

                                    DatabaseReference userDBReference = FirebaseUtils.getUserDBReference();
                                    Map<String, Object> childUpdates = new HashMap<>();
                                    childUpdates.put("/"+ Utils.getUserID(GroupsActivity.this)+"/groups/"+dataSnapshot.getKey(),true);
                                    userDBReference.updateChildren(childUpdates);
                                    startMapsActivity(dataSnapshot.getKey());
                                }
                            }
                        });
                    }
                });
                dialog.setNegativeButton("Cancel",null);
                dialog.show();
            }
        });
    }

    private void startMapsActivity(String groupID){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(MapsActivity.GROUP_ID,groupID);
        startActivity(intent);
    }

    class MyAdapter extends FragmentPagerAdapter {



        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putInt(GroupFragment.POSITION,position);
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

}
