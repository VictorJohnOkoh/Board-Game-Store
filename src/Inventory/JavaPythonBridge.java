package Inventory;

import Users.Customer;
import jep.SharedInterpreter;

public class JavaPythonBridge {
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

    public static void run(String functionName, String[] args) {
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
}

