package dev.micx.antiaxe;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads only the current Alien Arcadium game state. The first-frame map line
 * is not reliable, so AA's persistent Area line is also accepted. A new world
 * always starts disarmed and not in AA.
 */
final class AntiAxeAaContext {

    private static volatile Object world;
    private static volatile boolean inAA;

    private AntiAxeAaContext() { }

    static boolean isInAA(Minecraft mc) {
        refresh(mc);
        return inAA;
    }

    static void clear() {
        world = null;
        inAA = false;
    }

    private static void refresh(Minecraft mc) {
        if (mc == null || mc.theWorld == null) {
            clear();
            return;
        }
        if (world != mc.theWorld) {
            world = mc.theWorld;
            inAA = false;
        }
        try {
            Scoreboard board = mc.theWorld.getScoreboard();
            if (board == null) {
                inAA = false;
                return;
            }
            ScoreObjective objective = board.getObjectiveInDisplaySlot(1);
            if (objective == null) {
                inAA = false;
                return;
            }

            String title = EnumChatFormatting.getTextWithoutFormattingCodes(objective.getDisplayName());
            List<String> lines = new ArrayList<String>();
            for (Score score : new ArrayList<Score>(board.getSortedScores(objective))) {
                String entry = score.getPlayerName();
                if (entry == null || entry.startsWith("#")) continue;
                ScorePlayerTeam team = board.getPlayersTeam(entry);
                String rendered = ScorePlayerTeam.formatPlayerName(team, entry);
                String clean = EnumChatFormatting.getTextWithoutFormattingCodes(rendered == null ? entry : rendered);
                lines.add(clean == null ? "" : clean);
            }
            // Never retain an AA result across a scoreboard/map transition.
            inAA = AntiAxeAaRules.isAlienArcadium(title, lines);
        } catch (Throwable ignored) {
            inAA = false;
        }
    }
}
