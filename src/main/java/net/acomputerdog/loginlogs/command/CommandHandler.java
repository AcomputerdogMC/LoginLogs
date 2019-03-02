package net.acomputerdog.loginlogs.command;

import net.acomputerdog.advplugin.async.AsyncTask;
import net.acomputerdog.advplugin.cmd.CmdHandler;
import net.acomputerdog.loginlogs.PluginLoginLogs;
import net.acomputerdog.loginlogs.db.PlayerInfo;
import org.bukkit.ChatColor;

import java.util.Date;
import java.util.List;

/**
 * Command handler for LoginLogs
 *
 */
public class CommandHandler {
    private final PluginLoginLogs plugin;

    public CommandHandler(PluginLoginLogs plugin) {
        this.plugin = plugin;
    }

    public CmdHandler defineLastLog() {
        // TODO find a way to specify type somewhere else
        return cmd -> plugin.runAsyncTask((AsyncTask.AsyncBlock<PlayerInfo>) t -> {
            t.result = plugin.getPlayerInfoDao().getPlayerByNameOrId(cmd.getArgsList()[0]);
        }, t -> {
            plugin.getALogger().logError("Exception handling player login", t.getException());
            cmd.getUser().sendMessage(ChatColor.RED + "An error occurred while processing this command.  Please report this to a server administrator.");
        }, t -> {
            PlayerInfo player = t.getResult();
            if (player != null) {
                cmd.getUser().sendMessage(new String[]{
                        ChatColor.AQUA + "Log info for player " + player.getCombinedName() + ":",
                        ChatColor.DARK_AQUA + "First known login: " + formatFirstLogin(player),
                        ChatColor.DARK_AQUA + "Last known login: " + formatLastLogin(player),
                        ChatColor.DARK_AQUA + "Last known logout: " + formatLastLogout(player)
                });
            } else {
                cmd.getUser().sendMessage(ChatColor.YELLOW + "No data for that player.");
            }
        });
    }

    public CmdHandler defineLastLogins() {
        return cmd -> plugin.runAsyncTask((AsyncTask.AsyncBlock<List<PlayerInfo>>) t -> {
            t.result = plugin.getPlayerInfoDao().getByLoginAfter(new Date(System.currentTimeMillis() - plugin.getRecentLoginTime()));
        }, t -> {
            plugin.getALogger().logError("Exception handling list recent logins", t.getException());
            cmd.getUser().sendMessage(ChatColor.RED + "An error occurred while processing this command.  Please report this to a server administrator.");
        }, t -> {
            List<PlayerInfo> recentLogins = t.getResult();
            if (!recentLogins.isEmpty()) {
                cmd.getUser().sendMessage(ChatColor.AQUA + "Recent logins:");
                for (PlayerInfo player : recentLogins) {
                    if (player != null) {
                        cmd.getUser().sendMessage(ChatColor.DARK_AQUA + player.getLastKnownName() + " - " + formatLastLogin(player));
                    }
                }
            } else {
                cmd.getUser().sendMessage(ChatColor.AQUA + "No recent logins.");
            }
        });
    }

    public CmdHandler defineLastLogouts() {
        return cmd -> plugin.runAsyncTask((AsyncTask.AsyncBlock<List<PlayerInfo>>) t -> {
            t.result = plugin.getPlayerInfoDao().getByLogoutAfter(new Date(System.currentTimeMillis() - plugin.getRecentLogoutTime()));
        }, t -> {
            plugin.getALogger().logError("Exception handling list recent logouts", t.getException());
            cmd.getUser().sendMessage(ChatColor.RED + "An error occurred while processing this command.  Please report this to a server administrator.");
        }, t -> {
            List<PlayerInfo> recentLogouts = t.getResult();
            if (!recentLogouts.isEmpty()) {
                cmd.getUser().sendMessage(ChatColor.AQUA + "Recent logouts:");
                for (PlayerInfo player : recentLogouts) {
                    if (player != null) {
                        cmd.getUser().sendMessage(ChatColor.DARK_AQUA + player.getLastKnownName() + " - " + formatLastLogout(player));
                    }
                }
            } else {
                cmd.getUser().sendMessage(ChatColor.AQUA + "No recent logouts.");
            }
        });
    }

    private String formatTime(Date date) {
        if (date == null) {
            return "N/A";
        } else {
            return date.toString();
        }
    }

    private String formatLastLogin(PlayerInfo player) {
        return formatTime(player.getLastLogin());
    }

    private String formatFirstLogin(PlayerInfo player) {
        return formatTime(player.getFirstLogin());
    }

    private String formatLastLogout(PlayerInfo player) {
        return formatTime(player.getLastLogout());
    }
}
