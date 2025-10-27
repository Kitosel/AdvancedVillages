package pl.kiosel.common.command;

import java.util.LinkedHashMap;
import java.util.stream.Stream;

public class NestedCommand {
    final AbstractCommand parent;
    final LinkedHashMap<String, AbstractCommand> children = new LinkedHashMap<>();

    protected NestedCommand(AbstractCommand parent) {
        this.parent = parent;
    }

    public NestedCommand addSubCommand(AbstractCommand command) {
        command.getCommands().forEach(cmd -> this.children.put(cmd.toLowerCase(), command));
        return this;
    }

    public NestedCommand addSubCommands(AbstractCommand... commands) {
        Stream.of(commands).forEach(command -> command.getCommands().forEach(cmd -> this.children.put(cmd.toLowerCase(), command)));
        return this;
    }
}
