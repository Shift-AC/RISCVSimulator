package com.github.ShiftAC.RISCVSimulator;

class ControlSignals
{
    int needGRRead = 0;
    int needFRRead = 0;
    int needIALU = 0;
    int needFALU = 0;
    int needMRead = 0;
    int needMWrite = 0;
    int needGRWrite = 0;
    int needFRWrite = 0; 

    // control signals
    int grLength = 0;

    int iALUSetCnd = 0; // Cnd for "set" instructions
    int iALUA = 0;
    int iALUB = 0;
    int iALUOp = 0;
    int iALUIsUnsigned = 0;
    int iALUIsHigh = 0;
    int iALULength = 0;

    int fALUSetCnd = 0; // Cnd for "fset" instructions
    int fALUA = 0;
    int fALUB = 0;
    int fALUC = 0;
    int fALUOp = 0;

    int mAddr = 0;
    int mData = 0;
    int mLength = 0;
    int mIsUnsigned = 0;

    int grData = 0;
    int frData = 0;

    int pcSrc = 0;
    int pcCnd = 0;


    boolean initControlSignals(int insType) {
        needGRRead = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".needGRRead"))).intValue();
        needFRRead = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".needFRRead"))).intValue();
        needIALU = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".needIALU"))).intValue();
        needFALU = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".needFALU"))).intValue();
        needMRead = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".needMRead"))).intValue();
        needMWrite = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".needMWrite"))).intValue();
        needGRWrite = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".needGRWrite"))).intValue();
        needFRWrite = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".needFRWrite"))).intValue(); 

        grLength = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".grLength"))).intValue();

        iALUSetCnd = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".iALUSetCnd"))).intValue();
        iALUA = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".iALUA"))).intValue();
        iALUB = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".iALUB"))).intValue();
        iALUOp = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".iALUOp"))).intValue();
        iALUIsUnsigned = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".iALUIsUnsigned"))).intValue();
        iALUIsHigh = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".iALUIsHigh"))).intValue();
        iALULength = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".iALULength"))).intValue();


        fALUSetCnd = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".fALUSetCnd"))).intValue();
        fALUA = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".fALUA"))).intValue();
        fALUB = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".fALUB"))).intValue();
        fALUC = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".fALUC"))).intValue();
        fALUOp = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".fALUOp"))).intValue();

        mAddr = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".mAddr"))).intValue();
        mData = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".mData"))).intValue();
        mLength = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".mLength"))).intValue();
        mIsUnsigned = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".mIsUnsigned"))).intValue();

        grData = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".grData"))).intValue();
        frData = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".frData"))).intValue();

        pcSrc = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".pcSrc"))).intValue();
        pcCnd = ((Integer)(Util.configManager.getConfig(
            "instructions/" + insType + ".pcCnd"))).intValue();

        if (!(grLength == 0 || grLength == 1 || grLength == 2 ||
              grLength == 4 || grLength == 8)) {
            System.err.println("Control Signal: invalid grLength "
                + grLength + " in Instruction " + insType + " !");
            return false;
        }
        return true;
    }
}

abstract class MachineController extends CombineLogic
{
    RISCVMachine machine;
    CombineLogic[] modules;
    Syscall[] syscalls;

    ControlSignals[] controlSignals;

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

    abstract protected boolean initControlSignals();

    abstract protected void initSyscall();
    abstract protected void initCombineLogic();


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
        long num = this.machine.generalRegister[17];

/*	long[] reg = this.machine.generalRegister;
        System.out.println("Syscall #" + num);
        System.out.println("Name: " + findSyscall(num).name);
        System.out.printf("Param: %d %d %d %d\n", 
            reg[10], reg[11], reg[12], reg[13]);*/

        findSyscall(num).call(this.machine);
    }
}

class DefMachineController extends MachineController
{
    CombineLogic
        DECODER,
        GENE_REG,
        FLOAT_REG,
        I_ALU,
        F_ALU,
        MMU,
        STATE,
        PC_UPDATE;

    // index of current instruction in the sequence
    int insIndex = 0;
    // which instruction it is
    int insType = 0;
    // whether it is an invalid instruction
    int invalidIns = 0;


    public DefMachineController()
    {
        super();
    }

    public DefMachineController(RISCVMachine machine)
    {
        super(machine);
    }

    public void initCombineLogic() {
        modules = new CombineLogic[9];

        modules[0] = new Decoder(machine);
        modules[1] = new GeneralRegisterFile(machine);
        modules[2] = new FloatRegisterFile(machine);
        modules[3] = new IntegerALU(machine);
        modules[4] = new FloatALU(machine);
        modules[5] = new MemoryManageUnit(machine);
        modules[6] = new PCUpdate(machine);
        modules[7] = new MachineState(machine);

        DECODER = modules[0];
        GENE_REG = modules[1];
        FLOAT_REG = modules[2];
        I_ALU = modules[3];
        F_ALU = modules[4];
        MMU = modules[5];
        PC_UPDATE = modules[6];
        STATE = modules[7];
    }

