# Droiditor

Droiditor is an Android library that can be used to capture---in real time---information about the nature and operation of an Android-based application. 
Example use-cases include:

* to understand what the user was doing, how the system was behaving, and in which context it was being used -- perhaps when something went wrong
* to learn how to improve applications from a user or operation perspective -- perhaps to increase safety.

## Publications

To learn more about Droiditor and the motivation behind its development, see our publication: https://dl.acm.org/doi/pdf/10.1145/3495001

Other relevant publications include:

* A Call for Auditable Virtual, Augmented & Mixed Reality (ACM VRST); This manuscript describes XR risks and harms, and surveys practitioners regarding audit and tooling landscape. https://dl.acm.org/doi/pdf/10.1145/3385956.3418960 
* Navigating the Audit Landscape: A Framework for Developing Transparent and Auditable XR  (ACM FAccT); This manuscript describes a method/guide for developers on what to record from XR/ubicomp systems, and the implications. https://dl.acm.org/doi/pdf/10.1145/3593013.3594090
* Accountability Capture: Considerations and risks in algorithmic system record-keeping (under review); This manuscript describes the risks, considerations and implications of record-keeping for system in general.

You can find a full list of our publications and learn more about the Compliant and Accountable Systems group, University of Cambridge, at: https://www.compacctsys.net/

## Building and exporting

1. Clone this repository
2. Open the project in Android Studio
3. Sync the Gradle files get the depencies
4. Open the build.gradle file and:
  * update the `source_dir` path to match your setup
  * update the `destination_dir` path to match your setup - this indicates where the .aar file will be exported after building.
5. Open the Gradle Tasks window, find the `exportAAR` task and execute it.

Droiditor will compile and the resulting `.aar` file will be put into the directory specified.

## Importing Droiditor into your project

1. Create a `libs` directory under `app` (or whatever your app directory is called).
2. Add the `droiditor.aar` file to the `libs` directory. If you updated the Droiditor build.gradle file and built the project, this file should be put here automatically (see building and exporting).
3. In your apps build.gradle file, add the following dependencies:
  * implementation(name:'droiditor', ext:'aar')
  * implementation("com.squareup.okhttp3:okhttp:4.9.0")
  * implementation("commons-io:commons-io:2.8.0")
  * implementation("net.lightbody.bmp:browsermob-core:2.1.5"){
        exclude group: 'org.slf4j'
    }
  * implementation('net.lightbody.bmp:littleproxy:1.1.0-beta-bmp-17')
  * implementation 'org.zeromq:jeromq:0.5.2'

You _may_ also need to declare the following package options in the `android` section:

    packagingOptions {
        exclude '**/resources/*'
        exclude 'sslSupport/ca-keystore-rsa.p12'
        exclude 'sslSupport/ca-certificate-rsa.cer'
        exclude 'sslSupport/ca-keystore-ec.p12'
        exclude 'sslSupport/ca-certificate-ec.cer'
        exclude 'cacerts.pem'
        exclude 'net/lightbody/bmp/version'
        exclude 'default-ciphers.txt'
    }

## Configuring Droiditor

Droiditor provides a number of `Capture Mechanisms` - each capturing data from a specific source. At the moment, the following capture mechanims are available:
* ScreenCapture [ScrCap]
  * Captures the content of the screen as a video
* SnapShot [ScrShot]
  * Captures a single screenshot
* CameraCapture [CamCap]
  * Captures a single image from the camera
* AudioCapture [AudCap]
  * Captures the internal audio (i.e. application audio) or audio from the microphone
* SensorCapture [SenCap]
  * Captures data from various (specified) on-device sensors (e.g. gyroscope, light, acceleration, etc.)
* NetworkCapture [NetCap]
  * Captures all network traffic in and out of Droiditor. **Note: At the moment, Droiditor only captures HTTP/S requests and responses**
* ControllerCapture [ConCap]
  * Captures input from controllers. **Note: At the moment, Droiditor only captures screen taps and gestures**
* GeneralCapture [GenCap]
  * Essentially just a logger, it will capture any text provided.

Capture mechanisms are configured and enabled via the `audit_config.xml` file, which must be placed in your applications `assets` directory.
Below are a couple of example configurations. Note that for sensors, the ID may change as Android devices (and sensors) evolve, so if a sensor is not being detected, look up the correct sensor ID in the Android documentation.

**Example 1: SensorCapture**

    <mechanism name="sencap" enabled="true">
      <sensor>
          <name>TYPE_ORIENTATION</name>
          <filename_prefix>TYPE_ORIENTATION</filename_prefix>
          <id>3</id>
      </sensor>
      <sensor>
          <name>TYPE_ACCELEROMETER_UNCALIBRATED</name>
          <filename_prefix>TYPE_ACCELEROMETER_UNCALIBRATED</filename_prefix>
          <id>35</id>
      </sensor>
      <sensor>
          <name>TYPE_GYROSCOPE_UNCALIBRATED</name>
          <filename_prefix>TYPE_GYROSCOPE_UNCALIBRATED</filename_prefix>
          <id>16</id>
      </sensor>
      <output_directory>SENSOR_DATA</output_directory>
    </mechanism>

