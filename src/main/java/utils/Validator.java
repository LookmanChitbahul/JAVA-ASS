package utils;

import java.util.regex.Pattern;

public class Validator {

    public static boolean isValidInteger(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(input.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidDouble(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(input.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.matches(emailRegex, email.trim());
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        // Allow digits, spaces, hyphens, parentheses
        String phoneRegex = "^[\\d\\s\\-\\(\\)]+$";
        return Pattern.matches(phoneRegex, phone.trim());
    }

    public static boolean isPositiveNumber(double number) {
        return number > 0;
    }

    public static boolean isNonNegativeNumber(double number) {
        return number >= 0;
    }

    public static boolean isWithinRange(double number, double min, double max) {
        return number >= min && number <= max;
    }

    public static boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }
}