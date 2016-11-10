package com.github.ShiftAC.RISCVSimulator;

import java.util.*;
import java.io.*;
import java.lang.*;

public class ELFReader
{
	static final String[] SEG_NAMES = Util.NAMES_OF_MEMSEG;

    //Decoder decoder;	// byte codes --> RISCVInstructions
    RISCVMachine machine;	// parsed contents of RISCV machine
    byte[] elfAllBytes;	// buffer all contents in ELF file
	Elf64_Ehdr elfHeader;
	Elf64_Phdr proHeader;
	Elf64_Shdr[] secHeader;
	Elf64_Sym[] symTE;	// symbol table element
	Symbol[] symbol;
	int[] codes;
 
	/**
	 * Constructor.
	 */
    public ELFReader()
    {
        //decoder = new Decoder();
        machine = new RISCVMachine();
    }

	public static void main(String[] args) {
		//Util.initUtil();

		RISCVMachine machine;

		ELFReader elfReader = new ELFReader();
		String fileName = "test/hello";
		try {
			InputStream is = new FileInputStream(fileName);
			machine = elfReader.read(is, fileName);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		machine.printInfo();

		return;
	}

    /** 
     * Main Procedure of this class.
     * @return:
     *		Success: a RISCVMachine object that contains all parsed information in ELF.
     *		Fail: null
     * @param:
     *		InputStream is: an InputStream of the ELF file.
	 */
    public RISCVMachine read(InputStream is, String fileName)
    {
    	// read headers
    	int ret = readELFSection(is);
    	if (ret == -1) {
			System.err.printf("Error in readELFSection!\n");
    		return null;
    	}
    	
    	// parse memory segments
    	initMemorySegment();
    	/*for (int i = 0; i < machine.memory.length; ++i) {
    		System.err.printf("%s", machine.memory[i].toString());
    	}*/

    	// parse symbols
    	ret = initSymbol();
    	if (ret == -1) {
    		System.err.printf("Error in symbol parsing!\n");
    		return null;
    	}
    	/*for (int i = 0; i < symbol.length; ++i)
    		System.err.printf("%s", symbol[i].toString());*/

    	ret = initInstruction(fileName);
    	if (ret == -1) {
    		System.err.printf("Invalid instruction(s) detected!\n");
    	}

    	initPC();

        return machine;
    }

    /**
     * Read and parse ELF header, program header and section headers.
     * @return:
     *		Success: 0, Fail: -1
     * @param:
     *		InputStream is: an InputStream of the ELF file.
     */
    public int readELFSection(InputStream is)
	{
		// read ELFHeader
    	byte[] ehdr = new byte[64];
		elfHeader = new Elf64_Ehdr();
		elfHeader.read(is, ehdr);

		// get ELF file size from ELFHeader
		int size = (int)(elfHeader.e_shoff + elfHeader.e_shentsize * elfHeader.e_shnum);
		elfAllBytes = new byte[size+64];
		int count;
		try {
			if ((count = is.read(elfAllBytes, 64, size)) == -1) {
				System.err.printf("ELF file error!\n");
				return -1;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		System.arraycopy(ehdr, 0, elfAllBytes, 0, 64);

		// parse sectionHeader and programHeader
		proHeader = new Elf64_Phdr();
		proHeader.read(elfAllBytes, elfHeader.e_phoff);
		secHeader = new Elf64_Shdr[elfHeader.e_shnum];
		for (int i = 0; i < (int)elfHeader.e_shnum; ++i) {
			secHeader[i] = new Elf64_Shdr();
			secHeader[i].read(elfAllBytes, elfHeader.e_shoff + i*elfHeader.e_shentsize);
		}

		// get shStrtab from .shstrtab section
		int strtabSize = (int)secHeader[elfHeader.e_shstrndx].sh_size;
		long strtabOffset = secHeader[elfHeader.e_shstrndx].sh_offset;
		byte[] shStrtab = new byte[strtabSize];
        System.arraycopy(elfAllBytes, (int)strtabOffset, shStrtab, 0, strtabSize);

        // get each name and byte-contents
		for (int i = 0; i < elfHeader.e_shnum; ++i) {
			secHeader[i].getName(shStrtab);
			secHeader[i].getContents(elfAllBytes);
		}

		return 0;
	}

	public int initMemorySegment() {
		// load sections to memory
		MemorySegment memory;
		for (int i = 0; i < elfHeader.e_shnum; ++i) {
			for (int ndx = 0; ndx < SEG_NAMES.length; ndx++) {
				if (secHeader[i].name.equals(SEG_NAMES[ndx])) {
					memory = machine.memory[ndx];
					memory.startAddress = secHeader[i].sh_addr;
					memory.endAddress = secHeader[i].sh_addr + secHeader[i].sh_size;
					memory.writable = (secHeader[i].sh_flags & 1) > 0 ? true : false;
					memory.executable = ((secHeader[i].sh_flags >> 1) & 1) > 0 ? true : false;
					int size = (int)secHeader[i].sh_size;
					memory.memory = new byte[size];
					if (secHeader[i].sh_type != Util.SHT_NOBITS)
						System.arraycopy(secHeader[i].contents, 0,
										memory.memory, 0, size);
					break;
				}
			}
		}

		// set heap and stack memory
		final int HEAP = machine.SEGMENT_HEAP;
		final int STACK = machine.SEGMENT_STACK;
		machine.memory[HEAP].startAddress = proHeader.p_vaddr + proHeader.p_memsz;
		machine.memory[HEAP].endAddress = machine.memory[HEAP].startAddress + 0x10000000;	// 256 MB
		machine.memory[HEAP].readable = true;
		machine.memory[HEAP].writable = true;
		machine.memory[HEAP].executable = false;
		machine.memory[HEAP].memory = new byte[0x10000000];
		machine.memory[STACK].startAddress = Util.STACK_END;
		machine.memory[STACK].endAddress = Util.STACK_BEGIN;	// 8 MB
		machine.memory[STACK].readable = true;
		machine.memory[STACK].writable = true;
		machine.memory[STACK].executable = false;
		machine.memory[STACK].memory = new byte[(int)(Util.STACK_BEGIN-Util.STACK_END)];
		return 0;
	}

	public int initSymbol() {
		int symNum = -1;
		int symSize = -1;
    	byte[] strtab = new byte[2];
    	byte[] symtab = new byte[2];
    	int i, j;
    	for (i = 0; i < elfHeader.e_shnum; ++i) {
    		if (secHeader[i].name.equals(".strtab")) {
    			strtab = secHeader[i].contents;
    		}
    		if (secHeader[i].name.equals(".symtab")) {
    			symSize = (int)secHeader[i].sh_entsize;
    			symNum = (int)secHeader[i].sh_size / symSize;

    			symtab = secHeader[i].contents;
    			symTE = new Elf64_Sym[symNum];
    		}
    	}
    	if (strtab.length == 2 || symSize == -1)
    		return -1;

    	// read symTE,and allocate symbol space
    	int symbolNum = 0;
    	for (i = 0; i < symNum; ++i) {
    		symTE[i] = new Elf64_Sym();
    		symTE[i].read(symtab, i*symSize);
    		if (symTE[i].st_value > 0)
    			symbolNum++;
    	}
    	symbol = new Symbol[symbolNum];

    	// set symbols
    	int ret;
    	for (i = 0, j = 0; i < symNum; ++i) {
    		symbol[j] = new Symbol();
    		ret = symTE[i].setSymbol(symbol[j], strtab, secHeader);
    			// if this symbol is not in memory
    		if (ret == -1)
    			continue;
    			// if this symbol is the name of a section
    		if (symTE[i].st_info == 0x3)	// STT_SECTION = 0x3
    			symbol[j].name = secHeader[symTE[i].st_shndx].name;
    		++j;
    	}

    	machine.symbol = symbol;

    	return 0;
	}

	static boolean isRInstruction(int opcode, int funct7)
	{
		return 
			opcode == 0b0110011 ||
			opcode == 0b0111011 ||
			(opcode == 0b1010011 && (
				funct7 == 0b0010000 ||
				funct7 == 0b0010100));
	}

	static boolean isR4Instruction(int opcode)
	{
		return 
			opcode == 0b1000011 ||
			opcode == 0b1000111 ||
			opcode == 0b1001011 ||
			opcode == 0b1001111;
	}

	static boolean isS5Instruction(int opcode, int funct3)
	{
		return 
			opcode == 0b0011011 && (
				funct3 == 0b001 ||
				funct3 == 0b101);
	}

	static boolean isS6Instruction(int opcode, int funct3)
	{
		return 
			opcode == 0b0010011 && (
				funct3 == 0b001 ||
				funct3 == 0b101);
	}

	static boolean isIInstruction(int opcode, int funct3)
	{
		return 
			opcode == 0b1100111 ||
			opcode == 0b0000011 ||
			(opcode == 0b0010011 && (
				funct3 == 0b000 ||
				funct3 == 0b010 ||
				funct3 == 0b011 ||
				funct3 == 0b100 ||
				funct3 == 0b110 ||
				funct3 == 0b111)) ||
			(opcode == 0b0011011 && 
				funct3 == 0b000) ||
			opcode == 0b0000111;
	}

	static boolean isSInstruction(int opcode)
	{
		return 
			opcode == 0b0100011 ||
			opcode == 0b0100111;
	}

	static boolean isSBInstruction(int opcode)
	{
		return 
			opcode == 0b1100011;
	}

	static boolean isUInstruction(int opcode)
	{
		return 
			opcode == 0b0010111 ||
			opcode == 0b0110111;
	}

	static boolean isUJInstruction(int opcode)
	{
		return 
			opcode == 0b1101111;
	}

	static boolean isFZInstruction(int opcode)
	{
		return false;
	}

	/* use `parse` to open a ELF file and return it's standard output as an 
	 * InputStream which can be read directly. 
	 * on error, dump returns null.*/
	InputStream dump(String fileName)
	{
	    try
	    {
	        String osName = System.getProperties().getProperty("os.name");
	        String script = null;
	        if (osName.indexOf("Linux") != -1)
	        {
	            script = "./parse ";
	        }
	        else
	        {
	            script = "parse ";
	        }

	        script += fileName;

	        Process ps = Runtime.getRuntime().exec(script);
	        return ps.getInputStream();
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	        return null;
	    }
	}

	public int initInstruction(String fileName) {
		byte[] texts = machine.memory[RISCVMachine.SEGMENT_TEXT].memory;
		int num = texts.length / 4;
		int code;
		boolean hasInvalid = false;
		machine.instructions = new RISCVInstruction[num];
		String[] asm = new String[num];

		// read asm string from file
		InputStream is = dump(fileName);
		String buf;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			for(int i = 0; (buf = br.readLine())!=null; ++i)
				asm[i] = buf;
			br.close();
		} catch (IOException ie) {
			ie.printStackTrace();
			return -1;
		}

		// parse code
		for (int i = 0; i < num; ++i) {
			code = Util.byteArray2Int(texts, 4*i);

			// devide instructions by opcode
			int opcode = code & 0x7F;
			int funct3 = (code >> 12) & 0x7;
			int funct7 = (code >> 25) & 0x7F;
			if (isRInstruction(opcode, funct7))
				machine.instructions[i] = new RInstruction();
			else if (isS5Instruction(opcode, funct3))
				machine.instructions[i] = new S5Instruction();
			else if (isS6Instruction(opcode, funct3))
				machine.instructions[i] = new S6Instruction();
			else if (isR4Instruction(opcode))
				machine.instructions[i] = new R4Instruction();
			else if (isIInstruction(opcode, funct3))		// 32F
				machine.instructions[i] = new IInstruction();
			else if (isSInstruction(opcode))		// 32/64I, 32F
				machine.instructions[i] = new SInstruction();
			else if (isSBInstruction(opcode))
				machine.instructions[i] = new SBInstruction();
			else if (isUInstruction(opcode))
				machine.instructions[i] = new UInstruction();
			else if (isUJInstruction(opcode))
				machine.instructions[i] = new UJInstruction();
			else if (isFZInstruction(opcode))
				machine.instructions[i] = new FZInstruction();
			else {
				hasInvalid = true;
				machine.instructions[i] = new UnknownInstruction();
			}

			machine.instructions[i].code = code;
			machine.instructions[i].isBreakpoint = false;
			machine.instructions[i].asm = asm[i];
		}
		if (hasInvalid)
			return -1;
		else
			return 0;
	}

	public void initPC() {
		machine.programCounter = elfHeader.e_entry;
	}

}

class Elf64_Ehdr
{
	byte[] e_ident = new byte[16]; /* ELF魔数，ELF字长，字节序，ELF文件版本等 */
	short e_type; 		/*ELF文件类型，REL, 可执行文件，共享目标文件等 */
	short e_machine; 	/* ELF的CPU平台属性 */
	int e_version; 		/* ELF版本号 */
	long e_entry; 		/* ELF程序的入口虚拟地址，REL一般没有入口地址为0 */
	long e_phoff;
	long e_shoff; 		/* 段表在文件中的偏移 */
	int e_flags; 		/* 用于标识ELF文件平台相关的属性 */
	short e_ehsize; 	/* 本文件头的长度 */
	short e_phentsize; 
	short e_phnum;
	short e_shentsize; /* 段表描述符的大小 */
	short e_shnum; 		/* 段表描述符的数量 */
	short e_shstrndx; 	/* 段表字符串表所在的段在段表中的下标 */

	int read(InputStream is, byte[] buffer) {
		try {
			buffer = new byte[64];
            int count = 0;
 			if((count=is.read(buffer, 0, 64)) != -1) {
				e_type = Util.byteArray2Short(buffer, 16);
				e_machine = Util.byteArray2Short(buffer, 18);
				e_version = Util.byteArray2Int(buffer, 20);
				e_entry = Util.byteArray2Long(buffer, 24);
				e_phoff = Util.byteArray2Long(buffer, 32);
				e_shoff = Util.byteArray2Long(buffer, 40);
				e_flags = Util.byteArray2Int(buffer, 48);
				e_ehsize = Util.byteArray2Short(buffer, 52);
				e_phentsize = Util.byteArray2Short(buffer, 54);
				e_phnum = Util.byteArray2Short(buffer, 56);
				e_shentsize = Util.byteArray2Short(buffer, 58);
				e_shnum = Util.byteArray2Short(buffer, 60);
				e_shstrndx = Util.byteArray2Short(buffer, 62);
				System.err.printf(this.toString());
			}
			else {
				return -1;
			}
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		return 0;
	}

	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("----ELF HEADER----\n");
		str.append(String.format("e_type      : %x\n", e_type));
		str.append(String.format("e_machine   : %x\n", e_machine));
		str.append(String.format("e_version   : %x\n", e_version));
		str.append(String.format("e_entry     : %x\n", e_entry));
		str.append(String.format("e_phoff     : %x\n", e_phoff));
		str.append(String.format("e_shoff     : %x\n", e_shoff));
		str.append(String.format("e_flags     : %x\n", e_flags));
		str.append(String.format("e_ehsize    : %x\n", e_ehsize));
		str.append(String.format("e_phentsize : %x\n", e_phentsize));
		str.append(String.format("e_phnum     : %x\n", e_phnum));
		str.append(String.format("e_shentsize : %x\n", e_shentsize));
		str.append(String.format("e_shnum     : %x\n", e_shnum));
		str.append(String.format("e_shstrndx  : %x\n\n", e_shstrndx));
		return str.toString();
	}
}

class Elf64_Phdr
{
	int p_type;/*段类型*/
	int p_flags;/*标志*/
	long p_offset;/*在文件中的偏移*/
	long p_vaddr;/*执行时的虚地址*/
	long p_paddr;/*执行时的物理地址*/
	long p_filesz;/*在文件中的字节数*/
	long p_memsz;/*在内存中的字节数*/
	long p_align;/*字节对齐*/

	long offset = 0;

	int read(byte[] elfAllBytes, long offset) {
		byte[] buffer = new byte[56];
        System.arraycopy(elfAllBytes, (int)offset, buffer, 0, 56);
            
        p_type = Util.byteArray2Int(buffer, 0);
		p_flags = Util.byteArray2Int(buffer, 4);
		p_offset = Util.byteArray2Long(buffer, 8);
		p_vaddr = Util.byteArray2Long(buffer, 16);
		p_paddr = Util.byteArray2Long(buffer, 24);
		p_filesz = Util.byteArray2Long(buffer, 32);
		p_memsz = Util.byteArray2Int(buffer, 40);
		p_align = Util.byteArray2Short(buffer, 48);
		System.err.printf(this.toString());
		return 0;
	}

	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("----Program HEADER----\n");
		str.append(String.format("p_type      : %x\n", p_type));
		str.append(String.format("p_flags     : %x\n", p_flags));
		str.append(String.format("p_offset    : %x\n", p_offset));
		str.append(String.format("p_vaddr     : %x\n", p_vaddr));
		str.append(String.format("p_paddr     : %x\n", p_paddr));
		str.append(String.format("p_filesz    : %x\n", p_filesz));
		str.append(String.format("p_memsz     : %x\n", p_memsz));
		str.append(String.format("p_align     : %x\n\n", p_align));
		return str.toString();
	}
}

class Elf64_Shdr
{
	int sh_name;		/* Section name, string tbl index */
	int sh_type;		/* Section type */
	long sh_flags;	/* Section flags */
	long sh_addr;		/* Section virtual addr at execution */
	long sh_offset;	/* Section file offset */
	long sh_size;	/* Section size in bytes */
	int sh_link;		/* Link to another section */
	int sh_info;		/* Additional section information */
	long sh_addralign;	/* Section alignment */
	long sh_entsize;		/* Entry size if section holds table */

