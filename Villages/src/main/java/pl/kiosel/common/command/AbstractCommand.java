package pl.kiosel.common.command;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractCommand {

    private final CommandType commandType;
    private final boolean hasArgs;
    private final List<String> handledCommands = new ArrayList<>();

    protected AbstractCommand(CommandType type, String... command) {
        this.handledCommands.addAll(Arrays.asList(command));
        this.hasArgs = false;
        this.commandType = type;
    }

    protected AbstractCommand(CommandType type, boolean hasArgs, String... command) {
        this.handledCommands.addAll(Arrays.asList(command));
        this.hasArgs = hasArgs;
        this.commandType = type;
    }

    @Deprecated
    protected AbstractCommand(boolean noConsole, String... command) {
        this.handledCommands.addAll(Arrays.asList(command));
        this.hasArgs = false;
        this.commandType = noConsole ? CommandType.PLAYER_ONLY : CommandType.CONSOLE_CAN;
    }

    @Deprecated
    protected AbstractCommand(boolean noConsole, boolean hasArgs, String... command) {
        this.handledCommands.addAll(Arrays.asList(command));
        this.hasArgs = hasArgs;
        this.commandType = noConsole ? CommandType.PLAYER_ONLY : CommandType.CONSOLE_CAN;
    }

    public final List<String> getCommands() {
        return Collections.unmodifiableList(this.handledCommands);
    }

    public final void addSubCommand(String command) {
        this.handledCommands.add(command);
    }

    protected abstract ReturnType runCommand(CommandSender sender, String... args);

    protected abstract List<String> onTab(CommandSender sender, String... args);

    public abstract String getPermissionNode();

    public abstract String getSyntax();

    public abstract String getDescription();

    public boolean hasArgs() {
        return this.hasArgs;
    }

    public boolean isNoConsole() {
        return this.commandType == CommandType.PLAYER_ONLY;
    }

    public enum ReturnType {SUCCESS, NEEDS_PLAYER, FAILURE, SYNTAX_ERROR}

    public enum CommandType {PLAYER_ONLY, CONSOLE_CAN}
}
