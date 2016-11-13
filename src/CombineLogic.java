package com.github.ShiftAC.RISCVSimulator;

import java.math.*;
import java.nio.*;

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

    public CombineLogic() 
    {
        //initSignals();
    }

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

    static boolean isEffectiveAddress(MemorySegment segment, long address)
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

    void parseFJ(RISCVInstruction currentIns) {
        FJInstruction ins = (FJInstruction)currentIns;
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
        else if (currentIns instanceof FJInstruction)
            parseFJ(currentIns);
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
        input[0] = new Signal("insIndex", 0);

        output = new Signal[14];
        output[0] = new Signal("opcode", 0);
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
        output[11] = new Signal("invalidIns", 0);
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
            }
            else if (length == 2) {
                findOutputByName("val1").value = readShort(rs1).longValue();
                findOutputByName("val2").value = readShort(rs2).longValue();
            }
            else if(length == 4) {
                findOutputByName("val1").value = readInt(rs1).longValue();
                findOutputByName("val2").value = readInt(rs2).longValue();
            }
            else if(length == 8) {
                findOutputByName("val1").value = readLong(rs1).longValue();
                findOutputByName("val2").value = readLong(rs2).longValue();
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
        input = new Signal[7];
        input[0] = new Signal("src1", 0);
        input[1] = new Signal("src2", 0);
        input[2] = new Signal("dst", 0);
        input[3] = new Signal("gregRead", 0);
        input[4] = new Signal("gregWrite", 0);
        input[5] = new Signal("regData", 0);
        input[6] = new Signal("regLength", 0);

        output = new Signal[2];
        output[0] = new Signal("val1", 0);
        output[1] = new Signal("val2", 0);
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
        int rs3 = (int)findInputByName("src3").value;
        int rd = (int)findInputByName("dst").value;
        boolean read = findInputByName("fregRead").value == 1;
        boolean write = findInputByName("fregWrite").value == 1;
        int data = (int)findInputByName("regData").value;

        if (read) {
            findOutputByName("fval1").value = (long)floatRegister[rs1];
            findOutputByName("fval2").value = (long)floatRegister[rs2];
            findOutputByName("fval3").value = (long)floatRegister[rs3];
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
        input = new Signal[7];
        input[0] = new Signal("src1", 0);
        input[1] = new Signal("src2", 0);
        input[2] = new Signal("src3", 0);
        input[3] = new Signal("dst", 0);
        input[4] = new Signal("fregRead", 0);
        input[5] = new Signal("fregWrite", 0);
        input[6] = new Signal("regData", 0);

        output = new Signal[3];
        output[0] = new Signal("fval1", 0);
        output[1] = new Signal("fval2", 0);
        output[2] = new Signal("fval3", 0);
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
            ELFReader.isSInstruction(opcode) ||  //Store
            ELFReader.isUJInstruction(opcode))   //AUIPC
             aluB = imm;
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

    // conditions
    boolean less = false;
    boolean equal = false;
    boolean greater = false;


    public IntegerALU(RISCVMachine machine) {
        super();
    }

    private byte[] long2byteArray(long num, boolean isUnsigned) {
        int length = isUnsigned ? 9 : 8;
        byte[] ba = new byte[length];
        for (int i = length-1; i >= length-8; --i) {
            ba[i] = (byte)(num & 0xFF);
            num >>= 8;
        }
        return ba;
    }
    private long byteArray2Long(byte[] ba, int offset) {
        long res = 0;
        for (int i = offset; i < 8+offset; ++i) {
            res = (res<<8) + (ba[i] & 0x0FF);
        }
        return res;
    }

    // signed word to long
    private long SW2Long(long in) {
        in &= 0xFFFFFFFF;
        long mask = ((1 << 32) - 1) << 32;
        return (in >> 31) != 0 ? mask : 0;
    }

    long longAdd(long val1, long val2, int length) {
        long res;
        if (length == 4) {
            val1 &= 0xFFFFFFFF;
            val2 &= 0xFFFFFFFF;
            res = SW2Long(val1 + val2);
        }
        else {
            res = val1 + val2;
        }
        return res;
    }

    long longSub(long val1, long val2, boolean isUnsigned, int length) {
        long res;
        if (length == 4) {
            if (isUnsigned) {
                val1 &= 0xFFFFFFFF;
                val2 &= 0xFFFFFFFF;
                res = (val1 - val2) & 0xFFFFFFFF;
            }
            else {
                val1 = SW2Long(val1);
                val2 = SW2Long(val2);
                res = SW2Long(val1 - val2);
        }
        }
        else {
            res = val1 - val2;
            // set CC only when 64-bit
            if (isUnsigned) {
                less = ((val1 >>> 32) < (val2 >>> 32)) ||
                      (((val1 >>> 32) == (val2 >>> 32)) &&
                            ((val1 & 0xFFFFFFFF) < (val2 & 0xFFFFFFFF)));
                equal = (val1 == val2);
                greater = ((val1 >>> 32) > (val2 >>> 32)) ||
                      (((val1 >>> 32) == (val2 >>> 32)) &&
                            ((val1 & 0xFFFFFFFF) > (val2 & 0xFFFFFFFF)));
            }
            else {
                less = (val1 < val2);
                equal = (val1 == val2);
                greater = (val1 > val2);
            }
        }

        return res;
    }


    long longMult(long val1, long val2,
                        boolean isUnsigned, boolean isHigh, int length) {
        // truncate and then extend
        if (length == 4) {
            val1 &= 0xFFFFFFFF;
            val2 &= 0xFFFFFFFF;
            if (isUnsigned) {
                long mask = ((1 << 32) - 1) << 32;
                val1 |= (val1 >> 31) != 0 ? mask : 0;
                val2 |= (val2 >> 31) != 0 ? mask : 0;
            }
        }
        byte[] bval1 = long2byteArray(val1, isUnsigned);
        byte[] bval2 = long2byteArray(val2, isUnsigned);

        /*int length = isUnsigned ? 9 : 8;
        System.err.printf("bval1: ");
        for (int i = 0; i < length; ++i)
            System.err.printf("%02x", bval1[i]);
        System.err.println();
        System.err.printf("bval2: ");
        for (int i = 0; i < length; ++i)
            System.err.printf("%02x", bval2[i]);
        System.err.println();*/

        BigInteger mult1 = new BigInteger(bval1);
        BigInteger mult2 = new BigInteger(bval2);
        BigInteger multp = mult1.multiply(mult2);
        byte[] mult = multp.toByteArray();
        byte[] pro = new byte[16];
        System.arraycopy(mult, 0, pro, 16-mult.length, mult.length);

        if (!isUnsigned && (mult[0] & 0x80) != 0) {
            for (int i = 0; i < 16-mult.length; ++i)
                pro[i] = (byte)0xFF;
        }
        /*System.err.printf("pro : ");
        for (int i = 0; i < 16; ++i)
            System.err.printf("%02x", pro[i]);
        System.err.println();*/

        byte[] res = new byte[8];
        if (isHigh)
            System.arraycopy(pro, 0, res, 0, 8);
        else
            System.arraycopy(pro, 8, res, 0, 8);
        return byteArray2Long(res, 0);
    }

    long longDiv(long val1, long val2, boolean isUnsigned, boolean isRem) {
        byte[] bval1 = long2byteArray(val1, isUnsigned);
        byte[] bval2 = long2byteArray(val2, isUnsigned);

        int length = isUnsigned ? 9 : 8;
        /*System.err.printf("bval1: ");
        for (int i = 0; i < length; ++i)
            System.err.printf("%02x", bval1[i]);
        System.err.println();
        System.err.printf("bval2: ");
        for (int i = 0; i < length; ++i)
            System.err.printf("%02x", bval2[i]);
        System.err.println();*/

        BigInteger div1 = new BigInteger(bval1);
        BigInteger div2 = new BigInteger(bval2);

        byte[] ans;
        if (isRem)
            ans = (div1.remainder(div2)).toByteArray();
        else
            ans = (div1.divide(div2)).toByteArray();
        byte[] res = new byte[8];
        System.arraycopy(ans, 0, res, 8-ans.length, ans.length);

        // if MSB is 1, then it is a signed negative number
        if (!isUnsigned && (ans[0] & 0x80) != 0) {
            for (int i = 0; i < 8-ans.length; ++i)
                res[i] = (byte)0xFF;
        }
        /*System.err.printf("res : ");
        for (int i = 0; i < 8; ++i)
            System.err.printf("%02x", res[i]);
        System.err.println();*/

        return byteArray2Long(res, 0);
    }

    public void parse() {
        long val1 = findInputByName("aluA").value;
        long val2 = findInputByName("aluB").value;
        int operator = (int)findInputByName("aluOp").value;
        int length = (int)findInputByName("aluLength").value;
        boolean isHigh = findInputByName("aluIsHigh").value == 1;
        boolean isUnsigned = findInputByName("aluIsUnsigned").value == 1;

        // NORMAL OPERATIONS
        long res = 0;
        switch (operator){
        case ALU_ADD:
            res = longAdd(val1, val2, length);
            break;
        case ALU_SUB:
            res = longSub(val1, val2, isUnsigned, length);
            break;
        case ALU_MUL:
        case ALU_MUL_U:
            res = longMult(val1, val2, isUnsigned, isHigh, length);
            break;
        case ALU_DIV:
        case ALU_DIV_U:
            res = longDiv(val1, val2, isUnsigned, false);
        case ALU_REM:
        case ALU_REM_U:
            res = longDiv(val1, val2, isUnsigned, true);

        case ALU_AND:
            res = val1 & val2;
            break;
        case ALU_OR:
            res = val1 | val2;
            break;
        case ALU_XOR:
            res = val1 ^ val2;
            break;
        case ALU_SLL:
            res = val1 << val2;
            break;
        case ALU_SRL:
            res = val1 >> val2;
            break;
        case ALU_SRA:
            res = val1 >>> val2;
            break;
        }

        // output
        findOutputByName("valE").value = res;
        findOutputByName("equal").value = (equal ? 1 : 0);
        findOutputByName("less").value = (less ? 1 : 0);
        findOutputByName("greater").value = (greater ? 1 : 0);
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
        input[0] = new Signal("aluA", 0);
        input[1] = new Signal("aluB", 0);
        input[2] = new Signal("aluOp", 0);
        input[3] = new Signal("aluLength", 0);
        input[4] = new Signal("aluIsHigh", 0);
        input[5] = new Signal("aluIsUnsigned", 0);

        output = new Signal[4];
        output[0] = new Signal("valE", 0);
        output[1] = new Signal("equal", 0);
        output[2] = new Signal("less", 0);
        output[3] = new Signal("greater", 0);
    }
}

