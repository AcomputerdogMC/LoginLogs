package net.acomputerdog.loginlogs.main;

/**
 * Player-related data from data files
 *
 * TODO rewrite this when PlayerList is replaced with a database
 */
public class LLPlayer implements Comparable<LLPlayer> {
    public static final long NEVER = -1L;

    private final String uuid;
    private String name;

    private long lastLogin;
    private long firstLogin;
    private long lastLogout;

    public LLPlayer(String uuid, String name, long lastLogin, long lastLogout, long firstLogin) {
        this.uuid = uuid;
        this.name = name;
        this.lastLogin = lastLogin;
        this.firstLogin = firstLogin;
        this.lastLogout = lastLogout;
    }

    public LLPlayer(String uuid, String name) {
        this(uuid, name, NEVER, NEVER, NEVER);
    }

    public LLPlayer(String uuid) {
        this(uuid, uuid);
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long loginTime) {
        this.lastLogin = loginTime;
    }

    public long getFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(long firstLogin) {
        this.firstLogin = firstLogin;
    }

    public long getLastLogout() {
        return lastLogout;
    }

    public void setLastLogout(long lastLogout) {
        this.lastLogout = lastLogout;
    }

    public String getCombinedName() {
        return name + " (" + uuid + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LLPlayer)) return false;

        LLPlayer llPlayer = (LLPlayer) o;

        return uuid.equals(llPlayer.uuid) && name.equals(llPlayer.name);

    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return uuid + ":" + name + ":" + lastLogin + ":" + lastLogout + ":" + firstLogin;
    }

    @Override
    public int compareTo(LLPlayer o) {
        return (int) (o.lastLogin - lastLogin);
    }
}