    protected boolean initControlSignals() {
        int length = ((Integer)(Util.configManager.getConfig(
            "RISCVInstruction.length"))).intValue();
        controlSignals = new ControlSignals[length];

        boolean ret = true;
        for (int i = 0; i < length; ++i) {
	    controlSignals[i] = new ControlSignals();
            if (!controlSignals[i].initControlSignals(i))
                ret = false;
        }
        return ret;
    }

    void decode() {
        /* decoder */
        modules[0].findInputByName("insIndex").value = (long)insIndex;

        modules[0].parse();

        insType = (int)modules[0].findOutputByName("insType").value;
        invalidIns = (int)modules[0].findOutputByName("invalidIns").value;
    }

    void geneRegRead() {
        /* general register */
        modules[1].findInputByName("gregRead").value = 1;
        modules[1].findInputByName("gregWrite").value = 0;
        modules[1].findInputByName("regData").value = 0;
        modules[1].findInputByName("regLength").value =
            (long)controlSignals[insType].grLength;

        bind(DECODER, "src1", GENE_REG, "src1");
        bind(DECODER, "src2", GENE_REG, "src2");

        modules[1].parse();

    }

    void floatRegRead() {
        /* float register */
        modules[2].findInputByName("fregRead").value = 1;
        modules[2].findInputByName("fregWrite").value = 0;
        modules[2].findInputByName("regData").value = 0;

        bind(DECODER, "src1", FLOAT_REG, "src1");
        bind(DECODER, "src2", FLOAT_REG, "src2");
        bind(DECODER, "src3", FLOAT_REG, "src3");

        modules[2].parse();
    }

    void intALU() {
        /* integer ALU */
        modules[3].findInputByName("aluA").value =
            (long)controlSignals[insType].iALUA;
        modules[3].findInputByName("aluB").value =
            (long)controlSignals[insType].iALUB;
        modules[3].findInputByName("aluOp").value =
            (long)controlSignals[insType].iALUOp;
        modules[3].findInputByName("aluLength").value =
            (long)controlSignals[insType].iALULength;
        modules[3].findInputByName("aluIsHigh").value =
            (long)controlSignals[insType].iALUIsHigh;
        modules[3].findInputByName("aluIsUnsigned").value =
            (long)controlSignals[insType].iALUIsUnsigned;
        modules[3].findInputByName("aluSetCnd").value = 
            (long)controlSignals[insType].iALUSetCnd;
        modules[3].findInputByName("pc").value =
            machine.programCounter;

        bind(DECODER, "shamt", I_ALU, "shamt");
        bind(DECODER, "imm", I_ALU, "imm");
        bind(GENE_REG, "val1", I_ALU, "reg1");
        bind(GENE_REG, "val2", I_ALU, "reg2");

        modules[3].parse();
    }

    void floatALU() {
        /* float ALU */
        modules[4].findInputByName("fALUA").value = 
            (long)controlSignals[insType].fALUA;
        modules[4].findInputByName("fALUB").value = 
            (long)controlSignals[insType].fALUB;
        modules[4].findInputByName("fALUC").value = 
            (long)controlSignals[insType].fALUC;
        modules[4].findInputByName("fALUOp").value = 
            (long)controlSignals[insType].fALUOp;
        modules[4].findInputByName("fALUSetCnd").value = 
            (long)controlSignals[insType].fALUSetCnd;

        bind(DECODER, "fmt", F_ALU, "fmt");
        bind(DECODER, "rm", F_ALU, "rm");
        bind(GENE_REG, "val1", F_ALU, "ival1");
        bind(FLOAT_REG, "fval1", F_ALU, "fval1");
        bind(FLOAT_REG, "fval2", F_ALU, "fval2");
        bind(FLOAT_REG, "fval3", F_ALU, "fval3");

        modules[4].parse();

        //bind(F_ALU, "meetNAN", STATE, "meetNAN");
    }

