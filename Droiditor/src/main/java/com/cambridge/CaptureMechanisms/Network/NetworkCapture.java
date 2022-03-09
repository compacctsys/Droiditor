package com.cambridge.CaptureMechanisms.Network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.security.KeyChain;
import android.util.Log;

import com.cambridge.CaptureMechanisms.CaptureMechanismBase;
import com.cambridge.CaptureMechanisms.ICaptureMechanism;
import com.cambridge.CaptureMechanisms.Network.Utils.FileUtil;
import com.cambridge.CaptureMechanisms.Network.Utils.ProxyUtils;
import com.cambridge.CaptureMechanisms.Network.Utils.SharedPreferenceUtils;
import com.cambridge.Config.IConfig;
import com.cambridge.DataLoggers.IDataLogger;
import com.cambridge.DataTransformers.IDataTransformer;
import com.cambridge.utils.AuditHelpers;
import com.cambridge.utils.CaptureMechanismEventData;
import com.cambridge.utils.GLOBALS;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarLog;
import net.lightbody.bmp.proxy.CaptureType;
import net.lightbody.bmp.proxy.dns.AdvancedHostResolver;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.cambridge.utils.GLOBALS.PROXY_REQUST_CODE;

public class NetworkCapture extends CaptureMechanismBase implements ICaptureMechanism {

    HarLog harLog;

    private NetworkCaptureConfig config = new NetworkCaptureConfig();

    public static Boolean isInitProxy = false;
    public static int proxyPort = 8888;
    public BrowserMobProxy proxy;
    public final AtomicBoolean running = new AtomicBoolean(false);

    private static String TAG = NetworkCapture.class.getSimpleName();

    @Override
    public void start() {
        if(config.enabled == false){ return;}
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "STARTING PROXY!!");
                startProxy();

                Intent intent = new Intent();
                intent.setAction("proxyfinished");
                GLOBALS.MAIN_ACTIVITY.sendBroadcast(intent);
            }
        }).start();

        running.set(false);

        pollThread.start();
    }

    private Thread pollThread = new Thread(new Runnable() {
        @Override
        public void run() {
            running.set(true);
            while (running.get()) {
                if(isInitProxy == false) { continue; }

                Har har = proxy.getHar();
                harLog = har.getLog();
                List<HarEntry> harEntryList = harLog.getEntries();

                if(harEntryList.size() > 0){
                    Writer w = new StringWriter();
                    try {
                        har.writeTo(w);
                        String message = w.toString();
                        CaptureMechanismEventData data = new CaptureMechanismEventData(message);
                        onDataReady(data);
                        harLog.getEntries().clear();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    });

    public void getData(){
        if(isInitProxy) {
            Log.e(TAG, "DATA");
        }
    }


    private void startProxy(){

        proxy = new BrowserMobProxyServer();

        if(proxy.isStarted()){
            proxy.stop();
        }
        try {
            proxy.setTrustAllServers(true);
            proxy.start(9999);
        } catch (Exception e) {
            Random rand = new Random();
            int randNum = rand.nextInt(1000) + 8000;
            proxyPort = randNum;

            proxy.start(randNum);
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        Log.e("~~~", proxy.getPort() + "");

        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(GLOBALS.MAIN_ACTIVITY);

        if(shp.getString("system_host", "").length()>0){
            AdvancedHostResolver advancedHostResolver = proxy.getHostNameResolver();
            for (String temp : shp.getString("system_host", "").split("\\n")){
                if(temp.split(" ").length==2) {
                    advancedHostResolver.remapHost(temp.split(" ")[1],temp.split(" ")[0]);
                    Log.e("~~~~remapHost ",temp.split(" ")[1] +" " + temp.split(" ")[0]);
                }
            }
            proxy.setHostNameResolver(advancedHostResolver);
        }

        proxy.enableHarCaptureTypes(CaptureType.REQUEST_HEADERS, CaptureType.REQUEST_COOKIES,
                CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_HEADERS, CaptureType.REQUEST_COOKIES,
                CaptureType.RESPONSE_CONTENT);

        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                .format(new Date(System.currentTimeMillis()));
        proxy.newHar(time);

        isInitProxy = true;
    }

    @Override
    public String getName() {
        return NetworkCapture.class.getName();
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
    public void initialise() {
        if(config.enabled == false){ return;}
        String path = config.output_dir + "/"  + (config.filename_prefix + "_" + AuditHelpers.auditSessionId + "_" + System.nanoTime() + ".json");
        this.logger.initialize(AuditHelpers.getPath(path));
        installCert();
        ProxyUtils.setProxyLollipop(GLOBALS.MAIN_ACTIVITY, "127.0.0.1", 9999);
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
    public void stop() {
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


    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            installCert();
            ProxyUtils.setProxyLollipop(GLOBALS.MAIN_ACTIVITY, "127.0.0.1", 9999);
            Log.e("~~~~", "Receiver installCert");
        }
    }

    public void installCert() {
        final String CERTIFICATE_RESOURCE = Environment.getExternalStorageDirectory() + "/har/littleproxy-mitm.pem";
        Boolean isInstallCert = SharedPreferenceUtils.getBoolean(GLOBALS.MAIN_ACTIVITY, "isInstallNewCert", true);

//        Boolean isInstallCert = false;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] keychainBytes;

                    FileInputStream is = null;
                    try {
                        is = new FileInputStream(CERTIFICATE_RESOURCE);
                        keychainBytes = new byte[is.available()];
                        is.read(keychainBytes);
                    } finally {
                        IOUtils.closeQuietly(is);
                    }

                    Intent intent = KeyChain.createInstallIntent();
                    intent.putExtra(KeyChain.EXTRA_CERTIFICATE, keychainBytes);
                    intent.putExtra(KeyChain.EXTRA_NAME, "NetworkDiagnosis CA Certificate");
                    GLOBALS.MAIN_ACTIVITY.startActivityForResult(intent, PROXY_REQUST_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        if (!isInstallCert) {
            FileUtil.checkPermission(GLOBALS.MAIN_ACTIVITY,runnable);
        }
    }

}
