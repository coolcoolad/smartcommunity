package xjy.smartcommunity.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.TimeZone;

import xjy.smartcommunity.R;
import xjy.smartcommunity.util.HttpUtils;
import xjy.smartcommunity.util.SharedPrefUtils;
import xjy.smartcommunity.util.Utils;

public class LoginActivity extends AppCompatActivity {
    private String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //TimeZone.setDefault(TimeZone.getTimeZone(Utils.defaultTimeZone)); //设置默认时区

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                checkLoginStatus();
            }
        });
        thread.start();
    }

    private void checkLoginStatus(){
        String urlStr = getString(R.string.server_host)+"/test.do";
        String errMsg = "";
        Bundle bundle = new Bundle();
        try {
            JSONObject json = HttpUtils.requestJson(this,urlStr,null,false);
            if(json.getInt("status") != 0)
                bundle.putBoolean("flag",false);
            else
                bundle.putBoolean("flag",true);
        } catch (Exception ex){
            errMsg = ex.getMessage();
            if(errMsg == null || errMsg.length() == 0)
                errMsg = "请求失败";
        }
        bundle.putString("errMsg",errMsg);
        Message msg = new Message();
        msg.setData(bundle);
        checkLoginHander.sendMessage(msg);
    }

    private Handler checkLoginHander = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String errMsg = msg.getData().getString("errMsg");
            if(errMsg.length() > 0){
                Toast.makeText(getApplicationContext(),errMsg,Toast.LENGTH_SHORT).show();
                return;
            }
            if(msg.getData().getBoolean("flag"))
                toMainAcyivity();
        }
    };

    public void loginInBtnOnClick(View view) {
        username = ((EditText) findViewById(R.id.username)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();
        if (username.length() == 0) {
            Toast.makeText(getApplicationContext(), "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() == 0) {
            Toast.makeText(getApplicationContext(), "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }

        final String encodedPwd = Utils.md5Digest(password);
        findViewById(R.id.loginInBtn).setClickable(false);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                userAuthentication(username, encodedPwd);
            }
        });
        thread.start();
    }

    private void userAuthentication(final String username, final String password) {
        String urlStr = getString(R.string.server_host) + "/login.lr";
        HashMap<String, String> paraMap = new HashMap<>();
        paraMap.put("username", username);
        paraMap.put("password", password);
        String errMsg = "";
        Bundle bundle = new Bundle();
        try {
            JSONObject json = HttpUtils.requestJson(this, urlStr, paraMap, true);
            if (json.getInt("status") != 0)
                throw new Exception(json.getString("message"));
            bundle.putInt("data", json.getInt("data"));
        } catch (Exception ex) {
            errMsg = ex.getMessage();
            if (errMsg == null || errMsg.length() == 0)
                errMsg = "";
        }
        bundle.putString("errMsg", errMsg);
        Message msg = new Message();
        msg.setData(bundle);
        userAuthenHandler.sendMessage(msg);
    }

    private Handler userAuthenHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            findViewById(R.id.loginInBtn).setClickable(true);
            String errMsg = msg.getData().getString("errMsg");
            if (errMsg.length() > 0) {
                Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_SHORT).show();
                return;
            }
            int id = msg.getData().getInt("data");
            if (id != -1) {
                SharedPrefUtils.putValToSP(getApplicationContext(),SharedPrefUtils.username,username); //保存当前用户名
                SharedPrefUtils.putValToSP(getApplicationContext(),SharedPrefUtils.userId,String.valueOf(id)); //保存当前用户id
                System.out.println(id);
                toMainAcyivity();
            }
            else
                Toast.makeText(getApplicationContext(), "用户名或者密码有误", Toast.LENGTH_SHORT).show();
        }
    };

    private void toMainAcyivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }

    public void signUpBtnOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivity(intent);
    }
}
