package com.cambridge.utils;

import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class GLOBALS {

    public static final int REQUEST_MEDIA_PROJECTION = 1010;
    public static final int PROXY_REQUST_CODE = 3015;

    public static AppCompatActivity MAIN_ACTIVITY;
    public static String BASE_AUDIT_DATA_PATH;
    public static DisplayMetrics DISPLAY_METRICS;
    public static View ROOT_VIEW;
    public static SurfaceView SURFACE_VIEW;
    public static ENUMS.MODE MODE = ENUMS.MODE.DEFAULT;

    public static int mediaProjectionResultCode;
    public static Intent mediaProjectionResultData;

}
