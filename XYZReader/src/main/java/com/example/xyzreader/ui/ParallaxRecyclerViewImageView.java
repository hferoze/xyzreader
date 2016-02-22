package com.example.xyzreader.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.android.volley.toolbox.NetworkImageView;
import com.example.xyzreader.R;

public class ParallaxRecyclerViewImageView extends NetworkImageView {

    private RecyclerView mRecyclerView;
    private float mAspectRatio = 1.5f;
    private Context mContext;
    private View mContainer;
    private float mParallaxFactor=0.4f;
    private int mImageVisibilityFactor=60;

    private float mScreenDensity;
    private int mScreenCenter;

    public ParallaxRecyclerViewImageView(Context context) {
        super(context);
        init(context);
    }

    public ParallaxRecyclerViewImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

        if (attrs != null){
            final TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.ParallaxRecyclerViewImageView);
            if (customAttrs != null) {
                if (customAttrs.hasValue(R.styleable.ParallaxRecyclerViewImageView_parallaxFactor)) {
                    setParallaxFactor(customAttrs.getFloat(R.styleable.ParallaxRecyclerViewImageView_parallaxFactor, mParallaxFactor));
                }

                customAttrs.recycle();
            }
        }
    }

    public ParallaxRecyclerViewImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setParallaxFactor (float parallaxFactor){
        if (parallaxFactor<1 && parallaxFactor>0) {
            mParallaxFactor = parallaxFactor;
            mImageVisibilityFactor = (int) ((1 - parallaxFactor) * 100);
        }
    }

    public void init(Context context){
        mContext = context;
        mContainer = (ViewGroup)getParent();
        mScreenDensity = getResources().getDisplayMetrics().density;
        mScreenCenter = (int) getPx(getContext(), getScreenHeightDpi(getContext()))/2;
    }

    public void setContainerAndRecyclerView(View container, RecyclerView recyclerView){
        mContainer = container;
        mRecyclerView = recyclerView;
        mRecyclerView.getViewTreeObserver().addOnScrollChangedListener(recyclerViewScrollerListener);
    }

    ViewTreeObserver.OnScrollChangedListener recyclerViewScrollerListener = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            Rect scrollBounds = new Rect();
            mRecyclerView.getHitRect(scrollBounds);

            if (getLocalVisibleRect(scrollBounds)) {
                int centerOfContainer = mScreenCenter - (mContainer.getHeight()/ 2);
                int[] loc_screen = {0, 0};
                getLocationOnScreen(loc_screen);
                int image_half_height = loc_screen[1] + getHeight() / 2;
                int translateY = ((image_half_height - centerOfContainer) * mImageVisibilityFactor) / mScreenCenter;
                float translateYScaled = translateY * mParallaxFactor * mScreenDensity;
                setTranslationY(-translateYScaled);
            }
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float measuredWidth = getMeasuredWidth();
        float adjustedHeight = (measuredWidth / mAspectRatio);
        setMeasuredDimension((int)measuredWidth, (int) adjustedHeight);
    }

    public void setAspectRatio(float aspectRatio) {
        mAspectRatio = aspectRatio;
        requestLayout();
    }

    private static float getPx(Context context, float dpi) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float px = dpi * (metrics.densityDpi / 160f);
        return px;
    }

    private static float getScreenHeightDpi(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.ydpi;
    }
}