class FloatALU extends CombineLogic
{
    public FloatALU(RISCVMachine machine) {
        super();
    }

    long f2i(int f) {
        int expo = (f & 0x7F800000) >> 23;
        int frac = (f & 0x007FFFFF) + 0x00800000;
        int minus = f & 0x80000000;

        if (expo > 0x9D) {
            if (minus == 0x80000000)
                return 0x80000000;
            else
                return 0x7FFFFFFF;
        }
        else if (expo < 0x7F) {
            return 0;
        }
        else {
            frac = (frac << 8) >> (0x9E - expo);
            if (minus == 0x80000000)
                return (long)-frac;
            else
                return (long)frac;
        }
    }
    long f2l(int f) {
        int expo = (f & 0x7F800000) >> 23;
        long frac = (long)((f & 0x007FFFFF) + 0x00800000);
        int minus = f & 0x80000000;

        if (expo > 0xBD) {
            if (minus == 0x80000000)
                return 0x8000000000000000L;
            else
                return 0x7FFFFFFFFFFFFFFFL;
        }
        else if (expo < 0x7F) {
            return 0;
        }
        else {
            frac = (frac << 40) >> (0xBE - expo);
            if (minus == 0x80000000)
                return -frac;
            else
                return frac;
        }
    }
    long f2iu(int f) {
        int expo = (f & 0x7F800000) >> 23;
        int frac = (f & 0x007FFFFF) + 0x00800000;
        int minus = f  & 0x80000000;

        if (expo > 0x9E) {
            if (minus == 0x80000000)
                return 0;
            else
                return 0xFFFFFFFF;
        }
        else if (expo < 0x7F || minus == 0x80000000) {
            return 0;
        }
        frac = (frac << 8) >> (0x9F - expo);
        return (long)(frac & 0x0FFFFFFFF);
    }
    long f2lu(int f) {
        int expo = (f & 0x7F800000) >> 23;
        int frac = (f & 0x007FFFFF) + 0x00800000;
        int minus = f  & 0x80000000;

        if (expo > 0xBE) {
            if (minus == 0x80000000)
                return 0;
            else
                return 0xFFFFFFFFFFFFFFFFL;
        }
        else if (expo < 0x7F || minus == 0x80000000) {
            return 0;
        }
        frac = (frac << 40) >> (0xBF - expo);
        return frac;
    }

