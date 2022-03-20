package com.voc.tablepet.ui;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.voc.tablepet.R;
import com.voc.tablepet.util.FloatingService;


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

public class MainActivity extends AppCompatActivity {

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(MainActivity.this,FloatingService.class);
    }

    public void startFloatingService(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "請先授權使用懸浮窗", Toast.LENGTH_SHORT);
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 2048);
            } else {
                startService(intent);
            }
        }
    }
    public void stopFloatingService(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "請先授權使用懸浮窗", Toast.LENGTH_SHORT);
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 2047);
            } else {
                stopService(intent);
            }
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2048) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "授權失敗", Toast.LENGTH_SHORT).show();
            } else {
                startService(intent);
                sendBroadcast(intent);
            }
        }else if (requestCode == 2047){
            stopService(intent);
        }
    }
}