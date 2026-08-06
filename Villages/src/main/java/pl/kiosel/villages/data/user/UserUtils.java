package pl.kiosel.villages.data.user;

import org.apache.commons.lang3.StringUtils;
import panda.std.Option;
import panda.std.Result;
import pl.kiosel.core.data.yaml.YamlWrapper;
import pl.kiosel.core.utils.format.RawString;
import pl.kiosel.villages.AdvancedVillages;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class UserUtils {

    /**
     * Gets the set of usernames (with tags to format) from collection of users.
     *
     * @param users collection of users
     * @return set of usernames (with tags to format)
     */
    public static Set<String> getOnlineNames(Collection<User> users) {
        Set<String> set = new HashSet<>();
        for (User user : users) {
            set.add(user.isOnline() ? "<online>" + user.getName() + "</online>" : user.getName());
        }

        return set;
    }

    public static String getUserPosition(@Nullable User user) {
        if (user == null) {
            return "";
        }

        if (user.isOwner()) {
            return new RawString("*").getValue();
        }

        return new RawString("").getValue();
    }

    /**
     * Check if user file is correct and if not - try migrating it
     *
     * @param file user file
     * @return A final (source or migrated) user file
     */
    public static Option<File> checkUserFile(File file) {
        String filenameWithoutExtension = StringUtils.removeEnd(file.getName(), ".yml");
        if (UserValidator.validateUUID(filenameWithoutExtension)) {
            return Option.of(file);
        }

        if (UserValidator.validateUsername(filenameWithoutExtension) != UserValidator.NameResult.VALID) {
            return migrateUserFile(file)
                    .onError(error -> AdvancedVillages.getInstance().getDebug().debug(error))
                    .toOption();
        }

        return Option.none();
    }

    /**
     * Try migrating a user file to a new name
     *
     * @param file user file
     * @return Result with migrated user file or a migration error message
     */
    public static Result<File, String> migrateUserFile(File file) {
        YamlWrapper wrapper = new YamlWrapper(file);
        String id = wrapper.getString("uuid");

        if (id == null || !UserValidator.validateUUID(id)) {
            return Result.error("Migration of user file '" + file.getName() + "' failed, UUID is invalid");
        }

        Path source = file.toPath();
        Path target = source.resolveSibling(String.format("%s.yml", id));

        if (Files.exists(target)) {
            return Result.ok(target.toFile());
        }

        return Result.attempt(IOException.class, () -> Files.move(source, target, StandardCopyOption.REPLACE_EXISTING).toFile())
                .mapErr(error -> "Could not move file '" + source + "' to '" + target + "': " + error.getMessage());
    }

}
