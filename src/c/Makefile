TARGET = .
CC = gcc
CPP = g++
CFLAGS = -std=c99 -O2 -I . -w
CPPFLAGS = -O2 -I . -w	
PROG = parse syscallServer cacheServer
SOCKET =

# This flag includes the Pthreads library on a Linux box.
# Others systems will probably require something different.
LIB = -lpthread

.PHONY: all
all: $(PROG)

cacheServer: cacheServer.cpp nativeServer.c csapp.c
	$(CPP) $(CPPFLAGS) $(SOCKET) -o $(TARGET)/cacheServer cacheServer.cpp nativeServer.c csapp.c $(LIB)

syscallServer: syscallServer.c nativeServer.c csapp.o
	$(CC) $(CFLAGS) $(SOCKET) -o $(TARGET)/syscallServer syscallServer.c nativeServer.c csapp.o $(LIB)

csapp.o: csapp.c
	$(CC) $(CFLAGS) -c csapp.c

parse: parse.cpp
	$(CPP) $(CPPFLAGS) -o $(TARGET)/parse parse.cpp

.PHONY: clean
clean:
	-rm -f *.o $(PROG) *~

