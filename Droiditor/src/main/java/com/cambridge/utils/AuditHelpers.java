package com.cambridge.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class AuditHelpers {

    public static int auditSessionId = new Random().nextInt(Integer.SIZE) + 1;

    //https://stackoverflow.com/a/45783746/596841
    public static <T extends SurfaceView> ArrayList<T> getViewsByType(ViewGroup root, Class<T> tClass) {
        final ArrayList<T> result = new ArrayList<>();
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup)
                result.addAll(getViewsByType((ViewGroup) child, tClass));

            if (tClass.isInstance(child))
                result.add(tClass.cast(child));
        }
        return result;
    }

    public static String getPath(String filename, String extension) {

        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.e("ExternalStorage", "Failed to get External Storage");
            return null;
        }

        String fName = (filename + "_" + auditSessionId + "_" + System.nanoTime() + "." + extension);
        return getAuditDataDir() + fName;
    }

    public static String getPath(String path){
        String fullpath = getAuditDataDir() + path;
        File file = new File(fullpath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        return fullpath;
    }

    public static String getAuditDataDir() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.e("ExternalStorage", "Failed to get External Storage");

            return null;
        }

        final File folder = new File(GLOBALS.BASE_AUDIT_DATA_PATH);

        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            return GLOBALS.BASE_AUDIT_DATA_PATH + File.separator;
        } else {
            Log.e("AUDITOR", "Failed to create AUDIT_RESULTS directory");

        }
        return null;
    }

    public static void store(Bitmap bm, String fileName, int quality) {
        File file = new File(fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, quality, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void storeAsync(Bitmap bitmap, String fileName, int quality) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(fileName);
                try {
                    FileOutputStream fOut = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fOut);
                    fOut.flush();
                    fOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static Notification getNotification(Context context) {
        String CHANNEL_ONE_ID = "com.primedu.cn";
        String CHANNEL_ONE_NAME = "Channel One";
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }
        Notification notification = new Notification.Builder(context).setChannelId(CHANNEL_ONE_ID)
                .setTicker("Nature")
                .setContentTitle("PlayInAudio")
                .setContentText("获取游戏声音")
                .getNotification();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        return notification;
    }

    public static List<Integer> getAppUid(Context context) {
        List<Integer> uids = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(packageInfo.packageName, 0);
                    uids.add(ai.uid);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return uids;
    }

}
