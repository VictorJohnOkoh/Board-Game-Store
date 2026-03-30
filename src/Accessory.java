public class Accessory extends Product{
    final String compatibility;

    public Accessory(String[] line){
        super(Integer.parseInt(line[0]), ProductCategory.ACCESSORY, line[3], Double.parseDouble(line[6]), Integer.parseInt(line[5]), Double.parseDouble(line[4]));
        compatibility = line[7].trim();
    }

    public String getCompatibility(){return compatibility;}

    @Override
    public String toString() {
        return String.format("Product ID:%d %s, Type:%s, Price:%.2f, Compatibility:%s, Stock:%d", getProductID(), getProductName(), getCategory(), getPrice(), getCompatibility(), getQuantityInStock());
    }
}
