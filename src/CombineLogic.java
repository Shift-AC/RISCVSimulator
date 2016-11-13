package com.github.ShiftAC.RISCVSimulator;

class Signal
{
    String name;
    Signal bind = null;
    long value;

    public Signal(String name, long value)
    {
        this.name = name;
        this.value = value;
    }
}

public abstract class CombineLogic
{
    Signal[] input = null;
    Signal[] output = null;

    public CombineLogic() {}

    abstract public void parse();

    abstract protected void initSignals();

    abstract public void reset();

    public void setInput(String name, long value)
    {
        Signal signal = findInputByName(name);

        if (signal != null)
        {
            signal.value = value;
        }
    }

    public Long getOutput(String name)
    {
        parse();
        
        Signal signal = findOutputByName(name);

        if (signal != null)
        {
            return new Long(signal.value);
        }
        return null;
    }

    protected Signal findSignalByName(String name, Signal[] arr)
    {
        for (Signal signal : arr)
        {
            if (signal.name.equals(name))
            {
                if (signal.bind != null)
                {
                    return signal.bind;
                }
                return signal;
            }
        }
        return null;
    } 

    protected Signal findInputByName(String name)
    {
        return findSignalByName(name, input);
    }

    protected Signal findOutputByName(String name)
    {
        return findSignalByName(name, output);
    }
}


class MemoryManageUnit extends CombineLogic
{
    MemorySegment[] memory;
    public MemoryManageUnit(RISCVMachine machine)
    {
        super();

        this.memory = machine.memory;
    }

    private boolean isEffectiveAddress(MemorySegment segment, long address)
    {
        return 
            (address >= segment.startAddress) && 
            (address < segment.endAddress);
    }

    boolean saveByte(long address, byte data)
    {
        for (MemorySegment segment : memory)
        {
            if (isEffectiveAddress(segment, address))
            {
                segment.memory[(int)(address - segment.startAddress)] = data;
                return true;
            }
        }
        return false;
    }

    boolean saveShort(long address, short data)
    {
        byte[] ba;
        for (MemorySegment segment : memory)
        {
            if (isEffectiveAddress(segment, address))
            {
                ba = Util.short2byteArray(data);
                System.arraycopy(segment.memory, (int)(address - segment.startAddress),
                                 ba, 0, 2);
                return true;
            }
        }
        return false;
    }

    boolean saveInt(long address, int data)
    {
        byte[] ba;
        for (MemorySegment segment : memory)
        {
            if (isEffectiveAddress(segment, address))
            {
                ba = Util.int2byteArray(data);
                System.arraycopy(segment.memory, (int)(address - segment.startAddress),
                                 ba, 0, 4);
                return true;
            }
        }
        return false;
    }

    boolean saveLong(long address, long data)
    {
        byte[] ba;
        for (MemorySegment segment : memory)
        {
            if (isEffectiveAddress(segment, address))
            {
                ba = Util.long2byteArray(data);
                System.arraycopy(segment.memory, (int)(address - segment.startAddress),
                                 ba, 0, 8);
                return true;
            }
        }
        return false;
    }

    Long loadByte(long address, boolean isUnsigned)
    {
        byte data;
        for (MemorySegment segment : memory)
        {
            if (isEffectiveAddress(segment, address))
            {
                data = segment.memory[(int)(address - segment.startAddress)];
                if (isUnsigned)
                    return new Long(data & 0x0FF);
                else
                    return new Long(data);
            }
        }
        return null;
    }

    Long loadShort(long address, boolean isUnsigned)
    {
        short data;
        for (MemorySegment segment : memory)
        {
            if (isEffectiveAddress(segment, address))
            {
                data = Util.byteArray2Short(segment.memory, (int)(address - segment.startAddress));
                if (isUnsigned)
                    return new Long(data & 0x0FFFF);
                else
                    return new Long(data);
            }
        }
        return null;
    }

    Long loadInt(long address, boolean isUnsigned)
    {
        int data;
        for (MemorySegment segment : memory)
        {
            if (isEffectiveAddress(segment, address))
            {
                data = Util.byteArray2Int(segment.memory, (int)(address - segment.startAddress));
                if (isUnsigned)
                    return new Long(data & 0x0FFFFFFFF);
                else
                    return new Long(data);
            }
        }
        return null;
    }

