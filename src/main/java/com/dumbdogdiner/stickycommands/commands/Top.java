package com.dumbdogdiner.stickycommands.commands;

import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickycommands.utils.LocationUtil;
import com.ristexsoftware.koffee.bukkit.command.AsyncCommand;
import com.ristexsoftware.koffee.translation.LocaleProvider;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Top extends AsyncCommand {

    LocaleProvider locale = Main.getInstance().getLocaleProvider();
    TreeMap<String, String> variables = locale.newVariables();
    public Top(Plugin owner) {
        super("top", owner);
        setPermission("stickycommands.top");
        setDescription("Teleport to the highest block above you");
        variables.put("syntax", "/top");
    }

    @Override
    public void onSyntaxError(CommandSender sender, String label, String[] args) {
        sender.sendMessage(locale.translate("invalid-syntax", variables));    }

    @Override
    public void onPermissionDenied(CommandSender sender, String label, String[] args) {
        sender.sendMessage(locale.translate("no-permission", variables));
    }

    @Override
    public void onError(CommandSender sender, String label, String[] args) {
        sender.sendMessage(locale.translate("server-error", variables));
    }

    @Override
    public int executeCommand(CommandSender sender, String commandLabel, String[] args) {
        try {
            if (!sender.hasPermission("stickycommands.top"))
                return 2;
    
            if (!(sender instanceof Player))
                sender.sendMessage(locale.translate("must-be-player", variables));
            var player = (Player) sender;
            var loc = LocationUtil.getSafeDestination(new Location(player.getWorld(), player.getLocation().getBlockX(), player.getWorld().getMaxHeight(), player.getLocation().getBlockZ(), player.getLocation().getYaw(), player.getLocation().getPitch()));
            variables.put("x", String.valueOf(loc.getX()));
            variables.put("y", String.valueOf(loc.getY()));
            variables.put("z", String.valueOf(loc.getZ()));
            variables.put("player", player.getName());
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> player.teleport(loc), 1L);
            sender.sendMessage(locale.translate("top-message", variables));
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }
    
}