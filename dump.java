// use `parse` to open a ELF file and return it's standard output as an 
// InputStream which can be read directly. 
// on error, dump returns null.

InputStream dump(String fileName)
{
    try
    {
        String osName = System.getProperties().getProperty("os.name");
        String script = null;
        if (name.indexOf("Linux") != -1)
        {
            script = "./parse";
        }
        else
        {
            script = "parse";
        }

        script += fileName;

        Process ps = Runtime.getRuntime.exec(script);
        return ps.getInputStream();
    }
    catch (Exception e)
    {
        e.printStackTrace();
        return null;
    }
}