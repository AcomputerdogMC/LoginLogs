package net.acomputerdog.loginlogs.test;

import net.acomputerdog.loginlogs.log.PlayerList;
import net.acomputerdog.loginlogs.main.LLPlayer;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestPlayerList {

    private static final String CONF = "aaaa-aaaa-aaaa-aaaa:acomputerdog:123456:654321:12345\nbbbb-bbbb-bbbb-bbbb:immortalkitten:123456:-1:123456";

    private static PlayerList playerList;

    @BeforeClass
    public static void init() {
        playerList = new PlayerList(Logger.getLogger("Test"));
        playerList.load(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(CONF.getBytes()))));
    }

    @Test
    public void testGetUUID() {
        LLPlayer player1 = playerList.getByUUID("aaaa-aaaa-aaaa-aaaa");
        LLPlayer player2 = playerList.getByUUID("bbbb-bbbb-bbbb-bbbb");

        checkPlayers(player1, player2);
    }

    @Test
    public void testGetName() {
        LLPlayer player1 = playerList.getByName("acomputerdog");
        LLPlayer player2 = playerList.getByName("immortalkitten");

        checkPlayers(player1, player2);
    }

    @Test
    public void testSave() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out);
        playerList.save(writer);
        try {
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String save = out.toString();

        assertEquals(CONF, save);
    }

    private void checkPlayers(LLPlayer player1, LLPlayer player2) {
        assertNotNull(player1);
        assertEquals("acomputerdog", player1.getName());
        assertEquals("aaaa-aaaa-aaaa-aaaa", player1.getUuid());
        assertEquals("acomputerdog (aaaa-aaaa-aaaa-aaaa)", player1.getCombinedName());
        assertEquals(123456, player1.getLastLogin());
        assertEquals(654321, player1.getLastLogout());
        assertEquals(12345, player1.getFirstLogin());

        assertNotNull(player2);
        assertEquals("immortalkitten", player2.getName());
        assertEquals("bbbb-bbbb-bbbb-bbbb", player2.getUuid());
        assertEquals("immortalkitten (bbbb-bbbb-bbbb-bbbb)", player2.getCombinedName());
        assertEquals(123456, player2.getLastLogin());
        assertEquals(-1L, player2.getLastLogout());
        assertEquals(123456, player2.getFirstLogin());
    }

}
