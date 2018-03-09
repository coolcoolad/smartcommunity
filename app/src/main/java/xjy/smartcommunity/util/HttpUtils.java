package xjy.smartcommunity.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangjie on 2017/4/7.
 */

public class HttpUtils {
    public static JSONObject requestJson(Context context, String urlStr, HashMap<String,String> paraMap, boolean type) throws Exception{
        StringBuilder paraSB = new StringBuilder();
        if(paraMap != null)
            for(Map.Entry<String,String> e: paraMap.entrySet())
                paraSB.append(e.getKey()+"="+ URLEncoder.encode(e.getValue(),"utf8")+"&");
        String paraStr = paraSB.length()>0?paraSB.substring(0,paraSB.length()-1):"";
        if(!type && paraStr.length() > 0)
            urlStr += "?"+paraStr;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(3000);
        conn.setRequestMethod("GET");
        String sessionId = SharedPrefUtils.getValFromSP(context,SharedPrefUtils.sessionId); //读取上次的sessionId
        if(sessionId.length() > 0)
            conn.setRequestProperty("cookie",sessionId);
        conn.setDoInput(true);
        if(type) {
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(paraStr.length()));
            OutputStream os = conn.getOutputStream();
            os.write(paraStr.getBytes("utf8"));
        }
        InputStream is = conn.getInputStream();
        int code = conn.getResponseCode();
        String cookieStr = conn.getHeaderField("Set-Cookie");
        if(cookieStr != null) {
            sessionId = cookieStr.substring(0, cookieStr.indexOf(";"));
            SharedPrefUtils.putValToSP(context,SharedPrefUtils.sessionId,sessionId); //写入新的sessionId
        }
        if(code == 200){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = 0;
            byte[] data = new byte[1024];
            while (-1 != (len = is.read(data)))
                baos.write(data,0,len);
            return new JSONObject(new String(baos.toByteArray(),"utf8"));
        }
        throw new Exception("code != 200");
    }
}
