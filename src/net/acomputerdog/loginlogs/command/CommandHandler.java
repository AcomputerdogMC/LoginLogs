package net.acomputerdog.loginlogs.command;

import net.acomputerdog.loginlogs.log.PlayerList;
import net.acomputerdog.loginlogs.main.LLPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Date;
import java.util.List;

public class CommandHandler {
    private final PlayerList playerList;
    private final Date date;

    public CommandHandler(PlayerList playerList) {
        this.playerList = playerList;
        this.date = new Date();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            String cmd = command.getName().toLowerCase();
            switch (cmd) {
                case "lastlog":
                    onLastLog(sender, args);
                case "lastlogin":
                    onLastLogin(sender, args);
                case "lastlogout":
                    onLastLogout(sender, args);
                case "lastlogins":
                    onLastLogins(sender);
                case "firstlogin":
                    onFirstLogin(sender, args);
                case "lastlogouts":
                    onLastLogouts(sender);
                default:
                    sender.sendMessage(ChatColor.RED + "Illegal command passed to plugin!  Please report this!");
            }
            return true;
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Uncaught exception while processing the command!");
            sender.sendMessage(ChatColor.RED + "Please report this: " + e.getClass().getName());
            e.printStackTrace();
            return false;
        }
    }

    private void onLastLogins(CommandSender sender) {
        if (sender.hasPermission("loginlogs.command.lastlogins")) {
            sender.sendMessage(ChatColor.AQUA + "Recent logins:");
            List<LLPlayer> recentLogins = playerList.getRecentLogins();
            for (LLPlayer player : recentLogins) {
                if (player != null) {
                    sender.sendMessage(ChatColor.DARK_AQUA + player.getName() + " - " + formatLastLogin(player));
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
        }
    }

    private void onLastLogouts(CommandSender sender) {
        if (sender.hasPermission("loginlogs.command.lastlogouts")) {
            sender.sendMessage(ChatColor.AQUA + "Recent logouts:");
            List<LLPlayer> recentLogouts = playerList.getRecentLogouts();
            for (LLPlayer player : recentLogouts) {
                if (player != null) {
                    sender.sendMessage(ChatColor.DARK_AQUA + player.getName() + " - " + formatLastLogout(player));
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
        }
    }

    private void onLastLog(CommandSender sender, String[] args) {
        if (sender.hasPermission("loginlogs.command.lastlog")) {
            if (args.length >= 1) {
                String id = args[0];
                LLPlayer player = playerList.find(id);
                if (player != null) {
                    sender.sendMessage(new String[]{
                            ChatColor.AQUA + "Log info for player " + player.getCombinedName() + ":",
                            ChatColor.DARK_AQUA + "First known login: " + formatFirstLogin(player),
                            ChatColor.DARK_AQUA + "Last known login: " + formatLastLogin(player),
                            ChatColor.DARK_AQUA + "Last known logout: " + formatLastLogout(player)
                    });
                } else {
                    sender.sendMessage(ChatColor.RED + "Unable to find a player by that name/uuid!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid usage!  Use lastlog <name | uuid>.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
        }
    }

    private void onLastLogin(CommandSender sender, String[] args) {
        if (sender.hasPermission("loginlogs.command.lastlogin")) {
            if (args.length >= 1) {
                String id = args[0];
                LLPlayer player = playerList.find(id);
                if (player != null) {
                    sender.sendMessage(ChatColor.DARK_AQUA + "Last known login for player " + player.getCombinedName() + ": " + formatLastLogin(player));
                } else {
                    sender.sendMessage(ChatColor.RED + "Unable to find a player by that name/uuid!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid usage!  Use lastlogin <name | uuid>.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
        }
    }

    private void onLastLogout(CommandSender sender, String[] args) {
        if (sender.hasPermission("loginlogs.command.lastlogout")) {
            if (args.length >= 1) {
                String id = args[0];
                LLPlayer player = playerList.find(id);
                if (player != null) {
                    sender.sendMessage(ChatColor.DARK_AQUA + "Last known logout for player " + player.getCombinedName() + ": " + formatLastLogout(player));
                } else {
                    sender.sendMessage(ChatColor.RED + "Unable to find a player by that name/uuid!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid usage!  Use lastlogout <name | uuid>.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
        }
    }

    private void onFirstLogin(CommandSender sender, String[] args) {
        if (sender.hasPermission("loginlogs.command.firstlogin")) {
            if (args.length >= 1) {
                String id = args[0];
                LLPlayer player = playerList.find(id);
                if (player != null) {
                    sender.sendMessage(ChatColor.DARK_AQUA + "First known login for player " + player.getCombinedName() + ": " + formatFirstLogin(player));
                } else {
                    sender.sendMessage(ChatColor.RED + "Unable to find a player by that name/uuid!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid usage!  Use firstlogin <name | uuid>.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
        }
    }

    private String formatTime(long time) {
        if (time == LLPlayer.NEVER) {
            return "N/A";
        } else {
            date.setTime(time);
            return date.toString();
        }
    }

    private String formatLastLogin(LLPlayer player) {
        return formatTime(player.getLastLogin());
    }

    private String formatFirstLogin(LLPlayer player) {
        return formatTime(player.getFirstLogin());
    }

    private String formatLastLogout(LLPlayer player) {
        return formatTime(player.getLastLogout());
    }
}
