package dev.foxikle.webnetbedwars;

import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ItemAbilityDispatcher {
    private final WebNetBedWars plugin;

    public ItemAbilityDispatcher(WebNetBedWars plugin) {
        this.plugin = plugin;
    }

    public void dispatch(String abilityKey, Player user, PlayerInteractEvent event) {
        switch (abilityKey) {
            case "FIREBALL" -> {
                if(event.getAction().isRightClick()) {
                    event.setCancelled(true);
                    event.getItem().setAmount(event.getItem().getAmount()-1);
                    Fireball fb = event.getPlayer().getLocation().getWorld().spawn(user.getEyeLocation().add(user.getEyeLocation().getDirection()), Fireball.class);
                    fb.setDirection(user.getEyeLocation().getDirection());
                    fb.setVisualFire(false);
                    fb.setIsIncendiary(false);
                    fb.setShooter(user);
                }
            }
            case "TNT" -> {
                if(event.getAction().isRightClick()){
                    if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            event.setCancelled(true);
                            event.getItem().setAmount(event.getItem().getAmount()-1);
                            user.getLocation().getWorld().spawn(event.getInteractionPoint(), TNTPrimed.class);
                    }
                }
            }
            case "SPECTATOR_COMPASS" -> plugin.getGameManager().getMenuManager().getSpectatorSelectorMenu().open(user);
            case "LOBBY_REQUEST" -> user.sendMessage("No leaving >:)");
            case "SPECTATOR_SPEED_SELECTOR" -> plugin.getGameManager().getMenuManager().getSpectatorSpeedMenu().open(user);
            default -> { // not an ability
                return;
            }
        }
        event.setCancelled(true);
    }
}
