import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.io.PrintWriter;

public class Admin extends User{

    public Admin(int id, String name, Address address){
        super(id, name, address, "admin");
    }

    public void addBoardGame(File productFile){
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
        type = input.next();
        System.out.print("Enter the board game's price: ");
        price = input.nextDouble();
        System.out.print("Enter the board game's purchase cost: ");
        purchase_cost = input.nextDouble();
        System.out.print("Enter the board game's maximum number of players: ");
        num_players = input.nextInt();
        System.out.print("Enter the amount of stock: ");
        stock = input.nextInt();

        try(PrintWriter stock_file = new PrintWriter(productFile)){

            String new_entry = String.format("%d; board game; %s; %s; %.2f; %d; %.2f; %d\n", product_id, type, name, price, stock, purchase_cost, num_players);
            stock_file.append(new_entry);


        }catch (IOException e){
            System.out.println("Error: Could not access the stock file");
        }

        input.close();
    }

    public void addAccessory(File productFile){
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

        try(PrintWriter stock_file = new PrintWriter(productFile)){
            String new_entry = String.format("%d; accessory; %s; %s; %.2f; %d; %.2f; %s\n", product_id, type, name, price, stock, purchase_cost, compatibility);
            stock_file.append(new_entry);

        }catch (IOException e){
            System.out.println("Error: Could not access the stock file");
        }

        input.close();
    }






}
