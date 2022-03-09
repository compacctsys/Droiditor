package com.cambridge.CaptureMechanisms.Audio;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.cambridge.Auditor;
import com.cambridge.utils.AuditHelpers;
import com.cambridge.utils.GLOBALS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class AudioCaptureServiceNew extends Service implements Runnable {

    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_BPP = 16;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static String TAG = AudioCaptureServiceNew.class.getSimpleName();
    private final IBinder mBinder = new AudioCaptureServiceNew.LocalBinder();
    private final int sampleRateInHz = 44100;
    private final int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int mBufferSizeInBytes;
    private AudioRecord mAudioRecord;
    private Thread mCurThread;
    private boolean mRecordState;
    private AudioCaptureServiceNew.ProjectionStateListener mListener;
    private String temp_filename;
    private String output_filename;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, AuditHelpers.getNotification(getApplicationContext()),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "ProjectionService ----> onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "ProjectionService ----> onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void run() {

        Log.e(TAG, "ProjectionService ----> run");

        final byte[] buffer = new byte[mBufferSizeInBytes];
        Log.e(TAG, "Buffer Size PortalAndroidAudioDeviceModule " + mBufferSizeInBytes);
        byte[] data = new byte[mBufferSizeInBytes];

        FileOutputStream os = null;

        try {
            os = new FileOutputStream(temp_filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int read = 0;
        if (null != os) {
            while (mRecordState) {
                read = mAudioRecord.read(data, 0, mBufferSizeInBytes);
                if (read > 0) {
                }

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteTempFile() {
        File file = new File(temp_filename);
        file.delete();
    }

    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = ((RECORDER_CHANNELS == AudioFormat.CHANNEL_IN_MONO) ? 1
                : 2);
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;

        byte[] data = new byte[mBufferSizeInBytes];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while (in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (((RECORDER_CHANNELS == AudioFormat.CHANNEL_IN_MONO) ? 1
                : 2) * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    public void setListener(AudioCaptureServiceNew.ProjectionStateListener listener) {
        this.mListener = listener;
    }

    public boolean isRecording() {
        return mRecordState;
    }

    private MediaProjection mediaProjection;

    private MediaProjection createMediaProjection() {
        Log.i(TAG, "Create MediaProjection");
        return Auditor.INSTANCE().projectionManager.getMediaProjection(GLOBALS.mediaProjectionResultCode, GLOBALS.mediaProjectionResultData);
    }

    private void createFiles(String filename) {
        output_filename = filename;
        temp_filename = filename.replace(".wav", "_temp.raw");
        File file = new File(temp_filename);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
    }

    public void Start(String filename) {
        createFiles(filename);
        stopRecord();
        mBufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        Log.e(TAG, "ProjectionService ----> BufferSizeInBytes: " + mBufferSizeInBytes);

        mediaProjection = createMediaProjection();

        mAudioRecord = createAudioRecoder();
        mAudioRecord.startRecording();
        mRecordState = true;
        mCurThread = new Thread(this);
        mCurThread.start();
        if (null != mListener) mListener.onStateChanged(mRecordState);
    }

    public void stopRecord() {
        Log.e(TAG, "ProjectionService ----> stopRecord");
        mRecordState = false;
        if (null != mCurThread) {
            mCurThread.interrupt();
            Log.e(TAG, "ProjectionService ----> mCurThread.interrupt()");
            File file = new File(temp_filename);
            if (file.exists()) {
                copyWaveFile(temp_filename, output_filename);
                deleteTempFile();
            }
        }
        if (null != mListener) mListener.onStateChanged(mRecordState);


    }

    private AudioRecord createAudioRecoder() {

        if (mediaProjection == null) {
            mediaProjection = createMediaProjection();
        }
        List<Integer> uids = AuditHelpers.getAppUid(getApplicationContext());
        AudioPlaybackCaptureConfiguration.Builder builder = new AudioPlaybackCaptureConfiguration.Builder(mediaProjection);
        for (Integer uid : uids) {
            builder.addMatchingUid(uid);
            builder.addMatchingUsage(AudioAttributes.USAGE_UNKNOWN);
            builder.addMatchingUsage(AudioAttributes.USAGE_GAME);
            builder.addMatchingUsage(AudioAttributes.USAGE_MEDIA);
        }

        if (ActivityCompat.checkSelfPermission(
                GLOBALS.MAIN_ACTIVITY,
                Manifest.permission.RECORD_AUDIO
        )
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    GLOBALS.MAIN_ACTIVITY,
                    new String[]{Manifest.permission.RECORD_AUDIO},1
                );
        }

        AudioRecord audioRecord = new AudioRecord.Builder()
                .setAudioPlaybackCaptureConfig(builder.build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(audioFormat)
                        .setSampleRate(sampleRateInHz)
                        .setChannelMask(channelConfig)
                        .build())
                .setBufferSizeInBytes(mBufferSizeInBytes)
                .build();
        return audioRecord;
    }

    public interface ProjectionStateListener {
        void onStateChanged(boolean recordState);
    }

    public class LocalBinder extends Binder {
        public AudioCaptureServiceNew getService() {
            return AudioCaptureServiceNew.this;
        }
    }

}