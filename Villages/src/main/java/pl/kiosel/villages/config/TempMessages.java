package pl.kiosel.villages.config;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;
import pl.kiosel.core.utils.format.Formater;
import pl.kiosel.core.utils.format.RangeFormatting;
import pl.kiosel.core.utils.format.RawString;
import pl.kiosel.villages.data.village.Village;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TempMessages {

	public static String noValue = "Brak";

	public static RawString ptop = new RawString(" &7[%VALUE-FORMAT%&7]");
	public static RawString gtop = new RawString(" &7[&b%VALUE-FORMAT%&7]");
	public static RawString gtopPoints = new RawString(" &7[&b%POINTS-FORMAT%&7]");
	public static RawString ptopPoints = new RawString(" &7[%POINTS%&7]");

	public static List<RangeFormatting> pointsFormat = Arrays.asList(
			new RangeFormatting(0, 749, "&4%POINTS%"),
			new RangeFormatting(750, 999, "&c%POINTS%"),
			new RangeFormatting(1000, 1499, "&a%POINTS%"),
			new RangeFormatting(1500, Integer.MAX_VALUE, "&6&l%POINTS%")
	);

	public static List<RangeFormatting> pingFormat = Arrays.asList(
			new RangeFormatting(0, 75, "&a%PING%"),
			new RangeFormatting(76, 150, "&e%PING%"),
			new RangeFormatting(151, 300, "&c%PING%"),
			new RangeFormatting(301, Integer.MAX_VALUE, "&c%PING%")
	);

	public static Map<String, List<RangeFormatting>> ptopValueFormatting = ImmutableMap.<String, List<RangeFormatting>>builder()
			.put("points", Arrays.asList(
					new RangeFormatting(0, 749, "&4%VALUE%"),
					new RangeFormatting(750, 999, "&c%VALUE%"),
					new RangeFormatting(1000, 1499, "&a%VALUE%"),
					new RangeFormatting(1500, Integer.MAX_VALUE, "&6&l%VALUE%")
			))
			.put("kills", Arrays.asList(
					new RangeFormatting(0, 10, "&c%VALUE%"),
					new RangeFormatting(11, 25, "&a%VALUE%"),
					new RangeFormatting(26, 50, "&e%VALUE%"),
					new RangeFormatting(51, Integer.MAX_VALUE, "&6&l%VALUE%")
			))
			.put("deaths", Arrays.asList(
					new RangeFormatting(0, 10, "&c%VALUE%"),
					new RangeFormatting(11, 25, "&a%VALUE%"),
					new RangeFormatting(26, 50, "&e%VALUE%"),
					new RangeFormatting(51, Integer.MAX_VALUE, "&6&l%VALUE%")
			))
			.build();

	public static Map<String, List<RangeFormatting>> gtopValueFormatting = ImmutableMap.<String, List<RangeFormatting>>builder()
			.put("kills", Arrays.asList(
					new RangeFormatting(0, 30, "&c%VALUE%"),
					new RangeFormatting(31, 75, "&a%VALUE%"),
					new RangeFormatting(76, 150, "&e%VALUE%"),
					new RangeFormatting(151, Integer.MAX_VALUE, "&6&l%VALUE%")
			))
			.put("deaths", Arrays.asList(
					new RangeFormatting(0, 30, "&c%VALUE%"),
					new RangeFormatting(31, 75, "&a%VALUE%"),
					new RangeFormatting(76, 150, "&e%VALUE%"),
					new RangeFormatting(151, Integer.MAX_VALUE, "&6&l%VALUE%")
			))
			.put("avg_points", Arrays.asList(
					new RangeFormatting(0, 749, "&4%VALUE%"),
					new RangeFormatting(750, 999, "&c%VALUE%"),
					new RangeFormatting(1000, 1499, "&a%VALUE%"),
					new RangeFormatting(1500, Integer.MAX_VALUE, "&6&l%VALUE%")
			))
			.build();

	public static RelationalTag relationalTag = new RelationalTag();

	public static class RelationalTag {

		public RawString our = new RawString("&a%TAG%&f");

		public RawString other = new RawString("&7%TAG%&f");

		public String chooseTag(@Nullable Village guild, @Nullable Village targetGuild) {
			if (targetGuild == null) {
				return "";
			}

			if (guild == null) {
				return this.other.getValue();
			}

			if (guild.equals(targetGuild)) {
				return this.our.getValue();
			}

			return this.other.getValue();
		}

		public String chooseAndPrepareTag(@Nullable Village guild, @Nullable Village targetGuild) {
			if (targetGuild == null) {
				return "";
			}

			return Formater.of("%TAG%", targetGuild.getTag())
					.format(this.chooseTag(guild, targetGuild));
		}

	}

	public static RawString online = new RawString("&a");
	public static RawString offline = new RawString("&c");
}
