import services.UserService;
import services.ItemService;
import services.TransactionService;

public class Database {
    private UserService userService;
    private ItemService itemService;
    private TransactionService transactionService;

    public Database() {
        this.userService = new UserService();
        this.itemService = new ItemService();
        this.transactionService = new TransactionService();
    }

    public Result createAccount(String username, String password, String government, int balance) {
        // Map government input to gov1 or gov2
        String mappedGovernment = "Cairo".equalsIgnoreCase(government) ? "gov1" : "gov2";
        int temp = userService.createAccount(username, password, balance, mappedGovernment,government);
        return new Result((String) null, temp); // msg_num 1 indicates success, 2 indicates failure
    }
 
    public Result login(String username, String password) {
        if ("admin".equals(username) && "admin".equals(password)) {
            return new Result("user id: -1, user government: no_gov", 3); // Admin login
        }
    
        String loginResult = userService.login(username, password); // Call UserService's login method
        if (loginResult != null) {
            String[] parts = loginResult.split(","); // Split the result into id, government, and msg_num
            String formattedResult = "id: " + parts[0] + ", government: " + parts[1]; // Format the result
            int msgNum = Integer.parseInt(parts[2]); // Extract msg_num
            /*
             * debug
             */
            System.out.println("msg_num: " + msgNum); // Print msg_num for debugging
            System.out.println("formattedResult: " + formattedResult); // Print formatted result for debugging
            /*
             * debug
             */
            return new Result(formattedResult, msgNum); // Return the formatted result
        }
    
        return new Result((String)null, 4); // Login failed
    }

    public Result addItem(int userId, String government, String itemName, double price, int quantity) {
        // Validate price and quantity
        if (price <= 0) {
            return new Result((String)null, 6); // Price should be a positive number
        }
        if (quantity <= 0) {
            return new Result((String)null, 7); // Quantity should be a positive number
        }
        String mappedGovernment = "Cairo".equalsIgnoreCase(government) ? "gov1" : "gov2";
        // Call the addItem function in ItemService
        int resultCode = itemService.addItem(userId, itemName, price, quantity, mappedGovernment);

        // Return the appropriate result based on the resultCode
        return new Result((String)null, resultCode); // 5 for success, 6 for invalid price, 7 for invalid quantity, 8 for duplicate item
    }

    public Result editItem(int userId, String government, String itemName, String newItemName, double newPrice, int newQuantity) {
        String mappedGovernment = "Cairo".equalsIgnoreCase(government) ? "gov1" : "gov2";
        int resultCode = itemService.editItem(itemName, userId, newItemName, newPrice, newQuantity, mappedGovernment);
        return new Result((String)null, resultCode); // Return the appropriate result code
    }

    public Result deleteItem(int userId, String government, String itemName) {
        String mappedGovernment = "Cairo".equalsIgnoreCase(government) ? "gov1" : "gov2";
        int resultCode = itemService.deleteItem(itemName, userId, mappedGovernment);
        return new Result((String)null, resultCode); // Return the appropriate result code
    }

    public Result depositCash(int userId, String government, double amount) {
        String mappedGovernment = "Cairo".equalsIgnoreCase(government) ? "gov1" : "gov2";
        int resultCode = userService.depositCash(userId, amount, mappedGovernment);
        return new Result((String)null, resultCode); // Return the appropriate result code
    }

    public Result viewItemsForSale(int userId, String government) {
        String[] items = transactionService.searchAndPurchaseItems(userId, government);

        if (items == null || items.length == 0) {
            return new Result((String)null, 15); // No items for sale
        }

        return new Result(items, 14); // Items shown successfully
    }
    
    public Result buyItems(int buyerId, String buyerGovernment, int itemId, String sellerGovernment, int quantity) {
        // Validate the item and seller's government
        int validationCode = transactionService.validateItemAndSellerGovernment(itemId, sellerGovernment);
        if (validationCode == 19) {
            return new Result((String) null, 19); // Item not exist or seller's government does not match
        }

        // Map buyer and seller governments to gov1 or gov2
        String mappedBuyerGovernment = "Cairo".equalsIgnoreCase(buyerGovernment) ? "gov1" : "gov2";
        String mappedSellerGovernment = "Cairo".equalsIgnoreCase(sellerGovernment) ? "gov1" : "gov2";

        // Proceed with the purchase
        int resultCode = transactionService.purchaseItem(buyerId, mappedBuyerGovernment, itemId, quantity, mappedSellerGovernment, buyerGovernment, sellerGovernment);
        return new Result((String) null, resultCode); // Return the appropriate result code
    }

    public Result getAccountInfo(int userId, String government) {
        String mappedGovernment1 = "Cairo".equalsIgnoreCase(government) ? "gov1" : "gov2";
        try {
            String[] accountInfo = userService.getAccountInfo(userId, mappedGovernment1);
            if (accountInfo != null) {
                return new Result(accountInfo, 20); // Account info showed successfully
            }
            return new Result((String) null, 27); // You don’t have access
        } catch (Exception e) {
            return new Result((String) null, 27); // You don’t have access
        }
    }
    public Result getSoldItems(int userId, String government) {
        try {
            String[] soldItems = transactionService.getSoldItems(userId, government);
            if (soldItems != null && soldItems.length > 0) {
                return new Result(soldItems, 23); // Sold items showed successfully
            }
            return new Result((String) null, 24); // No sold items
        } catch (Exception e) {
            return new Result((String) null, 27); // You don’t have access
        }
    }
    public Result getPurchasedItems(int userId, String government) {
        try {
            String[] purchasedItems = transactionService.getPurchasedItems(userId, government);
            if (purchasedItems != null && purchasedItems.length > 0) {
                return new Result(purchasedItems, 21); // Purchased items showed successfully
            }
            return new Result((String) null, 22); // No purchased items
        } catch (Exception e) {
            return new Result((String) null, 27); // You don’t have access
        }
    }
    public Result getMyItems(int userId, String government) {
        String mappedGovernment1 = "Cairo".equalsIgnoreCase(government) ? "gov1" : "gov2";
        try {
            String[] myItems = itemService.getMyItems(userId, mappedGovernment1);
            if (myItems != null && myItems.length > 0) {
                return new Result(myItems, 25); // My items showed successfully
            }
            return new Result((String) null, 26); // No items
        } catch (Exception e) {
            return new Result((String) null, 27); // You don’t have access
        }
    }
    public Result viewSystemTransactions(int userId, String government) {
        // Check if the user has admin access
        if (userId != -1 || !"no_gov".equalsIgnoreCase(government)) {
            return new Result((String) null, 27); // You don’t have access
        }

        // Call the TransactionService function to get all system transactions
        String[] transactions = transactionService.viewSystemTransactions();
        if (transactions == null || transactions.length == 0) {
            return new Result((String) null, 29); // No transaction found
        }

        return new Result(transactions, 28); // Transaction views successfully
    }
}
