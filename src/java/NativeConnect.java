package com.github.ShiftAC.RISCVSimulator;

import java.util.*;
import java.io.*;
import java.net.*;

public class NativeConnect
{
    int hostPort;

    ConnectReader out;
    ConnectWriter in;

    public NativeConnect(int hostPort, String serverName)
        throws IOException
    {
        Process ps = Runtime.getRuntime().exec(serverName);
        Socket socket = new Socket(InetAddress.getLocalHost(), hostPort);
        out = new ConnectReader(socket.getInputStream());
        in = new ConnectWriter(socket.getOutputStream());
    }
}

class ConnectReader
{
    InputStream out;
    
    public ConnectReader(InputStream out)
    {
        this.out = out; 
    }

    private boolean isWhiteSpace(int c)
    {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }

    private void blockCount(long count)
        throws IOException
    {
        while (out.available() < count);
    }

    private void block()
        throws IOException
    {
        blockCount(1);
    }

    public byte[] readStream()
        throws IOException
    {
        block();
        int len = (int)readLong();
        byte[] res = new byte[len];
        blockCount(len);
        out.read(res, 0, len);
        return res;
    }

    public long readLong()
        throws IOException
    {
        block();
        int b;
        while (isWhiteSpace(b = out.read()));

        long ret = 0;

        boolean isMinus = b == '-';
        if (!isMinus)
        {
            ret = b - '0';
        }
        while (out.available() != 0)
        {
            b = out.read();

            if (isWhiteSpace(b))
            {
                break;
            }
            ret = ret * 10 + b - '0';
        }
        return isMinus ? -ret: ret;
    }
}

class ConnectWriter
{
    OutputStream in;
    LinkedList<byte[]> buffer = new LinkedList<byte[]>();

    public ConnectWriter(OutputStream in)
    {
        this.in = in;
    }

    public void flush()
        throws IOException
    {
        while (!buffer.isEmpty())
        {
            in.write(buffer.remove());
        }
        in.flush();
    }

    public void write(byte[] bytes)
        throws IOException
    {
        if (bytes != null)
        {
            buffer.add(bytes);
        }
    }
}