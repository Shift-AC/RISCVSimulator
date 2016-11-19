mkdir bin
mkdir ref
mkdir test
mkdir tmp
mkdir tools
echo "git will ignore files in 'test/', 'tmp/', 'bin/', and 'ref/'."
echo "Feel free to use these folders."
echo "Recommended usage of the folders:"
echo   "bin/: store the .class file only."
echo   "ref/: store the references you need."
echo   "tmp/: store temporary files that the program will generate when running."
echo   "test/: test your modules here!"
chmod 777 ./*.sh
