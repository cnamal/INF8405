package com.ensipoly.events.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ensipoly.events.EventActivity;
import com.ensipoly.events.FirebaseUtils;
import com.ensipoly.events.Group;
import com.ensipoly.events.R;
import com.ensipoly.events.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment {


    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView desc;
        TextView organizer;
        View root;

        public ItemViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            title = (TextView) itemView.findViewById(R.id.groupTitleTextView);
            desc = (TextView) itemView.findViewById(R.id.groupDescTextView);
            organizer = (TextView) itemView.findViewById(R.id.organizerTextView);
        }

        public void show(String title, Group group, boolean isOrganizer) {
            this.title.setText(title);
            if (!isOrganizer)
                organizer.setVisibility(View.GONE);
            desc.setText(group.getNbUsers() + " members");
        }

        public void setOnClickListener(View.OnClickListener listener) {
            root.setOnClickListener(listener);
        }
    }

    public static final int SIZE = 2;
    public static final String POSITION = "position";
    private int mPosition;
    private String mUserId;

    private DatabaseReference mGroupsDBReference;
    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_group, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.myList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mPosition = getArguments().getInt(POSITION);
        mGroupsDBReference = FirebaseUtils.getGroupDBReference();
        getCurrentUser();
        if (mPosition == 0)
            setupMyGroups();
        else
            setupAllGroups();

        return v;
    }

    public static CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "My Groups";
            case 1:
                return "All Groups";
        }
        return null;
    }

    private void setupMyGroups() {
        final DatabaseReference ref = FirebaseUtils.getUserDBReference().child(mUserId);
        FirebaseRecyclerAdapter<Boolean, ItemViewHolder> adapter =
                new FirebaseRecyclerAdapter<Boolean, ItemViewHolder>(
                        Boolean.class, R.layout.item_group, ItemViewHolder.class, ref.child("groups")) {
                    protected void populateViewHolder(final ItemViewHolder viewHolder, Boolean model, int position) {
                        String key = this.getRef(position).getKey();
                        mGroupsDBReference.child(key).addValueEventListener(new ValueEventListener() {
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Group group = dataSnapshot.getValue(Group.class);
                                boolean isOrganizer = group.getOrganizer().equals(mUserId);
                                viewHolder.show(dataSnapshot.getKey(), group, isOrganizer);
                                if (isOrganizer)
                                    viewHolder.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startEventActivity();
                                        }
                                    });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });
                    }
                };
        mRecyclerView.setAdapter(adapter);
    }

    private void setupAllGroups() {
        FirebaseRecyclerAdapter<Group, ItemViewHolder> adapter =
                new FirebaseRecyclerAdapter<Group, ItemViewHolder>(
                        Group.class, R.layout.item_group, ItemViewHolder.class, mGroupsDBReference) {
                    protected void populateViewHolder(final ItemViewHolder viewHolder, Group group, int position) {
                        String key = this.getRef(position).getKey();
                        boolean isOrganizer = group.getOrganizer().equals(mUserId);
                        viewHolder.show(key, group, isOrganizer);
                        if(isOrganizer)
                        viewHolder.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startEventActivity();
                            }
                        });
                    }
                };
        mRecyclerView.setAdapter(adapter);
    }

    private void startEventActivity(){
        Intent intent = new Intent(this.getActivity(), EventActivity.class);
        startActivity(intent);
        this.getActivity().finish();
    }

    private void getCurrentUser() {
        mUserId = Utils.getUserID(getContext());
    }

}
