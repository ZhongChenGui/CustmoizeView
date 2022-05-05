package com.learning.custmoizeview.watchFace;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.learning.custmoizeview.R;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by chengui.zhong
 * on 2022/4/26
 */
public class WatchFaceView extends View {
    private static final String TAG = "WatchFaceView";
    private int mSecondColor;
    private int mMinColor;
    private int mHourColor;
    private int mScaleColor;
    private int mBgResId;
    private boolean mScaleShow;
    private Paint mSecondPaint;
    private Paint mMinPaint;
    private Paint mHourPaint;
    private Paint mScalePaint;
    private Bitmap mBgBitmap = null;
    private int mWidth;
    private int mHeight;
    private Rect mSrcRect;
    private Rect mDesRect;
    public Calendar mCalendar;

    public WatchFaceView(Context context) {
        this(context, null);
    }

    public WatchFaceView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WatchFaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 设置、获取属性
        setUpAttrs(context, attrs);
        initPaints();
//        initView(context);
        mCalendar = Calendar.getInstance();
        mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+08"));
    }

    private void initPaints() {
        // 秒针
        mSecondPaint = new Paint();
        mSecondPaint.setColor(mSecondColor);
        mSecondPaint.setStyle(Paint.Style.STROKE);
        mSecondPaint.setStrokeWidth(5f);
        mSecondPaint.setAntiAlias(true);   // 抗锯齿

        // 分针
        mMinPaint = new Paint();
        mMinPaint.setColor(mMinColor);
        mMinPaint.setStyle(Paint.Style.STROKE);
        mMinPaint.setStrokeWidth(10f);
        mMinPaint.setAntiAlias(true);   // 抗锯齿

        // 时针
        mHourPaint = new Paint();
        mHourPaint.setColor(mHourColor);
        mHourPaint.setStyle(Paint.Style.STROKE);
        mHourPaint.setStrokeWidth(15f);
        mHourPaint.setAntiAlias(true);   // 抗锯齿

        // 刻度
        mScalePaint = new Paint();
        mScalePaint.setColor(mMinColor);
        mScalePaint.setStyle(Paint.Style.STROKE);
        mScalePaint.setStrokeWidth(5f);
        mScalePaint.setAntiAlias(true);   // 抗锯齿
    }


    private void setUpAttrs(Context context, @Nullable AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WatchFaceView);
        mSecondColor = ta.getColor(R.styleable.WatchFaceView_secondColor, getResources().getColor(R.color.secondDefaultColor));
        mMinColor = ta.getColor(R.styleable.WatchFaceView_minColor, getResources().getColor(R.color.minDefaultColor));
        mHourColor = ta.getColor(R.styleable.WatchFaceView_hoursColor, getResources().getColor(R.color.hoursDefaultColor));
        mScaleColor = ta.getColor(R.styleable.WatchFaceView_scaleColor, getResources().getColor(R.color.scaleDefaultColor));
        mBgResId = ta.getResourceId(R.styleable.WatchFaceView_faceBackground, -1);
        mScaleShow = ta.getBoolean(R.styleable.WatchFaceView_scaleShow, true);
        ta.recycle();

        if (mBgResId != -1) {
            mBgBitmap = BitmapFactory.decodeResource(getResources(), mBgResId);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 获取大小
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        // 取小的
        int widthTargetSize = widthSize - getPaddingLeft() - getPaddingRight();
        int heightTargetSize = heightSize - getPaddingTop() - getPaddingBottom();
        int targetSize = Math.min(widthTargetSize, heightTargetSize);
        setMeasuredDimension(targetSize, targetSize);
        // 初始化Rect
        initRect();
    }

    private void initRect() {
        if (mBgBitmap == null) {
            return;
        }
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        // 源大小
        mSrcRect = new Rect();
        mSrcRect.left = 0;
        mSrcRect.right = mBgBitmap.getWidth();
        mSrcRect.top = 0;
        mSrcRect.bottom = mBgBitmap.getHeight();
        // 目标大小
        mDesRect = new Rect();
        mDesRect.left = 0;
        mDesRect.right = mWidth;
        mDesRect.top = 0;
        mDesRect.bottom = mHeight;
    }

    private boolean isUpdate = false;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isUpdate = true;
        post(new Runnable() {
            @Override
            public void run() {
                if (isUpdate) {
                    invalidate();
                    Log.d(TAG, "run: ................");
                    postDelayed(this, 1000);
                } else {
                    removeCallbacks(this);
                }
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isUpdate = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 绘制背景
        canvas.drawColor(Color.parseColor("#000000"));
        // 绘制刻度
        drawScale(canvas);
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        int radius = (int) (mWidth / 2f);
        int secondValue = mCalendar.get(Calendar.SECOND);
        if (secondValue == 0) {
            // 绘制秒针
            drawSecondLine(canvas, radius);
            // 绘制分针
            drawMinuteLine(canvas, radius);
            // 绘制时针
            drawHourLine(canvas, radius);

        } else {
            // 绘制时针
            drawHourLine(canvas, radius);

            // 绘制分针
            drawMinuteLine(canvas, radius);

            // 绘制秒针
            drawSecondLine(canvas, radius);
        }


    }

    private void drawSecondLine(Canvas canvas, int radius) {
        int secondRadius = (int) (radius * 0.8f);
        int secondValue = mCalendar.get(Calendar.SECOND);
        float secondRotate = secondValue * 6f;
        canvas.save();
        canvas.rotate(secondRotate, radius, radius);
        canvas.drawLine(radius, radius - secondRadius, radius, radius - 15f, mSecondPaint);
        canvas.restore();
    }

    private void drawMinuteLine(Canvas canvas, int radius) {
        int minuteRadius = (int) (radius * 0.75f);
        int minuteValue = mCalendar.get(Calendar.MINUTE);
        float minuteRotate = minuteValue * 6f;
        canvas.save();
        canvas.rotate(minuteRotate, radius, radius);
        canvas.drawLine(radius, radius - minuteRadius, radius, radius - 15f, mMinPaint);
        canvas.restore();
    }

    private void drawHourLine(Canvas canvas, int radius) {
        int hourRadius = (int) (radius * 0.7f);
        int hourValue = mCalendar.get(Calendar.HOUR);
        float hourOffsetRotate = mCalendar.get(Calendar.MINUTE) / 2f;
        float hourRotate = hourValue * 30 + hourOffsetRotate;
        canvas.save();
        canvas.rotate(hourRotate, radius, radius);
        canvas.drawLine(radius, radius - hourRadius, radius, radius - 15f, mHourPaint);
        canvas.restore();
    }

    private void drawScale(Canvas canvas) {
//        mBgBitmap = null;
        if (mBgBitmap != null) {
            canvas.drawBitmap(mBgBitmap, mSrcRect, mDesRect, mScalePaint);
        } else {
            int radius = (int) (mWidth / 2f);
            // 内环半径
            int innerC = (int) (mWidth / 2 * 0.85f);
            // 外环半径
            int outerC = (int) (mWidth / 2 * 0.95f);
            // 绘制刻度方法一
//            for (int i = 0; i < 12; i++) {
//                double th = i * Math.PI * 2 / 12;
//                // 内环
//                int innerB = (int) (Math.cos(th) * innerC);
//                int innerX = mHeight / 2 - innerB;
//                int innerA = (int) (innerC * Math.sin(th));
//                int innerY = mWidth / 2 + innerA;
//                // 外环
//                int outerB = (int) (Math.cos(th) * outerC);
//                int outerX = mHeight / 2 - outerB;
//                int outerA = (int) (outerC * Math.sin(th));
//                int outerY = mWidth / 2 + outerA;
//                canvas.drawLine(innerX, innerY, outerX, outerY, mScalePaint);
//            }
            // 绘制刻度方法二
            canvas.drawCircle(radius, radius, 15f, mScalePaint);
            canvas.save();
            for (int i = 0; i < 12; i++) {
                canvas.rotate(30, radius, radius);
                canvas.drawLine(radius, radius - outerC, radius, radius - innerC, mScalePaint);
            }
            canvas.restore();
        }
    }
}
