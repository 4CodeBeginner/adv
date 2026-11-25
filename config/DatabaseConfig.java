package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final String DB_URL = "jdbc:mysql://mysql-e37d74f-naufalrizqyalfath-4b7e.a.aivencloud.com:28803/defaultdb?useSSL=true&requireSSL=true&serverTimezone=Asia/Jakarta";
    private static final String DB_USER = "avnadmin";
    private static final String DB_PASS = "AVNS_HUN5zAyVMLCYDVeigqT";

    private DatabaseConfig() {

    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
}