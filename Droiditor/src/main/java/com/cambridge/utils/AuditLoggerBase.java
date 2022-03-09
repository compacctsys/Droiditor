package com.cambridge.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class AuditLoggerBase {

    public static String TAG = AuditLoggerBase.class.getSimpleName();
    public FileWriter writer;
    public BufferedWriter bw;

    public void initialize(String path) {
        try {

            writer = new FileWriter(path);
            bw = new BufferedWriter(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String record) throws IOException {
        record = record + "\n";
        bw.write(record);
        bw.flush();
    }


}