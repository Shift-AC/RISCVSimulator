package com.github.ShiftAC.RISCVSimulator;

public abstract class RISCVInstruction
{
    static long insID[];
    static int
        LUI = 0,    //32I
        AUIPC = 1,
        JAL = 2,
        JALR = 3,
        BEQ = 4,
        BNE = 5,
        BLT = 6,
        BGE = 7,
        BLTU = 8,
        BGEU = 9,
        LB = 10,
        LH = 11,
        LW = 12,
        LBU = 13,
        LHU = 14,
        SB = 15,
        SH = 16,
        SW = 17,
        ADDI = 18,
        SLTI = 19,
        SLTIU = 20,
        XORI = 21,
        ORI = 22,
        ANDI = 23,
        SLLI = 24,
        SRLI = 25,
        SRAI = 26,
        ADD = 27,
        SUB = 28,
        SLL = 29,
        SLT = 30,
        SLTU = 31,
        XOR = 32,
        SRL = 33,
        SRA = 34,
        OR = 35,
        AND = 36,
        LWU = 37,   //64I
        LD = 38,
        SD = 39,
        ADDIW = 40,
        SLLIW = 41,
        SRLIW = 42,
        SRAIW = 43,
        ADDW = 44,
        SUBW = 45,
        SLLW = 46,
        SRLW = 47,
        SRAW = 48,
        MUL = 49,   //32M
        MULH = 50,
        MULHSU = 51,
        MULHU = 52,
        DIV = 53,
        DIVU = 54,
        REM = 55,
        REMU = 56,
        MULW = 57,  //64M
        DIVW = 58,
        DIVUW = 59,
        REMW = 60,
        REMUW = 61,
        FLW = 62,       //32F
        FSW = 63,
        FMADD = 64,
        FMSUB = 65,
        FNMSUB = 66,
        FNMADD = 67,
        FADD = 68,
        FSUB = 69,
        FMUL = 70,
        FDIV = 71,
        FSQRT = 72,
        FSGNJ = 73,
        FSGNJN = 74,
        FSGNJX = 75,
        FMIN = 76,
        FMAX = 77,
        FCVTWS = 78,
        FCVTWUS = 79,
        FMVXS = 80,
        FEQ = 81,
        FLT = 82,
        FLE = 83,
        FCLASS = 84,
        FCVTSW = 85,
        FCVTSWU = 86,
        FMVSX = 87,
        FCVTLS = 88,    //64F
        FCVTLUS = 89,
        FCVTSL = 90,
        FCVTSLU = 91;

    int code;
    String asm;
    boolean isBreakpoint;
    
    public RISCVInstruction() {}
    public RISCVInstruction(int _code, String _asm, boolean _isBreakpoint)
    {
        this.code = _code;
        this.asm = _asm;
        this.isBreakpoint = _isBreakpoint;
    }

    public int opcode()
    {
        return code & 0x7F;
    }

    protected int getField(int lowBit, int length)
    {
        return (code >>> lowBit) & ((1 << length) - 1);
    }

    protected long _generateID(int opcode, int funct7, int funct6,
                              int funct5, int funct3, int funct2,
                              int rs2)
    {
        long id = 0;
        id |= ((long)opcode) << 56;
        id |= ((long)funct7) << 48;
        id |= ((long)funct6) << 40;
        id |= ((long)funct5) << 32;
        id |= ((long)funct3) << 24;
        id |= ((long)funct2) << 16;
        id |= ((long)rs2) << 8;
        // preserve the lowest 8 bits for future use
        return id;
    }

    public long generateID() { return 0; }

    protected static int signExtend(int immediate, int originalLength)
    {
        int shift = 32 - originalLength;
        return (immediate << shift) >> shift;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append(String.format("%b\t%08x\t%-30s\n", isBreakpoint, code, asm));
        return str.toString();     
    }

    //static {
    //
    //}
}

class RInstruction extends RISCVInstruction
{
    public int funct7()
    {
        return getField(25, 7);
    }
    public int funct3()
    {
        return getField(12, 3);
    }
    public int rd()
    {
        return getField(7, 5);
    }
    public int rs1()
    {
        return getField(15, 5); 
    }
    public int rs2()
    {
        return getField(20, 5);
    }

    @Override
    public long generateID()
    {
        return _generateID(opcode(), funct7(), 0, 0, funct3(), 0, 0);
    }
}

/**
 * Integer instructions with 5-bit shamt.
 */
class S5Instruction extends RISCVInstruction
{
    public int funct7()
    {
        return getField(25, 7);
    }
    public int funct3()
    {
        return getField(12, 3);
    }
    public int rd()
    {
        return getField(7, 5);
    }
    public int rs1()
    {
        return getField(15, 5); 
    }
    public int shamt()
    {
        return getField(20, 5);
    }

    @Override
    public long generateID()
    {
        return _generateID(opcode(), funct7(), 0, 0, funct3(), 0, 0);
    }
}

/**
 * Integer instructions with 6-bit shamt.
 */
