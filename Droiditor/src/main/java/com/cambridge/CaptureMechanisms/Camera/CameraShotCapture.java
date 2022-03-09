package com.cambridge.CaptureMechanisms.Camera;

import android.media.Image;
import android.os.AsyncTask;

import com.cambridge.CaptureMechanisms.CaptureMechanismBase;
import com.cambridge.CaptureMechanisms.ICaptureMechanism;
import com.cambridge.Config.IConfig;
import com.cambridge.DataLoggers.IDataLogger;
import com.cambridge.DataTransformers.IDataTransformer;
import com.cambridge.utils.AuditHelpers;
import com.cambridge.utils.CaptureMechanismEventData;
import com.cambridge.utils.ImageWrapper;
//import com.google.ar.core.Frame;
//import com.google.ar.core.exceptions.NotYetAvailableException;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class CameraShotCapture extends CaptureMechanismBase implements ICaptureMechanism {

    private CameraShotCaptureConfig config = new CameraShotCaptureConfig();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private static String TAG = CameraShotCapture.class.getSimpleName();

    private int frameCount = 0;

    @Override
    public void start() {
        running.set(true);
    }

    @Override
    public void stop() {
        running.set(false);
    }

    public String getName() {
        return CameraShotCapture.class.getName();
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

    public void process(ImageData data) {

        ImageWrapper imageWrapper = new ImageWrapper();
        imageWrapper.imageData = data;
        imageWrapper.quality = config.quality;

        String path = config.output_dir + "/" + (config.filename_prefix + "_" + AuditHelpers.auditSessionId + "_" + System.nanoTime() + ".jpg");
        String filename = AuditHelpers.getPath(path);

        CaptureMechanismEventData enddata = new CaptureMechanismEventData(imageWrapper);
        enddata.filePath = filename;
        onDataReady(enddata);

    }


    //Only for XR
//    public void captureShotXR(Frame frame) {
//
//        if (frame == null) {
//            return;
//        }
//
//        // If capture mechanism is not enabled, or if 'start' has not been called, just return...
//        if (!config.enabled || !running.get()) {
//            return;
//        }
//
//        if (config.capture_every_nth_frame > frameCount) {
//            frameCount++;
//            return;
//        }
//
//        try {
//            CameraShotCapture.ImageData data = new CameraShotCapture.ImageData(frame.acquireCameraImage());
//            AsyncTask.execute(new Runnable() {
//                @Override
//                public void run() {
//                    process(data);
//                }
//            });
//        } catch (NotYetAvailableException e) {
//            e.printStackTrace();
//        }
//
//        frameCount = 0;
//    }

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


    public class ImageData {

        public int width;
        public int height;

        private ByteBuffer yBuffer;
        private ByteBuffer uBuffer;
        private ByteBuffer vBuffer;

        public ImageData(Image image) {
            // Get the three planes.
            this.yBuffer = image.getPlanes()[0].getBuffer();
            this.uBuffer = image.getPlanes()[1].getBuffer();
            this.vBuffer = image.getPlanes()[2].getBuffer();
            this.width = image.getWidth();
            this.height = image.getHeight();

            image.close();
        }

        public byte[] getNV21Bytes() {
            byte[] nv21;

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            nv21 = new byte[ySize + uSize + vSize];

            //U and V are swapped
            yBuffer.get(nv21, 0, ySize);
            vBuffer.get(nv21, ySize, vSize);
            uBuffer.get(nv21, ySize + vSize, uSize);

            return nv21;

        }
    }

}
