package com.ensipoly.events.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ensipoly.events.FirebaseUtils;
import com.ensipoly.events.R;
import com.ensipoly.events.Utils;
import com.ensipoly.events.activities.MapsActivity;
import com.ensipoly.events.models.Event;
import com.ensipoly.events.models.Location;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class CreateEventFragment extends Fragment {

    private static final String GROUP_ID = "groupID";

    private EditText startingDateText;
    private Calendar startingDateCalendar = Calendar.getInstance();
    private EditText endingDateText;
    private Calendar endingDateCalendar = Calendar.getInstance();
    private Calendar currentCalendar = Calendar.getInstance();
    private EditText nameText;
    private TextView placeText;
    private EditText infoText;

    private String mGroupID;
    private Location mLocation;

    public static CreateEventFragment getInstance(Location location, String groupID) {
        CreateEventFragment fragment = new CreateEventFragment();
        Bundle bundle = new Bundle();
        location.addArguments(bundle);
        bundle.putString(GROUP_ID, groupID);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_create_event, container, false);
        final FloatingActionButton mFAB = (FloatingActionButton) getActivity().findViewById(R.id.fab_done);
        mFAB.hide(true);
        mLocation = Location.getLocationFromBundle(getArguments());
        mGroupID = getArguments().getString(GROUP_ID);
        startingDateText = (EditText) v.findViewById(R.id.input_starting_date);
        endingDateText = (EditText) v.findViewById(R.id.input_ending_date);
        nameText = (EditText) v.findViewById(R.id.input_name);
        nameText.setText(mLocation.getName());
        placeText = (TextView) v.findViewById(R.id.location);
        placeText.setText(Utils.formatLocation(mLocation));
        infoText = (EditText) v.findViewById(R.id.input_info);
        setupDates();

        final DatabaseReference eventDBReference = FirebaseUtils.getEventDBReference();

        // Setup the submit button
        Button submitButton = (Button) v.findViewById(R.id.create_event_create_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the form is OK. If not print error and return
                String error = verifyForm();
                if (error != null) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    return;
                }

                Event event = new Event(nameText.getText().toString(),
                        infoText.getText().toString().trim(),
                        mLocation.getLatitude(),
                        mLocation.getLongitude(),
                        startingDateCalendar.getTime(), endingDateCalendar.getTime());
                String eventId = eventDBReference.push().getKey();
                DatabaseReference ref = FirebaseUtils.getDatabase().getReference();
                Map<String, Object> children = new HashMap<>();
                children.put("/groups/" + mGroupID + "/event", eventId);
                children.put("/events/" + eventId, event);
                ref.updateChildren(children).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Event created", Toast.LENGTH_SHORT).show();
                        ((MapsActivity) getActivity()).hideBottomSheet();
                    }
                });
            }
        });
        return v;
    }

    private void setupDates() {

        // Setup the listener to call when the date is set. Therefore we need to set the time.
        // At the end, the label is updated.
        final TimePickerDialog.OnTimeSetListener startingTime = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                startingDateCalendar.set(Calendar.HOUR_OF_DAY, hour);
                startingDateCalendar.set(Calendar.MINUTE, minute);
                updateStartingLabel();
            }
        };

        final TimePickerDialog.OnTimeSetListener endingTime = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                endingDateCalendar.set(Calendar.HOUR_OF_DAY, hour);
                endingDateCalendar.set(Calendar.MINUTE, minute);
                updateEndingLabel();
            }
        };

        // Setup the listener to call when we want to set the date.
        // At the end, set the time.
        final DatePickerDialog.OnDateSetListener startingDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                startingDateCalendar.set(Calendar.YEAR, year);
                startingDateCalendar.set(Calendar.MONTH, monthOfYear);
                startingDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // Now set the time
                new TimePickerDialog(getContext(), startingTime,
                        currentCalendar.get(Calendar.HOUR_OF_DAY),
                        currentCalendar.get(Calendar.MINUTE),
                        false).show();
            }

        };

        final DatePickerDialog.OnDateSetListener endingDate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                endingDateCalendar.set(Calendar.YEAR, year);
                endingDateCalendar.set(Calendar.MONTH, monthOfYear);
                endingDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);


                // Now set the time
                new TimePickerDialog(getContext(), endingTime,
                        currentCalendar.get(Calendar.HOUR_OF_DAY),
                        currentCalendar.get(Calendar.MINUTE),
                        false).show();
            }

        };


        // Set the listener to open the DatePicker on click
        // Open it at the current date.
        startingDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), startingDate,
                        currentCalendar.get(Calendar.YEAR),
                        currentCalendar.get(Calendar.MONTH),
                        currentCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        endingDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), endingDate,
                        currentCalendar.get(Calendar.YEAR),
                        currentCalendar.get(Calendar.MONTH),
                        currentCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    // Format the calendar to update the starting date label
    private void updateStartingLabel() {

        String myFormat = "MM/dd/yy hh:mm aaa"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        startingDateText.setText(sdf.format(startingDateCalendar.getTime()));
    }

    // Format the calendar to update the ending date label
    private void updateEndingLabel() {

        String myFormat = "MM/dd/yy hh:mm aaa"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        endingDateText.setText(sdf.format(endingDateCalendar.getTime()));
    }

    private String verifyForm() {
        if (nameText.getText().toString().equals(""))
            return getString(R.string.create_event_name_missing);
        if (placeText.getText().toString().equals(""))
            return getString(R.string.create_event_place_missing);
        if (startingDateText.getText().toString().equals(""))
            return getString(R.string.create_event_starting_date_missing);
        if (endingDateText.getText().toString().equals(""))
            return getString(R.string.create_event_ending_date_missing);
        if (startingDateCalendar.after(endingDateCalendar))
            return getString(R.string.create_event_ending_date_before_starting_date_error);
        return null;
    }
}
