package CLIbasis.CLIbasis;

import Users.Admin;
import java.util.Scanner;

public class AdminCLI {
    public static void run(Scanner consoleInput, Admin admin) {
    	System.out.println("ADMIN VIEW");

        while (true) {
        	printAdminMenu();

            int selection = ValidationUtils.getChoice(consoleInput, "Enter your choice: ", 0, 2);
            
            try {
                switch (selection) {
                    // displays all products
                    case 1:
                        admin.viewProducts();
                        System.out.println();
                        break;

                    // add new product
                    case 2:
                        int choice = ValidationUtils.getChoice(consoleInput, "Select product type (1=Board Game, 2=Accessory, 0=Cancel): ", 0, 2);
                        
                        switch (choice) {
                            case 1:
                                admin.addBoardGame(consoleInput);
                                System.out.println();
                                break;
                            case 2:
                                admin.addAccessory(consoleInput);
                                System.out.println();
                                break;
                            default:
                                break;
                        }
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
    
    private static void printAdminMenu() {
        System.out.println("PLEASE SELECT ACTION BY INPUTTING THE CORRESPONDING NUMBER (or 0 for logout)");
        System.out.println("1) View all products");
        System.out.println("2) Add new product");
        

        System.out.println("0) Log out");
    }
}