import java.io.*;
import java.sql.*;
import java.util.Scanner;
import java.util.logging.*;
import java.text.*;

public class Ing_DatabaseInteractions {

  // DB variables
  static Connection pos_conn;
  static Statement pos_stmt;
  static String dbName;

  // Location of file containing data
  String data_file_path;

  // Defining date format
  static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");


  // Class that handles the insertion configurations
  public Ing_DatabaseInteractions (String dbName, String data_file_path) {
    this.dbName=dbName;
    this.data_file_path=data_file_path;
  }

  // Inserting one tuple at a time
  public void insertOneTuple(Logger logger) {

    // Defining variables useful for method
    String[] fields;
    String row;

    // Timestamp variables
    java.util.Date parsedDate;
    Timestamp timestamp;

    try {

      // Preparing file scanner
      Scanner reader = new Scanner(new File(data_file_path));

      // Defining variables for the insertion
      String insertion_query = "INSERT INTO "+dbName+" (time, value) VALUES (?, ?)";
      PreparedStatement pst = pos_conn.prepareStatement(insertion_query);

      // Signaling start of test
      logger.info("--Start of test--");
      while (reader.hasNextLine()) {

        // Retrieving the data and preparing insertion script
        row = reader.nextLine();
        fields = row.split(",");

        // Casting timestamp
        parsedDate = dateFormat.parse((String)fields[0]);
        timestamp = new Timestamp(parsedDate.getTime());

        // Inserting the variables in the prepared statement
        pst.setTimestamp(1, timestamp);
        pst.setInt(2, Integer.parseInt(fields[1]));

        // Executing the query and checking the result
        if (pst.executeUpdate() != 1) {
            logger.severe("Problem executing the query\n");
        } else {
            logger.info("Query successfully executed: ("+fields[0]+","+fields[1]+")\n");
        }
      }

      // Closing the file reader
      reader.close();

    } catch (FileNotFoundException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
      logger.severe("Insertion: \"One tuple at a time\" - problems with the execution");
    } catch (SQLException e) {
      logger.severe("Problem executing the script\n");
      e.printStackTrace();
    } catch (ParseException e) {
      logger.severe("Problem with parsing a timestamp\n");
      e.printStackTrace();
    }
  }

  //-----------------------UTILITY----------------------------------------------

  // Connecting to the PostgreSQL database
  public static void createDBConnection(String url) {
    try {
      pos_conn = DriverManager.getConnection(url);
      pos_stmt = pos_conn.createStatement();
    } catch (SQLException e) {
      System.out.println("Problems with creating the database connection");
      e.printStackTrace();
    }
  }

  // Closing the connections to the database
  public static void closeDBConnection() {
    try{
       if(pos_stmt!=null) pos_stmt.close();
    } catch(SQLException se2) {
        se2.printStackTrace();
    }
    try {
       if(pos_conn!=null) pos_conn.close();
    } catch(SQLException se){
       se.printStackTrace();
    }

    // Nulling the database variables
    pos_conn = null;
    pos_stmt = null;
  }
}