    Long loadLong(long address, boolean isUnsigned)
    {
        long data;
        for (MemorySegment segment : memory)
        {
            if (isEffectiveAddress(segment, address))
            {
                data = Util.byteArray2Long(segment.memory, (int)(address - segment.startAddress));
                return new Long(data);
            }
        }
        return null;
    }

    public void parse()
    {
        long address = findInputByName("memAddr").value;
        long data = findInputByName("memData").value;
        boolean isUnsigned = findInputByName("memIsUnsigned").value == 1;
        boolean write = findInputByName("memWrite").value == 1;
        boolean read = findInputByName("memRead").value == 1;
        int length = (int)findInputByName("memLength").value;

        // READ PROCESS
        Long outData = null;
        if (read)
        {
            if (length == 1)
                outData = loadByte(address, isUnsigned);
            else if (length == 2)
                outData = loadShort(address, isUnsigned);
            else if (length == 4)
                outData = loadInt(address, isUnsigned);
            else if (length == 8)
                outData = loadLong(address, isUnsigned);
        }
        // output signals
        if (outData != null) {
            findOutputByName("valM").value = outData.longValue();
            findOutputByName("invalidAddress").value = 0;
        }
        else {
            System.err.printf("Memory Error: invalid address %016x\n", address);
            findOutputByName("invalidAddress").value = 1;
        }

        // WRITE PROCESS
        boolean success = false;
        if (write) {
            if (length == 1)
                success = saveByte(address, (byte)data);
            else if (length == 2)
                success = saveShort(address, (short)data);
            else if (length == 4)
                success = saveInt(address, (int)data);
            else if (length == 8)
                success = saveLong(address, data);
        }
        if (success) {
            findOutputByName("invalidAddress").value = 0;
        }
        else {
            System.err.printf("Memory Error: invalid address %016x\n", address);
            findOutputByName("invalidAddress").value = 1;
        }
    }

    public void reset()
    {
        for (int i = 0; i < input.length; ++i)
        {
            input[i].value = 0;
        }
        for (int i = 0; i < output.length; ++i)
        {
            output[i].value = 0;
        }
    }

    public void initSignals()
    {
        input = new Signal[6];
        input[0] = new Signal("memAddr", 0);
        input[1] = new Signal("memData", 0);
        input[2] = new Signal("memWrite", 0);
        input[3] = new Signal("memRead", 0);
        input[4] = new Signal("memLength", 0);
        input[5] = new Signal("memIsUnsigned", 0);

        output = new Signal[2];
        output[0] = new Signal("valM", 0);
        output[1] = new Signal("invalidAddress", 0);
    }
}

// TODO
class Decoder extends CombineLogic
{
    RISCVInstruction[] instructions;
    public Decoder(RISCVMachine machine) {
        super();

        this.instructions = machine.instructions;
    }

    void parseR(RISCVInstruction currentIns) {
        RInstruction ins = (RInstruction)currentIns;
        findOutputByName("funct7").value = (long)ins.funct7();
        findOutputByName("funct3").value = (long)ins.funct3();
        findOutputByName("dst").value = (long)ins.rd();
        findOutputByName("src1").value = (long)ins.rs1();
        findOutputByName("src2").value = (long)ins.rs2();
    }

    void parseS5(RISCVInstruction currentIns) {
        S5Instruction ins = (S5Instruction)currentIns;
        findOutputByName("funct7").value = (long)ins.funct7();
        findOutputByName("funct3").value = (long)ins.funct3();
        findOutputByName("dst").value = (long)ins.rd();
        findOutputByName("src1").value = (long)ins.rs1();
        findOutputByName("shamt").value = (long)ins.shamt();
    }

    void parseS6(RISCVInstruction currentIns) {
        S6Instruction ins = (S6Instruction)currentIns;
        findOutputByName("funct6").value = (long)ins.funct6();
        findOutputByName("funct3").value = (long)ins.funct3();
        findOutputByName("dst").value = (long)ins.rd();
        findOutputByName("src1").value = (long)ins.rs1();
        findOutputByName("shamt").value = (long)ins.shamt();
    }

    void parseR4(RISCVInstruction currentIns) {
        R4Instruction ins = (R4Instruction)currentIns;
        findOutputByName("funct7").value = (long)ins.funct7();
        findOutputByName("funct3").value = (long)ins.funct3();
        findOutputByName("funct2").value = (long)ins.funct2();
        findOutputByName("dst").value = (long)ins.rd();
        findOutputByName("src1").value = (long)ins.rs1();
        findOutputByName("src2").value = (long)ins.rs2();
        findOutputByName("src3").value = (long)ins.rs3();
    }

