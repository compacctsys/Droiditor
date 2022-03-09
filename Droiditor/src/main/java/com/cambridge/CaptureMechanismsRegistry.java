package com.cambridge;

import java.util.ArrayList;
import java.util.List;

public class CaptureMechanismsRegistry {

    public List<String> capture_mechanisms = new ArrayList<String>();

    public CaptureMechanismsRegistry(){
        capture_mechanisms.add("com.cambridge.CaptureMechanisms.Sensors.SensorCapture, com.cambridge.DataTransformers.NoTransform, com.cambridge.DataLoggers.JSONDataLogger");
        capture_mechanisms.add("com.cambridge.CaptureMechanisms.Audio.AudioCaptureNew, com.cambridge.DataTransformers.NoTransform, com.cambridge.DataLoggers.CSVDataLogger");
        capture_mechanisms.add("com.cambridge.CaptureMechanisms.Audio.MicrophoneCapture, com.cambridge.DataTransformers.NoTransform, com.cambridge.DataLoggers.CSVDataLogger");
        capture_mechanisms.add("com.cambridge.CaptureMechanisms.Camera.CameraShotCapture, com.cambridge.DataTransformers.NoTransform, com.cambridge.DataLoggers.YuvImageDataLogger");
        capture_mechanisms.add("com.cambridge.CaptureMechanisms.Screen.ScreenShotCapture, com.cambridge.DataTransformers.NoTransform, com.cambridge.DataLoggers.BitmapDataLogger");
        capture_mechanisms.add("com.cambridge.CaptureMechanisms.Screen.ScreenCapture, com.cambridge.DataTransformers.NoTransform, com.cambridge.DataLoggers.CSVDataLogger");
        capture_mechanisms.add("com.cambridge.CaptureMechanisms.Network.NetworkCapture, com.cambridge.DataTransformers.NoTransform, com.cambridge.DataLoggers.CSVDataLogger");
        capture_mechanisms.add("com.cambridge.CaptureMechanisms.Metrics.FPSCapture, com.cambridge.DataTransformers.NoTransform, com.cambridge.DataLoggers.CSVDataLogger");
        capture_mechanisms.add("com.cambridge.CaptureMechanisms.Metrics.MemoryCapture, com.cambridge.DataTransformers.NoTransform, com.cambridge.DataLoggers.CSVDataLogger");
        capture_mechanisms.add("com.cambridge.CaptureMechanisms.Metrics.PowerCapture, com.cambridge.DataTransformers.NoTransform, com.cambridge.DataLoggers.CSVDataLogger");
        capture_mechanisms.add("com.cambridge.CaptureMechanisms.Metrics.StorageCapture, com.cambridge.DataTransformers.NoTransform, com.cambridge.DataLoggers.CSVDataLogger");
        capture_mechanisms.add("com.cambridge.CaptureMechanisms.Input.InputCapture, com.cambridge.DataTransformers.NoTransform, com.cambridge.DataLoggers.CSVDataLogger");
        capture_mechanisms.add("com.cambridge.CaptureMechanisms.General.GeneralCapture, com.cambridge.DataTransformers.NoTransform, com.cambridge.DataLoggers.JSONDataLogger");
    }

}
