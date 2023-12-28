package dev.foxikle.webnetbedwars.listeners;

import dev.foxikle.webnetbedwars.WebNetBedWars;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.HashMap;
import java.util.Map;

public class ExplosionListener implements Listener {
    private final WebNetBedWars plugin;

    public ExplosionListener(WebNetBedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event){
        if(event.getEntityType() == EntityType.PRIMED_TNT || event.getEntityType() == EntityType.FIREBALL){
            event.setYield(0f);
            event.setCancelled(false);

            Map<Location, Material> testing = new HashMap<>();

            event.blockList().forEach(block -> testing.put(block.getLocation(), block.getType()));

            Bukkit.getScheduler().runTaskLater(plugin, () -> event.blockList().forEach(block -> {
                if(!block.hasMetadata("blockdata")) {
                    block.setType(testing.get(block.getLocation()));
                }
            }), 0);
        }
    }

    @EventHandler
    public void onExplode(BlockExplodeEvent event){
        event.setCancelled(true);
        event.blockList().forEach(block -> block.getMetadata("blockdata").forEach(metadataValue -> {
            if(!metadataValue.asBoolean()){
                block.breakNaturally();
            }
        }));
    }
}
