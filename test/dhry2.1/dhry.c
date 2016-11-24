#include "dhry.h"

int strlen(const char *src)
{
    int len = 0;
    while (src[len++]);
    return len - 1;
}

char *strcpy(char *dest, char *src)
{
    int len = 0;
    int i = 0;

    len = strlen(src);
    for (; i < len; ++i)
    {
        dest[i] = src[i];
    }
    dest[len] = 0;

    return dest;
}

int strcmp(const char *s1, const char *s2)
{
    while (*s1 && *s2)
    {
        int sub = *s1 - *s2;
        if (sub != 0)
        {
            return sub;
        }
	++s1, ++s2;
    }
    if (!(*s1 || *s2))
    {
        return 0;
    }
    else
    {
        return *s1 ? -1 : 1;
    }
}

static char buffer[15] = ">>";
static int ind = 2;
void generatenumstring(const int dnum)
{
    int num = dnum;
    int flag = num < 0;
    if (num < 0)
    {
        num = -num;
    }

    buffer[15] = 0;
    ind = 14;
    do
    {
        buffer[ind--] = num % 10 + 48;
        num /= 10;
    }
    while (num != 0);

    if (flag)
    {
        buffer[ind--] = '-';
    }
}

void printnum(const int num)
{
    generatenumstring(num);

    write(1, buffer + ind + 1, 14 - ind);
}

void printstring(const char *x)
{
    write(1, x, strlen(x));
}

void printchar(const char x)
{
    write(1, &x, 1);
}

void printnewline()
{
    printchar('\n');
}

int isnum(const char c)
{
    return c < 58 && c > 47;
}

static char readbuf[100];
void readnum(int *x)
{
    int i = 0;
    int res = 0;
    read(0, readbuf, 100);
    for (; isnum(readbuf[i]); ++i)
    {
        res = res * 10 + readbuf[i] - 48;
    }
    *x = res;
}
