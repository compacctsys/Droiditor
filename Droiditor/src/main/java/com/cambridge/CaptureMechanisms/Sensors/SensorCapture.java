package com.cambridge.CaptureMechanisms.Sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.cambridge.CaptureMechanisms.CaptureMechanismBase;
import com.cambridge.CaptureMechanisms.ICaptureMechanism;
import com.cambridge.Config.IConfig;
import com.cambridge.DataLoggers.IDataLogger;
import com.cambridge.DataTransformers.IDataTransformer;
import com.cambridge.utils.AuditHelpers;
import com.cambridge.utils.CaptureMechanismEventData;
import com.cambridge.utils.GLOBALS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SensorCapture extends CaptureMechanismBase implements ICaptureMechanism, SensorEventListener {

    private SensorConfig config = new SensorConfig();
    public SensorManager sensorManager;
    List<Sensor> registeredSensors = new ArrayList<Sensor>();

    @Override
    public void setDataLogger(IDataLogger logger) {
        this.logger = logger;
    }

    @Override
    public void setDataTransformer(IDataTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public void start() {
        if (config.enabled == false) {
            return;
        }
        for (SensorConfig.SensorObject s : config.sensors) {
            //Log.i("ZMQ", "SENSOR ID: " + s.id);
            Sensor sensor = sensorManager.getDefaultSensor(s.id);
            if (sensor == null){
                Log.i("SENSOR_CAPTURE", "sensor is Null!");
            }else{
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                registeredSensors.add(sensor);
            }
        }
    }

    @Override
    public void stop() {
        for (Sensor sensor : registeredSensors) {
            sensorManager.unregisterListener(this, sensor);
        }
    }

    @Override
    public void initialise() throws Exception {
        if (config.enabled == false) return;
        String path = config.output_dir + "/" + (config.filename_prefix + "_" + AuditHelpers.auditSessionId + "_" + System.nanoTime() + ".data");
        this.logger.initialize(AuditHelpers.getPath(path));
        sensorManager = (SensorManager) GLOBALS.MAIN_ACTIVITY.getSystemService(Context.SENSOR_SERVICE);
    }


    @Override
    public String getName() {
        return SensorCapture.class.getName();
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
    public void onSensorChanged(SensorEvent event) {
        String message = Arrays.toString(event.values);
        CaptureMechanismEventData data = new CaptureMechanismEventData(message);
        onDataReady(data);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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

