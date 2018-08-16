package com.cheng.app2048.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cheng.app2048.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class View2048 extends GridLayout {
    private Context mContext;
    private final int ANIMATION_TIME = 100;
    private Paint paint = new Paint();
    private RectF rectF = new RectF();
    private Random random = new Random();
    private final String TEXT_COLOR_WRITE = "#f9f6f2";
    private final String TEXT_COLOR_BLACK = "#776E65";
    private SparseArray<String> colors = new SparseArray<>(12);
    private SparseIntArray backgrounds = new SparseIntArray(12);

    /**
     * textView的背景集合
     */
    private SparseArray<ColorDrawable> drawables = new SparseArray<>(12);

    /**
     * 行数
     */
    private int rowCounts = 4;

    /**
     * 列数
     */
    private int columnCounts = 4;

    /**
     * 数据模型，映射各textView，所有的数值变化都是发生在模型中
     */
    private int[][] model;
//    private int[][] model = new int[][]{{16, 2, 4, 1}, {2, 0, 2, 4}, {0, 2, 2, 1}, {2048, 2048, 2048, 2048}};

    /**
     * 上一次的mode值，用于返回上次
     */
    private int[][] oldModel;

    /**
     * textView集合，用于移动时交换位置
     */
    private SparseArray<TextView> tvs;

    /**
     * textView之间间隔
     */
    private int space = 15;

    /**
     * gridView宽
     */
    private int viewWidth;

    /**
     * gridView高
     */
    private int viewHeight;

    /**
     * textView宽高
     */
    private int blockSideLength;

    /**
     * 记录textView的坐标点，在onLayout时确定，之后不再改变
     */
    private PointF[] pointFS;

    /**
     * 是否已经布局
     */
    private boolean isLayout;

    /**
     * 动画集合，手指抬起时遍历模型，遍历完之后再去执行动画
     */
    private ArrayList<Animator> animators;

    /**
     * 延X轴方向平移
     */
    private final int X = 0X000A;

    /**
     * 延Y轴方向平移
     */
    private final int Y = 0x000B;

    /**
     * 模型是否发生改变
     */
    private boolean isModelChange;

    /**
     * 值为0的模型坐标集合
     */
    private List<String> zeroInModels;

    public View2048(Context context) {
        this(context, null);
    }

    public View2048(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public View2048(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        setWillNotDraw(false);
        setColor();
        setBackgrounds();
        setPadding(space, space, space, space);
        init();
    }

    private void init() {
        model = new int[rowCounts][columnCounts];
        oldModel = new int[rowCounts][columnCounts];
        tvs = new SparseArray<>(rowCounts * columnCounts);
        pointFS = new PointF[rowCounts * columnCounts];
        animators = new ArrayList<>(rowCounts * columnCounts);
        zeroInModels = new ArrayList<>(rowCounts * columnCounts);
        randomNum();
        //设置行列
        setRowCount(rowCounts);
        setColumnCount(columnCounts);
        for (int i = 0; i < rowCounts; i++) {
            for (int j = 0; j < columnCounts; j++) {
                TextView textView = new TextView(mContext);
                if (model[i][j] > 1) {
                    textView.setText(model[i][j] + "");
                }
//                if (drawables.get(model[i][j]) == null) {
//                    drawables.put(model[i][j], new ColorDrawable(Color.parseColor(colors.get(model[i][j]))));
//                }
//                textView.setBackgroundDrawable(drawables.get(model[i][j] > 2048 || model[i][j] == 0 ? 0 : model[i][j]));
                if (model[i][j] != 1) {
                    textView.setBackgroundResource(backgrounds.get(model[i][j] > 2048 || model[i][j] == 0 ? 0 : model[i][j]));
                } else {
                    textView.setBackgroundResource(R.drawable.ice);
                }
                textView.setVisibility(model[i][j] > 0 ? VISIBLE : INVISIBLE);
                textView.setTextSize(20);
                textView.setTypeface(Typeface.DEFAULT_BOLD);
                textView.setTextColor(Color.parseColor(model[i][j] > 4 ? TEXT_COLOR_WRITE : TEXT_COLOR_BLACK));
                textView.setGravity(Gravity.CENTER);
                tvs.put(i * columnCounts + j, textView);
                addView(textView);
            }
        }
    }

    /**
     * 随机在model中产生两个值为2的数
     */
    private void randomNum() {
        int row1 = 0, row2 = 0, col1 = 0, col2 = 0;
        while (row1 == row2 && col1 == col2) {
            row1 = random.nextInt(rowCounts);
            row2 = random.nextInt(rowCounts);
            col1 = random.nextInt(columnCounts);
            col2 = random.nextInt(columnCounts);
        }
        model[row1][col1] = 2;
        model[row2][col2] = 2;
    }

    /**
     * 设置颜色
     */
    private void setColor() {
        colors.put(2, "#eee4da");
        colors.put(4, "#ede0c8");
        colors.put(8, "#f2b179");
        colors.put(16, "#f59563");
        colors.put(32, "#f67c5f");
        colors.put(64, "#f65e3b");
        colors.put(128, "#edcf72");
        colors.put(256, "#edcc61");
        colors.put(512, "#edc850");
        colors.put(1024, "#edc53f");
        colors.put(2048, "#edc22e");
        colors.put(0, "#3c3a32");
    }

    private void setBackgrounds() {
        backgrounds.put(2, R.drawable.background_2);
        backgrounds.put(4, R.drawable.background_4);
        backgrounds.put(8, R.drawable.background_8);
        backgrounds.put(16, R.drawable.background_16);
        backgrounds.put(32, R.drawable.background_32);
        backgrounds.put(64, R.drawable.background_64);
        backgrounds.put(128, R.drawable.background_128);
        backgrounds.put(256, R.drawable.background_256);
        backgrounds.put(512, R.drawable.background_512);
        backgrounds.put(1024, R.drawable.background_1024);
        backgrounds.put(2048, R.drawable.background_2048);
        backgrounds.put(0, R.drawable.background_0);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        int width = 1000, height = 1000;
        if (MeasureSpec.getMode(widthSpec) == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthSpec);
        }
        if (MeasureSpec.getMode(heightSpec) == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightSpec);
        }
        //宽取最小值
        viewWidth = width > height ? height : width;
        blockSideLength = (viewWidth - space * 2 - columnCounts * 2 * space) / columnCounts;
        if (rowCounts == columnCounts) {
            viewHeight = viewWidth;
        } else {
            viewHeight = rowCounts * blockSideLength + rowCounts * 2 * space + 2 * space;
        }
        for (int i = 0; i < getChildCount(); i++) {
            LayoutParams layoutParams = (LayoutParams) getChildAt(i).getLayoutParams();
            layoutParams.width = blockSideLength;
            layoutParams.height = blockSideLength;
            layoutParams.setMargins(space, space, space, space);
            getChildAt(i).setLayoutParams(layoutParams);
        }
        setMeasuredDimension(viewWidth, viewHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!isLayout) {
            for (int i = 0; i < getChildCount(); i++) {
                PointF pointF = new PointF(getChildAt(i).getX(), getChildAt(i).getY());
                pointFS[i] = pointF;
            }
            isLayout = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#BBADA0"));
        rectF.left = 0;
        rectF.top = 0;
        rectF.right = viewWidth;
        rectF.bottom = viewHeight;
        canvas.drawRoundRect(rectF, 10, 10, paint);
        paint.setColor(Color.parseColor("#CDC1B4"));
        for (PointF pointF : pointFS) {
            rectF.left = pointF.x;
            rectF.top = pointF.y;
            rectF.right = pointF.x + blockSideLength;
            rectF.bottom = pointF.y + blockSideLength;
            canvas.drawRoundRect(rectF, 10, 10, paint);
        }
    }

    private float downX, downY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float currentX = event.getX();
                float currentY = event.getY();
                animators.clear();
                if (Math.sqrt(Math.pow((currentX - downX), 2) + Math.pow((currentY - downY), 2)) > 20) {
                    if (currentX - downX > 0) {//右
                        if (currentY > downY) {//下
                            if (currentX - downX > currentY - downY) {//右
                                right();
                            } else {//下
                                bottom();
                            }
                        } else {
                            if (currentX - downX > downY - currentY) {//右
                                right();
                            } else {//上
                                top();
                            }
                        }
                    } else {//左
                        if (currentY > downY) {//下
                            if (downX - currentX > currentY - downY) {//左
                                left();
                            } else {//下
                                bottom();
                            }
                        } else {
                            if (downX - currentX > downY - currentY) {//左
                                left();
                            } else {//上
                                top();
                            }
                        }
                    }
                    break;
                }
                return true;
        }
        return true;

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    private void left() {
        copyModelToOldModel();
        for (int i = 0; i < rowCounts; i++) {
            for (int j = 1, skip = 1; j < columnCounts; j++) {
                if (model[i][j] > 1) {
                    if (model[i][j - 1] == 1) {
                        if (skip == 0) {
                            skip++;
                        }
//                        continue;
                    }
                    if (model[i][j - skip] == 0) {
                        model[i][j - skip] = model[i][j];
                        model[i][j] = 0;
                        preTranslateAnimation(i * columnCounts + j, i * columnCounts + j - skip, false, X);
                        skip++;
                    } else if (model[i][j - skip] != 1) {
                        if (model[i][j] == model[i][j - skip]) {
                            model[i][j - skip] += model[i][j];
                            model[i][j] = 0;
                            preTranslateAnimation(i * columnCounts + j, i * columnCounts + j - skip, true, X);
                        } else {
                            if (skip > 1) {
                                model[i][j - skip + 1] = model[i][j];
                                model[i][j] = 0;
                                preTranslateAnimation(i * columnCounts + j, i * columnCounts + j - skip + 1, false, X);
//                                skip = 1;
                            }
                        }
                    }
                } else if (model[i][j] == 1) {
                    skip = 0;
                } else {
                    if (model[i][j - 1] != 1) {
                        skip++;
                    }
                }
                //遍历到最后一个且动画集合大于0时执行动画
                if (i == rowCounts - 1 && j == columnCounts - 1 && animators.size() > 0) {
                    translateAnimation();
                }
            }
        }
    }

    private void top() {
        copyModelToOldModel();
        for (int j = 0; j < columnCounts; j++) {
            for (int i = 1, skip = 1; i < rowCounts; i++) {
                if (model[i][j] > 1) {
                    if (model[i - 1][j] == 1) {
                        if (skip == 0) {
                            skip++;
                        }
//                        continue;
                    }
                    if (model[i - skip][j] == 0) {
                        model[i - skip][j] = model[i][j];
                        model[i][j] = 0;
                        preTranslateAnimation(i * columnCounts + j, (i - skip) * columnCounts + j, false, Y);
                        skip++;
                    } else if (model[i - skip][j] != 1) {
                        if (model[i][j] == model[i - skip][j]) {
                            model[i - skip][j] += model[i][j];
                            model[i][j] = 0;
                            preTranslateAnimation(i * columnCounts + j, (i - skip) * columnCounts + j, true, Y);
                        } else {
                            if (skip > 1) {
                                model[i - skip + 1][j] = model[i][j];
                                model[i][j] = 0;
                                preTranslateAnimation(i * columnCounts + j, (i - skip + 1) * columnCounts + j, false, Y);
//                                skip = 1;
                            }
                        }
                    }
                } else if (model[i][j] == 1) {
                    skip = 0;
                } else {
                    if (model[i - 1][j] != 1) {
                        skip++;
                    }
                }
                if (j == columnCounts - 1 && i == rowCounts - 1 && animators.size() > 0) {
                    translateAnimation();
                }
            }
        }
    }

    private void right() {
        copyModelToOldModel();
        for (int i = 0; i < rowCounts; i++) {
            for (int j = columnCounts - 2, skip = 1; j >= 0; j--) {
                if (model[i][j] > 1) {
                    if (model[i][j + 1] == 1) {
                        if (skip == 0) {
                            skip++;
                        }
//                        continue;
                    }
                    if (model[i][j + skip] == 0) {
                        model[i][j + skip] = model[i][j];
                        model[i][j] = 0;
                        preTranslateAnimation(i * columnCounts + j, i * columnCounts + j + skip, false, X);
                        skip++;
                    } else if (model[i][j + skip] != 1) {
                        if (model[i][j] == model[i][j + skip]) {
                            model[i][j + skip] += model[i][j];
                            model[i][j] = 0;
                            preTranslateAnimation(i * columnCounts + j, i * columnCounts + j + skip, true, X);
                        } else {
                            if (skip > 1) {
                                model[i][j + skip - 1] = model[i][j];
                                model[i][j] = 0;
                                preTranslateAnimation(i * columnCounts + j, i * columnCounts + j + skip - 1, false, X);
//                                skip = 1;
                            }
                        }
                    }
                } else if (model[i][j] == 1) {
                    skip = 0;
                } else {
                    if (model[i][j + 1] != 1) {
                        skip++;
                    }
                }
                if (i == rowCounts - 1 && j == 0 && animators.size() > 0) {
                    translateAnimation();
                }
            }
        }
    }

    private void bottom() {
        copyModelToOldModel();
        for (int j = 0; j < columnCounts; j++) {
            for (int i = rowCounts - 2, skip = 1; i >= 0; i--) {
                if (model[i][j] > 1) {
                    if (model[i + 1][j] == 1) {
                        if (skip == 0) {
                            skip++;
                        }
//                        continue;
                    }
                    if (model[i + skip][j] == 0) {
                        model[i + skip][j] = model[i][j];
                        model[i][j] = 0;
                        preTranslateAnimation(i * columnCounts + j, (i + skip) * columnCounts + j, false, Y);
                        skip++;
                    } else if (model[i + skip][j] != 1) {
                        if (model[i][j] == model[i + skip][j]) {
                            model[i + skip][j] += model[i][j];
                            model[i][j] = 0;
                            preTranslateAnimation(i * columnCounts + j, (i + skip) * columnCounts + j, true, Y);
                        } else {
                            if (skip > 1) {
                                model[i + skip - 1][j] = model[i][j];
                                model[i][j] = 0;
                                preTranslateAnimation(i * columnCounts + j, (i + skip - 1) * columnCounts + j, false, Y);
//                                skip = 1;
                            }
                        }
                    }
                } else if (model[i][j] == 1) {
                    skip = 0;
                } else {
                    if (model[i + 1][j] != 1) {
                        skip++;
                    }
                }
                if (j == columnCounts - 1 && i == 0 && animators.size() > 0) {
                    translateAnimation();
                }
            }
        }
    }

    /**
     * 位移动画预处理
     */
    private void preTranslateAnimation(final int from, final int to, final boolean isAdd, int direction) {
        ObjectAnimator objectAnimator;
        if (direction == X) {
            objectAnimator = ObjectAnimator.ofFloat(tvs.get(from), "X", pointFS[to].x);
        } else {
            objectAnimator = ObjectAnimator.ofFloat(tvs.get(from), "Y", pointFS[to].y);
        }
        objectAnimator.addListener(new AnimatorListener(from, to, isAdd));
        animators.add(objectAnimator);
    }

    /**
     * 位移动画
     */
    private void translateAnimation() {
        isModelChange = true;
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        animatorSet.setDuration(ANIMATION_TIME);
        animatorSet.addListener(new AnimatorListener());
        animatorSet.start();
    }

    /**
     * 随机添加一个数，并改变视图，注意要在之前的位移动画全部执行完成后再执行，否则此方法中的动画会在位移动画之前执行从而影响动美观
     */
    private void changeView() {
        //只有当数组发生变化时才会产生新数
        if (isModelChange) {
            //取出值为0的坐标，从中随机取出一个坐标添加一个新值
            zeroInModels.clear();
            for (int i = 0; i < rowCounts; i++) {
                for (int j = 0; j < columnCounts; j++) {
                    if (model[i][j] == 0) {
                        zeroInModels.add(i + "," + j);
                    }
                }
            }
            String[] newPosition = zeroInModels.get(random.nextInt(zeroInModels.size())).split(",");
            int newValue = 2;
            //产生4的概率为20%
            if (random.nextInt(10) >= 8) {
                newValue = 4;
            }
            int row = Integer.parseInt(newPosition[0]);
            int col = Integer.parseInt(newPosition[1]);
            //有新添加的数字，执行缩放动画
            ObjectAnimator.ofFloat(tvs.get(row * columnCounts + col), "scaleX", 0, 1).setDuration(ANIMATION_TIME).start();
            ObjectAnimator.ofFloat(tvs.get(row * columnCounts + col), "scaleY", 0, 1).setDuration(ANIMATION_TIME).start();
            model[row][col] = newValue;
            isModelChange = false;
            //填充完最后一个空检查是否game over
            if (zeroInModels.size() == 1) {
                if (isGameOver()) {
                    Toast.makeText(mContext, "game over", Toast.LENGTH_SHORT).show();
                }
            }
        }
        show();
    }

    /**
     * 显示
     */
    private void show() {
        for (int i = 0; i < rowCounts; i++) {
            for (int j = 0; j < columnCounts; j++) {
                tvs.get(i * columnCounts + j).setTextColor(Color.parseColor(model[i][j] > 4 ? TEXT_COLOR_WRITE : TEXT_COLOR_BLACK));
//                if (drawables.get(model[i][j]) == null) {
//                    drawables.put(model[i][j], new ColorDrawable(Color.parseColor(colors.get(model[i][j]))));
//                }
//                tvs.get(i * columnCounts + j).setBackgroundDrawable(drawables.get(model[i][j] > 2048 || model[i][j] == 0 ? 0 : model[i][j]));
                if (model[i][j] != 1) {
                    tvs.get(i * columnCounts + j).setBackgroundResource(backgrounds.get(model[i][j] > 2048 || model[i][j] == 0 ? 0 : model[i][j]));
                } else {
                    tvs.get(i * columnCounts + j).setBackgroundResource(R.drawable.ice);
                }
                if (model[i][j] > 1) {
                    tvs.get(i * columnCounts + j).setText(model[i][j] + "");
                }
                tvs.get(i * columnCounts + j).setVisibility(model[i][j] > 0 ? VISIBLE : INVISIBLE);
            }
        }
    }

    /**
     * 全部填充满时检查是否game over，检查水平方向和竖直方向相邻两个数是否相等，1除外
     */
    private boolean isGameOver() {
        for (int i = 0; i < rowCounts; i++) {
            for (int j = 1; j < columnCounts; j++) {
                if (model[i][j] != 1) {
                    if (model[i][j] == model[i][j - 1]) {
                        return false;
                    }
                    if (model[j][i] == model[j - 1][i]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 撤销
     */
    public void revoke() {
        copyOldModelToModel();
        show();
    }

    /**
     * 复制模型
     */
    private void copyModelToOldModel() {
        for (int i = 0; i < rowCounts; i++) {
            oldModel[i] = model[i].clone();
        }
    }

    /**
     * 复制模型
     */
    private void copyOldModelToModel() {
        for (int i = 0; i < rowCounts; i++) {
            model[i] = oldModel[i].clone();
        }
    }

    private class AnimatorListener implements Animator.AnimatorListener {
        private int from;
        private int to;
        private boolean isAdd;

        private AnimatorListener() {

        }

        private AnimatorListener(int from, int to, boolean isAdd) {
            this.from = from;
            this.to = to;
            this.isAdd = isAdd;
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (animation instanceof ObjectAnimator) {
                //交换TextView位置
                TextView ToTextView = tvs.get(to);
                TextView fromTextView = tvs.get(from);
                ToTextView.setX(pointFS[from].x);
                ToTextView.setY(pointFS[from].y);
                tvs.put(from, ToTextView);
                tvs.put(to, fromTextView);
                if (isAdd) {//如果该数相加，执行缩放动画
                    ObjectAnimator.ofFloat(fromTextView, "scaleX", 1.2f, 1).setDuration(ANIMATION_TIME).start();
                    ObjectAnimator.ofFloat(fromTextView, "scaleY", 1.2f, 1).setDuration(ANIMATION_TIME).start();
                }
            } else if (animation instanceof AnimatorSet) {
                changeView();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}