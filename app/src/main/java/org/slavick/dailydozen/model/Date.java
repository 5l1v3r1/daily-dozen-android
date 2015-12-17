package org.slavick.dailydozen.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Table(name = "dates")
public class Date extends Model implements Serializable {
    @Column(name = "date", index = true)
    private String date;

    public Date() {
    }

    public Date(String dateString) {
        setDate(dateString);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        String dateString = "";

        try {
            java.util.Date javaDate = new SimpleDateFormat("yyyyMMdd").parse(date);
            dateString = new SimpleDateFormat("EEE, MMM d").format(javaDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateString;
    }

    private static boolean exists(String dateString) {
        return new Select().from(Date.class).where("date = ?", dateString).exists();
    }

    public static Date createToday() {
        return createDateIfDoesNotExist(formatDateString(new java.util.Date()));
    }

    public static Date getByDate(String dateString) {
        return new Select().from(Date.class).where("date = ?", dateString).executeSingle();
    }

    private static String formatDateString(java.util.Date date) {
        return new SimpleDateFormat("yyyyMMdd").format(date);
    }

    private static Date createDateIfDoesNotExist(final String dateString) {
        Date date;

        if (!exists(dateString)) {
            date = new Date(dateString);
            date.save();
        } else {
            date = getByDate(dateString);
        }

        return date;
    }

    public static int getNumDates() {
        return new Select().from(Date.class).count();
    }

    public static Date getDateByOffsetFromBeginning(final int offset) {
        return new Select().from(Date.class)
                .offset(offset)
                .executeSingle();
    }
}
