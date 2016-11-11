package com.github.ShiftAC.RISCVSimulator;

import java.io.*;
import java.util.*;

// Configuring Rules:
// 1. ConfigManager manages all usable configures in the system.
// 2. Each config(accessed by program) has format: fileName.configName
//    that means: we should find a config named [configName] in file 
//    config/fileName
// 3. Each config(in files) has format: [valueType] name=value 
//    that means: config [name] is a [valueType] variant with value [value].
//    valueType is a character(see Config.valueType).
// 4. To use config in program, use Util.configManager.

class ConfigUtil
{
    static final String configPath = "config/";
    static ConfigType[] configTypes = 
    {
        new IntegerConfig(),
        new BooleanConfig(),
        new StringConfig()
    };
    static ConfigType findConfigType(char valueType)
    {
        for (ConfigType type : configTypes)
        {
            if (type.valueType() == valueType)
            {
                return type;
            }
        }
        return null;
    }
}

class ConfigManager
{
    ConfigFile[] files;
    public ConfigManager(String[] fileNames)
        throws FileNotFoundException,
               IOException
    {
        files = new ConfigFile[fileNames.length];
        for (int i = 0; i < fileNames.length; ++i)
        {
            files[i] = new ConfigFile(fileNames[i]);
        }
    }

    public ConfigFile findConfigFile(String name)
    {
        if (files != null)
        {
            for (ConfigFile file : files)
            {
                if (file != null)
                {
                    if (file.name.equals(name))
                    {
                        return file;
                    }
                }
            }
        }
        return null;
    }
    public Object getConfig(String fullName)
    {
        //System.err.println("getConfig: getting " + fullName);

        int separate = fullName.indexOf('.');
        String fileName = fullName.substring(0, separate);
        ConfigFile file = findConfigFile(fileName);
        if (file == null)
        {
            System.err.println("getConfig: can't find config file " + fileName);
            return null;
        }
        String configName = fullName.substring(separate + 1);
        Config config = file.findConfig(configName);
        if (config == null)
        {
            System.err.println("getConfig: can't find config " + configName +
                               " in file " + fileName);
            return null;
        }
        return config.getValue();
    }
    /*
     * for later versions.
    public void setConfig(String fullName, Object newValue)
    {
        // don't need to modify files in config/ immediately.
        // modify them in saveConfig().
    }
    public void saveConfig()
    {

    }

    public void addConfigFile(String name)
    {

    }
    
    public void addConfig(String fullName)
    {

    }
    */
}

class ConfigFile
{
    final String name;
    String version;
    private Config[] configs;
    public ConfigFile(String name)
        throws FileNotFoundException,
               IOException
    {
        this.name = name;
        // open and read file
        BufferedReader is = null;
        try
        {
            System.err.println("ConfigFile: " + filePath());
            is = new BufferedReader(new FileReader(filePath()));
            version = is.readLine();

            LinkedList<Config> configList = new LinkedList<Config>(); 
            String line;
            while ((line = is.readLine()) != null)
            {
                Config tmp;
                try
                {
                    tmp = new Config(line);
                }
                catch (IllegalArgumentException e)
                {
                    continue;
                }
                configList.addLast(tmp);
            }
            configs = new Config[configList.size()];
            configList.toArray(configs);
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
    }
    private String filePath()
    {
        return ConfigUtil.configPath + name;
    }
    public Config findConfig(String name)
    {
        if (configs != null)
        {
            for (Config config : configs)
            {
                if (config != null)
                {
                    if (config.name.equals(name))
                    {
                        return config;
                    }
                }
            }
        }
        return null;
    }
}

class Config
{
    String name;
    Object value;
    char valueType;

    public Config(String name, String value, char valueType)
    {
        System.err.println("Config: " + name);

        this.name = name;
        this.value = value;
        this.valueType = valueType;
    }

    public Config(String line)
        throws IllegalArgumentException
    {
        ConfigType myType = ConfigUtil.findConfigType(line.charAt(0));
        if (myType == null)
        {
            throw new IllegalArgumentException(
                "Illegal valueType in config string " + line);
        }
        this.valueType = valueType;
        line = line.substring(2);

        int separate = line.indexOf('=');
        this.name = line.substring(0, separate);
        
        line = line.substring(separate + 1);
        if ((this.value = (Object)myType.parse(line)) == null)
        {
            throw new IllegalArgumentException(
                "Illegal value " + line + " for config type " + 
                myType.getType());
        }
    }

    public Object getValue()
    {
        return value;
    }
}

abstract class ConfigType<T>
{
    abstract public char valueType();
    abstract public T parse(String line);
    abstract public String getType();
}

class IntegerConfig extends ConfigType<Integer>
{
    @Override
    public char valueType()
    {
        return 'I';
    }
    @Override
    public Integer parse(String line)
    {
        int x;
        try
        {
            x = Integer.parseInt(line);
        }
        catch (Exception e)
        {
            return null;
        }
        return new Integer(x);
    }
    @Override
    public String getType()
    {
        return "Integer";
    }
}

class BooleanConfig extends ConfigType<Boolean>
{
    @Override
    public char valueType()
    {
        return 'B';
    }
    @Override
    public Boolean parse(String line)
        throws IllegalArgumentException
    {
        switch (line.charAt(0) & ~32)
        {
            case 'Y':
                return new Boolean(true);
            case 'N':
                return new Boolean(false);
            default:
                return null;
        }
    }
    @Override
    public String getType()
    {
        return "Boolean";
    }
}

class StringConfig extends ConfigType<String>
{
    @Override
    public char valueType()
    {
        return 'S';
    }
    @Override
    public String parse(String line)
    {
        return line;
    }
    @Override
    public String getType()
    {
        return "String";
    }
}

/*
 * Implement in later versions

class RefrenceConfig extends ConfigType<Object>
{
    @Override
    public char valueType()
    {
        return 'R';
    }
    @Override
    public Object parse()
    {

    }
    @Override
    

}
*/