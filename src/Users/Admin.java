package Users;

import Inventory.*;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Admin extends User{

    private final Stock stockClass = new Stock();

    public Admin(int id, String name, Address address) throws IOException {
        super(id, name, address, "admin");
    }

    public void addBoardGame(Scanner consoleInput){
        int product_id = 0;
        String name;
        String type;
        double price;
        int stock;
        double purchase_cost;
        int num_players;

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

        Product product = new BoardGame(product_id, type, name, price, purchase_cost, stock, num_players);
        stockClass.addStock(product);

    }

    public void addAccessory(Scanner consoleInput){
        int product_id = 0;
        String name;
        String type = "";
        double price;
        int stock;
        double purchase_cost;
        String compatibility;

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
            type = Accessory.ACCESSORY_KIT;
        } else if (choice == 2){
            type = Accessory.MINIATURE;
        } else if (choice == 3){
            type = Accessory.DICE;
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

        Product product = new Accessory(product_id, type, name, price, purchase_cost, stock, compatibility);
        stockClass.addStock(product);

    }

    // Allows the customer to view the list of available products in descending order of the unit price
    public String viewProducts() throws IOException {
        return stockClass.showStockAdmin();
    }

    public String toString(){
        return String.format("%d | %s | %s", getUserID(), getUserName(), getRole());
    }
}
