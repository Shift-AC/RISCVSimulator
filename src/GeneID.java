package com.github.ShiftAC.RISCVSimulator;

import java.io.*;

public class GeneID {
    public static void main(String[] args)
        throws IOException
    {
        PrintWriter pw = new PrintWriter(
                         new BufferedWriter(
                         new FileWriter("config/RISCVInstructionNew", false)));
        String prnt;

	// help info
	pw.write("# /config/RISCVInstruction\n");
	pw.write("# \tstores the IDs of instructions.\n");
	pw.write("# \t\"length\" indicates the number of instructions.\n");
	pw.write("# \tAn ID is a 64-bit integer. From MSB, every 8-bit means\n");
	pw.write("# \t\topcode, funct7, funct6, funct5, funct3, funct2, rs2,\n");
	pw.write("# respectively. The lowest 8-bit is preserved for future use.\n\n");

	// length
        int length = args.length;
        prnt = String.format("I length=%d\n", length/2);
        pw.write(prnt);

	// instruction IDs

        for (int cnt = 0; cnt < length-1; cnt+=2) {
System.err.printf("cnt = %d\n", cnt);
            byte[] b = args[cnt].getBytes();
            int code = 0;
            for (int i = 0; i < 32; ++i) {
                b[i] -= '0';
                System.err.printf("%x", b[i]);
                code |= (b[i] << (31 - i));
            }
            System.err.println();

            RISCVInstruction ins;
            int opcode = code & 0x7F;
            int funct3 = (code >> 12) & 0x7;
            int funct7 = (code >> 25) & 0x7F;
            if (ELFReader.isRInstruction(opcode, funct7))
                ins = new RInstruction();
            else if (ELFReader.isS5Instruction(opcode, funct3))
                ins = new S5Instruction();
            else if (ELFReader.isS6Instruction(opcode, funct3))
                ins = new S6Instruction();
            else if (ELFReader.isR4Instruction(opcode))
                ins = new R4Instruction();
            else if (ELFReader.isIInstruction(opcode, funct3))        // 32F
                ins = new IInstruction();
            else if (ELFReader.isSInstruction(opcode))        // 32/64I, 32F
                ins = new SInstruction();
            else if (ELFReader.isSBInstruction(opcode))
                ins = new SBInstruction();
            else if (ELFReader.isUInstruction(opcode))
                ins = new UInstruction();
            else if (ELFReader.isUJInstruction(opcode))
                ins = new UJInstruction();
            else if (ELFReader.isFAInstruction(opcode, funct7))
                ins = new FAInstruction();
            else if (ELFReader.isFBInstruction(opcode, funct7))
                ins = new FBInstruction();
            else if (ELFReader.isFCInstruction(opcode, funct7))
                ins = new FCInstruction();
            else if (ELFReader.isFDInstruction(opcode, funct7))
                ins = new FDInstruction();
            else {
                System.err.println("error");
                return;
            }

            ins.code = code;
            ins.asm = args[cnt+1];
            ins.isBreakpoint = false;

            prnt = String.format("L %s=%016x\n", ins.asm,
                ins.generateID());
            pw.write(prnt);
        }
        pw.close();
    }
}
