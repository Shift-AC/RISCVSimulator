#include "include/nativeServer.h"

unsigned long hostPort = 23333;
char traceName[] = "tmp/trace.txt";

class CacheCell
{
public:
	unsigned long valid, tag, prio, dirty;

	CacheCell()
	{
		valid = tag = prio = dirty = 0;
	}
	CacheCell(unsigned long validInit, unsigned long tagInit, unsigned long prioInit, unsigned long dirtyInit)
	{
		valid = validInit; tag = tagInit;
		prio = prioInit; dirty = dirtyInit;
	}
};

class CacheLine
{
public:
	CacheCell *cell;
	unsigned long cellCnt;

	CacheLine() {}
	void CacheLineInit(unsigned long setAsso)
	{
		cellCnt = setAsso;
		cell = new CacheCell[cellCnt];
	}
	void ModifyPrio(unsigned long p)
	{
		for (unsigned long i = 0; i < cellCnt; ++i)
			if (cell[i].prio <= cell[p].prio) ++cell[i].prio;
		cell[p].prio = 0;
	}
	unsigned long Find(unsigned long tag)
	{
		for (unsigned long i = 0; i < cellCnt; ++i)
			if (cell[i].valid && cell[i].tag == tag)
			{
				ModifyPrio(i);
				return i;
			}
		return -1;
	}
	unsigned long FindReplacement()
	{
		unsigned long p, maxm = -1;
		for (unsigned long i = 0; i < cellCnt; ++i)
			if (!cell[i].valid) return i;
			else if (cell[i].prio > maxm) { p = i; maxm = cell[i].prio; }
			return p;
	}
};

class CacheLevel
{
public:
	unsigned long size, setAsso, blockSz, lineCnt;
	unsigned long readMiss, writeMiss, readCount, writeCount;
	bool isWriteBack, isWriteAlloc, isMem;
	CacheLine *line;
	CacheLevel *nextL;

	CacheLevel() { }
	void CacheLevelInit(unsigned long sizeInit, unsigned long setAssoInit, unsigned long blockSzInit,
		bool isWBInit, bool isWAInit, bool isMemInit)
	{
		size = sizeInit; setAsso = setAssoInit; blockSz = blockSzInit; 
		isWriteBack = isWBInit; isWriteAlloc = isWAInit; isMem = isMemInit;
		lineCnt = size / blockSz / setAsso;
		line = new CacheLine[lineCnt];
		for (unsigned long i = 0; i < lineCnt; ++i) line[i].CacheLineInit(setAsso);
		readMiss = 0;
		writeMiss = 0;
		readCount = 0;
		writeCount = 0;
	}

	unsigned long ext(unsigned long addr) 
	{
		//fprintf(stderr, "%d %d", blockSz, lineCnt);
		return addr / blockSz % lineCnt;
	}
	unsigned long tag(unsigned long addr)
	{
		return addr / blockSz / lineCnt;
	}
	unsigned long Fetch(CacheLine *p, unsigned long addr);
	void Read(unsigned long addr);
	void Write(unsigned long addr);
};

unsigned long CacheLevel::Fetch(CacheLine *p, unsigned long addr)
{
	nextL->Read(addr);
	unsigned long rpln = p->FindReplacement();
	CacheCell *rplCell = &(p->cell[rpln]);
	if (rplCell->valid && rplCell->dirty)
	{
		unsigned long stAddr = rplCell->tag * blockSz * lineCnt + ext(addr) * blockSz;
		nextL->Write(stAddr);
	}
	rplCell->valid = 1; rplCell->tag = tag(addr); rplCell->dirty = 0;
	p->ModifyPrio(rpln);
	return rpln;
}

void CacheLevel::Write(unsigned long addr)
{	
	if (isMem) return;
	++writeCount;
	CacheLine *p = &line[ext(addr)];
	unsigned long pos = p->Find(tag(addr));
	if (pos != -1)
	{
		if (isWriteBack) p->cell[pos].dirty = 1;
		else nextL->Write(addr);
	}
	else
	{
		++writeMiss;
		if (isWriteAlloc)
		{
			pos = Fetch(p, addr);
			if (isWriteBack) p->cell[pos].dirty = 1;
			else nextL->Write(addr);
		}
		else nextL->Write(addr);
	}
}

void CacheLevel::Read(unsigned long addr)
{
	if (isMem) return;
	++readCount;
	CacheLine *p = &line[ext(addr)];
	if (p->Find(tag(addr)) != -1) return;
	else
	{
		++readMiss;
		Fetch(p, addr);
	}
}

class Cache
{
public:
	unsigned long levelCnt;
	CacheLevel *level;

	Cache() { }
	void CacheInit(unsigned long levelInit)
	{
		levelCnt = levelInit;
		level = new CacheLevel[levelCnt + 1];
		unsigned long size, setAsso, blockSz, isWriteBack, isWriteAlloc;
		for (unsigned long i = 0; i < levelCnt; ++i)
		{
			char c;
			long tmp;
			getLong(&tmp);
			size = tmp;
			getLong(&tmp);
			setAsso = tmp;
			getLong(&tmp);
			blockSz = tmp;
			getLong(&tmp);
			isWriteBack = tmp;
			getLong(&tmp);
			isWriteAlloc = tmp;
			level[i].CacheLevelInit(size, setAsso, blockSz, isWriteBack, isWriteAlloc, false);
			level[i].nextL = &level[i + 1];
		}
		level[levelCnt].isMem = true;
	}
	void Read(unsigned long addr)
	{
		level[0].Read(addr);
	}
	void Write(unsigned long addr)
	{
		level[0].Write(addr);
	}
};

Cache c;

void Init()
{
	nativeInit(hostPort);

	long level;
	getLong(&level);
	c.CacheInit(level);
}

void putResult()
{
	static char outputBuf[256];
	
	sprintf(outputBuf, "%d\n", c.levelCnt);
	putN(outputBuf, strlen(outputBuf));

	for (unsigned long i = 0; i < c.levelCnt; ++i)
	{
		CacheLevel layer = c.level[i];
		sprintf(outputBuf, "%ld %ld %ld %ld\n", 
			layer.readCount, layer.readMiss, layer.writeCount, layer.writeMiss);
		putN(outputBuf, strlen(outputBuf));
	}
	exit(0);
}

void Query()
{
	char ch; unsigned long addr;
	FILE *is = fopen(traceName, "r");
	while (fscanf(is, "%c%d", &ch, &addr) > 0)
	{
		switch (ch)
		{
		case 'r':
			c.Read(addr);
			break;
		case 'w':
			c.Write(addr);
			break;
		}
	}
	fclose(is);
}

void Operate()
{
	char ch;
	unsigned long addr;
	while (1)
	{
		getChar(&ch);

		switch (ch)
		{
		case 'q':
			exit(0);
		case 'p':
			Query();
			break;
		case 'g':
			fflush(stdout);
			putResult();
			break;
		}
	}
}

int main()
{
	Init();
	Operate();
}

