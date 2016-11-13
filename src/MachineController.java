package com.github.ShiftAC.RISCVSimulator;

abstract class MachineController extends CombineLogic
{
    RISCVMachine machine;
    CombineLogic[] modules;
    Syscall[] syscalls;

    public MachineController()
    {
        super();
 
        this.machine = null;
    }

    public MachineController(RISCVMachine machine)
    {
        super();

        this.machine = machine;
    }

    // do nothing since we don't need signals for a controller.
    protected void initSignals() {}

    abstract protected void initSyscall();

    // bind a CombineLogic's input to other CombineLogic's output
    // CAUTION: if an input signal is binded to an output signal, it will 
    // ignore it's original value and therefore setInput won't be able to
    // change it's value anymore. to set it's value, unbind() is needed.
    static protected boolean bind(CombineLogic srcLogic, String srcName, 
        CombineLogic destLogic, String destName)
    {
        Signal dest = destLogic.findInputByName(destName);
        Signal src = srcLogic.findOutputByName(srcName);

        if (dest == null || src == null)
        {
            return false;
        }

        dest.bind = src;

        return true;
    }

    static protected boolean unbind(CombineLogic logic, String name)
    {
        Signal signal = logic.findInputByName(name);
        
        if (signal == null)
        {
            return false;
        }

        signal.bind = null;

        return true;
    }

    public Syscall findSyscall(long num)
    {
        for (int i = 0; i < syscalls.length; ++i)
        {
            if (syscalls[i].num == num)
            {
                return syscalls[i];
            }
        }
        return null;
    }

    public void doSyscall()
    {
        // don't know which register to copy yet
        long num = 0;

        findSyscall(num).call(this.machine);
    }
}

class DefMachineController extends MachineController
{
    public DefMachineController()
    {
        super();
        initSyscall();
    }

    public DefMachineController(RISCVMachine machine)
    {
        super(machine);
        initSyscall();
    }

    @Override
    public void parse()
    {
        //System.out.println("???");
        if (machine.getPCIndex() == machine.instructions.length - 1)
        {
            machine.machineStateRegister = RISCVMachine.MACHINE_STAT[3].stat;
            return;
        }
        machine.programCounter += 4;
    }

    @Override
    public void reset()
    {
        machine.programCounter = 
            machine.memory[RISCVMachine.SEGMENT_TEXT].startAddress;
    }

    @Override
    protected void initSyscall()
    {
        int syscallCount = ((Integer)(Util.configManager.getConfig(
            "DefMachineController.syscallCount"))).intValue();
        syscalls = new Syscall[syscallCount];
        
        String syscallName = null;
        String syscallClass = null;
        try
        {
            for (int i = 0; i < syscallCount; ++i)
            {
                syscallName = (String)(Util.configManager.getConfig(
                    "DefMachineController.syscallName" + i));
                syscallClass = Util.packageName + ".SYS" + syscallName;
                syscalls[i] = 
                    (Syscall)Class.forName(syscallClass).newInstance();
                syscalls[i].num = ((Integer)(Util.configManager.getConfig(
                    "DefMachineController.syscallNum" + i))).intValue();
                syscalls[i].name = syscallName;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Util.reportErrorAndExit(
                "致命错误：无法读取系统调用信息\n类名：" + "SYS" + syscallName);
        }
    }
}
