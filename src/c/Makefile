TARGET = .
CC = gcc
CPP = g++
CFLAGS = -std=c99 -O2 -I . -w
CPPFLAGS = -O2 -I . -w	
PROG = csapp.o nativeServer.o parse syscallServer # cacheServer

# This flag includes the Pthreads library on a Linux box.
# Others systems will probably require something different.
LIB = -lpthread

.PHONY: all
all: $(PROG)

#cacheServer: cacheServer.cpp csapp.o

syscallServer: syscallServer.c nativeServer.o csapp.o
	$(CC) $(CFLAGS) -o $(TARGET)/syscallServer syscallServer.c nativeServer.o csapp.o $(LIB)

nativeServer.o: nativeServer.c csapp.o
	$(CC) $(CFLAGS) -c nativeServer.c csapp.o

csapp.o: csapp.c
	$(CC) $(CFLAGS) -c csapp.c

parse: parse.cpp
	$(CPP) $(CPPFLAGS) -o $(TARGET)/parse parse.cpp

.PHONY: clean
clean:
	-rm -f *.o $(PROG) *~

