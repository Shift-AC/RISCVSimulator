#include "csapp.h"
#include <sys/times.h>

//#define SOCKET

#define min(a, b) ((a) < (b) ? (a) : (b))
#define max(a, b) ((a) > (b) ? (a) : (b))

static long tmpLong;

void nativeInit(int port);
ssize_t getStream(char *arr);
void putStream(char *arr, int len);
void putLong(long val);
void putN(char *arr, int len);
void getLong(long *dest);
inline void getInt(int *dest)
{
    getLong(&tmpLong);
    *dest = tmpLong;
}
void getChar(char *dest);
void getLine(char *arr, size_t max);
