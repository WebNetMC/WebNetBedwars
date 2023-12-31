package dev.foxikle.webnetbedwars.managers;

import dev.foxikle.customnpcs.api.Action;
import dev.foxikle.customnpcs.api.ActionType;
import dev.foxikle.customnpcs.api.NPCApi;
import dev.foxikle.customnpcs.api.conditions.Conditional;
import dev.foxikle.webnetbedwars.WebNetBedWars;
import dev.foxikle.webnetbedwars.data.enums.GameState;
import dev.foxikle.webnetbedwars.data.objects.Team;
import dev.foxikle.webnetbedwars.runnables.RespawnRunnable;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import javax.annotation.Nullable;
import java.util.*;

public class GameManager {
    private final WebNetBedWars plugin;
    private List<Team> teamlist = new ArrayList<>();

    private Map<Team, List<UUID>> playerTeams = new HashMap<>();
    private List<UUID> alivePlayers = new ArrayList<>();
    private Map<Team, Boolean> beds = new HashMap<>();
    private final Map<Team, org.bukkit.scoreboard.Team> mcTeams = new HashMap<>();
    private final List<NPCApi.NPC> npcs = new ArrayList<>();
    public List<UUID> spectators = new ArrayList<>();

    private GameState beforeFrozen;
    private GameState gameState;

    private static final String NPC_SKIN_VALUE = "ewogICJ0aW1lc3RhbXAiIDogMTY2MjQ2NzA5Njc1NywKICAicHJvZmlsZUlkIiA6ICJmNTgyNGRmNGIwMTU0MDA4OGRhMzUyYTQxODU1MDQ0NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJGb3hHYW1lcjUzOTIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTI5YWI4YmRiMjI4ZTQ3MjZiNzQ1MzZhY2EwNTlhMTZjYWNjNzBjNThlNGEyZGFhMTQzZDIxOWYzNzRhOGI0YSIKICAgIH0KICB9Cn0=";
    private static final String NPC_SKIN_SIGNATURE = "yKToy4cFqIM5A3JWqXkeOaWjOd8MjAm+ECb1ga8tlBZzGvsLVHVaatVcvdYvLqxeUcWrrGLE8F4cqdVl+XyqUyILjmqw8elFwKCS28fIryuvAMaH28SRjDUsAVtTyt6xHSh2yx30IvuN+OmatcTTYQO0AmTzG6VlrOd4COzfrcOEteZb6yqh43hfxpawlavdQw7LQ3ecFXe5JPINNzXPEbbcAYeV9Gh9j6ej9n2P8KsMcTfEjb+UWh82fLegPt3pBQWdXUJVyh1SualBqVaX8hqk38KbfwtC7A9FWvycY7OacjXOyTeWEqZnGUNwc1YgXnS5EidzG/xXNJz2pgzOBlwtAv80jAXnVQcyJkuhSijSehKvMuEEd1gcY7O3itAdSb0636zjAhcKsqskzUhaRNK8QNpbIowBDA2t4EXaFkGSpBSRrOVthox6MhxDLC+ZKADNuiGEtVgpw6vY5gfulovaIX7wOWGLrxGrA6JsA9Fq7XuwHq8d8k8kI6XNRSxdKoKgHhdmlzjPax/GelXt6a9VkRoagtY8EmnliWyOorIMazjdDKq+QmddHH3sDAeahLtXoCf64Jus8bqqyNL4B0E3HwlKjQ2XZw1v/G9c70uJscaoUgpATwvHg2+dH0uxs2MSkN/GZM3GWbmyerFz+AapDjsZhBhylJ570jcbuS4=";

    private StatsManager statsManager;
    private ScoreboardManager scoreboardManager;
    private WorldManager worldManager;
    private MenuManager menuManager;
    private EconomyManager economyManager;

    public boolean STARTED = false;

    public GameManager(WebNetBedWars plugin) {
        this.plugin = plugin;
        statsManager = new StatsManager();
        scoreboardManager = new ScoreboardManager(this, plugin);
        worldManager = new WorldManager(plugin, this);
        menuManager = new MenuManager(plugin);
    }

    public void setup() {
        worldManager.createSpawnPlatform();
        scoreboardManager.init();
        gameState = GameState.WAITING;
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("Teams");
        for (String key : section.getKeys(false)) {
            ConfigurationSection teamSection = section.getConfigurationSection(key);
            try {
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam(key).unregister();
            } catch (Exception ignored){}
            Team t = new Team(
                    key,
                    teamSection.getString("TAB_PREFIX"),
                    ChatColor.valueOf(teamSection.getString("TEAM_COLOR")),
                    Material.valueOf(teamSection.getString("BED_ITEM")),
                    teamSection.getLocation("SPAWN_LOCATION"),
                    teamSection.getLocation("GENERATOR_LOCATION"),
                    teamSection.getLocation("ITEM_SHOP_LOCATION"),
                    teamSection.getLocation("TEAM_SHOP_LOCATION"),
                    teamSection.getLocation("TEAM_CHEST_LOCATION")
                    );
            teamlist.add(t);
            beds.put(t, true);
        }
    }

