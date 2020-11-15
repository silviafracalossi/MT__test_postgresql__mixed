import java.io.*;
import java.sql.*;
import java.util.Scanner;
import java.util.logging.*;
import java.util.*;

public class Q_Main {

    // Store users' configurations - default settings written here
    static boolean useServerPostgresDB = false;
    static String log_folder = "logs/";
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

    // Defining the connection and statement variables for PostgreSQL
    static Connection pos_conn = null;
    static Statement pos_stmt = null;

    // Creating the database interactor
    static Q_DatabaseInteractions dbi;

    public static void main(String[] args) {

        try {

            // Checking if the inserted parameters are enough
            if (args.length != 4) {
                System.out.println("Please, insert args: "+args.length+"/4");
                return;
            }

            // Getting what the user inserted ([l/s] [dbName] [log_folder] [0/1/2])
            getInfo(args);

            // Instantiate loggers and printing index name
            Logger logger = instantiateLogger("querying");
            logger.info("Index: " + index_types[index_no]);

            // Loading the credentials to the new postgresql database
            if (useServerPostgresDB) {
              logger.info("Reading database credentials");
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

            // Loading the credentials to the new database
            logger.info("Instantiating database interactor");
            dbi = new Q_DatabaseInteractions(dbName, logger);

            // Marking start of tests
            logger.info("---Start of Tests!---");

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
            logger.info("Connecting to the PostgreSQL database...");
            dbi.createDBConnection(pos_complete_url);

            // Getting the max timestamp
            dbi.getMaxMin();

            // Repeating infinitely the query
            logger.info("Starting queries execution");
            while(true) {
              dbi.lastTwoDays_timedMovingAverage();
              for (int i=0; i<80; i++) {
                dbi.lastThirtyMinutes_avgMaxMin();
              }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbi.closeDBConnection();
        }
    }

    //-----------------------UTILITY----------------------------------------------

    public static void getInfo(String args[]) {
        useServerPostgresDB = (args[0].compareTo("s") == 0);
        dbName = args[1];
        log_folder += args[2]+"/";
        index_no = Integer.parseInt(args[3]);
    }

    // Instantiating the logger for the general information or errors
    public static Logger instantiateLogger (String file_name) throws IOException {

        // Instantiating general logger
        String log_complete_path = log_folder + file_name + "_" + index_types[index_no] + ".xml";
        Logger logger = Logger.getLogger("GeneralLog");
        logger.setLevel(Level.ALL);

        // Loading properties of log file
        Properties preferences = new Properties();
        try {
            FileInputStream configFile = new FileInputStream("resources/logging.properties");
            preferences.load(configFile);
            LogManager.getLogManager().readConfiguration(configFile);
        } catch (IOException ex) {
            System.out.println("[WARN] Could not load configuration file");
        }

        // Instantiating file handler
        FileHandler gl_fh = new FileHandler(log_complete_path);
        logger.addHandler(gl_fh);

        // Returning the logger
        return logger;
    }

    // Returns the index_no of the specified string in the string array
    public static int returnStringIndex(String[] list, String keyword) {
        for (int i=0; i<list.length; i++) {
            if (list[i].compareTo(keyword) == 0) {
                return i;
            }
        }
        return -1;
    }
}
