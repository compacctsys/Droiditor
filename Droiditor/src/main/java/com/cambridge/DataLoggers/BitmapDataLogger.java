package com.cambridge.DataLoggers;

import android.graphics.Bitmap;
import android.util.Base64;

import com.cambridge.Auditor;
import com.cambridge.utils.CaptureMechanismEventData;
import com.cambridge.utils.ImageWrapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class BitmapDataLogger implements IDataLogger {

    @Override
    public void initialize(String path) {

    }

    @Override
    public void log(String name, CaptureMechanismEventData data, boolean offload) {

        ImageWrapper imageWrapper = (ImageWrapper) data.getData();
        Bitmap bitmap = (Bitmap) imageWrapper.imageData;

        if (offload) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, imageWrapper.quality, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
            Auditor.INSTANCE().dataOffloader.send("image", encoded);
            return;
        }

        File file = new File(data.filePath);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, imageWrapper.quality, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
