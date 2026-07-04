package CLIbasis.CLIbasis;

import Users.Customer;
import Inventory.JavaPythonBridge;
import java.io.IOException;
import java.util.Scanner;

public class CustomerCLI {

    public static void run(Scanner consoleInput, Customer customer) throws IOException {

        System.out.println("CUSTOMER VIEW");
        while (true) {
            printCustomerMenu();
            try {
                int choice = Integer.parseInt(consoleInput.nextLine());
                switch (choice) {
                    // displays all products
                    case 1:
                        JavaPythonBridge.run("getProducts");
                        break;
                    // add product to the basket using the product ID
                    case 2:
                        System.out.print("Enter product ID: ");
                        customer.basket.addShopping(Integer.parseInt(consoleInput.nextLine()));
                        break;
                    // shows all contents of the customer's basket
                    case 3:
                        System.out.println(customer.showBasket());
                        System.out.println();
                        break;
                    // pays for products in the basket
                    case 4:
                        customer.pay(consoleInput);
                        System.out.println();
                        break;
                    // empties the basket
                    case 5:
                        customer.basket.emptyBasket();
                        System.out.println();
                        break;
                    // searches for products using product ID
                    case 6:
                        System.out.print("Search (either the Product ID or the compatibility NOT BOTH): ");
                        String term = consoleInput.nextLine();
                        try{
                            int inputInt = Integer.parseInt(term);
                            System.out.println(customer.search(inputInt));

                        } catch (NumberFormatException e) {
                            System.out.println(customer.search(term));
                        }
                        System.out.println();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Please enter a number between 1-6 for an option or 0 to log out\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid entry type\n");
            }
        }

    }
    
    private static void printCustomerMenu() {
        System.out.println("PLEASE SELECT ACTION BY INPUTTING THE CORRESPONDING NUMBER (or 0 for logout)");
        System.out.println("1) View all products");
        System.out.println("2) Add product to shopping basket");
        System.out.println("3) View contents of shopping basket");
        System.out.println("4) Purchase items in the basket");
        System.out.println("5) Cancel shopping basket");
        System.out.println("6) Lookup with product ID or compatibility");

        System.out.println("0) Log out");

    }

}