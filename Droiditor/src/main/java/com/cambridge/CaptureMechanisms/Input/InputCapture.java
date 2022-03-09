package com.cambridge.CaptureMechanisms.Input;

import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;

import com.cambridge.CaptureMechanisms.CaptureMechanismBase;
import com.cambridge.CaptureMechanisms.ICaptureMechanism;
import com.cambridge.Config.IConfig;
import com.cambridge.DataLoggers.IDataLogger;
import com.cambridge.DataTransformers.IDataTransformer;
import com.cambridge.utils.AuditHelpers;
import com.cambridge.utils.CaptureMechanismEventData;
import com.cambridge.utils.GLOBALS;

public class InputCapture extends CaptureMechanismBase implements ICaptureMechanism {

    //SURFACE VIEW STUFF IS FOR AR

    InputCaptureConfig config = new InputCaptureConfig();

    @Override
    public void start() {
        if (config.enabled == false) {
            return;
        }
        GLOBALS.ROOT_VIEW.setOnTouchListener(androidTouchHandler);
        //GLOBALS.SURFACE_VIEW.setOnTouchListener(xrTouchHandler);
    }

    @Override
    public void stop() {
        GLOBALS.ROOT_VIEW.setOnTouchListener(null);
        //GLOBALS.SURFACE_VIEW.setOnTouchListener(null);
    }

    @Override
    public void initialise() throws Exception {
        if (config.enabled == false) {
            return;
        }
        String header = "x, y, action";
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
        return InputCapture.class.getName();
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
        data = super.transformer.transform(data);
        super.logger.log("", data, config.offloadData);
    }


    private View.OnTouchListener androidTouchHandler = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return logOnTouch(v, event);
        }
    };

    private SurfaceView.OnTouchListener xrTouchHandler = new SurfaceView.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return logOnTouch(v, event);
        }
    };

    boolean logOnTouch(View v, MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();

        String action = "";
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                break;
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                break;
        }

        String message = String.format("%d, %d, %s", x, y, action);

        CaptureMechanismEventData data = new CaptureMechanismEventData(message);
        onDataReady(data);
        return true;
    }
}
