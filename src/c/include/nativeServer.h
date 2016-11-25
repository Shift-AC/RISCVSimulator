#include "csapp.h"
#include <sys/times.h>

#define SOCKET

#define min(a, b) ((a) < (b) ? (a) : (b))
#define max(a, b) ((a) > (b) ? (a) : (b))

void nativeInit(int port);
ssize_t getStream(char *arr);
void putStream(char *arr, int len);
void putLong(long val);
void putN(char *arr, int len);
void getLong(long *dest);
void getLine(char *arr, int max);