    int lu2f(long x) {
        long sgn = 0;
        int expo = 0;
        int shift = 0;
        int rshift = 0;
        long frac = 0;

        long ux = x;
        if (ux != 0) {
            // shift frac to MSB
            shift = 64;
            long tmp = ux;
            while (tmp != 0) {
                tmp >>>= 1;
                --shift;
            }
            ux <<= shift;
            
            // judge carry or not
            int tricky = 0;
            int carry = (int)((ux >>> 39) & 1);
            tmp = ux << 25;
            if (tmp != 0)
                tricky = 1;
            frac = ux >>> 40;

            expo = 190 - shift;     // 190=127(bias)+63

            // take the carry
            if ((carry == 1 && tricky == 1) ||
               (carry == 1 && tricky == 0 && (frac & 1) == 1))
                frac += 1;
            if ((frac >>> 24) == 1) {
                frac >>>= 1;
                expo += 1;
            }
            frac &= 0x7FFFFF;

            return (int)sgn + (expo << 23) + (int)frac;
        }
        return 0;
    }
    int iu2f(long x) {
        return lu2f(x & 0xFFFFFFFF);
    }
    int l2f(long x) {
        if (x == 0)
            return 0;

        long ux = x;
        long sgn = ux & 0x8000000000000000L;
        //System.err.printf("sgn = %016x\n", sgn);

        if (sgn != 0)
            ux = -x;
        //System.err.printf("ux = %016x\n", ux);

        // TMIN
        if (ux == 0x8000000000000000L)
            return 0xDF000000;
        else {
            int res = lu2f(ux);
            return (int)(sgn >>> 32) | lu2f(ux);
        }
    }
    int i2f(long x) {
        x &= 0xFFFFFFFF;
        // sign extend
        if ((x >>> 31) == 1)
            x |= 0xFFFFFFFF00000000L;
        return l2f(x);
    }

