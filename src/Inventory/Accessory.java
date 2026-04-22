package Inventory ;

public class Accessory extends Product{
    final private String compatibility;
    final private String type;

    public final static String ACCESSORY_KIT = "accessory kit";
    public final static String MINIATURE = "miniature";
    public final static String DICE = "dice";

    public Accessory(String[] line){
        super(Integer.parseInt(line[0].trim()), ProductCategory.ACCESSORY, line[3].trim(), Double.parseDouble(line[6].trim()), Integer.parseInt(line[5].trim()), Double.parseDouble(line[4].trim()));
        compatibility = line[7].trim();
        type = line[2].trim();
    }

    public Accessory(int productID, String Type, String productName, double price, double purchaseCost, int quantityInStock, String comp){
        super(productID, ProductCategory.ACCESSORY, productName, purchaseCost, quantityInStock, price);
        compatibility = comp;
        type = Type;
    }

    public String getCompatibility(){return compatibility;}
    public String getType(){return type;}

    @Override
    public String toString() {
        return String.format("%d; %s; %s; %s; %.2f; %d; %.2f; %s", getProductID(), "accessory", type, getProductName(), getPrice(), getQuantityInStock(), getPurchaseCost(), compatibility);
    }

    public String partString(){
        return String.format("|Product ID: %d |Category: %s |Type: %s |Name: %s |Price: %.2f |Stock: %d |Compatibility: %s |", getProductID(), "accessory", type, getProductName(), getPrice(), getQuantityInStock(), compatibility);

    }
}
