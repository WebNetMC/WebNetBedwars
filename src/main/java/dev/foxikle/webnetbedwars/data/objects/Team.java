package dev.foxikle.webnetbedwars.data.objects;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;
import java.util.UUID;

public record Team(String displayName, String prefix, ChatColor color, Material bedType, Location spawnLocation, Location generatorLocation,
                   Location itemShopLocation, Location teamShopLocation, Location chestLocation, Material woolType, Material glassType, Material terracottaType) {
}
