package com.github.ShiftAC.RISCVSimulator;

class MemorySegment
{
    long startAddress;
    long endAddress;
    byte[] memory;

    boolean readable;
    boolean writable;
    boolean executable;

    public MemorySegment()
    {
        memory = new byte[2];   // to avoid nullPointerException...
        readable = true;
    }

    public MemorySegment(MemorySegment src)
    {
        this.startAddress = src.startAddress;
        this.endAddress = src.endAddress;
        this.memory = new byte[src.memory.length];
        for (int i = 0; i < src.memory.length; ++i)
        {
            this.memory[i] = src.memory[i];
        }
        this.readable = src.readable;
        this.writable = src.writable;
        this.executable = src.executable;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("----Memory Segment----\n");
        str.append(String.format("startAddress: %x\n", startAddress));
        str.append(String.format("endAddress  : %x\n", endAddress));
        str.append(String.format("readable    : %b\n", readable));
        str.append(String.format("writable    : %b\n", writable));
        str.append(String.format("executable  : %b\n", executable));
        /*System.err.printf("byte[]:\n");
        for (int i = 0; i < memory.length; ++i) {
            System.err.printf("%02x " , memory[i]);
            if (i%16 == 15)
                System.err.printf("\n");
        }*/
        return str.toString();
    }
}

class Symbol
{
    int segment;
    long address;
    String name;

    public Symbol() {}

    public Symbol(int segment, long address, String name)
    {
        this.name = name;
        this.segment = segment;
        this.address = address;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("----Symbol----\n");
        str.append(String.format("segment     : %x\n", segment));
        str.append(String.format("address     : %x\n", address));
        str.append(String.format("name        : %s\n", name));
        return str.toString();     
    }
}

class MachineStat
{
    int stat;
    String name;

    public MachineStat(int _stat, String _name) {
        stat = _stat;
        name = _name;
    }
}

public class RISCVMachine
{
    static final int
        SEGMENT_UNDEF = ((Integer)(Util.configManager.getConfig(
            "RISCVMachine.undefSegment"))).intValue(),
        SEGMENT_TEXT = ((Integer)(Util.configManager.getConfig(
            "RISCVMachine.textSegment"))).intValue(),
        SEGMENT_RODATA = ((Integer)(Util.configManager.getConfig(
            "RISCVMachine.rodataSegment"))).intValue(),
        SEGMENT_INIT_ARRAY = ((Integer)(Util.configManager.getConfig(
            "RISCVMachine.initArraySegment"))).intValue(),
        SEGMENT_FINI_ARRAY = ((Integer)(Util.configManager.getConfig(
            "RISCVMachine.finiArraySegment"))).intValue(),
        SEGMENT_EH_FRAME = ((Integer)(Util.configManager.getConfig(
            "RISCVMachine.ehFrameSegment"))).intValue(),
        SEGMENT_JCR = ((Integer)(Util.configManager.getConfig(
            "RISCVMachine.jcrSegment"))).intValue(),
        SEGMENT_DATA = ((Integer)(Util.configManager.getConfig(
            "RISCVMachine.dataSegment"))).intValue(),
        SEGMENT_SDATA = ((Integer)(Util.configManager.getConfig(
            "RISCVMachine.sdataSegment"))).intValue(),
        SEGMENT_SBSS = ((Integer)(Util.configManager.getConfig(
            "RISCVMachine.sbssSegment"))).intValue(),
        SEGMENT_BSS = ((Integer)(Util.configManager.getConfig(
            "RISCVMachine.bssSegment"))).intValue(),
        SEGMENT_HEAP = ((Integer)(Util.configManager.getConfig(
            "RISCVMachine.heapSegment"))).intValue(),
        SEGMENT_STACK = ((Integer)(Util.configManager.getConfig(
            "RISCVMachine.stackSegment"))).intValue();
    static final int NUM_OF_MEMSEG = 13;
    
    static MachineStat[] MACHINE_STAT;
    static {
        MACHINE_STAT = new MachineStat[4];
        MACHINE_STAT[0] = new MachineStat(0, "AOK");
        MACHINE_STAT[1] = new MachineStat(1, "INS");
        MACHINE_STAT[2] = new MachineStat(2, "ADR");
        MACHINE_STAT[3] = new MachineStat(3, "HLT");
    }

    MemorySegment[] memory;

    Symbol[] symbol;

    long[] generalRegister;
    int[] floatRegister;

    long programCounter;

    int machineStateRegister;

    RISCVInstruction[] instructions;

    MachineController controller;

    public RISCVMachine()
    {
        int count;
        
        // debug
        //symbol = new Symbol[1];
        //symbol[0] = new Symbol(0, 98731983, "Symbol");

        count = ((Integer)(Util.configManager.getConfig(
            "RISCVMachine.generalRegisterCount"))).intValue();
        generalRegister = new long[count];
        generalRegister[2] = Util.STACK_BEGIN;      // register "sp"
        
        count = ((Integer)(Util.configManager.getConfig(
            "RISCVMachine.floatRegisterCount"))).intValue();
        floatRegister = new int[count];

        memory = new MemorySegment[NUM_OF_MEMSEG];
        for (int i = 0; i < NUM_OF_MEMSEG; ++i)
            memory[i] = new MemorySegment();

        machineStateRegister = MACHINE_STAT[0].stat;
        // instructions.asm?

        String controlName = (String)(Util.configManager.getConfig(
                "RISCVMachine.machineControllerName"));
        controlName = Util.packageName + "." + controlName;
        try
        {
            controller = 
                (MachineController)Class.forName(controlName).newInstance();
            controller.machine = this;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Util.reportErrorAndExit(
                "致命错误：无法读取虚拟机控制部件信息\n类名：" + controlName);
        }
    }

    void printInfo() {
        int i;
        for (i = 0; i < memory.length; ++i)
            System.err.printf("%s", memory[i].toString());
        for (i = 0; i < symbol.length; ++i)
            System.err.printf("%s", symbol[i].toString());
        System.err.printf("\n========generalRegister========\n");
        for (i = 0; i < generalRegister.length; ++i)
            System.err.printf("%#x ", generalRegister[i]);
        System.err.printf("\n\n");
        System.err.printf("========floatRegister========\n");
        for (i = 0; i < floatRegister.length; ++i)
            System.err.printf("%#x ", floatRegister[i]);
        System.err.printf("\n\n");
        System.err.printf("========machineStateRegister========\n");
        System.err.printf("%x\n\n", machineStateRegister);
        System.err.printf("========programCounter========\n");
        System.err.printf("%x\n\n", programCounter);
        System.err.printf("========Instruction========\n");
        for (i = 0; i < instructions.length; ++i)
            System.err.printf("%s", instructions[i].toString());
        System.err.printf("\n");       
    }

    public void stepOperate()
    {
        controller.parse();
    }

    public void turnBreakpointState(int index)
    {
        instructions[index].isBreakpoint = !instructions[index].isBreakpoint;
    }

    public boolean isRunnable()
    {
        return true;
    }
}