package com.ray.learnscroller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;


/**
 * 水平滚动刻度尺
 * Created by mateng on 2017/12/4.
 */
public class ScaleScrollView extends View {

    private Paint mPaint;// 画笔
    private VelocityTracker mVelocityTracker;// 速度追踪辅助类


    protected int mMax; // 最大刻度
    protected int mMin; // 最小刻度
    protected int unitValue; // 每刻度单位
    protected int mScaleMargin;    // 刻度间距
    protected int mScaleHeight;    // 刻度线的高度
    protected int mTextSize;   // 大刻度上方字体大小
    protected int mTextColor;  // 文字颜色
    protected int mBottomLineColor;// 底部线条的颜色
    protected int mScaleColor;// 刻度线的颜色


    protected int mScaleMaxHeight; // 整刻度线高度
    protected int mRectWidth;  // 控件总宽度
    protected int mRectHeight; // 控件总高度


    private int mDrawStartPos;// 绘制的起始位置
    protected int mScrollLastX;  // 滚动最后的横坐标
    private boolean isScrolling = false;// 是否正在滑动


    protected Scroller mScroller;// 滚动的辅助类
    protected OnScrollListener mScrollListener;// 刻度回调监听


    public ScaleScrollView(Context context) {
        super(context);
        init(null);
    }

    public ScaleScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ScaleScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    protected void init(AttributeSet attrs) {
        // 获取自定义属性

        @SuppressLint("CustomViewStyleable")
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.scale_attrs);
        mMax = ta.getInteger(R.styleable.scale_attrs_lf_scale_view_max, 200);
        mMin = ta.getInteger(R.styleable.scale_attrs_lf_scale_view_min, 0);
        unitValue = ta.getInteger(R.styleable.scale_attrs_lf_scale_view_unit, 1);
        mScaleMargin = ta.getDimensionPixelOffset(R.styleable.scale_attrs_lf_scale_view_margin, 15);
        mScaleHeight = ta.getDimensionPixelOffset(R.styleable.scale_attrs_lf_scale_view_height, 20);
        mTextSize = ta.getDimensionPixelOffset(R.styleable.scale_attrs_lf_scale_view_textSize, 20);
        mTextColor = ta.getColor(R.styleable.scale_attrs_lf_scale_view_textColor, Color.parseColor("#C2C2C2"));
        mBottomLineColor = ta.getColor(R.styleable.scale_attrs_lf_scale_view_bottomLineColor, Color.parseColor("#EDEDED"));
        mScaleColor = ta.getColor(R.styleable.scale_attrs_lf_scale_view_scaleColor, Color.parseColor("#F7B62D"));

        ta.recycle();

        mScroller = new Scroller(getContext());

        initVar();

        initPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        onDrawLine(canvas, mPaint);// 绘制底部线条

        onDrawScale(canvas, mPaint); //画刻度
    }

    private void initPaint() {
        // 画笔
        mPaint = new Paint();
        // 抗锯齿
        mPaint.setAntiAlias(true);
        // 设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰
        mPaint.setDither(true);
        // 充满
        mPaint.setStyle(Paint.Style.FILL);
        // 文字居中
        mPaint.setTextAlign(Paint.Align.CENTER);
        // 文字大小
        mPaint.setTextSize(mTextSize);
        // 线条宽度
        mPaint.setStrokeWidth(2);
    }

    // 计算空间宽高
    protected void initVar() {

        mDrawStartPos = ScreenUtils.getScreenWidth(getContext()) / 2;

        mRectWidth = (mMax - mMin) * mScaleMargin;

        mRectHeight = mScaleHeight * 6;

        mScaleMaxHeight = mScaleHeight * 2;

        // 设置layoutParams
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(mRectWidth, mRectHeight);
        this.setLayoutParams(lp);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = View.MeasureSpec.makeMeasureSpec(mRectHeight, View.MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, height);
    }

    // 绘制底部线条
    protected void onDrawLine(Canvas canvas, Paint paint) {
        paint.setColor(mBottomLineColor);
        canvas.drawLine(0, mRectHeight, mRectWidth, mRectHeight, paint);
    }

    // 绘制刻度
    protected void onDrawScale(Canvas canvas, Paint paint) {

        for (int i = 0, k = mMin; i <= mMax - mMin; i++) {
            paint.setColor(mScaleColor);
            if (i % 10 == 0) { //整值
                canvas.drawLine(mDrawStartPos + i * mScaleMargin, mRectHeight, mDrawStartPos + i * mScaleMargin, mRectHeight - mScaleMaxHeight, paint);
                //整值文字
                paint.setColor(mTextColor);
                canvas.drawText(String.valueOf(k), mDrawStartPos + i * mScaleMargin, mRectHeight - mScaleMaxHeight - 20, paint);
                k += unitValue * 10;
            } else {
                canvas.drawLine(mDrawStartPos + i * mScaleMargin, mRectHeight, mDrawStartPos + i * mScaleMargin, mRectHeight - mScaleHeight, paint);
            }
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();// 触摸事件的当前位置
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 初始化速度追踪器
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                }
                mVelocityTracker.addMovement(event);
                // 初始化滑动辅助类
                if (mScroller != null && !mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                // 记录当前坐标
                mScrollLastX = x;
                return true;
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(isScrolling);// 禁止父容器处理触摸事件
                int dataX = mScrollLastX - x;// 位移距离
                // 计算速度
                mVelocityTracker.addMovement(event);
                // 开始滑动
                mScroller.startScroll(mScroller.getFinalX(), 0, dataX, 0);

                mScrollLastX = x;

                postInvalidate();

                isScrolling = true;
                return true;
            case MotionEvent.ACTION_UP:// 手指离开屏幕
            case MotionEvent.ACTION_CANCEL:
                mVelocityTracker.computeCurrentVelocity(1000);
                // 执行惯性滑动
                mScroller.fling(mScroller.getFinalX(), 0,
                        -(int) (mVelocityTracker.getXVelocity() * 0.15), 0,
                        0, mRectWidth,
                        0, 0);

                isScrolling = false;
                mVelocityTracker.clear();
                mVelocityTracker.recycle();// 回收
                mVelocityTracker = null;
                return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 使用Scroller时需重写
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        // 判断Scroller是否执行完毕
        if (mScroller.computeScrollOffset()) {
            // 回调滑动刻度
            if (mScrollListener != null) {
                mScrollListener.onScaleScroll(mScroller.getFinalX() / mScaleMargin * unitValue);
            }
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            // 通过重绘来不断调用computeScroll
            invalidate();
        } else {
            // 此处修正位置
            if (mScaleMargin / 2 > (mScroller.getFinalX() % mScaleMargin)) {
                scrollTo(mScroller.getCurrX() - (mScroller.getFinalX() % mScaleMargin), mScroller.getCurrY());
                // 回调滑动刻度
                if (mScrollListener != null) {
                    mScrollListener.onScaleScroll(mScroller.getFinalX() / mScaleMargin * unitValue);
                }
            } else {
                scrollTo(mScroller.getCurrX() + (mScaleMargin - (mScroller.getFinalX() % mScaleMargin)), mScroller.getCurrY());
                // 回调滑动刻度
                if (mScrollListener != null) {
                    mScrollListener.onScaleScroll(mScroller.getFinalX() / mScaleMargin * unitValue + unitValue);
                }
            }
        }
    }

    /**
     * 设置回调监听
     */
    public void setOnScrollListener(OnScrollListener listener) {
        this.mScrollListener = listener;
    }

    public interface OnScrollListener {
        void onScaleScroll(int value);
    }
}
