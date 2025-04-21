package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_URL_GOV1 = "jdbc:mysql://localhost:3307/cairo";
    private static final String DB_URL_GOV2 = "jdbc:mysql://localhost:3308/other";
    private static final String USER = "root";
    private static final String PASSWORD = "midgeek";

    public static Connection getConnection(String government) throws SQLException {
        if (government.equalsIgnoreCase("gov1")) {
            return DriverManager.getConnection(DB_URL_GOV1, USER, PASSWORD);
        } else  {
            return DriverManager.getConnection(DB_URL_GOV2, USER, PASSWORD);
        } 
    }
}