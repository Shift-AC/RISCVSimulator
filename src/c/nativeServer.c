#include "include/nativeServer.h"

static inline int isWhiteSpace(char c)
{
    return c == ' ' || c == '\t' || c == '\r' || c == '\n';
}

// if we use socket
#ifdef SOCKET

#define MAXLINE 256

static rio_t rio;
static int fd;

void nativeInit(int port)
{
    struct sockaddr_in clientaddr;
    socklen_t clientlen;
    int listenfd;

    listenfd = Open_listenfd(port);
    clientlen = sizeof(clientaddr);
    fd = Accept(listenfd, (SA *)&clientaddr, &clientlen);
    Rio_readinitb(&rio, fd);
}

ssize_t getStream(char *arr)
{
    long len;
    getLong(&len);
    return Rio_readnb(&rio, arr, len);
}

void putStream(char *arr, int len)
{
    static char space = ' ';
    if (len <= 0)
    {
        len = 0;
    }
    putLong(len);
    if (len > 0)
    {
        Rio_writen(fd, &space, 1);
        Rio_writen(fd, arr, len);
    }
}

void putLong(long val)
{
    char buf[22];
    sprintf(buf, "%ld", val);
    Rio_writen(fd, buf, strlen(buf));
}

void putN(char *arr, int len)
{
    Rio_writen(fd, arr, len);
}

void getLong(long *dest)
{
    int res = 0;
    char num;
    for (Rio_readnb(&rio, &num, 1); 
        !isWhiteSpace(num); Rio_readnb(&rio, &num, 1))
    {
        res = res * 10 + (num - '0');
    }
    *dest = res;
}

void getChar(char *dest)
{
    Rio_readnb(&rio, dest, 1);
}

void getLine(char *arr, size_t max)
{
    Rio_readlineb(&rio, arr, max);
}

// if we use stdin and stdout
#else
void nativeInit(int port)
{
    ;
}

ssize_t getStream(char *arr)
{
    long len;
    getLong(&len);
    return read(0, arr, (int)len);
}

void putStream(char *arr, int len)
{
    putLong(len);
    write(1, arr, len);
}

void putLong(long val)
{
    printf("%ld ", val);
    fflush(stdout);
}

void putN(char *arr, int len)
{
    write(1, arr, len);
}

void getLong(long *dest)
{
    long res = 0;
    char num;
    for (read(0, &num, 1); !isWhiteSpace(num); read(0, &num, 1))
    {
        res = res * 10 + (num - '0');
    }
    *dest = res;
}

void getChar(char *dest)
{
    *dest = getchar();
}

void getLine(char *arr, size_t max)
{
    getline(&arr, &max, stdout);
}
#endif
