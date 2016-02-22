package com.example.xyzreader.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;


public class DynamicHeightNetworkImageView extends NetworkImageView {
    private float mAspectRatio = 1.5f;
    private Context mContext;

    public DynamicHeightNetworkImageView(Context context) {
        super(context);
        initImageView(context);
    }

    public DynamicHeightNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initImageView(context);
    }

    public DynamicHeightNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initImageView(context);
    }

    public void initImageView(Context context){
        mContext = context;
    }

    public void setAspectRatio(float aspectRatio) {
        mAspectRatio = aspectRatio;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float measuredWidth = getMeasuredWidth();
        float adjustedHeight = (measuredWidth / mAspectRatio);
        setMeasuredDimension((int)measuredWidth, (int) adjustedHeight);
    }
}
