package dev.foxikle.webnetbedwars;

import dev.foxikle.customnpcs.api.NPCApi;
import dev.foxikle.webnetbedwars.commands.DebugCommand;
import dev.foxikle.webnetbedwars.commands.ItemCommand;
import dev.foxikle.webnetbedwars.listeners.*;
import dev.foxikle.webnetbedwars.managers.GameManager;
import me.flame.menus.menu.Menus;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class WebNetBedWars extends JavaPlugin {

    private GameManager gameManager;
    public static WebNetBedWars INSTANCE;
    private ItemAbilityDispatcher itemAbilityDispatcher;

    @Override
    public void onEnable() {
        INSTANCE = this;
        File file = new File("plugins/WebNetBedWars/config.yml");
        if(!file.exists())
            this.saveResource("config.yml", false);

        gameManager = new GameManager(this);
        registerCommands();
        registerListeners();
        gameManager.setup();
        NPCApi.initialize();
        changeBlastResistence();
        itemAbilityDispatcher = new ItemAbilityDispatcher(this);
        Menus.init(this);
    }

    private void changeBlastResistence(){
        //aF == resistence
        Arrays.stream(Blocks.GLASS.getClass().getSuperclass().getSuperclass().getSuperclass().getSuperclass().getDeclaredFields()).forEach(field -> {
            if(field.getType() == float.class){
                try {
                    field.setAccessible(true);
                    if(field.getName().equals("aF")){
                        field.setFloat(Blocks.GLASS, 1200.0F);

                        field.setFloat(Blocks.BLACK_STAINED_GLASS, 1200.0F);
                        field.setFloat(Blocks.WHITE_STAINED_GLASS, 1200.0F);
                        field.setFloat(Blocks.CYAN_STAINED_GLASS, 1200.0F);
                        field.setFloat(Blocks.LIGHT_BLUE_STAINED_GLASS, 1200.0F);
                        field.setFloat(Blocks.BLUE_STAINED_GLASS, 1200.0F);
                        field.setFloat(Blocks.PINK_STAINED_GLASS, 1200.0F);
                        field.setFloat(Blocks.PURPLE_STAINED_GLASS, 1200.0F);
                        field.setFloat(Blocks.LIME_STAINED_GLASS, 1200.0F);
                        field.setFloat(Blocks.GREEN_STAINED_GLASS, 1200.0F);
                        field.setFloat(Blocks.RED_STAINED_GLASS, 1200.0F);
                        field.setFloat(Blocks.ORANGE_STAINED_GLASS, 1200.0F);
                        field.setFloat(Blocks.YELLOW_STAINED_GLASS, 1200.0F);
                        field.setFloat(Blocks.LIGHT_GRAY_STAINED_GLASS, 1200.0F);
                        field.setFloat(Blocks.GRAY_STAINED_GLASS, 1200.0F);
                        field.setFloat(Blocks.BROWN_STAINED_GLASS, 1200.0F);
                        field.setFloat(Blocks.MAGENTA_STAINED_GLASS, 1200.0F);

                        field.setFloat(Blocks.BLACK_BED, 1200.0F);
                        field.setFloat(Blocks.WHITE_BED, 1200.0F);
                        field.setFloat(Blocks.CYAN_BED, 1200.0F);
                        field.setFloat(Blocks.LIGHT_BLUE_BED, 1200.0F);
                        field.setFloat(Blocks.BLUE_BED, 1200.0F);
                        field.setFloat(Blocks.PINK_BED, 1200.0F);
                        field.setFloat(Blocks.PURPLE_BED, 1200.0F);
                        field.setFloat(Blocks.LIME_BED, 1200.0F);
                        field.setFloat(Blocks.GREEN_BED, 1200.0F);
                        field.setFloat(Blocks.RED_BED, 1200.0F);
                        field.setFloat(Blocks.ORANGE_BED, 1200.0F);
                        field.setFloat(Blocks.YELLOW_BED, 1200.0F);
                        field.setFloat(Blocks.LIGHT_GRAY_BED, 1200.0F);
                        field.setFloat(Blocks.GRAY_BED, 1200.0F);
                        field.setFloat(Blocks.BROWN_BED, 1200.0F);
                        field.setFloat(Blocks.MAGENTA_BED, 1200.0F);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    @Override
    public void onDisable() {
        gameManager.cleanup();
    }

    private void registerListeners(){
        getServer().getPluginManager().registerEvents(new DamageListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new LeaveListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(this), this);
        getServer().getPluginManager().registerEvents(new InteractListener(this), this);
        getServer().getPluginManager().registerEvents(new MoveListener(this), this);
        getServer().getPluginManager().registerEvents(new GamemodeChangeListener(this), this);
        getServer().getPluginManager().registerEvents(new ArmorEquipListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new DropItemListener(this), this);
        getServer().getPluginManager().registerEvents(new ProjectileShootListener(this), this);
    }



    private void registerCommands(){
        getCommand("debug").setExecutor(new DebugCommand(this));
        getCommand("item").setExecutor(new ItemCommand());
        getCommand("item").setAliases(List.of("i"));
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public ItemAbilityDispatcher getItemAbilityDispatcher() {
        return itemAbilityDispatcher;
    }
}
