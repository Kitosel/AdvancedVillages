package pl.kiosel.villages.data.user;

import pl.kiosel.core.configuration.Regex;
import pl.kiosel.core.configuration.RegexPattern;

import java.util.regex.Pattern;

public final class UserValidator {

    private static final Pattern UUID_PATTERN = Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");

    private UserValidator() {
    }

    /**
     * Validate username.
     *
     * @param name username to validate
     * @return if username is valid
     */
    public static NameResult validateUsername(String name) {
        if (name.length() < 3) {
            return NameResult.TOO_SHORT;
        }

        if (name.length() > 16) {
            return NameResult.TOO_LONG;
        }
		RegexPattern regexPattern = new RegexPattern(Regex.LETTERS_DIGITS_UNDERSCORE);

        if (!regexPattern.matches(name)) {
            return NameResult.INVALID;
        }

        return NameResult.VALID;
    }

    /**
     * Validate universally unique identifier.
     *
     * @param uuid universally unique identifier to validate
     * @return if universally unique identifier is valid
     */
    public static boolean validateUUID(String uuid) {
        return UUID_PATTERN.matcher(uuid).matches();
    }

    public enum NameResult {
        TOO_SHORT,
        TOO_LONG,
        INVALID,
        VALID
    }


}
