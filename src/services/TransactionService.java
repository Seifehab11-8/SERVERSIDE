package services;

import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {

    // SQL Queries as constants
    private static final String FETCH_ITEM_QUERY = "SELECT name, price, quantity, user_id FROM items WHERE item_id = ?";
    private static final String FETCH_BALANCE_QUERY = "SELECT balance FROM users WHERE user_id = ?";
    private static final String UPDATE_BALANCE_QUERY = "UPDATE users SET balance = balance + ? WHERE user_id = ?";
    private static final String INSERT_TRANSACTION_QUERY = "INSERT INTO transactions (buyer_id, buyer_government, seller_id, seller_government, "
            +
            "item_id, item_government, item_name, quantity, amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_ITEM_QUERY = "UPDATE items SET quantity = quantity - ? WHERE item_id = ?";

    /**
     * Handles the purchase of an item by a buyer from a seller.
     */
    public int purchaseItem(int buyerId, String buyerGovernment, int itemId, int quantity, String sellerGovernment,
            String buyerGov, String sellerGov) {
        try (Connection connSeller = DatabaseConnection.getConnection(sellerGovernment);
                PreparedStatement fetchItemStmt = connSeller.prepareStatement(FETCH_ITEM_QUERY)) {

            fetchItemStmt.setInt(1, itemId);
            ResultSet rs = fetchItemStmt.executeQuery();

            if (!rs.next()) {
                return 19; // Item not exist
            }

            String itemName = rs.getString("name");
            double price = rs.getDouble("price");
            int availableQuantity = rs.getInt("quantity");
            int sellerId = rs.getInt("user_id");

            // Validate buyer and seller
            if (buyerId == sellerId && buyerGovernment.equals(sellerGovernment)) {
                return 19; // Buyer cannot purchase their own item
            }

            // Check if the quantity is sufficient
            if (quantity > availableQuantity) {
                return 17; // Not enough quantity
            }

            double totalAmount = price * quantity;

            // Check if the buyer has sufficient balance
            try (Connection connBuyer = DatabaseConnection.getConnection(buyerGovernment);
                    PreparedStatement fetchBalanceStmt = connBuyer.prepareStatement(FETCH_BALANCE_QUERY)) {
                fetchBalanceStmt.setInt(1, buyerId);
                ResultSet balanceRs = fetchBalanceStmt.executeQuery();

                if (!balanceRs.next() || balanceRs.getDouble("balance") < totalAmount) {
                    return 18; // Insufficient balance
                }
            }

            // Process the transaction
            if (processTransaction(buyerId, sellerId, buyerGovernment, sellerGovernment, itemId, itemName, quantity,
                    totalAmount, buyerGov, sellerGov)) {
                return 16; // Item paid successfully
            } else {
                return 19; // Item not exist (fallback case)
            }
        } catch (SQLException e) {
            return 19; // Item not exist (fallback case)
        }
    }

    /**
     * Processes the transaction by updating balances, recording the transaction,
     * and updating item quantity.
     */
    private boolean processTransaction(int buyerId, int sellerId, String buyerGovernment, String sellerGovernment,
            int itemId, String itemName, int quantity, double totalAmount, String buyerGov, String sellerGov) {
        try (Connection connBuyer = DatabaseConnection.getConnection(buyerGovernment)) {
            connBuyer.setAutoCommit(false);

            // Update buyer's balance
            if (!updateBalance(connBuyer, buyerId, -totalAmount)) {
                connBuyer.rollback();
                return false;
            }

            // Update seller's balance
            try (Connection connSeller = DatabaseConnection.getConnection(sellerGovernment)) {
                if (!updateBalance(connSeller, sellerId, totalAmount)) {
                    connBuyer.rollback();
                    return false;
                }
            }

            // Record the transaction
            try (PreparedStatement insertTransactionStmt = connBuyer.prepareStatement(INSERT_TRANSACTION_QUERY)) {
                insertTransactionStmt.setInt(1, buyerId);
                insertTransactionStmt.setString(2, buyerGov);
                insertTransactionStmt.setInt(3, sellerId);
                insertTransactionStmt.setString(4, sellerGov);
                insertTransactionStmt.setInt(5, itemId);
                insertTransactionStmt.setString(6, sellerGov);
                insertTransactionStmt.setString(7, itemName);
                insertTransactionStmt.setInt(8, quantity);
                insertTransactionStmt.setDouble(9, totalAmount);
                insertTransactionStmt.executeUpdate();
            }

            // Update seller's item quantity
            try (Connection connSeller = DatabaseConnection.getConnection(sellerGovernment);
                    PreparedStatement updateItemStmt = connSeller.prepareStatement(UPDATE_ITEM_QUERY)) {
                updateItemStmt.setInt(1, quantity);
                updateItemStmt.setInt(2, itemId);
                updateItemStmt.executeUpdate();
            }

            // Add or update the item in the buyer's inventory
            if (!updateBuyerInventory(connBuyer, buyerId, itemId, itemName, quantity, buyerGovernment)) {
                connBuyer.rollback();
                return false;
            }

            connBuyer.commit();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Updates a user's balance in the database.
     */
    private boolean updateBalance(Connection conn, int userId, double amount) throws SQLException {
        try (PreparedStatement fetchBalanceStmt = conn.prepareStatement(FETCH_BALANCE_QUERY)) {
            fetchBalanceStmt.setInt(1, userId);
            ResultSet rs = fetchBalanceStmt.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("balance");
                if (balance + amount < 0)
                    return false;

                try (PreparedStatement updateBalanceStmt = conn.prepareStatement(UPDATE_BALANCE_QUERY)) {
                    updateBalanceStmt.setDouble(1, amount);
                    updateBalanceStmt.setInt(2, userId);
                    updateBalanceStmt.executeUpdate();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the buyer's inventory by adding or updating the purchased item.
     */
    private boolean updateBuyerInventory(Connection conn, int buyerId, int itemId, String itemName, int quantity,
            String buyerGovernment) throws SQLException {
        String checkItemQuery = "SELECT quantity FROM items WHERE user_id = ? AND name = ?";
        try (PreparedStatement checkItemStmt = conn.prepareStatement(checkItemQuery)) {
            checkItemStmt.setInt(1, buyerId);
            checkItemStmt.setString(2, itemName);
            ResultSet rs = checkItemStmt.executeQuery();

            if (rs.next()) {
                // If the item exists, update its quantity
                int existingQuantity = rs.getInt("quantity");
                String updateItemQuery = "UPDATE items SET quantity = ? WHERE user_id = ? AND name = ?";
                try (PreparedStatement updateItemStmt = conn.prepareStatement(updateItemQuery)) {
                    updateItemStmt.setInt(1, existingQuantity + quantity);
                    updateItemStmt.setInt(2, buyerId);
                    updateItemStmt.setString(3, itemName);
                    updateItemStmt.executeUpdate();
                }
            } else {
                // If the item does not exist, insert it as a new item
                String insertItemQuery = "INSERT INTO items (user_id, name, price, quantity) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertItemStmt = conn.prepareStatement(insertItemQuery)) {
                    insertItemStmt.setInt(1, buyerId);
                    insertItemStmt.setString(2, itemName);
                    insertItemStmt.setDouble(3, 0.0); // Price is set to 0.0 for buyer's inventory
                    insertItemStmt.setInt(4, quantity);
                    insertItemStmt.executeUpdate();
                }
            }
        }
        return true;
    }

    /**
     * Searches for items available for sale and allows the user to purchase them.
     */
    public String[] searchAndPurchaseItems(int userId, String userGovernment) {
        String query = "SELECT i.item_id, i.name, i.price, i.quantity, u.username AS seller_name, u.government AS seller_government "
                +
                "FROM items i " +
                "JOIN users u ON i.user_id = u.user_id " +
                "WHERE NOT (i.user_id = ? AND u.government = ?) AND i.quantity > 0 AND i.price > 0.0";

        List<String> itemsForSale = new ArrayList<>();

        try {
            fetchItemsForSale("gov1", query, userId, userGovernment, itemsForSale);
            fetchItemsForSale("gov2", query, userId, userGovernment, itemsForSale);

            if (itemsForSale.isEmpty()) {
                return null;
            } else {
                return itemsForSale.toArray(new String[0]);
            }
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Fetches items available for sale from a specific government database.
     */

    private void fetchItemsForSale(String government, String query, int userId, String userGovernment,
            List<String> itemsForSale) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(government);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, userGovernment);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itemsForSale.add(formatItem(rs));
                }
            }
        }
    }

    /**
     * Formats item details into a readable string.
     */

    private String formatItem(ResultSet rs) throws SQLException {
        return "Item ID: " + rs.getInt("item_id") +
                ", Name: " + rs.getString("name") +
                ", Price: " + rs.getDouble("price") +
                ", Quantity: " + rs.getInt("quantity") +
                ", Seller: " + rs.getString("seller_name") +
                ", Government: " + rs.getString("seller_government");
    }

    /**
     * Formats transaction details into a readable string.
     */
    private String formatTransaction(ResultSet rs, String buyerDetails, String sellerDetails) throws SQLException {
        return "Transaction ID: " + rs.getInt("transaction_id") +
                ", Item: " + rs.getString("item_name") +
                ", Quantity: " + rs.getInt("quantity") +
                ", Amount: " + rs.getDouble("amount") +
                ", Date: " + rs.getTimestamp("transaction_date") +
                ", Buyer: " + buyerDetails +
                ", Buyer government: " + rs.getString("buyer_government") +
                ", Seller: " + sellerDetails +
                ", Seller government: " + rs.getString("seller_government");
    }

    /**
     * Fetches user details (username and government) from the appropriate database.
     */
    private String fetchUserDetails(int userId, String government) throws SQLException {
        // Map the real government to the corresponding database
        String mappedGovernment = "Cairo".equalsIgnoreCase(government) ? "gov1" : "gov2";

        String query = "SELECT username FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(mappedGovernment);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        }
        return "Unknown";
    }

    /**
     * Fetches user details (username and government) from the appropriate database.
     */
    private String fetchUserDetailsWithGovernment(int userId, String government) throws SQLException {
        // Map the real government to the corresponding database
        String mappedGovernment = "Cairo".equalsIgnoreCase(government) ? "gov1" : "gov2";

        String query = "SELECT username, government FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(mappedGovernment);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        }
        return "Unknown";
    }

    /**
     * Fetches transactions from a specific government database.
     */
    private void fetchTransactions(String government, String query, List<String> allTransactions) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(government);
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int buyerId = rs.getInt("buyer_id");
                String buyerGovernment = rs.getString("buyer_government");
                int sellerId = rs.getInt("seller_id");
                String sellerGovernment = rs.getString("seller_government");

                // Fetch buyer details (name and real government)
                String buyerDetails = fetchUserDetailsWithGovernment(buyerId, buyerGovernment);

                // Fetch seller details (name and real government)
                String sellerDetails = fetchUserDetailsWithGovernment(sellerId, sellerGovernment);

                // Format the transaction
                allTransactions.add(formatTransaction(rs, buyerDetails, sellerDetails));
            }
        }
    }

    /**
     * Displays all transactions in the system.
     */
    public String[] viewSystemTransactions() {
        String query = "SELECT t.transaction_id, t.item_name, t.quantity, t.amount, t.transaction_date, " +
                "t.buyer_id, t.buyer_government, t.seller_id, t.seller_government " +
                "FROM transactions t";

        List<String> allTransactions = new ArrayList<>();

        try {
            fetchTransactions("gov1", query, allTransactions);
            fetchTransactions("gov2", query, allTransactions);

            if (allTransactions.isEmpty()) {
                return null; // No transactions found
            }

            return allTransactions.toArray(new String[0]);
        } catch (SQLException e) {
            return null; // Error occurred
        }
    }

    /**
     * Retrieves items sold by a specific user.
     */

    public String[] getSoldItems(int userId, String government) throws SQLException {
        // Determine the user's government
        String myDatabase = "Cairo".equalsIgnoreCase(government) ? "gov1" : "gov2";
        String otherDatabase = "Cairo".equalsIgnoreCase(government) ? "gov2" : "gov1";

        String query = "SELECT t.item_name, t.quantity, t.amount, t.buyer_id, t.buyer_government, t.transaction_date " +
                "FROM transactions t WHERE t.seller_id = ? AND t.seller_government = ?";
        List<String> soldItems = new ArrayList<>();

        // Fetch transactions from my database
        fetchSoldItemsFromDatabase(myDatabase, userId, government, query, soldItems);

        // Fetch transactions from the other database
        fetchSoldItemsFromDatabase(otherDatabase, userId, government, query, soldItems);

        return soldItems.toArray(new String[0]);
    }

    private void fetchSoldItemsFromDatabase(String database, int userId, String government, String query,
            List<String> soldItems) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(database);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, government);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int buyerId = rs.getInt("buyer_id");
                String buyerGovernment = rs.getString("buyer_government");

                // Fetch buyer details
                String buyerDetails = fetchUserDetails(buyerId, buyerGovernment);

                // Format the sold item details
                soldItems.add("Item: " + rs.getString("item_name") +
                        ", Quantity: " + rs.getInt("quantity") +
                        ", Amount: " + rs.getDouble("amount") +
                        ", Buyer: " + buyerDetails + ", Buyer government: " + buyerGovernment +
                        ", Date: " + rs.getTimestamp("transaction_date"));
            }
        }
    }

    /**
     * Retrieves items purchased by a specific user.
     */
    public String[] getPurchasedItems(int userId, String government) throws SQLException {
        // Determine the user's database
        String myDatabase = "Cairo".equalsIgnoreCase(government) ? "gov1" : "gov2";

        String query = "SELECT t.item_name, t.quantity, t.amount, t.seller_id, t.seller_government, t.transaction_date "
                +
                "FROM transactions t WHERE t.buyer_id = ? AND t.buyer_government = ?";
        List<String> purchasedItems = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection(myDatabase);
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, government);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int sellerId = rs.getInt("seller_id");
                String sellerGovernment = rs.getString("seller_government");

                // Fetch seller details
                String sellerDetails = fetchUserDetails(sellerId, sellerGovernment);

                // Format the purchased item details
                purchasedItems.add("Item: " + rs.getString("item_name") +
                        ", Quantity: " + rs.getInt("quantity") +
                        ", Amount: " + rs.getDouble("amount") +
                        ", Seller: " + sellerDetails + ", Seller government: " + sellerGovernment +
                        ", Date: " + rs.getTimestamp("transaction_date"));
            }
        }

        return purchasedItems.toArray(new String[0]);
    }

    public int validateItemAndSellerGovernment(int itemId, String sellerGovernment) {
        String query = "SELECT i.item_id " +
                "FROM items i " +
                "JOIN users u ON i.user_id = u.user_id " +
                "WHERE i.item_id = ? AND u.government = ?";
        try (Connection conn = DatabaseConnection
                .getConnection("Cairo".equalsIgnoreCase(sellerGovernment) ? "gov1" : "gov2");
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, itemId);
            stmt.setString(2, sellerGovernment);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return 19; // Item does not exist or seller's government does not match
            }
            return 0; // Validation successful
        } catch (SQLException e) {
            return 19; // Return 19 in case of an error
        }
    }
}
