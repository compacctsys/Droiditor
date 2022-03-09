package com.cambridge.DataLoggers;

import com.cambridge.Auditor;
import com.cambridge.utils.AuditLoggerBase;
import com.cambridge.utils.CaptureMechanismEventData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class JSONDataLogger extends AuditLoggerBase implements IDataLogger {


    @Override
    public void initialize(String path) {
        super.initialize(path);
    }

    @Override
    public void log(String name, CaptureMechanismEventData data, boolean offload) {

        String _data = (String) data.getData();

        try {

            JSONObject obj2 = new JSONObject();
            obj2.put("time", System.currentTimeMillis());
            obj2.put("name", name);
            obj2.put("message", _data);

            String jsonString = obj2.toString();

            if (offload) {
                Auditor.INSTANCE().dataOffloader.send("json", jsonString);
                return;
            }

            super.write(jsonString);

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

    }

}
