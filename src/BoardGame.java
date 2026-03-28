public class BoardGame extends Product{
    final int num_players;

    public BoardGame(String[] line){
        super(Integer.parseInt(line[0]), ProductCategory.BOARDGAME, line[3], Double.parseDouble(line[6]), Integer.parseInt(line[5]), Double.parseDouble(line[4]));
        num_players = Integer.parseInt(line[7]);
    }
    public int getNum_players(){return num_players;}

    @Override
    public String toString() {
        return String.format("Product ID:%d %s Type:%s, Price:%.2f, Number of players:%d, Stock:%d", getProductID(), getProductName(), getCategory(), getPrice(), getNum_players(), getQuantityInStock());
    }

}
