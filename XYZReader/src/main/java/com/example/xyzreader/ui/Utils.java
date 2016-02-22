package com.example.xyzreader.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;

public class Utils {

    public static float getScreenWidthDpi(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.xdpi;
    }

    public static float getScreenHeightDpi(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.ydpi;
    }

    public static float getDpi(Context context,float px) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float dpi = px / (metrics.densityDpi / 160f);
        return dpi;
    }

    public static float getPx(Context context, float dpi) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float px = dpi * (metrics.densityDpi / 160f);
        return px;
    }

    public static int getCurrentOrientation(Context context){
        return context.getResources().getConfiguration().orientation;
    }

    public static int getCurrentVersion(){
        return android.os.Build.VERSION.SDK_INT;
    }

    public static boolean isDataAvaialable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isDataConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isDataConnected) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE
                    || activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
