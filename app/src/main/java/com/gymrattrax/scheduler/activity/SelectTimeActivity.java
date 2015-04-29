package com.gymrattrax.scheduler.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.gymrattrax.scheduler.BuildConfig;
import com.gymrattrax.scheduler.R;
import com.gymrattrax.scheduler.data.DatabaseHelper;
import com.gymrattrax.scheduler.model.CardioWorkoutItem;
import com.gymrattrax.scheduler.model.ExerciseName;
import com.gymrattrax.scheduler.model.StrengthWorkoutItem;
import com.gymrattrax.scheduler.receiver.NotifyReceiver;

import java.util.Calendar;
import java.util.Date;

public class SelectTimeActivity extends ActionBarActivity {
    private static final String TAG = "SelectTimeActivity";
    private TextView timeText;
    private String name;

    private int selectedHour = 0, selectedMinutes = 0;
    private TimePicker timepicker;
    private long eventId;

    private Date d;
    private String weight;
    private String date, distance, duration, details;
    private String sets;
    private String reps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_time);

        timepicker = (TimePicker) findViewById(R.id.time_picker);
        final Calendar cal = Calendar.getInstance();
        this.selectedHour = cal.get(Calendar.HOUR_OF_DAY);
        this.selectedMinutes = cal.get(Calendar.MINUTE);
        Button doneButton = (Button) findViewById(R.id.doneButton);
        Button addToGoogle = (Button) findViewById(R.id.addGoogleCalButton);
        TextView notifText = (TextView) findViewById(R.id.notifications_text);
        timeText = (TextView) findViewById(R.id.TimeSelected);
        updateTimeUI();
        final TextView exName = (TextView) findViewById(R.id.ex_name);
        TextView exDetails = (TextView) findViewById(R.id.ex_details);
        final TextView exDate = (TextView) findViewById(R.id.ex_date);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            name = extras.getString("name");
            date = extras.getString("date");

            if (ExerciseName.Cardio.fromString(name) != null) {
                distance = extras.getString("distance");
                duration = extras.getString("duration");
                String dStr;
                String tStr;

                // Display cardio details in text view
                if (Double.parseDouble(distance) == 1) {
                    dStr = distance + " mile";
                } else {
                    dStr = distance + " miles";
                }
                if (Double.parseDouble(duration) == 1) {
                    tStr = " in " + duration + " minute";
                } else {
                    tStr = duration + " minutes";
                }
                exName.setText(name + " ");
                exDetails.setText(dStr + " in " + tStr);
                details = (dStr + " in " + tStr);
                exDate.setText("on " + date);
            } else {
                // Display strength details
                weight = extras.getString("weight");
                sets = extras.getString("sets");
                reps = extras.getString("reps");
                String weightStr;
                String setsStr;
                String repsStr;

                if (Double.parseDouble(weight) == 1) {
                    weightStr = weight + " lb x ";
                } else {
                    weightStr = weight + " lbs x ";
                }
                if (Integer.parseInt(sets) == 1) {
                    setsStr = sets + " set x ";
                } else {
                    setsStr = sets + " sets x ";
                }
                if (Integer.parseInt(reps) == 1) {
                    repsStr = reps + " rep";
                } else {
                    repsStr = reps + " reps";
                }
                exName.setText(name + " ");
                exDetails.setText(weightStr + setsStr + repsStr);
                details = (weightStr + setsStr + repsStr);
                exDate.setText("on " + date);
            }
        }

        doneButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                SelectTimeActivity.this.loadSchedule();
            }
        });

        addToGoogle.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventId = addEventToGCal();
            }
        });
    }

    private long addEventToGCal() {
        long startMillis = 0;
        long endMillis = 0;

        // get workout date time
        String[] divDate = date.split("/", 3);

        int month = Integer.parseInt(divDate[0]) - 1;
        int day = Integer.parseInt(divDate[1]);
        int year = Integer.parseInt(divDate[2]);
        int hourInt = timepicker.getCurrentHour();
        int minInt = timepicker.getCurrentMinute();
        String title;
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(year, month, day, hourInt, minInt);

        if(name.equals("Biking")){
            title = "Workout: Cycling" + "\n" + details;
        } else {
            title = "Workout: " + name + "\n" + details;
        }
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("beginTime", beginTime.getTimeInMillis());
        intent.putExtra("allDay", false);
        intent.putExtra("endTime", beginTime.getTimeInMillis()+60*60*1000);
        intent.putExtra("title", title);
        startActivity(intent);
        return eventId;
    }

    private void loadSchedule() {
        Intent intent = new Intent(SelectTimeActivity.this, ViewScheduleActivity.class);
        addThisWorkout();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    private void updateTimeUI() {
        String am = "AM";
        String pm = "PM";
        String hour;
        String minutes;


        if (selectedHour > 12) {

            selectedHour -= 12;
            hour = (selectedHour > 9) ? "" + selectedHour : "" + selectedHour;
            minutes = (selectedMinutes > 9) ? "" + selectedMinutes : "0" + selectedMinutes;
            timeText.setText(hour + ":" + minutes + " " + pm);
        }
        else if (selectedHour == 0) {
            selectedHour += 12;
            hour = (selectedHour > 9) ? "" + selectedHour : "" + selectedHour;
            minutes = (selectedMinutes > 9) ? "" + selectedMinutes : "0" + selectedMinutes;
            timeText.setText(hour + ":" + minutes + " " + am);
        }
        else if (selectedHour == 12) {
            hour = (selectedHour > 9) ? "" + selectedHour : "" + selectedHour;
            minutes = (selectedMinutes > 9) ? "" + selectedMinutes : "0" + selectedMinutes;
            timeText.setText(hour + ":" + minutes + " " + pm);
        }
        else {
            hour = (selectedHour > 9) ? "" + selectedHour : "0" + selectedHour;
            minutes = (selectedMinutes > 9) ? "" + selectedMinutes : "0" + selectedMinutes;
            timeText.setText(hour + ":" + minutes + " " + am);
        }
    }

    private void addThisWorkout() {
        if (ExerciseName.Cardio.fromString(name) != null)
            addThisCardioWorkout();
        else
            addThisStrengthWorkout();
    }

    public void addThisCardioWorkout( ) {
        if (BuildConfig.DEBUG_MODE) Log.d(TAG, "cancelNotifications called.");
        NotifyReceiver.cancelNotifications(this);
        DatabaseHelper dbh = new DatabaseHelper(SelectTimeActivity.this);
        CardioWorkoutItem cItem = new CardioWorkoutItem();
        updateTimeUI();

        // Set cardio item date
        String[] dateArray = date.split("/", 3);
        int month = Integer.parseInt(dateArray[0]);
        int day = Integer.parseInt(dateArray[1]);
        int year = Integer.parseInt(dateArray[2]);
        int hourInt = timepicker.getCurrentHour();
        int minInt = timepicker.getCurrentMinute();

        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hourInt, minInt);
        d = cal.getTime();
        cItem.setDateScheduled(d);

        // Set cardio item name
//        ExerciseName exName = ExerciseName.fromString(name);
        cItem.setName(name);

        // Set cardio item distance
        cItem.setDistanceScheduled(Double.parseDouble(distance));

        // Set cardio item duration
        cItem.setTimeScheduled(Double.parseDouble(duration));

        // Add cardio workout to db
        cItem.setNotificationDefault(true);
        dbh.addWorkout(cItem);
        Toast.makeText(this, name + " added to schedule", Toast.LENGTH_SHORT).show();
        dbh.close();
        if (BuildConfig.DEBUG_MODE) Log.d(TAG, "setNotifications called.");
        NotifyReceiver.setNotifications(this);
    }

    public void addThisStrengthWorkout() {
        if (BuildConfig.DEBUG_MODE) Log.d(TAG, "cancelNotifications called.");
        NotifyReceiver.cancelNotifications(this);
        DatabaseHelper dbh = new DatabaseHelper(SelectTimeActivity.this);
        StrengthWorkoutItem sItem = new StrengthWorkoutItem();
        updateTimeUI();

        // Set Strength date and duration
        String dateString = date;
        String[] dateArray = dateString.split("/", 3);
        int month = Integer.parseInt(dateArray[0]);
        int day = Integer.parseInt(dateArray[1]);
        int year = Integer.parseInt(dateArray[2]);
        int hour = timepicker.getCurrentHour();
        int minute = timepicker.getCurrentMinute();

        Calendar cal = Calendar.getInstance();
        cal.set(year, month-1, day, hour, minute);
        d = cal.getTime();
        sItem.setDateScheduled(d);

        sItem.setNotificationDefault(true);

        // Set Strength name
//        ExerciseName exName = ExerciseName.fromString(name);
        sItem.setName(name);

        // Set strength details
        sItem.setWeightUsed(Double.parseDouble(weight));
        sItem.setRepsScheduled(Integer.parseInt(reps));
        sItem.setSetsScheduled(Integer.parseInt(sets));

        sItem.setNotificationDefault(true);
        dbh.addWorkout(sItem);
        Toast.makeText(this, name + " added to schedule", Toast.LENGTH_SHORT).show();
        dbh.close();
        if (BuildConfig.DEBUG_MODE) Log.d(TAG, "setNotifications called.");
        NotifyReceiver.setNotifications(this);
    }
}