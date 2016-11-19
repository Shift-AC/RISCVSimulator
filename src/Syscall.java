package com.github.ShiftAC.RISCVSimulator;

import java.util.*;
import java.io.*;
import java.net.*;

public abstract class Syscall
{
    long num;
    String name;

    public Syscall() {}
    abstract public void call(RISCVMachine machine);
}

class NativeSyscall extends Syscall
{
    static final int hostPort = 2333;

    static InputStream stdout;
    //static InputStream stderr;
    static OutputStream stdin;
    static
    {
        try
        {
            String nativeProgram = "bin/syscallServer";
            Process ps = Runtime.getRuntime().exec(nativeProgram);
            // standard library doesn't work well here.
            //stdin = ps.getOutputStream();
            //stdout = new InputStream(ps.getInputStream());
            //stderr = new InputStream(ps.getErrorStream());

            Socket socket = new Socket(
                InetAddress.getLocalHost(), hostPort);
            stdin = socket.getOutputStream();
            stdout = socket.getInputStream();
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
            System.out.println("calling");
            for (byte[] message : messages)
            {
                System.out.println(new String(message));
                stdin.write(message);
            }
            System.out.println("call");
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
            sb.append(num + " 0 0 0 0\n");
        }
        else
        {
            long[] generalRegister = machine.generalRegister;
            sb.append(num + " ");
            sb.append(generalRegister[10] + " ");
            sb.append(generalRegister[11] + " ");
            sb.append(generalRegister[12] + " ");
            sb.append(generalRegister[13] + "\n");
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
            machine.generalRegister[10] = getLongFromStdout();
            System.out.println("ret "  + machine.generalRegister[10]);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Util.reportErrorAndExit("致命错误：无法取得系统调用返回值");
        }
    }
    protected long getLongFromStdout()
        throws IOException
    {
        return getLongFromNetworkStream(stdout);
    }

    byte[] buf = new byte[1];
    protected long getLongFromNetworkStream(InputStream is)
        throws IOException
    {
        while (is.available() == 0);

        long ret = 0;
        is.read(buf, 0, 1);
        boolean isMinus = buf[0] == '-';
        if (!isMinus)
        {
            ret = buf[0] - '0';
        }
        while (is.available() != 0)
        {
            is.read(buf, 0, 1);
            if (buf[0] == '\n')
            {
                continue;
            }
            if (buf[0] == ' ')
            {
                break;
            }
            ret = ret * 10 + buf[0] - '0';
        }
        return isMinus ? -ret: ret;
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
        byte[] prefix = (new String(stream.length + " ")).getBytes();

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
        if (bytes == null)
        {
            return true;
        }
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

    protected byte[] readStream(InputStream is)
    {
        try
        {
            int len = (int)getLongFromNetworkStream(is);
            if (len == 0)
            {
                return null;
            }

            while (is.available() == 0);

            System.out.println("st " + len + " " + is.available());

            byte[] arr = new byte[len];
            is.read(arr, 0, len);
            System.out.println("retstream " + new String(arr));
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
    @Override
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

// pseudo-syscalls, used to control syscallManager only.
abstract class PseudoSyscall extends NativeSyscall
{
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
            System.out.println("calling");
            for (byte[] message : messages)
            {
                System.out.println("`" + new String(message) + '`');
                stdin.write(message);
            }
            System.out.println("call");
            stdin.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Util.reportErrorAndExit("致命错误：无法与系统调用管理器交互");
        }
    }
}

abstract class StreamParameteredPseudoSyscall 
    extends StreamParameteredNativeSyscall
{
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
            System.out.println("calling");
            for (byte[] message : messages)
            {
                System.out.println("`" + new String(message) + '`');
                stdin.write(message);
            }
            System.out.println("call");
            stdin.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Util.reportErrorAndExit("致命错误：无法与系统调用管理器交互");
        }
    }
}


class SYSsetstarttime extends PseudoSyscall
{
    public SYSsetstarttime()
    {
        this.name = new String("setstarttime");
        this.num = 2147483647;
    }
}

class SYSstdin extends StreamParameteredPseudoSyscall
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

class SYSclosemanager extends PseudoSyscall
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