    int isNAN(int x) {
        int expx = (x >> 23) & 0xFF;
        int frac = x & 0x7FFFFF;
        return (expx == 0xFF && frac > 0) ? 1 : 0;
    }

    /* @return: 0b(xyz), x-less, y-equal, z-greater */
    int fcmp(int x, int y) {
        if (isNAN(x) == 1 || isNAN(y) == 1)
            return 0b1000;

        int sgnx = x >>> 31;
        int sgny = y >>> 31;
        if (sgnx == 1 && sgny == 0)
            return 0b100;
        else if (sgnx == 0 && sgny == 1)
            return 0b001;

        int ret = 0;
        int expx = (x >> 23) & 0xFF;
        int expy = (y >> 23) & 0xFF;
        if (expx < expy)
            return 0b100;
        else if (expx > expy)
            return 0b001;

        int fracx = x & 0x7FFFFF;
        int fracy = y & 0x7FFFFF;
        if (fracx < fracy)
            return 0b100;
        else if (fracx > fracy)
            return 0b001;
        else
            return 0b010;
    }

    long fclass(int f) {
        int sgn = f >>> 31;
        int expo = (f >>> 23) & 0xFF;
        int frac = f & 0x7FFFFF;

        if (expo == 0xFF) {
            if (frac == 0) {
                if (sgn == 0)
                    return (1 << 7);   // +inf
                else
                    return 1;          // -inf
            }
            else if (frac >= 0x400000)
                return (1 << 9);       // quiet NaN
            else
                return (1 << 8);       // signaling NaN
        }
        else if (expo == 0) {
            if (sgn == 0)
                return (1 << 5);    // pos.subnormal
            else
                return (1 << 2);    // neg.subnormal
        }
        else if (f == 0x0)
            return (1 << 4);        // +0
        else if (f == 0x80000000)
            return (1 << 3);        // -0
        else if (sgn == 0)
            return (1 << 6);        // pos.normal
        else
            return (1 << 1);        // neg.normal
    }

