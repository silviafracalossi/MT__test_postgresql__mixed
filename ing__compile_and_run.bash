rm -r build/
mkdir build/
javac src/ingestion/Main.java src/ingestion/DatabaseInteractions.java -d build/
java -cp build/:resources/postgresql-42.2.14.jar Main $1 $2 $3 $4 $5
