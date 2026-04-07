package Inventory ;

public class Accessory extends Product{
    final private String compatibility;
    final private String type;

    public Accessory(String[] line){
        super(Integer.parseInt(line[0].trim()), ProductCategory.ACCESSORY, line[3].trim(), Double.parseDouble(line[6].trim()), Integer.parseInt(line[5].trim()), Double.parseDouble(line[4].trim()));
        compatibility = line[7].trim();
        type = line[2].trim();
    }

    public String getCompatibility(){return compatibility;}

    @Override
    public String toString() {
        return String.format("%d; %s; %s; %s; %.2f; %d; %.2f; %s", getProductID(), "accessory", type, getProductName(), getPrice(), getQuantityInStock(), getPurchaseCost(), getCompatibility());
    }

    public String partString(){
        return String.format("%d; %s; %s; %s; %.2f; %d; %s", getProductID(), "accessory", type, getProductName(), getPrice(), getQuantityInStock(), getCompatibility());

    }
}
