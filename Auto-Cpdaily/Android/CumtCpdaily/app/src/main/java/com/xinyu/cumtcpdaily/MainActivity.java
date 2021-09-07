package com.xinyu.cumtcpdaily;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import static android.widget.Toast.LENGTH_LONG;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private CheckBox rememberBox;
    private EditText usernameText, passwordText, addressText;
    private String TAG = "message";
    private String SP_username = "sp_username";
    private String SP_password = "sp_password";
    private String SP_address = "sp_address";
    private String SP_school = "0";
    private String SP_remember_exit = "sp_remember_exit";
    private String SP_remember_send = "sp_remember_send";
    private String SP_remember_pwd = "sp_remember_psd";
    private Button sendButton;

    private RadioButton submitRadio;
    private RadioButton signRadio;
    private Boolean exitChecked;
    private Boolean sendChecked;
    private TextView activateText;
    private Spinner schoolSpinner;
    private ProgressBar progressBar;
    private String type;
    boolean isRemember,isAutoexit,isAutosend;
    String line;
    private ArrayAdapter<String> adapter;
    private static final String[] spinnerList={"是","否"};

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

        activateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://authserver.cumt.edu.cn/authserver/login?service=http%3A//portal.cumt.edu.cn/casservice");
                intent.setData(content_url);
                startActivity(intent);
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (submitRadio.isChecked()) {
                    type = "submit";
                } else {
                    type = "sign";
                }
                send(type);
            }
        });
        //
        if (isRemember) {
            String username = pref.getString(SP_username, "");
            String password = pref.getString(SP_password, "");
            String address = pref.getString(SP_address, "");
            Long school = pref.getLong(SP_school,0);
            usernameText.setText(username);
            passwordText.setText(password);
            schoolSpinner.setSelection(Math.toIntExact(school),true);
            addressText.setText(address);
            rememberBox.setChecked(true);
        }

        if(isAutoexit){
            exitChecked = true;
        }else{
            exitChecked = false;
        }
        System.out.println(sendChecked);
        if(isAutosend){
            Intent intent3 = new Intent(MainActivity.this,MyService.class);
            startService(intent3);
        }

    }

    //初始化
    private void init(){

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        usernameText = (EditText) findViewById(R.id.username);
        passwordText = (EditText) findViewById(R.id.password);
        addressText = (EditText) findViewById(R.id.address);
        rememberBox = (CheckBox) findViewById(R.id.remember);

        schoolSpinner = (Spinner) findViewById(R.id.schoolSpinner);

        sendButton = (Button) findViewById(R.id.send);

        submitRadio = (RadioButton) findViewById(R.id.submit);
        signRadio = (RadioButton) findViewById(R.id.sign);
        progressBar = (ProgressBar) findViewById(R.id.progess);

        activateText = (TextView) findViewById(R.id.activate);

        isRemember = pref.getBoolean(SP_remember_pwd, false);
        isAutoexit = pref.getBoolean(SP_remember_exit, false);
        isAutosend = pref.getBoolean(SP_remember_send, false);

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,spinnerList);

        //设置下拉列表的风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        schoolSpinner.setAdapter(adapter);
        //添加事件Spinner事件监听
//        schoolSpinner.setOnItemSelectedListener(new SpinnerSelectedListener());
        //设置默认值
        schoolSpinner.setVisibility(View.VISIBLE);
    }

    public void senda(JSONObject params,String type){
        try {
            String line;
            String path = "http://cpdaily.xinyu.ink/" + type;
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json");
            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = params.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            InputStream is = conn.getInputStream();//获取输入流
            InputStreamReader isr = new InputStreamReader(is, "utf-8");//字节转字符，字符集是utf-8
            BufferedReader bufferedReader = new BufferedReader(isr);//通过BufferedReader可以读取一行字符串

            while ((line = bufferedReader.readLine()) != null) {
//                Toast.makeText(MainActivity.this, line, LENGTH_LONG).show();
                System.out.println("get"+type);
            }
            bufferedReader.close();
            isr.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
//            Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            System.out.println("error");
        }
    }


    public void send(String type) {
        progressBar.setVisibility(View.VISIBLE);
        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();
        String address = addressText.getText().toString();
        String school = schoolSpinner.getSelectedItem().toString();
        System.out.println(school);
        Long schoolID = schoolSpinner.getSelectedItemId();
        editor = pref.edit();
        if (rememberBox.isChecked()) {
            editor.putString(SP_username, username);
            editor.putString(SP_password, password);
            editor.putString(SP_address, address);
            editor.putLong(SP_school,schoolID);
            editor.putBoolean(SP_remember_pwd, true);
        } else {
            editor.apply();
        }
        editor.commit();
        //POST访问
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = "http://127.0.0.1:7920/" + type;
                    JSONObject params = new JSONObject();
                    params.put("username", username);
                    params.put("password", password);
                    params.put("school", school);
                    params.put("address", address);
                    URL url = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(5000);
                    conn.setRequestProperty("Content-Type", "application/json");
                    // POST请求
                    try(OutputStream os = conn.getOutputStream()) {
                        byte[] input = params.toString().getBytes("utf-8");
                        System.out.println(params);
                        os.write(input, 0, input.length);
                    }
                    InputStream is = conn.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf-8");
                    BufferedReader bufferedReader = new BufferedReader(isr);
                    //获取网页内容
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
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
        //bundle.putString("abc", "bbb");
        //要求目标页面传数据回来
        startActivityForResult(intent,100);
    }
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            progressBar.setVisibility(View.GONE);
            switch (msg.what) {
                case 0:
                    System.out.println(line);
                    Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                    break;


            }
            if(exitChecked){
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.exit(0);
                    }
                }, 2000);//2秒后执行Runnable中的run方法
            }

        }
    };
    //会自动接收目标页面回传的值
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == 200 ){
            exitChecked = data.getBooleanExtra("exitChecked",false);
            sendChecked = data.getBooleanExtra("sendChecked",false);
        }
    }
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