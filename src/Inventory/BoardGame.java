package Inventory;

public class BoardGame extends Product{
    final private int num_players;
    final private String type;

    public BoardGame(String[] line){
        super(Integer.parseInt(line[0].trim()), ProductCategory.BOARDGAME, line[3].trim(), Double.parseDouble(line[6].trim()), Integer.parseInt(line[5].trim()), Double.parseDouble(line[4].trim()));
        num_players = Integer.parseInt(line[7].trim());
        type = line[2].trim();
    }
    public int getNum_players(){return num_players;}

    @Override
    public String toString() {
        return String.format("%d; %s; %s; %s; %.2f; %d; %.2f; %d", getProductID(), "board game", type, getProductName(), getPrice(), getQuantityInStock(), getPurchaseCost(), num_players);
    }

}
