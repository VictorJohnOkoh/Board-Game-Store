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

    public void addBoardGame(){
        Scanner input = new Scanner(System.in);
        int product_id;
        String name;
        String type;
        double price;
        int stock;
        double purchase_cost;
        int num_players;

        System.out.print("Enter the board game's ID: ");
        product_id = input.nextInt();
        System.out.print("Enter the board game's name: ");
        name = input.next();
        System.out.print("Enter the board game's type: ");
        type = input.next().toLowerCase();
        System.out.print("Enter the board game's price: ");
        price = input.nextDouble();
        System.out.print("Enter the board game's purchase cost: ");
        purchase_cost = input.nextDouble();
        System.out.print("Enter the board game's maximum number of players: ");
        num_players = input.nextInt();
        System.out.print("Enter the amount of stock: ");
        stock = input.nextInt();

        try(PrintWriter stock_file = new PrintWriter(new FileWriter(super.getStockFile(), true))){

            String new_entry = String.format("\n%d; board game; %s; %s; %.2f; %d; %.2f; %d\n", product_id, type, name, price, stock, purchase_cost, num_players);
            stock_file.append(new_entry);


        }catch (IOException e){
            System.out.println("Error: Could not access the stock file");
        }

        input.close();
    }

    public void addAccessory(){
        Scanner input = new Scanner(System.in);
        int product_id;
        String name;
        String type;
        double price;
        int stock;
        double purchase_cost;
        String compatibility;

        System.out.print("Enter the accessory's ID: ");
        product_id = input.nextInt();
        System.out.print("Enter the accessory's name: ");
        name = input.next();
        System.out.print("Enter the accessory's type: ");
        type = input.next();
        System.out.print("Enter the accessory's price: ");
        price = input.nextDouble();
        System.out.print("Enter the accessory's purchase cost: ");
        purchase_cost = input.nextDouble();
        System.out.print("Enter the accessory's compatibility: ");
        compatibility = input.next();
        System.out.print("Enter the amount of stock: ");
        stock = input.nextInt();

        try(PrintWriter stock_file = new PrintWriter(new FileWriter(super.getStockFile(), true))){
            String new_entry = String.format("\n%d; accessory; %s; %s; %.2f; %d; %.2f; %s\n", product_id, type, name, price, stock, purchase_cost, compatibility);
            stock_file.append(new_entry);

        }catch (IOException e){
            System.out.println("Error: Could not access the stock file");
        }

        input.close();
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
