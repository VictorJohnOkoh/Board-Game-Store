package Users;

import Inventory.Stock;

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
   protected final static Stock stockClass = new Stock();

   public User(int id, String n, Address addr, String r){
       userID = id;
       name = n;
       address = addr;
       role = r;
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
