package com.pya.javamusicplayer;

import java.sql.*;

public class DB {
    private static DB instance;
    private Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/java_music_player";
    private static final String USER = "root";
    private static final String PASSWORD = System.getenv("LOCAL_MYSQL_PWD");

    public static DB getInstance() {
        if (instance == null) {
            synchronized (DB.class) {
                if (instance == null) {
                    instance = new DB();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertMusicHashAndLiked(int code, boolean isLiked) {
        String sql = "INSERT INTO music_info (code, is_liked) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, code);
            pstmt.setBoolean(2, isLiked);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Boolean getMusicIsLikedByHashCode(int hashCode) {
        String query = "SELECT is_liked FROM music_info WHERE code = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, hashCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean("is_liked");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateMusicIsLikedByHashCode(int hashCode, boolean isLiked) {
        String query = "UPDATE music_info SET is_liked = ? WHERE code = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, isLiked);
            statement.setInt(2, hashCode);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private DB() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the database successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
