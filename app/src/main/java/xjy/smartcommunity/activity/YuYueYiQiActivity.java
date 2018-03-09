package xjy.smartcommunity.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import xjy.smartcommunity.R;
import xjy.smartcommunity.util.SharedPrefUtils;
import xjy.smartcommunity.util.HttpUtils;
import xjy.smartcommunity.util.Utils;

public class YuYueYiQiActivity extends AppCompatActivity {
    private final static int dateActivityCode = 0;
    private Button dateBtn = null;
    private ListView yiQiLv = null;
    private long datetime = 0;

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
        setContentView(R.layout.activity_yu_yue_yi_qi);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Calendar calendar = Calendar.getInstance();
        datetime = calendar.getTimeInMillis();
        datetime = Utils.getDateZeroMilis(datetime);
        dateBtn = (Button) findViewById(R.id.dateBtn);
        dateBtn.setText(Utils.formatDate(datetime)+" "+"上午");
        yiQiLv = (ListView) findViewById(R.id.yiQiLv);

        yiQiLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int pos = position;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        changeYiQiStatus(pos);
                    }
                });
                thread.start();
            }
        });
        updateYiQiLv();
    }

    private void updateYiQiLv(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                fillYiQiLv();
            }
        });
        thread.start();
    }

    private void changeYiQiStatus(int pos){
        HashMap<String, String> map = (HashMap<String, String>) yiQiLv.getItemAtPosition(pos);
        if(map.get("status").equals("未预约"))
            orderYiQi(pos);
        else
            cancelYiQi(pos);
    }

    private void cancelYiQi(int pos){
        HashMap<String, String> map = (HashMap<String, String>) yiQiLv.getItemAtPosition(pos);
        String urlStr = getString(R.string.server_host)+"/deleteOrderById.do";
        String errMsg = "";
        Bundle bundle = new Bundle();
        try {
            String orderId = map.get("orderId");
            HashMap<String, String> paras = new HashMap<>();
            paras.put("id",orderId);
            JSONObject json = HttpUtils.requestJson(this,urlStr,paras,false);
            if(json.getInt("status") != 0 && !json.getBoolean("data"))
                throw new Exception(json.getString("message"));
            bundle.putInt("pos",pos);
        } catch (Exception ex){
            errMsg = ex.getMessage();
            if(errMsg == null || errMsg.length() == 0)
                errMsg = "请求失败";
        }
        bundle.putString("errMsg",errMsg);
        Message msg = new Message();
        msg.setData(bundle);
        cancelYiQiHandler.sendMessage(msg);
    }

    private Handler cancelYiQiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String errMsg = msg.getData().getString("errMsg");
            if(errMsg.length() > 0){
                Toast.makeText(getApplicationContext(),errMsg,Toast.LENGTH_SHORT).show();
                return;
            }
            int pos = msg.getData().getInt("pos");
            HashMap<String, String> map = (HashMap<String, String>) yiQiLv.getItemAtPosition(pos);
            map.put("orderId","-1");
            map.put("status","未预约");
            int num = Integer.valueOf(map.get("num"));
            map.put("num",String.valueOf(num+1));
            SimpleAdapter adapter = (SimpleAdapter) yiQiLv.getAdapter();
            adapter.notifyDataSetChanged(); //更新view数据
        }
    };

    private void orderYiQi(int pos){
        HashMap<String, String> map = (HashMap<String, String>) yiQiLv.getItemAtPosition(pos);
        String urlStr = getString(R.string.server_host)+"/insertOrder.do";
        String errMsg = "";
        Bundle bundle = new Bundle();
        try {
            String userId = SharedPrefUtils.getValFromSP(this, SharedPrefUtils.userId);
            String yiQiId = map.get("id");
            long time = datetime;
            HashMap<String, String> paras = new HashMap<>();
            paras.put("userId",userId);
            paras.put("yiQiId",yiQiId);
            paras.put("time",String.valueOf(time));
            JSONObject json = HttpUtils.requestJson(this,urlStr,paras,false);
            if(json.getInt("status") != 0)
                throw new Exception(json.getString("message"));
            int orderId = json.getInt("data");
            bundle.putInt("orderId",orderId);
        } catch (Exception ex){
            errMsg = ex.getMessage();
            if(errMsg == null || errMsg.length() == 0)
                errMsg = "请求失败";
        }
        bundle.putInt("pos",pos);
        bundle.putString("errMsg",errMsg);
        Message msg = new Message();
        msg.setData(bundle);
        orderYiQiHandler.sendMessage(msg);
    }

    private Handler orderYiQiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String errMsg = msg.getData().getString("errMsg");
            if(errMsg.length() > 0){
                Toast.makeText(getApplicationContext(),errMsg,Toast.LENGTH_SHORT).show();
                return;
            }
            int pos = msg.getData().getInt("pos");
            HashMap<String, String> map = (HashMap<String, String>) yiQiLv.getItemAtPosition(pos);
            int orderId = msg.getData().getInt("orderId");
            map.put("orderId",String.valueOf(orderId));
            map.put("status","已预约");
            int num = Integer.valueOf(map.get("num"));
            map.put("num",String.valueOf(num-1));
            SimpleAdapter adapter = (SimpleAdapter) yiQiLv.getAdapter();

            adapter.notifyDataSetChanged(); //更新view数据
        }
    };

    private void fillYiQiLv(){
        String urlStr1 = getString(R.string.server_host)+"/getYiQiNumByTime.do";
        String urlStr2 = getString(R.string.server_host)+"/getUserOrderByTime.do";
        String errMsg = "";
        Bundle bundle = new Bundle();
        try{
            HashMap<String, String> paras1 = new HashMap<>();
            paras1.put("time",String.valueOf(datetime));
            JSONObject jsonYiQi = HttpUtils.requestJson(this,urlStr1,paras1,false);
            if(jsonYiQi.getInt("status") != 0)
                throw new Exception(jsonYiQi.getString("message"));

            HashMap<String, String> paras2 = new HashMap<>();
            paras2.put("userId", SharedPrefUtils.getValFromSP(this, SharedPrefUtils.userId));
            paras2.put("time", String.valueOf(datetime));
            JSONObject jsonOrders = HttpUtils.requestJson(this,urlStr2,paras2,false);
            if(jsonOrders.getInt("status") != 0)
                throw new Exception(jsonOrders.getString("message"));
            JSONArray orders = jsonOrders.getJSONArray("data");
            HashMap<Integer, Integer> yiQiMap = new HashMap<>();
            for(int i=0; i < orders.length(); i++){
                JSONObject order = orders.getJSONObject(i);
                yiQiMap.put(order.getInt("yi_qi_id"),order.getInt("id"));
            }

            ArrayList<HashMap<String,String>> list = new ArrayList<>();
            JSONArray arr = jsonYiQi.getJSONArray("data");
            for(int i=0; i < arr.length(); i++){
                JSONObject yiQi = arr.getJSONObject(i);
                HashMap<String,String> map = new HashMap<>();
                map.put("name",yiQi.getString("name"));
                map.put("num",yiQi.getString("num"));
                map.put("status","未预约");
                map.put("id",yiQi.getString("id"));
                map.put("orderId","-1");
                int yiQiId = Integer.valueOf(yiQi.getString("id"));
                if(yiQiMap.containsKey(yiQiId)) {
                    map.put("status", "已预约");
                    map.put("orderId",String.valueOf(yiQiMap.get(yiQiId)));
                }
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
        fillYiQiLvHandler.sendMessage(msg);
    }

    private Handler fillYiQiLvHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String errMsg = msg.getData().getString("errMsg");
            if(errMsg.length() > 0){
                Toast.makeText(getApplicationContext(),errMsg,Toast.LENGTH_SHORT).show();
                return;
            }
            ArrayList<HashMap<String,String>> list = (ArrayList<HashMap<String,String>>) msg.getData().getSerializable("list");
            SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(),list,R.layout.listitem_yi_qi,
                    new String[]{"name","num","status","id","orderId"},
                    new int[]{R.id.yiQiNameTv, R.id.yiQiNumTv, R.id.yiQiStatusTv,R.id.yiQiIdTv,R.id.orderIdTv});
            adapter.setViewBinder(viewBinder);
            yiQiLv.setAdapter(adapter);
        }
    };

    private SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            TextView textView = (TextView) view;
            if(textView.getId() == R.id.yiQiStatusTv){
                if(textRepresentation.equals("已预约"))
                    textView.setTextColor(Color.RED);
                else
                    textView.setTextColor(Color.GREEN);
            }
            textView.setText(textRepresentation);
            return true;
        }
    };

    public void dateBtnOnClick(View view){
        Intent intent = new Intent(this,DateActivity.class);
        intent.putExtra("date",datetime);
        startActivityForResult(intent,dateActivityCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == dateActivityCode && data != null) {
            long time = data.getLongExtra("date",0);
            String suff = "上午";
            long base = Utils.getDateZeroMilis(time);
            if(time-base > 0)
                suff = "下午";
            dateBtn.setText(Utils.formatDate(time)+" "+suff);
            datetime = time;
            updateYiQiLv();
        }
    }
}
