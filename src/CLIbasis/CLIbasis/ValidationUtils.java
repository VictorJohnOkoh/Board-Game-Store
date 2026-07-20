package CLIbasis.CLIbasis;

import java.util.Scanner;

public class ValidationUtils {

    /**
     * Prompts user for input and validates it's a positive integer.
     * Re-prompts until valid input is provided.
     */
    public static int getPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine());
                if (value > 0) {
                    return value;
                } else {
                    System.out.println("Please enter a positive integer greater than 0.\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.\n");
            }
        }
    }

    /**
     * Prompts user for input and validates it's an integer within the given range.
     * Re-prompts until valid input is provided.
     */
    public static int getIntInRange(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine());
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.println("Please enter a number between " + min + " and " + max + ".\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.\n");
            }
        }
    }

    /**
     * Prompts user for non-empty string input.
     * Re-prompts until non-empty input is provided.
     */
    public static String getNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            } else {
                System.out.println("Input cannot be empty. Please try again.\n");
            }
        }
    }

    /**
     * Validates a product ID (must be positive integer).
     */
    public static boolean isValidProductId(String input) {
        try {
            int id = Integer.parseInt(input);
            return id > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates a search term (non-empty string).
     */
    public static boolean isValidSearchTerm(String input) {
        return input != null && !input.trim().isEmpty();
    }

    /**
     * Handles exceptions from customer operations with user feedback.
     */
    public static void handleCustomerOperationException(Exception e, String operationName) {
        System.out.println("Error during " + operationName + ": " + e.getMessage());
        if (e.getClass().getName().contains("NumberFormatException")) {
            System.out.println("Please check your input format.");
        } else if (e.getClass().getName().contains("IndexOutOfBoundsException")) {
            System.out.println("The requested item does not exist in the basket.");
        } else {
            System.out.println("An unexpected error occurred. Please try again.");
        }
    }

    /**
     * Prompts user for a choice between options with validation.
     */
    public static int getChoice(Scanner scanner, String prompt, int minOption, int maxOption) {
        while (true) {
            System.out.print(prompt);
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice >= minOption && choice <= maxOption) {
                    return choice;
                } else {
                    System.out.println("Please enter a number between " + minOption + " and " + maxOption + ".\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.\n");
            }
        }
    }

    /**
     * Checks if the basket is empty before performing an operation.
     */
    public static boolean checkBasketNotEmpty(Object basket, String message) {
        try {
            java.lang.reflect.Method isEmpty = basket.getClass().getMethod("isEmpty");
            boolean empty = (boolean) isEmpty.invoke(basket);
            if (empty) {
                System.out.println(message);
                return false;
            }
            return true;
        } catch (Exception e) {
            System.out.println("Unable to check basket status.");
            return false;
        }
    }

    /**
     * Prompts user for input and validates it contains only English letters (a-z, A-Z).
     * Re-prompts until valid input is provided.
     */
    public static String getAlphaString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.matches("[a-zA-Z]+")) {
                return input;
            } else {
                System.out.println("Invalid input. Please enter only English letters (no numbers or special characters).\n");
            }
        }
    }

}