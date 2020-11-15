rm -r build/ingestion/
mkdir build/ingestion/
javac src/ingestion/Ing_Main.java src/ingestion/Ing_DatabaseInteractions.java -d build/ingestion/
java -cp build/ingestion/:resources/postgresql-42.2.14.jar Ing_Main $1 $2 $3 $4 $5
