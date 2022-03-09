package com.cambridge.CaptureMechanisms.Screen;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.cambridge.Auditor;
import com.cambridge.utils.GLOBALS;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;


//https://github.com/coderJohnZhang/ScreenRecord

public class ScreenRecordService extends Service {

    private static final String TAG = ScreenRecordService.class.getSimpleName();

    /**
     * 是否为标清视频
     */
    private boolean isVideoSd;
    /**
     * 是否开启音频录制
     */
    private boolean isAudio;
    private final IBinder mBinder = new ScreenRecordService.LocalBinder();
    private MediaProjection mMediaProjection;
    private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;

    String filename = "";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "Service onCreate() is called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "Service onStartCommand() is called");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelId = "001";
            String channelName = "myChannel";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Notification notification;

                notification = new Notification.
                        Builder(getApplicationContext(), channelId).setOngoing(true).setCategory(Notification.CATEGORY_SERVICE).build();

                startForeground(101, notification);
            }
        } else {
            startForeground(101, new Notification());
        }

        return Service.START_STICKY;
    }

    private MediaProjection createMediaProjection() {
        Log.i(TAG, "Create MediaProjection");
        return Auditor.INSTANCE().projectionManager.getMediaProjection(GLOBALS.mediaProjectionResultCode, GLOBALS.mediaProjectionResultData);
    }

    public void Start(String path, boolean audio) {
        isVideoSd = true;
        isAudio = audio;
        filename = path;
        mMediaProjection = createMediaProjection();
        mMediaRecorder = createMediaRecorder();
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
    }

    private MediaRecorder createMediaRecorder() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        String curTime = formatter.format(curDate).replace(" ", "");
        String videoQuality = "HD";
        if (isVideoSd) {
            videoQuality = "SD";
        }

        Log.i(TAG, "Create MediaRecorder");
        MediaRecorder mediaRecorder = new MediaRecorder();
        if (isAudio) {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        }
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(filename);
        mediaRecorder.setVideoSize(GLOBALS.DISPLAY_METRICS.widthPixels, GLOBALS.DISPLAY_METRICS.heightPixels);  //after setVideoSource(), setOutFormat()
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);  //after setOutputFormat()

        if (isAudio) {
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);  //after setOutputFormat()
        }
        int bitRate;
        if (isVideoSd) {
            mediaRecorder.setVideoEncodingBitRate(GLOBALS.DISPLAY_METRICS.widthPixels * GLOBALS.DISPLAY_METRICS.heightPixels);
            mediaRecorder.setVideoFrameRate(30);
            bitRate = GLOBALS.DISPLAY_METRICS.widthPixels * GLOBALS.DISPLAY_METRICS.heightPixels / 1000;
        } else {
            mediaRecorder.setVideoEncodingBitRate(5 * GLOBALS.DISPLAY_METRICS.widthPixels * GLOBALS.DISPLAY_METRICS.heightPixels);
            mediaRecorder.setVideoFrameRate(60); //after setVideoSource(), setOutFormat()
            bitRate = 5 * GLOBALS.DISPLAY_METRICS.widthPixels * GLOBALS.DISPLAY_METRICS.heightPixels / 1000;
        }
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            Log.e(TAG, "createMediaRecorder: e = " + e.toString());
        }
        Log.i(TAG, "Audio: " + isAudio + ", SD video: " + isVideoSd + ", BitRate: " + bitRate + "kbps");

        return mediaRecorder;
    }

    private VirtualDisplay createVirtualDisplay() {
        Log.i(TAG, "Create VirtualDisplay");
        return mMediaProjection.createVirtualDisplay(TAG,
                GLOBALS.DISPLAY_METRICS.widthPixels,
                GLOBALS.DISPLAY_METRICS.heightPixels,
                GLOBALS.DISPLAY_METRICS.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(),
                null, null);
    }

    @Override
    public void onDestroy() {

        Log.i(TAG, "Service onDestroy");

        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
        }
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public ScreenRecordService getService() {
            return ScreenRecordService.this;
        }
    }

}
