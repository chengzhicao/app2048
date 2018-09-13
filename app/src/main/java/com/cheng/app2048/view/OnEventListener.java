package com.cheng.app2048.view;

import java.util.List;

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
     * 每次结合的数值
     */
    void composeNums(List<Integer> composeNums);

    /**
     * 游戏结束
     */
    void gameOver();

}
