package Users;

import Inventory.*;

import java.util.Scanner;


public class Admin extends User{

    public Admin(int id, String name, Address address) {
        super(id, name, address, "admin");
    }

    // adds a boardgame to the stock file
    public void addBoardGame(Scanner consoleInput) {
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
        name = consoleInput.nextLine();
        System.out.print("Enter the board game's type: ");
        type = consoleInput.nextLine().toLowerCase();
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
      JavaPythonBridge.run(JavaPythonBridge.ADD_BOARD_GAME, product);

    }

    // adds an accessory to the stock file
    public void addAccessory(Scanner consoleInput) {
        int product_id = 0;
        String name;
        AccessoryType type = null;
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
        name = consoleInput.nextLine();
        System.out.println("What is the accessory's type: \n1) accessory kit 2) miniature 3) dice");
        int choice = Integer.parseInt(consoleInput.nextLine());
        if (choice == 1){
            type = AccessoryType.accessory_kit;
        } else if (choice == 2){
            type = AccessoryType.miniature;
        } else if (choice == 3){
            type = AccessoryType.dice;
        } else {
            System.out.println("Invalid choice");
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
        JavaPythonBridge.run(JavaPythonBridge.ADD_ACCESSORY, product);
    }


    public void viewProducts() {
         System.out.println(JavaPythonBridge.run(JavaPythonBridge.GET_ADMIN_PRODUCTS, getUserID()));
    }

    public String toString(){
        return String.format("%d | %s | %s", getUserID(), getUserName(), getRole());
    }
}
