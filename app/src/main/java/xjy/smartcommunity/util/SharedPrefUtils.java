package xjy.smartcommunity.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yangjie on 2017/4/24.
 */

public class SharedPrefUtils {
    private static final String sharedPreferenceName = "app";
    public static final String userId = "userId";
    public static final String username = "username";
    public static final String sessionId = "sessionId";

    public static String getValFromSP(Context context, String key){
        SharedPreferences sp = context.getSharedPreferences(sharedPreferenceName,0);
        return sp.getString(key,"");
    }
    public static void putValToSP(Context context, String key, String val){
        SharedPreferences sp = context.getSharedPreferences(sharedPreferenceName,0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, val);
        editor.apply();
    }
}
