package com.zwl.materviewlib;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


import java.util.ArrayList;
import java.util.List;

public class MaterView extends View {
    private Context mContext;
    private Paint mBacPaint;
    private Paint mLinePaint;
    private Paint mCenterCirclePaint;
    private Paint mTextPaint;
    private Paint mIntervalTextPaint;
    private Paint mUnitTextPaint;
    private Paint mTopPaint;
    private int mWidth = 0;
    private int mHeight = 0;
    private float mCircleRadius = 0;
    private float mPadding = 25;
    private float mLineWidth = 0;
    private Bitmap mPointerBitmap;
    private List<String> mScaleList = new ArrayList<>();
    private String mBottomText = "";
    private float mProcessValue = 0;
    private String mProcessValueStr = "";
    private String mUnit = "";
    //指针角度
    private float mProcessDegress = -135;
    //进度条角度
    private float mProcessCircleDegress = 0;

    private float mSmallTextSize = 0;
    private float mBigTextSize = 0;

    private Matrix mBitmapMatrix;
    private Rect mTextRect;
    private boolean drawRed;

    public MaterView(Context context) {
        super(context);
        init(context, null);
    }

    public MaterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MaterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
        String ratio = null;
        mTextRect = new Rect();
        mBitmapMatrix = new Matrix();
        mPointerBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.icon_pointer);

        for (int i = 0; i < 110; i += 10) {
            mScaleList.add(String.valueOf(i));
        }
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Meter_View);
            mSmallTextSize = array.getDimension(R.styleable.Meter_View_smallTextSize, 0);
            mBigTextSize = array.getDimension(R.styleable.Meter_View_bigTextSize, 0);
            mPadding = array.getDimension(R.styleable.Meter_View_padding, 0);
            mLineWidth = array.getDimension(R.styleable.Meter_View_width, 0);
            ratio = array.getString(R.styleable.Meter_View_pointer_ratio);
            mBottomText = array.getString(R.styleable.Meter_View_bottom_text);
            if (mBottomText == null) {
                mBottomText = "";
            }
        }
        if (mPointerBitmap != null) {
            float scale = 0.6f;
            if (ratio != null && ratio.length() > 0) {
                try {
                    scale = Float.parseFloat(ratio);
                } catch (Exception e) {
                    scale = 1;
                }
            }
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale); // 长和宽放大缩小的比例
            mPointerBitmap = Bitmap.createBitmap(mPointerBitmap, 0, 0, mPointerBitmap.getWidth(),
                    mPointerBitmap.getHeight(), matrix, true);
        }
        mBacPaint = new Paint();
        mBacPaint.setColor(Color.parseColor("#efefef"));
        mBacPaint.setStrokeWidth(mLineWidth);
        mBacPaint.setStyle(Paint.Style.STROKE);


        mTopPaint = new Paint();
        mTopPaint.setColor(Color.parseColor("#0099FF"));
        mTopPaint.setStrokeWidth(mLineWidth);
        mTopPaint.setStyle(Paint.Style.STROKE);


        mLinePaint = new Paint();
        mLinePaint.setColor(Color.parseColor("#198AFA"));
        mLinePaint.setStrokeWidth(3);
        mLinePaint.setStyle(Paint.Style.STROKE);

        mCenterCirclePaint = new Paint();
        mCenterCirclePaint.setColor(Color.parseColor("#ffffff"));
        mLinePaint.setStyle(Paint.Style.FILL);


        mTextPaint = new Paint();
        mTextPaint.setColor(Color.parseColor("#333333"));
        mTextPaint.setStrokeWidth(1);
        mTextPaint.setTextSize(mSmallTextSize);
        mTextPaint.setStyle(Paint.Style.FILL);


        mIntervalTextPaint = new Paint();
        mIntervalTextPaint.setColor(Color.parseColor("#75B8FA"));
        mIntervalTextPaint.setStrokeWidth(1);
        mIntervalTextPaint.setTextSize(mSmallTextSize);
        mIntervalTextPaint.setStyle(Paint.Style.FILL);


        mUnitTextPaint = new Paint();
        mUnitTextPaint.setColor(Color.parseColor("#75B8FA"));
        mUnitTextPaint.setStrokeWidth(1);
        mUnitTextPaint.setTextSize(mSmallTextSize);
        mUnitTextPaint.setStyle(Paint.Style.FILL);
        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        mUnitTextPaint.setTypeface(font);

    }

    private int measureWidth(int defaultWidth, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.AT_MOST:
                defaultWidth = getPaddingLeft() + getPaddingRight();
                break;
            case MeasureSpec.EXACTLY:
                defaultWidth = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                defaultWidth = Math.max(defaultWidth, specSize);
        }
        return defaultWidth;
    }


    private int measureHeight(int defaultHeight, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.AT_MOST:
                defaultHeight = getPaddingTop() + getPaddingBottom();
                break;
            case MeasureSpec.EXACTLY:
                defaultHeight = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                defaultHeight = Math.max(defaultHeight, specSize);
                break;
        }
        return defaultHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();
        int width = measureWidth(minimumWidth, widthMeasureSpec);
        // int height = measureHeight(minimumHeight, heightMeasureSpec);
        mWidth = width;
        mHeight = mWidth;
        mCircleRadius = (float) (mWidth / 2 - mPadding);
        setMeasuredDimension(width, width);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float bottomTextY = 0;
        for (int i = 0; i < mScaleList.size(); i++) {
            double degrees = 45 - (i * (270 / (mScaleList.size() - 1)));
            double radians = Math.toRadians(degrees);
            float x = (float) (Float.valueOf(mWidth) / 2f - (Math.cos(radians) * mCircleRadius));
            float y = (float) (Float.valueOf(mHeight) / 2f + (Math.sin(radians) * mCircleRadius));
            if (i == 0) {
                bottomTextY = y;
            }
            canvas.drawLine(x, y, mWidth / 2, mHeight / 2, mLinePaint);
        }
        canvas.drawArc(mPadding, mPadding, mWidth - mPadding, mHeight - mPadding, 135, 270, false, mBacPaint);
        canvas.drawCircle(mWidth / 2, mHeight / 2, mCircleRadius - mPadding / 2, mCenterCirclePaint);
        mIntervalTextPaint.setTextSize(mSmallTextSize);
        for (int i = 0; i < mScaleList.size(); i++) {
            double degrees = 45 - (i * (270 / (mScaleList.size() - 1)));
            double radians = Math.toRadians(degrees);
            float x = (float) (Float.valueOf(mWidth) / 2f - (Math.cos(radians) * (mCircleRadius - (mPadding * 2) + mPadding)));
            float y = (float) (Float.valueOf(mHeight) / 2f + (Math.sin(radians) * (mCircleRadius - (mPadding * 2) + mPadding)));
            String text = mScaleList.get(i);
            mIntervalTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
            int w = mTextRect.width();
            int h = mTextRect.height();
            float textX = 0;
            float textY = 0;
            if ((int) x > mWidth / 2) {
                textX = x - (w / 4) * 3;
            } else if ((int) x < mWidth / 2) {
                textX = x - w / 4;
            } else {
                textX = x - w / 2;
            }
            if (y > mHeight / 2) {
                textY = y;
            } else if (y < mHeight / 2) {
                if ((int) x == mWidth / 2) {
                    textY = y + h / 2;
                } else {
                    textY = y + h;
                }
                if (i == 4 || i == 6) {
                    textY += h / 4;
                }

            } else {
                textY = y;
            }
            canvas.drawText(text, textX, textY, mIntervalTextPaint);
        }
        canvas.drawArc(mPadding, mPadding, mWidth - mPadding, mHeight - mPadding, 135, mProcessCircleDegress, false, mTopPaint);
        mUnitTextPaint.setTextSize(mBigTextSize);
        mUnitTextPaint.getTextBounds(mUnit, 0, mUnit.length(), mTextRect);
        int width = mTextRect.width();
        int height = mTextRect.height();
        canvas.drawText(mUnit, mWidth / 2 - width / 2, mHeight / 4 + height * 2, mUnitTextPaint);
        mTextPaint.setTextSize(mBigTextSize);
        mTextPaint.getTextBounds(mBottomText, 0, mBottomText.length(), mTextRect);
        int w = mTextRect.width();
        int h = mTextRect.height();

        mTextPaint.getTextBounds(mProcessValueStr, 0, mProcessValueStr.length(), mTextRect);
        int w1 = mTextRect.width();
        if (drawRed) {
            mTextPaint.setColor(Color.parseColor("#F82929"));
        }
        canvas.drawText(mProcessValue <= 0 ? "-" : mProcessValueStr, mWidth / 2 - w1 / 2, bottomTextY - h / 2 + 20, mTextPaint);
        mTextPaint.setColor(Color.parseColor("#333333"));
        canvas.drawText(mBottomText, mWidth / 2 - w / 2, mHeight - h, mTextPaint);
        mBitmapMatrix.setRotate(mProcessDegress, (float) mPointerBitmap.getWidth() / 2, (float) mPointerBitmap.getHeight() / 2);
        mBitmapMatrix.postTranslate(mWidth / 2 - mPointerBitmap.getWidth() / 2, mHeight / 2 - mPointerBitmap.getHeight() / 2);
        canvas.drawBitmap(mPointerBitmap, mBitmapMatrix, mTopPaint);
    }


    public MaterView setScale(List<String> list) {
        this.mScaleList.clear();
        if (list != null) {
            this.mScaleList.addAll(list);
            invalidate();
        }
        return this;
    }

    public MaterView setUnit(String unit) {
        this.mUnit = unit;
        invalidate();
        return this;
    }

    public MaterView setBottomProcessValue(String valueStr, float cvalue, float minvalue, float maxvalue) {
        mProcessValueStr = valueStr;
        ValueAnimator animator = ValueAnimator.ofFloat((float) mProcessValue, cvalue);
        animator.setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float updateValue = (float) animation.getAnimatedValue();
                mProcessValue = (float) Math.round(updateValue * 100) / 100;
                if (cvalue == 0 || cvalue == -1) {
                    mProcessCircleDegress = 0;
                    mProcessDegress = -135;
                } else {
                    float cdegress = (float) (((updateValue - minvalue) / (maxvalue - minvalue)) * 270);
                    mProcessCircleDegress = cdegress < 0 ? 0 : cdegress;
                    mProcessCircleDegress = mProcessCircleDegress > 270 ? 270 : mProcessCircleDegress;
                    float pdegress = (float) (-135 + ((updateValue - minvalue) / (maxvalue - minvalue)) * 270);
                    mProcessDegress = pdegress < -135 ? -135 : pdegress;
                    mProcessDegress = mProcessDegress > 135 ? 135 : mProcessDegress;
                }
                invalidate();
            }
        });
        animator.start();
        return this;
    }

    public MaterView setBottomText(String bottomText) {
        mBottomText = bottomText;
        invalidate();
        return this;
    }


    public MaterView setDrawRed(boolean drawRed) {
        this.drawRed = drawRed;
        return this;
    }
}