`name="sencap"` specifies that this bit of configuration deals with the SensorCapture mechanism, while `enabled="true"`specifies that the mechanism is enabled.
Each `<sensor>` consists of `name`, `filename_prefix` and `id` nodes. `name` and `id` must match the specified names and constant values in the Android documentation (see https://developer.android.com/reference/android/hardware/Sensor#TYPE_ACCELEROMETER). `output_directory` specifies the name of the directory output data files should be put into.

In the case of SensorCapture, a JSON file for each sensor will be created and written to. `filename_prefix` allows you to give a custom nametag to each file.

**Example 2: ScreenCapture**

    <mechanism name="scrcap" enabled="true">
        <output_directory>SCREEN</output_directory>
        <filename_prefix>SCREENCAPTURE</filename_prefix>
    </mechanism>

Here, the ScreenCapture mechanism is enabled. The output video will be stored in the `SCREEN` directory and the filename will be prefixed with `SCREENCAPTURE`.

You can also specify some general Droiditor configurations. For instance, you can specify how long data should be captured for, how long to wait before starting and where captured data should be stored:

    <general enabled="true">
        < audit_duration_seconds>600</audit_duration_seconds>
         <audit_start_delay_seconds>5</audit_start_delay_seconds>
         <output_directory>AUDIT_DATA</output_directory>
     </general>

## Using Droiditor

Once imported and configured, Droiditor can be started with:

    Auditor.INSTANCE().initialize(this, config);
    Auditor.INSTANCE().start();

`config` in this case is a string representing the `audit_config.xml`.
In a later version, this will be done automatically.

Droiditor will now capture data for any of the enabled capture mechanisms and store the data in whatever directory name you specified in the `output_directory` node for the `general` config.

**Note:** CameraCapture, SnapShot and GenCap need to be called from within code - they do not automatically capture data. This is because they take individual captures and as such, need to be used more like helpers:

    Auditor.INSTANCE().screenShotCapture.takeScreenshot();
    Auditor.INSTANCE().cameraShotCapture.captureShotXR(frame);
    Auditor.INSTANCE().genCap.log(some_text);

## Permissions and additional configuration

Because Droiditor follows Androids permission model, you will need to ensure that your application has the nessasay permissions in the manifest. The permissions required will depend on the capture mechanisms used.

    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    <uses-permission android:name="android.permission.CAMERA" />

    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

Some capture mechanisms, such as NetCap, ScreenCap and AudCap require permission to be given at runtime. For instance, ScreenCap and AudCap require permissions to use the Media Projection Service. As such, your `onActivityResult` function needs the following (if using the mentioned mechanisms):

    if (requestCode == REQUEST_MEDIA_PROJECTION) {
        if (resultCode == RESULT_OK) {
            Auditor.INSTANCE().giveMediaProjectionPermission(resultCode,data);
        }
    }


## Extending Droiditor

Extending Droiditor is easy, but there are a few steps to it. First, you should create a new class, referencing the `ICaptureMechanism` interface:

    public class MyCaptureMechanism implements ICaptureMechanism{

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void initialise() {

        }

        @Override
        public ENUMS.CaptureMechanism getName() {
            return null;
        }
    }

Implement the methods. Note the `isReady` and `getName` methods. `isReady` should return `true` when your capture mechanism is fully initialized/setup. `getName` returns `ENUMS.CaptureMechanism` and as such, you will need to add an additional capture mechanism to the CaptureMechanism enum located in `Enums.java`:

    public enum CaptureMechanism {

        MICROPHONE("MICROPHONE"),
        SCREEN_CAPTURE("SCREEN_CAPTURE"),
        ...
        ...
        MY_CAPTURE_MECHANISM("MINE");

        CaptureMechanism(String name) {
        }
    }

You can then modify `getName` to return your newly defined capture mechanism:

    @Override
    public ENUMS.CaptureMechanism getName() {
        return ENUMS.CaptureMechanism.MY_CAPTURE_MECHANISM;
    }

So that your capture mechanism can access configurations specified in the `audit_config.xml` file, you must create a config class to go with your capture mechanism, e.g.:

    public class GeneralCaptureConfig  extends CaptureConfigBase implements IConfig {

        @Override
        public void parse(String config) {

        }

    }

Finally, you need to add your capture mechanism to the `CaptureMechanismsRegistry.java`:

    capture_mechanisms.add("com.cambridge.CaptureMechanisms.General.GeneralCapture, com.cambridge.DataTransformers.NoTransform, com.cambridge.DataLoggers.JSONDataLogger");


That is all you need to do if you want to extend Droiditor. However, if you would prefer to not modify Droiditor itself, you can also inject your own capture mechanisms. For instance, create a Capture Mechanism and Config class as described above, and then register it with Droiditor using the `Auditor.INSTANCE().registerCaptureMechanism()` function.

## Accessing individual capture mechanisms

If you'd like to access individual Capture Mechansims, you can iterate over the `registeredCaptureMechanisms()`:

    for (Map.Entry<String, ICaptureMechanism> entry :  Auditor.INSTANCE().registeredCaptureMechanisms.entrySet()) {
        ICaptureMechanism captureMechanism = entry.getValue();
        if (captureMechanism.getName().equals("com.cambridge.CaptureMechanisms.General.GeneralCapture")) {
            generalCapture = (GeneralCapture) captureMechanism;
        }
    }



