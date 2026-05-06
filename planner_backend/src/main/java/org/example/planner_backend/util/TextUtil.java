package org.example.planner_backend.util;

import java.text.Normalizer;

public final class TextUtil {

    private TextUtil() {
    }

    public static String capitalize(final String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        if (trimmed.isEmpty()) return trimmed;
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }

    public static String normalize(final String s) {
        if (s == null) return null;
        return Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
