CC = gcc
CFLAGS = -std=c99 -O2 -I . -w

# This flag includes the Pthreads library on a Linux box.
# Others systems will probably require something different.
LIB = -lpthread

syscallServer: syscallServer.c csapp.o
	$(CC) $(CFLAGS) -o syscallServer syscallServer.c csapp.o $(LIB)

csapp.o:
	$(CC) $(CFLAGS) -c csapp.c

clean:
	rm -f *.o syscallServer *~
	(cd cgi-bin; make clean)

