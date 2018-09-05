package com.cheng.app2048.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import com.cheng.app2048.R;

class BlockView extends View {
    private int width, height;
    private Paint paint;
    private RectF rectF;
    private int preNum;
    private int num;
    private Rect rect;
    private SparseArray<String> colors = new SparseArray<>(12);

    public BlockView(Context context) {
        this(context, null);
    }

    public BlockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        rectF = new RectF();
        rect = new Rect();
        setColor();
    }

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
        colors.put(4096, "#784CB4");
        colors.put(0, "#3c3a32");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        rectF.right = width;
        rectF.bottom = height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        preNum = num;
        if (num > 1) {
            paint.setColor(Color.parseColor(colors.get(num > 4096 ? 0 : num)));
            canvas.drawRoundRect(rectF, 10, 10, paint);
            if (num < 100) {
                paint.setTextSize(70);
            } else if (num < 1000) {
                paint.setTextSize(60);
            } else if (num < 10000) {
                paint.setTextSize(50);
            } else if (num < 100000) {
                paint.setTextSize(40);
            } else {
                paint.setTextSize(30);
            }
            String value = String.valueOf(num);
            paint.setColor(Color.parseColor(num > 4 ? "#f9f6f2" : "#776E65"));
            paint.getTextBounds(value, 0, value.length(), rect);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            float valueWidth = rect.right - rectF.left;
            float valueHeight = rect.bottom = rect.top;
            canvas.drawText(value, (width - valueWidth) / 2, (height - valueHeight) / 2, paint);
        } else if (num == 1) {
            setBackgroundResource(R.mipmap.ice);
        } else if (num == 0) {
//            canvas.drawColor(Color.TRANSPARENT);
            setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public int getNum() {
        return num;
    }

    public void setText(int num) {
        this.num = num;
        if (preNum != num) {
            invalidate();
        }
    }
}
