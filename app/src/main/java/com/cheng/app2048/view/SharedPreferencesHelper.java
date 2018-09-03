package com.cheng.app2048.view;

import android.content.Context;
import android.content.SharedPreferences;

class SharedPreferencesHelper {
    private SharedPreferences sp;
    private final String CONTINUE_GAME = "continueGame";
    private final String MODELS = "models";
    private final String SCORE = "score";
    private final String HIGHEST_SCORE = "highestScore";
    private final String ROW_COUNTS = "rowCounts";
    private final String COLUMN_COUNTS = "columnCounts";
    private final String FIXED_COUNTS = "fixedCounts";

    void setSpName(Context context, String gameMode) {
        if (sp == null && gameMode != null) {
            sp = context.getSharedPreferences(gameMode, Context.MODE_PRIVATE);
        }
    }

    /**
     * 获取最高分
     */
    int getHighestScore() {
        if (sp != null) {
            return sp.getInt(HIGHEST_SCORE, 0);
        }
        return 0;
    }

    /**
     * 获取上局分数
     */
    int getScore() {
        if (sp != null) {
            return sp.getInt(SCORE, 0);
        }
        return 0;
    }

    String getModels() {
        if (sp != null) {
            return sp.getString(MODELS, null);
        }
        return null;
    }

    boolean isContinueGame() {
        return sp != null && sp.getBoolean(CONTINUE_GAME, false);
    }

    int getRowCounts() {
        if (sp != null) {
            return sp.getInt(ROW_COUNTS, 0);
        }
        return 0;
    }

    int getColumnCounts() {
        if (sp != null) {
            return sp.getInt(COLUMN_COUNTS, 0);
        }
        return 0;
    }

    int getFixedCounts() {
        if (sp != null) {
            return sp.getInt(FIXED_COUNTS, 0);
        }
        return 0;
    }

    /**
     * 保存是否继续游戏状态
     */
    void saveContinueGame(boolean isContinueGame) {
        if (sp != null) {
            sp.edit().putBoolean(CONTINUE_GAME, isContinueGame).apply();
        }
    }

    void saveScore(int score) {
        if (sp != null) {
            sp.edit().putInt(SCORE, score).apply();
        }
    }

    /**
     * 保存模型
     */
    void saveModels(String models) {
        if (sp != null) {
            sp.edit().putString(MODELS, models).apply();
        }
    }

    void saveHighestScore(int highestScore) {
        if (sp != null) {
            sp.edit().putInt(HIGHEST_SCORE, highestScore).apply();
        }
    }

    void saveFixedCounts(int rowCounts, int columnCounts, int fixedCounts) {
        if (sp != null) {
            SharedPreferences.Editor edit = sp.edit();
            edit.putInt(ROW_COUNTS, rowCounts);
            edit.putInt(COLUMN_COUNTS, columnCounts);
            edit.putInt(FIXED_COUNTS, fixedCounts);
            edit.apply();
        }
    }
}
