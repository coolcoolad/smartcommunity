package xjy.smartcommunity.util;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by yangjie on 2017/4/20.
 */

public class Utils {
    private final static String[] weekMap = new String[]{"周日","周一","周二","周三","周四","周五","周六"};
    public final static long hourMili = 3600*1000;
    public final static long dayMili = hourMili*24;
    //private static final String datetimeFormat = "yyyy-MM-dd HH:mm:ss";
    private static final String datetimeFormat = "yyyy-MM-dd";
    private static final SimpleDateFormat sdFormat = new SimpleDateFormat(datetimeFormat);
    //public static final String defaultTimeZone = "GMT+8:00";
    //static {sdFormat.setTimeZone(TimeZone.getTimeZone(defaultTimeZone));}

    public static Date parseDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        try {
            Date ans = format.parse(str);
            return ans;
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static String md5Digest(String str){
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(str.getBytes(Charset.forName("utf8")));
            byte[] arr = messageDigest.digest();
            StringBuilder ans = new StringBuilder();
            for(byte b: arr)
                ans.append((char)b);
            return ans.toString();
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return "";
    }

    public static long getDateZeroMilis(long time){
        time -= time%dayMili;
        return time;//-hourMili*8;
    }

    public static String formatDate(int month, int day, int week){
        return month+"月"+day+"日 "+weekMap[week];
    }

    public static String formatDate(long milis){
        //return sdFormat.format(new Date(milis-TimeZone.getDefault().getRawOffset()));
        return sdFormat.format(new Date(milis));
    }

    public static String formatDate(Calendar calendar){
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int week = calendar.get(Calendar.DAY_OF_WEEK)-1;
        return month+"月"+day+"日 "+weekMap[week];
    }

    public static int calcDayDelta(Calendar a, Calendar b){
        return (int)(Math.abs(a.getTimeInMillis()-b.getTimeInMillis())/dayMili);
    }

    public static long calcDayDelta(long a, long b){
        return Math.abs(a-b)/dayMili;
    }
}
