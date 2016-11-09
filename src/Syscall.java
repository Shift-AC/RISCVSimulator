package com.github.ShiftAC.RISCVSimulator;

public abstract class Syscall
{
    long num;
    String name;

    abstract public void call(RISCVMachine machine);
}

class SyscallConfig extends ConfigType<Syscall>
{
    @Override
    public char valueType()
    {
        return 'C';
    }
    @Override
    public Syscall parse(String line)
    {
        int separate = line.indexOf(' ');
        if (separate == -1)
        {
            return null;
        }
        Syscall res;
        try
        {
            res = (Syscall)Class.forName(
                line.substring(separate + 1)).newInstance();
        }
        catch (Exception e)
        {
            return null;
        }
        res.num = Long.parseLong(line.substring(0, separate));
        return res;
    }
    @Override
    public String getType()
    {
        return "Syscall";
    }
}