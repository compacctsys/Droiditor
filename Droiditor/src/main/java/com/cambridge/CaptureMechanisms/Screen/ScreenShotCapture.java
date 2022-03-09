package com.cambridge.CaptureMechanisms.Screen;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.PixelCopy;

import com.cambridge.CaptureMechanisms.CaptureMechanismBase;
import com.cambridge.CaptureMechanisms.ICaptureMechanism;
import com.cambridge.Config.IConfig;
import com.cambridge.DataLoggers.IDataLogger;
import com.cambridge.DataTransformers.IDataTransformer;
import com.cambridge.utils.AuditHelpers;
import com.cambridge.utils.CaptureMechanismEventData;
import com.cambridge.utils.GLOBALS;
import com.cambridge.utils.ImageWrapper;

import java.util.concurrent.atomic.AtomicBoolean;


public class ScreenShotCapture extends CaptureMechanismBase implements ICaptureMechanism {

    private static String TAG = ScreenShotCapture.class.getSimpleName();
    int frameCount = 0;

    private ScreenShotCaptureConfig config = new ScreenShotCaptureConfig();
    private final AtomicBoolean running = new AtomicBoolean(false);

    //https://stackoverflow.com/a/51103037/596841
    public void takeScreenshot() {

        Bitmap bitmap = Bitmap.createBitmap(GLOBALS.ROOT_VIEW.getWidth(), GLOBALS.ROOT_VIEW.getHeight(), Bitmap.Config.ARGB_8888);
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        // Make the request to copy.
        PixelCopy.request(GLOBALS.SURFACE_VIEW, bitmap, (copyResult) -> {
            if (copyResult == PixelCopy.SUCCESS) {

                ImageWrapper imageWrapper = new ImageWrapper();
                imageWrapper.imageData = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                ;
                imageWrapper.quality = config.quality;

                String path = config.output_dir + "/" + (config.filename_prefix + "_" + AuditHelpers.auditSessionId + "_" + System.nanoTime() + ".jpg");
                String filename = AuditHelpers.getPath(path);
                CaptureMechanismEventData data = new CaptureMechanismEventData(imageWrapper);
                data.filePath = filename;
                onDataReady(data);
                frameCount = 0;
            } else {

            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));

    }

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
                    try {
                        takeScreenshot();
                        Thread.sleep(config.capture_delay);
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
        Log.i(TAG, "ScreenShotCapture is not running, so there is nothing to stop.");
    }

    @Override
    public void initialise() {

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
        return ScreenShotCapture.class.getName();
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