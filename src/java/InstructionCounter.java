package com.github.ShiftAC.RISCVSimulator;

public class InstructionCounter
{
    long total = 0;
    long jump = 0;
    long memory = 0;
    long arithmetic = 0;
    long syscall = 0;
    long branch = 0;

    public void count(RISCVInstruction ins)
    {
        String asm = ins.asm;
        total++;
        if (asm.length() >= 5)
	{
            if (asm.substring(0, 5).equals("ecall"))
            {
                syscall++;
                return;
            }
        }
        
	if (asm.charAt(0) == 'b')
        {
            branch++;
        }
        else if (asm.charAt(0) == 'j')
        {
            jump++;
        }
        else
        {
            String prefix = asm.substring(0, 2);
            if (prefix.equals("lb") || prefix.equals("lh") ||
                prefix.equals("lw") || prefix.equals("lbu") ||
                prefix.equals("lhu") || prefix.equals("ld") ||
                prefix.equals("sb") || prefix.equals("sh") ||
                prefix.equals("sw") || prefix.equals("ld") ||
                prefix.equals("flw") || prefix.equals("fsw"))
            {
                memory++;
            }
            else
            {
                arithmetic++;
            }
        }
    }
}
