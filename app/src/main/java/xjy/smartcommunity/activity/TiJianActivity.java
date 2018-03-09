package xjy.smartcommunity.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;

import xjy.smartcommunity.R;
import xjy.smartcommunity.util.HttpUtils;

public class TiJianActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_ti_jian);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//启用返回按钮

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                showTiJianLiuCheng();
            }
        });
        thread.start();
    }

    private void showTiJianLiuCheng(){
        String urlStr = getString(R.string.server_host)+"/getLatestTiJianLiuCheng.do";
        String errMsg = "";
        Bundle bundle = new Bundle();
        try {
            JSONObject json = HttpUtils.requestJson(this, urlStr,null,false);
            if(json.getInt("status") != 0)
                throw new Exception(json.getString("message"));
            JSONObject liuCheng = json.getJSONObject("data");
            bundle.putString("content",liuCheng.getString("content"));
        } catch (Exception ex){
            errMsg = ex.getMessage();
            if(errMsg==null || errMsg.length() == 0)
                errMsg = "请求失败";
        }
        bundle.putString("errMsg",errMsg);
        Message msg = new Message();
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String errMsg = msg.getData().getString("errMsg");
            if(errMsg.length() > 0){
                Toast.makeText(getApplicationContext(),errMsg,Toast.LENGTH_SHORT).show();
                return;
            }
            TextView liuChengTv = (TextView) findViewById(R.id.liuChengTv);
            liuChengTv.setText(msg.getData().getString("content"));
        }
    };

    public void yuYueBtnOnClick(View view) {
        Intent intent = new Intent(this,YuYueYiQiActivity.class);
        startActivity(intent);
    }

    public void reportBtnOnClick(View view){
        Intent intent = new Intent(this,TiJianReportActivity.class);
        startActivity(intent);
    }
}
