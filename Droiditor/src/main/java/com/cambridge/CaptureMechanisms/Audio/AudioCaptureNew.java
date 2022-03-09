package com.cambridge.CaptureMechanisms.Audio;

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

public class AudioCaptureNew extends CaptureMechanismBase implements ICaptureMechanism {

    private AudioConfig config = new AudioConfig();

    private AudioCaptureServiceNew audioCaptureService;
    private Intent audioCaptureServiceIntent;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            AudioCaptureServiceNew.LocalBinder binder = (AudioCaptureServiceNew.LocalBinder) service;
            audioCaptureService = binder.getService();

            GLOBALS.MAIN_ACTIVITY.startService(audioCaptureServiceIntent);
            String path = config.output_dir + "/" + (config.filename_prefix + "_" + AuditHelpers.auditSessionId + "_" + System.nanoTime() + ".wav");
            String filename = AuditHelpers.getPath(path);
            audioCaptureService.Start(filename);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    public void setDataLogger(IDataLogger logger) {
        this.logger = logger;
    }

    @Override
    public void setDataTransformer(IDataTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public void initialise() throws Exception {
    }

    @Override
    public String getName() {
        return AudioCaptureNew.class.getName();
    }

    @Override
    public void start() {
        if (config.enabled == false) {
            return;
        }

        audioCaptureServiceIntent = new Intent(GLOBALS.MAIN_ACTIVITY, AudioCaptureServiceNew.class);
        GLOBALS.MAIN_ACTIVITY.bindService(audioCaptureServiceIntent, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void stop() {
        if (audioCaptureService == null) return;
        audioCaptureService.stopRecord();
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
