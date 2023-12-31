package dev.foxikle.webnetbedwars.managers;

import dev.foxikle.webnetbedwars.WebNetBedWars;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ScoreboardManager {
    private final GameManager gameManager;
    private final WebNetBedWars plugin;
    public int taskID;
    private final HashMap<UUID, FastBoard> boards = new HashMap();

    public ScoreboardManager(GameManager gameManager, WebNetBedWars plugin) {
        this.gameManager = gameManager;
        this.plugin = plugin;
    }

    public void init() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            FastBoard board = new FastBoard(player);
            board.updateTitle(ChatColor.translateAlternateColorCodes('&', "&e&lWEBNET BEDWARS"));
            board.updateLines("");
            boards.put(player.getUniqueId(), board);
        });

        taskID = Bukkit.getScheduler().runTaskTimer(plugin, this::updateBoards, 0, 20).getTaskId();
    }

    public void addScoreboard(Player player) {
        FastBoard board = new FastBoard(player);
        board.updateTitle(ChatColor.translateAlternateColorCodes('&', "&e&lWEBNET BEDWARS"));
        board.updateLines("");
        boards.put(player.getUniqueId(), board);
    }

    public void removeScoreboard(UUID player) {
        boards.remove(player);
    }

    public void updateBoards() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (!boards.containsKey(player.getUniqueId())) {
                FastBoard board = new FastBoard(player);
                board.updateTitle(ChatColor.translateAlternateColorCodes('&', "&e&lWEBNET BEDWARS"));
                board.updateLines("");
                boards.put(player.getUniqueId(), board);
            }
        });
        switch (gameManager.getGameState()) {
            case WAITING -> boards.forEach((uuid, board) -> board.updateLines(
                    ChatColor.GRAY + plugin.getPluginMeta().getVersion(),
                    "",
                    "Map: " + ChatColor.GREEN + plugin.getConfig().getString("Map"),
                    "Players: " + ChatColor.GREEN + "(" + Bukkit.getOnlinePlayers().size() + "/" + plugin.getConfig().getString("MaxPlayers") + ")",
                    "",
                    "Waiting...",
                    "Mode: " + ChatColor.GREEN + plugin.getConfig().getString("Mode"),
                    "",
                    ChatColor.YELLOW + plugin.getConfig().getString("ServerIP")

            ));
            case FROZEN -> boards.forEach((uuid, board) -> board.updateLines(
                    ChatColor.GRAY + plugin.getPluginMeta().getVersion(),
                    "",
                    "Map: " + ChatColor.GREEN + plugin.getConfig().getString("Map"),
                    "",
                    ChatColor.AQUA + "" + ChatColor.BOLD + "FROZEN",
                    "Mode: " + ChatColor.GREEN + plugin.getConfig().getString("Mode"),
                    "",
                    ChatColor.YELLOW + plugin.getConfig().getString("ServerIP")

            ));
            case PLAY -> boards.forEach((uuid, board) -> {
                List<String> scoreboardArgs = new ArrayList<>();
                scoreboardArgs.add(ChatColor.GRAY + plugin.getPluginMeta().getVersion());
                scoreboardArgs.add("");
                scoreboardArgs.add("Time: %TIME%");
                scoreboardArgs.add("Map: " + ChatColor.GREEN + plugin.getConfig().getString("Map"));
                scoreboardArgs.add("");
                plugin.getGameManager().getTeamlist().forEach(team -> {
                    // todo: check if team is eliminated, or has final kills
                    String s = "";
                    s += ChatColor.translateAlternateColorCodes('&', team.prefix()) + ChatColor.RESET + team.displayName() + " ";
                    if (plugin.getGameManager().getBeds().get(team)) s += ChatColor.GREEN + "✔";
                    else s += ChatColor.RED + "✘";
                    if(plugin.getGameManager().getPlayerTeam(uuid) == team) s += ChatColor.GRAY + " YOU";
                    scoreboardArgs.add(s);
                });
                scoreboardArgs.add("");
                scoreboardArgs.add(ChatColor.YELLOW + plugin.getConfig().getString("ServerIP"));
                board.updateLines(scoreboardArgs);
            });
        }
    }
}
