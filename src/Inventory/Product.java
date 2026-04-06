package Inventory;

public abstract class Product {
    private final int ID;
    private final ProductCategory category;
    private final String name;
    private final double pricing;
    private final double cost;
    private int stock;

    public Product(int productId, ProductCategory productCategory, String productName, double purchaseCost, int quantityInStock, double price){
        ID = productId;
        category = productCategory;
        name = productName;
        pricing = price;
        cost = purchaseCost;
        stock = quantityInStock;
    }

    public int getProductID() {
        return ID;
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
    public ProductCategory getCategory(){
        return category;
    }
    public int getQuantityInStock() {
        return stock;
    }
    public void setQuantityInStock(int newQuantity) {
        stock = newQuantity;
    }
    public abstract String toString();

}


