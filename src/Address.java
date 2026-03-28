public class Address {
    private int house_num;
    private String postcode;
    private String city;

    public Address(int hn, String pc, String c){
        house_num = hn;
        postcode = pc;
        city = c;
    }

    @Override
    public String toString() {
        return String.format("%d %s %s", house_num, postcode, city);
    }
}
