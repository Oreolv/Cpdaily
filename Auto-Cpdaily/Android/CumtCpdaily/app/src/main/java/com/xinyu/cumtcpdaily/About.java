package com.xinyu.cumtcpdaily;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class About extends AppCompatActivity {
    private Intent intent = getIntent();;
    private Switch AutoExitSwitch;
    private Switch AutoSendSwitch;
    private String SP_remember_exit = "sp_remember_exit";
    private String SP_remember_send = "sp_remember_send";
    private SharedPreferences pref;
    private TextView QQqunText;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        
        AutoSendSwitch = (Switch) findViewById(R.id.autosend);
        AutoExitSwitch = (Switch) findViewById(R.id.autoexit);
        boolean isAutoexit = pref.getBoolean(SP_remember_exit, false);
        boolean isAutosend = pref.getBoolean(SP_remember_send, false);
        QQqunText = (TextView) findViewById(R.id.add);
        QQqunText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3DmC0n2_5pqyAOTsmcAndfDDYrh1Ug1_Vh"));
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(About.this," 未安装手Q或安装的版本不支持",Toast.LENGTH_SHORT);
                }
            }
        });

        AutoSendSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //remember
                editor = pref.edit();
                if (AutoSendSwitch.isChecked()) {
                    new AlertDialog.Builder(About.this)
                            .setTitle("提示")
                            .setMessage("12~13.00之间日报告,21.30~22.30之间晚打卡\n此选项会开启后台服务,在通知栏显示\n请将本程序加入白名单,并且请勿退出后台程序")
                            .setPositiveButton("我知道啦",null)
                            .show();
                    editor.putBoolean(SP_remember_send, true);
                    Intent intent1 = new Intent(About.this,MyService.class);
                    startService(intent1);
                } else {
                    editor.putBoolean(SP_remember_send, false);
                    editor.apply();
                    Intent intent2 = new Intent(About.this,MyService.class);
                    stopService(intent2);
                }
                editor.commit();
                //service

            }
        });

        AutoExitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor = pref.edit();
                if (AutoExitSwitch.isChecked()) {
                    editor.putBoolean(SP_remember_exit, true);
                } else {
                    editor.putBoolean(SP_remember_exit, false);
                    editor.apply();
                }
                editor.commit();
            }
        });
        if(isAutoexit){
            AutoExitSwitch.setChecked(true);
        }
        if(isAutosend){
            AutoSendSwitch.setChecked(true);
        }
    }

    //监听ActionBar返回键
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        if (item.getItemId() == android.R.id.home) {
            result();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //监听物理返回键
    @Override
    public void onBackPressed() {
        result();
        super.onBackPressed();
    }

    public void result(){
        Intent intent1 = new Intent(About.this,MainActivity.class);
        Boolean exitChecked = AutoExitSwitch.isChecked();
        Boolean sendChecked = AutoSendSwitch.isChecked();
        intent1.putExtra("exitChecked",exitChecked);
        intent1.putExtra("sendChecked",exitChecked);
        setResult(200,intent1);
        finish();
    }
}