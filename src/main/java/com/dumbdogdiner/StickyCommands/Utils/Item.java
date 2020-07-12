package com.dumbdogdiner.StickyCommands.Utils;

import java.io.File;
import java.io.IOException;

import com.dumbdogdiner.StickyCommands.Main;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;

public class Item {
    private Main self = Main.getPlugin(Main.class);
    private static File CustomConfigFile;
    private static FileConfiguration CustomConfig;
    private static Item localself = null;
    private static String[] modifierPool = {
        "white", "orange", "magenta", "lightblue", "yellow", "lime", "pink", "gray", "lightgray", "cyan", "purple", "blue", "brown", "green", "red", "black", "oak", "spruce", "birch", "jungle", "acacia", "darkoak"
    };

    private static String[] durItemPool = {
        "helmet", "tunic", "chestplate", "leggings", "boots", "axe", "shovel", "sword", "hoe"
    };

    // Initialized by our GetMessages() function.
    protected Item() {
        String worthFile = self.getConfig().getString("general.worthFile", "worth.yml");
        CustomConfigFile = new File(self.getDataFolder(), worthFile);
        if (!CustomConfigFile.exists()) {
            CustomConfigFile.getParentFile().mkdirs();
            self.saveResource(worthFile, false);
        }

        Reload();
    }

    /**
     * Get the messages object associated with messages.yml
     * 
     * @return A Messages object referencing the messages.yml file.
     */
    public static Item getItems() {
        if (Item.localself == null)
            Item.localself = new Item();
        return Item.localself;
    }

    /**
     * Reload the messages.yml file and update the internal configuration values.
     */
    public static void Reload() {
        FileConfiguration fc = new YamlConfiguration();
        try {
            fc.load(CustomConfigFile);
            CustomConfig = fc;
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lookup an item
     * 
     * @param item The config node for the message in messages.yml
     * @return 0 if the item doesn't exist, the worth if it does
     */
    public static double getItem(String item) {
        getItems();
        double worth = Item.CustomConfig.getDouble(item);
      
        if (worth == 0) {
            return GeneralizeItem(item);
        }

        return worth;
    }

    private static double GeneralizeItem(String item) {
        for(String s : modifierPool) {
            if(item.startsWith(s)) {
                String it = item.replace(s, "");
                return Item.CustomConfig.getDouble(it);
            }
        }

        return 0;
    }

    public static boolean HasDurability(String item) {
        for(String s : durItemPool) {
            if(item.endsWith(s)) {
                return true;
            }
        }
        return false;
    } 
    /**
     * Get the configuration object for messages.yml
     * 
     * @return {@link org.bukkit.configuration.file.FileConfiguration}
     */
    public FileConfiguration GetConfig() {
        return CustomConfig;
    }

}