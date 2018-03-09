package xjy.smartcommunity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;

import xjy.smartcommunity.R;
import xjy.smartcommunity.util.HttpUtils;

public class DangJianDetailActivity extends AppCompatActivity implements View.OnTouchListener, GestureDetector.OnGestureListener {

    private GestureDetector detector = new GestureDetector(this);
    private TextView titleTv = null;
    private TextView contentTv = null;
    private String[] pages = null;
    private int curPage = 0;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_jian_detail);

        titleTv = (TextView) findViewById(R.id.titleView);
        contentTv = (TextView) findViewById(R.id.contentView);
        contentTv.setOnTouchListener(this);
        contentTv.setClickable(true);
        detector.setIsLongpressEnabled(true);

        Intent intent = getIntent();
        pages = intent.getStringArrayExtra("pages");
        if(pages != null) {
            String title = intent.getStringExtra("title");
            titleTv.setText(title);
            curPage = intent.getIntExtra("curPage", 0);
            contentTv.setText(pages[curPage]);

        } else {
            final String idStr = intent.getStringExtra("id");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    fillDetail(idStr);
                }
            });
            thread.start();
        }
    }

    private void fillDetail(String idStr) {
        String urlStr = getString(R.string.server_host) + "/getDangJianDetailById.do";
        HashMap<String, String> paraMap = new HashMap<>();
        paraMap.put("id", idStr);
        String errMsg = "";
        Bundle bundle = new Bundle();
        try {
            JSONObject json = HttpUtils.requestJson(this, urlStr, paraMap, false);
            if (json.getInt("status") != 0)
                throw new Exception(json.getString("message"));
            JSONObject data = json.getJSONObject("data");
            bundle.putString("title", data.getString("title"));
            bundle.putString("content", data.getString("context"));
            bundle.putString("time", data.getString("time"));
        } catch (Exception ex) {
            errMsg = ex.getMessage();
            if (errMsg == null || errMsg.length() == 0)
                errMsg = "请求失败";
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
            String errMsg = msg.getData().getString("errMsg");
            if (errMsg.length() > 0) {
                Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_SHORT).show();
                return;
            }
            TextView titleView = (TextView) findViewById(R.id.titleView);
            titleView.setText(msg.getData().getString("title"));
            contentTv.setText(msg.getData().getString("content"));
        }
    };

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {}

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {}

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if(pages == null) {
            int height = contentTv.getHeight();
            int lineH = contentTv.getLineHeight();
            int linePer = height/lineH;
            Layout layout = contentTv.getLayout();
            int lineAll = layout.getLineCount();
            int pageNum = lineAll/linePer+(lineAll%linePer==0?0:1);
            pages = new String[pageNum];
            String content = contentTv.getText().toString();
            for(int i=0; i < pages.length; i++){
                int startLine = i*linePer;
                int endLine = Math.min(startLine+linePer-1,lineAll-1);
                int start = layout.getLineStart(startLine);
                int end = layout.getLineEnd(endLine);
                pages[i] = content.substring(start, end);
            }
        }
        int verticalMinDistance = 10;
        int minVelocity = 0;
        if (e1.getX() - e2.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity) {
            //Toast.makeText(this, "向左手势", Toast.LENGTH_SHORT).show();
            if(curPage+1 < pages.length) {
                curPage++;
                goToNextPage();
            }
        } else if (e2.getX() - e1.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity) {
            //Toast.makeText(this, "向右手势", Toast.LENGTH_SHORT).show();
            if(curPage-1 >= 0){
                curPage--;
                goToNextPage();
            }
        }
        return false;
    }

    private void goToNextPage(){
        Intent intent = new Intent(this,DangJianDetailActivity.class);
        intent.putExtra("title",titleTv.getText().toString());
        intent.putExtra("pages",pages);
        intent.putExtra("curPage",curPage);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        detector.onTouchEvent(event);
        return false;
    }
}
