package CLIbasis.CLIbasis;

import Inventory.JavaPythonBridge;
import Users.Admin;

import java.io.IOException;
import java.util.Scanner;

public class AdminCLI {
	public final static String INVALID = "Invalid input";

    public static void run(Scanner consoleInput, Admin admin) throws IOException {
    	System.out.println("ADMIN VIEW");

        while (true) {
        	printAdminMenu();

            try {
                int selection = Integer.parseInt(consoleInput.nextLine().trim());

                switch (selection) {
                    // displays all products
                    case 1:
                        JavaPythonBridge.run(JavaPythonBridge.GET_ADMIN_PRODUCTS, admin.getUserID());
                        System.out.println();
                        break;

                    // add new product
                    case 2:
                        System.out.println();
                        System.out.println("1) Add a board game");
                        System.out.println("2) Add an accessory");
                        System.out.println("PRESS ANY OTHER NUMBER BUTTON TO CANCEL");
                        int choice = Integer.parseInt(consoleInput.nextLine());
                        switch (choice) {
                            case 1:
                                System.out.println();
                                admin.addBoardGame(consoleInput);
                                break;
                            case 2:
                                System.out.println();
                                admin.addAccessory(consoleInput);
                                break;
                            default:
                                break;

                        }
                        System.out.println();
                        break;

                    case 0:
                        return;

                    default:
                        System.out.println(INVALID);
                        System.out.println();
                }
            } catch (NumberFormatException e) {
                System.out.println("Entry type is not recognised.\n");
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