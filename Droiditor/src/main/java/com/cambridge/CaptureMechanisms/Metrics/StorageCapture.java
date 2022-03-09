package com.cambridge.CaptureMechanisms.Metrics;

import android.os.StatFs;

import com.cambridge.CaptureMechanisms.CaptureMechanismBase;
import com.cambridge.CaptureMechanisms.ICaptureMechanism;
import com.cambridge.Config.IConfig;
import com.cambridge.DataLoggers.IDataLogger;
import com.cambridge.DataTransformers.IDataTransformer;
import com.cambridge.utils.AuditHelpers;
import com.cambridge.utils.CaptureMechanismEventData;
import com.cambridge.utils.GLOBALS;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class StorageCapture extends CaptureMechanismBase implements ICaptureMechanism {

    StorageCaptureConfig config = new StorageCaptureConfig();
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void start() {
        if (config.enabled == false) {
            return;
        }

        Thread runThread = new Thread(new Runnable() {
            @Override
            public void run() {
                running.set(true);
                while (running.get()) {
                    File f = new File(GLOBALS.BASE_AUDIT_DATA_PATH);
                    String message = String.valueOf(getFileSize(f));
                    CaptureMechanismEventData data = new CaptureMechanismEventData(message);
                    onDataReady(data);
                    try {
                        Thread.sleep(config.updateRateMilliseconds);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        runThread.start();
    }

    //    https://stackoverflow.com/a/8826357/596841
    public long freeMemory() {
        String p = GLOBALS.MAIN_ACTIVITY.getExternalFilesDir(null).getAbsolutePath();
        StatFs statFs = new StatFs(p);
        long free = (statFs.getFreeBlocksLong() * statFs.getBlockSizeLong());
        return free;
    }

    public static String floatForm(double d) {
        return new DecimalFormat("#.##").format(d);
    }

    public static String bytesToHuman(long size) {
        long Kb = 1 * 1024;
        long Mb = Kb * 1024;
        long Gb = Mb * 1024;
        long Tb = Gb * 1024;
        long Pb = Tb * 1024;
        long Eb = Pb * 1024;

        return floatForm((double) size / Mb);

    }


    @Override
    public void stop() {
        running.set(false);
    }

    @Override
    public void initialise() throws Exception {
        if (config.enabled == false) {
            return;
        }
        String header = "size";
        String path = config.output_dir + "/" + (config.filename_prefix + "_" + AuditHelpers.auditSessionId + "_" + System.nanoTime() + ".data");
        this.logger.initialize(AuditHelpers.getPath(path));
    }

    @Override
    public void setDataLogger(IDataLogger logger) {
        this.logger = logger;
    }

    @Override
    public void setDataTransformer(IDataTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public String getName() {
        return StorageCapture.class.getName();
    }

    @Override
    public IConfig parseConfig(String config_string) {
        config.parse(config_string);
        this.enabled = config.enabled;
        this.output_dir = config.output_dir;
        this.filename_prefix = config.filename_prefix;
        return config;
    }


    @Override
    public boolean requiresMediaProjection() {
        return false;
    }


    @Override
    public void onDataReady(CaptureMechanismEventData data) {
        super.transformer.transform(data);
        super.logger.log("", data, config.offloadData);
    }


    public static long getDirectorySize() {
//        https://stackoverflow.com/a/19877372/596841
        final AtomicLong size = new AtomicLong(0);

        try {

            Files.walkFileTree(Paths.get(GLOBALS.BASE_AUDIT_DATA_PATH), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

                    size.addAndGet(attrs.size());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {

                    System.out.println("skipped: " + file + " (" + exc + ")");
                    // Skip folders that can't be traversed
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {

                    if (exc != null)
                        System.out.println("had trouble traversing: " + dir + " (" + exc + ")");
                    // Ignore errors traversing a folder
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
        }

        return size.get();
    }

    public static long getFileSize(final File file) {
        if (file == null || !file.exists())
            return 0;
        if (!file.isDirectory())
            return file.length();
        final List<File> dirs = new LinkedList<>();
        dirs.add(file);
        long result = 0;
        while (!dirs.isEmpty()) {
            final File dir = dirs.remove(0);
            if (!dir.exists())
                continue;
            final File[] listFiles = dir.listFiles();
            if (listFiles == null || listFiles.length == 0)
                continue;
            for (final File child : listFiles) {
                result += child.length();
                if (child.isDirectory())
                    dirs.add(child);
            }
        }
        return result;
    }

}