    void parseI(RISCVInstruction currentIns) {
        IInstruction ins = (IInstruction)currentIns;
        findOutputByName("funct3").value = (long)ins.funct3();
        findOutputByName("dst").value = (long)ins.rd();
        findOutputByName("src1").value = (long)ins.rs1();
        findOutputByName("imm").value = (long)ins.imm();
    }

    void parseS(RISCVInstruction currentIns) {
        SInstruction ins = (SInstruction)currentIns;
        findOutputByName("funct3").value = (long)ins.funct3();
        findOutputByName("src1").value = (long)ins.rs1();
        findOutputByName("src2").value = (long)ins.rs2();
        findOutputByName("imm").value = (long)ins.imm();
    }

    void parseSB(RISCVInstruction currentIns) {
        SBInstruction ins = (SBInstruction)currentIns;
        findOutputByName("funct3").value = (long)ins.funct3();
        findOutputByName("src1").value = (long)ins.rs1();
        findOutputByName("src2").value = (long)ins.rs2();
        findOutputByName("imm").value = (long)ins.imm();
    }

    void parseU(RISCVInstruction currentIns) {
        UInstruction ins = (UInstruction)currentIns;
        findOutputByName("imm").value = ins.imm() & 0x0FFFFFFFF;
        findOutputByName("dst").value = (long)ins.rd();
    }

    void parseUJ(RISCVInstruction currentIns) {
        UJInstruction ins = (UJInstruction)currentIns;
        findOutputByName("imm").value = ins.imm() & 0x0FFFFFFFF;
        findOutputByName("dst").value = (long)ins.rd();
    }

    void parseFZ(RISCVInstruction currentIns) {
        FZInstruction ins = (FZInstruction)currentIns;
        findOutputByName("funct5").value = (long)ins.funct5();
        findOutputByName("dst").value = (long)ins.rd();
        findOutputByName("src1").value = (long)ins.rs1();
        findOutputByName("src2").value = (long)ins.rs2();
        findOutputByName("fmt").value = (long)ins.fmt();
        findOutputByName("rm").value = (long)ins.rm();
    }

   public void parse() {
        int index = (int)findInputByName("insIndex").value;
        RISCVInstruction currentIns = instructions[index];

        findOutputByName("opcode").value = (long)currentIns.opcode();
        if (currentIns instanceof RInstruction)
            parseR(currentIns);
        else if (currentIns instanceof S5Instruction)
            parseS5(currentIns);
        else if (currentIns instanceof S6Instruction)
            parseS6(currentIns);
        else if (currentIns instanceof R4Instruction)
            parseR4(currentIns);
        else if (currentIns instanceof IInstruction)
            parseI(currentIns);
        else if (currentIns instanceof SInstruction)
            parseS(currentIns);
        else if (currentIns instanceof SBInstruction)
            parseSB(currentIns);
        else if (currentIns instanceof UInstruction)
            parseU(currentIns);
        else if (currentIns instanceof UJInstruction)
            parseUJ(currentIns);
        else if (currentIns instanceof FZInstruction)
            parseFZ(currentIns);
        else
            findOutputByName("invalidIns").value = 1;
    }

    public void reset()
    {
        for (int i = 0; i < input.length; ++i)
        {
            input[i].value = 0;
        }
        for (int i = 0; i < output.length; ++i)
        {
            output[i].value = 0;
        }
    }

    public void initSignals()
    {
        input = new Signal[1];
        input[0] = new Signal("opcode", 0);

        output = new Signal[14];
        output[0] = new Signal("funct7", 0);
        output[1] = new Signal("funct6", 0);
        output[2] = new Signal("funct5", 0);
        output[3] = new Signal("funct3", 0);
        output[4] = new Signal("funct2", 0);
        output[5] = new Signal("dst", 0);
        output[6] = new Signal("src1", 0);
        output[7] = new Signal("src2", 0);
        output[8] = new Signal("src3", 0);
        output[9] = new Signal("imm", 0);
        output[10] = new Signal("shamt", 0);
        output[11] = new Signal("invalidIns", 1);
        output[12] = new Signal("fmt", 0);
        output[13] = new Signal("rm", 0);
    }
}

