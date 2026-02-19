package ui;

import java.awt.Color;
import services.SettingsService;

public class AppTheme {
    private static boolean isDarkMode = true;
    private static final SettingsService settingsService = new SettingsService();

    static {
        isDarkMode = Boolean.parseBoolean(settingsService.getSetting("theme_dark", "true"));
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
        settingsService.saveSetting("theme_dark", String.valueOf(darkMode));
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
        return Color.decode("#3B82F6");
    }

    public static Color getGradient1() {
        return isDarkMode ? Color.decode("#000428") : Color.decode("#3B82F6");
    }

    public static Color getGradient2() {
        return isDarkMode ? Color.decode("#004e92") : Color.decode("#60A5FA");
    }
}
