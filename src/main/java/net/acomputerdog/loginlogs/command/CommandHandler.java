package net.acomputerdog.loginlogs.command;

import net.acomputerdog.loginlogs.PluginLoginLogs;
import net.acomputerdog.loginlogs.db.PlayerInfo;
import net.acomputerdog.loginlogs.util.AsyncTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * Command handler for LoginLogs
 *
 * TODO refactor to avoid duplicated permission checks / etc
 */
public class CommandHandler {
    private final PluginLoginLogs plugin;

    public CommandHandler(PluginLoginLogs plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            String cmd = command.getName().toLowerCase();
            switch (cmd) {
                case "lastlog":
                    onLastLog(sender, args);
                    break;
                case "lastlogins":
                    onLastLogins(sender);
                    break;
                case "lastlogouts":
                    onLastLogouts(sender);
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Illegal command passed to plugin!  Please report this!");
                    plugin.getLogger().log(Level.WARNING, "Unexpected command: " + command.getName());
            }
            return true;
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Uncaught exception while processing the command!");
            sender.sendMessage(ChatColor.RED + "Please report this: " + e.getClass().getName());

            plugin.getLogger().log(Level.SEVERE, "Exception handling command: " + command.getName(), e);
            return false;
        }
    }

    private void onLastLogins(CommandSender sender) {
        if (sender.hasPermission("loginlogs.command.lastlogins")) {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new AsyncTask<List<PlayerInfo>>(this.plugin, t -> {
                t.setResult(plugin.getPlayerInfoDao().getByLoginAfter(new Date(System.currentTimeMillis() - plugin.getRecentLoginTime())));
            }, t -> {
                if (t.isFail()) {
                    plugin.getLogger().log(Level.SEVERE, "Exception handling list recent logins", t.getException());
                    sender.sendMessage(ChatColor.RED + "An error occurred while processing this command.  Please report this to a server administrator.");
                } else {
                    List<PlayerInfo> recentLogins = t.getResult();
                    if (!recentLogins.isEmpty()) {
                        sender.sendMessage(ChatColor.AQUA + "Recent logins:");
                        for (PlayerInfo player : recentLogins) {
                            if (player != null) {
                                sender.sendMessage(ChatColor.DARK_AQUA + player.getLastKnownName() + " - " + formatLastLogin(player));
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.AQUA + "No recent logins.");
                    }
                }
            }));
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
        }
    }

    private void onLastLogouts(CommandSender sender) {
        if (sender.hasPermission("loginlogs.command.lastlogouts")) {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new AsyncTask<List<PlayerInfo>>(this.plugin, t -> {
                t.setResult(plugin.getPlayerInfoDao().getByLogoutAfter(new Date(System.currentTimeMillis() - plugin.getRecentLogoutTime())));
            }, t -> {
                if (t.isFail()) {
                    plugin.getLogger().log(Level.SEVERE, "Exception handling list recent logouts", t.getException());
                    sender.sendMessage(ChatColor.RED + "An error occurred while processing this command.  Please report this to a server administrator.");
                } else {
                    List<PlayerInfo> recentLogouts = t.getResult();
                    if (!recentLogouts.isEmpty()) {
                        sender.sendMessage(ChatColor.AQUA + "Recent logouts:");
                        for (PlayerInfo player : recentLogouts) {
                            if (player != null) {
                                sender.sendMessage(ChatColor.DARK_AQUA + player.getLastKnownName() + " - " + formatLastLogout(player));
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.AQUA + "No recent logouts.");
                    }
                }
            }));
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
        }
    }

    private void onLastLog(CommandSender sender, String[] args) {
        if (sender.hasPermission("loginlogs.command.lastlog")) {
            if (args.length == 1) {
                Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new AsyncTask<PlayerInfo>(this.plugin, t -> {
                    t.setResult(plugin.getPlayerInfoDao().getPlayerByNameOrId(args[0]));
                }, t -> {
                    if (t.isFail()) {
                        plugin.getLogger().log(Level.SEVERE, "Exception handling player login", t.getException());
                        sender.sendMessage(ChatColor.RED + "An error occurred while processing this command.  Please report this to a server administrator.");
                    } else {
                        PlayerInfo player = t.getResult();
                        if (player != null) {
                            sender.sendMessage(new String[]{
                                    ChatColor.AQUA + "Log info for player " + player.getCombinedName() + ":",
                                    ChatColor.DARK_AQUA + "First known login: " + formatFirstLogin(player),
                                    ChatColor.DARK_AQUA + "Last known login: " + formatLastLogin(player),
                                    ChatColor.DARK_AQUA + "Last known logout: " + formatLastLogout(player)
                            });
                        } else {
                            sender.sendMessage(ChatColor.YELLOW + "No data for that player.");
                        }
                    }
                }));
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid usage!  Use lastlog <name | uuid>.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
        }
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
