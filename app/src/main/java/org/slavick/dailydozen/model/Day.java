package org.slavick.dailydozen.model;

import android.text.TextUtils;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

@Table(name = "dates")
public class Day extends TruncatableModel {
    private final static String TAG = Day.class.getSimpleName();

    public static final String DATE = "date";
    public static final String YEAR = "year";
    public static final String MONTH = "month";
    private static final String DAY = "day";

    @Column(name = DATE, unique = true, index = true)
    private long date;

    @Column(name = YEAR)
    private int year;

    @Column(name = MONTH)
    private int month;

    @Column(name = DAY)
    private int day;

    public Day() {
    }

    public Day(DateTime date) {
        setDate(date);
    }

    public String getDateString() {
        return getDateString(getDateTime());
    }

    public String getDateString(final DateTime dateTime) {
        return dateTime.format("YYYYMMDD");
    }

    // Calculates the number of days between epoch == 0 (Jan 1, 1970) and now
    public static int getNumDaysSinceEpoch() {
        return getEpoch().numDaysFrom(getToday()) + 1;
    }

    public int getNumDaysSince() {
        return getDateTime().numDaysFrom(getToday()) + 1;
    }

    public static DateTime getEpoch() {
        return DateTime.forInstant(0, TimeZone.getDefault());
    }

    private static DateTime getToday() {
        return DateTime.today(TimeZone.getDefault());
    }

    public DateTime getDateTime() {
        return DateTime.forDateOnly(year, month, day);
    }

    private void setDate(DateTime dateTime) {
        this.date = Long.valueOf(getDateString(dateTime));

        this.year = dateTime.getYear();
        this.month = dateTime.getMonth();
        this.day = dateTime.getDay();
    }

    public long getDate() {
        return date;
    }

    @Override
    public String toString() {
        return getDateTime().format("WWW, MMM D", Locale.getDefault());
    }

    public String getDayOfWeek() {
        return getDateTime().format("WWW", Locale.getDefault());
    }

    public static Day getByDate(String dateString) {
        Day day = new Select().from(Day.class)
                .where("date = ?", dateString)
                .executeSingle();

        if (day == null) {
            day = new Day(fromDateString(dateString));
        }

        return day;
    }

    public static Day createDateIfDoesNotExist(final String dateString) {
        Day day = getByDate(dateString);

        if (day.getId() == null) {
            day.save();
        }

        return day;
    }

    public static List<Day> getAllDays() {
        return new Select().from(Day.class)
                .orderBy("date ASC")
                .execute();
    }

    public static int getCount() {
        return new Select().from(Day.class)
                .count();
    }

    public static boolean isEmpty() {
        return getCount() == 0;
    }

    public static Day getEarliestDay() {
        return new Select().from(Day.class)
                .orderBy("date ASC")
                .limit(1)
                .executeSingle();
    }

    public Day getDayBefore() {
        return Day.getByDate(getDateString(getDateTime().minusDays(1)));
    }

    private static DateTime fromDateString(String dateString) {
        if (!TextUtils.isEmpty(dateString)) {
            return DateTime.forDateOnly(
                    Integer.valueOf(dateString.substring(0, 4)),  // year
                    Integer.valueOf(dateString.substring(4, 6)),  // month
                    Integer.valueOf(dateString.substring(6, 8))); // day
        }

        return null;
    }

    public List<Day> getDaysAfter() {
        return new Select().from(Day.class)
                .where("date >= ?", getDateString(getDateTime()))
                .orderBy("date ASC")
                .execute();
    }

    public static Day getByOffsetFromEpoch(int daysSinceEpoch) {
        return new Day(getEpoch().plusDays(daysSinceEpoch));
    }

    public static String getTabTitleForDay(int daysSinceEpoch) {
        return getEpoch().plusDays(daysSinceEpoch).format("WWW, MMM D", Locale.getDefault());
    }

    public static String getDayByOffset(Day earliestDay, int offset) {
        return earliestDay.getDateTime().plusDays(offset).format("YYYYMMDD", Locale.getDefault());
    }
}
