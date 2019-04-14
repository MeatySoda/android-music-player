package com.example.myapplication.Util;

import android.app.Activity;
import android.view.WindowManager;

public class MyLight {

    /**
     * 设置手机屏幕亮度变暗
     */
    public static void lightoff(Activity activity) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = 0.3f;
        activity.getWindow().setAttributes(lp);
    }

    /**
     * 设置手机屏幕亮度显示正常
     */
    public static void lighton(Activity activity) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = 1f;
        activity.getWindow().setAttributes(lp);
    }
}
