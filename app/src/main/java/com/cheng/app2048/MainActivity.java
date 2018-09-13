package com.cheng.app2048;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String GAME_MODE = "gameMmode";
    public static final String GAME_MODE_CLASSIC = "ClassicMode";
    public static final String GAME_MODE_CHALLENGE = "ChallengeMode";
    public static final String GAME_MODE_PROP_4X4_FIXED = "PropMode_4x4_fixed";
    public static final String GAME_MODE_PROP_4X4 = "PropMode_4x4";
    public static final String GAME_MODE_PROP_10X9_FIXED = "PropMode_10x9_fixed";
    public static final String GAME_MODE_PROP_10X9 = "PropMode_10x9";
    private final String APP_ID = "ca-app-pub-7365304655459838~5623471706";
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
        MobileAds.initialize(this, APP_ID);
        mAdView = findViewById(R.id.adView);
        loadBannerAd();
    }

    private void loadBannerAd() {
        if (!mAdView.isLoading()) {
            mAdView.loadAd(new AdRequest.Builder().build());
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadBannerAd();
    }

    public void mode1(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GAME_MODE, GAME_MODE_CLASSIC);
        startActivity(intent);
    }

    private Dialog dialog;

    public void mode2(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GAME_MODE, GAME_MODE_CHALLENGE);
        startActivity(intent);
    }

    public void mode3(View view) {
        showDialog();
    }

    private void showDialog() {
        if (dialog == null) {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_mode, null);
            dialogView.findViewById(R.id.tv_44_fixed).setOnClickListener(this);
            dialogView.findViewById(R.id.tv_44).setOnClickListener(this);
            dialogView.findViewById(R.id.tv_109_fixed).setOnClickListener(this);
            dialogView.findViewById(R.id.tv_109).setOnClickListener(this);
            dialog = new Dialog(this);
            Context context = dialog.getContext();
            //去掉低版本dialog上部的蓝色分割线
            try {
                int dividerID = context.getResources().getIdentifier("android:id/titleDivider", null, null);
                View divider = dialog.findViewById(dividerID);
                divider.setBackgroundColor(Color.TRANSPARENT);
            } catch (Exception e) {
                e.printStackTrace();
            }
            dialog.setContentView(dialogView);
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(R.color.transparent);
            }
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, GameActivity.class);
        switch (v.getId()) {
            case R.id.tv_44_fixed:
                intent.putExtra(GAME_MODE, GAME_MODE_PROP_4X4_FIXED);
                startActivity(intent);
                dialog.dismiss();
                break;
            case R.id.tv_44:
                intent.putExtra(GAME_MODE, GAME_MODE_PROP_4X4);
                startActivity(intent);
                dialog.dismiss();
                break;
            case R.id.tv_109_fixed:
                intent.putExtra(GAME_MODE, GAME_MODE_PROP_10X9_FIXED);
                startActivity(intent);
                dialog.dismiss();
                break;
            case R.id.tv_109:
                intent.putExtra(GAME_MODE, GAME_MODE_PROP_10X9);
                startActivity(intent);
                dialog.dismiss();
                break;
            default:
                break;
        }
    }

    private long mPressedTime = 0;

    @Override
    public void onBackPressed() {
        long mNowTime = System.currentTimeMillis();
        if ((mNowTime - mPressedTime) > 2000) {
            Toast.makeText(this, R.string.tip_exit, Toast.LENGTH_SHORT).show();
            mPressedTime = mNowTime;
        } else {//退出程序
            this.finish();
            System.exit(0);
        }
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    /**
     * 关于
     *
     * @param view
     */
    public void about(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }
}
