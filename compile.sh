cd src
javac -d ../bin/ -encoding utf8 *.java
g++ parse.cpp -o ../bin/parse
g++ ecall.cpp -o ../bin/syscallManager
chmod 777 ../bin/parse
chmod 777 ../bin/syscallManager
cd ..
