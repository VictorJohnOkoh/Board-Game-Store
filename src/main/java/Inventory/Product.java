package Inventory;

import java.util.List;

public abstract class Product {
    private final int productID;
    private final ProductCategory productCategory;
    private final String name;
    private final double pricing;
    private final double cost;
    private int stock;

    public Product(int productId, ProductCategory category, String productName, double purchaseCost, int quantityInStock, double price){
        productID = productId;
        productCategory = category;
        name = productName;
        pricing = price;
        cost = purchaseCost;
        stock = quantityInStock;
    }

    public int getProductID() {
        return productID;
    }
    public double getPrice() {
        return pricing;
    }
    public double getPurchaseCost() {
        return cost;
    }
    public String getProductName(){
        return name;
    }
    public ProductCategory getProductCategory(){
        return productCategory;
    }
    public int getQuantityInStock() {
        return stock;
    }
    public void setQuantityInStock(int newQuantity) {
        stock = newQuantity;
    }
    public abstract String toString();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        // Replace `productID` with whatever unique field identifies your products
        return this.productID == product.productID;
    }

    @Override
    public int hashCode() {
        // Must use the exact same field used in equals()
        return Integer.hashCode(productID);
    }

    /** Takes in a string containing the
     * product ID, genre/type, name, price, quantity, purchase cost, and compatibility/no players
     * (depending on whether it's a @BoardGame or @Accessory being made)
     * in the format pid;genre;name;price;quantity;pcost;compatibiility/noplayers
     * */
    public static Product buildProduct(String unparsedData){
        List<String> parsedData = List.of(unparsedData.split(";"));
//        creating a boardgame
        if (parsedData.get(1).equalsIgnoreCase("boardgame")){
            int id = Integer.parseInt(parsedData.getFirst());
            String genre = parsedData.get(2);
            String name = parsedData.get(3);
            double price = Double.parseDouble(parsedData.get(4));
            int quantity = Integer.parseInt(parsedData.get(5));
            double pcost = Double.parseDouble(parsedData.get(6));
            int noplayers = Integer.parseInt(parsedData.get(7));
            return new BoardGame(id, genre, name, price, pcost, quantity, noplayers);
        }
//        creating an accessory
        else {
            int id = Integer.parseInt(parsedData.getFirst());
            AccessoryType genre = AccessoryType.getValueOf(parsedData.get(2));
            String name = parsedData.get(3);
            double price = Double.parseDouble(parsedData.get(4));
            int quantity = Integer.parseInt(parsedData.get(5));
            double pcost = Double.parseDouble(parsedData.get(6));
            String compatibility = parsedData.get(7);
            return new Accessory(id, genre, name, price, pcost, quantity, compatibility);
        }
    }
}


