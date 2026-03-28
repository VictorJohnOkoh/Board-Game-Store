import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

abstract class User {
   final int userID;
   private String name;
   private Address address;
   final String role;

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

   public StringBuilder viewProducts(File stockFile) throws Exception{
       List<String> lines = Files.readAllLines(stockFile.toPath());
       ArrayList<List<String>> splitLines = new ArrayList<>();
       for (String line : lines) {
           splitLines.add(List.of(line.split(";")));
       }
       String[][] orderedLines =  descOrder(splitLines);
       StringBuilder output = new StringBuilder();
       for (String[] orderedLine : orderedLines) {
           for (int j = 0; j < orderedLines[0].length; j++) {
               output.append(orderedLine[j]);
               if (j < orderedLine.length-2){
                   output.append(",");
               }
           }
           output.append("\n");
       }
       return output;
   }
    /*
        Takes in a 2D array of the stock data from Stock.txt and uses a Bubble sort
        to order it in descending order then returns a StringBuilder to be printed
        */
    public String[][] descOrder(ArrayList<List<String>> list){
        List<String> temp;
        for (int i = 0; i<list.size()-2; i++){
            boolean swapped = false;
            for (int j = 0; j<list.size()-2-i; j++) {
                double currentPrice = Double.parseDouble(list.get(j).get(4).trim());
                double nextPrice = Double.parseDouble(list.get(j+1).get(4).trim());
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
        // The -1 of the list size removes the empty list at the end of the returned arraylist
        String[][] final_arr = new String[list.size()-1][list.getFirst().size()];
        for (int i = 0; i< final_arr.length; i++){
            for (int j = 0; j<final_arr[1].length; j++){
                final_arr[i][j] = list.get(i).get(j);
            }
        }
//        StringBuilder orderedList = new StringBuilder();
//        for (List<String> strings : list) {
//            for (int j = 0; j < strings.size(); j++) {
//                orderedList.append(strings.get(j));
//                if (j < strings.size() - 1) {
//                    orderedList.append(", ");
//                }
//            }
//            orderedList.append("\n");
//        }
//        return orderedList;
        return final_arr;
    }
}
