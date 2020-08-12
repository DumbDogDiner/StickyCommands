package com.dumbdogdiner.stickycommands.listeners;

import java.sql.Timestamp;
import java.util.concurrent.Future;

import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickycommands.utils.DatabaseUtil;
import com.dumbdogdiner.stickycommands.utils.DebugUtil;
import com.dumbdogdiner.stickycommands.utils.Messages;
import com.dumbdogdiner.stickycommands.utils.TimeUtil;
import com.dumbdogdiner.stickycommands.utils.User;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListeners implements Listener {
    private Main self = Main.getPlugin(Main.class);

    @EventHandler
    public void OnPlayerConnect(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        User u = Main.USERS.get(player.getUniqueId());
        // Lets run this outside of bukkit's dogshit async scheduler because it causes a race condition with the playerMoveEvent.
        if (u == null) {
            DebugUtil.sendDebug("Adding "+player.getName()+" to the Users HashMap", this.getClass(), DebugUtil.getLineNumber());
            u = new User(player);
            Main.USERS.put(player.getUniqueId(), u);
        }
        Bukkit.getScheduler().runTaskAsynchronously(self, new Runnable() {
            @Override
            public void run() {
                String uuid = player.getUniqueId().toString();
                String ipaddr = player.getAddress().getAddress().getHostAddress();
                Timestamp firstjoin = TimeUtil.TimestampNow();
                Timestamp lastjoin = TimeUtil.TimestampNow();
                if (self.sqlError)
                    return;
                if (!player.hasPlayedBefore()) {
                    DebugUtil.sendDebug(player.getName() + " has not played before, calling InsertUser", this.getClass(), DebugUtil.getLineNumber());
                    DatabaseUtil.InsertUser(uuid, player.getName(), ipaddr, firstjoin, lastjoin, true);
                    return;
                }
                DebugUtil.sendDebug(player.getName() + " has played before, calling UpdateUser", this.getClass(), DebugUtil.getLineNumber());
                DatabaseUtil.UpdateUser(uuid, player.getName(), ipaddr, lastjoin, true, true);

                try {
                    Future<Float> fly = DatabaseUtil.GetSpeed("FlySpeed", player.getUniqueId().toString());
                    Future<Float> walk = DatabaseUtil.GetSpeed("WalkSpeed", player.getUniqueId().toString());
                    Float fly2 = fly.get();
                    Float walk2 = walk.get();
                    DebugUtil.sendDebug("Attempting to set "+player.getName()+ "'s fly speed to"+fly2, this.getClass(), DebugUtil.getLineNumber());
                    DebugUtil.sendDebug("Attempting to set "+player.getName()+ "'s walk speed to"+walk2, this.getClass(), DebugUtil.getLineNumber());
                    player.setFlySpeed(fly2);
                    player.setWalkSpeed(walk2);
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(Messages.serverError);
                }
            }
        });
    }

    @EventHandler
    public void OnPlayerKick(PlayerKickEvent event) {
        if (self.sqlError)
            return;
        Player player = event.getPlayer();
        DatabaseUtil.UpdateUser(player.getUniqueId().toString(), player.getName(), player.getAddress().getAddress().getHostAddress(), TimeUtil.TimestampNow(), false, false);
        DebugUtil.sendDebug("Removing "+player.getName()+" from the Users HashMap", this.getClass(), DebugUtil.getLineNumber());
        Main.USERS.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void OnPlayerQuit(PlayerQuitEvent event) {
        if (self.sqlError)
            return;
        Player player = event.getPlayer();
        DatabaseUtil.UpdateUser(player.getUniqueId().toString(), player.getName(), player.getAddress().getAddress().getHostAddress(), TimeUtil.TimestampNow(), false, false);
        DebugUtil.sendDebug("Removing "+player.getName()+" from the Users HashMap", this.getClass(), DebugUtil.getLineNumber());
        Main.USERS.remove(event.getPlayer().getUniqueId());
    }

}