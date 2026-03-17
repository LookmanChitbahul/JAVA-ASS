package ui;

import java.awt.Color;
import services.SettingsService;

public class AppTheme {
    private static boolean isDarkMode = true;
    private static boolean isColorBlindMode = false;
    private static final SettingsService settingsService = new SettingsService();

    static {
        isDarkMode = Boolean.parseBoolean(settingsService.getSetting("theme_dark", "true"));
        isColorBlindMode = Boolean.parseBoolean(settingsService.getSetting("accessibility_colorblind", "false"));
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
        settingsService.saveSetting("theme_dark", String.valueOf(darkMode));
    }

    public static boolean isColorBlindMode() {
        return isColorBlindMode;
    }

    public static void setColorBlindMode(boolean colorBlindMode) {
        isColorBlindMode = colorBlindMode;
        settingsService.saveSetting("accessibility_colorblind", String.valueOf(colorBlindMode));
    }

    // Colors
    public static Color getBgColor() {
        return isDarkMode ? Color.decode("#111827") : Color.decode("#F3F4F6");
    }

    public static Color getCardColor() {
        return isDarkMode ? Color.decode("#1F2937") : Color.decode("#FFFFFF");
    }

    public static Color getTextColor() {
        return isDarkMode ? Color.decode("#F3F4F6") : Color.decode("#111827");
    }

    public static Color getSubTextColor() {
        return isDarkMode ? Color.decode("#9CA3AF") : Color.decode("#4B5563");
    }

    public static Color getBorderColor() {
        return isDarkMode ? Color.decode("#374151") : Color.decode("#E5E7EB");
    }

    public static Color getPrimaryColor() {
        // High contrast Blue (#0072B2) for Color Blind Mode
        return isColorBlindMode ? Color.decode("#0072B2") : Color.decode("#3B82F6");
    }

    public static Color getSuccessColor() {
        // Distinct Orange (#E69F00) instead of Green for Color Blind Mode
        return isColorBlindMode ? Color.decode("#E69F00") : new Color(34, 197, 129);
    }

    public static Color getWarningColor() {
        // Using distinct palette: Vermillion (#D55E00) or Yellow-ish
        return isColorBlindMode ? Color.decode("#F0E442") : new Color(251, 146, 60);
    }

    public static Color getDangerColor() {
        // Red is often hard; using a very dark Red or Purple for contrast in color blind mode
        return isColorBlindMode ? Color.decode("#D55E00") : new Color(239, 68, 68);
    }

    public static Color getInfoColor() {
        // Sky Blue (#56B4E9)
        return isColorBlindMode ? Color.decode("#56B4E9") : new Color(56, 189, 248);
    }

    public static Color getGradient1() {
        return isDarkMode ? Color.decode("#000428") : (isColorBlindMode ? Color.decode("#0072B2") : Color.decode("#3B82F6"));
    }

    public static Color getGradient2() {
        return isDarkMode ? Color.decode("#004e92") : (isColorBlindMode ? Color.decode("#D55E00") : Color.decode("#60A5FA"));
    }
}
