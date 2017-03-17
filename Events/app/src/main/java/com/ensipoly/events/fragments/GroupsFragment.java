package com.ensipoly.events.fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.ensipoly.events.FirebaseUtils;
import com.ensipoly.events.Group;
import com.ensipoly.events.R;
import com.ensipoly.events.Utils;
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
public class GroupsFragment extends Fragment {

    private DatabaseReference mGroupsDBReference;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_groups, container, false);
        ViewPager viewPager = (ViewPager) v.findViewById(R.id.pager);
        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));
        mGroupsDBReference = FirebaseUtils.getGroupDBReference();
        FloatingActionButton button = (FloatingActionButton) v.findViewById(R.id.add_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View layout = inflater.inflate(R.layout.dialog_group_add,null);
                final EditText groupInput = (EditText) layout.findViewById(R.id.input_group);
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());

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
                                String user = Utils.getUserID(getActivity());
                                if(group==null){
                                    group = new Group();
                                    group.setOrganizer(user);
                                    map = new HashMap<>();
                                    group.setMembers(map);
                                }else
                                    map = group.getMembers();

                                map.put(user,true);
                                mutableData.setValue(group);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                if(databaseError!=null){
                                    // TODO R.string
                                    Toast.makeText(getContext(),"An error occurred, please retry", Toast.LENGTH_SHORT).show();
                                }else{
                                    if(dataSnapshot.getChildrenCount()==0)
                                        Toast.makeText(getContext(),"Group created", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(getContext(),"Added to group", Toast.LENGTH_SHORT).show();
                                    DatabaseReference userDBReference = FirebaseUtils.getUserDBReference();
                                    Map<String, Object> childUpdates = new HashMap<>();
                                    childUpdates.put("/"+ Utils.getUserID(getContext())+"/groups/"+dataSnapshot.getKey(),true);
                                    userDBReference.updateChildren(childUpdates);
                                }
                            }
                        });
                    }
                });
                dialog.setNegativeButton("Cancel",null);
                dialog.show();
            }
        });
        return v;
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
