/Config/RISCVInstruction: 
	Configs in this directory store the signals for each instruction through its execution. The following will express the meaning of numbers for every signal.

	/******************** ATTENTION!! **********************/
	A config file must define all of following signals, otherwise the simulator probably would not work correctly!


needXXX:
	0- this instruction does not need to enter XXX CombineLogic block,
	1- this instruction needs to enter XXX CombineLogic block.

grLength:
	The length (by Byte) of data needed from general register.
	The only valid values are 1,2,4,8 in RISCV (64-bit machine).

iALUSetCnd:
	Only for "set" instructions. Decide when the rd register should be set to 1.
	The only valid values are 0~7 (0bxxx, or the simulator will do "mod 8" automatically), the three bits means, respectively, rd should be set to 1 when R[rs1] is less/equal/greater than R[rs2]. For example, 0b100 means only when R[rs1] < R[rs2].
iALUA:
	Decide which data should be the first operand of integerALU.
	0- ival1,
	1- ival1 & 0xFFFFFFFF,
	2- pc.
iALUB:
	Decide which data should be the second operand of integerALU.
	0- ival2,
	1- ival2 & 0xFFFFFFFF,
	2- imm,
	3- shamt,
	4- 4.
iALUOp:
	Decide which operation should be done in integerALU.
	0- add,
	1- sub,
	11-subU,
	2- mul,
	21-mulU,
	3- div,
	31-divU,
	4- rem,
	41-remU,
	5- and,
	6- or,
	7- xor,
	100- sll,
	101- srl,
	102- sra.
iALUIsUnsigned:
	Decide if integerALU operates unsigned data.
	0- signed,
	1- unsigned,
	2- signed for aluA, unsigned for aluB.
iALUIsHigh:
	Decide if integerALU outputs high-bit part of the result.
	0- low,
	1- high.
iALULength:
	Decide the length of data to be calculated in integerALU.
	4- word,
	8- long.

fALUSetCnd:
	Only for "fset" instructions. Decide when the rd register should be set to 1.
	The only valid values are 0~7 (0bxxx), the three bits means, respectively, rd should be set to 1 when R[rs1] is less/equal/greater than R[rs2]. For example, 0b100 means only when R[rs1] < R[rs2].
fALUA:
	Decide which data should be the first operand of floatALU.
	0- fval1,				// float-type float from floatRegister
	1- val1,				// int-type float from floatRegister
	2- val1 & 0x7FFFFFFF,
	3- ival1.				// long-type integer from generalRegister
fALUB:
	Decide which data should be the second operand of floatALU.
	0- fval2,				// float-type float from floatRegister
	1- val2,				// int-type float from floatRegister
	2- val2 & 0x80000000,
	3- ~val2 & 0x80000000.
fALUC:
	0- fval3.
fALUOp:
	0- add,
	1- sub,
	2- mul,
	3- div,
	5- and,
	6- or,
	7- xor,
	8- sqrt,
	22- madd,
	23- msub,
	24- nmadd,
	25- nmsub,
	103- fcmp,
	104- fclass,
	110- i2f,
	111- iu2f,
	112- l2f,
	113- lu2f,
	114- f2i,
	115- f2iu,
	116- f2l,
	117- f2lu,
	118- mv.x.s, 			// FMV.X.S
	119- mv.s.x. 			// FMV.S.X
	120- fsgn,
	121- fsgnjn,
	122- fsgnjx,
	123- fmin,
	124- fmax.


mAddr:
	Address to read/write memory.
	0- R[rs1]+imm.(that is, ivalE)
mData:
	Data to write to memory.
	0- ivalE,
	1- fvalE,
	2- ival2,
	3- fval2.
mLength:
	Data length to read from/ write to memory.
	The only valid values are 1,2,4,8 in RISCV (64-bit machine).
mIsUnsigned:
	Decide whether we read an unsigned from memory.
	0- signed,
	1- unsigned.

grData:
	Data to be writen to generalRegister GR[rd].
	0- ivalE,(produced by iALU)
	1- valM,
	2- imm,
	3- SignExtended(imm)
	4- cnd,(for Set instructions)
	5- fivalE.(produced by fALU)
frData:
	Data to be writen to floatRegister FR[rd].
	0- fvalE,
	1- valM.

pcSrc:
	Invalid when pcCnd is 1.
	0- pc+4,
	1- pc+imm,
	2- R[rs1]+imm.(and set the LSB to zero.)
pcCnd:
	Decide whether we will take "cnd" into account.
	0- unconditional jump,
	1- if (cnd == 1) then pc+imm, else pc+4.
	