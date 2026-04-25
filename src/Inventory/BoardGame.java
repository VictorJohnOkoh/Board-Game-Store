package Inventory;

public class BoardGame extends Product{
    final private int num_players;
    final private String type;

    public BoardGame(String[] line){
        super(Integer.parseInt(line[0].trim()), ProductCategory.BOARDGAME, line[3].trim(), Double.parseDouble(line[6].trim()), Integer.parseInt(line[5].trim()), Double.parseDouble(line[4].trim()));
        num_players = Integer.parseInt(line[7].trim());
        type = line[2].trim();
    }

    public BoardGame(int productID, String Type, String productName, double price, double purchaseCost, int quantityInStock, int numPlayers){
        super(productID, ProductCategory.BOARDGAME, productName, purchaseCost, quantityInStock, price);
        num_players = numPlayers;
        type = Type;
    }

    public int getNumPlayers(){return num_players;}
    public String getType(){return type;}

    // Full string representation of the board game
    @Override
    public String toString() {
        return String.format("%d; %s; %s; %s; %.2f; %d; %.2f; %d", getProductID(), "board game", type, getProductName(), getPrice(), getQuantityInStock(), getPurchaseCost(), num_players);
    }

    public String partString()
    {
        return String.format("|Product ID: %d |Category: %s |Type: %-13s |Name: %-27s |Price: %.2f |Stock: %d|Number of Players: %d |", getProductID(), "board game", type, getProductName(), getPrice(), getQuantityInStock(), num_players);

    }
}
