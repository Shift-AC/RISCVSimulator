package com.github.ShiftAC.RISCVSimulator;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.nio.ByteBuffer;

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

    public CombineLogic() {
        initSignals();
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



class Decoder extends CombineLogic
{
    RISCVMachine machine;
    public Decoder(RISCVMachine _machine) {
        super();

        this.machine = _machine;
    }

    void parseR(RISCVInstruction currentIns) {
        RInstruction ins = (RInstruction)currentIns;
        findOutputByName("dst").value = (long)ins.rd();
        findOutputByName("src1").value = (long)ins.rs1();
        findOutputByName("src2").value = (long)ins.rs2();
    }

    void parseS5(RISCVInstruction currentIns) {
        S5Instruction ins = (S5Instruction)currentIns;
        findOutputByName("dst").value = (long)ins.rd();
        findOutputByName("src1").value = (long)ins.rs1();
        findOutputByName("shamt").value = (long)ins.shamt();
    }

    void parseS6(RISCVInstruction currentIns) {
        S6Instruction ins = (S6Instruction)currentIns;
        findOutputByName("dst").value = (long)ins.rd();
        findOutputByName("src1").value = (long)ins.rs1();
        findOutputByName("shamt").value = (long)ins.shamt();
    }

    void parseR4(RISCVInstruction currentIns) {
        R4Instruction ins = (R4Instruction)currentIns;
        findOutputByName("dst").value = (long)ins.rd();
        findOutputByName("src1").value = (long)ins.rs1();
        findOutputByName("src2").value = (long)ins.rs2();
        findOutputByName("src3").value = (long)ins.rs3();
    }

    void parseI(RISCVInstruction currentIns) {
        IInstruction ins = (IInstruction)currentIns;
        findOutputByName("dst").value = (long)ins.rd();
        findOutputByName("src1").value = (long)ins.rs1();
        findOutputByName("imm").value = (long)ins.imm();
    }

    void parseS(RISCVInstruction currentIns) {
        SInstruction ins = (SInstruction)currentIns;
        findOutputByName("src1").value = (long)ins.rs1();
        findOutputByName("src2").value = (long)ins.rs2();
        findOutputByName("imm").value = (long)ins.imm();
    }

    void parseSB(RISCVInstruction currentIns) {
        SBInstruction ins = (SBInstruction)currentIns;
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

    void _parseF(FInstruction ins) {
        findOutputByName("dst").value = (long)ins.rd();
        findOutputByName("src1").value = (long)ins.rs1();
        findOutputByName("src2").value = (long)ins.rs2();
        findOutputByName("fmt").value = (long)ins.fmt();
        findOutputByName("rm").value = (long)ins.rm();   
    }
    void parseFA(RISCVInstruction currentIns) {
        FAInstruction ins = (FAInstruction)currentIns;
        _parseF(ins);
    }
    void parseFB(RISCVInstruction currentIns) {
        FBInstruction ins = (FBInstruction)currentIns;
        _parseF(ins);
    }
    void parseFC(RISCVInstruction currentIns) {
        FCInstruction ins = (FCInstruction)currentIns;
        _parseF(ins);
    }
    void parseFD(RISCVInstruction currentIns) {
        FDInstruction ins = (FDInstruction)currentIns;
        _parseF(ins);
    }

   public void parse() {
        int index = (int)findInputByName("insIndex").value;
        RISCVInstruction currentIns = machine.instructions[index];
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
        else if (currentIns instanceof FAInstruction)
            parseFA(currentIns);
        else if (currentIns instanceof FBInstruction)
            parseFB(currentIns);
        else if (currentIns instanceof FCInstruction)
            parseFC(currentIns);
        else if (currentIns instanceof FDInstruction)
            parseFD(currentIns);
        else
            findOutputByName("invalidIns").value = 1;

        long insID = currentIns.generateID();
        for (int i = 0; i < RISCVInstruction.insID.length; ++i) {
            if (insID == RISCVInstruction.insID[i]) {
                findOutputByName("insType").value = (long)i;
                return;
            }
        }
        findOutputByName("insType").value = -1;
        findOutputByName("invalidIns").value = 1;
        return;
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

        output = new Signal[10];
        output[0] = new Signal("dst", 0);
        output[1] = new Signal("src1", 0);
        output[2] = new Signal("src2", 0);
        output[3] = new Signal("src3", 0);
        output[4] = new Signal("imm", 0);
        output[5] = new Signal("shamt", 0);
        output[6] = new Signal("invalidIns", 0);
        output[7] = new Signal("fmt", 0);
        output[8] = new Signal("rm", 0);
        output[9] = new Signal("insType", 0);
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

    long signExtend(long immediate, int originalLength)
    {
        int shift = 64 - originalLength;
        return (immediate << shift) >> shift;
    }

    public void parse() {
        int rs1 = (int)findInputByName("src1").value;
        int rs2 = (int)findInputByName("src2").value;
        int rd = (int)findInputByName("dst").value;
        boolean read = findInputByName("gregRead").value == 1;
        boolean write = findInputByName("gregWrite").value == 1;
        int regData = (int)findInputByName("regData").value;
        //boolean isUnsigned = findInputByName("isUnsigned").value == 1;
        int length = (int)findInputByName("regLength").value;

        long ivalE = findInputByName("ivalE").value;
        long fivalE = findInputByName("fivalE").value;
        long valM = findInputByName("valM").value;
        long imm = findInputByName("imm").value;
        long cnd = findInputByName("cnd").value;

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
            long data = 0;
            switch(regData){
            case 0:
                data = ivalE;
                break;
            case 1:
                data = valM;
                break;
            case 2:
                data = imm;
                break;
            case 3:
                data = signExtend(imm, 32);
                break;
            case 4:
                data = cnd;
                break;
            case 5:
                data = fivalE;
                break;
            default:
            }
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
        input = new Signal[12];
        input[0] = new Signal("src1", 0);
        input[1] = new Signal("src2", 0);
        input[2] = new Signal("dst", 0);
        input[3] = new Signal("gregRead", 0);
        input[4] = new Signal("gregWrite", 0);
        input[5] = new Signal("regData", 0);
        input[6] = new Signal("regLength", 0);
        input[7] = new Signal("ivalE", 0);
        input[8] = new Signal("fivalE", 0);
        input[9] = new Signal("valM", 0);
        input[10] = new Signal("imm", 0);
        input[11] = new Signal("cnd", 0);

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
        int regData = (int)findInputByName("regData").value;

        int fvalE = (int)findInputByName("fvalE").value;
        int valM = (int)findInputByName("fvalM").value;

        if (read) {
            findOutputByName("fval1").value = (long)floatRegister[rs1];
            findOutputByName("fval2").value = (long)floatRegister[rs2];
            findOutputByName("fval3").value = (long)floatRegister[rs3];
        }
        if (write) {
            int data = 0;
            switch(regData) {
            case 0:
                data = fvalE;
                break;
            case 1:
                data = valM;
                break;
            default:
            }
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
        input = new Signal[9];
        input[0] = new Signal("src1", 0);
        input[1] = new Signal("src2", 0);
        input[2] = new Signal("src3", 0);
        input[3] = new Signal("dst", 0);
        input[4] = new Signal("fregRead", 0);
        input[5] = new Signal("fregWrite", 0);
        input[6] = new Signal("regData", 0);
	input[7] = new Signal("fvalE", 0);
	input[8] = new Signal("fvalM", 0);

        output = new Signal[3];
        output[0] = new Signal("fval1", 0);
        output[1] = new Signal("fval2", 0);
        output[2] = new Signal("fval3", 0);
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
        //long mask = ((1 << 32) - 1) << 32;
        return (in << 32) >> 32;// != 0 ? mask|in : 0;
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
        //for (int i = 0; i < 8; ++i)
        //    System.err.printf("%02x", res[i]);
        //System.err.println();*/

        return byteArray2Long(res, 0);
    }

    public void parse() {
        // control signals
        int aluA = (int)findInputByName("aluA").value;
        int aluB = (int)findInputByName("aluB").value;
        int operator = (int)findInputByName("aluOp").value;
        boolean isUnsigned = findInputByName("aluIsUnsigned").value == 1;
        boolean isHigh = findInputByName("aluIsHigh").value == 1;
        int length = (int)findInputByName("aluLength").value;
        int setCnd = (int)findInputByName("aluSetCnd").value;
        // data
        long reg1 = findInputByName("reg1").value;
        long reg2 = findInputByName("reg2").value;
        long pc = findInputByName("pc").value;
        long imm = findInputByName("imm").value;
        int shamt = (int)findInputByName("shamt").value;


        // choose correct val1 and val2
        long res = 0;
        long val1 = 0, val2 = 0;

        switch (aluA){
        case 0:
            val1 = reg1;
            break;
        case 1:
            val1 = reg1 & 0xFFFFFFFF;
            break;
        case 2:
            val1 = pc;
            break;
        default:
        }

        switch (aluB){
        case 0:
            val2 = reg2;
            break;
        case 1:
            val2 = reg2 & 0xFFFFFFFF;
            break;
        case 2:
            val2 = imm;
            break;
        case 3:
            val2 = (long)shamt;
            break;
        case 4:
            val2 = 4;
            break;
        default:
        }
        // operating
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
	    break;
        case ALU_REM:
        case ALU_REM_U:
            res = longDiv(val1, val2, isUnsigned, true);
	    break;
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
            if (length == 4)
                val2 &= 0x1F;
            else if (length == 8)
                val2 &= 0x3F;
            res = val1 << val2;
            break;
        case ALU_SRL:
            if (length == 4)
                val2 &= 0x1F;
            else if (length == 8)
                val2 &= 0x3F;
            res = val1 >> val2;
            break;
        case ALU_SRA:
            if (length == 4)
                val2 &= 0x1F;
            else if (length == 8)
                val2 &= 0x3F;
            res = val1 >>> val2;
            break;
        }

        // calculate cnd
        int cnd = 0;
        if (less) cnd += 0b100;
        if (equal) cnd += 0b010;
        if (greater) cnd += 0b001;
        if ((cnd & setCnd) > 0)
            cnd = 1;
        else
            cnd = 0;

        // output
        findOutputByName("valE").value = res;
        findOutputByName("equal").value = (equal ? 1 : 0);
        findOutputByName("less").value = (less ? 1 : 0);
        findOutputByName("greater").value = (greater ? 1 : 0);
        findOutputByName("cnd").value = (long)cnd;
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
        input = new Signal[12];
        input[0] = new Signal("aluA", 0);
        input[1] = new Signal("aluB", 0);
        input[2] = new Signal("aluOp", 0);
        input[3] = new Signal("aluLength", 0);
        input[4] = new Signal("aluIsHigh", 0);
        input[5] = new Signal("aluIsUnsigned", 0);
        input[6] = new Signal("aluSetCnd", 0);
        input[7] = new Signal("reg1", 0);
        input[8] = new Signal("reg2", 0);
        input[9] = new Signal("pc", 0);
        input[10] = new Signal("imm", 0);
        input[11] = new Signal("shamt", 0);


        output = new Signal[5];
        output[0] = new Signal("valE", 0);
        output[1] = new Signal("equal", 0);
        output[2] = new Signal("less", 0);
        output[3] = new Signal("greater", 0);
        output[4] = new Signal("cnd", 0);
    }
}

class FloatALU extends CombineLogic
{
    static final int
        ALU_ADD = 0,
        ALU_SUB = 1,
        ALU_MUL = 2,
        ALU_DIV = 3,
        ALU_AND = 5,
        ALU_OR = 6,
        ALU_XOR = 7,
        ALU_SQRT = 8,
        ALU_MADD = 22,
        ALU_MSUB = 23,
        ALU_NMADD = 24,
        ALU_NMSUB = 25,
        ALU_FCMP = 103,
        ALU_FCLASS = 104,
        ALU_I2F = 110,
        ALU_IU2F = 111,
        ALU_L2F = 112,
        ALU_LU2F = 113,
        ALU_F2I = 114,
        ALU_F2IU = 115,
        ALU_F2L = 116,
        ALU_F2LU = 117,
        ALU_FMVXS = 118,
        ALU_FMVSX = 119,
        ALU_FSGN = 120,
        ALU_FSGNJN = 121,
        ALU_FSGNJX = 122,
        ALU_FMIN = 123,
        ALU_FMAX = 124;

    public FloatALU(RISCVMachine machine) {
        super();
    }

    long f2i(int f) {
	float fa = Float.intBitsToFloat(f);
	if (fa > (float)(0x7FFFFFFF))
        {
            return 0x7FFFFFFF;
        }
        if (fa < (float)(0x80000000))
        {
            return 0x80000000;
        }
	return (long)(fa);
    }
    long f2l(int f) {
	float fa = Float.intBitsToFloat(f);
	if (fa > (float)(0x7FFFFFFFFFFFFFFFL))
        {
            return 0x7FFFFFFFFFFFFFFFL;
        }
        if (fa < (float)(0x8000000000000000L))
        {
            return 0x800000000000000L;
        }
        return (long)(fa);
    }
    long f2iu(int f) {
	float fa = Float.intBitsToFloat(f);
	if (fa > (float)(0x00000000FFFFFFFFL))
        {
            return 0x00000000FFFFFFFFL;
        }
        if (fa < 0.0f)
        {
            return 0x0;
        }
	return (long)(fa);
    }
    long f2lu(int f) {
	float fa = Float.intBitsToFloat(f);
        float smax = (float)(0x7FFFFFFFFFFFFFFFL);
	float umax = smax * 2;
	if (fa > umax)
        {
            return 0xFFFFFFFFFFFFFFFFL;
        }
	if (fa > smax)
        {
            fa -= smax;
            return 0x8000000000000000L & (long)fa;
        }
        if (fa < 0.0f)
        {
            return 0x0;
        }
	return (long)(fa);
    }

    int lu2f(long x) {
	boolean sgn = x < 0;
        float fx = 0.0f;	
	if (sgn)
	{
            x &= 0x7FFFFFFFFFFFFFFFL;
            fx += (float)(0x7FFFFFFFFFFFFFFFL) + 1.0f;
	}
	fx += (float)x;
	return Float.floatToIntBits(fx);
    }
    int iu2f(long x) {
	float fx = (float)x;
	return Float.floatToIntBits(fx);
    }
    int l2f(long x) {
	float fx = (float)x;
	return Float.floatToIntBits(fx);
    }
    int i2f(long x) {
	float fx = (float)x;
	return Float.floatToIntBits(fx);
    }

    int isNAN(int x) {
        int expx = (x >> 23) & 0xFF;
        int frac = x & 0x7FFFFF;
        return (expx == 0xFF && frac > 0) ? 1 : 0;
    }

    /* @return: 0b(xyz), x-less, y-equal, z-greater */
    int fcmp(int x, int y) {
        /*if (isNAN(x) == 1 || isNAN(y) == 1)
            return 0b1000;

	int sgnx = x >>> 31;
	int sgny = y >>> 31;
	if (sgnx == 1 && sgny == 0)
	    return 0b100;
	else if(sgnx == 0 && sgny == 1)
	    return 0b001;

        int otherx = x & ((1 << 31) - 1);
        int othery = y & ((1 << 31) - 1);
        if (otherx < othery)
            return 0b100;
        else if (otherx > othery)
            return 0b001;
        else
            return 0b010;*/
	float fx = realI2F(x);
	float fy = realI2F(y);
	
        System.out.println(fx + " " + fy);

	if (fx < fy)
	    return 0b100;
	else if (fx > fy)
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
        //return ((ByteBuffer)(ByteBuffer.allocate(4).putFloat(f).position(0))).getInt();
	return Float.floatToIntBits(f);
    }
    float realI2F (int f) {
        //return ((ByteBuffer)(ByteBuffer.allocate(4).putInt(f).position(0))).getFloat();
	return Float.intBitsToFloat(f);
    }

    public void parse() {
        int fALUA = (int)findInputByName("fALUA").value;
        int fALUB = (int)findInputByName("fALUB").value;
        int fALUC = (int)findInputByName("fALUC").value;
        int fALUOp = (int)findInputByName("fALUOp").value;
        int fALUSetCnd = (int)findInputByName("fALUSetCnd").value;

        int val1 = (int)findInputByName("fval1").value;
        int val2 = (int)findInputByName("fval2").value;
        int val3 = (int)findInputByName("fval3").value;
        long ival1 = findInputByName("ival1").value;
        int fmt = (int)findInputByName("fmt").value;
        int rm = (int)findInputByName("rm").value;

        //if (fmt != 0b00) {
        //    findOutputByName("invalidIns").value = 1;
        //    return;
        //}

        float fval1 = realI2F(val1);
        float fval2 = realI2F(val2);
        float fval3 = realI2F(val3);

        int cmp = 0;
        int res = 0;
        long ires = 0;


        switch (fALUOp) {
        case ALU_MADD:
            res = realF2I(fval1 * fval2 + fval3);   //FMADD
            break;
        case ALU_MSUB:
            res = realF2I(fval1 * fval2 - fval3);   //FMSUB
            break;
        case ALU_NMADD:
            res = realF2I(-(fval1 * fval2 + fval3));   //FNMADD
            break;
        case ALU_NMSUB:
            res = realF2I(-(fval1 * fval2 - fval3));   //FNMSUB
            break;
        case ALU_ADD:
            res = realF2I(fval1 + fval2);
            break;
        case ALU_SUB:
            res = realF2I(fval1 - fval2);
            break;
        case ALU_MUL:
            res = realF2I(fval1 * fval2);
            break;
        case ALU_DIV:
            res = realF2I(fval1 / fval2);
            break;
        case ALU_SQRT:
            res = realF2I((float)(Math.sqrt((double)fval1)));
            break;
        case ALU_FSGN:
            res = (val1 & 0x7FFFFFFF) | (val2 & 0x80000000);
            break;
        case ALU_FSGNJN:
            res = (val1 & 0x7FFFFFFF) | (~val2 & 0x80000000);
            break;
        case ALU_FSGNJX:
            res = val1 ^ (val2 & 0x80000000);
            break;
        case ALU_FMIN:
            cmp = (int)(fcmp(val1, val2));
            if (cmp == 0b1000)
                findOutputByName("meetNAN").value = 1;
            else
                res = (cmp == 0b100) ? val1 : val2;
            break;
        case ALU_FMAX:
            cmp = (int)(fcmp(val1, val2));
            if (cmp == 0b1000)
                findOutputByName("meetNAN").value = 1;
            else
                res = (cmp == 0b001) ? val1 : val2;
            break;
        case ALU_FCMP:
            cmp = (int)(fcmp(val1, val2));
            if (cmp == 0b1000)
                findOutputByName("meetNAN").value = 1;
            else
                ires = ((cmp & fALUSetCnd) != 0) ? 1 : 0;
            break;
        //FCVT
        case ALU_I2F:           //FCVT.W.S
            res = i2f(ival1);
            break;
        case ALU_IU2F:          //FCVT.WU.S
            res = iu2f(ival1);
            break;
        case ALU_L2F:           //FCVT.L.S
            res = l2f(ival1);
            break;
        case ALU_LU2F:          //FCVT.LU.S
            res = lu2f(ival1);
            break;
        case ALU_F2I:           //FCVT.S.W
            ires = f2i(val1);
            break;
        case ALU_F2IU:          //FCVT.S.WU
            ires = f2iu(val1);
            break;
        case ALU_F2L:           //FCVT.S.L
            ires = f2l(val1);
            break;
        case ALU_F2LU:          //FCVT.S.LU
            ires = f2lu(val1);
            break;
        case ALU_FMVXS:         //FMV.X.S
            ires = (long)val1;
            if ((ires >>> 31) == 1)
                ires |= 0xFFFFFFFF00000000L;
            break;
        case ALU_FMVSX:         //FMV.S.X
            res = (int)(ival1 & 0xFFFFFFFF);
            break;
        case ALU_FCLASS:        //FCLASS.S
            ires = fclass(val1);
           break;
        }//END OF switch

        findOutputByName("fvalE").value = (long)res;
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
        input = new Signal[11];
        input[0] = new Signal("fALUA", 0);
        input[1] = new Signal("fALUB", 0);
        input[2] = new Signal("fALUC", 0);
        input[3] = new Signal("fALUOp", 0);
        input[4] = new Signal("fALUSetCnd", 0);

        input[5] = new Signal("fval1", 0);
        input[6] = new Signal("fval2", 0);
        input[7] = new Signal("fval3", 0);
        input[8] = new Signal("ival1", 0);
        input[9] = new Signal("fmt", 0);
        input[10] = new Signal("rm", 0);

        output = new Signal[3];
        output[0] = new Signal("fvalE", 0);
        output[1] = new Signal("valE", 0);
        output[2] = new Signal("meetNAN", 0);
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
            ((address >= segment.startAddress) && 
            (address < segment.endAddress));
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
                System.arraycopy(ba, 0, segment.memory, 
                                 (int)(address - segment.startAddress), 2);
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
                System.arraycopy(ba, 0, segment.memory, 
                                 (int)(address - segment.startAddress), 4);
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
                System.arraycopy(ba, 0, segment.memory, 
                                 (int)(address - segment.startAddress), 8);
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
        int mAddr = (int)findInputByName("memAddr").value;
        int mData = (int)findInputByName("memData").value;
        boolean write = findInputByName("memWrite").value == 1;
        boolean read = findInputByName("memRead").value == 1;
        int length = (int)findInputByName("memLength").value;
        boolean isUnsigned = findInputByName("memIsUnsigned").value == 1;

        long ival1 = findInputByName("ival1").value;
        long imm = findInputByName("imm").value;
        long ivalE = findInputByName("ivalE").value;
        int fvalE = (int)findInputByName("fvalE").value;
        long ival2 = findInputByName("ival2").value;
        int fval2 = (int)findInputByName("fval2").value;

        long address = 0;
        long inData = 0;

        // choose address and data
        switch (mAddr) {
        case 0:
            address = ival1 + imm;
            break;
        default:
        }

        switch (mData) {
        case 0:
            inData = ivalE;
            break;
        case 1:
            inData = (long)fvalE;
            break;
        case 2:
            inData = ival2;
            break;
        case 3:
            inData = (long)fval2;
            break;
        default:
        }

        // READ PROCESS
        if (read)
        {
            Long outData = null;
            if (length == 1)
                outData = loadByte(address, isUnsigned);
            else if (length == 2)
                outData = loadShort(address, isUnsigned);
            else if (length == 4)
                outData = loadInt(address, isUnsigned);
            else if (length == 8)
                outData = loadLong(address, isUnsigned);

            // output signals
            if (outData != null) {
                findOutputByName("valM").value = outData.longValue();
                findOutputByName("invalidAddress").value = 0;
            }
            else {
                System.err.printf("Memory Error: invalid address %016x\n", address);
                findOutputByName("invalidAddress").value = 1;
            }
        }

        // WRITE PROCESS
        if (write) {
//System.err.printf("addr = %016x\n", address);
//System.err.printf("inData = %016x\n", inData);
            boolean success = false;
            if (length == 1)
                success = saveByte(address, (byte)inData);
            else if (length == 2)
                success = saveShort(address, (short)inData);
            else if (length == 4)
                success = saveInt(address, (int)inData);
            else if (length == 8)
                success = saveLong(address, inData);

	    // output signals
            if (success) {
                findOutputByName("invalidAddress").value = 0;
            }
            else {
                System.err.printf("Memory Error: invalid address %016x\n", address);
                findOutputByName("invalidAddress").value = 1;
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
        input = new Signal[12];
        input[0] = new Signal("memAddr", 0);
        input[1] = new Signal("memData", 0);
        input[2] = new Signal("memWrite", 0);
        input[3] = new Signal("memRead", 0);
        input[4] = new Signal("memLength", 0);
        input[5] = new Signal("memIsUnsigned", 0);
        input[6] = new Signal("ival1", 0);
        input[7] = new Signal("imm", 0);
        input[8] = new Signal("ivalE", 0);
        input[9] = new Signal("fvalE", 0);
        input[10] = new Signal("ival2", 0);
        input[11] = new Signal("fval2", 0);

        output = new Signal[2];
        output[0] = new Signal("valM", 0);
        output[1] = new Signal("invalidAddress", 0);
    }
}


class PCUpdate extends CombineLogic
{
    RISCVMachine machine;

    public PCUpdate(RISCVMachine _machine) {
        super();
        machine = _machine;
    }

    public void parse() {
        int pcCnd = (int)findInputByName("pcCnd").value;
        int pcSrc = (int)findInputByName("pcSrc").value;
        long pc = findInputByName("pc").value;
        long cnd = findInputByName("cnd").value;    //???
        long imm = findInputByName("imm").value;
        long ival1 = findInputByName("ival1").value;

        long newPC = 0;
        if (pcCnd == 1)
            newPC = (cnd == 1) ? (pc+imm) : (pc+4);
        else {
            switch(pcSrc) {
            case 0:
                newPC = pc+4;
                break;
            case 1:
                newPC = pc+imm;
                break;
            case 2:
                newPC = (ival1+imm)&(~0-1);
                break;
            default:
            }
        }

        findOutputByName("newPC").value = newPC;
        machine.programCounter = newPC;
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
        input[0] = new Signal("pcCnd", 0);
        input[1] = new Signal("pcSrc", 0);
        input[2] = new Signal("pc", 0);
        input[3] = new Signal("cnd", 0);
        input[4] = new Signal("imm", 0);
        input[5] = new Signal("ival1", 0);

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
        long iInsDecoder = findInputByName("iInsDecoder").value;
        long iAddrMMU = findInputByName("iAddrMMU").value;
        long meetNAN = findInputByName("meetNAN").value;
        long halt = findInputByName("halt").value;

        MachineStat[] MACHINE_STAT = RISCVMachine.MACHINE_STAT;
        if (iInsDecoder == 1)
            machine.machineStateRegister = MACHINE_STAT[1].stat;
        else if (iAddrMMU == 1)
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
        input = new Signal[4];
        input[0] = new Signal("iInsDecoder", 0);
        input[1] = new Signal("iAddrMMU", 0);
        input[2] = new Signal("meetNAN", 0);
        input[3] = new Signal("halt", 0);

        output = new Signal[1];
        output[0] = new Signal("none", 0);
    }
}