class GeneralRegisterFile extends CombineLogic
{
    long[] generalRegister;
    public GeneralRegisterFile(RISCVMachine machine) {
        super();

        this.generalRegister = machine.generalRegister;
    }

    boolean writeLong(int regNdx, long data) {
        generalRegister[regNdx] = data;
        return true;
    }

    Byte readByte(int regNdx) {
        return new Byte((byte)(generalRegister[regNdx] & 0xFF));
    }

    Short readShort(int regNdx) {
        return new Short((short)(generalRegister[regNdx] & 0xFFFF));
    }

    Integer readInt(int regNdx) {
        return new Integer((int)(generalRegister[regNdx] & 0xFFFFFFFF));
    }

    Long readLong(int regNdx) {
        return new Long(generalRegister[regNdx]);
    }

    public void parse() {
        int rs1 = (int)findInputByName("src1").value;
        int rs2 = (int)findInputByName("src2").value;
        int rs3 = (int)findInputByName("src3").value;
        int rd = (int)findInputByName("dst").value;
        boolean read = findInputByName("gregRead").value == 1;
        boolean write = findInputByName("gregWrite").value == 1;
        long data = findInputByName("regData").value;
        //boolean isUnsigned = findInputByName("isUnsigned").value == 1;
        int length = (int)findInputByName("regLength").value;

        if (read) {
            if (length == 1) {
                findOutputByName("val1").value = readByte(rs1).longValue();
                findOutputByName("val2").value = readByte(rs2).longValue();
                findOutputByName("val3").value = readByte(rs3).longValue();
            }
            else if (length == 2) {
                findOutputByName("val1").value = readShort(rs1).longValue();
                findOutputByName("val2").value = readShort(rs2).longValue();
                findOutputByName("val3").value = readShort(rs3).longValue();
            }
            else if(length == 4) {
                findOutputByName("val1").value = readInt(rs1).longValue();
                findOutputByName("val2").value = readInt(rs2).longValue();
                findOutputByName("val3").value = readInt(rs3).longValue();
            }
            else if(length == 8) {
                findOutputByName("val1").value = readLong(rs1).longValue();
                findOutputByName("val2").value = readLong(rs2).longValue();
                findOutputByName("val3").value = readLong(rs3).longValue();
            }
        }
        if (write) {
            writeLong(rd, data);
        }
    }

    public void reset()
    {
        for (int i = 0; i < input.length; ++i)
        {
            input[i].value = 0;
        }
        for (int i = 0; i < output.length; ++i)
        {
            output[i].value = 0;
        }
    }

    public void initSignals()
    {
        input = new Signal[8];
        input[0] = new Signal("src1", 0);
        input[1] = new Signal("src2", 0);
        input[2] = new Signal("src3", 0);
        input[3] = new Signal("dst", 0);
        input[4] = new Signal("gregRead", 0);
        input[5] = new Signal("gregWrite", 0);
        input[6] = new Signal("regData", 0);
        input[7] = new Signal("regLength", 0);

        output = new Signal[3];
        output[0] = new Signal("val1", 0);
        output[1] = new Signal("val2", 0);
        output[2] = new Signal("val3", 0);
    }
}

class FloatRegisterFile extends CombineLogic
{
    int[] floatRegister;

    public FloatRegisterFile(RISCVMachine machine) {
        super();

        this.floatRegister = machine.floatRegister;
    }

    public void parse() {
        int rs1 = (int)findInputByName("src1").value;
        int rs2 = (int)findInputByName("src2").value;
        int rd = (int)findInputByName("dst").value;
        boolean read = findInputByName("fregRead").value == 1;
        boolean write = findInputByName("fregWrite").value == 1;
        int data = (int)findInputByName("regData").value;

        if (read) {
            findOutputByName("fval1").value = (long)floatRegister[rs1];
            findOutputByName("fval2").value = (long)floatRegister[rs2];
        }
        if (write) {
            floatRegister[rd] = data;
        }
    }

    public void reset()
    {
        for (int i = 0; i < input.length; ++i)
        {
            input[i].value = 0;
        }
        for (int i = 0; i < output.length; ++i)
        {
            output[i].value = 0;
        }
    }

    public void initSignals()
    {
        input = new Signal[6];
        input[0] = new Signal("src1", 0);
        input[1] = new Signal("src2", 0);
        input[2] = new Signal("dst", 0);
        input[3] = new Signal("fregRead", 0);
        input[4] = new Signal("fregWrite", 0);
        input[5] = new Signal("regData", 0);

        output = new Signal[2];
        output[0] = new Signal("fval1", 0);
        output[1] = new Signal("fval2", 0);
    }
}

