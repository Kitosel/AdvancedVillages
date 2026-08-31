package pl.kiosel.villages.storage;

import lombok.Getter;
import pl.kiosel.rosacore.database.DatabaseManager;
import pl.kiosel.rosacore.database.DatabaseSession;
import pl.kiosel.rosacore.database.SqlAction;

import java.util.Objects;

public final class VillageDataManager {

	@Getter
	private final DatabaseManager database;

	public VillageDataManager(DatabaseManager database) {
		this.database = Objects.requireNonNull(database, "database");
	}

	public String getTablePrefix() {
		return this.database.getTablePrefix();
	}

	public void executeUpdate(String sql, Object... parameters) {
		this.database.executeUpdate(sql, parameters);
	}

	public void withConnection(SqlAction action) {
		this.database.withConnection(action);
	}

	public void withSession(SessionAction action) {
		Objects.requireNonNull(action, "action");
		this.database.withConnection(connection -> action.accept(this.database.session(connection)));
	}

	public void withTransaction(SessionAction action) {
		Objects.requireNonNull(action, "action");
		this.database.transaction(connection -> action.accept(this.database.session(connection)));
	}

	@FunctionalInterface
	public interface SessionAction {
		void accept(DatabaseSession session) throws Exception;
	}
}
