package Inventory ;

public class Accessory extends Product{
    final private String compatibility;
    final private AccessoryType type;


//    public Accessory(String[] line){
//        super(Integer.parseInt(line[0].trim()), ProductCategory.ACCESSORY, line[3].trim(), Double.parseDouble(line[6].trim()), Integer.parseInt(line[5].trim()), Double.parseDouble(line[4].trim()));
//        compatibility = line[7].trim();
//        type = line[2].trim();
//    }

    public Accessory(int productID, AccessoryType Type, String productName, double price, double purchaseCost, int quantityInStock, String comp){
        super(productID, ProductCategory.ACCESSORY, productName, purchaseCost, quantityInStock, price);
        compatibility = comp;
        type = Type;
    }

    public String getCompatibility(){return compatibility;}
    public String getType(){
        return type.toString();
    }

    @Override
    public String toString() {
        return String.format("%d; %s; %s; %s; %.2f; %d; %.2f; %s", getProductID(), "accessory", getType(), getProductName(), getPrice(), getQuantityInStock(), getPurchaseCost(), compatibility);
    }

}
