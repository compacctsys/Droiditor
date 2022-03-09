package com.cambridge.CaptureMechanisms.Metrics;

import android.app.ActivityManager;
import android.content.Context;

import com.cambridge.CaptureMechanisms.CaptureMechanismBase;
import com.cambridge.CaptureMechanisms.ICaptureMechanism;
import com.cambridge.Config.IConfig;
import com.cambridge.DataLoggers.IDataLogger;
import com.cambridge.DataTransformers.IDataTransformer;
import com.cambridge.utils.AuditHelpers;
import com.cambridge.utils.CaptureMechanismEventData;
import com.cambridge.utils.GLOBALS;

import java.util.concurrent.atomic.AtomicBoolean;

public class MemoryCapture extends CaptureMechanismBase implements ICaptureMechanism {
    ActivityManager activityManager;
    private ActivityManager.MemoryInfo memoryInfo;
    private final AtomicBoolean running = new AtomicBoolean(false);
    MemoryCaptureConfig config = new MemoryCaptureConfig();

    @Override
    public void start() {

        if (config.enabled == false) {
            return;
        }
        activityManager = (ActivityManager) GLOBALS.MAIN_ACTIVITY.getSystemService(Context.ACTIVITY_SERVICE);
        memoryInfo = new ActivityManager.MemoryInfo();
        Thread runThread = new Thread(new Runnable() {
            @Override
            public void run() {
                running.set(true);
                while (running.get()) {
                    activityManager.getMemoryInfo(memoryInfo);
                    //            https://stackoverflow.com/a/3192348/596841
                    String message = String.format("%d,%d,%b,%d", memoryInfo.totalMem, memoryInfo.availMem, memoryInfo.lowMemory, memoryInfo.threshold);
                    CaptureMechanismEventData data = new CaptureMechanismEventData(message);
                    onDataReady(data);

                    try {
                        Thread.sleep(config.updateRateMilliseconds);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        runThread.start();

    }

    @Override
    public void stop() {
        running.set(false);
    }

    @Override
    public void initialise() {
        if (config.enabled == false) {
            return;
        }
        memoryInfo = new ActivityManager.MemoryInfo();
        String header = "total_memory, available_memory, threshold";
        String path = config.output_dir + "/" + (config.filename_prefix + "_" + AuditHelpers.auditSessionId + "_" + System.nanoTime() + ".data");
        this.logger.initialize(AuditHelpers.getPath(path));
        activityManager = (ActivityManager) GLOBALS.MAIN_ACTIVITY.getSystemService(Context.ACTIVITY_SERVICE);
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
        return MemoryCapture.class.getName();
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
        return false;
    }

    @Override
    public void onDataReady(CaptureMechanismEventData data) {

        super.transformer.transform(data);
        super.logger.log("", data, config.offloadData);
    }


}
