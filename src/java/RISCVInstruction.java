package com.github.ShiftAC.RISCVSimulator;

public abstract class RISCVInstruction
{
    static int idLength = ((Integer)(Util.configManager.getConfig(
        "RISCVInstruction.length"))).intValue();
    static long insID[] = new long[idLength];
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
        FCVTSLU = 91,
        ECALL = 92;


    int code;
    String asm;
    boolean isBreakpoint;
    
    public RISCVInstruction()
    {
        this(0, null, false);
    }
    public RISCVInstruction(int _code, String _asm, boolean _isBreakpoint)
    {
        this.code = _code;
        this.asm = _asm;
        this.isBreakpoint = _isBreakpoint;
    }

    private static void initInsID()
    {
        insID[LUI] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.LUI"))).longValue();
        insID[AUIPC] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.AUIPC"))).longValue();
        insID[JAL] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.JAL"))).longValue();
        insID[JALR] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.JALR"))).longValue();
        insID[BEQ] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.BEQ"))).longValue();
        insID[BNE] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.BNE"))).longValue();
        insID[BLT] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.BLT"))).longValue();
        insID[BGE] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.BGE"))).longValue();
        insID[BLTU] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.BLTU"))).longValue();
        insID[BGEU] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.BGEU"))).longValue();
        insID[LB] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.LB"))).longValue();
        insID[LH] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.LH"))).longValue();
        insID[LW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.LW"))).longValue();
        insID[LBU] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.LBU"))).longValue();
        insID[LHU] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.LHU"))).longValue();
        insID[SB] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SB"))).longValue();
        insID[SH] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SH"))).longValue();
        insID[SW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SW"))).longValue();
        insID[ADDI] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.ADDI"))).longValue();
        insID[SLTI] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SLTI"))).longValue();
        insID[SLTIU] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SLTIU"))).longValue();
        insID[XORI] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.XORI"))).longValue();
        insID[ORI] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.ORI"))).longValue();
        insID[ANDI] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.ANDI"))).longValue();
        insID[SLLI] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SLLI"))).longValue();
        insID[SRLI] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SRLI"))).longValue();
        insID[SRAI] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SRAI"))).longValue();
        insID[ADD] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.ADD"))).longValue();
        insID[SUB] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SUB"))).longValue();
        insID[SLL] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SLL"))).longValue();
        insID[SLT] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SLT"))).longValue();
        insID[SLTU] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SLTU"))).longValue();
        insID[XOR] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.XOR"))).longValue();
        insID[SRL] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SRL"))).longValue();
        insID[SRA] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SRA"))).longValue();
        insID[OR] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.OR"))).longValue();
        insID[AND] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.AND"))).longValue();
        
        insID[LWU] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.LWU"))).longValue();
        insID[LD] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.LD"))).longValue();
        insID[SD] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SD"))).longValue();
        insID[ADDIW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.ADDIW"))).longValue();
        insID[SLLIW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SLLIW"))).longValue();
        insID[SRLIW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SRLIW"))).longValue();
        insID[SRAIW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SRAIW"))).longValue();
        insID[ADDW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.ADDW"))).longValue();
        insID[SUBW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SUBW"))).longValue();
        insID[SLLW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SLLW"))).longValue();
        insID[SRLW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SRLW"))).longValue();
        insID[SRAW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.SRAW"))).longValue();
        
        insID[MUL] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.MUL"))).longValue();
        insID[MULH] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.MULH"))).longValue();
        insID[MULHSU] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.MULHSU"))).longValue();
        insID[MULHU] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.MULHU"))).longValue();
        insID[DIV] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.DIV"))).longValue();
        insID[DIVU] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.DIVU"))).longValue();
        insID[REM] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.REM"))).longValue();
        insID[REMU] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.REMU"))).longValue();
        
        insID[MULW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.MULW"))).longValue();
        insID[DIVW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.DIVW"))).longValue();
        insID[DIVUW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.DIVUW"))).longValue();
        insID[REMW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.REMW"))).longValue();
        insID[REMUW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.REMUW"))).longValue();
        
        insID[FLW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FLW"))).longValue();
        insID[FSW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FSW"))).longValue();
        insID[FMADD] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FMADD"))).longValue();
        insID[FMSUB] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FMSUB"))).longValue();
        insID[FNMSUB] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FNMSUB"))).longValue();
        insID[FNMADD] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FNMADD"))).longValue();
        insID[FADD] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FADD"))).longValue();
        insID[FSUB] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FSUB"))).longValue();
        insID[FMUL] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FMUL"))).longValue();
        insID[FDIV] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FDIV"))).longValue();
        insID[FSQRT] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FSQRT"))).longValue();
        insID[FSGNJ] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FSGNJ"))).longValue();
        insID[FSGNJN] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FSGNJN"))).longValue();
        insID[FSGNJX] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FSGNJX"))).longValue();
        insID[FMIN] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FMIN"))).longValue();
        insID[FMAX] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FMAX"))).longValue();
        insID[FCVTWS] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FCVTWS"))).longValue();
        insID[FCVTWUS] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FCVTWUS"))).longValue();
        insID[FMVXS] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FMVXS"))).longValue();
        insID[FEQ] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FEQ"))).longValue();
        insID[FLT] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FLT"))).longValue();
        insID[FLE] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FLE"))).longValue();
        insID[FCLASS] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FCLASS"))).longValue();
        insID[FCVTSW] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FCVTSW"))).longValue();
        insID[FCVTSWU] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FCVTSWU"))).longValue();
        insID[FMVSX] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FMVSX"))).longValue();
        insID[FCVTLS] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FCVTLS"))).longValue();
        insID[FCVTLUS] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FCVTLUS"))).longValue();
        insID[FCVTSL] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FCVTSL"))).longValue();
        insID[FCVTSLU] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.FCVTSLU"))).longValue();

        insID[ECALL] = ((Long)(Util.configManager.getConfig(
            "RISCVInstruction.ECALL"))).longValue();
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

    static {
        initInsID();
    }
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
abstract class FInstruction extends RISCVInstruction
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
}

/**
 * Float instructions with rs2 and rm.
 */
class FAInstruction extends FInstruction
{
    @Override
    public long generateID()
    {
        return _generateID(opcode(), 0, 0, funct5(), 0, 0, 0);
    }
}

/**
 * Float instructions that use "rs2" for identifying.
 */
class FBInstruction extends FInstruction
{
    @Override
    public long generateID()
    {
        return _generateID(opcode(), 0, 0, funct5(), 0, 0, rs2());
    }
}

/**
 * Float instructions that use "rm" for identifying (as funct3).
 */
class FCInstruction extends FInstruction
{
    @Override
    public long generateID()
    {
        return _generateID(opcode(), 0, 0, funct5(), rm(), 0, 0);
    }
}

/**
 * Float instructions that use "rs2" and "rm" for identifying.
 */
class FDInstruction extends FInstruction
{
    @Override
    public long generateID()
    {
        return _generateID(opcode(), 0, 0, funct5(), rm(), 0, rs2());
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