class IntegerALUMux extends CombineLogic
{
    public IntegerALUMux(RISCVMachine machine) {
        super();
    }

    boolean isSub(int funct7, int funct3, int opcode) {
        return ((opcode == 0b0110011 || opcode == 0b0111011) &&
                funct3 == 0b000 && funct7 == 0b0100000) ||
            (opcode == 0b1100011 && (
                funct3 == 0b000 || 
                funct3 == 0b001 ||
                funct3 == 0b100 ||
                funct3 == 0b101)) ||
            ((opcode == 0b0010011 || opcode == 0b0110011) &&
                funct3 == 0b010);
    }

    boolean isSubU(int funct7, int funct3, int opcode) {
        return (opcode == 0b1100011 && (
                funct3 == 0b110 ||
                funct3 == 0b111)) ||
            ((opcode == 0b0010011 || opcode == 0b0110011) &&
                funct3 == 0b011);
    }

    boolean isAnd(int funct7, int funct3, int opcode) {
        return ((opcode == 0b0010011 || opcode == 0b0110011) &&
                funct3 == 0b111);
    }

    boolean isOr(int funct7, int funct3, int opcode) {
        return ((opcode == 0b0010011 || opcode == 0b0110011) &&
                funct3 == 0b110);
    }

    boolean isXor(int funct7, int funct3, int opcode) {
        return ((opcode == 0b0010011 || opcode == 0b0110011) &&
                funct3 == 0b100);
    }

    boolean isSll(int funct7, int funct3, int opcode) {
        return ((opcode == 0b0010011 || opcode == 0b0110011 ||
                    opcode == 0b0011011 || opcode == 0b0111011) &&
                funct3 == 0b001);
    }

    boolean isSrl(int funct7, int funct3, int opcode) {
        return ((opcode == 0b0010011 || opcode == 0b0110011 ||
                    opcode == 0b0011011 || opcode == 0b0111011) &&
                funct3 == 0b101 && funct7 == 0b0000000);
    }

    boolean isSra(int funct7, int funct3, int opcode) {
        return ((opcode == 0b0010011 || opcode == 0b0110011 ||
                    opcode == 0b0011011 || opcode == 0b0111011) &&
                funct3 == 0b101 && funct7 == 0b0100000);
    }

    boolean isMul(int funct7, int funct3, int opcode) {
        return (opcode == 0b0110011 && (
                funct3 == 0b000 || funct3 == 0b001)) ||
            (opcode == 0111011 &&
                funct3 == 0b000);
    }

    boolean isMulU(int funct7, int funct3, int opcode) {
        return (opcode == 0b0110011 && (
                funct3 == 0b010 || funct3 == 0b011));
    }

    boolean isDiv(int funct7, int funct3, int opcode) {
        return ((opcode == 0b0110011 || opcode == 0b0111011) &&
                funct3 == 0b100);
    }

    boolean isDivU(int funct7, int funct3, int opcode) {
        return ((opcode == 0b0110011 || opcode == 0b0111011) &&
                funct3 == 0b101);
    }

    boolean isRem(int funct7, int funct3, int opcode) {
        return ((opcode == 0b0110011 || opcode == 0b0111011) &&
                funct3 == 0b110);
    }

    boolean isRemU(int funct7, int funct3, int opcode) {
        return ((opcode == 0b0110011 || opcode == 0b0111011) &&
                funct3 == 0b111);
    }


