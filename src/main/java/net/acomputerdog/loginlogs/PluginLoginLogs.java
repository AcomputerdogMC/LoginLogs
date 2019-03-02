package net.acomputerdog.loginlogs;

import net.acomputerdog.advplugin.AdvancedPluginFull;
import net.acomputerdog.advplugin.cmd.CmdHandler;
import net.acomputerdog.loginlogs.command.CommandHandler;
import net.acomputerdog.loginlogs.db.PlayerInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Date;
import java.util.logging.Level;

/**
 * Plugin main class
 */
public class PluginLoginLogs extends AdvancedPluginFull implements Listener {
    private CommandHandler commandHandler;

    private PlayerInfo.PlayerInfoDao playerInfoDao;

    private long recentLoginTime;
    private long recentLogoutTime;

    @Override
    public void onEnable() {
        try {
            commandHandler = new CommandHandler(this);

            super.onEnable();
            playerInfoDao = getDatabase().lookupDao(PlayerInfo.class);

            recentLoginTime = getConfig().getLong("recentLoginTime");
            recentLogoutTime = getConfig().getLong("recentLogoutTime");

            getServer().getPluginManager().registerEvents(this, this);

            getALogger().logInfo("Startup complete.");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Uncaught exception starting LoginLogs", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        playerInfoDao = null;

        getLogger().info("Shutdown complete.");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLogin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Date now = new Date();

        runAsyncTask(t -> {
            PlayerInfo player = playerInfoDao.getOrCreatePlayer(p);
            if (player.getFirstLogin() == null) {
                player.setFirstLogin(now);
            }
            player.setLastLogin(now);
            playerInfoDao.update(player);
        }, t -> {
            if (t.isFail()) {
                getALogger().logError("Exception handling player login", t.getException());
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogout(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        runAsyncTask(t -> {
            PlayerInfo player = playerInfoDao.getOrCreatePlayer(p);
            player.setLastLogout(new Date());
            playerInfoDao.update(player);
        }, t -> {
            if (t.isFail()) {
                getALogger().logError("Exception handling player logout", t.getException());
            }
        });
    }

    @Override
    public CmdHandler defineCommandHandlerFor(String commandName) {
        switch (commandName) {
            case "lastlog": return commandHandler.defineLastLog();
            case "lastlogins": return commandHandler.defineLastLogins();
            case "lastlogouts": return commandHandler.defineLastLogouts();
            default: return null;
        }
    }

    public PlayerInfo.PlayerInfoDao getPlayerInfoDao() {
        return playerInfoDao;
    }

    public long getRecentLoginTime() {
        return recentLoginTime;
    }

    public long getRecentLogoutTime() {
        return recentLogoutTime;
    }
}
