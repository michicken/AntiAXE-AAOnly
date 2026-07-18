package dev.micx.antiaxe;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class AntiAxeAaRulesTest {

    @Test
    public void acceptsAlienArcadiumMapLine() {
        assertTrue(AntiAxeAaRules.isAlienArcadium("ZOMBIES", Arrays.asList("Map: Alien Arcadium")));
    }

    @Test
    public void acceptsPersistentAlienArcadiumArea() {
        assertTrue(AntiAxeAaRules.isAlienArcadium("ZOMBIES", Arrays.asList("Area: Bumper Cars")));
    }

    @Test
    public void rejectsOtherZombiesMapsAndNonZombiesBoards() {
        assertFalse(AntiAxeAaRules.isAlienArcadium("ZOMBIES", Arrays.asList("Map: Dead End", "Area: Hotel")));
        assertFalse(AntiAxeAaRules.isAlienArcadium("HYPIXEL", Arrays.asList("Map: Alien Arcadium")));
    }
}
