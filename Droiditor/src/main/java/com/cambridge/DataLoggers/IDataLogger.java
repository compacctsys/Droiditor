package com.cambridge.DataLoggers;

import com.cambridge.utils.CaptureMechanismEventData;

public interface IDataLogger {

    void initialize(String path);
    void log(String name, CaptureMechanismEventData data, boolean offload);

}
