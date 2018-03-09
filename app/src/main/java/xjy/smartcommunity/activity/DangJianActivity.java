package xjy.smartcommunity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import xjy.smartcommunity.R;
import xjy.smartcommunity.util.HttpUtils;

public class DangJianActivity extends AppCompatActivity {
    private ListView listView;

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
        setContentView(R.layout.activity_dang_jian);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//启用返回按钮

        listView = (ListView)findViewById(R.id.dangJianLv);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                TextView v = (TextView) view;
//                v.setTextColor(0xFF0000);
                toDangJianDetailActivity(position);
            }
        });
        Thread thread = new Thread(new Runnable() { //必须异步发request
            @Override
            public void run() {
                fillListView();
            }
        });
        thread.start();
    }

    private void toDangJianDetailActivity(int pos){
        HashMap<String,String> map = (HashMap<String, String>) listView.getItemAtPosition(pos);
        Intent intent = new Intent(this,DangJianDetailActivity.class);
        intent.putExtra("id",map.get("id"));
        startActivity(intent);
    }

    private void fillListView(){
        String urlStr = getString(R.string.server_host)+"/showDangJian.do";
        String errMsg = "";
        Bundle bundle = new Bundle();
        try {
            JSONObject json = HttpUtils.requestJson(this, urlStr,null,false);
            if(json.getInt("status") != 0)
                throw new Exception(json.getString("message"));
            JSONArray arr = json.getJSONArray("data");
            ArrayList<HashMap<String,String>> list = new ArrayList<>();
            for(int i=0; i < arr.length(); i++){
                JSONObject jsonObject = arr.getJSONObject(i);
                HashMap<String,String> map = new HashMap<>();
                map.put("title",jsonObject.getString("title"));
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
        handler.sendMessage(msg);//把异步请求的数据发给handler
    }
    private Handler handler = new Handler() {//使用handler更新ui
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String errMsg = msg.getData().getString("errMsg");
            if(errMsg.length() > 0) {
                Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_SHORT).show();
                return;
            }
            ArrayList<HashMap<String,String>> list = (ArrayList<HashMap<String,String>>) msg.getData().getSerializable("list");
            SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(),list, R.layout.listitem_dang_jian,
                    new String[]{"title","id"},new int[]{R.id.itemTitle,R.id.itemId});
            listView.setAdapter(adapter);
        }
    };
}
