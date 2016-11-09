import java.util.*;
import java.io.*;
import java.text.*;
import java.util.concurrent.*;

class MessageQueue
{
    protected LinkedList<Object> queue;

    public MessageQueue()
    {
        queue = new LinkedList<Object>();
    }

    private static final int 
        MESSAGE_ADD = 0,
        MESSAGE_REMOVE = 1,
        MESSAGE_PEEK = 2;
    private synchronized Object access(Object arg, int operation)
    {
        switch (operation)
        {
        case MESSAGE_ADD:
            if (arg != null)
            {
                queue.add(arg);
            }
            break;
        case MESSAGE_REMOVE:
            if (queue.isEmpty())
            {
                break;
            }
            return queue.remove();
        case MESSAGE_PEEK:
            if (queue.isEmpty())
            {
                break;
            }
            return queue.peek();
        }
        return null;
    }
    public void add(Object arg)
    {
        access(arg, MESSAGE_ADD);
    }
    public Object remove()
    {
        return access(null, MESSAGE_REMOVE);
    }
    public Object peek()
    {
        return access(null, MESSAGE_PEEK);
    }
}

class MachineInfo
{
    Long[] generalRegister;
    Float[] floatRegister;
    Long programCounter;
    Integer machineStateRegister;
    Symbol[] symbol;

    public MachineInfo(RISCVMachine machine)
    {
        generalRegister = new Long[machine.generalRegister.length];
        floatRegister = new Float[machine.floatRegister.length];
        symbol = machine.symbol;
    }

    public void record(RISCVMachine machine)
    {
        for (int i = 0; i < generalRegister.length; ++i)
        {
            generalRegister[i] = new Long(machine.generalRegister[i]);
        }
        for (int i = 0; i < floatRegister.length; ++i)
        {
            floatRegister[i] = new Float(
                Float.intBitsToFloat(machine.floatRegister[i]));
        }
        programCounter = new Long(machine.programCounter);
        machineStateRegister = new Integer(machine.machineStateRegister);
    }

    public void save(RISCVMachine machine)
    {
        for (int i = 0; i < generalRegister.length; ++i)
        {
            machine.generalRegister[i] = generalRegister[i].longValue();
        }
        for (int i = 0; i < floatRegister.length; ++i)
        {
            machine.floatRegister[i] = Float.floatToIntBits(
                floatRegister[i].floatValue());
        }
        machine.programCounter = programCounter.longValue();
        machine.machineStateRegister = machineStateRegister.intValue();
    }
}

class MachineStateSnapshot extends MachineInfo
{
    MemoryManageUnit mmu;
    Byte[] memoryFrag;
    Long startAddress;

    public MachineStateSnapshot(RISCVMachine machine)
    {
        super(machine);
        int fragLength = ((Integer)(Util.configManager.getConfig(
            "MachineStateSnapshot.memoryFragmentLength"))).intValue();
        memoryFrag = new Byte[fragLength];
        mmu = new MemoryManageUnit(machine);
    }

    public void record(RISCVMachine machine, long startAddress)
    {
        super.record(machine);

        this.startAddress = new Long(startAddress);
        for (int i = 0; i < memoryFrag.length; ++i)
        {
            memoryFrag[i] = mmu.loadByte(startAddress + i, true).byteValue();
        }
    }

    public void save(RISCVMachine machine)
    {
        super.save(machine);

        for (int i = 0; i < memoryFrag.length; ++i)
        {
            if (memoryFrag[i] == null)
            {
                continue;
            }
            mmu.saveByte(memoryFrag[i].byteValue(), 
                         (byte)(startAddress.longValue() + i));
        }
    }
}

public class MachineManager implements Runnable
{
    static private class MachineInitInfo extends MachineInfo
    {
        MemorySegment[] savedSegments;

        public MachineInitInfo(RISCVMachine machine)
        {
            super(machine);
            record(machine);
        }

        public void record(RISCVMachine machine)
        {
            super.record(machine);

            int segmentCount = ((Integer)(Util.configManager.getConfig(
                "MachineInitInfo.savedSegmentCount"))).intValue();
            savedSegments = new MemorySegment[segmentCount];
            for (int i = 0; i < segmentCount; ++i)
            {
                savedSegments[i] = 
                    new MemorySegment(machine.memory[getSegmentIndex(i)]);
            }
        }

        public void resetMachine(RISCVMachine machine)
        {
            super.save(machine);

            int segmentCount = savedSegments.length;
            for (int i = 0; i < segmentCount; ++i)
            {
                byte[] dest = machine.memory[getSegmentIndex(i)].memory;
                for (int j = 0; j < savedSegments[i].memory.length; ++i)
                {
                    dest[j] = savedSegments[i].memory[j];
                }
            }

            machine.controller.reset();
        }

