package dev.micx.antiaxe;

import java.util.List;
import java.util.Locale;

/** Pure AA classifier shared by the UI event handlers and the input guard. */
final class AntiAxeAaRules {

    private static final String[] AREA_MARKERS = {
        "park entrance", "ferris wheel", "roller coaster", "bumper cars"
    };

    private AntiAxeAaRules() { }

    static boolean isAlienArcadium(String title, List<String> lines) {
        if (title == null || !title.toUpperCase(Locale.ROOT).contains("ZOMBIES")) return false;
        if (lines == null) return false;
        for (String line : lines) {
            if (line == null) continue;
            String lower = line.toLowerCase(Locale.ROOT);
            if (lower.contains("map:") && lower.contains("alien arcadium")) return true;
            if (!lower.contains("area")) continue;
            for (String marker : AREA_MARKERS) {
                if (lower.contains(marker)) return true;
            }
        }
        return false;
    }
}
