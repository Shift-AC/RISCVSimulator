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
    abstract protected void initControlSignals();
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
        long num = this.machine.generalRegister[10];

        findSyscall(num).call(this.machine);
    }
}

class DefMachineController extends MachineController
{
    CombineLogic
        DECODER,
        GENE_REG,
        FLOAT_REG,
        I_ALUMUX,
        I_ALU,
        F_ALU,
        MMU,
        STATE,
        PC_UPDATE;

    int insIndex;
    int opcode;
    int funct7;
    int funct6;
    int funct5;
    int funct3;

    public DefMachineController()
    {
        super();
    }

    public DefMachineController(RISCVMachine machine)
    {
        super(machine);
        initSyscall();
        initCombineLogic();
    }


    public void initCombineLogic() {
        modules = new CombineLogic[9];

        modules[0] = new Decoder(machine);
        modules[1] = new GeneralRegisterFile(machine);
        modules[2] = new FloatRegisterFile(machine);
        modules[3] = new IntegerALUMux(machine);
        modules[4] = new IntegerALU(machine);
        modules[5] = new FloatALU(machine);
        modules[6] = new MemoryManageUnit(machine);
        modules[7] = new MachineState(machine);
        modules[8] = new PCUpdate(machine);

        DECODER = modules[0];
        GENE_REG = modules[1];
        FLOAT_REG = modules[2];
        I_ALUMUX = modules[3];
        I_ALU = modules[4];
        F_ALU = modules[5];
        MMU = modules[6];
        STATE = modules[7];
        PC_UPDATE = modules[8];
    }

    void decode() {
        /* decoder */
        modules[0].findInputByName("insIndex").value = (long)insIndex;

        modules[0].parse();

        bind(DECODER, "src1", GENE_REG, "src1");
        bind(DECODER, "src2", GENE_REG, "src2");
        bind(DECODER, "dst", GENE_REG, "dst");
        bind(DECODER, "src1", FLOAT_REG, "src1");
        bind(DECODER, "src2", FLOAT_REG, "src2");
        bind(DECODER, "src3", FLOAT_REG, "src3");
        bind(DECODER, "dst", FLOAT_REG, "dst");
        bind(DECODER, "opcode", I_ALUMUX, "opcode");
        bind(DECODER, "funct7", I_ALUMUX, "funct7");
        bind(DECODER, "funct6", I_ALUMUX, "funct6");
        bind(DECODER, "funct3", I_ALUMUX, "funct3");
        bind(DECODER, "shamt", I_ALUMUX, "shamt");
        bind(DECODER, "imm", I_ALUMUX, "imm");
        bind(DECODER, "opcode", F_ALU, "opcode");
        bind(DECODER, "funct5", F_ALU, "funct5");
        bind(DECODER, "fmt", F_ALU, "fmt");
        bind(DECODER, "rm", F_ALU, "rm");
        bind(DECODER, "src2", F_ALU, "src2");
        bind(DECODER, "imm", PC_UPDATE, "imm");
        bind(DECODER, "invalidIns", STATE, "iiDecode");

        opcode = (int)modules[0].findOutputByName("opcode").value;
        funct7 = (int)modules[0].findOutputByName("funct7").value;
        funct6 = (int)modules[0].findOutputByName("funct6").value;
        funct5 = (int)modules[0].findOutputByName("funct5").value;
        funct3 = (int)modules[0].findOutputByName("funct3").value;
    }

    void geneRegRead() {
        /* general register */
        int regLength = 8;
        if (opcode == 0b0100011)        // STOREs
            regLength = (1 << funct3);
        modules[1].findInputByName("gregRead").value = 1;
        modules[1].findInputByName("gregWrite").value = 0;
        modules[1].findInputByName("regData").value = 0;
        modules[1].findInputByName("regLength").value = (long)regLength;

        modules[1].parse();

        bind(GENE_REG, "val1", I_ALUMUX, "val1");
        bind(GENE_REG, "val2", I_ALUMUX, "val2");
        bind(GENE_REG, "val1", F_ALU, "val1");
    }

    void floatRegRead() {
        /* float register */
        modules[2].findInputByName("fregRead").value = 1;
        modules[2].findInputByName("fregWrite").value = 0;
        modules[2].findInputByName("regData").value = 0;

        modules[2].parse();

        bind(FLOAT_REG, "fval1", F_ALU, "fval1");
        bind(FLOAT_REG, "fval2", F_ALU, "fval2");
        bind(FLOAT_REG, "fval3", F_ALU, "fval3");
    }

    void intALUMux() {
        /* integer ALU MUX */
        //modules[3].findInputByName("pc").value = pcAddress;

        modules[3].parse();

        bind(I_ALUMUX, "aluA", I_ALU, "aluA");
        bind(I_ALUMUX, "aluB", I_ALU, "aluB");
        bind(I_ALUMUX, "aluOp", I_ALU, "aluOp");
    }

    void intALU() {
        /* integer ALU */
        int aluLength = 8;
        int aluHigh = 0;
        int aluIsUnsigned = 0;

        modules[4].findInputByName("aluLength").value = (long)aluLength;
        modules[4].findInputByName("aluHigh").value = (long)aluHigh;
        modules[4].findInputByName("aluIsUnsigned").value = (long)aluIsUnsigned;

        modules[4].parse();
    }

    void floatALU() {
        /* float ALU */
        modules[5].parse();

        bind(F_ALU, "meetNAN", STATE, "meetNAN");
        bind(F_ALU, "invalidIns", STATE, "iiFALU");
    }

    void memory() {
        /* memory manage unit */
        int memLength = 8;
        long memAddr = 0;
        long memData = 0;
        int memRead = 0;
        int memWrite = 0;
        int memIsUnsigned = 0;

        modules[6].parse();

        bind(MMU, "invalidAddress", STATE, "iaMMU");
    }

    void pcUpdate() {
        /* PC update */
        int cnd = 0;

        modules[7].parse();
    }

    void machineState() {
        /* machine state */
        long halt = 0;
        // if PC overflow, then halt = 1
        
        modules[8].findOutputByName("halt").value = halt;

        modules[8].parse();
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

    public void doParse()
    {
        long pcAddress = machine.programCounter;
        // pcAddress to insIndex

        decode();

        if (opcode == 0b1010011 && (
                funct5 == 0b11000 ||        //FCVT.int.S
                funct5 == 0b11110))         //FMV.S.X
            geneRegRead();
        else if (opcode != 0b0010111 &&     //AUIPC
                 opcode != 0b0110111 &&     //LUI
                 opcode != 0b1101111)       //JAL
            geneRegRead();

        if (opcode == 0b1000011 ||          //FMADD.S
            opcode == 0b1000111 ||          //FMSUB.S
            opcode == 0b1001011 ||          //FNMSUB.S
            opcode == 0b1001111 ||          //FNMADD.S
            (opcode == 0b1010011 && (       // other F
                funct5 != 0b11000 &&        //!FCVT.int.S
                funct5 != 0b11110)))        //!FMV.S.X
            floatRegRead();

        //if (opcode == )
        intALUMux();

        intALU();

        floatALU();

        memory();

        pcUpdate();

        machineState();
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
                syscalls[i] = (Syscall)(Util.configManager.getConfig(
                    "DefMachineController.syscall" + i));
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
