package CLIbasis.CLIbasis;

import Users.Customer;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class CustomerCLI {

    public static void run(Scanner consoleInput, Customer customer) {

        System.out.println("CUSTOMER VIEW");
        while (true) {
            printCustomerMenu();
            int choice = ValidationUtils.getChoice(consoleInput, "Enter your choice: ", 0, 6);
            
            try {
                switch (choice) {
                    // displays all products
                    case 1:
                        customer.viewProducts();
                        System.out.println();
                        break;
                        
                    // add product to the basket using the product ID
                    case 2:
                        int productId = ValidationUtils.getPositiveInt(consoleInput, "Enter product ID: ");
                        customer.basket.addShopping(productId);
                        System.out.println("Product added to basket successfully.\n");
                        break;
                        
                    // shows all contents of the customer's basket
                    case 3:
                        customer.showBasket();
                        System.out.println();
                        break;
                        
                    // pays for products in the basket
                    case 4:
                        if (ValidationUtils.checkBasketNotEmpty(customer.basket, "The basket is empty. Unable to pay.\n")) {
                            customer.pay(consoleInput);
                            System.out.println();
                        }
                        break;
                        
                    // empties the basket
                    case 5:
                        customer.basket.emptyBasket();
                        System.out.println("Basket has been cleared.\n");
                        break;
                        
                    // searches for products using product ID or compatibility
                    case 6:
                        String searchTerm = ValidationUtils.getNonEmptyString(consoleInput, "Search (either the Product ID or the compatibility NOT BOTH): ");
                        
                        if (ValidationUtils.isValidProductId(searchTerm)) {
                            System.out.println(customer.search(Integer.parseInt(searchTerm)));
                        } else if (ValidationUtils.isValidSearchTerm(searchTerm)) {
                            System.out.println(customer.search(searchTerm));
                        } else {
                            System.out.println("Invalid search term. Please enter a valid product ID or compatibility.\n");
                        }
                        System.out.println();
                        break;
                        
                    case 0:
                        return;
                }
            } catch (Exception e) {
                ValidationUtils.handleCustomerOperationException(e, "customer operation");
                System.out.println();
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