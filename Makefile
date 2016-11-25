.PHONY: all
all:
	make -C src

.PHONY: init
init:
	-@mkdir bin
	-@mkdir ref
	-@mkdir tmp
	-@mkdir tools
	-@chmod 777 tools/objdump
	-@echo "git will ignore files in 'test/', 'tmp/', 'bin/', and 'ref/'."
	-@echo "Feel free to use these folders."
	-@echo "Recommended usage of the folders:"
	-@echo   "bin/: store the .class file only."
	-@echo   "ref/: store the references you need."
	-@echo   "tmp/: store temporary files that the program will generate when running."

.PHONY: clean
clean:
	-@rm -R bin
	-@mkdir bin
	make clean -C src
