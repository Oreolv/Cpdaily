package com.xinyu.cumtnetwork;

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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class About extends AppCompatActivity {
    private Intent intent = getIntent();
    private Boolean isBk;
    private Button startButton;
    private Switch autosendSwitch,forbidenSwitch,foreverSwitch;
    private String SP_remember_bk = "sp_remember_bk";
    private String SP_remember_as = "sp_remember_as";
    private String SP_remember_fb = "sp_remember_fb";
    private String SP_remember_fe = "sp_remember_fe";
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
        isBk = pref.getBoolean(SP_remember_bk, false);
        startButton = (Button) findViewById(R.id.start);
        autosendSwitch = (Switch) findViewById(R.id.autosend);
        autosendSwitch.setChecked(pref.getBoolean(SP_remember_as,false));
        forbidenSwitch = (Switch) findViewById(R.id.forbiden);
        forbidenSwitch.setChecked(pref.getBoolean(SP_remember_fb,false));
        foreverSwitch = (Switch) findViewById(R.id.forever);
        foreverSwitch.setChecked(pref.getBoolean(SP_remember_fe,false));

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
        if (isBk){
            startButton.setText("关闭以上功能");
        }else{
            startButton.setText("开启以上功能");
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor = pref.edit();
                if(startButton.getText()=="开启以上功能"){
                    new AlertDialog.Builder(About.this)
                            .setTitle("提示")
                            .setMessage("此选项会开启后台服务,在通知栏显示\n请将本程序加入白名单,并且请勿退出后台程序")
                            .setPositiveButton("我知道啦",null)
                            .show();
                    editor.putBoolean(SP_remember_bk, true);
                    Intent intent1 = new Intent(About.this,MyService.class);
                    intent1.putExtra("isAutosend",autosendSwitch.isChecked());
                    System.out.println(autosendSwitch.isChecked());
                    intent1.putExtra("isForbiden",forbidenSwitch.isChecked());
                    intent1.putExtra("isForever",foreverSwitch.isChecked());
                    startService(intent1);
                    startButton.setText("关闭以上功能");

                    editor.putBoolean(SP_remember_as, autosendSwitch.isChecked());
                    editor.putBoolean(SP_remember_fb, forbidenSwitch.isChecked());
                    editor.putBoolean(SP_remember_fe, foreverSwitch.isChecked());
                }else{
                    editor.putBoolean(SP_remember_bk, false);
                    editor.apply();
                    Intent intent2 = new Intent(About.this,MyService.class);
                    stopService(intent2);
                    startButton.setText("开启以上功能");
                }
                editor.commit();
            }
        });
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
//        intent1.putExtra("sendChecked",isStart);
        setResult(200,intent1);
        finish();
    }


}