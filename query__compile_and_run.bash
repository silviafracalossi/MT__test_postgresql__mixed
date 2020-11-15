rm -r build/querying/
mkdir build/querying/
javac src/querying/Main.java src/querying/DatabaseInteractions.java -d build/querying/
java -cp build/querying/:resources/postgresql-42.2.14.jar Main $1 $2 $3 $4
