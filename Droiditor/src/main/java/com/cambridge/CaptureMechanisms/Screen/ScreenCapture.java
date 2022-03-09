package com.cambridge.CaptureMechanisms.Screen;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.cambridge.CaptureMechanisms.CaptureMechanismBase;
import com.cambridge.CaptureMechanisms.ICaptureMechanism;
import com.cambridge.Config.IConfig;
import com.cambridge.DataLoggers.IDataLogger;
import com.cambridge.DataTransformers.IDataTransformer;
import com.cambridge.utils.AuditHelpers;
import com.cambridge.utils.CaptureMechanismEventData;
import com.cambridge.utils.GLOBALS;

public class ScreenCapture extends CaptureMechanismBase implements ICaptureMechanism {

    private static String TAG = ScreenCapture.class.getSimpleName();

    private ScreenCaptureConfig config = new ScreenCaptureConfig();

    /**********************************************************************************************
     * *******************************************************************************************
     * SCREEN CAPTURE STUFF
     * *******************************************************************************************
     * ********************************************************************************************/

    Intent screenRecorderIntent;
    ScreenRecordService screenRecordService;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ScreenRecordService.LocalBinder binder = (ScreenRecordService.LocalBinder) service;
            screenRecordService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    public void start() {
        if (config.enabled == false) return;

        String path = config.output_dir + "/" + (config.filename_prefix + "_" + AuditHelpers.auditSessionId + "_" + System.nanoTime() + ".mp4");
        String filename = AuditHelpers.getPath(path);
        screenRecordService.Start(filename, false);
    }

    @Override
    public void stop() {
        if (screenRecorderIntent == null) {
            return;
        }
        GLOBALS.MAIN_ACTIVITY.stopService(screenRecorderIntent);
        GLOBALS.MAIN_ACTIVITY.unbindService(mConnection);
    }

    @Override
    public void initialise() {
        screenRecorderIntent = new Intent(GLOBALS.MAIN_ACTIVITY, ScreenRecordService.class);
        GLOBALS.MAIN_ACTIVITY.startService(screenRecorderIntent);
        GLOBALS.MAIN_ACTIVITY.bindService(screenRecorderIntent, mConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void setDataLogger(IDataLogger logger) {
        this.logger = logger;
    }

    @Override
    public void setDataTransformer(IDataTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public String getName() {
        return ScreenCapture.class.getName();
    }

    @Override
    public IConfig parseConfig(String config_string) {
        config.parse(config_string);
        this.enabled = config.enabled;
        this.output_dir = config.output_dir;
        this.filename_prefix = config.filename_prefix;
        return config;
    }

    @Override
    public boolean requiresMediaProjection() {
        return true;
    }

    @Override
    public void onDataReady(CaptureMechanismEventData data) {
        super.transformer.transform(data);
        super.logger.log("", data, config.offloadData);
    }

}
