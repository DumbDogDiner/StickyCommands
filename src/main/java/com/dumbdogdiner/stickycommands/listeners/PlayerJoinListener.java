package com.dumbdogdiner.stickycommands.listeners;

import com.dumbdogdiner.stickyapi.common.util.TimeUtil;
import com.dumbdogdiner.stickycommands.StickyCommands;
import com.dumbdogdiner.stickycommands.User;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles logic relating to players joining and leaving the server.
 */
public class PlayerJoinListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        StickyCommands.getInstance().getOnlineUserCache().put(player.getUniqueId(), User.fromPlayer(player));
        StickyCommands.getInstance().getDatabase().updateUser(player.getUniqueId().toString(), player.getName(), player.getAddress().getAddress().getHostAddress(), TimeUtil.now(), true, true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        StickyCommands.getInstance().getOnlineUserCache().remove(player.getUniqueId());
        StickyCommands.getInstance().getDatabase().updateUser(player.getUniqueId().toString(), player.getName(), player.getAddress().getAddress().getHostAddress(), TimeUtil.now(), false, false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerKickEvent event) {
        var player = event.getPlayer();
        StickyCommands.getInstance().getOnlineUserCache().remove(player.getUniqueId());
        StickyCommands.getInstance().getDatabase().updateUser(player.getUniqueId().toString(), player.getName(), player.getAddress().getAddress().getHostAddress(), TimeUtil.now(), false, false);
    }
}
