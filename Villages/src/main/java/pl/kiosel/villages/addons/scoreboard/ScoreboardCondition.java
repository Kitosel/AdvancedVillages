package pl.kiosel.villages.addons.scoreboard;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Permission;
import pl.kiosel.villages.data.village.Village;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ScoreboardCondition {

	private final Node root;

	private ScoreboardCondition(Node root) {
		this.root = root;
	}

	static ScoreboardCondition compile(String expression) {
		if (expression == null || expression.isBlank()) {
			throw new IllegalArgumentException("condition cannot be blank");
		}
		return new ScoreboardCondition(new Parser(expression).parse());
	}

	boolean test(Context context) {
		return this.root.test(context);
	}

	static Context context(AdvancedVillages plugin, Player player, User user, Village village) {
		return new Context(plugin, player, user, village);
	}

	@FunctionalInterface
	private interface Node {
		boolean test(Context context);
	}

	static final class Context {

		private final AdvancedVillages plugin;
		private final Player player;
		private final User user;
		private final Village village;

		private Context(AdvancedVillages plugin, Player player, User user, Village village) {
			this.plugin = plugin;
			this.player = player;
			this.user = user;
			this.village = village;
		}
	}

	public static final class Parser {
		private final String source;
		private int position;

		private Parser(String source) {
			this.source = source;
		}

		private Node parse() {
			Node result = parseOr();
			skipWhitespace();
			if (this.position != this.source.length()) {
				throw error("unexpected text");
			}
			return result;
		}

		private Node parseOr() {
			Node left = parseAnd();
			while (match("||")) {
				Node previous = left;
				Node right = parseAnd();
				left = context -> previous.test(context) || right.test(context);
			}
			return left;
		}

		private Node parseAnd() {
			Node left = parseUnary();
			while (match("&&")) {
				Node previous = left;
				Node right = parseUnary();
				left = context -> previous.test(context) && right.test(context);
			}
			return left;
		}

		private Node parseUnary() {
			if (match("!")) {
				Node nested = parseUnary();
				return context -> !nested.test(context);
			}
			if (match("(")) {
				Node nested = parseOr();
				expect(")");
				return nested;
			}
			return parseValue();
		}

		private Node parseValue() {
			String identifier = readIdentifier();
			if (identifier.equalsIgnoreCase("true")) return context -> true;
			if (identifier.equalsIgnoreCase("false")) return context -> false;

			expect("(");
			List<String> arguments = readArguments();
			return function(identifier, arguments);
		}

		private Node function(String identifier, List<String> arguments) {
			String name = identifier.toLowerCase(Locale.ROOT);
			switch (name) {
				case "user.invillage":
				case "user.hasvillage":
					requireArguments(identifier, arguments, 0);
					return context -> context.user != null && context.user.hasVillage();
				case "user.isowner":
					requireArguments(identifier, arguments, 0);
					return context -> context.user != null && context.user.isOwner();
				case "user.isonline":
					requireArguments(identifier, arguments, 0);
					return context -> context.user != null && context.user.isOnline();
				case "user.isvanished":
					requireArguments(identifier, arguments, 0);
					return context -> context.user != null && context.user.isVanished();
				case "user.haspermission":
				case "player.haspermission": {
					requireArguments(identifier, arguments, 1);
					String permission = arguments.get(0);
					return context -> context.player != null && context.player.hasPermission(permission);
				}
				case "user.hasvillagepermission": {
					requireArguments(identifier, arguments, 1);
					Permission permission;
					try {
						permission = Permission.valueOf(arguments.get(0).trim().toUpperCase(Locale.ROOT).replace('-', '_'));
					} catch (IllegalArgumentException exception) {
						throw error("unknown village permission '" + arguments.get(0) + "'");
					}
					Permission required = permission;
					return context -> context.user != null && context.user.hasVillagePermission(required);
				}
				case "player.isop":
					requireArguments(identifier, arguments, 0);
					return context -> context.player != null && context.player.isOp();
				case "village.exists":
					requireArguments(identifier, arguments, 0);
					return context -> context.village != null;
				case "village.hastag":
					requireArguments(identifier, arguments, 0);
					return context -> context.village != null && context.village.isTag();
				case "village.pvpenabled":
				case "village.ispvp":
					requireArguments(identifier, arguments, 0);
					return context -> context.village != null && context.village.hasPvPEnabled();
				case "village.tntenabled":
				case "village.istnt":
					requireArguments(identifier, arguments, 0);
					return context -> context.village != null && context.village.hasTntEnabled();
				case "village.animationsenabled":
				case "village.isanimationsenabled":
					requireArguments(identifier, arguments, 0);
					return context -> context.village != null && context.village.isAnimationsEnabled();
				case "village.hashome":
					requireArguments(identifier, arguments, 0);
					return context -> context.village != null && context.village.hasHome();
				case "village.hasregion":
					requireArguments(identifier, arguments, 0);
					return context -> context.village != null && context.village.hasRegion();
				case "village.hasallies":
					requireArguments(identifier, arguments, 0);
					return context -> context.plugin != null && context.village != null
							&& !context.plugin.getDiplomacyManager().getAllies(context.village).isEmpty();
				case "village.haswars":
					requireArguments(identifier, arguments, 0);
					return context -> context.plugin != null && context.village != null
							&& context.plugin.getDiplomacyManager().countCurrentWars(context.village) > 0;
				case "village.levelatleast": {
					int level = integerArgument(identifier, arguments);
					return context -> context.village != null && context.village.getLevel() != null
							&& context.village.getLevel().getLevel() >= level;
				}
				case "village.bankatleast": {
					int bank = integerArgument(identifier, arguments);
					return context -> context.village != null && context.village.getBank() >= bank;
				}
				case "village.livesatleast": {
					int lives = integerArgument(identifier, arguments);
					return context -> context.village != null && context.village.getLives() >= lives;
				}
				default:
					throw error("unknown function '" + identifier + "'");
			}
		}

		private int integerArgument(String identifier, List<String> arguments) {
			requireArguments(identifier, arguments, 1);
			try {
				return Integer.parseInt(arguments.get(0));
			} catch (NumberFormatException exception) {
				throw error(identifier + " requires an integer");
			}
		}

		private void requireArguments(String identifier, List<String> arguments, int expected) {
			if (arguments.size() != expected) {
				throw error(identifier + " requires " + expected + " argument(s)");
			}
		}

		private List<String> readArguments() {
			List<String> arguments = new ArrayList<>();
			skipWhitespace();
			if (match(")")) return arguments;

			while (true) {
				arguments.add(readArgument());
				if (match(")")) return arguments;
				expect(",");
			}
		}

		private String readArgument() {
			skipWhitespace();
			if (this.position >= this.source.length()) throw error("missing argument");

			char first = this.source.charAt(this.position);
			if (first == '\'' || first == '"') {
				this.position++;
				StringBuilder value = new StringBuilder();
				boolean escaped = false;
				while (this.position < this.source.length()) {
					char current = this.source.charAt(this.position++);
					if (escaped) {
						value.append(current);
						escaped = false;
					} else if (current == '\\') {
						escaped = true;
					} else if (current == first) {
						return value.toString();
					} else {
						value.append(current);
					}
				}
				throw error("unterminated quoted argument");
			}

			int start = this.position;
			while (this.position < this.source.length()) {
				char current = this.source.charAt(this.position);
				if (current == ',' || current == ')') break;
				this.position++;
			}
			String value = this.source.substring(start, this.position).trim();
			if (value.isEmpty()) throw error("argument cannot be blank");
			return value;
		}

		private String readIdentifier() {
			skipWhitespace();
			int start = this.position;
			while (this.position < this.source.length()) {
				char current = this.source.charAt(this.position);
				if (!Character.isLetterOrDigit(current) && current != '_' && current != '-' && current != '.') break;
				this.position++;
			}
			if (start == this.position) throw error("expected a function or boolean value");
			return this.source.substring(start, this.position);
		}

		private boolean match(String expected) {
			skipWhitespace();
			if (!this.source.startsWith(expected, this.position)) return false;
			this.position += expected.length();
			return true;
		}

		private void expect(String expected) {
			if (!match(expected)) throw error("expected '" + expected + "'");
		}

		private void skipWhitespace() {
			while (this.position < this.source.length() && Character.isWhitespace(this.source.charAt(this.position))) {
				this.position++;
			}
		}

		private IllegalArgumentException error(String message) {
			return new IllegalArgumentException(message + " at position " + this.position + " in '" + this.source + "'");
		}
	}
}
