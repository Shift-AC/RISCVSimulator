package com.github.ShiftAC.RISCVSimulator;

import java.util.*;
import java.io.*;
import java.net.*;

public class CacheSimulator
{
    static final int hostPort = 23333;
    NativeConnect connect;

    static final char READ = 'r';
    static final char WRITE = 'w';
    static final char GET_RESULT = 'g';
    static final char READ_AND_POST = 'R';
    static final char WRITE_AND_POST = 'W';

    public CacheSimulator()
    {
        try
        {
            connect = new NativeConnect(hostPort, "bin/cacheServer");
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
    public void sendTrace(char op, long address)
    {
        try
        {
            connect.in.write(op + " " + address);
            connect.in.flush();
        }
        catch (IOException e)
        {
            Util.reportExceptionAndExit("致命错误：无法向Cache模拟器发送数据", e);
        }
    }
    public void sendRead(long address)
    {
        sendTrace('r', address);
    }
    public void sendWrite(long address)
    {
        sendTrace('w', address);
    }
    public CacheLog getResult()
    {
        try
        {
            sendTrace('g', 0);
            
            CacheLog log = new CacheLog();

            log.total = connect.out.readLong();
            log.miss = connect.out.readLong();

            return log;
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
    long total;
    long miss;
}

class CacheLayerInfo
{
    int associative;
    int blockSize;
    int size;
    boolean writeBack;
    boolean writeThrough;
    public CacheLayerInfo(int associative, int blockSize, int size, 
        boolean writeBack, boolean writeThrough)
    {
        this.associative = associative;
        this.blockSize = blockSize;
        this.size = size;
        this.writeBack = writeBack;
        this.writeThrough = writeThrough;
    }
    @Override
    public String toString()
    {
        int wb = writeBack ? 1 : 0;
        int wt = writeThrough ? 1 : 0;
        return associative + " " + blockSize + " " + size + " " + wb + " " + wt;
    }
}