    public void parse() {
        long val1 = findInputByName("val1").value;
        long val2 = findInputByName("val2").value;
        long imm = findInputByName("imm").value;
        int opcode = (int)findInputByName("opcode").value;
        int funct7 = (int)findInputByName("funct7").value;
        int funct6 = (int)findInputByName("funct6").value;
        int funct3 = (int)findInputByName("funct3").value;
        long pc = findInputByName("pc").value;
        int shamt = (int)findInputByName("shamt").value;

        // ALUOP
        int aluOp;
        if (isSub(funct7, funct3, opcode))
            aluOp = IntegerALU.ALU_SUB;
        else if (isSubU(funct7, funct3, opcode))
            aluOp = IntegerALU.ALU_SUB_U;
        else if (isAnd(funct7, funct3, opcode))
            aluOp = IntegerALU.ALU_AND;
        else if (isOr(funct7, funct3, opcode))
            aluOp = IntegerALU.ALU_OR;
        else if (isXor(funct7, funct3, opcode))
            aluOp = IntegerALU.ALU_XOR;
        else if (isSll(funct7, funct3, opcode))
            aluOp = IntegerALU.ALU_SLL;
        else if (isSrl(funct7, funct3, opcode))
            aluOp = IntegerALU.ALU_SRL;
        else if (isSra(funct7, funct3, opcode))
            aluOp = IntegerALU.ALU_SRA;
        else if (isMul(funct7, funct3, opcode))
            aluOp = IntegerALU.ALU_MUL;
        else if (isMulU(funct7, funct3, opcode))
            aluOp = IntegerALU.ALU_MUL_U;
        else if (isDiv(funct7, funct3, opcode))
            aluOp = IntegerALU.ALU_DIV;
        else if (isDivU(funct7, funct3, opcode))
            aluOp = IntegerALU.ALU_DIV_U;
        else if (isRem(funct7, funct3, opcode))
            aluOp = IntegerALU.ALU_REM;
        else if (isRemU(funct7, funct3, opcode))
            aluOp = IntegerALU.ALU_REM_U;
        else
            aluOp = IntegerALU.ALU_ADD;
        findOutputByName("aluOp").value = (long)aluOp;

        // ALUA
        long aluA;
        if (opcode == 0b0010111)    //AUIPC
            aluA = pc;
        else if (opcode == 0b0111011)      // (MUL/DIV/REM)W
            aluA = val1 & 0xFFFFFFFF;
        else
            aluA = val1;
        findOutputByName("aluA").value = aluA;

        // ALUB: THE FOLLOWING CODE IS NOT EXCLUSIVE!
        long aluB;
        if (ELFReader.isIInstruction(opcode, funct3) ||
            ELFReader.isSInstruction(opcode))
             aluB = imm;                 //AUIPC
        else if (aluOp == IntegerALU.ALU_SLL ||
                aluOp == IntegerALU.ALU_SRL ||
                aluOp == IntegerALU.ALU_SRA)
            aluB = shamt;               // Shift
        else if (opcode == 0b0011011  || opcode == 0b0111011 || opcode == 0b0111011)
            aluB = val2 & 0xFFFFFFFF;   // (MUL/DIV/REM)W
        else
            aluB = val2;
        findOutputByName("aluB").value = aluB;
    }

    public void reset()
    {
        for (int i = 0; i < input.length; ++i)
        {
            input[i].value = 0;
        }
        for (int i = 0; i < output.length; ++i)
        {
            output[i].value = 0;
        }
    }

    public void initSignals()
    {
        input = new Signal[9];
        input[0] = new Signal("val1", 0);
        input[1] = new Signal("val2", 0);
        input[2] = new Signal("imm", 0);
        input[3] = new Signal("opcode", 0);
        input[4] = new Signal("funct7", 0);
        input[5] = new Signal("funct6", 0);
        input[6] = new Signal("funct3", 0);
        input[7] = new Signal("pc", 0);
        input[8] = new Signal("shamt", 0);

        output = new Signal[3];
        output[0] = new Signal("aluA", 0);
        output[1] = new Signal("aluB", 0);
        output[2] = new Signal("aluOp", 0);
    }
}

class IntegerALU extends CombineLogic
{
    static final int
        ALU_ADD = 0,
        ALU_SUB = 1,
        ALU_SUB_U = 11,
        ALU_MUL = 2,
        ALU_MUL_U = 21,
        ALU_DIV = 3,
        ALU_DIV_U = 31,
        ALU_REM = 4,
        ALU_REM_U = 41,
        ALU_AND = 5,
        ALU_OR = 6,
        ALU_XOR = 7,
        ALU_SLL = 100,
        ALU_SRL = 101,
        ALU_SRA = 102;



    public IntegerALU(RISCVMachine machine) {
        super();
    }

