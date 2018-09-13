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

import com.cheng.app2048.view.OnEventListener;
import com.cheng.app2048.view.View2048;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.util.List;

public class GameActivity extends AppCompatActivity implements RewardedVideoAdListener, OnEventListener, View.OnClickListener {
    private View2048 view2048;
    private TextView tvScore;
    private TextView tvHighest;
    private ImageView cbSound;
    private String gameMode;
    private RewardedVideoAd mRewardedVideoAd;
    private int revokeCounts;
    private static final String REWARD_AD_UNIT_ID = "ca-app-pub-7365304655459838/5327016287";
    private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-7365304655459838/8283543617";
    private Dialog dialog;
    private AdView mAdView;

    /**
     * 插屏广告
     */
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2048);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }

        mAdView = findViewById(R.id.adView);
        loadBannerAd();

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(INTERSTITIAL_AD_UNIT_ID);
        loadInterstitialAd();
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                loadInterstitialAd();
            }
        });

        view2048 = findViewById(R.id.view2048);
        gameMode = getIntent().getStringExtra(MainActivity.GAME_MODE);
        view2048.setGameMode(gameMode);
        view2048.addEventListener(this);
        tvScore = findViewById(R.id.tv_score);
        tvScore.setText(getString(R.string.score) + "\n" + 0);
        cbSound = findViewById(R.id.iv_sound);
        cbSound.setOnClickListener(this);
        tvHighest = findViewById(R.id.tv_highest);
        tvHighest.setText(getString(R.string.record) + "\n" + 0);
        LinearLayout llGame = findViewById(R.id.ll_game);
        if (gameMode.equals(MainActivity.GAME_MODE_CLASSIC) || gameMode.equals(MainActivity.GAME_MODE_PROP_4X4)
                || gameMode.equals(MainActivity.GAME_MODE_PROP_4X4_FIXED)) {
            llGame.setPadding(llGame.getPaddingTop() * 2, llGame.getPaddingTop(), llGame.getPaddingTop() * 2, llGame.getPaddingTop());
        }

        if (view2048.isContinueGame()) {
            view2048.continueGame();
            tvScore.setText(getString(R.string.score) + "\n" + view2048.getScore());
        } else if (gameMode != null) {
            switch (gameMode) {
                case MainActivity.GAME_MODE_CLASSIC:
                    view2048.setStructure(4, 4, 0, false, 3, 0);
                    break;
                case MainActivity.GAME_MODE_CHALLENGE:
                    view2048.setStructure(10, 9, 0, false, 3, 0);
                    break;
                case MainActivity.GAME_MODE_PROP_4X4_FIXED:
                    view2048.setStructure(4, 4, 1, false, 3, 1);
                    break;
                case MainActivity.GAME_MODE_PROP_4X4:
                    view2048.setStructure(4, 4, 1, true, 3, 1);
                    break;
                case MainActivity.GAME_MODE_PROP_10X9_FIXED:
                    view2048.setStructure(10, 9, 3, false, 3, 5);
                    break;
                case MainActivity.GAME_MODE_PROP_10X9:
                    view2048.setStructure(10, 9, 3, true, 3, 5);
                    break;
            }
        }
        cbSound.setImageResource(view2048.isPlaySound() ? R.mipmap.sound_on : R.mipmap.sound_off);
        tvHighest.setText(getString(R.string.record) + "\n" + view2048.getHighestScore());
    }

    private void loadBannerAd() {
        if (!mAdView.isLoading()) {
            mAdView.loadAd(new AdRequest.Builder().build());
        }
    }

    private void loadInterstitialAd() {
        if (!mInterstitialAd.isLoading()) {
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadBannerAd();
    }

    public void revoke(View view) {
        if (revokeCounts < 3) {
            if (view2048.revoke()) {
                revokeCounts++;
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.tip).
                        setMessage(R.string.tip_back).
                        show();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.tip).
                    setMessage(R.string.tip_watch_ad).
                    setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showAd();
                        }
                    }).
                    setNegativeButton(R.string.next, null);
            builder.show();
        }
    }

    public void newGame(View view) {
        view2048.newGame();
        tvScore.setText(getString(R.string.score) + "\n" + 0);
        revokeCounts = 0;
    }

    @Override
    public void scoreListener(int score) {
        tvScore.setText(getString(R.string.score) + "\n" + score);
    }

    @Override
    public void highestListener(int highestScore) {
        tvHighest.setText(getString(R.string.record) + "\n" + highestScore);
    }

    /**
     * 步数计数器，每弹出一次插屏广告时开始计数，当步数大于5步时才会再次弹出广告，防止每走一步都会弹出广告，影响用户体验
     */
    private int step = 6;

    @Override
    public void composeNums(List<Integer> composeNums) {
        if (step < 6) {
            step++;
        }
        if (composeNums.size() != 0) {
            int maxComposeNum = 0;
            for (Integer num : composeNums) {
                if (num > maxComposeNum) {
                    maxComposeNum = num;
                }
            }
            if (maxComposeNum % 512 == 0 && step > 5) {
                if (mInterstitialAd.isLoaded()) {
                    step = 0;
                    mInterstitialAd.show();
                }
            }
        }
    }

    @Override
    public void gameOver() {
        if (dialog == null) {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_gameover, null);
            dialogView.findViewById(R.id.tv_newgame).setOnClickListener(this);
            dialogView.findViewById(R.id.tv_backmenu).setOnClickListener(this);
            dialog = new Dialog(this);
            dialog.setContentView(dialogView);
        }
        if (!dialog.isShowing()) {
            dialog.show();
            dialog.setCancelable(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_sound:
                view2048.setPlaySound(!view2048.isPlaySound());
                cbSound.setImageResource(view2048.isPlaySound() ? R.mipmap.sound_on : R.mipmap.sound_off);
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
            mRewardedVideoAd.loadAd(REWARD_AD_UNIT_ID,
                    new AdRequest.Builder().build());
        }
    }

    public void showAd() {
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.tip).setMessage(R.string.tip_wait).show();
        }
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
//        Toast.makeText(this, "onRewardedVideoAdLeftApplication", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdClosed() {
//        Toast.makeText(this, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
        // Preload the next video ad.
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
//        Toast.makeText(this, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show();
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
//        Toast.makeText(this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdOpened() {
//        Toast.makeText(this, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewarded(RewardItem reward) {
        revokeCounts = 0;
//        Toast.makeText(this,
//                String.format(" onRewarded! currency: %s amount: %d", reward.getType(),
//                        reward.getAmount()),
//                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoStarted() {
//        Toast.makeText(this, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoCompleted() {
//        Toast.makeText(this, "onRewardedVideoCompleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRewardedVideoAd.resume(this);
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
        mRewardedVideoAd.pause(this);
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
        mRewardedVideoAd.destroy(this);
    }
}
