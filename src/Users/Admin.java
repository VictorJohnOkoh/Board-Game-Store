package Users;

import Inventory.Accessory;
import Inventory.BoardGame;
import Inventory.Product;
import Inventory.ProductCategory;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Admin extends User{

    public Admin(int id, String name, Address address){
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


        try(PrintWriter stock_file = new PrintWriter(new FileWriter(super.getStockFile(), true))){

            String new_entry = String.format("\n%d; board game; %s; %s; %.2f; %d; %.2f; %d", product_id, type, name, price, stock, purchase_cost, num_players);
            stock_file.append(new_entry);


        }catch (IOException e){
            System.out.println("Error: Could not access the stock file");
        }

    }

    public void addAccessory(Scanner consoleInput){
        int product_id = 0;
        String name;
        String type;
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
        System.out.print("Enter the accessory's type: ");
        type = consoleInput.nextLine().toLowerCase();
        System.out.print("Enter the accessory's price: ");
        price = Double.parseDouble(consoleInput.nextLine());
        System.out.print("Enter the accessory's purchase cost: ");
        purchase_cost = Double.parseDouble(consoleInput.nextLine());
        System.out.print("Enter the accessory's maximum number of players: ");
        compatibility = consoleInput.nextLine();
        System.out.print("Enter the amount of stock: ");
        stock = Integer.parseInt(consoleInput.nextLine());

        try(PrintWriter stock_file = new PrintWriter(new FileWriter(super.getStockFile(), true))){
            String new_entry = String.format("\n%d; accessory; %s; %s; %.2f; %d; %.2f; %s", product_id, type, name, price, stock, purchase_cost, compatibility);
            stock_file.append(new_entry);

        }catch (IOException e){
            System.out.println("Error: Could not access the stock file");
        }

    }

    // Allows the customer to view the list of available products in descending order of the unit price
    public String viewProducts() throws IOException {
        List<String> lines = Files.readAllLines(super.getStockFile().toPath());
        ArrayList<List<String>> splitlines = new ArrayList<>();
        for (String line : lines) {
            splitlines.add(List.of(line.split(";")));
        }
        ArrayList<Product> listedProducts = loadProducts(splitlines);
        ArrayList<Product> orderedLines = super.descOrder(listedProducts);
        StringBuilder output = new StringBuilder();
        for (Product line : orderedLines) {
            if (line.getCategory().equals(ProductCategory.BOARDGAME)) {
                BoardGame game = (BoardGame) line;
                output.append(game);
            } else{
                Accessory accessory = (Accessory) line;
                output.append(accessory);
            }
            output.append("\n");
        }
        return output.toString();
    }

    public String toString(){
        return String.format("%d | %s | %s", getUserID(), getUserName(), getRole());
    }
}
