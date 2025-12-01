package com.mindsoccer.shared.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utilitaires pour la manipulation de chaînes.
 */
public final class StringUtils {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-zA-Z0-9\\s]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private StringUtils() {
        // Utility class
    }

    /**
     * Vérifie si une chaîne est null ou vide (après trim).
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Vérifie si une chaîne n'est pas null et non vide.
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Normalise une réponse pour comparaison.
     * - Supprime les accents
     * - Convertit en minuscules
     * - Supprime les caractères spéciaux
     * - Normalise les espaces
     */
    public static String normalizeAnswer(String answer) {
        if (isBlank(answer)) {
            return "";
        }

        // Supprime les accents
        String normalized = Normalizer.normalize(answer, Normalizer.Form.NFD);
        normalized = DIACRITICS.matcher(normalized).replaceAll("");

        // Minuscules
        normalized = normalized.toLowerCase(Locale.ROOT);

        // Supprime les caractères spéciaux
        normalized = NON_ALPHANUMERIC.matcher(normalized).replaceAll(" ");

        // Normalise les espaces
        normalized = WHITESPACE.matcher(normalized).replaceAll(" ").trim();

        return normalized;
    }

    /**
     * Compare deux réponses de manière tolérante.
     */
    public static boolean answersMatch(String given, String expected) {
        if (isBlank(given) || isBlank(expected)) {
            return false;
        }
        return normalizeAnswer(given).equals(normalizeAnswer(expected));
    }

    /**
     * Compare deux réponses avec tolérance aux fautes de frappe.
     * Utilise la distance de Levenshtein.
     */
    public static boolean answersMatchFuzzy(String given, String expected, int maxDistance) {
        if (isBlank(given) || isBlank(expected)) {
            return false;
        }
        String normalizedGiven = normalizeAnswer(given);
        String normalizedExpected = normalizeAnswer(expected);

        int distance = levenshteinDistance(normalizedGiven, normalizedExpected);
        return distance <= maxDistance;
    }

    /**
     * Calcule la distance de Levenshtein entre deux chaînes.
     */
    public static int levenshteinDistance(String s1, String s2) {
        int[] prev = new int[s2.length() + 1];
        int[] curr = new int[s2.length() + 1];

        for (int j = 0; j <= s2.length(); j++) {
            prev[j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }

        return prev[s2.length()];
    }

    /**
     * Tronque une chaîne à une longueur maximale.
     */
    public static String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }

    /**
     * Génère un slug à partir d'une chaîne.
     */
    public static String slugify(String input) {
        if (isBlank(input)) {
            return "";
        }
        String normalized = normalizeAnswer(input);
        return normalized.replaceAll("\\s+", "-");
    }
}
