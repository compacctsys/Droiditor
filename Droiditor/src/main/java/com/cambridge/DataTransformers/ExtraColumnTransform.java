package com.cambridge.DataTransformers;

import com.cambridge.utils.CaptureMechanismEventData;

public class ExtraColumnTransform implements IDataTransformer{

    @Override
    public CaptureMechanismEventData transform(CaptureMechanismEventData data) {
        String newline = String.valueOf(data.getData()) + ", " + "test";
        return new CaptureMechanismEventData(newline);
    }
}
