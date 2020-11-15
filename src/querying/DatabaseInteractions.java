import java.io.*;
import java.sql.*;
import java.util.Scanner;
import java.util.logging.*;
import java.text.*;

public class DatabaseInteractions {

  // DB variables
  static Connection pos_conn;
  static Statement pos_stmt;
  static String DB_TABLE_NAME;

  // Defining date format
  static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  // Logger
  static Logger logger;

  // Class variables
  static String min, max;


  // Class that handles the insertion configurations
  public DatabaseInteractions (String dbName, Logger logger) {
    this.DB_TABLE_NAME=dbName;
    this.logger=logger;
  }


  //-----------------------QUERIES----------------------------------------------

  // 0. Max and Min
  public static void getMaxMin() throws SQLException {

    // Printing method name
    logger.info("==0. Max and Min==");

    // Creating the query
    String count_query = "SELECT MIN(time), MAX(time) FROM "+DB_TABLE_NAME;

    // Executing the query
    ResultSet rs = pos_stmt.executeQuery(count_query);

    // Printing the result
    while (rs.next()) {
      min = rs.getString(1);
      max = rs.getString(2);
    }

    // Printing retrieved data
    logger.info("Result: Min: " +min+ " and max: "+max);
    System.out.println("Min: " +min+ " and max: "+max);

    // Closing the set
    rs.close();
  }

    //-----------------------SECOND QUERY----------------------------------------------
    // 2. Every 2 minutes of data, computes the average of the current temperature value
    //    and the ones of the previous 4 minutes on last 2 days of data
    public static void lastTwoDays_timedMovingAverage() throws SQLException {

      // Printing method name
      System.out.println("2) lastTwoDays_timedMovingAverage");

      String lastTwoDays_query = "\n"+
        " WITH t AS ( \n"+
        " SELECT date_trunc('hour', hour_trunc_t) as hour_trunc, minute_part \n"+
        " FROM generate_series( \n"+
        "	 	date_trunc('hour', '"+max+"'::timestamp + interval '1 hour') - interval '2 days', \n"+
        "	 	date_trunc('hour', '"+max+"'::timestamp + interval '1 hour'), \n"+
        "	 	'1 hour') AS hour_trunc_t, \n"+
        "	  generate_series(0, 29) AS minute_part \n"+
        ") \n"+

        " SELECT hour_trunc, (minute_part*2) as min, ROUND(AVG(AVG(value)) OVER (ORDER BY hour_trunc, minute_part ROWS BETWEEN 2 PRECEDING AND CURRENT ROW), 2) as AVG \n"+
        " FROM t \n"+
        "   LEFT OUTER JOIN "+DB_TABLE_NAME+" ON date_trunc('hour', "+DB_TABLE_NAME+".time) = t.hour_trunc AND (EXTRACT(minutes FROM time) / 2)::int = minute_part \n"+
        " GROUP BY hour_trunc, minute_part";

      // Executing the query
      logger.info("Executing timedMovingAverage on LastTwoDays");
      ResultSet rs = pos_stmt.executeQuery(lastTwoDays_query);
      logger.info("Completed execution");
    }


    //-----------------------THIRD QUERY----------------------------------------------
    // 3. Calculate mean, max and min on last (arbitrary) 30 minutes of data
    public static void lastThirtyMinutes_avgMaxMin() throws SQLException {

      // Printing method name
      System.out.println("3) lastThirtyMinutes_avgMaxMin");

      // Creating the query
      String lastThirtyMinutes_query = "" +
          " SELECT (max_time - interval '30 minutes') AS start_time, max_time AS end_time, "+
          "   ROUND(AVG(value), 2) AS avg, MAX(value), MIN(value) "+
          " FROM "+DB_TABLE_NAME+", "+
        	"   (SELECT date_trunc('minute', '"+max+"'::timestamp + interval '1 minute') as max_time) t "+
          " WHERE time > (max_time - interval '30 minutes') "+
          " GROUP BY start_time, end_time; ";

      // Executing the query
      logger.info("Executing AvgMaxMin on LastThirtyMinutes");
      ResultSet rs = pos_stmt.executeQuery(lastThirtyMinutes_query);
      logger.info("Completed execution");
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
