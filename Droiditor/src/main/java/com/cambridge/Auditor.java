package com.cambridge;

import static com.cambridge.utils.GLOBALS.REQUEST_MEDIA_PROJECTION;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cambridge.CaptureMechanisms.ICaptureMechanism;
import com.cambridge.DataLoggers.IDataLogger;
import com.cambridge.DataTransformers.IDataTransformer;
import com.cambridge.utils.AuditHelpers;
import com.cambridge.utils.DataOffloader;
import com.cambridge.utils.GLOBALS;

import org.apache.commons.io.IOUtils;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


public class Auditor {

    public MediaProjectionManager projectionManager;
    private static String TAG = Auditor.class.getSimpleName();
    private static Auditor _instance = null;
    private static boolean mediaProjectionPermissionsGranted = false;
    private String baseAuditDataPath = "AUDIT_DATA";
    private boolean mediaProjectionPermissionsRequested = false;
    private Handler handler = new Handler();
    public DataOffloader dataOffloader = new DataOffloader();
    private final AtomicBoolean zmqMessageThreadRunning = new AtomicBoolean(false);
    private ZContext ZMQcontext;
    private ZMQ.Socket socket;
    public Map<String, ICaptureMechanism> registeredCaptureMechanisms = new HashMap<String, ICaptureMechanism>();

    /**********************************************************************************************
     * *******************************************************************************************
     * AUDIT CAPTURE STUFF
     * *******************************************************************************************
     * ********************************************************************************************/

    public static Auditor INSTANCE() {
        if (_instance == null)
            _instance = new Auditor();

        return _instance;
    }