	String name;
	byte[] contents;

	int read(byte[] elfAllBytes, long offset) {
        byte[] buffer = new byte[64];
        System.arraycopy(elfAllBytes, (int)offset, buffer, 0, 64);

        sh_name = Util.byteArray2Int(buffer, 0);
		sh_type = Util.byteArray2Int(buffer, 4);
		sh_flags = Util.byteArray2Long(buffer, 8);
		sh_addr = Util.byteArray2Long(buffer, 16);
		sh_offset = Util.byteArray2Long(buffer, 24);
		sh_size = Util.byteArray2Long(buffer, 32);
		sh_link = Util.byteArray2Int(buffer, 40);
		sh_info = Util.byteArray2Int(buffer, 44);
		sh_addralign = Util.byteArray2Long(buffer, 48);
		sh_entsize = Util.byteArray2Long(buffer, 56);
	
		System.err.printf(this.toString());
		return 0;
	}

	void getName(byte[] shStrtab) {
		// get section name
		byte[] bName = new byte[20];
		int i;
		for (i = sh_name; shStrtab[i] > 0x0; ++i) {
			bName[i-sh_name] = shStrtab[i];
		}
		try {
			name = new String(bName, 0, i-sh_name, "UTF-8");
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}
	}

	void getContents(byte[] elfAllBytes) {
		if (sh_type != Util.SHT_NOBITS) {
			contents = new byte[(int)sh_size];
			System.arraycopy(elfAllBytes, (int)sh_offset, contents, 0, (int)sh_size);
		}
		else {
			contents = null;
		}
	}

	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("----Section HEADER----\n");
		str.append(String.format("sh_name     : %x\n", sh_name));
		str.append(String.format("sh_type     : %x\n", sh_type));
		str.append(String.format("sh_flags    : %x\n", sh_flags));
		str.append(String.format("sh_addr     : %x\n", sh_addr));
		str.append(String.format("sh_offset   : %x\n", sh_offset));
		str.append(String.format("sh_size     : %x\n", sh_size));
		str.append(String.format("sh_link     : %x\n", sh_link));
		str.append(String.format("sh_info     : %x\n", sh_info));
		str.append(String.format("sh_addralign: %x\n", sh_addralign));
		str.append(String.format("sh_entsize  : %x\n\n", sh_entsize));
		return str.toString();
	}
}

