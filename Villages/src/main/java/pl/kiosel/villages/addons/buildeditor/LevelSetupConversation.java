package pl.kiosel.villages.addons.buildeditor;

final class LevelSetupConversation {

	private static final LevelEditorField[] CREATION_ORDER = {
			LevelEditorField.ITEMS,
			LevelEditorField.EXPERIENCE,
			LevelEditorField.ECONOMY,
			LevelEditorField.SIZE
	};

	private final int level;
	private final LevelDraft draft;
	private final boolean creation;
	private int step;
	private final LevelEditorField editedField;
	private final BuildEditorType type;

	private LevelSetupConversation(int level, LevelDraft draft, boolean creation,
	                               LevelEditorField editedField, BuildEditorType type) {
		this.level = level;
		this.draft = draft;
		this.creation = creation;
		this.editedField = editedField;
		this.type = type;
	}

	static LevelSetupConversation creation(int level) {
		return creation(level, BuildEditorType.VILLAGE);
	}

	static LevelSetupConversation creation(int level, BuildEditorType type) {
		return new LevelSetupConversation(level, new LevelDraft(), true, null, type);
	}

	static LevelSetupConversation edit(int level, LevelDraft draft, LevelEditorField field) {
		return edit(level, draft, field, BuildEditorType.VILLAGE);
	}

	static LevelSetupConversation edit(int level, LevelDraft draft, LevelEditorField field, BuildEditorType type) {
		return new LevelSetupConversation(level, draft, false, field, type);
	}

	int getLevel() {
		return level;
	}

	LevelDraft getDraft() {
		return draft;
	}

	boolean isCreation() {
		return creation;
	}

	BuildEditorType getType() {
		return type;
	}

	LevelEditorField getCurrentField() {
		return creation ? CREATION_ORDER[step] : editedField;
	}

	boolean advance() {
		if (!creation || step + 1 >= CREATION_ORDER.length) {
			return false;
		}
		step++;
		return true;
	}
}
