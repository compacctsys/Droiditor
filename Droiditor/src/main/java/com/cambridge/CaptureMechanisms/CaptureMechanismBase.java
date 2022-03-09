package com.cambridge.CaptureMechanisms;

import com.cambridge.DataLoggers.IDataLogger;
import com.cambridge.DataTransformers.IDataTransformer;

public class CaptureMechanismBase {

    public IDataLogger logger;
    public IDataTransformer transformer;
    public boolean enabled = false;
    public String filename_prefix = "";
    public String output_dir = "";

}
