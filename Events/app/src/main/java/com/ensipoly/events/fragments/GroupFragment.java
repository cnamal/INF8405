package com.ensipoly.events.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ensipoly.events.FirebaseUtils;
import com.ensipoly.events.R;
import com.ensipoly.events.Utils;
import com.ensipoly.events.activities.GroupsActivity;
import com.ensipoly.events.activities.MapsActivity;
import com.ensipoly.events.models.Group;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import static com.ensipoly.events.R.string.members;


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

        void show(String groupID, Group group, boolean isOrganizer, boolean isMember) {
            this.title.setText(groupID);
            if (!isOrganizer && !isMember)
                organizer.setVisibility(View.GONE);
            if (!isOrganizer && isMember)
                organizer.setText(R.string.member);
            desc.setText(group.getNbUsers() + " " +
                    ((group.getNbUsers() == 1) ? root.getContext().getResources().getString(R.string.member) : root.getContext().getResources().getString(members)));
        }

        void setOnClickListener(View.OnClickListener listener) {
            root.setOnClickListener(listener);
        }

        void setOnLongClickListener(View.OnLongClickListener listener) {
            root.setOnLongClickListener(listener);
        }
    }

    public static final int SIZE = 2;
    public static final String POSITION = "position";
    private String mUserId;

    private DatabaseReference mGroupsDBReference;
    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_group, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.myList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        int position = getArguments().getInt(POSITION);
        mGroupsDBReference = FirebaseUtils.getGroupDBReference();
        getCurrentUser();
        if (position == 0)
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
                                final String key = dataSnapshot.getKey();
                                final Group group = dataSnapshot.getValue(Group.class);
                                boolean isOrganizer = group.getOrganizer().equals(mUserId);
                                viewHolder.show(key, group, isOrganizer, true);
                                viewHolder.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        startMapsActivity(key);
                                    }
                                });
                                if (!isOrganizer)
                                    viewHolder.setOnLongClickListener(new View.OnLongClickListener() {
                                        @Override
                                        public boolean onLongClick(View view) {
                                            quitGroup(key, group);
                                            return true;
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

    private void quitGroup(final String key, final Group group) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setMessage("Quit this group?")
                .setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Map<String, Object> children = new HashMap<>();
                        children.put("/groups/" + key + "/members/" + mUserId, null);
                        children.put("/users/" + mUserId + "/groups/" + key, null);
                        if (group.getEvent() != null) {
                            String eventId = group.getEvent();
                            children.put("/events/" + eventId + "/participations/" + mUserId, null);
                        } else if (group.getLocations() != null) {
                            for (String locationID : group.getLocations().keySet())
                                children.put("/locations/" + locationID + "/votes/" + mUserId, null);
                        }
                        FirebaseUtils.getDatabase().getReference().updateChildren(children);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupAllGroups() {
        FirebaseRecyclerAdapter<Group, ItemViewHolder> adapter =
                new FirebaseRecyclerAdapter<Group, ItemViewHolder>(
                        Group.class, R.layout.item_group, ItemViewHolder.class, mGroupsDBReference) {
                    protected void populateViewHolder(final ItemViewHolder viewHolder, final Group group, int position) {
                        final String key = this.getRef(position).getKey();
                        boolean isOrganizer = group.getOrganizer().equals(mUserId);
                        boolean inGroup = group.getMembers().containsKey(mUserId);
                        viewHolder.show(key, group, isOrganizer, inGroup);
                        if (inGroup) {
                            viewHolder.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startMapsActivity(key);
                                }
                            });
                            if (!isOrganizer)
                                viewHolder.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View view) {
                                        quitGroup(key, group);
                                        return true;
                                    }
                                });
                        } else
                            viewHolder.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    joinAlreadyCreatedGroup(key);
                                }
                            });
                    }
                };
        mRecyclerView.setAdapter(adapter);
    }

    private void startMapsActivity(String groupID) {
        Intent intent = new Intent(this.getActivity(), MapsActivity.class);
        intent.putExtra(MapsActivity.GROUP_ID, groupID);
        startActivity(intent);
    }

    private void getCurrentUser() {
        mUserId = Utils.getUserID(getContext());
    }

    private void joinAlreadyCreatedGroup(final String groupName) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

        dialog.setTitle(R.string.dialog_group_join_text_info_click);
        dialog.setPositiveButton(R.string.dialog_group_join_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((GroupsActivity) getActivity()).addGroupToDatabase(groupName, Utils.getUserID(getActivity()));
            }
        });
        dialog.setNegativeButton(R.string.dialog_group_join_cancel, null);
        dialog.show();
    }


}
