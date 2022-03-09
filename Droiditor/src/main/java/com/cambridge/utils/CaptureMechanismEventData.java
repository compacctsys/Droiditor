package com.cambridge.utils;

public class CaptureMechanismEventData {

    private Object eventData;
    private String dataType;
    public String filePath;

    public CaptureMechanismEventData(Object data){
        this.eventData = data;
    }

    public Object getData(){
            return eventData;
    }

    public String getDataType(){
        return eventData.getClass().getName();
    }


}
