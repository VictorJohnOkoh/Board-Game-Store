package Bridge;

import java.nio.file.Path;

import Inventory.Accessory;
import Inventory.BoardGame;
import jep.SharedInterpreter;

public class JavaPythonBridge {

    // Names of the Python functions in DatabaseManager.py. These are the ONE place
    // to update if a function is renamed in the Python file - every Java call site
    // refers to these constants instead of repeating the raw string.
    public static final String ADD_BOARD_GAME     = "add_board_game";
    public static final String ADD_ACCESSORY      = "add_accessory";
    public static final String GET_ADMIN_PRODUCTS = "get_admin_products";
    public static final String GET_ADMIN_PRODUCTS_RAW = "get_admin_products_raw";
    public static final String GET_PRODUCTS       = "get_products";
    public static final String CLOSE_CONNECTION   = "close_connection";
    public static final String GET_PRODUCT_BY_ID = "get_product_by_id";
    public static final String GET_USER_DETAILS = "get_user_details";
    public static final String UPDATE_STOCK      = "update_stock";
    public static final String FILTER_ID = "filter_product_id";
    public static final String FILTER_COMPATIBILITY = "filter_product_compatibility";
    // Customer-facing parseable variants. These omit pcost so the GUI can never
    // display a purchase cost to a customer - see get_products_raw in the Python.
    public static final String GET_PRODUCTS_RAW = "get_products_raw";
    public static final String FILTER_ID_RAW = "filter_product_id_raw";
    public static final String FILTER_COMPATIBILITY_RAW = "filter_product_compatibility_raw";
    public static final String ROLLBACK = "rollback";

    // global sharedInterpreter.
    // JEP locates its own native library via PYTHONHOME, which the launcher script sets - see
    // Scripts/run.bat and Scripts/run.sh. Nothing here loads native libraries by hand.
    private final static SharedInterpreter interp = new SharedInterpreter();
    static {
        try {
            Path scriptPath = PythonScriptLoader.tempCopy();
            // JEP's runScript() does not define __file__, unlike `python script.py`, and
            // DatabaseManager.py resolves the database relative to its own location.
            interp.set("__file__", scriptPath.toString());
            interp.runScript(scriptPath.toString());
        } catch (RuntimeException e) {
            System.err.println("Could not connect to the DBMS script: " + e.getMessage());
        }
    }


/*
Every method here returns what Python returned and prints nothing, so the same call works
from the CLI and the GUI - the interface decides how to show the outcome. A null return
means Python returned None or the call failed; the failure itself is logged to System.err,
which is a log for whoever is debugging rather than output for the user.
 */

//    running functions without any parameters
    public static String run_result(String functionName) {
        // SharedInterpreter opens an inline Python terminal inside your Java code
        try {
            // Calls the function name
            Object result = interp.invoke(functionName);
            return result == null ? null : result.toString();
        } catch (Exception e) {
            return logFailure(functionName, e);
        }
    }

//    running functions that need an ID
    public static String run_result(String functionName, int id) {
        try  {

            Object result = interp.invoke(functionName, id);
            return result == null ? null : result.toString();
        } catch (Exception e) {
            return logFailure(functionName, e);
        }
    }

//    for adding a board game object
    public static String run_result(String functionName, BoardGame bgame) {
        // SharedInterpreter opens an inline Python terminal inside your Java code
        try  {

            // Calls the addBoardGame function
            Object result = interp.invoke(functionName, bgame.getProductID(), bgame.getProductName(), bgame.getType(), bgame.getPrice(), bgame.getQuantityInStock(), bgame.getPurchaseCost(), bgame.getNumPlayers());
            return result == null ? null : result.toString();
        } catch (Exception e) {
            return logFailure(functionName, e);
        }
    }

    /** Passes the information for an accessory to be added to the database*/
    public static String run_result(String functionName, Accessory accessory) {

        try  {

            Object result = interp.invoke(functionName, accessory.getProductID(), accessory.getProductName(), accessory.getType(), accessory.getPrice(), accessory.getQuantityInStock(), accessory.getPurchaseCost(), accessory.getCompatibility());
            return result == null ? null : result.toString();
        } catch (Exception e) {
            return logFailure(functionName, e);
        }
    }

    public static String run_result(String functionName, String data) {
        try {

            Object result = interp.invoke(functionName, data);
            return result == null ? null : result.toString();
        } catch (Exception e) {
            return logFailure(functionName, e);
        }
    }

    public static String run_result(String functionName, String name, int id) {
        try {

            Object result = interp.invoke(functionName, name, id);
            return result == null ? null : result.toString();
        } catch (Exception e) {
            return logFailure(functionName, e);
        }
    }

    public static String run_result(String functionName, int id, int amount, String category) {
        try {

            Object result = interp.invoke(functionName, id, amount, category);
            return result == null ? null : result.toString();
        } catch (Exception e) {
            return logFailure(functionName, e);
        }
    }

    /**
     * Records a failed call and returns the null every caller treats as "no answer".
     * Kept out of stdout so it can never be mistaken for a value or interleave with
     * the CLI's menus.
     */
    private static String logFailure(String functionName, Exception e) {
        System.err.println("Error calling " + functionName + ": " + e.getMessage());
        return null;
    }

    public static String updateStock(java.util.Map<Integer, Integer> basketData) {
        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<Integer, Integer> entry : basketData.entrySet()) {
            if (!sb.isEmpty()) sb.append(";");
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        return run_result(UPDATE_STOCK, sb.toString());
    }

    public static void close() {
        try {
            interp.close();
        } catch (Exception e) {
            System.err.println("Error closing SharedInterpreter: " + e.getMessage());
        }
    }


}
