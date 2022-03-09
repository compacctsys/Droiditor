package com.cambridge.CaptureMechanisms;

import com.cambridge.Config.IConfig;
import com.cambridge.DataLoggers.IDataLogger;
import com.cambridge.DataTransformers.IDataTransformer;
import com.cambridge.utils.CaptureMechanismEventData;

public interface ICaptureMechanism {

    void start();

    void stop();

    void initialise() throws Exception;

    void setDataLogger(IDataLogger logger);

    void setDataTransformer(IDataTransformer transformer);

    String getName();

    IConfig parseConfig(String config_string);

    boolean requiresMediaProjection();

    void onDataReady(CaptureMechanismEventData data);

}
