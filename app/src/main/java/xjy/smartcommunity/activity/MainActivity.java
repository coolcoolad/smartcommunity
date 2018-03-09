package xjy.smartcommunity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import xjy.smartcommunity.R;
import xjy.smartcommunity.util.SharedPrefUtils;

public class MainActivity extends AppCompatActivity {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:toPersonalInfo();return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.main_icon);
    }

    public void dangJianBtnOnClick(View view){
        Intent intent = new Intent(this, DangJianActivity.class);
        startActivity(intent);
    }

    public void tiJianBtnOnClick(View view) {
        Intent intent = new Intent(this, TiJianActivity.class);
        startActivity(intent);
    }

    public void wuYeBtnOnClick(View view) {
        Intent intent = new Intent(this, WuYeActivity.class);
        startActivity(intent);
    }

    private void toPersonalInfo(){
        Intent intent = new Intent(this, PersonalInfoActivity.class);
        startActivity(intent);
    }

    public void logoutBtnOnClick(View view) {
        SharedPrefUtils.putValToSP(this,SharedPrefUtils.sessionId,"");
        this.finish();
    }
}
