package com.xinyu.cumtnetwork;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.TimeZone;

public class MyService extends Service{
    public static final String CHANNEL_ID = "com.xinyu.CumtNetwork.MyService";
    public static final String CHANNEL_NAME = "com.xinyu.CumtNetwork";
    private String SP_username = "sp_username";
    private String SP_password = "sp_password";
    private String SP_remember_yys = "sp_remember_yys";
    private Boolean isAutosend,isForbiden,isForever;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private int notifyId = (int) System.currentTimeMillis();
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
    boolean stopThread = false;
    int minute,hour;
    MainActivity mainActivity = new MainActivity();
    Calendar cal = Calendar.getInstance();

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        registerNotificationChannel();
        mBuilder.setSmallIcon(R.mipmap.logo_cumtcpdaily)
//                .setContentTitle("定时任务") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.logo_cumtcpdaily) // 设置状态栏内的小图标
                .setContentText("校园网任务正在后台运行")
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_HIGH);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mBuilder.setContentTitle(getResources().getString(R.string.app_name));
        }
        startForeground(notifyId, mBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isAutosend = intent.getBooleanExtra("isAutosend",false);
        isForbiden = intent.getBooleanExtra("isForbiden",false);
        isForever = intent.getBooleanExtra("isForever",false);
        startMyThread();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        stopThread = true;
    }

    // 主线程
    private void startMyThread() {
        stopThread=false;
        Thread dateFlash=new Thread() {
            @Override
            public void run() {
                super.run();
                while (!stopThread) {
                    try {
                        Thread.sleep(1000*60*2); //每2分刷新一次
                        if(isAutosend){
                            autoLogin();
                        }
                        if(isForbiden){
                            forbiden();
                        }
                        if(isForever){
                            forever();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    startForeground(notifyId, mBuilder.build());
                }
            }
        };
        dateFlash.start();
    }

    private void login(){
        String username = pref.getString(SP_username, "");
        String password = pref.getString(SP_password, "");
        int yys = (int) pref.getLong(SP_remember_yys,0);
        try {
            String path = "http://10.2.5.251:801/eportal/?c=Portal&a=login&login_method=1&user_account=" + username + yys + "&user_password=" + password;
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(isr);
            bufferedReader.close();
            isr.close();
            is.close();

        } catch (Exception e) {}
    }

    public static boolean isOnline(){
        URL url;
        try {
            url = new URL("https://www.baidu.com");
            InputStream stream = url.openStream();
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void autoLogin (){
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        minute = cal.get(Calendar.MINUTE);
        if (cal.get(Calendar.AM_PM) == 0)
            hour = cal.get(Calendar.HOUR);
        else
            hour = cal.get(Calendar.HOUR)+12;

        while(hour==6&&(minute>30||minute<40)){
            login();
        }
    }

    private void forever(){
        if(!isOnline()){
            login();
        }
    }

    private void forbiden(){
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        minute = cal.get(Calendar.MINUTE);
        if (cal.get(Calendar.AM_PM) == 0)
            hour = cal.get(Calendar.HOUR);
        else
            hour = cal.get(Calendar.HOUR)+12;

        while(hour==23 &&(minute>20||minute<30)){
            String packageName = "com.legendsec.sslvpn";
            try{
                Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
                startActivity(intent);
                System.out.println("aa");
            }catch (Exception e){
                Toast.makeText(this,"aaa",Toast.LENGTH_LONG);
            }
        }
    }

    //注册通知通道
    private void registerNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = mNotificationManager.getNotificationChannel(CHANNEL_ID);
            if (notificationChannel == null) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                //是否在桌面icon右上角展示小红点
                channel.enableLights(true);
                //小红点颜色
                channel.setLightColor(Color.RED);
                //通知显示
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                //是否在久按桌面图标时显示此渠道的通知
                channel.setShowBadge(true);
                mNotificationManager.createNotificationChannel(channel);
            }
        }
    }
}