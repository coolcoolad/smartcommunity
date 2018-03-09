package xjy.smartcommunity.activity;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import xjy.smartcommunity.R;
import xjy.smartcommunity.util.HttpUtils;

public class WuYeActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_wu_ye);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                showZhiNan();
            }
        });
        thread.start();
    }

    private void showZhiNan(){
        String urlStr = getString(R.string.server_host)+"/getAllBaoXiuInfo.do";
        String errMsg = "";
        Bundle bundle = new Bundle();
        try {
            JSONObject json = HttpUtils.requestJson(this,urlStr,null,false);
            if(json.getInt("status") != 0)
                throw new Exception(json.getString("message"));
            JSONArray arr = json.getJSONArray("data");
            ArrayList<HashMap<String,String>> list = new ArrayList<>();
            for(int i=0; i < arr.length(); i++){
                JSONObject jsonObject = arr.getJSONObject(i);
                HashMap<String,String> map = new HashMap<>();
                map.put("type",jsonObject.getString("type"));
                map.put("name",jsonObject.getString("name"));
                map.put("phone",jsonObject.getString("phone"));
                map.put("id",jsonObject.getString("id"));
                list.add(map);
            }
            bundle.putSerializable("list",list);
        } catch (Exception ex){
            errMsg = ex.getMessage();
            if(errMsg == null || errMsg.length() == 0)
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
            ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String,String>>) msg.getData().getSerializable("list");
            SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(),list, R.layout.listitem_bao_xiu_info,
                    new String[]{"type","name","phone","id"},new int[]{R.id.baoXiuTypeTv,R.id.baoXiuNameTv,R.id.baoXiuPhoneTv,R.id.baoXiuIdTv});
            ListView listView = (ListView) findViewById(R.id.baoXiuInfoLv);
            listView.setAdapter(adapter);
        }
    };
}
