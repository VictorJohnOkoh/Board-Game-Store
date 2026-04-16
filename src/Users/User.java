package Users;

import Inventory.Accessory;
import Inventory.BoardGame;
import Inventory.Product;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public abstract class User {
   private final int userID;
   private final String name;
   private final Address address;
   private final String role;
   private static File stockFile;

   public User(int id, String n, Address addr, String r){
       userID = id;
       name = n;
       address = addr;
       role = r;
       stockFile = new File("Stock.txt");
   }

   public File getStockFile(){
       return stockFile;
   }

   public String getRole(){
       return role;
   }

   public int getUserID(){
       return userID;
   }

   public String getUserName(){
       return name;
   }

   public Address getAddress() {
       return address;
   }

   public abstract String toString();

   public abstract String viewProducts() throws IOException;
    /*
     Takes in an array of Products and sorts them into descending order of price
     then returns an array of Products
    */
    public ArrayList<Product> descOrder(ArrayList<Product> list){
        Product temp;
        for (int i = 0; i<list.size()-2; i++){
            boolean swapped = false;
            for (int j = 0; j<list.size()-2-i; j++) {
                double currentPrice = list.get(j).getPrice();
                double nextPrice = list.get(j+1).getPrice();
                if (nextPrice > currentPrice){
                    temp = list.get(j);
                    list.set(j, list.get(j+1));
                    list.set(j+1, temp);
                    swapped = true;
                }
            }
            if (!swapped){
                break;
            }
        }
        return list;
    }

    // Returns an arrayList of Products after being passed a 2D array from a line in the stock file
    protected ArrayList<Product> loadProducts(ArrayList<List<String>> splitContents) {
        ArrayList<Product> productList = new ArrayList<>();
        for (List<String> list : splitContents) {
            if (list.get(1).trim().equals("board game")) {
                BoardGame newGame = new BoardGame(list.toArray(new String[0]));
                productList.add(newGame);
            } else {
                Accessory newAccessory = new Accessory(list.toArray(new String[0]));
                productList.add(newAccessory);
            }
        }
        return productList;
    }

    public static ArrayList<User> loadUsers() throws IOException {
        File userFile = new File("UserAccount.txt");
        List<String> lines = Files.readAllLines(userFile.toPath());
        ArrayList<List<String>> splitlines = new ArrayList<>();
        for (String line : lines) {
            splitlines.add(List.of(line.split(";")));
        }
        ArrayList<User> listedUsers = new ArrayList<>();
        for (List<String> line : splitlines) {
            Address address = new Address(Integer.parseInt(line.get(2).trim()), line.get(3).trim(), line.get(4).trim());
            User newUser;
            if (line.getLast().trim().equals("admin")) {
                newUser = new Admin(Integer.parseInt(line.getFirst().trim()), line.get(1).trim(), address);

            } else {
                newUser = new Customer(Integer.parseInt(line.getFirst().trim()), line.get(1).trim(), address);
            }
            listedUsers.add(newUser);
        }
        return listedUsers;
    }
}
