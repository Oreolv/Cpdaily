package com.xinyu.cumtcpdaily;

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
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.TimeZone;

public class MyService extends Service{
    public static final String CHANNEL_ID = "com.xinyu.CumtCpdaily.MyService";
    public static final String CHANNEL_NAME = "com.xinyu.CumtCpdaily";
    private String SP_username = "sp_username";
    private String SP_password = "sp_password";
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
                .setContentText("一键打卡定时任务正在后台运行")
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_HIGH);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mBuilder.setContentTitle(getResources().getString(R.string.app_name));
        }
        startForeground(notifyId, mBuilder.build());
        startMyThread();
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
                        Thread.sleep(1000*30*60); //每30分刷新一次
                        ifsign();
                        ifsubmit();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    startForeground(notifyId, mBuilder.build());
                }
            }
        };
        dateFlash.start();
    }
    //submit
    private void ifsubmit (){
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String username = pref.getString(SP_username, "");
        String password = pref.getString(SP_password, "");
        minute = cal.get(Calendar.MINUTE);
        if (cal.get(Calendar.AM_PM) == 0)
            hour = cal.get(Calendar.HOUR);
        else
            hour = cal.get(Calendar.HOUR)+12;
        if(hour==12 | hour ==13){
            mainActivity.senda(username,password,"submit");
            System.out.println("submit");
        }
    }
    //sign
    private void ifsign(){
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        minute = cal.get(Calendar.MINUTE);
        String username = pref.getString(SP_username, "");
        String password = pref.getString(SP_password, "");
        if (cal.get(Calendar.AM_PM) == 0)
            hour = cal.get(Calendar.HOUR);
        else
            hour = cal.get(Calendar.HOUR)+12;
        if(hour==21&&minute>30){
            mainActivity.senda(username,password,"sign");
            System.out.println("sign");
        }
        if (hour==22){
            mainActivity.senda(username,password,"sign");
            System.out.println("sign");
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