    public void freeze(){
        beforeFrozen = gameState;
        gameState = GameState.FROZEN;
    }

    public void thaw(){
        gameState = beforeFrozen;
        beforeFrozen = null;
    }

    public void start() {
        worldManager.removeSpawnPlatform();
        STARTED = true;
        setGameState(GameState.PLAY);
        Bukkit.getOnlinePlayers().forEach(player -> statsManager.propagatePlayer(player.getUniqueId()));
        // split players into teams
        List<UUID> players = new ArrayList<>();
        alivePlayers = players;
        Bukkit.getOnlinePlayers().forEach(player -> players.add(player.getUniqueId()));
        playerTeams = splitPlayersIntoTeams(players);

        playerTeams.keySet().forEach(team -> {
            List<UUID> uuids = playerTeams.get(team);
            uuids.forEach(uuid -> {
                Player p = Bukkit.getPlayer(uuid);
                if(p != null){
                    mcTeams.get(team).addEntry(p.getName());
                    p.teleport(team.spawnLocation());
                }
            });
        });
        for (Team t : teamlist) {
            NPCApi.NPC teamShop = new NPCApi.NPC(t.teamShopLocation().getWorld());
            teamShop.setHeading(t.teamShopLocation().getYaw())
                    .setPostion(t.teamShopLocation())
                    .setName("<aqua><bold>TEAM SHOP</bold></aqua>")
                    .setSkin("shopkeeper", NPC_SKIN_SIGNATURE, NPC_SKIN_VALUE)
                    .setInteractable(true)
                    .setActions(
                            List.of(
                                    new Action(
                                            ActionType.RUN_COMMAND,
                                            new ArrayList<>(List.of("openteamshop")),
                                            0,
                                            Conditional.SelectionMode.ONE,
                                            List.of()
                                    )
                            )
                    )
                    .create();
            npcs.add(teamShop);

            NPCApi.NPC itemShop = new NPCApi.NPC(t.itemShopLocation().getWorld());
            itemShop.setHeading(t.itemShopLocation().getYaw())
                    .setPostion(t.itemShopLocation())
                    .setName("<GOLD><bold>ITEM SHOP</bold></gold>")
                    .setSkin("shopkeeper", NPC_SKIN_SIGNATURE, NPC_SKIN_VALUE)
                    .setInteractable(true)
                    .setActions(
                            List.of(
                                    new Action(
                                            ActionType.RUN_COMMAND,
                                            new ArrayList<>(List.of("openitemshop")),
                                            0,
                                            Conditional.SelectionMode.ONE,
                                            List.of()
                                    )
                            )
                    )
                    .create();
            npcs.add(itemShop);
        }    }

    private Map<Team, List<UUID>> splitPlayersIntoTeams(List<UUID> players) {
        int numTeams = teamlist.size();
        int teamSize = players.size() / numTeams;
        int remainingPlayers = players.size() % numTeams;
        Map<Team, List<UUID>> result = new HashMap<>();

        int playerIndex = 0;
        for (Team team : teamlist) {
            org.bukkit.scoreboard.Team t = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(team.displayName());
            t.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.FOR_OTHER_TEAMS);
            t.setCanSeeFriendlyInvisibles(true);
            t.setAllowFriendlyFire(false);
            t.setColor(team.color());
            t.setPrefix(ChatColor.translateAlternateColorCodes('&', team.prefix()));
            mcTeams.put(team, t);
            List<UUID> teamPlayers = new ArrayList<>();
            int currentTeamSize = teamSize + (remainingPlayers > 0 ? 1 : 0);
            for (int i = 0; i < currentTeamSize; i++) {
                if (playerIndex < players.size()) {
                    teamPlayers.add(players.get(playerIndex));
                    playerIndex++;
                }
            }
            result.put(team, teamPlayers);
            if (remainingPlayers > 0) {
                remainingPlayers--;
            }
        }

