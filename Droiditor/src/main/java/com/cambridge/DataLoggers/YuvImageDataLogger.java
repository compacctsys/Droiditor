package com.cambridge.DataLoggers;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Base64;

import com.cambridge.Auditor;
import com.cambridge.CaptureMechanisms.Camera.CameraShotCapture;
import com.cambridge.utils.CaptureMechanismEventData;
import com.cambridge.utils.ImageWrapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class YuvImageDataLogger implements IDataLogger {

    public YuvImageDataLogger() {
    }

    @Override
    public void initialize(String path) {

    }

    @Override
    public void log(String name, CaptureMechanismEventData data, boolean offload) {

        ImageWrapper imageWrapper = (ImageWrapper) data.getData();

        CameraShotCapture.ImageData imageData = (CameraShotCapture.ImageData) imageWrapper.imageData;
        YuvImage yuvImage = new YuvImage(imageData.getNV21Bytes(), ImageFormat.NV21, imageData.width, imageData.height, null);

        if (offload) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), imageWrapper.quality, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
            Auditor.INSTANCE().dataOffloader.send("image", encoded);
            return;
        }

        File file = new File(data.filePath);
        FileOutputStream fOut = null;

        try {
            fOut = new FileOutputStream(file);

            yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), imageWrapper.quality, fOut);
            fOut.flush();
            fOut.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
