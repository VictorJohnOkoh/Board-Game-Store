package Users;

import CLIbasis.CLIbasis.ValidationUtils;
import Inventory.Accessory;
import Inventory.AccessoryType;
import Inventory.BoardGame;
import Bridge.JavaPythonBridge;

import java.util.Scanner;


public class Admin extends User{

    /**
     * The outcome of adding a product, so callers can tell the admin why an add did
     * not happen. Returning this instead of printing keeps adding a product usable
     * from both the CLI and the GUI - System.out messages are invisible in a JavaFX
     * window, and the GUI used to report success even when the database refused the row.
     */
    public enum AddResult {
        ADDED,
        DUPLICATE_ID,
        DUPLICATE_NAME,
        DUPLICATE_BOTH,
        FAILED
    }

    /**
     * The outcome of rolling the database back, for the same reason as {@link AddResult}:
     * the interface has to be able to tell a restore that happened from one that did not.
     */
    public enum RollbackResult {
        RESTORED,
        NO_BACKUP,
        FAILED
    }

    public Admin(int id, String name, Address address) {
        super(id, name, address, "admin");
    }

    /**
     * Adds a board game to the database.
     *
     * @return what happened, so the caller can report it to the admin
     */
    public static AddResult addProduct(BoardGame game) {
        return interpretAdd(JavaPythonBridge.run_result(JavaPythonBridge.ADD_BOARD_GAME, game));
    }

    /**
     * Adds an accessory to the database.
     *
     * @return what happened, so the caller can report it to the admin
     */
    public static AddResult addProduct(Accessory accessory) {
        return interpretAdd(JavaPythonBridge.run_result(JavaPythonBridge.ADD_ACCESSORY, accessory));
    }

    /**
     * Restores the database from the last backup.
     *
     * @return what happened, so the caller can report it to the admin
     */
    public static RollbackResult rollbackDatabase() {
        return interpretRollback(JavaPythonBridge.run_result(JavaPythonBridge.ROLLBACK));
    }

    /**
     * Turns the status token from add_board_game/add_accessory into a result.
     * A null means the call itself failed, which the bridge has already logged.
     * <p>
     * Kept separate from the calls above, and visible to the package, so the mapping can be
     * exercised without a database - the interface reads this to decide what to tell the admin.
     */
    static AddResult interpretAdd(String pythonResult) {
        if (pythonResult == null) {
            return AddResult.FAILED;
        }
        return switch (pythonResult) {
            case "ADDED" -> AddResult.ADDED;
            case "DUPLICATE_ID" -> AddResult.DUPLICATE_ID;
            case "DUPLICATE_NAME" -> AddResult.DUPLICATE_NAME;
            case "DUPLICATE_BOTH" -> AddResult.DUPLICATE_BOTH;
            default -> AddResult.FAILED;
        };
    }

    /** Turns the status token from the Python rollback into a result. See {@link #interpretAdd}. */
    static RollbackResult interpretRollback(String pythonResult) {
        if (pythonResult == null) {
            return RollbackResult.FAILED;
        }
        return switch (pythonResult) {
            case "ROLLED_BACK" -> RollbackResult.RESTORED;
            case "NO_BACKUP" -> RollbackResult.NO_BACKUP;
            default -> RollbackResult.FAILED;
        };
    }

    // adds a boardgame to the stock file
    public AddResult addBoardGame(Scanner consoleInput) {
        int product_id = 0;
        String name;
        String type;
        double price;
        int stock;
        double purchase_cost;
        int num_players;

        // loops while the length of the new ID isn't 4 characters
        boolean pass = false;
        while (!pass) {
            System.out.print("Enter the board game's ID: ");
            product_id = Integer.parseInt(consoleInput.nextLine());
            String test = String.format("%d", product_id);
            if (test.length() != 4) {
                System.out.println("Incorrect length");
            } else {
                pass = true;
            }
        }
        System.out.print("Enter the board game's name: ");
        name = ValidationUtils.getNonEmptyString(consoleInput, "");
        while (true) {
            System.out.print("Enter the board game's type: ");
            String input = consoleInput.nextLine().trim();
            if (!input.matches("[a-zA-Z]+")) {
                System.out.println("Invalid input. Please enter only English letters.\n");
                continue;
            }
            type = input.toLowerCase();
            break;
        }
        System.out.print("Enter the board game's price: ");
        price = Double.parseDouble(consoleInput.nextLine());
        System.out.print("Enter the board game's purchase cost: ");
        purchase_cost = Double.parseDouble(consoleInput.nextLine());
        System.out.print("Enter the board game's maximum number of players: ");
        num_players = Integer.parseInt(consoleInput.nextLine());
        System.out.print("Enter the amount of stock: ");
        stock = Integer.parseInt(consoleInput.nextLine());

      BoardGame product = new BoardGame(product_id, type, name, price, purchase_cost, stock, num_players);
//      User.stockClass.addStock(product);
      return addProduct(product);

    }

    // adds an accessory to the stock file
    public AddResult addAccessory(Scanner consoleInput) {
        int product_id = 0;
        String name;
        AccessoryType type;
        double price;
        int stock;
        double purchase_cost;
        String compatibility;

        // loops while the new ID isn't 4 characters long
        boolean pass = false;
        while (!pass) {
            System.out.print("Enter the accessory's ID: ");
            product_id = Integer.parseInt(consoleInput.nextLine());
            String test = String.format("%d", product_id);
            if (test.length() != 4) {
                System.out.println("Incorrect length");
            } else {
                pass = true;
            }
        }
        System.out.print("Enter the accessory's name: ");
        name = ValidationUtils.getNonEmptyString(consoleInput, "");
        while (true) {
            System.out.println("What is the accessory's type: \n1) accessory kit 2) miniature 3) dice");
            String input = consoleInput.nextLine().trim();
            if (!input.matches("[0-9]+")) {
                System.out.println("Invalid input. Please enter a number (1, 2, or 3).\n");
                continue;
            }
            int choice = Integer.parseInt(input);
            if (choice == 1) {
                type = AccessoryType.accessory_kit;
                break;
            } else if (choice == 2) {
                type = AccessoryType.miniature;
                break;
            } else if (choice == 3) {
                type = AccessoryType.dice;
                break;
            } else {
                System.out.println("Invalid choice. Please enter 1, 2, or 3.\n");
            }
        }
        System.out.print("Enter the accessory's price: ");
        price = Double.parseDouble(consoleInput.nextLine());
        System.out.print("Enter the accessory's purchase cost: ");
        purchase_cost = Double.parseDouble(consoleInput.nextLine());
        System.out.print("Enter the accessory's compatibility: ");
        compatibility = consoleInput.nextLine();
        System.out.print("Enter the amount of stock: ");
        stock = Integer.parseInt(consoleInput.nextLine());

        Accessory product = new Accessory(product_id, type, name, price, purchase_cost, stock, compatibility);
//        User.stockClass.addStock(product);
        return addProduct(product);
    }


    /**
     * @return every product with its purchase cost, ready to print, or null when the
     *         database could not be read - the caller decides what to show either way
     */
    public String viewProducts() {
         return JavaPythonBridge.run_result(JavaPythonBridge.GET_ADMIN_PRODUCTS, getUserID());
    }

    public String toString(){
        return String.format("%d | %s | %s", getUserID(), getName(), getRole());
    }
}
