package utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Jérémie on 01/12/2016.
 */

public class DateTool {

    private final static String TAG = "DateTool";
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static String getDateAsString(){
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static boolean isBefore(String dateBefore, String dateAfter){
        try {
            Date dateBef = dateFormat.parse(dateBefore);
            Date dateAft = dateFormat.parse(dateAfter);
            return dateBef.before(dateAft);
        }catch(ParseException e){
            Log.e(TAG, "Date parsing failed.");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isNewDay(String datePastDay, String dateNewDay){
        try {
            Date dateBef = dateFormat.parse(datePastDay);
            Date dateAft = dateFormat.parse(dateNewDay);
            return isBefore(datePastDay, dateNewDay) && dateBef.getTime() > dateAft.getTime();
        }catch(ParseException e){
            Log.e(TAG, "Date parsing failed.");
            e.printStackTrace();
        }
        return false;
    }

    public static String getDateTwoDaysBeforeAsString(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        Date dateTwoDaysBefore = cal.getTime();
        return dateFormat.format(dateTwoDaysBefore);
    }
}
