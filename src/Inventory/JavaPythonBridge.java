package Inventory;

import jep.SharedInterpreter;
import jep.MainInterpreter;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class JavaPythonBridge {

    // Names of the Python functions in DatabaseManager.py. These are the ONE place
    // to update if a function is renamed in the Python file - every Java call site
    // refers to these constants instead of repeating the raw string.
    public static final String ADD_BOARD_GAME     = "add_board_game";
    public static final String ADD_ACCESSORY      = "add_accessory";
    public static final String GET_ADMIN_PRODUCTS = "get_admin_products";
    public static final String GET_PRODUCTS       = "get_products";
    public static final String CLOSE_CONNECTION   = "close_connection";
    public static final String GET_PRODUCT_BY_ID = "get_product_by_id";
    public static final String GET_USER_DETAILS = "get_user_details";
    public static final String UPDATE_STOCK      = "update_stock";
    public static final String FILTER_ID = "filter_product_id";
    public static final String FILTER_COMPATIBILITY = "filter_product_compatibility";
    public static final String ROLLBACK = "rollback";

    static {
        // Point towards the jep.dll file in the lib/jep folder relative to the JAR file location
        try {
          File jarDir = new File(JavaPythonBridge.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
          File dll = new File(jarDir, "lib\\jep\\jep.dll");
          MainInterpreter.setJepLibraryPath(dll.getAbsolutePath());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    // global sharedInterpreter
    private final static SharedInterpreter interp = new SharedInterpreter();

    static {

        String scriptPathToUse;

        // Resolve the database path from the app location first, then fall back to the working directory.
        File dbFile = resolveDatabaseFile();
        String DB_PATH = dbFile.getAbsolutePath();

        // Try to find the Python script in the source directory
        java.io.File scriptFile = new java.io.File("src\\Inventory\\DatabaseManager.py");
        // temporary path for extracted Python script
        String TEMP_SCRIPT_PATH;
        if (scriptFile.exists()) {
            TEMP_SCRIPT_PATH = null;
            scriptPathToUse = "src\\Inventory\\DatabaseManager.py";
        } else {
            // Fallback: extract script from JAR resources to a temp file
            try {
                java.io.File tempScript = java.io.File.createTempFile("DatabaseManager", ".py");
                java.io.InputStream scriptIn = JavaPythonBridge.class.getResourceAsStream("/Inventory/DatabaseManager.py");
                if (scriptIn != null) {
                    java.io.OutputStream scriptOut = new java.io.FileOutputStream(tempScript);
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = scriptIn.read(buffer)) != -1) {
                        scriptOut.write(buffer, 0, bytesRead);
                    }
                    scriptOut.close();
                    scriptIn.close();
                    TEMP_SCRIPT_PATH = tempScript.getAbsolutePath();
                    scriptPathToUse = TEMP_SCRIPT_PATH;
                } else {
                    scriptPathToUse = "src\\Inventory\\DatabaseManager.py";
                }
            } catch (Exception e) {
                System.err.println("Error extracting Python script: " + e.getMessage());
                scriptPathToUse = "src\\Inventory\\DatabaseManager.py";
            }
        }

        try {
            interp.set("db_path", DB_PATH);
            interp.runScript(scriptPathToUse);
        } catch (Exception e) {
            System.err.println("Error initializing SharedInterpreter: " + e.getMessage());
        }
    }

    private static File resolveDatabaseFile() {
        List<File> searchRoots = new ArrayList<>();

        File codeLocation = getCodeLocation();
        if (codeLocation != null) {
            searchRoots.add(codeLocation);
        }

        File workingDir = new File(System.getProperty("user.dir"));
        if (workingDir != null) {
            searchRoots.add(workingDir);
        }

        for (File root : searchRoots) {
            File resolved = findDatabaseFileFromRoot(root);
            if (resolved != null) {
                return resolved;
            }
        }

        return new File(workingDir, "data" + File.separator + "StoreData.db");
    }

    private static File findDatabaseFileFromRoot(File startRoot) {
        File current = startRoot;
        for (int i = 0; i < 8 && current != null; i++) {
            File candidate = new File(current, "data" + File.separator + "StoreData.db");
            if (candidate.exists()) {
                return candidate;
            }

            File nestedCandidate = new File(current, "Board Game Store" + File.separator + "data" + File.separator + "StoreData.db");
            if (nestedCandidate.exists()) {
                return nestedCandidate;
            }

            current = current.getParentFile();
        }
        return null;
    }

    private static File getCodeLocation() {
        try {
            File codeSource = new File(JavaPythonBridge.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (codeSource.isFile()) {
                return codeSource.getParentFile();
            }
            return codeSource;
        } catch (URISyntaxException e) {
            return null;
        }
    }

/*
A run method typically will not return anything while a run_result method returns some value(s)
 */

//    running functions without any parameters
    public static void run(String functionName) {
        // SharedInterpreter opens an inline Python terminal inside your Java code
        try  {
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
        try {
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
    public static String run(String functionName, int id) {
        // SharedInterpreter opens an inline Python terminal inside your Java code
        try {

            // Calls the function name
            Object result = interp.invoke(functionName, id);
            if  (result == null) {
                return null;
            } else {
                return result.toString();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return "Something went wrong";
    }

//    for adding a board game object
    public static void run(String functionName, BoardGame bgame) {
        // SharedInterpreter opens an inline Python terminal inside your Java code
        try  {

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

    /** Passes the information for an accessory to be added to the database*/
    public static void run(String functionName, Accessory accessory) {

        try  {

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

    public static String run_result(String functionName, int id) {
        try  {

            Object result = interp.invoke(functionName, id);
            if (result == null) return null;
            return result.toString();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    public static String run_result(String functionName, String data) {
        try {

            Object result = interp.invoke(functionName, data);
            if (result == null) return null;
            return result.toString();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    public static String run_result(String functionName, String name, int id) {
        try {

            Object result = interp.invoke(functionName, name, id);
            if (result == null) return null;
            return result.toString();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    public static String run_result(String functionName, int id, int amount, String category) {
        try {

            Object result = interp.invoke(functionName, id, amount, category);
            if (result == null) return null;
            return result.toString();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
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
            System.out.println("Error closing SharedInterpreter: " + e.getMessage());
        }
        System.out.println("Bridge closed successfully.");
        return;
    }

    public static void rollback() {
        try {
            interp.invoke(ROLLBACK);
        } catch (Exception e) {
            System.out.println("Error rolling back database: " + e.getMessage());
        }
    }

}
