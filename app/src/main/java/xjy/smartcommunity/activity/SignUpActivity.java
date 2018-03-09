package xjy.smartcommunity.activity;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.HashMap;

import xjy.smartcommunity.R;
import xjy.smartcommunity.util.HttpUtils;
import xjy.smartcommunity.util.Utils;

public class SignUpActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:finish();return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void signUpBtnOnClick(View view) {
        final String username = ((EditText) findViewById(R.id.usernameSUA)).getText().toString();
        if (username.length() == 0) {
            Toast.makeText(getApplicationContext(), "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }
        String password = ((EditText) findViewById(R.id.passwordSUA)).getText().toString();
        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "密码长度至少6位", Toast.LENGTH_SHORT).show();
            return;
        }
        String password2 = ((EditText) findViewById(R.id.passwordSUA2)).getText().toString();
        if (!password.equals(password2)) {
            Toast.makeText(getApplicationContext(), "密码不一致，请重新输入", Toast.LENGTH_SHORT).show();
            return;
        }
        findViewById(R.id.loginInBtnSUA).setClickable(false);
        final String encodedPwd = Utils.md5Digest(password);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                signUpAuthontication(username, encodedPwd);
            }
        });
        thread.start();
    }

    private void signUpAuthontication(final String username, final String password) {
        String urlStr = getString(R.string.server_host) + "/registerUserByUsername.lr";
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
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            findViewById(R.id.loginInBtnSUA).setClickable(true);
            String errMsg = msg.getData().getString("errMsg");
            if (errMsg.length() > 0) {
                Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_SHORT).show();
                return;
            }
            int id = msg.getData().getInt("data");
            if (id != -1) {
                Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1500);
            } else
                Toast.makeText(getApplicationContext(), "注册失败", Toast.LENGTH_SHORT).show();
        }
    };
}
