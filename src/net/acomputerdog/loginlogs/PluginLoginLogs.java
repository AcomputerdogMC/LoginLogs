package net.acomputerdog.loginlogs;

import net.acomputerdog.loginlogs.command.CommandHandler;
import net.acomputerdog.loginlogs.log.PlayerList;
import net.acomputerdog.loginlogs.main.LLPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class PluginLoginLogs extends JavaPlugin implements Listener {
    private PlayerList playerList;
    private CommandHandler commandHandler;

    private File playerFile;

    @Override
    public void onEnable() {
        try {
            if (!(getDataFolder().exists() || getDataFolder().mkdirs())) {
                getLogger().warning("Unable to create data folder!");
            }
            playerFile = new File(getDataFolder(), "players.dat");

            playerList = new PlayerList(getLogger());
            if (playerFile.exists()) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(playerFile));
                    playerList.load(reader);
                    reader.close();
                } catch (Exception e) {
                    getLogger().warning("Exception loading player logs!");
                    e.printStackTrace();
                }
            } else {
                getLogger().warning("Player logs do not exist.  Logs will not be loaded.");
            }

            commandHandler = new CommandHandler(playerList);

            getServer().getPluginManager().registerEvents(this, this);

            getLogger().info("Startup complete.");
        } catch (Throwable t) {
            getLogger().severe("Uncaught exception starting LoginLogs!  LL may not work!");
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onDisable() {
        try {
            try {
                Writer writer = new FileWriter(playerFile);
                playerList.save(writer);
                writer.close();
            } catch (Exception e) {
                getLogger().warning("Exception saving player logs!");
                e.printStackTrace();
            }

            getLogger().info("Shutdown complete.");
        } catch (Throwable t) {
            getLogger().severe("Uncaught exception stopping LoginLogs!");
            t.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLogin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        String uuid = p.getUniqueId().toString();
        String name = p.getName();
        if (name == null) {
            name = "";
        }
        LLPlayer player = playerList.getOrCreate(uuid, name);
        player.setLastLogin(System.currentTimeMillis());
        playerList.updateRecentLogins(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogout(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        String uuid = p.getUniqueId().toString();
        String name = p.getName();
        if (name == null) {
            name = "";
        }
        LLPlayer player = playerList.getOrCreate(uuid, name);
        player.setLastLogout(System.currentTimeMillis());
        playerList.updateRecentLogouts(player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commandHandler.onCommand(sender, command, label, args);
    }

    public PlayerList getPlayerList() {
        return playerList;
    }

}
