rm -r build/index/
mkdir build/index/
javac src/index/Main.java -d build/index/
java -cp build/index/:resources/postgresql-42.2.14.jar Main $1 $2 $3
