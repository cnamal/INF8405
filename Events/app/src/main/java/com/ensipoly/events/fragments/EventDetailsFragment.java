package com.ensipoly.events.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ensipoly.events.FirebaseUtils;
import com.ensipoly.events.R;
import com.ensipoly.events.models.Event;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import static com.ensipoly.events.Utils.formatLocation;
import static com.ensipoly.events.models.Event.GOING;

public class EventDetailsFragment extends Fragment {


    private static final String USER_ID = "userID";
    private TextView location;
    private TextView name;
    private TextView desc;
    private TextView begin;
    private TextView end;
    private Button going;
    private Button maybe;
    private Button notGoing;

    public static EventDetailsFragment getInstance(Event event, @NonNull String userId) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle bundle = new Bundle();
        event.addArguments(bundle);
        bundle.putString(USER_ID, userId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event_details, container, false);
        name = (TextView) v.findViewById(R.id.event_name);
        location = (TextView) v.findViewById(R.id.location);
        desc = (TextView) v.findViewById(R.id.desc);
        begin = (TextView) v.findViewById(R.id.begin);
        end = (TextView) v.findViewById(R.id.end);
        going = (Button) v.findViewById(R.id.going);
        maybe = (Button) v.findViewById(R.id.maybe);
        notGoing = (Button) v.findViewById(R.id.not_going);
        final Event event = Event.getEventFromBundle(getArguments());
        final String userId = getArguments().getString(USER_ID);
        setup(event,userId);
        return v;
    }

    public void update(Event event, String userId){
        setup(event,userId);
    }

    private void setup(final Event event, final String userId) {
        final DatabaseReference eventDBReference = FirebaseUtils.getEventDBReference();


        name.setText(event.getName());

        if (event.getInfo().equals(""))
            desc.setVisibility(View.GONE);
        else
            desc.setText(event.getInfo());

        location.setText(formatLocation(event));

        begin.setText("Beginning: " + event.getStartingDate().toString());

        end.setText("Ending: " + event.getEndingDate().toString());

        if (event.hasAnswered(userId)) {
            going.setEnabled(false);
            maybe.setEnabled(false);
            notGoing.setEnabled(false);
            switch (event.getParticipations().get(userId)) {
                case Event.GOING:
                    going.setEnabled(true);
                    break;
                case Event.MAYBE:
                    maybe.setEnabled(true);
                    break;
                case Event.NOT_GOING:
                    notGoing.setEnabled(true);
                    break;
            }
        } else {
            going.setTag(GOING);
            maybe.setTag(Event.MAYBE);
            notGoing.setTag(Event.NOT_GOING);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog.setMessage("Submit your participation?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    eventDBReference
                                            .child(event.getId()).child("participations").child(userId)
                                            .setValue(view.getTag())
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getActivity(),"Vote added successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                    ;
                                }
                            }).show();

                }
            };
            going.setOnClickListener(listener);
            maybe.setOnClickListener(listener);
            notGoing.setOnClickListener(listener);
        }
    }

}