        return result;
    }

    public GameState getGameState() {
        return gameState;
    }

    private void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public List<Team> getTeamlist() {
        return teamlist;
    }

    public Map<Team, Boolean> getBeds() {
        return beds;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public Map<Team, List<UUID>> getPlayerTeams() {
        return playerTeams;
    }

    public void cleanup(){
        STARTED = false;
        setGameState(GameState.CLEANUP);
        mcTeams.values().forEach(org.bukkit.scoreboard.Team::unregister);
        npcs.forEach(NPCApi.NPC::remove);
    }

    @Nullable
    public Team getPlayerTeam(UUID uuid){
        for (Team t : teamlist) {
            if(playerTeams.get(t).contains(uuid))
                return t;
        }
        return null;
    }

    public void breakBed(Player player, Team t){
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1000f, 1f);
        Bukkit.broadcastMessage(getPlayerTeam(player.getUniqueId()).color() + player.getName() + ChatColor.YELLOW + " destroyed " + t.color() + t.displayName() + ChatColor.YELLOW + "'s bed!");
        // todo: display animations, messages, etc.
        beds.put(t, false);
    }

    public void kill(Player dead, @Nullable Player killer, EntityDamageEvent.DamageCause cause) {
        alivePlayers.remove(dead.getUniqueId());
        //todo: Death animation, not direct respawn!
        boolean finalkill = false;
        String message = ChatColor.translateAlternateColorCodes('&', getPlayerTeam(dead.getUniqueId()).prefix()) + dead.getName() + ChatColor.RESET;
        if(!beds.get(getPlayerTeam(dead.getUniqueId()))) {
            finalkill = true;
        }
        switch (cause) {
            case KILL -> {
                if(killer == null) {
                    kill(dead, null, EntityDamageEvent.DamageCause.CUSTOM);
                } else {
                    statsManager.addPlayerDeath(dead.getUniqueId());
                    statsManager.addPlayerKill(killer.getUniqueId());
                   message += ChatColor.GRAY + " was slain by " + ChatColor.translateAlternateColorCodes('&', getPlayerTeam(killer.getUniqueId()).prefix()) + killer.getName();
                }
            }
            case FALL -> {
                statsManager.addPlayerDeath(dead.getUniqueId());
               message += ChatColor.GRAY + " has fallen to their death";
            }
            case FIRE, FIRE_TICK -> {
                statsManager.addPlayerDeath(dead.getUniqueId());
               message += ChatColor.GRAY + " was roasted like a turkey";
            }
            case LAVA -> {
                statsManager.addPlayerDeath(dead.getUniqueId());
               message += ChatColor.GRAY + " discovered lava is hot";
            }
            case VOID -> {
                statsManager.addPlayerDeath(dead.getUniqueId());
               message += ChatColor.GRAY + " fell into the abyss";
            }
            case FREEZE -> {
                statsManager.addPlayerDeath(dead.getUniqueId());
               message += ChatColor.GRAY + " turned into an ice cube";
            }
            case DROWNING -> {
                statsManager.addPlayerDeath(dead.getUniqueId());
               message += ChatColor.GRAY + " forgot how to swim";
            }
            case ENTITY_EXPLOSION, BLOCK_EXPLOSION -> {
                statsManager.addPlayerDeath(dead.getUniqueId());
               message += ChatColor.GRAY + " went " + ChatColor.RED + "" + ChatColor.BOLD + "BOOM!";
            }
            case PROJECTILE -> {
                statsManager.addPlayerDeath(dead.getUniqueId());
                message += ChatColor.GRAY + " was remotley terminated";
            }
            default -> {
                if(cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
                statsManager.addPlayerDeath(dead.getUniqueId());
                plugin.getLogger().info(String.valueOf(cause));
               message += ChatColor.GRAY + " died under mysterious circumstances";
            }
        }

        if(finalkill) {
            dead.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "You DIED!", ChatColor.YELLOW + "You won't repsawn", 5, 15, 5);
            message += ChatColor.DARK_RED + "" + ChatColor.BOLD + " FINAL KILL!";
            dead.setGameMode(GameMode.SPECTATOR);
            Bukkit.broadcastMessage(message);
            return;
        }
        // respawn logic...
        Bukkit.broadcastMessage(message);
        dead.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "You DIED!", ChatColor.YELLOW + "You will repsawn soon", 5, 15, 5);
        dead.setGameMode(GameMode.SPECTATOR);
        dead.getInventory().clear();
        dead.setHealth(20.0);
        dead.setFireTicks(0); // reset fire

        new RespawnRunnable(plugin, 6, dead).runTaskTimer(plugin, 0, 20);
    }

    public void respawnPlayer(Player dead) {
        dead.setGameMode(GameMode.SURVIVAL);
        dead.setNoDamageTicks(100);// make them invincible for 5 sec
        dead.teleport(getPlayerTeam(dead.getUniqueId()).spawnLocation());
        alivePlayers.add(dead.getUniqueId());
    }

    public List<UUID> getAlivePlayers() {
        return alivePlayers;
    }
}
