#include <unistd.h>
#include <ctime>
#include <cstdio>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/times.h>
#include <fcntl.h>
#include <cstring>

int heapStAds = 0;
char s[1000000];
int No, len, startTime;
long long a0, a1, a2, a3;
struct tms tmp;

int main()
{
	while (1)
	{
		scanf("%i%lli%lli%lli%lli", &No, &a0, &a1, &a2, &a3);
		if (No == 214) printf("%i ", heapStAds += a0);     			//sbrk
		if (No == 80) printf("%i ", isatty(a0));	
		if (No == 57) printf("%i ", close(a0));
		if (No == 1024) 
		{
			scanf("%i ", &len);
			fgets(s, len, stdin);// getchar();
			printf("%i ", open(s, a1, a2));
		}
		if (No == 63)
		{
			int ret = read(a0, s, a2);
			printf("%i %s ", ret, s);
		}
		if (No == 64)
		{
			scanf("%i ", &len); 
			fgets(s, len, stdin);// getchar();
			printf("%li ", write(a0, s, a2));
		}
		if (No == 62) printf("%li ", lseek(a0, a1, a2));
		if (No == 169) 
		{
			int endTime = times(&tmp);
			printf("%lf\n ", (double) (endTime - startTime)/CLOCKS_PER_SEC);
		}
		if (No == 2147483647)
		{
			startTime = times(&tmp);
		}
	}
	return 0;
}
