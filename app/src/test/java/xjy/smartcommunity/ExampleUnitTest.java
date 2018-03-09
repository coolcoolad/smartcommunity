package xjy.smartcommunity;

import android.content.Intent;

import org.junit.Test;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import xjy.smartcommunity.util.Utils;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
//        System.out.println(TimeZone.getDefault().getDisplayName());
//        System.out.println(Utils.formatDate(new Date().getTime()));
//        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
//        System.out.println(TimeZone.getDefault().getDisplayName());
//        System.out.println(Utils.formatDate(new Date().getTime()));
//        System.out.println(Utils.formatDate(Utils.getDateZeroMilis(new Date().getTime())));
//        System.out.println(new Date().getTime());
        System.out.println(Utils.formatDate(0));
        System.out.println(Utils.formatDate(Utils.getDateZeroMilis(new Date().getTime())));
        System.out.println(Utils.parseDate("1900-01-01 00:00:00.0").getTime());
        Timestamp timestamp = new Timestamp(0);
    }
}