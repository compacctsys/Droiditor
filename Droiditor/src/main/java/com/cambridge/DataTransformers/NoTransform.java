package com.cambridge.DataTransformers;

import com.cambridge.utils.CaptureMechanismEventData;

public class NoTransform implements IDataTransformer{

    @Override
    public CaptureMechanismEventData transform(CaptureMechanismEventData data) {
        return data;
    }
}
