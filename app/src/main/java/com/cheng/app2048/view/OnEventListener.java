package com.cheng.app2048.view;

public interface OnEventListener {
    /**
     * 分数监听
     */
    void scoreListener(int everyScore);

    /**
     * 最高分数监听
     */
    void highestListener(int highestScore);

    /**
     * 游戏结束
     */
    void gameOver();

}
