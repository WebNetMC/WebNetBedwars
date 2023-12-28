package dev.foxikle.webnetbedwars.data.enums;

public enum ArmorLevel {
    NONE("%s_BOOTS", "%s_LEGS"), // replace with team color
    CHAINMAIL("CHAINMAIL_BOOTS", "CHAINMAIL_LEGS"),
    IRON("IRON_BOOTS", "IRON_LEGS"),
    DIAMOND("DIAMOND_BOOTS", "DIAMOND_LEGS"),
    NETHERITE("NETHERITE_BOOTS", "NETHERITE_LEGS");


    private final String bootsID;
    private final String legsID;

    ArmorLevel(String bootsID, String legsID) {
        this.bootsID = bootsID;
        this.legsID =  legsID;
    }

    public String getBootsID() {
        return bootsID;
    }

    public String getLegsID() {
        return legsID;
    }
}
