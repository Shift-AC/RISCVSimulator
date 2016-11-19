// Modified Code
// Source: ICS Lab8(proxy lab) handout

#include <sys/times.h>
#include "csapp.h"

#define LONG_BUF_LEN 4096
#define MAXLINE 256
//#define USE_STDOUT

static char short_buf[256];
static char long_buf[LONG_BUF_LEN];

#define min(a, b) ((a) < (b) ? (a) : (b))
void doit(int fd);
void parse_syscall(int fd, rio_t *prio);
ssize_t read_byte_stream(int fd, rio_t *prio, char *arr);
void write_byte_stream(int fd, rio_t *prio, char *arr, int len);
#ifndef USE_STDOUT
#define write_short_string(fd, prio, format, ...)\
do\
{\
    sprintf(short_buf, format, __VA_ARGS__);\
    Rio_writen(fd, short_buf, strlen(short_buf));\
}\
while (0)
#endif
void write_return(int fd, rio_t *prio, long long ret);

static int heapStAds = 0x8048000;
static char s[1000000];
static char buffer[10000000] = "hello world";
static int No, len;
static long long startTime;
static long long a0, a1, a2, a3;
static char *head = buffer, *tail = buffer + 20;
static struct tms tmp;

static const int listenPort = 2333;

int main(int argc, char **argv) 
{
    int listenfd, connfd, port, clientlen;
    struct sockaddr_in clientaddr;

    port = listenPort;

    listenfd = Open_listenfd(port);
    clientlen = sizeof(clientaddr);
	connfd = Accept(listenfd, (SA *)&clientaddr, &clientlen);

    while (1) 
    {
	    doit(connfd);
    }
}

void doit(int fd) 
{
    char buf[MAXLINE];
    rio_t rio;
  
    /* Read syscall parameters */
    Rio_readinitb(&rio, fd);
    Rio_readlineb(&rio, buf, MAXLINE);
    sscanf(buf, "%i%lli%lli%lli%lli", &No, &a0, &a1, &a2, &a3);

    parse_syscall(fd, &rio);
}

void parse_syscall(int fd, rio_t *prio)
{
    switch (No)
    {
    case 214:
        write_return(fd, prio, heapStAds += a0);
        break;
    case 80:
        write_return(fd, prio, isatty(a0));
        break;
    case 57:
        write_return(fd, prio, close(a0));
        break;
    case 1024:
        read_byte_stream(fd, prio, s);
	write_return(fd, prio, open(s, O_RDONLY, 0x1FF));        
//write_return(fd, prio, open(s, a1, a2));
        break;
    case 63:
        if (a0 == 0)
        {
            int len;
            len = min(a2, (long long)(tail - head)); 				
            for (int i = 0; i < len; ++i)
                s[i] = head[i];
            head += len;
            write_return(fd, prio, len);
            write_byte_stream(fd, prio, s, len);
        }
        else
        {
            int ret = read(a0, s, a2);
            write_return(fd, prio, ret);
            write_byte_stream(fd, prio, s, ret);
        }
        break;
    case 64:
        read_byte_stream(fd, prio, s);
        write_return(fd, prio, write(a0, s, a2));
        break;
    case 62:
        write_return(fd, prio, lseek(a0, a1, a2));
        break;
    case 169:
        write_return(fd, prio, times(&tmp) - startTime);
        break;
    case 2147483647:
        startTime = times(&tmp);
        break;
    case 2147483646:
        tail += read_byte_stream(fd, prio, tail);
        break;
    case 2147483645:
        exit(0);
    }
}

ssize_t read_byte_stream(int fd, rio_t *prio, char *arr)
{
    int len = 0;
    char num;
    for (Rio_readnb(prio, &num, 1); num != ' '; Rio_readnb(prio, &num, 1))
    {
        len = len * 10 + (num - '0');
    }

    return Rio_readnb(prio, arr, 2);
}

void write_byte_stream(int fd, rio_t *prio, char *arr, int len)
{
#ifdef USE_STDOUT
    printf("%d %s", len, arr);
#else
    char prefix[16];
    sprintf(prefix, "%d ", len);
    Rio_writen(fd, prefix, strlen(prefix));
    Rio_writen(fd, arr, len);
#endif
}

void write_return(int fd, rio_t *prio, long long ret)
{
#ifdef USE_STDOUT
    printf("%d\n", ret);
#else
    write_short_string(fd, prio, "%lld\n", ret);
#endif
}
