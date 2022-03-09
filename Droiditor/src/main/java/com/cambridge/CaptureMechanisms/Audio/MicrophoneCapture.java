package com.cambridge.CaptureMechanisms.Audio;

import android.media.MediaRecorder;
import android.util.Log;

import com.cambridge.CaptureMechanisms.CaptureMechanismBase;
import com.cambridge.CaptureMechanisms.ICaptureMechanism;
import com.cambridge.Config.IConfig;
import com.cambridge.DataLoggers.IDataLogger;
import com.cambridge.DataTransformers.IDataTransformer;
import com.cambridge.utils.AuditHelpers;
import com.cambridge.utils.CaptureMechanismEventData;

import java.io.IOException;

public class MicrophoneCapture extends CaptureMechanismBase implements ICaptureMechanism {

    private MicrophoneConfig config = new MicrophoneConfig();
    private static String TAG = MicrophoneCapture.class.getSimpleName();
    private MediaRecorder recorder = null;

    @Override
    public void setDataLogger(IDataLogger logger) {
        this.logger = logger;
    }

    @Override
    public void setDataTransformer(IDataTransformer transformer) {
this.transformer = transformer;
    }

    public void stop() {

        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    @Override
    public String getName() {
        return MicrophoneCapture.class.getName();
    }

    @Override
    public void initialise() {
        if(config.enabled == false){return;}
    }

    public void start() {
        if(config.enabled == false){return;}
        String path = config.output_dir + "/"  + (config.filename_prefix + "_" + AuditHelpers.auditSessionId + "_" + System.nanoTime() + ".3gp");
        String fileName = AuditHelpers.getPath(path);
        startAudioRecording(fileName);
    }

    private void startAudioRecording(String fileName) {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        recorder.start();
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
