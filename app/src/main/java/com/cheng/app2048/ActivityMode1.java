package com.cheng.app2048;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cheng.app2048.view.OnEventListener;
import com.cheng.app2048.view.View2048;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

public class ActivityMode1 extends AppCompatActivity implements RewardedVideoAdListener, OnEventListener, View.OnClickListener {
    private View2048 view2048;
    private TextView tvScore;
    private TextView tvHighest;
    private ImageView cbSound;
    private String gameMode;
    private RewardedVideoAd mRewardedVideoAd;
    private int revokeCounts;
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
    private static final String APP_ID = "ca-app-pub-3940256099942544~3347511713";
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2048);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }

        MobileAds.initialize(this, APP_ID);
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

        view2048 = findViewById(R.id.view2048);
        gameMode = getIntent().getStringExtra(MainActivity.GAME_MODE);
        view2048.setGameMode(gameMode);
        view2048.addEventListener(this);
        tvScore = findViewById(R.id.tv_score);
        cbSound = findViewById(R.id.iv_sound);
        cbSound.setOnClickListener(this);
        tvHighest = findViewById(R.id.tv_highest);
        LinearLayout llGame = findViewById(R.id.ll_game);
        if (gameMode.equals(MainActivity.GAME_MODE_CLASSIC) || gameMode.equals(MainActivity.GAME_MODE_PROP_4X4)) {
            llGame.setPadding(llGame.getPaddingTop() * 3, llGame.getPaddingTop(), llGame.getPaddingTop() * 3, llGame.getPaddingBottom());
        }

        if (view2048.isContinueGame()) {
            view2048.continueGame();
            tvScore.setText("分数\n" + view2048.getScore());
        } else if (gameMode != null) {
            switch (gameMode) {
                case MainActivity.GAME_MODE_CLASSIC:
                    view2048.setStructure(4, 4, 0);
                    break;
                case MainActivity.GAME_MODE_CHALLENGE:
                    view2048.setStructure(10, 9, 0);
                    break;
                case MainActivity.GAME_MODE_PROP_4X4:
                    view2048.setStructure(4, 4, 1);
                    break;
                case MainActivity.GAME_MODE_PROP_10X9:
                    view2048.setStructure(10, 9, 3);
                    break;
            }
        }
        cbSound.setImageResource(view2048.isPlaySound() ? R.mipmap.sound_on : R.mipmap.sound_off);
        tvHighest.setText("最高分\n" + view2048.getHighestScore());
    }

    public void revoke(View view) {
        if (revokeCounts < 3) {
            if (view2048.revoke()) {
                revokeCounts++;
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提示：").
                        setMessage("最多回退到前三步。").
                        show();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示：").
                    setMessage("是否要观看广告增加回退次数？").
                    setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showAd();
                        }
                    }).
                    setNegativeButton("下次吧", null);
            builder.show();
        }
    }

    public void newGame(View view) {
        view2048.newGame();
        tvScore.setText("分数\n" + 0);
        revokeCounts = 0;
    }

    @Override
    public void scoreListener(int everyScore) {
        tvScore.setText("分数\n" + everyScore);
    }

    @Override
    public void highestListener(int highestScore) {
        tvHighest.setText("最高分\n" + highestScore);
    }

    @Override
    public void gameOver() {
        Toast.makeText(ActivityMode1.this, "game over", Toast.LENGTH_SHORT).show();
        if (dialog == null) {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_gameover, null);
            dialogView.findViewById(R.id.tv_newgame).setOnClickListener(this);
            dialogView.findViewById(R.id.tv_backmenu).setOnClickListener(this);
            dialog = new Dialog(this);
            dialog.setContentView(dialogView);
        } else {
            if (!dialog.isShowing()) {
                dialog.show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_sound:
                if (dialog == null) {
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_gameover, null);
                    dialogView.findViewById(R.id.tv_newgame).setOnClickListener(this);
                    dialogView.findViewById(R.id.tv_backmenu).setOnClickListener(this);
                    dialog = new Dialog(this);
                    dialog.setContentView(dialogView);
                } else {
                    if (!dialog.isShowing()) {
                        dialog.show();
                    }
                }
//                view2048.setPlaySound(!view2048.isPlaySound());
//                cbSound.setImageResource(view2048.isPlaySound() ? R.mipmap.sound_on : R.mipmap.sound_off);
                break;
            case R.id.tv_newgame:
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                newGame(v);
                break;
            case R.id.tv_backmenu:
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                finish();
                break;
        }
    }

    private void loadRewardedVideoAd() {
        if (!mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.loadAd(AD_UNIT_ID,
                    new AdRequest.Builder().build());
        }
    }

    public void showAd() {
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示：").setMessage("广告还没有准备好，请等一会吧(*^_^*)").show();
        }
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Toast.makeText(this, "onRewardedVideoAdLeftApplication", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdClosed() {
        revokeCounts = 0;
        Toast.makeText(this, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
        // Preload the next video ad.
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        Toast.makeText(this, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show();
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        Toast.makeText(this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdOpened() {
        Toast.makeText(this, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewarded(RewardItem reward) {
        Toast.makeText(this,
                String.format(" onRewarded! currency: %s amount: %d", reward.getType(),
                        reward.getAmount()),
                Toast.LENGTH_SHORT).show();
//        addCoins(reward.getAmount());
    }

    @Override
    public void onRewardedVideoStarted() {
        Toast.makeText(this, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoCompleted() {
        Toast.makeText(this, "onRewardedVideoCompleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRewardedVideoAd.resume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mRewardedVideoAd.pause(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRewardedVideoAd.destroy(this);
    }
}
