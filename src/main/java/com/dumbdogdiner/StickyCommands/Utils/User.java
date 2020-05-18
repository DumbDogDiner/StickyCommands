package com.dumbdogdiner.StickyCommands.Utils;

import java.util.TreeMap;

import com.dumbdogdiner.StickyCommands.Main;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;

public class User {

    static Main self = Main.getPlugin(Main.class);

    /**
     * Send the player a permission denied message
     * 
     * @param sender         the person who is executing the command
     * @param PermissionNode The permission node they're being denied for
     * @return always true, for use in the command classes.
     */
    public static boolean PermissionDenied(CommandSender sender, String PermissionNode) {
        try {
            sender.sendMessage(
                    Messages.Translate("NoPermission", new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                        {
                            put("player", sender.getName());
                            put("permission", PermissionNode);
                        }
                    }));
        } catch (InvalidConfigurationException ex) {
            ex.printStackTrace();
            sender.sendMessage("Permission Denied!");
        }
        return true;
    }

}