    void memory() {
        /* memory manage unit */
        modules[5].findInputByName("memRead").value = 
            (long)controlSignals[insType].needMRead;
        modules[5].findInputByName("memWrite").value = 
            (long)controlSignals[insType].needMWrite;
        modules[5].findInputByName("memAddr").value = 
            (long)controlSignals[insType].mAddr;
        modules[5].findInputByName("memData").value = 
            (long)controlSignals[insType].mData;
        modules[5].findInputByName("memLength").value = 
            (long)controlSignals[insType].mLength;
        modules[5].findInputByName("memIsUnsigned").value =
            (long)controlSignals[insType].mIsUnsigned;

        bind(DECODER, "imm", MMU, "imm");
        bind(GENE_REG, "val1", MMU, "ival1");
        bind(GENE_REG, "val2", MMU, "ival2");
        bind(FLOAT_REG, "fval2", MMU, "fval2");
        bind(I_ALU, "valE", MMU, "ivalE");
        bind(F_ALU, "fvalE", MMU, "fvalE");

        modules[5].parse();

        //bind(MMU, "invalidAddress", STATE, "iaMMU");
    }

    void geneRegWrite() {
        /* general register */
        modules[1].findInputByName("gregRead").value = 0;
        modules[1].findInputByName("gregWrite").value = 1;
        modules[1].findInputByName("regData").value =
            (long)controlSignals[insType].grData;

        bind(DECODER, "dst", GENE_REG, "dst");
        bind(I_ALU, "valE", GENE_REG, "ivalE");
        bind(F_ALU, "valE", GENE_REG, "fivalE");
        bind(MMU, "valM", GENE_REG, "valM");
        bind(DECODER, "imm", GENE_REG, "imm");
        bind(I_ALU, "cnd", GENE_REG, "cnd");

        modules[1].parse();
    }

    void floatRegWrite() {
        /* float register*/
        modules[2].findInputByName("fregRead").value = 0;
        modules[2].findInputByName("fregWrite").value = 1;
        modules[2].findInputByName("regData").value =
            (long)controlSignals[insType].frData;

        bind(DECODER, "dst", FLOAT_REG, "dst");
        bind(F_ALU, "fvalE", FLOAT_REG, "fvalE");
        bind(MMU, "valM", FLOAT_REG, "fvalM");

        modules[2].parse();
    }

    void pcUpdate() {
        /* PC update */
        modules[6].findInputByName("pcSrc").value = 
            (long)controlSignals[insType].pcSrc;
        modules[6].findInputByName("pcCnd").value = 
            (long)controlSignals[insType].pcCnd;
        modules[6].findInputByName("pc").value =
            machine.programCounter;

        bind(DECODER, "imm", PC_UPDATE, "imm");
        bind(I_ALU, "cnd", PC_UPDATE, "cnd");
        bind(GENE_REG, "val1", PC_UPDATE, "ival1");

        modules[6].parse();
    }

    void machineState() {
        /* machine state */
        long halt = 0;
	long meetNAN = 0;
        // if PC overflow, then halt = 1
        
        modules[7].findInputByName("halt").value = halt;
	modules[7].findInputByName("meetNAN").value = meetNAN;

        bind(DECODER, "invalidIns", STATE, "iInsDecoder");
        bind(MMU, "invalidAddress", STATE, "iAddrMMU");
        //bind(F_ALU, "meetNAN", STATE, "meetNAN");

        modules[7].parse();
    }

    @Override
    public void parse()
    {
	// get current PC index
        insIndex = machine.getPCIndex();

	//System.err.printf("executing Ins %d: %08x %s\n", insIndex,
	//	machine.instructions[insIndex].code,
	//	machine.instructions[insIndex].asm);

        decode();

        if (invalidIns == 0) {
            // special check for syscall
            if (insType == RISCVInstruction.ECALL) {
                doSyscall();
		if (!machine.isRunnable())
		    return;
                pcUpdate();
                machineState();
                return;
            }

            if (controlSignals[insType].needGRRead == 1)
                geneRegRead();
            if (controlSignals[insType].needFRRead == 1)
                floatRegRead();

            if (controlSignals[insType].needIALU == 1)
                intALU();
            if (controlSignals[insType].needFALU == 1)
                floatALU();

            if ((controlSignals[insType].needMRead == 1) ||
                (controlSignals[insType].needMWrite == 1))
                memory();

            if (controlSignals[insType].needGRWrite == 1)
                geneRegWrite();
            if (controlSignals[insType].needFRWrite == 1)
                floatRegWrite();

            pcUpdate();
        }

        machineState();

	machine.generalRegister[0] = 0;
    }

    @Override
    public void reset()
    {
        machine.programCounter = 
            machine.memory[RISCVMachine.SEGMENT_TEXT].startAddress;
	machine.machineStateRegister =
	    RISCVMachine.MACHINE_STAT[0].stat;
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
                syscalls[i] = (Syscall)(Util.configManager.getConfig(
                    "DefMachineController.syscall" + i));
            }
        }
        catch (Exception e)
        {
            Util.reportErrorAndExit(
                "致命错误：无法读取系统调用信息\n类名：" + "SYS" + syscallName);
        }
    }
}
