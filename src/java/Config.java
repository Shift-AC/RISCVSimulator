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
    static final String configList = "configList";
    static final String configType = "configType";
    static ConfigType[] configTypes;
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

    static
    {
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(
                    ConfigUtil.configPath + ConfigUtil.configType)));
            
            int typeCount = Integer.parseInt(reader.readLine());

            configTypes = new ConfigType[typeCount];
            for (int i = 0; i < typeCount; ++i)
            {
                String name = reader.readLine();
                int ind = name.length() - 1;
                for (; ind > -1; --ind)
                {
                    char c = name.charAt(ind);
                    if (c != '\r' && c != '\n')
                    {
                        break;
                    }
                }
                name = name.substring(0, ind + 1);
                configTypes[i] = 
                    (ConfigType)(Class.forName(name).newInstance());
            }
        }
        catch (Exception e)
        {
            Util.reportExceptionAndExit("读取配置文件configTypes时出现错误", e);
        }
    }
}

class ConfigManager
{
    ArrayList<ConfigFile> files;
    ConfigFile searchKey = new ConfigFile();
    public ConfigManager()
        throws FileNotFoundException,
               IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            new FileInputStream(
                ConfigUtil.configPath + ConfigUtil.configList)));
        
        int fileCount = Integer.parseInt(reader.readLine());

        files = new ArrayList<ConfigFile>();
        for (int i = 0; i < fileCount; ++i)
        {
            files.add(new ConfigFile(reader.readLine()));
        }

        Collections.sort(files);
    }

    public ConfigFile findConfigFile(String name)
    {
        if (files != null)
        {
            searchKey.name = name;
            int index = Collections.binarySearch(files, searchKey);
            if (index < 0)
            {
                return null;
            }
            return files.get(index);
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

class ConfigFile implements Comparable<ConfigFile>
{
    String name;
    String version;
    private ArrayList<Config> configs;
    Config searchKey = new Config();
    public ConfigFile() {}

    public ConfigFile(String name)
        throws FileNotFoundException,
               IOException
    {
        this.name = name;
        // open and read file
        BufferedReader is = null;
        try
        {
            //System.err.println("ConfigFile: " + filePath());
            is = new BufferedReader(new FileReader(filePath()));
            version = is.readLine();

            configs = new ArrayList<Config>(); 
            String line;
            while ((line = is.readLine()) != null)
            {
                if (line.equals(""))
                {
                    continue;
                }
                if (line.charAt(0) == '#')
                {
                    continue;
                }

                Config tmp = null;
                try
                {
                    tmp = new Config(line);
                }
                catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                }
                configs.add(tmp);
            }
            Collections.sort(configs);
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
            searchKey.name = name;
            int index = Collections.binarySearch(configs, searchKey);
            if (index < 0)
            {
                return null;
            }
            return configs.get(index);
        }
        return null;
    }

    @Override
    public int compareTo(ConfigFile x)
    {
        return this.name.compareTo(x.name);
    }
}

class Config implements Comparable<Config>
{
    String name;
    Object value;
    char valueType;

    public Config() {}

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
                "Illegal value `" + line + "` for config type " + 
                myType.getType());
        }

        //System.err.println("Config: " + name);
    }

    public Object getValue()
    {
        return value;
    }

    @Override
    public int compareTo(Config x)
    {
        return this.name.compareTo(x.name);
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

    private boolean isWhiteSpace(char c)
    {
        return c == ' ' ||  c == '\t';
    }
    @Override
    public Integer parse(String line)
    {
        int x;
        try
        {
            int ind = line.length() - 1;
            for (; ind > -1; --ind)
            {
                if (!isWhiteSpace(line.charAt(ind)))
                {
                    break;
                }
            }
            line = line.substring(0, ind + 1);
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

class LongConfig extends ConfigType<Long>
{
    @Override
    public char valueType()
    {
        return 'L';
    }

    private boolean isWhiteSpace(char c)
    {
        return c == ' ' ||  c == '\t';
    }
    @Override
    public Long parse(String line)
    {
        long x;
        try
        {
            int ind = line.length() - 1;
            for (; ind > -1; --ind)
            {
                if (!isWhiteSpace(line.charAt(ind)))
                {
                    break;
                }
            }
            line = line.substring(0, ind + 1);
            x = Long.parseLong(line, 16);
        }
        catch (Exception e)
        {
            return null;
        }
        return new Long(x);
    }
    @Override
    public String getType()
    {
        return "Long";
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
