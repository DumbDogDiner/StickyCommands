package com.dumbdogdiner.stickycommands.commands;

import java.util.List;
import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickycommands.utils.Item;
import com.dumbdogdiner.stickyapi.common.arguments.Arguments;
import com.dumbdogdiner.stickyapi.bukkit.command.AsyncCommand;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class Sell extends AsyncCommand {
    static Main self = Main.getInstance();
    LocaleProvider locale = Main.getInstance().getLocaleProvider();
    TreeMap<String, String> variables = locale.newVariables();
    
    public Sell(Plugin owner) {
        super("sell", owner);
        setDescription("Check the worth of an item.");
        setPermission("stickycommands.worth");
        variables.put("syntax", "/worth");
    }

    @Override
    public ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.hasPermission("stickycommands.sell") || (!(sender instanceof Player)))
            return ExitCode.EXIT_PERMISSION_DENIED;

        Arguments a = new Arguments(args);
        a.optionalString("sellMode");

        var player = (Player) sender;
        var item = new Item(player.getInventory().getItemInMainHand());
        ItemStack[] inventory = player.getInventory().getContents();
        variables.put("player", player.getName());
        variables.put("item", item.getName());

        if (item.getAsItemStack().getType() == Material.AIR) {
            sender.sendMessage(locale.translate("cannot-sell", variables));
            return ExitCode.EXIT_SUCCESS;
        }

        var inventoryAmount = 0;
        for (var is : inventory) {
            if (is != null && is.getType() == item.getType() && is != item.getAsItemStack()) {
                inventoryAmount += is.getAmount();
            }
        }
        var worth = item.getWorth();
        double percentage = 100.00;
        if(item.hasDurability()) {
            double maxDur = item.getAsItemStack().getType().getMaxDurability();
            double currDur = maxDur - item.getAsItemStack().getDurability(); //TODO Change to use Damagables
            percentage = Math.round((currDur / maxDur) * 100.00) / 100.00;

            if((currDur / maxDur) < 0.4) {
                worth = 0.0;
            } else {
                worth = Math.round((worth * percentage) * 100.00) / 100.00;
            }

        }
        variables.put("single_worth", Double.toString(worth));
        variables.put("hand_worth", Double.toString(worth * item.getAmount()));
        variables.put("inventory_worth", Double.toString(worth * inventoryAmount));
        
        if (worth > 0.0) {
            switch (a.get("sellMode") == null ? "" : a.get("sellMode").toLowerCase()) {
                case "hand":
                case "":
                    variables.put("amount", String.valueOf(item.getAmount()));
                    variables.put("worth", String.valueOf(item.getWorth() * item.getAmount()));
                    player.sendMessage(locale.translate("sell-message", variables));
                    item.sell(player, false, variables, item.getAmount());
                    return ExitCode.EXIT_SUCCESS;


                case "inventory":
                case "invent":
                case "inv":
                    variables.put("amount", String.valueOf(inventoryAmount));
                    variables.put("worth", Item.getDecimalFormat().format(item.getWorth() * inventoryAmount));
                    player.sendMessage(locale.translate("sell-message", variables));
                    item.sell(player, true, variables, inventoryAmount);
                    return ExitCode.EXIT_SUCCESS;

                default:
                    return ExitCode.EXIT_INVALID_SYNTAX;
            }
        } else if(worth == 0.0) {
            sender.sendMessage(locale.translate("cannot-sell", variables));
            return ExitCode.EXIT_SUCCESS;
        } else {
            sender.sendMessage(locale.translate("bad-worth", variables)); // todo: set a locale for this
            return ExitCode.EXIT_ERROR;
        }
        // SHOULD NOT REACH HERE
    }

    @Override
    public void onSyntaxError(CommandSender sender, String label, String[] args) {
        sender.sendMessage(locale.translate("invalid-syntax", variables));
    }

    @Override
    public void onPermissionDenied(CommandSender sender, String label, String[] args) {
        sender.sendMessage(locale.translate("no-permission", variables));
    }

    @Override
    public void onError(CommandSender sender, String label, String[] args) {
        sender.sendMessage(locale.translate("server-error", variables));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return null;
    }
}