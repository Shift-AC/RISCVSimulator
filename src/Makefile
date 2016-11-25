TARGET = ../../bin

.PHONY: all
all:
	make TARGET=$(TARGET) -C java	
	make TARGET=$(TARGET) -C c
.PHONY: clean
clean:
	make clean -C java
	make clean -C c
