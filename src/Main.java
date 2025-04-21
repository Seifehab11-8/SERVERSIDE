public class Main {
    public static void main(String[] args) {
        Database database = new Database();
/* 
        // Hardcoded inputs for testing createAccount
        String username = "mohamed ashraf";
        String password = "1234567";
        String government = "cairo"; // Use "Cairo" for gov1 or "Other" for gov2
        int balance = 1000;

       
        Result createAccountResult = database.createAccount(username, password, government, balance);

        // Check the result of createAccount
        if (createAccountResult.getMsgNum() == 1) {
            System.out.println("Account created successfully!");
        } else if (createAccountResult.getMsgNum() == 2) {
            System.out.println("Account not created. Username already exists.");
        } else {
            System.out.println("Unexpected result.");
        }
 
      
        // Hardcoded inputs for testing login
        String loginUsername = "ahmed";
        String loginPassword = "123456";

        // Test login function
        Result loginResult = database.login(loginUsername, loginPassword);

        // Check the result of login
        if (loginResult.getMsgNum() == 3) {
            System.out.println("Login successful! Details: " + loginResult.getSingleString().orElse("No details"));
        } else if (loginResult.getMsgNum() == 4) {
            System.out.println("Invalid username or password. Please try again.");
        } else {
            System.out.println("Unexpected result.");
        }
         
         
          // Hardcoded inputs for testing addItem
        int userId = 2; // Replace with a valid user ID
        String government = "cairo"; // Use "Cairo" for gov1 or "Other" for gov2
        String itemName = "car"; // Replace with the item name
        double price = 3000.0;
        int quantity = 4;

        // Test addItem function
        Result addItemResult = database.addItem(userId, government, itemName, price, quantity);

        // Check the result of addItem
        switch (addItemResult.getMsgNum()) {
            case 5 -> System.out.println("Item added successfully!");
            case 6 -> System.out.println("Price should be a positive number.");
            case 7 -> System.out.println("Quantity should be a positive number.");
            case 8 -> System.out.println("Item name already exists.");
            default -> System.out.println("Unexpected result.");
        } 
       
        
         // Hardcoded inputs for testing editItem
         int userId = 2; // Replace with a valid user ID
        String government = "Cairo"; // Use "Cairo" for gov1 or "Other" for gov2
        String itemName = "car";
        String newItemName = "bike"; // Leave empty to keep the current name
        double newPrice = -1; // Use -1 to keep the current price
        int newQuantity = 20; // Use -1 to keep the current quantity

        // Test editItem function
        Result editItemResult = database.editItem(userId, government, itemName, newItemName, newPrice, newQuantity);

        // Check the result of editItem
        switch (editItemResult.getMsgNum()) {
            case 10 -> System.out.println("Item updated successfully!");
            case 6 -> System.out.println("Price should be a positive number.");
            case 7 -> System.out.println("Quantity should be a positive number.");
            case 8 -> System.out.println("Item name already exists.");
            case 9 -> System.out.println("Item not found.");
            default -> System.out.println("Unexpected result.");
        }
        
        // Hardcoded inputs for testing deleteItem
        int userId = 2; // Replace with a valid user ID
        String government = "Cairo"; // Use "Cairo" for gov1 or "Other" for gov2
        String itemName = "bike";

        // Test deleteItem function
        Result deleteItemResult = database.deleteItem(userId, government, itemName);

        // Check the result of deleteItem
        switch (deleteItemResult.getMsgNum()) {
            case 11 -> System.out.println("Item deleted successfully!");
            case 9 -> System.out.println("Item not found.");
            default -> System.out.println("Unexpected result.");
        }
        
         // Hardcoded inputs for testing depositCash
        int userId = 2; // Replace with a valid user ID
        String government = "cairo"; // Use "Cairo" for gov1 or "Other" for gov2
        double amount = 5000.0; // Replace with the deposit amount

        // Test depositCash function
        Result depositCashResult = database.depositCash(userId, government, amount);

        // Check the result of depositCash
        switch (depositCashResult.getMsgNum()) {
            case 13 -> System.out.println("Amount added successfully!");
            case 12 -> System.out.println("Deposit should be a positive number.");
            default -> System.out.println("Unexpected result.");
        }
         
         // Hardcoded inputs for testing viewItemsForSale
        int userId = 2; // Replace with a valid user ID
        String government = "cairo"; // Use "Cairo" for gov1 or "Other" for gov2

        // Test viewItemsForSale function
        Result viewItemsResult = database.viewItemsForSale(userId, government);

        // Check the result of viewItemsForSale
        switch (viewItemsResult.getMsgNum()) {
            case 14 -> {
                System.out.println("Items shown successfully!");
                String[] items = viewItemsResult.getMultipleStrings().orElse(new String[0]);
                for (String item : items) {
                    System.out.println(item);
                }
            }
            case 15 -> System.out.println("No items for sale.");
            default -> System.out.println("Unexpected result.");
        }   
         
         // Hardcoded inputs for testing buyItems
         int buyerId = 2; // Replace with a valid buyer ID
         String buyerGovernment = "cairo"; // Use "Cairo" for gov1 or "Other" for gov2
         int itemId = 4; // Replace with a valid item ID
         String sellerGovernment = "cairo"; // Use "Cairo" for gov1 or "Other" for gov2
         int quantity = 1; // Replace with the desired quantity
 
         // Test buyItems function
         Result buyItemsResult = database.buyItems(buyerId, buyerGovernment, itemId, sellerGovernment, quantity);
 
         // Check the result of buyItems
         switch (buyItemsResult.getMsgNum()) {
             case 16 -> System.out.println("Item paid successfully!");
             case 17 -> System.out.println("Not enough quantity.");
             case 18 -> System.out.println("Insufficient balance.");
             case 19 -> System.out.println("Item not exist, try again.");
             default -> System.out.println("Unexpected result.");
         }

         // Hardcoded inputs for testing buyItems
         int userId = 2; // Replace with a valid user ID
        String government = "alex"; // Replace with the user's government

        // Get account info
        Result accountInfoResult = database.getAccountInfo(userId, government);
        if (accountInfoResult.getMsgNum() == 20) {
            String[] accountInfo = accountInfoResult.getStringArray().orElse(new String[0]);
            System.out.println("Account Info:");
            System.out.println("Name: " + accountInfo[0]);
            System.out.println("Password: " + accountInfo[1]);
            System.out.println("Government: " + accountInfo[2]);
            System.out.println("Balance: " + accountInfo[3]);
        } else if (accountInfoResult.getMsgNum() == 27) {
            System.out.println("You don’t have access.");
        }

        // Get sold items
        Result soldItemsResult = database.getSoldItems(userId, government);
        if (soldItemsResult.getMsgNum() == 23) {
            String[] soldItems = soldItemsResult.getStringArray().orElse(new String[0]);
            System.out.println("\nSold Items:");
            for (String item : soldItems) {
                System.out.println(item);
            }
        } else if (soldItemsResult.getMsgNum() == 24) {
            System.out.println("\nNo sold items.");
        }

        // Get purchased items
        Result purchasedItemsResult = database.getPurchasedItems(userId, government);
        if (purchasedItemsResult.getMsgNum() == 21) {
            String[] purchasedItems = purchasedItemsResult.getStringArray().orElse(new String[0]);
            System.out.println("\nPurchased Items:");
            for (String item : purchasedItems) {
                System.out.println(item);
            }
        } else if (purchasedItemsResult.getMsgNum() == 22) {
            System.out.println("\nNo purchased items.");
        }
 
        // Get my items
        Result myItemsResult = database.getMyItems(userId, government);
        if (myItemsResult.getMsgNum() == 25) {
            String[] myItems = myItemsResult.getStringArray().orElse(new String[0]);
            System.out.println("\nMyItems:");
            for (String item : myItems) {
                System.out.println(item);
            }
        } else if (myItemsResult.getMsgNum() == 26) {
            System.out.println("\nNo items to be sold.");
        }

        // Hardcoded inputs for testing viewSystemTransactions
        int userId = -1; // Admin user ID
        String government = "no_gov"; // Admin government

        // Test viewSystemTransactions function
        Result systemTransactionsResult = database.viewSystemTransactions(userId, government);

        // Check the result of viewSystemTransactions
        switch (systemTransactionsResult.getMsgNum()) {
            case 28 -> {
                System.out.println("All Transactions in the System:");
                String[] transactions = systemTransactionsResult.getStringArray().orElse(new String[0]);
                for (String transaction : transactions) {
                    System.out.println(transaction);
                }
            }
            case 29 -> System.out.println("No transaction found.");
            case 27 -> System.out.println("You don’t have access.");
            default -> System.out.println("Unexpected result.");
        }
       */
    }
   
}