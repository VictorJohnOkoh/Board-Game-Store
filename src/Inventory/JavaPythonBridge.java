package Inventory;

import jep.SharedInterpreter;

public class JavaPythonBridge {

    // Names of the Python functions in DatabaseManager.py. These are the ONE place
    // to update if a function is renamed in the Python file - every Java call site
    // refers to these constants instead of repeating the raw string.
    public static final String ADD_BOARD_GAME     = "add_board_game";
    public static final String ADD_ACCESSORY      = "add_accessory";
    public static final String GET_ADMIN_PRODUCTS = "get_admin_products";
    public static final String GET_PRODUCTS       = "get_products";
    public static final String CLOSE_CONNECTION   = "close_connection";

//    running functions without any parameters
    public static void run(String functionName) {
        // SharedInterpreter opens an inline Python terminal inside your Java code
        try (SharedInterpreter interp = new SharedInterpreter()) {

            // Run your script file relative to your project directory
            interp.runScript("src\\Inventory\\DatabaseManager.py");

            // Calls the function name
            Object result = interp.invoke(functionName);
            if  (result == null) {
                System.out.println("Nothing was returned");
            } else {
                String output = result.toString();
                System.out.println(output);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static String run_result(String functionName) {
        // SharedInterpreter opens an inline Python terminal inside your Java code
        try (SharedInterpreter interp = new SharedInterpreter()) {

            // Run your script file relative to your project directory
            interp.runScript("src\\Inventory\\DatabaseManager.py");

            // Calls the function name
            Object result = interp.invoke(functionName);
            if  (result == null) {
                System.out.println("Nothing was returned");
            } else {
                return result.toString();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

//    running functions that need an ID
    public static void run(String functionName, int id) {
        // SharedInterpreter opens an inline Python terminal inside your Java code
        try (SharedInterpreter interp = new SharedInterpreter()) {

            // Run your script file relative to your project directory
            interp.runScript("src\\Inventory\\DatabaseManager.py");

            // Calls the function name
            Object result = interp.invoke(functionName, id);
            if  (result == null) {
                System.out.println("Nothing was returned");
            } else {
                String output = result.toString();
                System.out.println(output);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

//    for adding a board game object
    public static void run(String functionName, BoardGame bgame) {
        // SharedInterpreter opens an inline Python terminal inside your Java code
        try (SharedInterpreter interp = new SharedInterpreter()) {

            // Run your script file relative to your project directory
            interp.runScript("src\\Inventory\\DatabaseManager.py");

            // creates a python boardgame class for temporary data storage

            // Calls the addBoardGame function
            Object result = interp.invoke(functionName, bgame.getProductID(), bgame.getProductName(), bgame.getType(), bgame.getPrice(), bgame.getQuantityInStock(), bgame.getPurchaseCost(), bgame.getNumPlayers());
            if  (result == null) {
                System.out.println("Nothing was returned");
            } else {
                String output = result.toString();
                System.out.println(output);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

//    for adding an accessory object
    public static void run(String functionName, Accessory accessory) {

        try (SharedInterpreter interp = new SharedInterpreter()) {

            interp.runScript("src\\Inventory\\DatabaseManager.py");

            Object result = interp.invoke(functionName, accessory.getProductID(), accessory.getProductName(), accessory.getType(), accessory.getPrice(), accessory.getQuantityInStock(), accessory.getPurchaseCost(), accessory.getCompatibility());
            if (result == null) {
                System.out.println("Transaction failed");
            } else {
                String output = result.toString();
                System.out.println(output);
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

