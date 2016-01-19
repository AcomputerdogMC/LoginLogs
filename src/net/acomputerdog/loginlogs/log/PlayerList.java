package net.acomputerdog.loginlogs.log;

import net.acomputerdog.loginlogs.main.LLPlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.logging.Logger;

public class PlayerList {
    public static final int MAX_LOGINS = 5;

    private final List<LLPlayer> playerList = new ArrayList<>();
    private final Map<String, LLPlayer> uuidMap = new HashMap<>();
    private final Map<String, LLPlayer> nameMap = new HashMap<>();
    private final List<LLPlayer> loginList = new ArrayList<>(MAX_LOGINS + 1);
    private final Logger logger;

    public PlayerList(Logger logger) {
        this.logger = logger;
    }

    public void load(BufferedReader in) {
        in.lines().forEach(line -> {
            String[] parts = line.split(":");
            if (parts.length != 4) {
                logger.warning("Malformed line: \"" + line + "\"");
            } else {
                try {
                    String uuid = parts[0];
                    String name = parts[1];
                    long login = Long.parseLong(parts[2]);
                    long logout = Long.parseLong(parts[3]);
                    LLPlayer player = new LLPlayer(uuid, name, login, logout);
                    registerPlayer(player);
                } catch (NumberFormatException e) {
                    logger.warning("Malformed line: \"" + line + "\"");
                }
            }
        });
        logger.info("Loaded " + uuidMap.size() + " player records."); //can't write a counter variable from a lambada...
    }

    public void save(Writer out) {
        try {
            for (int index = 0; index < playerList.size(); index++) {
                if (index > 0) {
                    out.write("\n");
                }
                LLPlayer player = playerList.get(index);
                out.write(player.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Exception saving player logs!", e);
        }
    }

    public void registerPlayer(LLPlayer player) {
        uuidMap.put(player.getUuid(), player);
        nameMap.put(player.getName(), player);
        playerList.add(player);
    }

    public LLPlayer getByUUID(String uuid) {
        return uuidMap.get(uuid);
    }

    public LLPlayer getByName(String name) {
        return nameMap.get(name);
    }

    public LLPlayer getOrCreate(String uuid, String name) {
        LLPlayer player = getByUUID(uuid);
        if (player == null) {
            player = new LLPlayer(uuid, name);
            registerPlayer(player);
        }
        return player;
    }

    public LLPlayer getOrCreate(String uuid) {
        return getOrCreate(uuid, uuid);
    }

    public LLPlayer find(String id) {
        LLPlayer player = getByUUID(id);
        if (player == null) {
            player = getByName(id);
        }
        return player;
    }

    public List<LLPlayer> getRecentLogins() {
        return Collections.unmodifiableList(loginList);
    }

    public void updateRecentLogins(LLPlayer player) {
        loginList.remove(player);
        loginList.add(0, player);
        while (loginList.size() > MAX_LOGINS) {
            loginList.remove(loginList.size() - 1);
        }
    }
}
