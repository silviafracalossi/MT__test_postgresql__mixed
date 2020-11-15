rm -r build/querying/
mkdir build/querying/
javac src/querying/Q_Main.java src/querying/Q_DatabaseInteractions.java -d build/querying/
java -cp build/querying/:resources/postgresql-42.2.14.jar Q_Main $1 $2 $3 $4
