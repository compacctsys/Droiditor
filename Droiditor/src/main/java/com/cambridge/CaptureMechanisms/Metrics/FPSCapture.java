package com.cambridge.CaptureMechanisms.Metrics;

import android.os.SystemClock;

import com.cambridge.CaptureMechanisms.CaptureMechanismBase;
import com.cambridge.CaptureMechanisms.ICaptureMechanism;
import com.cambridge.Config.IConfig;
import com.cambridge.DataLoggers.IDataLogger;
import com.cambridge.DataTransformers.IDataTransformer;
import com.cambridge.utils.AuditHelpers;
import com.cambridge.utils.CaptureMechanismEventData;

import java.util.concurrent.atomic.AtomicBoolean;

public class FPSCapture extends CaptureMechanismBase implements ICaptureMechanism {

    private static String TAG = FPSCapture.class.getSimpleName();
    private long prevRuntime = SystemClock.elapsedRealtime();
    private long prevElapsedRealtime = SystemClock.elapsedRealtime();
    private int fps = 0;
    private float real_fps = 0;
    private int fpsMem = 0;
    FPSCaptureConfig config = new FPSCaptureConfig();
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void start() {
        if (config.enabled == false) {
            return;
        }

        Thread runThread = new Thread(new Runnable() {
            @Override
            public void run() {
                running.set(true);
                while (running.get()) {
                    long elapsedRealtime = SystemClock.elapsedRealtime();
                    if (prevRuntime + 1000 < elapsedRealtime) {
                        prevRuntime = SystemClock.elapsedRealtime();
                        fpsMem = fps;
                        real_fps = (fpsMem / 1000f) * 60;
                        CaptureMechanismEventData data = new CaptureMechanismEventData(real_fps);
                        onDataReady(data);
                        fps = 0;
                    }
                    if (prevElapsedRealtime != elapsedRealtime) {
                        fps++;
                    }
                    prevElapsedRealtime = elapsedRealtime;

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
        String path = config.output_dir + "/" + (config.filename_prefix + "_" + AuditHelpers.auditSessionId + "_" + System.nanoTime() + ".data");
        this.logger.initialize(AuditHelpers.getPath(path));

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
        return FPSCapture.class.getName();
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