    public void parse() {
        long val1 = findInputByName("aluA").value;
        long val2 = findInputByName("aluB").value;
        int shamt = (int)findInputByName("aluShamt").value;
        int operator = (int)findInputByName("aluOp").value;
        int length = (int)findInputByName("aluLength").value;
        boolean isUnsigned = findInputByName("aluIsUnsigned").value == 1;

        // NORMAL OPERATIONS
        long res;
        switch (operator){
        case ALU_ADD: res = val1 + val2;
                break;
        case ALU_SUB: res = val1 - val2; // ???
                break;
        //case 2: res = val1 * val2;  // deal with multiple&divide independently
        //        break;
        //case 3: res = val1 / val2;
        //        break;
        //case 4: res = val1 % val2;
        //        break;
        case 5: res = val1 & val2;
                break;
        case 6: res = val1 | val2;
                break;
        case 7: res = val1 ^ val2;
                break;
        case 20: res = val1 << shamt;
                break;
        case 21: res = val1 >> shamt;
                break;
        case 22: res = val1 >>> shamt;
                break;
        }
        if (length == 4) {
            //if (isUnsigned)
        }

        // MULTIPLE
        if (operator == 3) {
            if (length == 4) {
                val1 &= 0xFFFFFFFF;
                val2 &= 0xFFFFFFFF;
                res = val1 * val2;
            }
            else if (length == 8) {
                long val1l = val1 &= 0xFFFFFFFF;
                long val1h = val1 >>> 32;
                long val2l = val2 &= 0xFFFFFFFF;
                long val2h = val2 >>> 32;
            }
        }
    }

    public void reset()
    {
        for (int i = 0; i < input.length; ++i)
        {
            input[i].value = 0;
        }
        for (int i = 0; i < output.length; ++i)
        {
            output[i].value = 0;
        }
    }

    public void initSignals()
    {
        input = new Signal[6];
        input[0] = new Signal("memAddr", 0);
        input[1] = new Signal("memData", 0);
        input[2] = new Signal("memWrite", 0);
        input[3] = new Signal("memRead", 0);
        input[4] = new Signal("memLength", 0);
        input[5] = new Signal("memIsUnsigned", 0);

        output = new Signal[2];
        output[0] = new Signal("valM", 0);
        output[1] = new Signal("invalidAddress", 0);
    }
}

class FloatALU extends CombineLogic
{
    public FloatALU(RISCVMachine machine) {
        super();
    }

    public void parse() {

    }

    public void reset()
    {
        for (int i = 0; i < input.length; ++i)
        {
            input[i].value = 0;
        }
        for (int i = 0; i < output.length; ++i)
        {
            output[i].value = 0;
        }
    }

    public void initSignals()
    {
        input = new Signal[6];
        input[0] = new Signal("memAddr", 0);
        input[1] = new Signal("memData", 0);
        input[2] = new Signal("memWrite", 0);
        input[3] = new Signal("memRead", 0);
        input[4] = new Signal("memLength", 0);
        input[5] = new Signal("memIsUnsigned", 0);

        output = new Signal[2];
        output[0] = new Signal("valM", 0);
        output[1] = new Signal("invalidAddress", 0);
    }
}

class PCUpdate extends CombineLogic
{
    public PCUpdate(RISCVMachine machine) {
        super();
    }

    public void parse() {

    }

    public void reset()
    {
        for (int i = 0; i < input.length; ++i)
        {
            input[i].value = 0;
        }
        for (int i = 0; i < output.length; ++i)
        {
            output[i].value = 0;
        }
    }

    public void initSignals()
    {
        input = new Signal[6];
        input[0] = new Signal("memAddr", 0);
        input[1] = new Signal("memData", 0);
        input[2] = new Signal("memWrite", 0);
        input[3] = new Signal("memRead", 0);
        input[4] = new Signal("memLength", 0);
        input[5] = new Signal("memIsUnsigned", 0);

        output = new Signal[2];
        output[0] = new Signal("valM", 0);
        output[1] = new Signal("invalidAddress", 0);
    }
}

class MachineState extends CombineLogic
{
    public MachineState(RISCVMachine machine) {

    }

    public void parse() {

    }

    public void reset()
    {
        for (int i = 0; i < input.length; ++i)
        {
            input[i].value = 0;
        }
        for (int i = 0; i < output.length; ++i)
        {
            output[i].value = 0;
        }
    }

    public void initSignals()
    {
        input = new Signal[6];
        input[0] = new Signal("memAddr", 0);
        input[1] = new Signal("memData", 0);
        input[2] = new Signal("memWrite", 0);
        input[3] = new Signal("memRead", 0);
        input[4] = new Signal("memLength", 0);
        input[5] = new Signal("memIsUnsigned", 0);

        output = new Signal[2];
        output[0] = new Signal("valM", 0);
        output[1] = new Signal("invalidAddress", 0);
    }
}