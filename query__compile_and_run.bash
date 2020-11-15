rm -r build/
mkdir build/
javac src/querying/Main.java src/querying/DatabaseInteractions.java -d build/
java -cp build/:resources/postgresql-42.2.14.jar Main $1 $2 $3 $4
