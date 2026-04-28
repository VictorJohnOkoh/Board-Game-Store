package Inventory;

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

}