    int realF2I (float f) {
        return ((ByteBuffer)(ByteBuffer.allocate(4).putFloat(f).position(0))).getInt();
    }
    float realI2F (int f) {
        return ((ByteBuffer)(ByteBuffer.allocate(4).putInt(f).position(0))).getFloat();
    }

    public void parse() {
        int val1 = (int)findInputByName("fval1").value;
        int val2 = (int)findInputByName("fval2").value;
        int val3 = (int)findInputByName("fval3").value;
        int opcode = (int)findInputByName("opcode").value;
        int funct5 = (int)findInputByName("funct5").value;
        int fmt = (int)findInputByName("fmt").value;
        int rm = (int)findInputByName("rm").value;
        int src2 = (int)findInputByName("src2").value;
        long ival1 = findInputByName("val1").value;

        if (fmt != 0b00) {
            findOutputByName("invalidIns").value = 1;
            return;
        }

        float fval1 = realI2F(val1);
        float fval2 = realI2F(val2);
        float fval3 = realI2F(val3);
        int res = 0;
        long ires = 0;

        int cmp = 0;

        // for R4 type
        if (opcode == 0b1000011)
            res = realF2I(fval1 * fval2 + fval3);   //FMADD
        else if (opcode == 0b1000111)
            res = realF2I(fval1 * fval2 - fval3);   //FMSUB
        else if (opcode == 0b1001011)
            res = realF2I(-(fval1 * fval2 + fval3));   //FNMADD
        else if (opcode == 0b1001111)
            res = realF2I(-(fval1 * fval2 - fval3));   //FNMSUB
        else {
        switch (funct5){
        case 0b00000:
            res = realF2I(fval1 + fval2);
            break;
        case 0b00001:
            res = realF2I(fval1 - fval2);
            break;
        case 0b00010:
            res = realF2I(fval1 * fval2);
            break;
        case 0b00011:
            res = realF2I(fval1 / fval2);
            break;
        case 0b01011:
            if (src2 == 0b00000)
                res = realF2I((float)(Math.sqrt((double)fval1)));
            else
                findOutputByName("invalidIns").value = 1;
            break;
        case 0b00100:
            if (rm == 0b000)
                res = (val1 & 0x7FFFFFFF) | (val2 & 0x80000000);   //FSGN
            else if (rm == 001)
                res = (val1 & 0x7FFFFFFF) | (~val2 & 0x80000000);  //FSGNJN
            else if (rm == 001)
                res = val1 ^ (val2 & 0x80000000);                  //FSGNJX
            else
                findOutputByName("invalidIns").value = 1;
            break;
        case 0b00101:
            cmp = (int)(fcmp(val1, val2));
            if (cmp == 0b1000)
                findOutputByName("meetNAN").value = 1;
            else if (rm == 0b000)
                res = (cmp == 0b100) ? val1 : val2;    //FMIN
            else
                res = (cmp == 0b001) ? val1 : val2;    //FMAX
            break;
        //FCMP
        case 0b10100:
            cmp = (int)(fcmp(val1, val2));
            if (cmp == 0b1000)
                findOutputByName("meetNAN").value = 1;
            else if (rm == 0b000)                //FLE.S
                ires = ((cmp & 0b110) != 0) ? 1 : 0;
            else if (rm == 0b001)           //FLT.S
                ires = ((cmp & 0b100) != 0) ? 1 : 0;
            else if (rm == 0b010)           //FEQ.S
                ires = ((cmp & 0b010) != 0) ? 1 : 0;
            else
                findOutputByName("invalidIns").value = 1;
            break;
        //FCVT
        case 0b11000:
            if (src2 == 0b00000)        //FCVT.W.S
                res = i2f(ival1);
            else if (src2 == 0b00001)   //FCVT.WU.S
                res = iu2f(ival1);
            else if (src2 == 0b00010)   //FCVT.L.S
                res = l2f(ival1);
            else if (src2 == 0b00011)   //FCVT.LU.S
                res = lu2f(ival1);
            else
                findOutputByName("invalidIns").value = 1;
            break;
        case 0b11010:
            if(src2 == 0b00000)         //FCVT.S.W
                ires = f2i(val1);
            else if (src2 == 0b00001)   //FCVT.S.WU
                ires = f2iu(val1);
            else if (src2 == 0b00010)   //FCVT.S.L
                ires = f2l(val1);
            else if (src2 == 0b00011)   //FCVT.S.LU
                ires = f2lu(val1);
            else
                findOutputByName("invalidIns").value = 1;
            break;
        case 0b11100:
            if (src2 == 0b00000 && rm == 0b000)         //FMV.X.S
                res = (int)(val1 & 0xFFFFFFFFL);
            else if (src2 == 0b00000 && rm == 0b001)    //FCLASS.S
                ires = fclass(val1);
            else
                findOutputByName("invalidIns").value = 1;
            break;
        case 0b11110:
            if (src2 == 0b00000 && rm == 0b000) {       //FMV.S.X
                ires = ival1 & 0x0FFFFFFFFL;
                if ((ires >>> 31) == 1)
                    ires |= 0xFFFFFFFF00000000L;
            }
            else
                findOutputByName("invalidIns").value = 1;
            break;
        }//END OF switch
        }//END OF else

        findOutputByName("fvalE").value = res;
        findOutputByName("valE").value = ires;
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
        input[0] = new Signal("fval1", 0);
        input[1] = new Signal("fval2", 0);
        input[2] = new Signal("fval3", 0);
        input[3] = new Signal("opcode", 0);
        input[4] = new Signal("funct3", 0);
        input[5] = new Signal("fmt", 0);
        input[6] = new Signal("rm", 0);
        input[7] = new Signal("src2", 0);
        input[8] = new Signal("val1", 0);

        output = new Signal[4];
        output[0] = new Signal("fvalE", 0);
        output[1] = new Signal("valE", 0);
        output[2] = new Signal("invalidIns", 0);
        output[3] = new Signal("meetNAN", 0);
    }
}

