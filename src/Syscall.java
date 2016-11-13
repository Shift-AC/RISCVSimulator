package com.github.ShiftAC.RISCVSimulator;

public abstract class Syscall
{
    long num;
    String name;

    public Syscall()
    {
        this.name = getClass().getName().substring(name.lastIndexOf('.') + 4);
    }

    abstract public void call(RISCVMachine machine);
}

class NativeSyscall extends Syscall
{
    static DataInputStream stdout;
    static DataInputStream stderr;
    static OutputStream stdin;
    static
    {
        try
        {
            // need modify
            String nativeProgram = "./syscallManager";
            Process ps = Runtime.getRuntime().exec(nativeProgram);
            stdout = new DataInputStream(ps.getInputStream()));
            stderr = new DataInputStream(ps.getErrorStream()));
            stdin = ps.getOutputStream();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Util.reportExceptionAndExit("致命错误：无法启动系统调用管理器", e);
        }
    }
    @Override
    public void call(RISCVMachine machine)
    {
        byte[][] messages = getMessages(machine);
        for (byte[] message : messages)
        {
            stdin.write(message);
        }
        stdin.flush();

        setReturnValue(machine);
    }
    protected byte[][] getMessages(RISCVMachine machine)
    {
        StringBuilder sb = new StringBuilder();
        long[] generalRegister = machine.generalRegister;
        sb.append(generalRegister[17] + " ");
        sb.append(generalRegister[10] + " ");
        sb.append(generalRegister[11] + " ");
        sb.append(generalRegister[12] + " ");
        sb.append(generalRegister[13] + "");
        byte[][] messages = new byte[1][];
        messages[0] = sb.toString().getBytes();
        return messages;
    }
    protected void setReturnValue(RISCVMachine machine)
    {
        generalRegister[10] = stdout.readLong();
    }
}

abstract class StreamParameteredNativeSyscall extends NativeSyscall
{
    @Override
    protected byte[][] getMessages(RISCVMachine machine)
    {
        byte[] message = (super.getMessages(machine))[0];

        byte[] stream = getParamStream(machine);
        
        byte[] prefix = (new String(" " + stream.length)).getBytes();

        byte[][] messages = new byte[3][];

        messages[0] = message;
        messages[1] = prefix;
        messages[2] = stream;

        return messages;
    }

    abstract protected byte[] getParamStream(RISCVMachine machine);
}

class SYSopen extends StreamParameteredNativeSyscall
{
    protected byte[] getParamStream(RISCVMachine machine)
    {
        
    }
}

class SYSlseek extends NativeSyscall
{
    // standard NativeSyscall
}

class SYSread extends NativeSyscall
{
    protected void setReturnValue(RISCVMachine machine)
    {
        super.setReturnValue();
        //....
    }
}

class SYSwrite extends StreamParameteredNativeSyscall
{
    protected byte[] getParamStream(RISCVMachine machine)
    {
        
    }
}

class SYSfstat extends NativeSyscall
{
    // standard NativeSyscall
}

class SYSclose extends NativeSyscall
{
    // standard NativeSyscall
}

class SYSisatty extends NativeSyscall
{
    // standard NativeSyscall
}
 
class SYSsbrk extends NativeSyscall
{
    // standard NativeSyscall
}

class SYSgettimeofday extends NativeSyscall
{
    // standard NativeSyscall
}

class SYStimes extends Syscall
{
    public void call(RISCVMachine machine)
    {
        //....
    }
}

class SYSexit extends Syscall
{
    public void call(RISCVMachine machine)
    {
        machine.machineStateRegister = MACHINE_STAT[3].stat;
    }
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
