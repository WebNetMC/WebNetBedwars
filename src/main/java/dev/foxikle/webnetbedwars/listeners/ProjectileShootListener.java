package dev.foxikle.webnetbedwars.listeners;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import dev.foxikle.webnetbedwars.WebNetBedWars;
import org.bukkit.Bukkit;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class ProjectileShootListener implements Listener {
    private final WebNetBedWars plugin;

    public ProjectileShootListener(WebNetBedWars plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityMove(PlayerLaunchProjectileEvent event) {
        if(event.getProjectile() instanceof Trident trident) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (trident.getLocation().y() <= -40) {
                        Ghast g = trident.getWorld().spawn(trident.getLocation(), Ghast.class);
                        g.setSilent(true);
                        trident.hitEntity(g);
                        Bukkit.getScheduler().runTaskLater(plugin, g::remove, 1);
                        trident.setDamage(1);
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0, 1);
        }
    }
}
