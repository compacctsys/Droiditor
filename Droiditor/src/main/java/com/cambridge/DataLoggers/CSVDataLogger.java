package com.cambridge.DataLoggers;

import com.cambridge.Auditor;
import com.cambridge.utils.AuditLoggerBase;
import com.cambridge.utils.CaptureMechanismEventData;

import java.io.IOException;

public class CSVDataLogger extends AuditLoggerBase implements IDataLogger {


    @Override
    public void initialize(String path) {
        super.initialize(path);
    }

    @Override
    public void log(String name, CaptureMechanismEventData data, boolean offload) {
        try {

            String record = System.nanoTime() + "," + String.valueOf(data.getData());

            if (offload) {
                Auditor.INSTANCE().dataOffloader.send("csv", record);
                return;
            }

            write(record);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
