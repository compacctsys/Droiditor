# Creating a custom capture mechanism
Droiditor allows you to extend its functionality by creating and registering a custom capture mechanism.
To do this, you need to perform 4 simple steps.

1. Create a new class for your capture mechanism and implement the ICaptureMechanism interface
2. Create a new class for the config of your capture mechanism and implement the IConfig interface
3. Create a node in the audit_config.xml file
4. Register your capture mechanism with Droiditor

## 1. MyCustomCaptureMechanism

Below is an example custom capture mechanism.
The `initialize()`, `start()` and `stop()` functions will called by Droiditor. 
Populate these functions to control the initialization, starting and stopping of your custom capture mechanism.
Note the `_logger` and `config` variables. These you must define yourself; they are required. 
For the logger, Droiditor has various ready-to-use loggers (e.g. `JSONDataLogger`), so use that if you don't want to create your own logger.

```
public class MyCustomCaptureMechanism implements ICaptureMechanism {
    
    private IDataLogger _logger;
    private MyCustomCaptureMechanismConfig config = new MyCustomCaptureMechanismConfig();
    
    @Override
    public void start() {}
    
    @Override
    public void stop() {}
    
    @Override
    public void initialise() throws Exception {}
    
    @Override
    public void setDataLogger(IDataLogger logger) { _logger = logger; }
    
    @Override
    public String getName() { return MyCustomCaptureMechanism.class.getName(); }
    
    @Override
    public IConfig parseConfig(String config_string) {
        config.parse(config_string);
        return config;
    }
    
    @Override
    public IConfig getConfig() {return null;}
    
    @Override
    public boolean requiresMediaProjection() {return false;}
}
```

# 2. MyCustomCaptureMechanismConfig

Below is an example boilerplate for your config class.
Because Droiditor uses an XML-based config file, your `parse()` function must be built to parse XML.

```
public class MyCustomCaptureMechanismConfig extends CaptureConfigBase implements IConfig {
    @Override
    public void parse(String s) {
        
    }
}
```

Note the `extends CaptureConfigBase` - this pulls in some mandatory properties each capture mechanism must have:

```
    public boolean enabled = false;
    public String filename_prefix = "";
    public String output_dir = "";
```

# 3. XML Config 

Below is an example XML node that you must add to the XML config you initalize Droiditor with.
Important: `name` MUST match your capture mechanism name.
`output_directory` specifies where your data will be written (if not offloading...)
`filename_prefix` allows you to specify a prefix to the filenames produced by Droiditor - this just makes it easier to identify your files.
Note: You can add additional properties to your config (e.g. `<custom_property>30</custom_property>`), but you will need to update your `MyCustomCaptureMechanismConfig` class to handle this, and you will also need to update your `MyCustomCaptureMechanism` class to use the property.

```
    <capture-mechanism name="MyCustomCaptureMechanism" enabled="true">
        <output_directory>CUSTOM_SCREEN</output_directory>
        <filename_prefix>CUSTOM_SCREENSHOT</filename_prefix>
    </capture-mechanism>
```

# 4. Register your capture mechanism with Droiditor
Registering your capture mechanism is simple.
To register your capture mechanism, simply add the following line AFTER you have initialized Droiditor.
Note that the path to your capture mechanism. It is critical that it is correct. To find this path, go to your `MyCustomCaptureMechanism`.
The path you need will be the path from the `package` reference (first line of the class) and appended `.MyCustomCaptureMechanism` to the end.
E.g. `com.my_company.core.java.package_name.MyCustomCaptureMechanism`
    
    ```Auditor.INSTANCE().registerCaptureMechanism("com.full.path.to.your.custom.capturemechanism.MyCustomCaptureMechanism",config);```
