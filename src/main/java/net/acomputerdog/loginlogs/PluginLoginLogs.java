package net.acomputerdog.loginlogs;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import net.acomputerdog.loginlogs.command.CommandHandler;
import net.acomputerdog.loginlogs.db.PlayerInfo;
import net.acomputerdog.loginlogs.util.AsyncTask;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;

/**
 * Plugin main class
 */
public class PluginLoginLogs extends JavaPlugin implements Listener {
    private CommandHandler commandHandler;

    private JdbcPooledConnectionSource dbConnection;
    private PlayerInfo.PlayerInfoDao playerInfoDao;

    private long recentLoginTime;
    private long recentLogoutTime;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();

            recentLoginTime = getConfig().getLong("recentLoginTime");
            recentLogoutTime = getConfig().getLong("recentLogoutTime");

            setupDatabase();

            commandHandler = new CommandHandler(this);

            getServer().getPluginManager().registerEvents(this, this);

            getLogger().info("Startup complete.");
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Error connecting to database", e);
            getServer().getPluginManager().disablePlugin(this);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Uncaught exception starting LoginLogs", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void setupDatabase() throws SQLException {
        String host = getConfig().getString("database.host");
        String port = getConfig().getString("database.port");
        String db = getConfig().getString("database.db");
        String user = getConfig().getString("database.user");
        String pass = getConfig().getString("database.pass");


        String connString = String.format("jdbc:mysql://%s:%s/%s", host, port, db);
        dbConnection = new JdbcPooledConnectionSource(connString, user, pass);

        TableUtils.createTableIfNotExists(dbConnection, PlayerInfo.class);

        playerInfoDao = DaoManager.createDao(dbConnection, PlayerInfo.class);
    }


    @Override
    public void onDisable() {
        try {
            if (dbConnection != null) {
                dbConnection.close();
            }
            playerInfoDao = null;
            dbConnection = null;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Exception closing database connection", e);
        }

        getLogger().info("Shutdown complete.");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLogin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Date now = new Date();

        Bukkit.getScheduler().runTaskAsynchronously(this, new AsyncTask<PlayerInfo>(this, t -> {
            PlayerInfo player = playerInfoDao.getOrCreatePlayer(p);
            if (player.getFirstLogin() == null) {
                player.setFirstLogin(now);
            }
            player.setLastLogin(now);
            playerInfoDao.update(player);
        }, t -> {
            if (t.isFail()) {
                getLogger().log(Level.SEVERE, "Exception handling player login", t.getException());
            }
        }));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogout(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(this, new AsyncTask<PlayerInfo>(this, t -> {
            PlayerInfo player = playerInfoDao.getOrCreatePlayer(p);
            player.setLastLogout(new Date());
            playerInfoDao.update(player);
        }, t -> {
            if (t.isFail()) {
                getLogger().log(Level.SEVERE, "Exception handling player logout", t.getException());
            }
        }));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commandHandler.onCommand(sender, command, label, args);
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
