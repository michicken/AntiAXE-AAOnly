package dev.micx.antiaxe;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AntiAxeRulesTest {

    @Test
    public void onlyLocalPuncherResultArmsTheGuard() {
        assertEquals(AntiAxeRules.ChatAction.ARM, AntiAxeRules.classifyChat(
            "§7You found §6The Puncher §7in the §5Lucky Chest§7! You have §c10s §7to claim it before it disappears!"));
        assertEquals(AntiAxeRules.ChatAction.NONE, AntiAxeRules.classifyChat(
            "Noob_Cai_Niao found The Puncher in the Lucky Chest!"));
        assertEquals(AntiAxeRules.ChatAction.DISARM, AntiAxeRules.classifyChat(
            "You found Gold Digger in the Lucky Chest! You have 10s to claim it before it disappears!"));
    }

    @Test
    public void windowExpiresAndZoneIncludesBarrierHeight() {
        assertTrue(AntiAxeRules.isArmed(1_000L, 1_100L));
        assertFalse(AntiAxeRules.isArmed(1_100L, 1_100L));
        assertTrue(AntiAxeRules.intersects(0, 2, 0, 6, 2, 0, 3, 0, -2, 5, 5, 2));
        assertFalse(AntiAxeRules.intersects(0, 7, 0, 6, 7, 0, 3, 0, -2, 5, 5, 2));
    }
}
