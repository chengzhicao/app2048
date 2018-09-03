package com.cheng.app2048.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.widget.GridLayout;

import com.cheng.app2048.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class View2048 extends GridLayout {
    private Context mContext;
    private final int ANIMATION_TIME = 100;
    private Paint paint = new Paint();
    private RectF rectF = new RectF();
    private Random random = new Random();
    private final int CACHE_COUNTS = 3;//缓存数量
    private SharedPreferencesHelper helper;
    private SoundPool soundPool;
    private int mergerSoundId, moveSoundId;

    /**
     * 是否播放声音
     */
    private boolean isPlaySound;

    /**
     * 行数
     */
    private int rowCounts;

    /**
     * 列数
     */
    private int columnCounts;

    /**
     * 不能移动的数量
     */
    private int fixedCounts;

    /**
     * 数据模型，映射各textView，所有的数值变化都是发生在模型中
     */
    private int[][] models;
//    private int[][] models = new int[][]{{0, 2, 16, 32, 0, 1024}, {0, 64, 2, 1, 16384, 1024}, {0, 1, 1, 128, 0, 1024}, {0, 512, 0, 512, 0, 1024},
//            {0, 512, 512, 512, 2048, 1024},
//            {0, 2048, 2048, 2048, 2048, 1024}, {0, 2, 0, 0, 0, 1024}, {0, 2, 0, 0, 0, 1024}};

    /**
     * 上一次的mode值，用于返回上次
     */
    private List<int[][]> cacheModel = new ArrayList<>(CACHE_COUNTS);

    /**
     * 缓存分数
     */
    private List<Integer> cacheScore = new ArrayList<>(CACHE_COUNTS);

    /**
     * 移动之前时的model
     */
    private int[][] beforeModel;

    /**
     * textView集合，用于移动时交换位置
     */
    private SparseArray<BlockView> tvs;

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
     * 模型中的坐标集合
     */
    private List<Point> modelPoints;

    /**
     * 值为0的模型坐标集合
     */
    private List<Point> zeroModelPoints;

    /**
     * 每段分数
     */
    private int everyScore;

    /**
     * 总分数
     */
    private int totalScore;

    /**
     * 最高分
     */
    private int highestScore;

    private int highestScoreTemp;

    /**
     * 上一步的分数
     */
    private int beforeScore;

    /**
     * 是否合并
     */
    private boolean isMerge;

    public View2048(Context context) {
        this(context, null);
    }

    public View2048(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public View2048(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.View2048);
        rowCounts = typedArray.getInteger(R.styleable.View2048_rowCounts, 0);
        if (rowCounts < 0) {
            rowCounts = 0;
        }
        columnCounts = typedArray.getInteger(R.styleable.View2048_columnCounts, 0);
        if (columnCounts < 0) {
            columnCounts = 0;
        }
        fixedCounts = typedArray.getInteger(R.styleable.View2048_fixedCounts, 0);
        if (fixedCounts < 0) {
            fixedCounts = 0;
        }
        typedArray.recycle();
        this.mContext = context;
        init();
    }

    private void init() {
        setWillNotDraw(false);
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        mergerSoundId = soundPool.load(mContext, R.raw.merge, 1);
        moveSoundId = soundPool.load(mContext, R.raw.move, 1);
        helper = new SharedPreferencesHelper();
        setPadding(space, space, space, space);
        initView();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        initData();
        produceInitNum();
        for (int i = 0; i < rowCounts; i++) {
            for (int j = 0; j < columnCounts; j++) {
                int num = models[i][j];
                BlockView textView = new BlockView(mContext);
                textView.setText(num);
                tvs.put(i * columnCounts + j, textView);
                addView(textView);
            }
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        models = new int[rowCounts][columnCounts];
        beforeModel = new int[rowCounts][columnCounts];
        tvs = new SparseArray<>(rowCounts * columnCounts);
        pointFS = new PointF[rowCounts * columnCounts];
        animators = new ArrayList<>(rowCounts * columnCounts);
        modelPoints = new ArrayList<>(rowCounts * columnCounts);
        zeroModelPoints = new ArrayList<>(rowCounts * columnCounts);
        setRowCount(rowCounts);
        setColumnCount(columnCounts);
    }

    /**
     * 生产初始值
     */
    private void produceInitNum() {
        modelPoints.clear();
        for (int i = 0; i < rowCounts; i++) {
            for (int j = 0; j < columnCounts; j++) {
                models[i][j] = 0;
                modelPoints.add(new Point(i, j));
            }
        }
        if (rowCounts * columnCounts < fixedCounts + 2) {
            return;
        }
        for (int i = 0; i < fixedCounts + 2; i++) {
            int index = random.nextInt(modelPoints.size());
            Point point = modelPoints.get(index);
            int row = point.x;
            int col = point.y;
            if (i < 2) {//产生初始数字
                models[row][col] = 2;
            } else {//产生固定数
                models[row][col] = 1;
            }
            modelPoints.remove(index);
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (rowCounts < 1 || columnCounts < 1) {
            return;
        }
        int width = 800, height = 800;
        if (MeasureSpec.getMode(widthSpec) == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthSpec);
        }
        if (MeasureSpec.getMode(heightSpec) == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightSpec);
        }
        if ((float) rowCounts / columnCounts > (float) height / width) {
            viewHeight = height;
            blockSideLength = (viewHeight - space * 2 - rowCounts * 2 * space) / rowCounts;
            viewWidth = columnCounts * blockSideLength + columnCounts * 2 * space + 2 * space;
        } else {
            viewWidth = width;
            blockSideLength = (viewWidth - space * 2 - columnCounts * 2 * space) / columnCounts;
            viewHeight = rowCounts * blockSideLength + rowCounts * 2 * space + 2 * space;
        }
        for (int i = 0; i < getChildCount(); i++) {
            LayoutParams layoutParams = (LayoutParams) getChildAt(i).getLayoutParams();
            layoutParams.setMargins(space, space, space, space);
            getChildAt(i).setLayoutParams(layoutParams);
            getChildAt(i).measure(MeasureSpec.makeMeasureSpec(blockSideLength, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(blockSideLength, MeasureSpec.EXACTLY));
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
                everyScore = 0;
                isMerge = false;
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
        saveBeforeModel();
        for (int i = 0; i < rowCounts; i++) {
            for (int j = 1, skip = 1; j < columnCounts; j++) {
                if (models[i][j] > 1) {
                    if (models[i][j - skip] == 0) {
                        models[i][j - skip] = models[i][j];
                        models[i][j] = 0;
                        preTranslateAnimation(i * columnCounts + j, i * columnCounts + j - skip, false, X);
                        skip++;
                    } else if (models[i][j - skip] != 1) {
                        if (models[i][j] == models[i][j - skip]) {
                            models[i][j - skip] += models[i][j];
                            models[i][j] = 0;
                            preTranslateAnimation(i * columnCounts + j, i * columnCounts + j - skip, true, X);
                        } else {
                            if (skip > 1) {
                                models[i][j - skip + 1] = models[i][j];
                                models[i][j] = 0;
                                preTranslateAnimation(i * columnCounts + j, i * columnCounts + j - skip + 1, false, X);
//                                skip = 1;
                            }
                        }
                    }
                } else if (models[i][j] == 1) {
                    skip = 1;
                } else {
                    if (models[i][j - 1] != 1) {
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
        saveBeforeModel();
        for (int j = 0; j < columnCounts; j++) {
            for (int i = 1, skip = 1; i < rowCounts; i++) {
                if (models[i][j] > 1) {
                    if (models[i - skip][j] == 0) {
                        models[i - skip][j] = models[i][j];
                        models[i][j] = 0;
                        preTranslateAnimation(i * columnCounts + j, (i - skip) * columnCounts + j, false, Y);
                        skip++;
                    } else if (models[i - skip][j] != 1) {
                        if (models[i][j] == models[i - skip][j]) {
                            models[i - skip][j] += models[i][j];
                            models[i][j] = 0;
                            preTranslateAnimation(i * columnCounts + j, (i - skip) * columnCounts + j, true, Y);
                        } else {
                            if (skip > 1) {
                                models[i - skip + 1][j] = models[i][j];
                                models[i][j] = 0;
                                preTranslateAnimation(i * columnCounts + j, (i - skip + 1) * columnCounts + j, false, Y);
//                                skip = 1;
                            }
                        }
                    }
                } else if (models[i][j] == 1) {
                    skip = 1;
                } else {
                    if (models[i - 1][j] != 1) {
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
        saveBeforeModel();
        for (int i = 0; i < rowCounts; i++) {
            for (int j = columnCounts - 2, skip = 1; j >= 0; j--) {
                if (models[i][j] > 1) {
                    if (models[i][j + skip] == 0) {
                        models[i][j + skip] = models[i][j];
                        models[i][j] = 0;
                        preTranslateAnimation(i * columnCounts + j, i * columnCounts + j + skip, false, X);
                        skip++;
                    } else if (models[i][j + skip] != 1) {
                        if (models[i][j] == models[i][j + skip]) {
                            models[i][j + skip] += models[i][j];
                            models[i][j] = 0;
                            preTranslateAnimation(i * columnCounts + j, i * columnCounts + j + skip, true, X);
                        } else {
                            if (skip > 1) {
                                models[i][j + skip - 1] = models[i][j];
                                models[i][j] = 0;
                                preTranslateAnimation(i * columnCounts + j, i * columnCounts + j + skip - 1, false, X);
//                                skip = 1;
                            }
                        }
                    }
                } else if (models[i][j] == 1) {
                    skip = 1;
                } else {
                    if (models[i][j + 1] != 1) {
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
        saveBeforeModel();
        for (int j = 0; j < columnCounts; j++) {
            for (int i = rowCounts - 2, skip = 1; i >= 0; i--) {
                if (models[i][j] > 1) {
                    if (models[i + skip][j] == 0) {
                        models[i + skip][j] = models[i][j];
                        models[i][j] = 0;
                        preTranslateAnimation(i * columnCounts + j, (i + skip) * columnCounts + j, false, Y);
                        skip++;
                    } else if (models[i + skip][j] != 1) {
                        if (models[i][j] == models[i + skip][j]) {
                            models[i + skip][j] += models[i][j];
                            models[i][j] = 0;
                            preTranslateAnimation(i * columnCounts + j, (i + skip) * columnCounts + j, true, Y);
                        } else {
                            if (skip > 1) {
                                models[i + skip - 1][j] = models[i][j];
                                models[i][j] = 0;
                                preTranslateAnimation(i * columnCounts + j, (i + skip - 1) * columnCounts + j, false, Y);
//                                skip = 1;
                            }
                        }
                    }
                } else if (models[i][j] == 1) {
                    skip = 1;
                } else {
                    if (models[i + 1][j] != 1) {
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
    private void preTranslateAnimation(int from, final int to, boolean isAdd, int direction) {
        ObjectAnimator objectAnimator;
        if (direction == X) {
            objectAnimator = ObjectAnimator.ofFloat(tvs.get(from), "X", pointFS[to].x);
        } else {
            objectAnimator = ObjectAnimator.ofFloat(tvs.get(from), "Y", pointFS[to].y);
        }
        objectAnimator.addListener(new AnimatorListener(from, to, isAdd));
        animators.add(objectAnimator);
        if (isAdd) {
            isMerge = true;
        }
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
        if (isPlaySound) {
            if (isMerge) {//播放合并的声音
                soundPool.play(mergerSoundId, 1, 1, 0, 0, 1);
            } else {//播放移动的声音
                soundPool.play(moveSoundId, 1, 1, 0, 0, 1);
            }
        }
    }

    /**
     * 随机添加一个数，并改变视图，注意要在之前的位移动画全部执行完成后再执行，否则此方法中的动画会在位移动画之前执行从而影响动美观
     */
    private void changeView() {
        //只有当数组发生变化时才会产生新数
        if (isModelChange) {
            //取出值为0的坐标，从中随机取出一个坐标添加一个新值
            zeroModelPoints.clear();
            for (int i = 0; i < rowCounts; i++) {
                for (int j = 0; j < columnCounts; j++) {
                    if (models[i][j] == 0) {
                        zeroModelPoints.add(new Point(i, j));
                    }
                }
            }
            Point point = zeroModelPoints.get(random.nextInt(zeroModelPoints.size()));
            int newValue = 2;
            //产生4的概率为20%
            if (random.nextInt(10) >= 8) {
                newValue = 4;
            }
            int row = point.x;
            int col = point.y;
            //有新添加的数字，执行缩放动画
            ObjectAnimator.ofFloat(tvs.get(row * columnCounts + col), "scaleX", 0, 1).setDuration(ANIMATION_TIME).start();
            ObjectAnimator.ofFloat(tvs.get(row * columnCounts + col), "scaleY", 0, 1).setDuration(ANIMATION_TIME).start();
            models[row][col] = newValue;
            isModelChange = false;
            //填充完最后一个空检查是否game over
            if (zeroModelPoints.size() == 1) {
                if (isGameOver() && onEventListener != null) {
                    onEventListener.gameOver();
                }
            }
        }
        saveModel();
        cacheModel();
        show();
    }

    /**
     * 保存模型
     */
    private void saveModel() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rowCounts; i++) {
            for (int j = 0; j < columnCounts; j++) {
                sb.append(models[i][j]);
                if (i != rowCounts - 1 || j != columnCounts - 1) {
                    sb.append(",");
                }
            }
        }
        helper.saveModels(sb.toString());
        helper.saveContinueGame(true);
    }

    /**
     * 显示
     */
    private void show() {
        for (int i = 0; i < rowCounts; i++) {
            for (int j = 0; j < columnCounts; j++) {
                int num = models[i][j];
                BlockView textView = tvs.get(i * columnCounts + j);
                textView.setText(num);
            }
        }
    }

    /**
     * 全部填充满时检查是否game over，检查水平方向和竖直方向相邻两个数是否相等，1除外
     */
    private boolean isGameOver() {
        //检查横向
        for (int i = 0; i < rowCounts; i++) {
            for (int j = 1; j < columnCounts; j++) {
                if (models[i][j] != 1) {
                    if (models[i][j] == models[i][j - 1]) {
                        return false;
                    }
                }
            }
        }
        //检查竖向
        for (int i = 0; i < columnCounts; i++) {
            for (int j = 1; j < rowCounts; j++) {
                if (models[j][i] != 1) {
                    if (models[j][i] == models[j - 1][i]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 保存移动前的model
     */
    private void saveBeforeModel() {
        for (int i = 0; i < rowCounts; i++) {
            beforeModel[i] = models[i].clone();
        }
        beforeScore = totalScore;
    }

    /**
     * 缓存模型
     */
    private void cacheModel() {
        if (isCache()) {
            if (cacheModel.size() == CACHE_COUNTS) {
                cacheModel.remove(0);
                cacheScore.remove(0);
            }
            int[][] cModel = new int[rowCounts][columnCounts];
            for (int i = 0; i < rowCounts; i++) {
                cModel[i] = beforeModel[i].clone();
            }
            cacheModel.add(cModel);
            cacheScore.add(beforeScore);
        }
    }

    /**
     * 是否缓存模型，只有数据不一样才缓存
     *
     * @return
     */
    private boolean isCache() {
        for (int i = 0; i < rowCounts; i++) {
            if (!Arrays.equals(beforeModel[i], models[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * 撤销，从缓存中取出模型，取最后一个并显示
     */
    public boolean revoke() {
        if (cacheModel.size() > 0) {
            int totalScoreTemp = totalScore;
            totalScore = cacheScore.get(cacheScore.size() - 1);
            if (highestScore > highestScoreTemp) {
                highestScore = highestScore - (totalScoreTemp - totalScore);
                if (highestScore < highestScoreTemp) {
                    highestScore = highestScoreTemp;
                }
                helper.saveHighestScore(highestScore);
            }
            int[][] lastModel = cacheModel.get(cacheModel.size() - 1);
            for (int i = 0; i < rowCounts; i++) {
                models[i] = lastModel[i].clone();
            }
            saveModel();
            helper.saveScore(totalScore);
            cacheModel.remove(cacheModel.size() - 1);
            cacheScore.remove(cacheScore.size() - 1);
            show();
            if (onEventListener != null) {
                onEventListener.scoreListener(totalScore);
                onEventListener.highestListener(highestScore);
            }
            return true;
        }
        return false;
    }

    /**
     * 重新游戏
     */
    public void newGame() {
        totalScore = 0;
        highestScoreTemp = highestScore;
        helper.saveContinueGame(false);
        produceInitNum();
        show();
        cacheModel.clear();
    }

    /**
     * 继续游戏
     */
    public void continueGame() {
        totalScore = helper.getScore();
        String diskModels = helper.getModels();
        rowCounts = helper.getRowCounts();
        columnCounts = helper.getColumnCounts();
        fixedCounts = helper.getFixedCounts();
        initData();
        if (diskModels != null) {
            String[] split = diskModels.split(",");
            for (int i = 0; i < rowCounts; i++) {
                for (int j = 0; j < columnCounts; j++) {
                    int num = Integer.parseInt(split[i * columnCounts + j]);
                    models[i][j] = num;
                    BlockView textView = new BlockView(mContext);
                    textView.setText(num);
                    tvs.put(i * columnCounts + j, textView);
                    addView(textView);
                }
            }
        }
    }

    /**
     * 获取分数
     *
     * @return
     */
    public int getScore() {
        return helper.getScore();
    }

    /**
     * 是否继续游戏
     *
     * @return
     */
    public boolean isContinueGame() {
        return helper.isContinueGame();
    }

    /**
     * 是否开启声音
     *
     * @return
     */
    public boolean isPlaySound() {
        return isPlaySound;
    }

    /**
     * 打开/关闭声音
     *
     * @param playSound
     */
    public void setPlaySound(boolean playSound) {
        isPlaySound = playSound;
    }

    private OnEventListener onEventListener;

    /**
     * 注册监听事件
     *
     * @param onEventListener
     */
    public void addEventListener(OnEventListener onEventListener) {
        this.onEventListener = onEventListener;
    }

    /**
     * 设置结构
     */
    public void setStructure(int rowCounts, int columnCounts, int fixedCounts) {
        this.rowCounts = rowCounts;
        this.columnCounts = columnCounts;
        this.fixedCounts = fixedCounts;
        helper.saveFixedCounts(rowCounts, columnCounts, fixedCounts);
        initView();
    }

    /**
     * 获取最高成绩
     */
    public int getHighestScore() {
        return highestScore;
    }

    /**
     * 设置game mode，用于存储游戏状态，每种游戏模式的Id是唯一的
     */
    public void setGameMode(String gameMode) {
        helper.setSpName(mContext, gameMode);
        highestScore = helper.getHighestScore();
        highestScoreTemp = highestScore;
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
                BlockView toTextView = tvs.get(to);
                BlockView fromTextView = tvs.get(from);
                toTextView.setX(pointFS[from].x);
                toTextView.setY(pointFS[from].y);
                tvs.put(from, toTextView);
                tvs.put(to, fromTextView);
                if (isAdd) {//如果该数相加，执行缩放动画
                    everyScore += fromTextView.getNum() * 2;
                    ObjectAnimator.ofFloat(fromTextView, "scaleX", 1.2f, 1).setDuration(ANIMATION_TIME).start();
                    ObjectAnimator.ofFloat(fromTextView, "scaleY", 1.2f, 1).setDuration(ANIMATION_TIME).start();
                }
            } else if (animation instanceof AnimatorSet) {
                if (everyScore != 0) {
                    totalScore += everyScore;
                    if (onEventListener != null) {
                        onEventListener.scoreListener(totalScore);
                    }
                    //保存当前分数
                    helper.saveScore(totalScore);
                    if (totalScore > highestScore) {
                        highestScore = totalScore;
                        if (onEventListener != null) {
                            onEventListener.highestListener(highestScore);
                        }
                        //保存最高分数
                        helper.saveHighestScore(highestScore);
                    }
                }
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