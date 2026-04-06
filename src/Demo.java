import java.io.IOException;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import Inventory.*;
import Users.*;

public class Demo {
     static void main(String[] args) throws IOException {
        File user_file = new File("UserAccount.txt");
        File product_file = new File("Stock.txt");
        ArrayList<User> userList = new ArrayList<>();
        ArrayList<Product> productList = new ArrayList<>();
        try (Scanner scanner = new Scanner(user_file)) {
            while (scanner.hasNextLine()) {
                String[] contents = scanner.nextLine().split(";");
                User user = getUser(contents);
                userList.add(user);
            }
        } catch (FileNotFoundException e) {
            IO.println("Error: Could not open the customer account file");
        }
        try (Scanner scanner = new Scanner(product_file)){
            while (scanner.hasNextLine()) {
                 String[] contents = scanner.nextLine().split(";");
                 Product product = getProduct(contents);
                 productList.add(product);
            }
        } catch (FileNotFoundException e){
            IO.println("Error: could not open the products/stock file");
        }
        Admin admin = (Admin) userList.getFirst();
        Customer cust1 = (Customer) userList.get(1);

         for (int i = 0; i<productList.size(); i++){
             cust1.addShopping(productList.get(i));
         }
         for (int i = 0; i<productList.size(); i++){
             cust1.addShopping(productList.get(i));
         }
         System.out.println(cust1.showBasket());
         cust1.pay();

    }

    private static User getUser(String[] contents) {
        int id = Integer.parseInt(contents[0].trim());
        String name = contents[1].trim();
        int housenum = Integer.parseInt(contents[2].trim());
        String postcode = contents[3].trim();
        String city = contents[4].trim();
        String role = contents[5].trim();
        Customer customer;
        Admin admin;
        Address address = new Address(housenum, postcode, city);
        if (role.equalsIgnoreCase("customer")) {
            customer = new Customer(id, name, address);
            return customer;
        } else {
            admin = new Admin(id, name, address);
            return admin;
        }
    }

    private static Product getProduct(String[] contents){
        for (int i = 0; i<contents.length; i++){
            String temp = contents[i].trim();
            contents[i] = temp;
        }
        if (contents[1].equals("accessory")){
            return new Accessory(contents);
        } else {
            return new BoardGame(contents);
        }

    }
}