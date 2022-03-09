package com.cambridge.CaptureMechanisms;

import com.cambridge.Config.IConfig;
import com.cambridge.DataLoggers.IDataLogger;

public interface ICaptureMechanismNew {

    void start();

    void stop();

    void initialise() throws Exception;

    void setDataLogger(IDataLogger logger);

    String getName();

    IConfig parseConfig(String config_string);

    IConfig getConfig();

    boolean requiresMediaProjection();

}
