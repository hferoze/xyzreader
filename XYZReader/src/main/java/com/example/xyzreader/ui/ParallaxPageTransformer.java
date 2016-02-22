package com.example.xyzreader.ui;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.example.xyzreader.R;

public class ParallaxPageTransformer implements ViewPager.PageTransformer {
    private static final float SPEED_FACTOR = 0.5f;

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();
        if (position < -1) {
            view.setAlpha(1);
        } else if (position <= 1) {
            view.findViewById(R.id.header).setTranslationX(-position * (pageWidth) * SPEED_FACTOR);
        } else {
            view.setAlpha(1);
        }
    }
}