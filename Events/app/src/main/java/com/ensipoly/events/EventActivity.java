package com.ensipoly.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventActivity extends AppCompatActivity {

    private EditText startingDateText;
    private Calendar startingDateCalendar = Calendar.getInstance();
    private EditText endingDateText;
    private Calendar endingDateCalendar = Calendar.getInstance();
    private Calendar currentCalendar = Calendar.getInstance();
    private EditText nameText;
    private EditText placeText;
    private EditText infoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        startingDateText = (EditText) findViewById(R.id.input_starting_date);
        endingDateText = (EditText) findViewById(R.id.input_ending_date);
        nameText = (EditText) findViewById(R.id.input_name);
        placeText = (EditText) findViewById(R.id.input_place);
        infoText = (EditText) findViewById(R.id.input_info);
        setupDates();

        // Setup the submit button
        Button submitButton = (Button) findViewById(R.id.create_event_create_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the form is OK. If not print error and return
                String error = verifyForm();
                if(error != null){
                    Toast.makeText(EventActivity.this,error,Toast.LENGTH_SHORT).show();
                    return;
                }

                // Else print in log the event created.
                // TODO: Handle database
                Event event = new Event(null, nameText.getText().toString(),
                        infoText.getText().toString(),
                        placeText.getText().toString(),
                        startingDateCalendar.getTime(), endingDateCalendar.getTime());
                Log.d("EventActivity", "**************************************");
                Log.d("EventActivity", event.toString());
            }
        });
    }

    private void setupDates(){

        // Setup the listener to call when the date is set. Therefore we need to set the time.
        // At the end, the label is updated.
        final TimePickerDialog.OnTimeSetListener startingTime = new TimePickerDialog.OnTimeSetListener(){
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute){
                startingDateCalendar.set(Calendar.HOUR_OF_DAY, hour);
                startingDateCalendar.set(Calendar.MINUTE, minute);
                updateStartingLabel();
            }
        };

        final TimePickerDialog.OnTimeSetListener endingTime = new TimePickerDialog.OnTimeSetListener(){
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute){
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
                new TimePickerDialog(EventActivity.this, startingTime,
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
                new TimePickerDialog(EventActivity.this, endingTime,
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
                new DatePickerDialog(EventActivity.this, startingDate,
                        currentCalendar.get(Calendar.YEAR),
                        currentCalendar.get(Calendar.MONTH),
                        currentCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        endingDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(EventActivity.this, endingDate,
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

    private String verifyForm(){
        if(nameText.getText().toString().equals(""))
            return getString(R.string.create_event_name_missing);
        if(placeText.getText().toString().equals(""))
            return getString(R.string.create_event_place_missing);
        if(startingDateText.getText().toString().equals(""))
            return getString(R.string.create_event_starting_date_missing);
        if(endingDateText.getText().toString().equals(""))
            return getString(R.string.create_event_ending_date_missing);
        if(startingDateCalendar.after(endingDateCalendar))
            return getString(R.string.create_event_ending_date_before_starting_date_error);
        return null;
    }
}
