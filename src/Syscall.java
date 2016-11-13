package com.github.ShiftAC.RISCVSimulator;

import java.util.*;
import java.io.*;

public abstract class Syscall
{
    long num;
    String name;

    public Syscall() {}
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
            String nativeProgram = "bin/syscallManager";
            Process ps = Runtime.getRuntime().exec(nativeProgram);
            stdout = new DataInputStream(ps.getInputStream());
            stderr = new DataInputStream(ps.getErrorStream());
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
        if (messages == null)
        {
            return;
        }
        try
        {
            for (byte[] message : messages)
            {
                stdin.write(message);
            }
            stdin.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Util.reportErrorAndExit("致命错误：无法与系统调用管理器交互");
        }

        setReturnValue(machine);
    }
    protected byte[][] getMessages(RISCVMachine machine)
    {
        StringBuilder sb = new StringBuilder();
        if (machine == null)
        {
            sb.append(num + " 0 0 0 0");
        }
        else
        {
            long[] generalRegister = machine.generalRegister;
            sb.append(num + " ");
            sb.append(generalRegister[10] + " ");
            sb.append(generalRegister[11] + " ");
            sb.append(generalRegister[12] + " ");
            sb.append(generalRegister[13] + "");
            System.out.println(sb);
        }
        byte[][] messages = new byte[1][];
        messages[0] = sb.toString().getBytes();
        return messages;
    }
    protected void setReturnValue(RISCVMachine machine)
    {
        if (machine == null)
        {
            return;
        }
        try
        {
            //while (stdout.available() == 0);
            System.out.println("??");
            machine.generalRegister[10] = stdout.readLong();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Util.reportErrorAndExit("致命错误：无法取得系统调用返回值");
        }
    }
}

abstract class StreamParameteredNativeSyscall extends NativeSyscall
{
    static byte[] loadBytes(
        RISCVMachine machine, long startAddress, long length)
    {
        long endAddress = startAddress + length - 1;
        for (MemorySegment segment : machine.memory)
        {
            if (MemoryManageUnit.isEffectiveAddress(segment, startAddress))
            {
                if (MemoryManageUnit.isEffectiveAddress(segment, endAddress))
                {
                    byte[] res = new byte[(int)length];
                    int startIndex = (int)(startAddress - segment.startAddress);
                    for (int i = 0; i < length; ++i)
                    {
                        res[i] = segment.memory[i + startIndex];
                    }
                    return res;
                }
            }
        }
        return null;
    }

    static byte[] loadString(RISCVMachine machine, long startAddress)
    {
        for (MemorySegment segment : machine.memory)
        {
            if (MemoryManageUnit.isEffectiveAddress(segment, startAddress))
            {
                int startIndex = (int)(startAddress - segment.startAddress);
                int len = 0;
                int maxlen = (int)(segment.endAddress - startAddress);
                for (; len < maxlen; ++len)
                {
                    if (segment.memory[len + startIndex] == 0)
                    {
                        break;
                    }
                }
                byte[] string = new byte[len];
                for (int i = 0; i < len; ++i)
                {
                    string[i] = segment.memory[i + startIndex];
                }
                return string;
            }
        }
        return null;
    }

    @Override
    protected byte[][] getMessages(RISCVMachine machine)
    {
        byte[] stream = getParamStream(machine);
        if (stream == null)
        {
            return null;
        }

        byte[] message = (super.getMessages(machine))[0];
        byte[] prefix = (new String(" " + stream.length)).getBytes();

        byte[][] messages = new byte[3][];

        messages[0] = message;
        messages[1] = prefix;
        messages[2] = stream;

        return messages;
    }

    abstract protected byte[] getParamStream(RISCVMachine machine);
}

abstract class StreamReturnedNativeSyscall extends NativeSyscall
{
    static boolean saveBytes(
        RISCVMachine machine, long startAddress, byte[] bytes)
    {
        long endAddress = startAddress + bytes.length - 1;
        for (MemorySegment segment : machine.memory)
        {
            if (MemoryManageUnit.isEffectiveAddress(segment, startAddress))
            {
                if (MemoryManageUnit.isEffectiveAddress(segment, endAddress))
                {
                    int startIndex = (int)(startAddress - segment.startAddress);
                    for (int i = 0; i < bytes.length; ++i)
                    {
                        segment.memory[i + startIndex] = bytes[i];
                    }
                    return true;
                }
            }
        }
        return false;
    }

    protected byte[] readStream(DataInputStream is)
    {
        try
        {
            int len = is.readInt();
            byte[] arr = new byte[len];
            is.readFully(arr);
            return arr;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Util.reportErrorAndExit("致命错误：无法取得系统调用返回值");
        }
        return null;
    }
    protected byte[] readStreamFromStdout()
    {
        return readStream(stdout);
    }
}

class SYSopen extends StreamParameteredNativeSyscall
{
    protected byte[] getParamStream(RISCVMachine machine)
    {
        long address = machine.generalRegister[10];
        return loadString(machine, address);
    }
}

class SYSlseek extends NativeSyscall
{
    // standard NativeSyscall
}

class SYSread extends StreamReturnedNativeSyscall
{
    @Override
    protected void setReturnValue(RISCVMachine machine)
    {
        super.setReturnValue(machine);
        byte[] readValue = readStreamFromStdout();
        long address = machine.generalRegister[11];
        boolean result = saveBytes(machine, address, readValue);
        if (result == false)
        {
            machine.machineStateRegister = RISCVMachine.MACHINE_STAT[2].stat;
        }
    }
}

class SYSwrite extends StreamParameteredNativeSyscall
{
    @Override
    public void call(RISCVMachine machine)
    {
        long fd = machine.generalRegister[10];
        if (fd == 1 || fd == 2)
        {
            MachineManager.console.writeToScreen(getParamStream(machine));
        }
        else
        {
            super.call(machine);
        }
    }
    @Override
    protected byte[] getParamStream(RISCVMachine machine)
    {
        long address = machine.generalRegister[11];
        long length = machine.generalRegister[12];
        byte[] stream = loadBytes(machine, address, length);
        return stream;
    }
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

class SYStimes extends NativeSyscall
{
    // standard NativeSyscall
}

class SYSexit extends Syscall
{
    public void call(RISCVMachine machine)
    {
        machine.machineStateRegister = RISCVMachine.MACHINE_STAT[3].stat;
    }
}

//pseudo-syscalls
class SYSsetstarttime extends NativeSyscall
{
    public SYSsetstarttime()
    {
        this.name = new String("setstarttime");
        this.num = 2147483647;
    }
}

class SYSstdin extends StreamParameteredNativeSyscall
{
    byte[] bytesToWrite = null;
    public SYSstdin()
    {
        this.name = new String("stdin");
        this.num = 2147483646;
    }
    @Override
    protected byte[] getParamStream(RISCVMachine machine)
    {
        return bytesToWrite;
    }
}

class SYSclosemanager extends NativeSyscall
{
    public SYSclosemanager()
    {
        this.name = new String("closemanager");
        this.num = 2147483645;
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