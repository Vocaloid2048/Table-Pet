package com.voc.tablepet.util;

import static android.content.Context.WINDOW_SERVICE;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.squareup.picasso.Picasso;
import com.voc.tablepet.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Project "Genshin Spirit" (原神小幫手) was
 * Created & Develop by Voc-夜芷冰 , Programmer of Xectorda
 * Copyright © 2022 Xectorda 版權所有
 */

/**
 * Idea from :
 * https://www.bilibili.com/video/BV1Ha41187LP
 * Reference article :
 * https://blog.csdn.net/dongzhong1990/article/details/80512706
 */

public class FloatingService extends Service {
    //public static boolean isStarted = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private View displayView;

    private GifImageView gifImageView;
    private Context context;

    private int x;
    private int y;
    private long waiting_sec = 0;

    private String[] afkWaitList = new String[]{"ss.gif"};
    private String[] afkMoveList = new String[]{"s1.gif","s2.gif","runL1.gif","runL2.gif","backR1.gif","backR2.gif","backR1.gif","backR2.gif"};
    private String[]  actionMoveList= new String[]{"drag1.gif","drag2.gif"};

    private String AFK_WAIT = "AFK_WAIT";
    private String AFK_STAY = "AFK_STAY";
    private String ACT_MOVE = "ACT_MOVE";

    private Handler handler;
    int randSec = 0;
    boolean isDisplay = false;

    int loopCnt = 0;
    int randLoop = 0;

    int xMove = 0;
    int yMove = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        /**
         * init
         */
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = 400;
        layoutParams.height = 400;
        layoutParams.x = 300;
        layoutParams.y = 300;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this) && isDisplay == false) {
            isDisplay = true;

            LayoutInflater layoutInflater = LayoutInflater.from(this);
            displayView = layoutInflater.inflate(R.layout.image_display, null);
            gifImageView = (GifImageView) displayView.findViewById(R.id.global_bg);
            handler = new Handler();
            windowManager.addView(displayView, layoutParams);

            gifRand(AFK_STAY);

            displayView.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    displayView = view;
                    handler.removeCallbacks(runnable);
                    switch (event.getAction()) {

                        // Let character back to afk mode
                        case MotionEvent.ACTION_DOWN:
                            x = (int) event.getRawX();
                            y = (int) event.getRawY();
                            gifRand(AFK_STAY);

                            if(windowManager != null){
                                windowManager.updateViewLayout(displayView, layoutParams);
                            }
                            break;

                        case MotionEvent.ACTION_MOVE:
                            int nowX = (int) event.getRawX();
                            int nowY = (int) event.getRawY();
                            int movedX = nowX - x;
                            int movedY = nowY - y;
                            x = nowX;
                            y = nowY;
                            layoutParams.x = layoutParams.x + movedX;
                            layoutParams.y = layoutParams.y + movedY;
                            waiting_sec = 0;
                            gifRand(ACT_MOVE);
                            if(windowManager != null){
                                windowManager.updateViewLayout(displayView, layoutParams);
                            }
                            break;
                        default:
                            gifRand(AFK_WAIT);
                            if(windowManager != null){
                                windowManager.updateViewLayout(displayView, layoutParams);
                            }
                    }
                    return false;
                }
            });

            //eventManager.setup(context,windowManager,layoutParams,gifImageView,displayView);


        }
    }

    public void setGifImageByName(String fileName, boolean isRand) {
        System.out.println(fileName);
        handler.removeCallbacks(runnable);
        GifDrawable gifFromFile = null;
        try {
            InputStream stream = context.getAssets().open(fileName);
            gifFromFile = new GifDrawable(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /**
         *     + --> x
         *     |
         *     |
         *     v
         *     y
         */

        gifImageView.setImageDrawable(gifFromFile);

        switch (fileName){
            case "backL1.gif" :
            case "backL2.gif" : {
                xMove = -20;
                yMove = -20;
                break;
            }
            case "backR1.gif" :
            case "backR2.gif" : {
                xMove = +20;
                yMove = -20;
                break;
            }
            case "runL1.gif" :
            case "runL2.gif" : {
                xMove = -20;
                yMove = +20;
                break;
            }
            case "runR1.gif" :
            case "runR2.gif" : {
                xMove = +20;
                yMove = +20;
                break;
            }
            default:{
                xMove = 0;
                yMove = 0;
                break;
            }
        }


        if(isRand){
            randLoop = (int) (Math.random() * 150+50);
            handler.postDelayed(runnable2, 10);
        }

        handler.postDelayed(runnable, 1000);
    }

    public void gifRand(String code){
        switch (code){
            case "AFK_WAIT": setGifImageByName(afkWaitList[0],true);break;
            case "AFK_STAY": setGifImageByName(afkMoveList[(int) Math.random()*(afkMoveList.length+1)],true);break;
            case "ACT_MOVE": setGifImageByName(actionMoveList[(int) Math.random()*(actionMoveList.length+1)],false);break;
        }

    }

    final Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            loopCnt = loopCnt +1;
            if (layoutParams.x - 100 > 0 && layoutParams.y - 100 > 0 && layoutParams.x + 100 < 1080 && layoutParams.y + 100 < 1920 ){
                layoutParams.x = layoutParams.x + xMove;
                layoutParams.y = layoutParams.y + yMove;
            }else {
                if(layoutParams.x - 100 < 0){
                    layoutParams.x = layoutParams.x + Math.abs(xMove);
                }else if(layoutParams.x + 100 >1080){
                    layoutParams.x = layoutParams.x - Math.abs(xMove);
                }

                if(layoutParams.y - 100 < 0){
                    layoutParams.y = layoutParams.y + Math.abs(yMove);
                }else if(layoutParams.y + 100 >1920){
                    layoutParams.y = layoutParams.y - Math.abs(yMove);
                }
            }

            if(windowManager != null){
                windowManager.updateViewLayout(displayView, layoutParams);
            }
            if(loopCnt < randLoop ){
                handler.postDelayed(runnable2, 100);
            }else{
                randLoop = 0;
                loopCnt = 0;
                handler.postDelayed(runnable,10);
            }

        }
    };

    final Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if(waiting_sec == 0){
                randSec = (int) (Math.random()*5)+5;
            }
            waiting_sec = waiting_sec+1;

            if(isDisplay != true){
                isDisplay = true;
                waiting_sec = 0;
                String randType = afkMoveList[(int) (Math.random()*afkMoveList.length)];
                setGifImageByName(randType,true);
            }else if(waiting_sec == randSec){
                waiting_sec = 0;
                String randType = afkMoveList[(int) (Math.random()*afkMoveList.length)];
                setGifImageByName(randType,true);
            }
            handler.postDelayed(runnable, 1000);

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (windowManager != null && displayView != null){
            windowManager.removeView(displayView);
        }
    }
}