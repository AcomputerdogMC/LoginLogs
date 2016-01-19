package net.acomputerdog.loginlogs.main;

public class LLPlayer implements Comparable<LLPlayer> {
    public static final long NEVER = -1L;

    private final String uuid;
    private String name;

    private long loginTime;
    private long logoutTime;

    public LLPlayer(String uuid, String name, long loginTime, long logoutTime) {
        this.uuid = uuid;
        this.name = name;
        this.loginTime = loginTime;
        this.logoutTime = logoutTime;
    }

    public LLPlayer(String uuid, String name) {
        this(uuid, name, NEVER, NEVER);
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

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }

    public long getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(long logoutTime) {
        this.logoutTime = logoutTime;
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
        return uuid + ":" + name + ":" + loginTime + ":" + logoutTime;
    }

    @Override
    public int compareTo(LLPlayer o) {
        return (int)(o.loginTime - loginTime);
    }
}
