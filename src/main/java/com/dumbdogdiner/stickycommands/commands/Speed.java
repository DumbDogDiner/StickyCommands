package com.dumbdogdiner.stickycommands.commands;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import com.dumbdogdiner.stickycommands.Main;
import com.dumbdogdiner.stickycommands.SpeedType;
import com.dumbdogdiner.stickycommands.User;
import com.dumbdogdiner.stickyapi.bukkit.command.AsyncCommand;
import com.dumbdogdiner.stickyapi.bukkit.command.ExitCode;
import com.dumbdogdiner.stickyapi.common.arguments.Arguments;
import com.dumbdogdiner.stickyapi.common.translation.LocaleProvider;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Speed extends AsyncCommand {
    //TODO: Move constants to a config file
    private static final float DEFAULT_WALKING_SPEED = 0.2f; // 0.1 is sneak, supposedly.
    private static final float DEFAULT_FLYING_SPEED = 0.1f; // according to google
    LocaleProvider locale = Main.getInstance().getLocaleProvider();
    TreeMap<String, String> variables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public Speed(Plugin owner) {
        super("speed", owner);
        setPermission("stickycommands.speed");
        setDescription("Change your fly or walk speed");
    }
    
    @Override
    public ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player))
            return ExitCode.EXIT_PERMISSION_DENIED.setMessage(locale.translate("no-permission", variables));

        User user = Main.getInstance().getOnlineUser(((Player)sender).getUniqueId());
        Arguments a = new Arguments(args);
        a.optionalString("speed");

        if (!a.valid())
            return ExitCode.EXIT_INVALID_SYNTAX;
        boolean flying = ((Player)sender).isFlying(); // We save state to prevent a race condition
        float speed;
        if(!a.exists("speed")){ // No argument provided, use the default
            if(flying) {
                speed = DEFAULT_FLYING_SPEED;
            } else {
                speed = DEFAULT_WALKING_SPEED;
            }
        } else if (!(a.get("speed").matches("\\d*\\.?\\d+"))) {
            return ExitCode.EXIT_INVALID_SYNTAX;
        } else {
            speed = Float.parseFloat(a.get("speed"));

            if (speed > 10 || speed <= 0)
                return ExitCode.EXIT_INVALID_SYNTAX;
            else speed /= 10f;
        }
        if (flying) {
            user.setSpeed(SpeedType.FLY, speed);
        } else {
            user.setSpeed(SpeedType.WALK, speed);
        }
        sender.sendMessage(locale.translate("speed-message", variables));
        return ExitCode.EXIT_SUCCESS;
    }

    ExitCode onSyntaxError() {
        return ExitCode.EXIT_INVALID_SYNTAX.setMessage(locale.translate("invalid-syntax", variables));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length < 2) {
            return Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "0");
        }
        return null;
    }
}
