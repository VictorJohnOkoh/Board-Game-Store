package Users;

import java.util.List;

public abstract class User {
   private final int userID;
   private final String name;
   private final Address address;
   private final String role;

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

   // returns a string representation of the user's information
   public abstract String toString();

   /**allows user to see all available products*/
   public abstract void viewProducts();

    /** Takes in user data in the format userid;username;housenum;postcode;city;role*
     * and returns a User
     */
   public static User buildUser(String userdata) {
       List<String> parsedData = List.of(userdata.split(";"));
       int userid = Integer.parseInt(parsedData.getFirst());
       String username = parsedData.get(1);
       Address address = new Address(Integer.parseInt(parsedData.get(2)), parsedData.get(3), parsedData.get(4));
       String role = parsedData.get(5);
       if (role.equals("admin")){
           return new Admin(userid, username, address);
       } else {
           return new Customer(userid, username, address);
       }
   }


}
