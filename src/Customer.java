import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Customer extends User{
    private ArrayList<Product> basket;

    public Customer(int id, String name, Address address){
        super(id, name, address, "customer");
        basket = new ArrayList<>();
    }


    // Allows the customer to view the list of available products in descending order of the unit price
    public StringBuilder viewProducts(File stockFile) throws IOException {
        List<String> lines = Files.readAllLines(stockFile.toPath());
        ArrayList<List<String>> splitlines = new ArrayList<>();
        for (String line : lines) {
            splitlines.add(List.of(line.split(";")));
        }
        String[][] orderedLines = super.descOrder(splitlines);
        StringBuilder output = new StringBuilder();
        for (String[] orderedLine : orderedLines) {
            for (int j = 0; j < orderedLine.length; j++) {
                // Prevents the customer from being able to see the purchase cost
                if (j == 6) {
                    continue;
                }
                output.append(orderedLine[j]);
                if (j < orderedLine.length-2) {
                    output.append(",");
                }
            }
            output.append("\n");
        }
        return output;
    }


    // Shows the contents of the customer's basket
    public String showBasket(){
        StringBuilder basketContents = new StringBuilder();
        for (Product product : basket) {
            basketContents.append(product);
            basketContents.append("\n");
        }
        return basketContents.toString();
    }

    // Adds an item to the customer's basket
    public void addShopping(Product item){
        basket.add(item);
    }

    public void emptyBasket(){
        basket.clear();
    }
}
