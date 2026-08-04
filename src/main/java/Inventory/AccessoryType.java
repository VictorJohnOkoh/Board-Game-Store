package Inventory;

public enum AccessoryType {
    miniature("miniature"),
    dice("dice"),
    accessory_kit("accessory kit");

    private final String displayName;

    AccessoryType(String displayName){
        this.displayName = displayName;
    }

    @Override
    // returns the string representation of the enum
    public String toString() {
        return displayName;
    }



    public static AccessoryType getValueOf(String name){
    // returns the enum AccessoryType that matches the string
    // used instead of the built-in valueOf method as "accessory kit" doesn't exactly match accessory_kit

        switch (name) {
            case "accessory kit" -> {
                return accessory_kit;
            }
            case "dice" -> {
                return dice;
            }
            case "miniature" -> {
                return miniature;
            }
            default -> System.out.println("Invalid accessory type provided");
        }
        return null;
    }
}
