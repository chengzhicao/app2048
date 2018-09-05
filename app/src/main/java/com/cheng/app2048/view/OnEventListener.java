package com.cheng.app2048.view;

public interface OnEventListener {
    /**
     * 分数监听
     */
    void scoreListener(int score);

    /**
     * 最高分数监听
     */
    void highestListener(int highestScore);

    /**
     * 最大数字监听
     */
    void maxNum(int maxNum);

    /**
     * 游戏结束
     */
    void gameOver();

}
