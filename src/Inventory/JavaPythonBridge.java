package Inventory;

import jep.SharedInterpreter;

public class JavaPythonBridge {
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

