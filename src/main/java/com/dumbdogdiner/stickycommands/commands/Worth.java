package com.dumbdogdiner.stickycommands.commands;

import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickycommands.utils.Item;
import com.dumbdogdiner.stickyapi.bukkit.command.AsyncCommand;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class Worth extends AsyncCommand {
    static Main self = Main.getInstance();
    LocaleProvider locale = Main.getInstance().getLocaleProvider();
    TreeMap<String, String> variables = locale.newVariables();
    
    public Worth(Plugin owner) {
        super("worth", owner);
        setDescription("Check the worth of the item in your hand.");
        setPermission("stickycommands.worth");
        variables.put("syntax", "/worth");
    }

    @Override
    public ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args) {
        try {
            if (!(sender instanceof Player && sender.hasPermission("stickycommands.worth")))
                return ExitCode.EXIT_PERMISSION_DENIED.setMessage(locale.translate("no-permission", variables));
            
            var player = (Player) sender;
            var item = new Item(player.getInventory().getItemInMainHand());
            ItemStack[] inventory = player.getInventory().getContents();
            variables.put("player", player.getName());
            variables.put("item", item.getName());

            if (item.getAsItemStack().getType() == Material.AIR) {
                sender.sendMessage(locale.translate("sell.cannot-sell", variables));
                return ExitCode.EXIT_SUCCESS;
            }
            
            var worth = item.getWorth();

            var itemAmount = 0;
            for (var is : inventory) {
                if (is != null && is.getType() == item.getType()) {
                    itemAmount += is.getAmount();
                }
            }
            variables.put("single_worth", Double.toString(worth));
            variables.put("hand_worth", Double.toString(worth * item.getAmount()));
            variables.put("inventory_worth", Double.toString(worth * itemAmount));
            
            if (worth != 0.0) {
                sender.sendMessage(locale.translate("sell.worth-message", variables));
                return ExitCode.EXIT_SUCCESS;
            }
            
            return ExitCode.EXIT_EXPECTED_ERROR.setMessage(locale.translate("sell.cannot-sell", variables));
        } catch (Exception e) {
            e.printStackTrace();
            return ExitCode.EXIT_ERROR.setMessage(locale.translate("server-error", variables));
        }
    }
}