class Elf64_Sym
{
	static String[] SEG_NAMES = Util.NAMES_OF_MEMSEG;
 	int st_name;
	byte st_info;
	byte st_other;
	short st_shndx;
	long st_value;
	long st_size;

	int read(byte[] symtab, long offset) {
        byte[] buffer = new byte[24];
        System.arraycopy(symtab, (int)offset, buffer, 0, 24);

        st_name = Util.byteArray2Int(buffer, 0);
		st_info = buffer[4];
		st_other = buffer[5];
		st_shndx = Util.byteArray2Short(buffer, 6);
		st_value = Util.byteArray2Long(buffer, 8);
		st_size = Util.byteArray2Long(buffer, 16);
	
		//System.err.printf(this.toString());
		return 0;
	}

	int setSymbol(Symbol symbol, byte[] strtab, Elf64_Shdr[] secHdr) {
		// If st_value == 0, then it will not be loaded to memory.
		if (st_value == 0) {
			return -1;
		}
		// -----------SEGMENT-------------
		// normal segment
		int num = 0;
		if (st_shndx > 0 && st_shndx < secHdr.length) {
			String secName = secHdr[st_shndx].name;
			for (int i = 0; i < SEG_NAMES.length; ++i) {
				if (secName.equals(SEG_NAMES[i])) {
					num = i; break;
				}
			}
			symbol.segment = num;
		}
		// undefined
		else if (st_shndx == 0) {
			symbol.segment = 0;
		}
		// special
		else {
			symbol.segment = st_shndx;
		}

		// -----------ADDRESS-----------
		symbol.address = st_value;

		// -----------NAME-----------
		byte[] sName = new byte[64];
		int i;
		for (i = st_name; strtab[i] > 0x0; ++i) {
			sName[i-st_name] = strtab[i];
		}
		try {
			symbol.name = new String(sName, 0, i-st_name, "UTF-8");
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}	

		return 0;
	}

	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("----Symtab----\n");
		str.append(String.format("st_name     : %x\n", st_name));
		str.append(String.format("st_info     : %x\n", st_info));
		str.append(String.format("st_other    : %x\n", st_other));
		str.append(String.format("st_shndx    : %x\n", st_shndx));
		str.append(String.format("st_value    : %x\n", st_value));
		str.append(String.format("st_size     : %x\n", st_size));
		return str.toString();
	}
}


