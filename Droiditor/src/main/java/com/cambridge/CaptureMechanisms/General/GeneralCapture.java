package com.cambridge.CaptureMechanisms.General;

import android.util.Log;

import com.cambridge.CaptureMechanisms.CaptureMechanismBase;
import com.cambridge.CaptureMechanisms.ICaptureMechanism;
import com.cambridge.Config.IConfig;
import com.cambridge.DataLoggers.IDataLogger;
import com.cambridge.DataTransformers.IDataTransformer;
import com.cambridge.utils.AuditHelpers;
import com.cambridge.utils.CaptureMechanismEventData;

public class GeneralCapture extends CaptureMechanismBase implements ICaptureMechanism {

    private GeneralCaptureConfig config = new GeneralCaptureConfig();


    @Override
    public void start() {
    }

    @Override
    public void stop() {

    }

    @Override
    public void initialise() throws Exception {
        if (config.enabled == false) return;
        String path = config.output_dir + "/" + (config.filename_prefix + "_" + AuditHelpers.auditSessionId + "_" + System.nanoTime() + ".json");
        this.logger.initialize(AuditHelpers.getPath(path));
        Log.e("GENERAL CAPTURE", "INITIALIZED!!");
    }

    @Override
    public void setDataLogger(IDataLogger logger) {this.logger = logger;}

    @Override
    public void setDataTransformer(IDataTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public String getName() {
        return GeneralCapture.class.getName();
    }

    @Override
    public IConfig parseConfig(String config_string) {
        config.parse(config_string);
        this.enabled = config.enabled;
        this.output_dir = config.output_dir;
        this.filename_prefix = config.filename_prefix;
        Log.e("GENERAL CAPTURE: ENABLED", String.valueOf(this.enabled));
        return config;
    }

    @Override
    public boolean requiresMediaProjection() {return false;}

    @Override
    public void onDataReady(CaptureMechanismEventData captureMechanismEventData) {
        super.transformer.transform(captureMechanismEventData);
        super.logger.log("", captureMechanismEventData, config.offloadData);
    }

    public void Log(String message){
        onDataReady(new CaptureMechanismEventData(message));
        Log.e("GENERAL LOGGER", "Logged message!");
    }
}
