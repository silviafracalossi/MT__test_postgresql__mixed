rm -r build/index/
mkdir build/index/
javac src/index/Index_Main.java -d build/index/
java -cp build/index/:resources/postgresql-42.2.14.jar Index_Main $1 $2 $3