        private int getSegmentIndex(int index)
        {
            String segmentName = (String)(Util.configManager.getConfig(
                "MachineInfo.savedSegmentName" + index));

            return ((Integer)(Util.configManager.getConfig(
                "RISCVMachine." + segmentName))).intValue();
        }
    }

    static MessageQueue messageQueue = new MessageQueue();
    static MessageQueue notifyQueue = new MessageQueue();
    static MachineManager instance = new MachineManager();
    static private RISCVMachine machine = null;
    static public MachineStateSnapshot snapshot = null;
    static private MachineInitInfo initInfo = null;

    static private boolean simulatorWorking = false; 
    static private boolean managerWorking = false;

    static void setMachine(RISCVMachine newMachine)
    {
        machine = newMachine;
        snapshot = new MachineStateSnapshot(machine);
        initInfo = new MachineInitInfo(machine);
    }

    static boolean checkMachine()
    {
        return machine != null;
    }

    static boolean checkRunnable()
    {
        if (managerWorking)
        {
            return false;
        }
        if (!checkMachine())
        {
            return false;
        }
        return MachineManager.machine.isRunnable();
    }

    static void sleepIgnoreInterrupt(long time)
    {
        try
        {
            Thread.sleep(time);
        }
        catch (Exception e) {}
    }

    /*
     * deprecated since there's no need to read the machine itself.
     * use updateSnapshot instead.

    // to read machine state: hold this lock
    // this function will attempt to lock the machine for 60 times, after each 
    // attempt it will sleep for 50ms.
    // if it fails, null will be returned.
    // CAUTION: only READ is permitted! 
    static RISCVMachine lockAndGetMachine()
    {
        simulatorWorking = true;
        
        for (int attempt = 0; attempt < 60; ++attempt);
        {
            if (!managerWorking)
            {
                return machine;
            }
            sleepIgnoreInterrupt(50);
        }
        return null;
    }

    // call this to release read lock.
    // CAUTION: don't access the machine after calling this!
    static void unlockMachine()
    {
        simulatorWorking = false;
    }
    */

    static public boolean updateSnapshot(long startAddress)
    {
        simulatorWorking = true;

        for (int attempt = 0; attempt < 60; ++attempt);
        {
            if (!managerWorking)
            {
                snapshot.record(machine, startAddress);
                simulatorWorking = false;
                return true;
            }
            sleepIgnoreInterrupt(50);
        }

        simulatorWorking = false;
        return false;
    }

    static public boolean updateMachine()
    {
        simulatorWorking = true;

        for (int attempt = 0; attempt < 60; ++attempt);
        {
            if (!managerWorking)
            {
                snapshot.save(machine);
                simulatorWorking = false;
                return true;
            }
            sleepIgnoreInterrupt(50);
        }

        simulatorWorking = false;
        return false;
    }

    private MachineManager() {}

    @Override
    public void run()
    {
        for (; true; sleepIgnoreInterrupt(50))
        {
            if (simulatorWorking)
            {
                continue;
            }
            
            String message = (String)messageQueue.remove();
            managerWorking = message != null;
            if (managerWorking)
            {
                if (message.length() == 0)
                {
                    managerWorking = false;
                    continue;
                }
                char c = message.charAt(0);
                switch (c)
                {
                case 'S':
                    step();
                    break;
                case 'R':
                    reset();
                    break;
                case 'T':
                    terminate();
                    break;
                case 'P':
                    pause();
                    break;
                case 'C':
                    start();
                    break;
                case 'B':
                    breakpoint(Integer.parseInt(message.substring(1)));
                    break;
                default:
                    System.err.println(
                        "MessageManager: Unexpected message " + c);
                }
                managerWorking = false;
            }
        }
    }

    // to modify machine state: must hold managerWorking lock.
    private void step()
    {
        machine.stepOperate();
    }

    private void reset()
    {
        terminate();
        start();
    }

    private void terminate()
    {
        initInfo.resetMachine(machine);
        notifyQueue.add("T");
    }

    private void pause()
    {
        // machine supports only step operate, so "pause" doesn't make sense.
        // we will just notify the program to update UI.
        notifyQueue.add("P");
    }

    private void start()
    {
        String message;
        while (machine.isRunnable())
        {
            message = (String)messageQueue.peek();
            if (message != null)
            {
                if (message.length() != 0)
                {
                    char c = message.charAt(0);
                    if (c == 'T' || c == 'P')
                    {
                        break;
                    }
                }
                messageQueue.remove();
            }
            machine.stepOperate();
        }
    }

    private void breakpoint(int index)
    {
        machine.turnBreakpointState(index);
    }
}