package com.github.ShiftAC.RISCVSimulator;

import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class CacheSimulator
{
    static final int hostPort = 23333;
    static final String traceFile = "tmp/trace.txt";
    NativeConnect connect;
    OutputStream trace = null;

    static final char READ = 'r';
    static final char WRITE = 'w';
    static final char GET_RESULT = 'g';
    static final char READ_AND_POST = 'R';
    static final char WRITE_AND_POST = 'W';
    static CacheLayerInfo[] defaultCache = 
    {
        new CacheLayerInfo(8, 64, 1 << 15, 1, 1),
        new CacheLayerInfo(8, 64, 1 << 18, 1, 1),
        new CacheLayerInfo(8, 64, 1 << 23, 1, 1)
    };

    public CacheSimulator()
    {
        try
        {
            connect = new NativeConnect(hostPort, "bin/cacheServer");
            Thread.sleep(3000);
            initCache(defaultCache);
            trace = new FileOutputStream(traceFile);
        }
        catch (Exception e)
        {
            Util.reportExceptionAndExit("致命错误：无法启动Cache模拟器", e);
        }
    }
    public void initCache(CacheLayerInfo[] layers)
    {
        try
        {
            connect.in.write(layers.length + "\n");
            for (CacheLayerInfo layer : layers)
            {
                connect.in.write(layer.toString() + "\n");
            }
        }
        catch (IOException e)
        {
            Util.reportExceptionAndExit("致命错误：无法向Cache模拟器发送数据", e);
        }
    }
    public void writeTrace(char op, long address)
    {
        try
        {
            //String msg = op + " " + address;
            //System.out.println(msg);
            trace.write((op + " " + address + "\n").getBytes());
            trace.flush();
        }
        catch (IOException e)
        {
            Util.reportExceptionAndExit("致命错误：无法写入Cache模拟器元数据", e);
        }
    }
    public void read(long address)
    {
        writeTrace('r', address);
    }
    public void write(long address)
    {
        writeTrace('w', address);
    }
    public void exit()
    {
        sendOp('q');
    }
    public void sendOp(char op)
    {
        try
        {
            connect.in.write(op + "");
            connect.in.flush();
        }
        catch (IOException e)
        {
            Util.reportExceptionAndExit("致命错误：无法向Cache模拟器发送数据", e);
        }
    }
    public CacheLog[] getResult()
    {
        try
        {
            //System.out.println("?????");
            trace.flush();
            trace.close();
            sendOp('p');
            
            sendOp('g');

            int level = (int)connect.out.readLong();
            //System.out.println(level);
            CacheLog[] logs = new CacheLog[level];

            for (int i = 0; i < logs.length; ++i)
            {
                logs[i] = new CacheLog();
                logs[i].read = connect.out.readLong();
                logs[i].readMiss = connect.out.readLong();
                logs[i].write = connect.out.readLong();
                logs[i].writeMiss = connect.out.readLong();
            }
            trace = new FileOutputStream(traceFile);
            return logs;
        }
        catch (IOException e)
        {
            Util.reportExceptionAndExit("致命错误：无法从Cache模拟器读取数据", e);
        }
        return null;
    }
}

class CacheLog
{
    long read;
    long write;
    long readMiss;
    long writeMiss;
}

class CacheLayerInfo
{
    int associative;
    int blockSize;
    int size;
    int writeBack;
    int writeAlloc;
    public CacheLayerInfo(int associative, int blockSize, int size, 
        int writeBack, int writeAlloc)
    {
        this.associative = associative;
        this.blockSize = blockSize;
        this.size = size;
        this.writeBack = writeBack;
        this.writeAlloc = writeAlloc;
    }
    @Override
    public String toString()
    {
        return size + " " + associative + " " + blockSize + " " + writeBack + 
            " " + writeAlloc;
    }
}