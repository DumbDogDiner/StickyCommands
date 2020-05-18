package com.dumbdogdiner.StickyCommands.Utils;

import java.sql.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import com.dumbdogdiner.StickyCommands.Main;

public class DatabaseUtil {

    public static Connection connection;
    private static Main self = Main.getPlugin(Main.class);

    /**
     * Actually open the conenction to the database, this should not be used outside
     * this class.
     * 
     * @throws SQLException SQL exception if the connection fails
     */
    public static void OpenConnection() throws SQLException {
        if (connection != null && !connection.isClosed())
            return;

        synchronized (self) {
            if (connection != null && !connection.isClosed())
                return;

            connection = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s?autoReconnect=true&failOverReadOnly=false&maxReconnects=%d",
                            Configuration.dbhost, Configuration.dbport, Configuration.dbname,
                            Configuration.MaxReconnects),
                    Configuration.dbusername, Configuration.dbpassword);
        }
    }

    /**
     * Terminate the connection to the database.
     */
    public static void Terminate() {
        // Close the database connection (if open)
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initialize the database tables and connection. This also starts the
     * synchronization thread for the database.
     * 
     * @return True if the tables were created successfully and the connection
     *         completed successfully.
     */
    public static boolean InitializeDatabase() {
        try {
            DatabaseUtil.OpenConnection();
        } catch (SQLException e) {
            // e.printStackTrace();
            self.getLogger().severe(
                    "Cannot connect to database, ensure your database is setup correctly and restart the server.");
            // Just exit and let the user figure it out.
            return false;
        }

        // Ensure Our tables are created.
        try {
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS Users "
                    + "(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," 
                    + "UUID VARCHAR(36) NOT NULL,"
                    + "PlayerName VARCHAR(17)," 
                    + "IPAddress VARCHAR(48) NOT NULL," 
                    + "FirstLogin TIMESTAMP NOT NULL,"
                    + "LastLogin TIMESTAMP NOT NULL," 
                    + "LastServer TEXT NOT NULL," 
                    + "TimesConnected INT NULL" 
                    + ")")
                    .execute();
        } catch (SQLException e) {
            e.printStackTrace();
            self.getLogger()
                    .severe("Cannot create database tables, please ensure your SQL user has the correct permissions.");
            return false;
        }
        return true;
    }

    /**
     * Insert a user into the database.
     * 
     * @param UUID       UUID of the minecraft user
     * @param PlayerName Name of the minecraft player
     * @param IPAddress  IP address of the minecraft player
     * @param FirstLogin The first time they logged in (as a timestamp)
     * @param LastLogin  The last time they logged in (as a timestamp)
     * @return True if the user was created successfully
     */
    public static Future<Boolean> InsertUser(String UUID, String PlayerName, String IPAddress, Timestamp FirstLogin,
            Timestamp LastLogin) {
        FutureTask<Boolean> t = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                // This is where you should do your database interaction
                try {
                    // Make sure we're not duping data, if they already exist go ahead and update
                    // them
                    // This happens because we insert every time they join for the first time, but
                    // if the playerdata is removed on the world
                    // or the spigot plugin is setup in multiple servers using the same database, it
                    // would add them a second time
                    // lets not do that....
                    int j = 1;
                    PreparedStatement CheckUser = connection.prepareStatement("SELECT id FROM Users WHERE UUID = ?");
                    CheckUser.setString(j++, UUID);
                    ResultSet results = CheckUser.executeQuery();
                    if (results.next() && !results.wasNull()) {
                        UpdateUser(UUID, PlayerName, IPAddress, LastLogin);
                        return true;
                    }

                    // Preapre a statement
                    int i = 1;
                    PreparedStatement InsertUser = connection.prepareStatement(String.format(
                            "INSERT INTO Users (UUID, PlayerName, IPAddress, FirstLogin, LastLogin, TimesConnected) VALUES (?, ?, ?, ?, ?, ?)"));
                    InsertUser.setString(i++, UUID);
                    InsertUser.setString(i++, PlayerName);
                    InsertUser.setString(i++, IPAddress);
                    InsertUser.setTimestamp(i++, FirstLogin);
                    InsertUser.setTimestamp(i++, LastLogin);
                    InsertUser.setInt(i++, 1);
                    InsertUser.executeUpdate();
                } catch (Throwable e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        });

        Main.pool.execute(t);

        return (Future<Boolean>) t;
    }

    /**
     * Update a user record
     * 
     * @param UUID       Users current UUID
     * @param PlayerName Users current player name
     * @param IPAddress  Users current IP address
     * @param LastLogin  The timestamp of the last time a user logged in
     * @return True if the update was successful.
     */
    public static Future<Boolean> UpdateUser(String UUID, String PlayerName, String IPAddress, Timestamp LastLogin)
    // (Timestamp LastLogin, String PlayerName, String IPAddress, String UUID)
    {
        FutureTask<Boolean> t = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                // This is where you should do your database interaction
                try {
                    int j = 1;
                    // This is a fail-safe just incase the table was dropped or the player joined
                    // the server BEFORE the plugin was added...
                    // This will ensure they get added to the database no matter what.
                    PreparedStatement CheckUser = connection
                            .prepareStatement(String.format("SELECT id FROM Users WHERE UUID = ?"));
                    CheckUser.setString(j++, UUID);
                    ResultSet results = CheckUser.executeQuery();
                    if (!results.next()) {
                        Timestamp FirstLogin = TimeUtil.TimestampNow();
                        InsertUser(UUID, PlayerName, IPAddress, FirstLogin, LastLogin);
                        return true;
                    }

                    PreparedStatement gtc = connection
                            .prepareStatement(String.format("SELECT TimesConnected FROM Users WHERE UUID = ?"));
                    gtc.setString(1, UUID);

                    ResultSet gtc2 = gtc.executeQuery();
                    int tc = 1;
                    if (gtc2.next()) {
                        if (!gtc2.wasNull()) {
                            tc = gtc2.getInt("TimesConnected");
                        } else {
                            tc = 0;
                        }
                    }

                    // Preapre a statement
                    int i = 1;
                    PreparedStatement UpdateUser = connection.prepareStatement(String.format(
                            "UPDATE Users SET LastLogin = ?, PlayerName = ?, IPAddress = ?, TimesConnected = ? WHERE UUID = ?"));
                    UpdateUser.setTimestamp(i++, LastLogin);
                    UpdateUser.setString(i++, PlayerName);
                    UpdateUser.setString(i++, IPAddress);
                    UpdateUser.setInt(i++, ++tc);
                    UpdateUser.setString(i++, UUID);
                    UpdateUser.executeUpdate();
                } catch (Throwable e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        });

        Main.pool.execute(t);

        return (Future<Boolean>) t;
    }

}