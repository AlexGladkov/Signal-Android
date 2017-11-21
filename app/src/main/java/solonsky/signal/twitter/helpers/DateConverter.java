package solonsky.signal.twitter.helpers;

import android.content.Context;
import android.content.res.Resources;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import solonsky.signal.twitter.R;

/**
 * Created by neura on 23.05.17.
 */

public class DateConverter {
    private static final String TAG = DateConverter.class.getSimpleName();
    private Context mContext;

    public DateConverter(Context mContext) {
        this.mContext = mContext;
    }

    public String parseDateTime(LocalDateTime input) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("HH:mm, MMM dd");
        return input.toString(dateTimeFormatter);
    }

    public String parseTime(LocalDateTime input) {
        LocalDateTime joda_today = new LocalDateTime();
        Resources resources = mContext.getResources();

        long second = 1000; // second in millis
        long minute = 60 * second; // minute in millis
        long hour = 60 * minute; // hour in millis
        long day = 24 * hour; // day in millis
        long week = 7 * day; // week in millis
        long month = 30 * day; // month in millis
        long year = 365 * day; // year in millis

        long diff = Math.abs(joda_today.toDateTime().getMillis() - input.toDateTime().getMillis());

        if (diff < (minute)) {
            return String.valueOf(diff / second) + resources.getString(R.string.time_seconds);
        } else if (diff >= minute && diff < hour) {
            return String.valueOf(diff / minute) + resources.getString(R.string.time_minutes);
        } else if (diff >= hour && diff < day) {
            return String.valueOf(diff / hour) + resources.getString(R.string.time_hours);
        } else if (diff >= day && diff < year) {
            return String.valueOf(diff / day) + resources.getString(R.string.time_days);
        } else {
            return String.valueOf(diff / year) + resources.getString(R.string.time_years);
        }
    }

    public String parseAbsTime(LocalDateTime dateTime) {
        return dateTime.toString("dd.MM.yy, HH:mm");
    }
}
