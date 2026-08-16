package Users;

import Bridge.JavaPythonBridge;

import java.util.ArrayList;
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

   public String getName(){
       return name;
   }

   public Address getAddress() {
       return address;
   }

   // returns a string representation of the user's information
   public abstract String toString();

   /**
    * Returns every product this user is allowed to see, ready to print, or null when the
    * database could not be read. Returning the listing rather than printing it is what
    * lets the GUI put it in a table while the CLI writes it to the console.
    */
   public abstract String viewProducts();

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

    /** Loads the User accounts in the database into a list of User objects that can be accessed*/
    public static List<User> loadUsers(){
        List<User> userList = new ArrayList<>(List.of());
        String unparsedData = JavaPythonBridge.run_result(JavaPythonBridge.GET_USER_DETAILS);
        if (unparsedData == null || unparsedData.isEmpty()){
            // Carried on the exception rather than printed: the GUI shows this in its alert,
            // where an empty message used to leave the user reading the word "null".
            throw new IllegalStateException("No users could be read from the database.");
        }
        List<String> userDataList = List.of(unparsedData.split(","));
        for (String userData : userDataList){
            userList.add(User.buildUser(userData));
        }
        return userList;
    }


}
