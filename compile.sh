cd src
javac -d ../bin/ -encoding utf8 *.java
g++ parse.cpp -o ../bin/parse
cd syscallServer
make
mv ./syscallServer ../../bin/
cd ..
chmod 777 ../bin/parse
chmod 777 ../bin/syscallManager
cd ..
