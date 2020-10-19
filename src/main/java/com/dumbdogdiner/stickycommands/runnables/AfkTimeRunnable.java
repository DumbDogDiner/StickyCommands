package com.dumbdogdiner.stickycommands.runnables;

import java.util.TimerTask;
import java.util.TreeMap;

import com.dumbdogdiner.stickyapi.common.util.NumberUtil;
import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickycommands.User;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class AfkTimeRunnable extends TimerTask {
    protected Integer AFK_TIMEOUT = Main.getInstance().getConfig().getInt("afk-timeout", 300);
    @Override
    public void run() {
        for (User user : Main.getInstance().getOnlineUserCache().getAll()) {
            user.setAfkTime(user.getAfkTime()+1);
            if (user.getAfkTime() >= AFK_TIMEOUT) {
                var variables = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
                if (!user.isAfk()) {
                    user.setAfk(true);
                    variables.put("player", user.getName());
                    for (Player players : Bukkit.getOnlinePlayers()) {
                        players.sendMessage(Main.getInstance().getLocaleProvider().translate("afk.afk", variables));
                    }
                } else if (exceedsPermissionTime(user, user.getAfkTime() - AFK_TIMEOUT)) {
                    variables.put("time", String.valueOf(user.getAfkTime() - AFK_TIMEOUT));
                    System.out.println(variables.get("time"));
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> Bukkit.getPlayer(user.getUniqueId()).kickPlayer(Main.getInstance().getLocaleProvider().translate("afk.afk-kick", variables)), 1L);
                }
            }
        }
    }

    private Boolean exceedsPermissionTime(User user, Integer time) {
        for (PermissionAttachmentInfo permission : Bukkit.getPlayer(user.getUniqueId()).getEffectivePermissions()) {
            if (!permission.getPermission().contains("stickycommands.afk.autokick")) continue; // We don't care about other permissions
            var afkArr = permission.getPermission().split("\\.");
            var afkPerm = afkArr.length == 4 ? afkArr[3] : "unlimited"; // We only care about the time!
            if (!NumberUtil.isNumeric(afkPerm) || afkPerm.equals("unlimited"))
                return false;

            var permTime = Integer.valueOf(afkPerm);
            return permTime <= user.getAfkTime() - AFK_TIMEOUT;
        }
        return false;
    }
}
