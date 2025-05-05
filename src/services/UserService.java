package services;

import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class UserService {


    public int createAccount(String username, String password, double balance, String government,String gov) {
        if (isUsernameTaken(username)) {
        
            return 2; // Return 2 if the username already exists
        }
        // Proceed to create the account if the username is unique
        String query = "INSERT INTO users (username, password_hash, balance, government) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(government);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // Hash the password in a real application
            stmt.setDouble(3, balance);
            stmt.setString(4, gov); // Store the government value
            stmt.executeUpdate();
            return 1; // Return 1 if the account is created successfully
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage()); // Log the error message
            return 2; // Return 2 if there is an error during account creation
        }
    }


    private boolean isUsernameTaken(String username) {
        String query = "SELECT username FROM users WHERE username = ?";
        boolean existsInGov1 = false;
        boolean existsInGov2 = false;

        // Check in gov1
        try (Connection conn = DatabaseConnection.getConnection("gov1");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                existsInGov1 = true;
            }
        } catch (SQLException e) {
          return true;
        }

        // Check in gov2
        try (Connection conn = DatabaseConnection.getConnection("gov2");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                existsInGov2 = true;
            }
        } catch (SQLException e) {
            return true;
        }

        return existsInGov1 || existsInGov2;
    }



    public String login(String username, String password) {
        // Handle admin login
        if ("admin".equals(username) && "admin".equals(password)) {
            return "-1,no_gov,3"; // Admin login successful
        }

        String query = "SELECT user_id, government FROM users WHERE username = ? AND password_hash = ?";
        // Check in gov1
        try (Connection conn = DatabaseConnection.getConnection("gov1");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // Hash the password in a real application
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id") + ",gov1,3"; // Login successful in gov1
            }
        } catch (SQLException e) {
            return null; // Return null if an error occurs
        }

        // Check in gov2
        try (Connection conn = DatabaseConnection.getConnection("gov2");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // Hash the password in a real application
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id") + ",gov2,3"; // Login successful in gov2
            }
        } catch (SQLException e) {
            return null; // Return null if an error occurs
        }

        // If no match found, return null
        return null;
    }

    public int depositCash(int userId, double amount, String government) {
        // Validate the deposit amount
        if (amount <= 0) {
            return 12; // Deposit should be a positive number
        }

        String query = "UPDATE users SET balance = balance + ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(government);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            return 13; // Amount added successfully
        } catch (SQLException e) {
            return 12; // Return 12 for any SQL error
        }
    }
    public String[] getAccountInfo(int userId, String government) throws SQLException {
        String query = "SELECT username, password_hash, government, balance FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(government);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new String[]{
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("government"),
                    String.valueOf(rs.getDouble("balance"))
                };
            }
        }
        return null;
    }
    
}
