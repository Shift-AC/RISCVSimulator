package com.github.ShiftAC.RISCVSimulator;

public abstract class RISCVInstruction
{
    int code;
    String asm;
    boolean isBreakpoint;
    
    public int opcode()
    {
        return code & 0x7F;
    }

    protected int getField(int lowBit, int length)
    {
        return (code >>> lowBit) & ((1 << length) - 1);
    }

    protected static int signExtend(int immediate, int originalLength)
    {
        int shift = 33 - originalLength;
        return (immediate << shift) >> shift;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append(String.format("%b\t%08x\t%-30s\n", isBreakpoint, code, asm));
        return str.toString();     
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
}

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
}

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
}

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
}

class UnknownInstruction extends RISCVInstruction
{

}