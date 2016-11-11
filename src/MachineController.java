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
}

class DefMachineController extends MachineController
{
    long originalProgramCounter;
    public DefMachineController()
    {
        super();
    }

    public DefMachineController(RISCVMachine machine)
    {
        super(machine);
        originalProgramCounter = machine.programCounter;
    }

    @Override
    public void parse()
    {
        //System.out.println("???");
        machine.programCounter += 4;
    }

    @Override
    public void reset()
    {
        machine.programCounter = originalProgramCounter;
    }

    @Override
    protected void initSyscall()
    {

    }
}
