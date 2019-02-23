package net.acomputerdog.loginlogs.db;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import org.bukkit.entity.Player;

import javax.persistence.Column;
import javax.persistence.Id;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@DatabaseTable (daoClass = PlayerInfo.PlayerInfoDaoImpl.class, tableName = "PlayerInfo")
public class PlayerInfo {
    private static final String ID_COLUMN = "uuid";
    private static final String NAME_COLUMN = "lastKnownName";
    private static final String FIRST_LOGIN_COLUMN = "firstLogin";
    private static final String LAST_LOGIN_COLUMN = "lastLogin";
    private static final String LAST_LOGOUT_COLUMN = "lastLogout";
    
    @Id
    @Column(nullable = false, name = ID_COLUMN)
    UUID uuid;

    @Column(nullable = false, length = 64, name = NAME_COLUMN)
    String lastKnownName;

    @Column(name = FIRST_LOGIN_COLUMN)
    Date firstLogin;

    @Column(name = LAST_LOGIN_COLUMN)
    Date lastLogin;

    @Column(name = LAST_LOGOUT_COLUMN)
    Date lastLogout;

    PlayerInfo() {
        // no-args
    }

    public PlayerInfo(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getLastKnownName() {
        return lastKnownName;
    }

    public void setLastKnownName(String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }

    public Date getFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(Date firstLogin) {
        this.firstLogin = firstLogin;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getLastLogout() {
        return lastLogout;
    }

    public void setLastLogout(Date lastLogout) {
        this.lastLogout = lastLogout;
    }

    public String getCombinedName() {
        return lastKnownName + " (" + uuid + ")";
    }

    @Override
    public String toString() {
        return "PlayerInfo{" +
                "uuid=" + uuid +
                ", lastKnownName='" + lastKnownName + '\'' +
                ", firstLogin=" + firstLogin +
                ", lastLogin=" + lastLogin +
                ", lastLogout=" + lastLogout +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerInfo)) return false;
        PlayerInfo that = (PlayerInfo) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    public interface PlayerInfoDao extends Dao<PlayerInfo, UUID> {
        PlayerInfo getOrCreatePlayer(Player p) throws SQLException;
        PlayerInfo getPlayerByNameOrId(String id) throws SQLException;
        List<PlayerInfo> getByLoginAfter(Date date) throws SQLException;
        List<PlayerInfo> getByLogoutAfter(Date date) throws SQLException;
    }

    /**
     * TODO prepared statements
     */
    public static class PlayerInfoDaoImpl extends BaseDaoImpl<PlayerInfo, UUID> implements PlayerInfoDao {

        public PlayerInfoDaoImpl(ConnectionSource connectionSource) throws SQLException {
            super(connectionSource, PlayerInfo.class);
        }

        @Override
        public PlayerInfo getOrCreatePlayer(Player p) throws SQLException {
            PlayerInfo info = this.queryForId(p.getUniqueId());
            if (info == null) {
                info = new PlayerInfo(p.getUniqueId());
                info.setLastKnownName(p.getName());

                if (this.create(info) != 1) {
                    throw new SQLException("Failed to create database entry for player: " + p.getUniqueId());
                }
            }

            return info;
        }

        @Override
        public PlayerInfo getPlayerByNameOrId(String id) throws SQLException {
            try {
                QueryBuilder<PlayerInfo, UUID> builder = this.queryBuilder();
                if (couldBeUUID(id)) {
                    builder.where().eq(ID_COLUMN, UUID.fromString(id)).or().eq(NAME_COLUMN, id);
                } else {
                    builder.where().eq(NAME_COLUMN, id);
                }
                builder.orderBy(LAST_LOGIN_COLUMN, true);
                return builder.queryForFirst();
            } catch (IllegalArgumentException e) {
                // UUID was invalid
                return null;
            }
        }

        @Override
        public List<PlayerInfo> getByLoginAfter(Date date) throws SQLException {
            QueryBuilder<PlayerInfo, UUID> builder = this.queryBuilder();
            builder.where().ge(LAST_LOGIN_COLUMN, date);
            builder.orderBy(LAST_LOGIN_COLUMN, true);
            return builder.query();
        }

        @Override
        public List<PlayerInfo> getByLogoutAfter(Date date) throws SQLException {
            QueryBuilder<PlayerInfo, UUID> builder = this.queryBuilder();
            builder.where().ge(LAST_LOGOUT_COLUMN, date);
            builder.orderBy(LAST_LOGOUT_COLUMN, true);
            return builder.query();
        }

        private static boolean couldBeUUID(String str) {
            return str != null
                    && str.length() == 36
                    && str.charAt(8) == '-'
                    && str.charAt(13) == '-'
                    && str.charAt(18) == '-'
                    && str.charAt(23) == '-';
        }
    }
}