    public <T> T instantiate(final String className, final Class<T> type) {
        try {
            return type.cast(Class.forName(className).newInstance());
        } catch (InstantiationException
                | IllegalAccessException
                | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public void initialize(AppCompatActivity act, String baseDataPath, String config) {

        baseAuditDataPath = baseDataPath;

        GLOBALS.MAIN_ACTIVITY = act;
        GLOBALS.DISPLAY_METRICS = new DisplayMetrics();
        GLOBALS.MAIN_ACTIVITY.getWindowManager().getDefaultDisplay().getMetrics(GLOBALS.DISPLAY_METRICS);
        GLOBALS.ROOT_VIEW = GLOBALS.MAIN_ACTIVITY.getWindow().getDecorView().getRootView();
        //ArrayList<SurfaceView> xx= AuditHelpers.getViewsByType((ViewGroup) GLOBALS.ROOT_VIEW, SurfaceView.class);
        //Log.e(TAG, String.valueOf(xx.size()));
        //GLOBALS.SURFACE_VIEW = xx.get(0);
        GLOBALS.BASE_AUDIT_DATA_PATH = act.getExternalFilesDir(null) + File.separator + baseAuditDataPath;

        requestMediaProjectionPermission();
        registerCaptureMechanisms(config);

        initZMQ();
        ZMQMessageThread();

        Log.i(TAG, "Initialization complete...");

    }

    public void requestMediaProjectionPermission() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (projectionManager == null) {
                    if (GLOBALS.MAIN_ACTIVITY instanceof Activity) {
                        projectionManager = (MediaProjectionManager) GLOBALS.MAIN_ACTIVITY.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                        Intent permissionIntent = projectionManager != null ? projectionManager.createScreenCaptureIntent() : null;
                        GLOBALS.MAIN_ACTIVITY.startActivityForResult(permissionIntent, REQUEST_MEDIA_PROJECTION);
                        mediaProjectionPermissionsRequested = true;
                    }
                }
            }
        }).start();
    }

    private void registerCaptureMechanisms(String config) {

        CaptureMechanismsRegistry registry = new CaptureMechanismsRegistry();

        try {
            for (String line : registry.capture_mechanisms) {

                line = line.trim();
                String[] parts = line.split(",");

                String captureMechanismName = parts[0].trim();
                ICaptureMechanism captureMechanism = instantiate(captureMechanismName, ICaptureMechanism.class);
                captureMechanism.parseConfig(config);

                String dataLoggerName = parts[2].trim();
                IDataLogger dataLogger = instantiate(dataLoggerName, IDataLogger.class);
                captureMechanism.setDataLogger(dataLogger);

                String dataTransformerName = parts[1].trim();
                IDataTransformer dataTransformer = instantiate(dataTransformerName, IDataTransformer.class);
                captureMechanism.setDataTransformer(dataTransformer);

                captureMechanism.initialise();

                registeredCaptureMechanisms.put(captureMechanismName, captureMechanism);
            }
            Log.i(TAG, "REGISTERED " + registry.capture_mechanisms.size() + " capture mechanisms...");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerCaptureMechanism(String captureMechanismPath, String config) {
        try {
            ICaptureMechanism captureMechanism = instantiate(captureMechanismPath, ICaptureMechanism.class);
            captureMechanism.parseConfig(config);
            captureMechanism.initialise();
            registeredCaptureMechanisms.put(captureMechanismPath, captureMechanism);
            Log.i(TAG, "ADDED custom capture mechanism: " + captureMechanism.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start(int delaySeconds, int durationSeconds) {

        ZMQMessageThread();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, ICaptureMechanism> entry : registeredCaptureMechanisms.entrySet()) {
                    ICaptureMechanism captureMechanism = entry.getValue();
                    captureMechanism.start();
                    Log.i(TAG, "STARTED capture mechanism: " + captureMechanism.getName());
                }
            }
        };

        handler.postDelayed(r, delaySeconds * 1000); // <-- the "1000" is the delay time in miliseconds.

        //Stop audit after certain duration
        handler.postDelayed(auditStopRunnable, (delaySeconds + durationSeconds) * 1000);
    }

    public void start() {
        ZMQMessageThread();
        for (Map.Entry<String, ICaptureMechanism> entry : registeredCaptureMechanisms.entrySet()) {
            ICaptureMechanism captureMechanism = entry.getValue();
            captureMechanism.start();
            Log.i(TAG, "STARTED capture mechanism: " + captureMechanism.getName());
        }
    }

    public void startCaptureMechanism(String captureMechanismPath) {
        for (Map.Entry<String, ICaptureMechanism> entry : registeredCaptureMechanisms.entrySet()) {
            ICaptureMechanism captureMechanism = entry.getValue();
            if (captureMechanism.getName().equals(captureMechanismPath)) {
                captureMechanism.start();
                Log.i(TAG, "STARTED capture mechanism: " + captureMechanism.getName());
            }
        }
    }

    void initZMQ() {
        ZMQcontext = new ZContext();
        socket = ZMQcontext.createSocket(SocketType.SUB);
        socket.bind("tcp://*:6666");
        socket.subscribe("".getBytes());
        zmqMessageThreadRunning.set(false);
    }

    void ZMQMessageThread() {
        zmqMessageThreadRunning.set(false);
        Thread zmqMessageThread = new Thread(new Runnable() {
            @Override
            public void run() {
                zmqMessageThreadRunning.set(true);
                while (zmqMessageThreadRunning.get()) {
                    String reply = socket.recvStr();
                    processZMQMessage(reply);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        zmqMessageThread.start();

    }

    void processZMQMessage(String zmqmessage) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String[] messageParts = zmqmessage.split("#sep#");
                String command = messageParts[0];
                switch (command) {
                    case "CONFIG":
                        String message = messageParts[1];
                        for (Map.Entry<String, ICaptureMechanism> entry : registeredCaptureMechanisms.entrySet()) {
                            ICaptureMechanism captureMechanism = entry.getValue();
                            try {
                                ZMQMessageThread();
                                captureMechanism.stop();
                                captureMechanism.parseConfig(message);
                                captureMechanism.initialise();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.i(TAG, "UPDATED capture mechanism: " + captureMechanism.getName());
                        }
                        break;

                    case "START":
                        start();
                        break;

                    case "START-D":
                        int delay = Integer.parseInt(messageParts[1]);
                        int duration = Integer.parseInt(messageParts[2]);
                        start(delay, duration);
                        break;

                    case "STOP":
                        stopAudit();
                        break;
                }
            }
        });
        thread.start();
    }

    public void stopAudit() {

        zmqMessageThreadRunning.set(false);

        for (Map.Entry<String, ICaptureMechanism> entry : registeredCaptureMechanisms.entrySet()) {
            ICaptureMechanism captureMechanism = entry.getValue();
            captureMechanism.stop();
            Log.i(TAG, "STOPPED capture mechanism: " + captureMechanism.getName());
        }

    }

    Runnable auditStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopAudit();
            Toast.makeText(GLOBALS.MAIN_ACTIVITY.getApplicationContext(), "Stopping Droiditor...", Toast.LENGTH_LONG).show();
        }
    };

    public void stopCaptureMechanism(String captureMechanismName) {
        for (Map.Entry<String, ICaptureMechanism> entry : registeredCaptureMechanisms.entrySet()) {
            ICaptureMechanism captureMechanism = entry.getValue();
            if (captureMechanism.getName().equals(captureMechanismName)) {
                captureMechanism.stop();
                Log.i(TAG, "STOPPED capture mechanism: " + captureMechanism.getName());
            }
        }

    }

    public void giveMediaProjectionPermission(int resultCode, Intent data) {
        GLOBALS.mediaProjectionResultCode = resultCode;
        GLOBALS.mediaProjectionResultData = data;
        mediaProjectionPermissionsGranted = true;
    }


}
