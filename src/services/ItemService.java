package services;

import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemService {
    /**
     * Adds a new item to the database.
     */

    public int addItem(int userId, String name, double price, int quantity, String government) {
        // Check if the item already exists
        String checkQuery = "SELECT COUNT(*) FROM items WHERE user_id = ? AND name = ?";
        try (Connection conn = DatabaseConnection.getConnection(government);
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, userId);
            checkStmt.setString(2, name);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return 8; // Item already exists
            }
        } catch (SQLException e) {
            return 7; // Return 7 for any SQL error
        }

        // Validate price
        if (price <= 0) {
            return 6; // Price should be a positive number
        }

        // Validate quantity
        if (quantity <= 0) {
            return 7; // Quantity should be a positive number
        }

        // Insert the item into the database
        String insertQuery = "INSERT INTO items (user_id, name, price, quantity) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(government);
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            insertStmt.setInt(1, userId);
            insertStmt.setString(2, name);
            insertStmt.setDouble(3, price);
            insertStmt.setInt(4, quantity);
            insertStmt.executeUpdate();
            return 5; // Item added successfully
        } catch (SQLException e) {
            return 7; // Return 7 for any SQL error
        }
    }

    /**
     * Edits an existing item in the database.
     */

    public int editItem(String itemName, int userId, String newName, double newPrice, int newQuantity,
            String government) {
        // Check if the item exists
        String checkItemQuery = "SELECT * FROM items WHERE name = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(government);
                PreparedStatement checkStmt = conn.prepareStatement(checkItemQuery)) {
            checkStmt.setString(1, itemName);
            checkStmt.setInt(2, userId);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                return 9; // Item not found
            }

            // Retrieve current values from the database
            String currentName = rs.getString("name");
            double currentPrice = rs.getDouble("price");
            int currentQuantity = rs.getInt("quantity");

            // Use current values if new values are not provided
            String finalName = (newName != null && !newName.isEmpty()) ? newName : currentName;
            double finalPrice = (newPrice != -1) ? newPrice : currentPrice;
            int finalQuantity = (newQuantity != -1) ? newQuantity : currentQuantity;

            // Validate price and quantity
            if (finalPrice <= 0) {
                return 6; // Price should be a positive number
            }
            if (finalQuantity <= 0) {
                return 7; // Quantity should be a positive number
            }

            // Check if the new name already exists for the same user
            if (!finalName.equals(currentName)) {
                String checkNameQuery = "SELECT COUNT(*) FROM items WHERE name = ? AND user_id = ?";
                try (PreparedStatement checkNameStmt = conn.prepareStatement(checkNameQuery)) {
                    checkNameStmt.setString(1, finalName);
                    checkNameStmt.setInt(2, userId);
                    ResultSet nameRs = checkNameStmt.executeQuery();
                    if (nameRs.next() && nameRs.getInt(1) > 0) {
                        return 8; // Item name already exists
                    }
                }
            }

            // Update the item in the database
            String updateQuery = "UPDATE items SET name = ?, price = ?, quantity = ? WHERE name = ? AND user_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setString(1, finalName);
                updateStmt.setDouble(2, finalPrice);
                updateStmt.setInt(3, finalQuantity);
                updateStmt.setString(4, itemName);
                updateStmt.setInt(5, userId);
                updateStmt.executeUpdate();
                return 10; // Item updated successfully
            }
        } catch (SQLException e) {
            System.out.println("Error editing item: " + e.getMessage());
            return 7; // Return 7 for any SQL error
        }
    }

    /**
     * Deletes an item from the database.
     */
    public int deleteItem(String itemName, int userId, String government) {
        // Check if the item exists
        String checkItemQuery = "SELECT COUNT(*) FROM items WHERE name = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(government);
                PreparedStatement checkStmt = conn.prepareStatement(checkItemQuery)) {
            checkStmt.setString(1, itemName);
            checkStmt.setInt(2, userId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                return 9; // Item not found
            }
        } catch (SQLException e) {
            return 9; // Return 9 for any SQL error
        }

        // Delete the item from the database
        String deleteQuery = "DELETE FROM items WHERE name = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(government);
                PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
            deleteStmt.setString(1, itemName);
            deleteStmt.setInt(2, userId);
            deleteStmt.executeUpdate();
            return 11; // Item deleted successfully
        } catch (SQLException e) {
            return 9; // Return 9 for any SQL error
        }
    }

    /**
     * Retrieves the items owned by a specific user.
     */
    public String[] getMyItems(int userId, String government) throws SQLException {
        String query = "SELECT name, price, quantity FROM items WHERE user_id = ?";
        List<String> myItems = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(government);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                myItems.add("Name: " + rs.getString("name") +
                        ", Price: " + rs.getDouble("price") +
                        ", Quantity: " + rs.getInt("quantity"));
            }
        }
        return myItems.toArray(new String[0]);
    }

}