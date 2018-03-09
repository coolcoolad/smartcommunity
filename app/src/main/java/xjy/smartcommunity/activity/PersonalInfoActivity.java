package xjy.smartcommunity.activity;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import xjy.smartcommunity.R;
import xjy.smartcommunity.util.HttpUtils;
import xjy.smartcommunity.util.SharedPrefUtils;

public class PersonalInfoActivity extends AppCompatActivity {
    private TextView usernameTv = null;
    private EditText nameEt = null;
    private EditText sexEt = null;
    private EditText ageEt = null;
    private EditText telEt = null;

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
        setContentView(R.layout.activity_personal_info);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        usernameTv = (TextView) findViewById(R.id.usernameTv);
        nameEt = (EditText) findViewById(R.id.nameEt);
        sexEt = (EditText) findViewById(R.id.sexEt);
        ageEt = (EditText) findViewById(R.id.ageEt);
        telEt = (EditText) findViewById(R.id.telephoneEt);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                showPersonalInfo();
            }
        });
        thread.start();
    }

    private void showPersonalInfo(){
        String urlStr = getString(R.string.server_host)+"/getUserByUsername.do";
        String errMsg = "";
        Bundle bundle = new Bundle();
        try{
            HashMap<String, String> paras = new HashMap<>();
            String username = SharedPrefUtils.getValFromSP(this,SharedPrefUtils.username);
            paras.put("username", username);
            JSONObject json = HttpUtils.requestJson(this,urlStr,paras,false);
            if(json.getInt("status") != 0)
                throw new Exception(json.getString("message"));
            bundle.putString("username", username);
            JSONArray jsonArray = json.getJSONArray("data");
            if(jsonArray.length() == 0)
                throw new Exception("当前用户不存在");
            JSONObject user = jsonArray.getJSONObject(0);
            bundle.putString("name",user.getString("name"));
            int sex = user.getInt("sex");
            if(sex == 0)
                bundle.putString("sex","男");
            else
                bundle.putString("sex","女");
            bundle.putString("age",String.valueOf(user.getInt("age")));
            bundle.putString("tel",user.getString("tel"));
        } catch (Exception ex){
            errMsg = ex.getMessage();
            if(errMsg == null || errMsg.length() == 0)
                errMsg = "请求失败";
        }
        bundle.putString("errMsg",errMsg);
        Message msg = new Message();
        msg.setData(bundle);
        showPersonalInfoHandler.sendMessage(msg);
    }

    private Handler showPersonalInfoHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String errMsg = msg.getData().getString("errMsg");
            if(errMsg.length() > 0){
                Toast.makeText(getApplicationContext(),errMsg,Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle bundle = msg.getData();
            usernameTv.setText(bundle.getString("username"));
            nameEt.setText(bundle.getString("name"));
            sexEt.setText(bundle.getString("sex"));
            ageEt.setText(bundle.getString("age"));
            telEt.setText(bundle.getString("tel"));
        }
    };

    public void submitBtnOnClick(View view){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                submitPersonal();
            }
        });
        thread.start();
    }

    private void submitPersonal(){
        String urlStr = getString(R.string.server_host)+"/updateUserByUsername.do";
        String errMsg = "";
        Bundle bundle = new Bundle();
        try {
            HashMap<String, String> paras = new HashMap<>();
            paras.put("username",SharedPrefUtils.getValFromSP(this, SharedPrefUtils.username));
            paras.put("name", nameEt.getText().toString());
            String sex = sexEt.getText().toString();
            if(sex.equals("男"))
                paras.put("sex","0");
            else
                paras.put("sex","1");
            paras.put("age",ageEt.getText().toString());
            paras.put("tel",telEt.getText().toString());
            JSONObject json = HttpUtils.requestJson(this,urlStr,paras,true);
            if(json.getInt("status") != 0)
                throw new Exception(json.getString("message"));
            int id = json.getInt("data");
            bundle.putInt("id",id);
        } catch (Exception ex){
            errMsg = ex.getMessage();
            if(errMsg == null || errMsg.length() == 0)
                errMsg = "请求失败";
        }
        bundle.putString("errMsg",errMsg);
        Message msg = new Message();
        msg.setData(bundle);
        submitHandler.sendMessage(msg);
    }

    private Handler submitHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String errMsg = msg.getData().getString("errMsg");
            if(errMsg.length() > 0){
                Toast.makeText(getApplicationContext(),errMsg,Toast.LENGTH_SHORT).show();
                return;
            }
            int id = msg.getData().getInt("id");
            if(id > -1) {
                Toast.makeText(getApplicationContext(), "修改成功", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                },1500);
            } else
                Toast.makeText(getApplicationContext(),"修改失败",Toast.LENGTH_SHORT).show();
        }
    };
}
