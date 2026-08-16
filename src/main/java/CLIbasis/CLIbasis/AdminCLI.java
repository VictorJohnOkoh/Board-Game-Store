package CLIbasis.CLIbasis;

import Users.Admin;
import java.util.Scanner;

public class AdminCLI {
    public static void run(Scanner consoleInput, Admin admin) {
    	System.out.println("ADMIN VIEW");

        while (true) {
        	printAdminMenu();

            int selection = ValidationUtils.getChoice(consoleInput, "Enter your choice: ", 0, 3);
            
            try {
                switch (selection) {
                    // displays all products
                    case 1:
                        String products = admin.viewProducts();
                        System.out.println(products == null ? "Products could not be loaded." : products);
                        System.out.println();
                        break;

                    // add new product
                    case 2:
                        int choice = ValidationUtils.getChoice(consoleInput, "Select product type (1=Board Game, 2=Accessory, 0=Cancel): ", 0, 2);

                        switch (choice) {
                            case 1:
                                System.out.println(describeAddResult(admin.addBoardGame(consoleInput)));
                                break;
                            case 2:
                                System.out.println(describeAddResult(admin.addAccessory(consoleInput)));
                                break;
                            default:
                                break;
                        }
                        break;
                    case 3:
                        System.out.println(describeRollback(Admin.rollbackDatabase()));
                        break;
                    case 0:
                        return;
                }
            } catch (Exception e) {
                ValidationUtils.handleCustomerOperationException(e, "admin operation");
                System.out.println();
            }
        }
    }
    
    /** Turns the outcome of adding a product into the message shown on the console. */
    private static String describeAddResult(Admin.AddResult result) {
        return switch (result) {
            case ADDED -> "Product added successfully.\n";
            case DUPLICATE_ID -> "A product with that ID already exists. Nothing was added.\n";
            case DUPLICATE_NAME -> "A product with that name already exists. Nothing was added.\n";
            case DUPLICATE_BOTH -> "A product with that ID, and a product with that name, already exist. Nothing was added.\n";
            case FAILED -> "The product could not be added. Please try again.\n";
        };
    }

    /** Turns the outcome of a rollback into the message shown on the console. */
    private static String describeRollback(Admin.RollbackResult result) {
        return switch (result) {
            case RESTORED -> "Database rolled back to the last backup.\n";
            case NO_BACKUP -> "There is no backup to roll back to.\n";
            case FAILED -> "The database could not be rolled back.\n";
        };
    }

    private static void printAdminMenu() {
        System.out.println("PLEASE SELECT ACTION BY INPUTTING THE CORRESPONDING NUMBER (or 0 for logout)");
        System.out.println("1) View all products");
        System.out.println("2) Add new product");
        System.out.println("3) Roll back database");
        

        System.out.println("0) Log out");
    }
}