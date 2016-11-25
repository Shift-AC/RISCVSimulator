#include "include/nativeServer.h"

#define MAXLINE 256

static int heapStAds = 0x8048000;
static char s[1000000];
static char buffer[10000000];
static int No, len;
static long startTime;
static long a0, a1, a2, a3;
static char *head = buffer, *tail = buffer;
static struct tms tmp;
struct timeval tv_begin, tv_end;

static const int listenPort = 2333;

static void parseSyscall();
void writeReturn(long ret);

int main(int argc, char **argv) 
{
    nativeInit(listenPort);

    while (1) 
    {
        char buf[MAXLINE];

        getLine(buf, MAXLINE);
        sscanf(buf, "%i%lli%lli%lli%lli", &No, &a0, &a1, &a2, &a3);
        parseSyscall();
    }
}

void parseSyscall()
{
    char lineBuf[256];
    switch (No)
    {
    case 214:
        writeReturn(heapStAds += a0);
        break;
    case 80:
        writeReturn(isatty(a0));
        break;
    case 57:
        writeReturn(close(a0));
        break;
    case 1024:
        s[getStream(s)] = 0;
        writeReturn(open(s, a1, a2));
        break;
    case 63:
        if (a0 == 0)
        {
            if (a2 == 0)
            {
                break;
            }
            if (tail == head)
            {
                getLine(lineBuf, MAXLINE);
                tail += getStream(tail);
            }
            int len;
            int i;
            len = min(a2, (long)(tail - head)); 
            for (i = 0; i < len; ++i)
                s[i] = head[i];
            head += len;
            writeReturn(len);
            putStream(s, len);
        }
        else
        {
            int ret = read(a0, s, a2);
            writeReturn(ret);
            putStream(s, ret);
        }
        break;
    case 64:
        getStream(s);
        writeReturn(write(a0, s, a2));
        break;
    case 62:
        writeReturn(lseek(a0, a1, a2));
        break;
    case 169:
        gettimeofday(&tv_end, NULL);
        writeReturn(times(&tmp) - startTime);
        tmp.tms_utime = (tv_end.tv_sec - tv_begin.tv_sec);
        putStream(&tmp, sizeof(struct tms) / 2);
        break;
    case 2147483647:
        startTime = times(&tmp);
        gettimeofday(&tv_begin, NULL);
        break;
    case 2147483646:
        tail += getStream(tail);
        break;
    case 2147483645:
        exit(0);
    case 2147483644:
        heapStAds = a0;
        break;
    }
}

void writeReturn(long ret)
{
    static char short_buf[MAXLINE];
    sprintf(short_buf, "%lld\n", ret);
    putN(short_buf, strlen(short_buf));
}