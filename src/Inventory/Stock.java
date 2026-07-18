package Inventory;

import Users.Basket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;



public class Stock {
    private static final File stockFile = new File("Stock.txt");
    private static ArrayList<Product> loadedProducts = new ArrayList<>();

    static {
        try {
            loadedProducts = listProducts();
        } catch (IOException e) {
            System.out.println("Could not load products");
        }
    }

// Deprecated
    // Returns an arrayList of Products after being passed a 2D array from a line in the stock file
    private static ArrayList<Product> loadProducts(ArrayList<List<String>> splitContents) {
        ArrayList<Product> productList = new ArrayList<>();
        for (List<String> list : splitContents) {
            if (list.get(1).trim().equals("board game")) {
                BoardGame newGame = new BoardGame(list.toArray(new String[0]));
                productList.add(newGame);
            }
//            else {
//                Accessory newAccessory = new Accessory(list.toArray(new String[0]));
//                productList.add(newAccessory);
//            }
        }
        return productList;
    }

//    deprecated
    // Returns a list of products sorted into descending order by price
    private static ArrayList<Product> descOrder(ArrayList<Product> list){
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
//deprecated
    // returns an ArrayList of Products in the Stock.txt file ordered by price
    private static ArrayList<Product> listProducts() throws IOException {
        List<String> lines = Files.readAllLines(stockFile.toPath());
        ArrayList<List<String>> splitlines = new ArrayList<>();
        for (String line : lines) {
            splitlines.add(List.of(line.split(";")));
        }
        ArrayList<Product> listedProducts = loadProducts(splitlines);
        return descOrder(listedProducts);
    }
    // updates the loadedProducts attribute after the stock file has been changed so it is reflected when user views stock
    private void updateLoadedProducts() throws IOException {
        loadedProducts = listProducts();
    }
//deprecated
    // Shows all products currently stocked (excluding the purchase cost)
    public String showStockCustomer(){
        StringBuilder contents = new StringBuilder();
        for (Product product : loadedProducts) {
            if (product.getProductCategory().equals(ProductCategory.BOARDGAME)){
                BoardGame boardGame = (BoardGame) product;
                String new_entry = String.format("|Product ID: %d |Category: %s |Type: %-13s |Name: %-27s |Price: %.2f |Stock: %d|Number of Players: %d |", product.getProductID(), product.getProductCategory(), boardGame.getType(), product.getProductName(), product.getPrice(), product.getQuantityInStock(), boardGame.getNumPlayers());
                contents.append(new_entry);
            } else {
                Accessory accessory = (Accessory) product;
                String new_entry = String.format("|Product ID: %d |Category: %s |Type: %-13s |Name: %-27s |Price: %.2f |Stock: %d|Compatibility: %s |", product.getProductID(), product.getProductCategory(), accessory.getType(), product.getProductName(), product.getPrice(), product.getQuantityInStock(), accessory.getCompatibility());
                contents.append(new_entry);
            }
            contents.append("\n");
        }
        return contents.toString();
    }

//deprecated
    // shows all details for every product in stock (including purchase cost)
    public String showStockAdmin(){
        StringBuilder contents = new StringBuilder();
        for (Product product : loadedProducts) {
            if (product.getProductCategory().equals(ProductCategory.BOARDGAME)){
                BoardGame boardGame = (BoardGame) product;
                String new_entry = String.format("|Product ID: %d |Category: %s |Type: %-13s |Name: %-27s |Price: %.2f |Purchase Cost: %.2f |Stock: %d|Number of Players: %d |", product.getProductID(), product.getProductCategory(), boardGame.getType(), product.getProductName(), product.getPrice(), product.getPurchaseCost(),product.getQuantityInStock(), boardGame.getNumPlayers());
                contents.append(new_entry);
            } else {
                Accessory accessory = (Accessory) product;
                String new_entry = String.format("|Product ID: %d |Category: %s |Type: %-13s |Name: %-27s |Price: %.2f |Purchase Cost: %.2f |Stock: %d|Compatibility: %s |",
                        product.getProductID(), product.getProductCategory(), accessory.getType(), product.getProductName(), product.getPrice(), product.getPurchaseCost(),product.getQuantityInStock(), accessory.getCompatibility());
                contents.append(new_entry);
            }
            contents.append("\n");
        }
        return contents.toString();
    }


//deprecated
    // adds a new product to the Stock file
    public void addStock(Product product) throws IOException {
        try(
            PrintWriter stock_file = new PrintWriter(new FileWriter(stockFile, true))){
            String new_entry = "\n" + product.toString();
            if (checkConflicts(product)){
                System.out.println("Conflicts found in the database");
            } else {
                stock_file.append(new_entry);
                updateLoadedProducts();
            }
        }catch (IOException e){
            System.out.println("Error: Could not access the stock file");
        }
        updateLoadedProducts();
    }

    public static ArrayList<Product> getLoadedProducts() {
        return loadedProducts;
    }

//    deprecated
    // checks if the product being added has a matching name or ID, and returns true if there are any conflicts
    public static boolean checkConflicts(Product checkedProduct){
        boolean conflict = false;

        for (Product product : loadedProducts){
            if (product.getProductName().equalsIgnoreCase(checkedProduct.getProductName())){
                System.out.println("This Product's name is already in the stock database.");
                conflict = true;
            }
            if (product.getProductID() == checkedProduct.getProductID()){
                System.out.println("This Product ID is already in the product database.");
                conflict = true;
            }
        }
        return conflict;
    }
//deprecated
    // either checks if a single product passed is still in stock or there's enough stock for the products in a basket, returns false if it isn't in stock
    public static boolean checkStock(Product product){
		return product.getQuantityInStock() != 0;
	}
//deprecated
    public static boolean checkStock(Basket basket){
        // checks if the products being paid for are still in stock
        // if there isn't enough of one of the products in stock then an error is shown on the screen
        ArrayList<Integer> amountList = basket.getAmounts();
        StringBuilder outOfStock = new StringBuilder("There aren't enough of these products in stock for your purchase:\n");
        boolean enoughStock = true;
        for (Product product : basket.getBasket()){
            int amountInStock =  product.getQuantityInStock();
            int quantityWanted = amountList.get(basket.getBasket().indexOf(product));
            if (amountInStock < quantityWanted){
                outOfStock.append(product.getProductName()).append("\n");
                enoughStock = false;
            }
        }
        System.out.println(outOfStock);
		return enoughStock;
	}

}
