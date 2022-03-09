package com.cambridge.CaptureMechanisms.Metrics;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.cambridge.CaptureMechanisms.CaptureMechanismBase;
import com.cambridge.CaptureMechanisms.ICaptureMechanism;
import com.cambridge.Config.IConfig;
import com.cambridge.DataLoggers.IDataLogger;
import com.cambridge.DataTransformers.IDataTransformer;
import com.cambridge.utils.AuditHelpers;
import com.cambridge.utils.CaptureMechanismEventData;
import com.cambridge.utils.GLOBALS;

import java.util.concurrent.atomic.AtomicBoolean;

public class PowerCapture extends CaptureMechanismBase implements ICaptureMechanism {
    static boolean ready = false;
    private final AtomicBoolean running = new AtomicBoolean(false);
    BatteryManager mBatteryManager;
    Intent batteryStatus;

    PowerCaptureConfig config = new PowerCaptureConfig();

    public String chargingMethod() {
        // How are we charging?
        int chargePlug = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == android.os.BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == android.os.BatteryManager.BATTERY_PLUGGED_AC;

        if (usbCharge) {
            return "USB";
        }

        if (acCharge) {
            return "AC";
        }

        return "NO DATA!";
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

                    String message = String.format("%d,%d,%d,%d,%d,%s",
                            mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER),
                            mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW),
                            mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE),
                            mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY),
                            mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER), chargingMethod()
                    );
                    CaptureMechanismEventData data = new CaptureMechanismEventData(message);
                    onDataReady(data);
                    try {
                        Thread.sleep(config.updateRateMilliseconds);
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
    }

    @Override
    public void initialise() {
        if (config.enabled == false) {
            return;
        }
        String header = "charge_counter, current_now, current_average, capacity, energy_counter, charging_method";
        String path = config.output_dir + "/" + (config.filename_prefix + "_" + AuditHelpers.auditSessionId + "_" + System.nanoTime() + ".data");
        this.logger.initialize(AuditHelpers.getPath(path));
//        _dataLogger = new CSVDataLogger(AuditHelpers.getPath(path), header);
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = GLOBALS.MAIN_ACTIVITY.registerReceiver(null, ifilter);
        mBatteryManager = (BatteryManager) GLOBALS.MAIN_ACTIVITY.getSystemService(Context.BATTERY_SERVICE);
        ready = true;
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
        return PowerCapture.class.getName();
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
