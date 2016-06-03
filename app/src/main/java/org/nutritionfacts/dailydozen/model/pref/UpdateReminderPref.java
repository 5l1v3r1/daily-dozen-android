package org.nutritionfacts.dailydozen.model.pref;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;
import java.util.Locale;

public class UpdateReminderPref {
    private final static String TAG = UpdateReminderPref.class.getSimpleName();

    @SerializedName("hourOfDay")
    private int hourOfDay = 20; // Default to 8pm
    @SerializedName("minute")
    private int minute;

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getMinute() {
        return minute;
    }

    @Override
    public String toString() {
        int hour = hourOfDay < 12 ? hourOfDay : hourOfDay % 12;
        if (hour == 0) {
            hour = 12;
        }

        return String.format(Locale.getDefault(), "%s:%02d %s", hour, minute, hourOfDay < 12 ? "AM" : "PM");
    }

    public long getAlarmTimeInMillis() {
        final Calendar cal = Calendar.getInstance();
        final int currentHourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        final int currentMinute = cal.get(Calendar.MINUTE);

        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);

        // If the alarm time for today has already passed, add 24 hours to set the alarm for tomorrow.
        if (currentHourOfDay > hourOfDay || currentHourOfDay == hourOfDay && currentMinute >= minute) {
            cal.add(Calendar.HOUR, 24);
        }

        Log.d(TAG, String.format("getAlarmTimeInMillis %s = %s", cal.getTime(), cal.getTimeInMillis()));

        return cal.getTimeInMillis();
    }
}
