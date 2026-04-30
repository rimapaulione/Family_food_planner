package org.example.planner_backend.util;

public final class EmailUtil {

    private EmailUtil() {
    }

    public static String normalize(final String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }
}
