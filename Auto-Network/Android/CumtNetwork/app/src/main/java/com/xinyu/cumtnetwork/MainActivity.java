package com.xinyu.cumtnetwork;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private CheckBox rememberBox;
    private EditText usernameText, passwordText;
    private String TAG = "message";
    private String SP_username = "sp_username";
    private String SP_password = "sp_password";
    private String SP_remember_pwd = "sp_remember_psd";
    private String SP_remember_send = "sp_remember_send";
    private String SP_remember_yys = "sp_remember_yys";

    private ProgressBar progressBar;
    private Spinner spinner;
    private Button loginButton;
    private Button logoutButton;
    boolean isRemember,isAutosend;
    String line,yys,remsg;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //网络访问
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //初始化
        init();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String path = "http://10.2.5.251:801/eportal/?c=Portal&a=logout&login_method=1&user_account=drcom&user_password=123&ac_logout=0";
                    URL url = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    InputStream is = conn.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf-8");
                    BufferedReader bufferedReader = new BufferedReader(isr);
                    //获取网页内容
                    while ((line = bufferedReader.readLine()) != null) {
                        Toast.makeText(MainActivity.this, "注销成功", Toast.LENGTH_SHORT).show();
                    }
                    bufferedReader.close();
                    isr.close();
                    is.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                }
            }

        });

        //
        if (isRemember) {
            String username = pref.getString(SP_username, "");
            String password = pref.getString(SP_password, "");
            int yys = (int) pref.getLong(SP_remember_yys,0);
            usernameText.setText(username);
            passwordText.setText(password);
            rememberBox.setChecked(true);
            spinner.setSelection(yys,true);
        }

        if(isAutosend){
            Intent intent3 = new Intent(MainActivity.this,MyService.class);
            startService(intent3);
        }

    }
    //初始化
    private void init(){
        //
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        usernameText = (EditText) findViewById(R.id.username);
        passwordText = (EditText) findViewById(R.id.password);
        rememberBox = (CheckBox) findViewById(R.id.remember);
        spinner = (Spinner) findViewById(R.id.yys);
        loginButton = (Button) findViewById(R.id.login);
        logoutButton = (Button) findViewById(R.id.logout);
        progressBar = (ProgressBar) findViewById(R.id.progess);

        isRemember = pref.getBoolean(SP_remember_pwd, false);
        isAutosend = pref.getBoolean(SP_remember_send, false);

    }

    public void send() {
        progressBar.setVisibility(View.VISIBLE);
        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();


        editor = pref.edit();
        if (rememberBox.isChecked()) {
            editor.putString(SP_username, username);
            editor.putString(SP_password, password);
            editor.putBoolean(SP_remember_pwd, true);
            editor.putLong(SP_remember_yys,spinner.getSelectedItemId());
        } else {
            editor.apply();
        }
        editor.commit();
        switch ((int) spinner.getSelectedItemId()) {
            case 0:
                break;
            case 1:
                yys = "%40cmcc";
                break;
            case 2:
                yys = "%40unicom";
                break;
            case 3:
                yys = "%40telecom";
                break;
        }
        //POST访问
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = "http://10.2.5.251:801/eportal/?c=Portal&a=login&login_method=1&user_account=" + username + yys + "&user_password=" + password;
                    URL url = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    InputStream is = conn.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf-8");
                    BufferedReader bufferedReader = new BufferedReader(isr);
                    //获取网页内容
                    while ((line = bufferedReader.readLine()) != null) {
                        Message message = new Message();
                        message.what = 0;
                        message.obj = line;
                        handler.sendMessage(message);
                    }
                    bufferedReader.close();
                    isr.close();
                    is.close();

                } catch (Exception e) {
                    e.printStackTrace();
//                    Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }

            }
        }).start();

    }
    public void setting(){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, About.class);
        Bundle bundle = new Bundle();
        intent.putExtras(bundle);
        startActivityForResult(intent,100);
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            progressBar.setVisibility(View.GONE);
            remsg = msg.obj.toString();
            switch (msg.what) {
                case 0:
                    if(remsg.contains("认证成功")){
                        Toast.makeText(MainActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                    }else if(remsg.contains("\"ret_code\":\"2\"")){
                        Toast.makeText(MainActivity.this, "请勿重复登陆！", Toast.LENGTH_SHORT).show();
                    }else if(remsg.contains("dXNlcmlkIGVycm9yMQ")){
                        Toast.makeText(MainActivity.this, "登录失败，请检查账号密码", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "其他错误", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 1:
                    Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_setting:
                setting();
                break;
            default:
                break;
        }
        return true;
    }

}