package pl.kiosel.villages.data.user;

import java.util.regex.Pattern;

public final class UserValidator {

    private static final Pattern UUID_PATTERN = Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");
	private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]+$");

    private UserValidator() {
    }

    /**
     * Validate username.
     *
     * @param name username to validate
     * @return if username is valid
     */
    public static NameResult validateUsername(String name) {
		if (name == null || name.length() < 3) {
            return NameResult.TOO_SHORT;
        }

        if (name.length() > 16) {
            return NameResult.TOO_LONG;
        }
		if (!USERNAME_PATTERN.matcher(name).matches()) {
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
