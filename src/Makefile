TARGET = ../../bin

.PHONY: all
all:
	make TARGET=$(TARGET) -C java	
	make TARGET=$(TARGET) SOCKET=-DSOCKET -C c
.PHONY: clean
clean:
	-rm *~; 
	make clean -C java
	make clean -C c
