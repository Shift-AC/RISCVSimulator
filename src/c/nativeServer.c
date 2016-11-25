#include "include/nativeServer.h"

// if we use socket
#ifdef SOCKET

#define MAXLINE 256

static rio_t rio;
static int fd;

void nativeInit(int port)
{
    struct sockaddr_in clientaddr;
    int clientlen, listenfd;

    listenfd = Open_listenfd(port);
    clientlen = sizeof(clientaddr);
    fd = Accept(listenfd, (SA *)&clientaddr, &clientlen);
    Rio_readinitb(&rio, fd);
}

ssize_t getStream(char *arr)
{
    int len = 0;
    char num;
    for (Rio_readnb(&rio, &num, 1); num != ' '; Rio_readnb(&rio, &num, 1))
    {
        if (num == '\n')
        {
            continue;
        }
        len = len * 10 + (num - '0');
    }

    return Rio_readnb(&rio, arr, len);
}

void putStream(char *arr, int len)
{
    static const char space = ' '; 
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

void getLine(char *arr, int max)
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
    int len = 0;
    char num;
    for (read(0, &num, 1); num != ' '; read(0, &num, 1))
    {
        if (num == '\n')
        {
            continue;
        }
        len = len * 10 + (num - '0');
    }
    
    return read(0, arr, len);
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

void getLine(char *arr, int max)
{
    getline(&arr, &max, stdout);
}
#endif