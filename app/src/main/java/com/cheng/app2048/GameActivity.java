package com.cheng.app2048;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.cheng.app2048.view.OnEventListener;
import com.cheng.app2048.view.View2048;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.List;

public class GameActivity extends AppCompatActivity implements OnEventListener, View.OnClickListener {
    private View2048 view2048;
    private TextView tvScore;
    private TextView tvHighest;
    private ImageView cbSound;
    private RewardedAd mRewardedAd;
    private int revokeCounts;
    private static final String BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
    private static final String REWARD_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
    private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";
    private Dialog dialog;
    private AdView mAdView;
    private int score;

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

        createAndLoadRewardedAd();

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
        String gameMode = getIntent().getStringExtra(MainActivity.GAME_MODE);
        view2048.setGameMode(gameMode);
        view2048.addEventListener(this);
        tvScore = findViewById(R.id.tv_score);
        tvScore.setText(getString(R.string.score) + "\n" + 0);
        cbSound = findViewById(R.id.iv_sound);
        cbSound.setOnClickListener(this);
        tvHighest = findViewById(R.id.tv_highest);
        tvHighest.setText(getString(R.string.record) + "\n" + 0);

        mAdView = new AdView(this);
        mAdView.setAdSize(getAdSize());
        mAdView.setAdUnitId(BANNER_AD_UNIT_ID);
        mAdView.loadAd(new AdRequest.Builder().build());
        ((LinearLayout) findViewById(R.id.ll_content)).addView(mAdView,new LinearLayout.LayoutParams(-1,dp2px(this,60)));

        if (view2048.isContinueGame()) {
            view2048.continueGame();
            score = view2048.getScore();
            tvScore.setText(getString(R.string.score) + "\n" + score);
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

    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    private AdSize getAdSize() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private void createAndLoadRewardedAd() {
        mRewardedAd = new RewardedAd(this,
                REWARD_AD_UNIT_ID);
        mRewardedAd.loadAd(new AdRequest.Builder().build(), null);
    }

    private void loadInterstitialAd() {
        if (!mInterstitialAd.isLoading()) {
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        }
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
                            showRewardAd();
                        }
                    }).
                    setNegativeButton(R.string.next, null);
            builder.show();
        }
    }

    public void newGame(View view) {
        this.score = 0;
        view2048.newGame();
        tvScore.setText(getString(R.string.score) + "\n" + 0);
        revokeCounts = 0;
    }

    @Override
    public void scoreListener(int score) {
        this.score = score;
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
            if (maxComposeNum % 32 == 0 && step > 5) {
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
            TextView tvFinalScore = dialogView.findViewById(R.id.tv_final_score);
            tvFinalScore.setText(getString(R.string.thisScore) + " " + score);
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
        if (!mRewardedAd.isLoaded()) {
            mRewardedAd.loadAd(new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
                @Override
                public void onRewardedAdLoaded() {
                }

                @Override
                public void onRewardedAdFailedToLoad(LoadAdError adError) {
                }
            });
        }
    }

    public void showRewardAd() {
        if (mRewardedAd.isLoaded()) {
            mRewardedAd.show(this, new RewardedAdCallback() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    revokeCounts = 0;
                }

                @Override
                public void onRewardedAdClosed() {
                    createAndLoadRewardedAd();
                }

                @Override
                public void onRewardedAdFailedToShow(AdError adError) {
                    createAndLoadRewardedAd();
                }
            });
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.tip).setMessage(R.string.tip_wait).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
}
