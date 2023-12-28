package dev.foxikle.webnetbedwars.data.enums;

public enum PickaxeLevel {
    NONE(-1, null),
    WOODEN(0, "WOODEN_PICKAXE"),
    STONE(1, "STONE_PICKAXE"),
    IRON(2, "IRON_PICKAXE"),
    DIAMOND(3, "DIAMOND_PICKAXE");

    private final int order;
    private final String itemID;
    PickaxeLevel(int order, String itemID) {
        this.order = order;
        this.itemID = itemID;
    }

    public int getOrder() {
        return order;
    }

    public static PickaxeLevel getOrdered(PickaxeLevel level, int i) {
        if(level.getOrder() == 0 && i < 1) return WOODEN; // can't go below wooden
        return getByOrder(level.getOrder() + i);
    }

    public static PickaxeLevel getByOrder(int i) {
        switch (i) {
            case -1 -> {
                return NONE;
            }
            case 0 -> {
                return WOODEN;
            }

            case 1 -> {
                return STONE;
            }

            case 2 -> {
                return IRON;
            }

            case 3 -> {
                return DIAMOND;
            }
            default -> {
                return null;
            }
        }
    }

    public String getItemID() {
        return itemID;
    }
}