class S6Instruction extends RISCVInstruction
{
    public int funct6()
    {
        return getField(26, 6);
    }
    public int funct3()
    {
        return getField(12, 3);
    }
    public int rd()
    {
        return getField(7, 5);
    }
    public int rs1()
    {
        return getField(15, 5); 
    }
    public int shamt()
    {
        return getField(20, 6);
    }

    @Override
    public long generateID()
    {
        return _generateID(opcode(), 0, funct6(), 0, funct3(), 0, 0);
    }
}

class R4Instruction extends RISCVInstruction
{
    public int funct7()
    {
        return getField(25, 7);
    }
    public int funct3()
    {
        return getField(12, 3);
    }
    public int funct2()
    {
        return getField(25, 2);
    }
    public int rd()
    {
        return getField(7, 5);
    }
    public int rs1()
    {
        return getField(15, 5); 
    }
    public int rs2()
    {
        return getField(20, 5);
    }
    public int rs3()
    {
        return getField(27, 5);
    }

    @Override
    public long generateID()
    {
        return _generateID(opcode(), funct7(), 0, 0, funct3(), funct2(), 0);
    }
}

class IInstruction extends RISCVInstruction
{
    public int rd()
    {
        return getField(7, 5);
    }
    public int imm()
    {
        return signExtend(getField(20, 12), 12);
    }
    public int funct3()
    {
        return getField(12, 3);
    }
    public int rs1()
    {
        return getField(15, 5); 
    }

    @Override
    public long generateID()
    {
        return _generateID(opcode(), 0, 0, 0, funct3(), 0, 0);
    }
}

class SInstruction extends RISCVInstruction
{
    public int imm()
    {
        int tmp = (getField(7, 5) | 
                  ((getField(25, 7)) << 5));
        return signExtend(tmp, 12);
    }
    public int rs1()
    {
        return getField(15, 5); 
    }
    public int rs2()
    {
        return getField(20, 5);
    }
    public int funct3()
    {
        return getField(12, 3);
    }

    @Override
    public long generateID()
    {
        return _generateID(opcode(), 0, 0, 0, funct3(), 0, 0);
    }
}

class SBInstruction extends RISCVInstruction
{
    public int imm()
    {
        int tmp = (getField(8, 4) << 1) |
                  (getField(25, 6) << 5) |
                  (getField(7, 1) << 11) |
                  (getField(31, 1) << 12);
        return signExtend(tmp, 13);
    }
    public int rs1()
    {
        return getField(15, 5); 
    }
    public int rs2()
    {
        return getField(20, 5);
    }
    public int funct3()
    {
        return getField(12, 3);
    }

    @Override
    public long generateID()
    {
        return _generateID(opcode(), 0, 0, 0, funct3(), 0, 0);
    }
}

class UInstruction extends RISCVInstruction
{
    public int rd()
    {
        return getField(7, 5);
    }
    public int imm()
    {
        return getField(12, 20) << 12;
    }

    @Override
    public long generateID()
    {
        return _generateID(opcode(), 0, 0, 0, 0, 0, 0);
    }
}

class UJInstruction extends RISCVInstruction
{
    public int rd()
    {
        return getField(7, 5);
    }
    public int imm()
    {
        int tmp = (getField(21, 10) << 1) |
                  (getField(20, 1) << 11) |
                  (getField(12, 8) << 12) |
                  (getField(31, 1) << 20);
        return signExtend(tmp, 21);
    }

    @Override
    public long generateID()
    {
        return _generateID(opcode(), 0, 0, 0, 0, 0, 0);
    }
}

/**
 * Common float instructions.
 */
class FZInstruction extends RISCVInstruction
{
    public int funct5()
    {
        return getField(27, 5);
    }
    public int fmt()
    {
        return getField(25, 2);
    }
    public int rd()
    {
        return getField(7, 5);
    }
    public int rs1()
    {
        return getField(15, 5); 
    }
    public int rs2()
    {
        return getField(20, 5);
    }
    public int rm()
    {
        return getField(12, 3);
    }

    @Override
    public long generateID()
    {
        return _generateID(opcode(), 0, 0, funct5(), 0, 0, 0);
    }
}

/**
 * Float instructions that use "rs2" for identifying.
 */
class FJInstruction extends RISCVInstruction
{
    public int funct5()
    {
        return getField(27, 5);
    }
    public int fmt()
    {
        return getField(25, 2);
    }
    public int rd()
    {
        return getField(7, 5);
    }
    public int rs1()
    {
        return getField(15, 5); 
    }
    public int rs2()
    {
        return getField(20, 5);
    }
    public int rm()
    {
        return getField(12, 3);
    }

    @Override
    public long generateID()
    {
        return _generateID(opcode(), 0, 0, funct5(), 0, 0, rs2());
    }
}


class UnknownInstruction extends RISCVInstruction
{
    @Override
    public long generateID()
    {
        return _generateID(0, 0, 0, 0, 0, 0, 0);
    }    
}
