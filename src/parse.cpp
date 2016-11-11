#include <cstdio>
#include <fstream>
#include <cstring>
#include <cstdlib>
#include <string>
#include <iostream>

using namespace std;

const int N = 1000000;
const int LL = 200;
char *objdumpIn;
const char objdumpOut[] = "ObjdumpOut";

string asmcode[N];
int asmtot = 0;

void Objdump()
{
	char syscmd[LL] = "./objdump -D ";
	strcat(syscmd, objdumpIn);
	strcat(syscmd, " > ");
	strcat(syscmd, objdumpOut);
	system(syscmd);	
}

void Extract()
{
	freopen(objdumpOut, "r", stdin);
	char line[LL];
	while(true)
	{
		fgets(line, LL, stdin);
		if (strstr(line, "section .text") != NULL) break;
	}
	while(true)
	{
		fgets(line, LL, stdin);
		if (strstr(line, "section .") != NULL) break;
		int len = strlen(line);
		char *pos = strstr(line, "          \t");
		if (pos != NULL)
		{
			++asmtot;
			for (char *i = pos + 11; i < line + len; ++i)
				asmcode[asmtot].push_back(*i);
		}
	}
	fclose(stdin);
}

void Output()
{
	for (int i = 1; i <= asmtot; ++i)
		cout << asmcode[i];
}

int main(int argc, char **argv)
{
	if (argc < 2) return 0;
	objdumpIn = argv[1];
	Objdump();
	Extract();
	Output();
	return 0;
}