class PCUpdate extends CombineLogic
{
    public PCUpdate(RISCVMachine machine) {
        super();
    }

    public void parse() {
        // 未考虑 JAL(R)
        long pc = findInputByName("pc").value;
        long cnd = findInputByName("cnd").value;    //???
        long imm = findInputByName("imm").value;

        long newPC = (cnd == 1) ? (pc+imm) : (pc+4);

        findOutputByName("newPC").value = newPC;
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
        input = new Signal[3];
        input[0] = new Signal("pc", 0);
        input[1] = new Signal("cnd", 0);
        input[2] = new Signal("imm", 0);

        output = new Signal[1];
        output[0] = new Signal("newPC", 0);
    }
}

class MachineState extends CombineLogic
{
    RISCVMachine machine;
    public MachineState(RISCVMachine _machine) {
        super();

        machine = _machine;
    }

    public void parse() {
        long iiDecoder = findInputByName("iiDecoder").value;
        long iiFALU = findInputByName("iiFALU").value;
        long iaMMU = findInputByName("iaMMU").value;
        long meetNAN = findInputByName("meetNAN").value;
        long halt = findInputByName("halt").value;

        MachineStat[] MACHINE_STAT = RISCVMachine.MACHINE_STAT;
        if (iiDecoder == 1 || iiFALU == 1)
            machine.machineStateRegister = MACHINE_STAT[1].stat;
        else if (iaMMU == 1)
            machine.machineStateRegister = MACHINE_STAT[2].stat;
        else if (meetNAN == 1)
            machine.machineStateRegister = MACHINE_STAT[3].stat;
        else if (halt == 1)
            machine.machineStateRegister = MACHINE_STAT[4].stat;
        else
            machine.machineStateRegister = MACHINE_STAT[0].stat;
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
