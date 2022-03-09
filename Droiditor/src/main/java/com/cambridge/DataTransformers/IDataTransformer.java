package com.cambridge.DataTransformers;

import com.cambridge.utils.CaptureMechanismEventData;

public interface IDataTransformer {
    CaptureMechanismEventData transform(CaptureMechanismEventData data);
}
