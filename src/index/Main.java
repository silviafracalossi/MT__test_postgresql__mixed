import java.io.*;
import java.sql.*;
import java.util.Scanner;
import java.util.logging.*;
import java.sql.Timestamp;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

  // DB variables
  static Connection pos_conn;
  static Statement pos_stmt;

  // Store users' configurations - default settings written here
  static boolean useServerPostgresDB = false;
  static String dbName;

  // Index chosen
  static int index_no = -1;
  static String[] index_types = {"no", "timestamp", "timestamp_and_value"};

  // LOCAL Configurations
  static final String DB_PREFIX = "jdbc:postgresql://";
  static final String local_DB_HOST = "localhost";
  static final String local_DB_NAME = "thesis_data_ingestion";
  static final String local_DB_USER = "postgres";
  static final String local_DB_PASS = "silvia";

  // Configurations to server PostgreSQL database
  static final String DB_HOST = "ironmaiden.inf.unibz.it";
  static final int DB_PORT = 5433;
  static final String DB_NAME = "sfracalossi";
  static String DB_USER;
  static String DB_PASS;


  public static void main(String[] args) {

    // Checking if the inserted parameters are enough
    if (args.length != 3) {
        System.out.println("Please, insert args: "+args.length+"/3");
        return;
    }

    // Getting what the user inserted ([l/s] [dbName] [0/1/2])
    getInfo(args);

    // In case no index has to be applied
    if (index_no == 0) {
      System.out.println("Index: \""+index_types[index_no]+"\" applied");
      return;
    }

    // Loading the credentials to the new postgresql database
    if (useServerPostgresDB) {
      try {
        File myObj = new File("resources/server_postgresql_credentials.txt");
        Scanner myReader = new Scanner(myObj);
        DB_USER = myReader.nextLine();
        DB_PASS = myReader.nextLine();
        myReader.close();
      } catch (FileNotFoundException e) {
        System.out.println("Please, remember to create the database"+
          "credentials file (see README)");
        e.printStackTrace();
      }
    }

    // Defining connection URL
    String pos_complete_url;
    if (useServerPostgresDB) {
       pos_complete_url = DB_PREFIX + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
       + "?user=" + DB_USER + "&password=" + DB_PASS;
    } else {
       pos_complete_url = DB_PREFIX + local_DB_HOST + "/" + local_DB_NAME
       + "?user=" + local_DB_USER +"&password=" + local_DB_PASS;
    }

    // Opening a connection to the PostgreSQL database
    createDBConnection(pos_complete_url);

    // Applying index based on index required
    if (index_no == 1) {
      timestampIndex();
    } else {
      timestampAndValueIndexes();
    }
  }

  //-----------------------INDEXES----------------------------------------------

  // Applying the "index on timestamp" configuration
  public static void timestampIndex() {
    try {
      String timestamp_index_creation =
              "CREATE INDEX time_index_m " +
              "    ON "+dbName+" (time);";
      pos_stmt.executeUpdate(timestamp_index_creation);
      System.out.println("Index: \""+index_types[index_no]+"\" applied");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  // Applying the "index on timestamp and value" configuration
  public static void timestampAndValueIndexes() {
    try {
      String timestamp_index_creation =
              "CREATE INDEX time_index_m " +
              "    ON "+dbName+" (time, value);";
      pos_stmt.executeUpdate(timestamp_index_creation);
      System.out.println("Index: \""+index_types[index_no]+"\" applied");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  //-----------------------UTILITY----------------------------------------------

  //  ([l/s] [dbName] [0/1/2])
  public static void getInfo(String args[]) {
      useServerPostgresDB = (args[0].compareTo("s") == 0);
      dbName = args[1];
      index_no = Integer.parseInt(args[2]);
  }

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
