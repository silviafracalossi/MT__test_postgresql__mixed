# == Printing received params ([l/s] [table_name])
echo "==Printing selection=="
echo "Server or local: $1"
echo "Database Table Name: $2"
echo ""
#
# == Creating the log folder
echo "Creating folder..."
folder=$(date +'%Y-%m-%d__%H.%M.%S')
mkdir -m 777 -p "logs/${folder}"
mkdir -m 777 -p "logs/${folder}/extra"
#
# == Checking the data files to be used
local="l"
half_data="TEMPERATURE_HalfGB.csv"
full_data="TEMPERATURE_1GB.csv"
sleep_time="2h"
if [ "$1" = "$local" ]; then
    half_data="TEMPERATURE.csv"
    sleep_time="20s"
fi
#
# System variable
indexNames=("no" "timestamp" "timestamp_and_value")
#
# Iterating through indices
for index in 0 1 2
do
  #
  # == Executing N Ingestion ([M] [N] [l/s] [table_name] [file_name_in_data_folder])
  echo "Executing NDataIngestionTest.jar..."
  java -jar standalone/NDataIngestionTest.jar 300 300 $1 $2 ${half_data} > "logs/${folder}/extra/out__N_ingestion.txt"
  rm -r logs/2020*_300
  echo "Ingestion completed!"
  echo ""
  #
  # == Changing index ([l/s] [dbName] [0/1/2])
  echo "Set index to \"${indexNames[$index]}\""
  nohup bash index__compile_and_run.bash $1 $2 $index > "logs/${folder}/extra/out__index.txt" 2> "logs/${folder}/extra/err__index.txt" &
  # nohup java -jar standalone/IngestionMixed.jar $1 $2 $index > "logs/${folder}/extra/out__index.txt" 2> "logs/${folder}/extra/err__index.txt" &
  #
  # == Executing Ingestion Part ([l/s] [dbName] [data_file_name] [log_folder] [0/1/2])
  echo "Executing IngestionMixed.jar ..."
  nohup bash ing__compile_and_run.bash $1 $2 ${full_data} $folder $index > "logs/${folder}/extra/out__ingestion.txt" 2> "logs/${folder}/extra/err__ingestion.txt" &
  # nohup java -jar standalone/IngestionMixed.jar $1 $2 ${full_data} $folder $index > "logs/${folder}/extra/out__ingestion.txt" 2> "logs/${folder}/extra/err__ingestion.txt" &
  #
  # == Executing Querying Part ([l/s] [dbName] [log_folder] [0/1/2])
  echo "Executing QueryingMixed.jar ..."
  nohup bash query__compile_and_run.bash $1 $2 $folder $index > "logs/${folder}/extra/out__querying.txt" 2> "logs/${folder}/extra/err__querying.txt" &
  #nohup java -jar standalone/QueryingMixed.jar $1 $2 $folder $index > "logs/${folder}/extra/out__querying.txt" 2> "logs/${folder}/extra/err__querying.txt" &
  #
  # == Waiting 2 hours
  echo "Sleeping 2 hours..."
  sleep ${sleep_time}
  #
  # == Stopping the processes
  echo ""
  echo "Stopping processes..."
  kill $(ps | grep java | awk '{print $1;}')
  #
  # == Last fixes to output
  echo "Fixing some things..."
  echo "</logs>" >> "logs/${folder}/querying_${indexNames[$index]}.xml"
  echo "</logs>" >> "logs/${folder}/ingestion_${indexNames[$index]}.xml"
  #
  # == Completed!
  echo "Completed for this index!"
done
#
echo "Finished it all!"
