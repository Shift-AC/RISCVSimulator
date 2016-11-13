package com.github.ShiftAC.RISCVSimulator;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.text.*;

class Util
{
    static final String packageName = "com.github.ShiftAC.RISCVSimulator";
    // for sh_type
    static final int SHT_PROGBITS = 0x1;
    static final int SHT_NOBITS = 0x8;

    static final long STACK_END = 0x0FFFFFFFFF000000L;
    static final long STACK_BEGIN = 0x0FFFFFFFFF800000L;

    static final String[] NAMES_OF_MEMSEG = new String[]{
        "UNDEF", ".text", ".rodata", ".init_array", ".fini_array",
        ".eh_frame", ".jcr", ".data", ".sdata", ".sbss", ".bss"
    };

    static private long byteArray2Number(byte[] ba, int offset, int length) {
    	long res = 0;
    	for (int i = length+offset-1; i >= offset; --i) {
    		res = (res<<8) + (ba[i] & 0x0FF);
    	}
    	return res;
    }
    static long byteArray2Long(byte[] ba, int offset) {
    	return byteArray2Number(ba, offset, 8);
   }
    static int byteArray2Int(byte[] ba, int offset) {
    	return (int)(byteArray2Number(ba, offset, 4));
    }
    static short byteArray2Short(byte[] ba, int offset) {
    	return (short)(byteArray2Number(ba, offset, 2));
    }

    static private byte[] number2byteArray(long num, int length) {
        byte[] ba = new byte[length];
        for (int i = 0; i < length; ++i) {
            ba[i] = (byte)(num & 0xFF);
            num >>= 8;
        }
        return ba;
    }
    static byte[] long2byteArray(long num) {
        return number2byteArray(num, 8);
    }
    static byte[] int2byteArray(int num) {
        return number2byteArray(num, 4);
    }
    static byte[] short2byteArray(short num) {
        return number2byteArray(num, 2);
    }


    static String version = "1.0.0001a";
    static String[] configFileNames = 
    {
        "RISCVMachine",
        "MachineStateSnapshot",
        "MemoryViewFrame",
        "MachineInitInfo",
        "ProgramView",
        "CodeLinePane",
        "DefMachineController"
    };
    static ConfigManager configManager;
    static SYSclosemanager closemanager = new SYSclosemanager();
    static void reportException(String description, Exception e)
    {
        JOptionPane.showMessageDialog(
            null, 
            description + "\n" + e.getMessage(),
            "错误",
            JOptionPane.ERROR_MESSAGE);
    }

    static void reportError(String message)
    {
        JOptionPane.showMessageDialog(
            null, 
            message,
            "错误",
            JOptionPane.ERROR_MESSAGE);
    }
    
    static void reportErrorAndExit(String message)
    {
        reportError(message);
        closemanager.call(null);
        System.exit(0);
    }

    static void reportExceptionAndExit(String description, Exception e)
    {
        reportException(description, e);
        closemanager.call(null);
        System.exit(0);
    }

    public static int min(int a, int b)
    {
        return a < b ? a : b;
    }

    static
    {
        try
        {
            configManager = new ConfigManager(configFileNames);
        }
        catch (FileNotFoundException fe)
        {
            reportExceptionAndExit("打开关键文件时遇到问题，错误信息:", fe);
        }
        catch (IOException ie)
        {
            reportExceptionAndExit("读取关键文件时遇到问题，错误信息:", ie);
        }
    }
    
    static void sleepIgnoreInterrupt(long time)
    {
        try
        {
            Thread.sleep(time);
        }
        catch (Exception e) {}
    }

}
