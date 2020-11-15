rm -r build/ingestion/
mkdir build/ingestion/
javac src/ingestion/Main.java src/ingestion/DatabaseInteractions.java -d build/ingestion/
java -cp build/ingestion/:resources/postgresql-42.2.14.jar Main $1 $2 $3 $4 $5
