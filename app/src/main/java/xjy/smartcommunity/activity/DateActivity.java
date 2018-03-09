package xjy.smartcommunity.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import xjy.smartcommunity.R;
import xjy.smartcommunity.calendardecorator.SpecialDayDecorator;
import xjy.smartcommunity.util.HttpUtils;
import xjy.smartcommunity.util.Utils;

public class DateActivity extends AppCompatActivity {
    private MaterialCalendarView calendarView = null;
    private List<Date> specialDates = null;
    private long minDay = 0;
    private long maxDay = 0;

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
        setContentView(R.layout.activity_date);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final long lastTime = getIntent().getLongExtra("date",0);
        calendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
        Calendar calendar = Calendar.getInstance();
        MaterialCalendarView.StateBuilder stateBuilder = calendarView.state().edit();
        stateBuilder.setMinimumDate(calendar);
        minDay = Utils.getDateZeroMilis(calendar.getTimeInMillis());
        calendar.add(Calendar.MONTH,3);
        stateBuilder.setMaximumDate(calendar);
        maxDay = Utils.getDateZeroMilis(calendar.getTimeInMillis());
        calendar.setTimeInMillis(lastTime);
        calendarView.setCurrentDate(calendar);
        List<Date> list = new ArrayList<>();
        list.add(new Date(Utils.getDateZeroMilis(lastTime)-TimeZone.getDefault().getRawOffset()));
        SpecialDayDecorator decorator = new SpecialDayDecorator(list, Color.GREEN);
        calendarView.addDecorator(decorator);

        calendarView.setOnDateChangedListener(new OnDateSelectedListener(){
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                final long datetime = date.getCalendar().getTimeInMillis();
                if(specialDates.contains(new Date(datetime)) ||
                        datetime+TimeZone.getDefault().getRawOffset() < minDay || datetime > maxDay) {
                    Toast.makeText(getApplicationContext(),"本日不能预订",Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(calendarView.getContext());
                builder.setTitle("选择时间段");
                builder.setNegativeButton("上午", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendDate(datetime+Utils.hourMili*8);
                    }
                });
                builder.setPositiveButton("下午", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendDate(datetime+Utils.hourMili*16);
                    }
                });
                Dialog dialog = builder.create();
                dialog.show();
            }
        });

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                decorateNoServiceDate();
            }
        });
        thread.start();
    }

    private void decorateNoServiceDate(){
        String urlStr = getString(R.string.server_host)+"/getAllSpecialDate.do";
        String errMsg = "";
        Bundle bundle = new Bundle();
        try{
            JSONObject json = HttpUtils.requestJson(this,urlStr,null,false);
            if(json.getInt("status") != 0)
                throw new Exception(json.getString("message"));
            JSONArray jsonArray = json.getJSONArray("data");
            ArrayList<String> list = new ArrayList<>();
            for(int i=0; i < jsonArray.length(); i++){
                String date = jsonArray.getJSONObject(i).getString("time");
                list.add(date);
            }
            bundle.putStringArrayList("list",list);
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
            ArrayList<String> list = msg.getData().getStringArrayList("list");
            specialDates = new ArrayList<>();
            for(String v: list)
                specialDates.add(Utils.parseDate(v));
            calendarView.addDecorator(new SpecialDayDecorator(specialDates));
        }
    };

    private void sendDate(long datetime){
        Intent intent = new Intent();
        intent.putExtra("date",datetime);
        setResult(RESULT_OK, intent);
        finish();
    }
}
