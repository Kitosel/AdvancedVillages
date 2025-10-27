package pl.kiosel.villages.village;

import lombok.Getter;
import pl.kiosel.villages.Wioski;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VillageSettings {

    @Getter
    private boolean tnt, pvp, protection;
    private final Wioski plugin;
    private final String village_name;

    public VillageSettings(String village_name) {
        this.village_name = village_name;
        this.plugin = Wioski.getInstance();

        try {
            PreparedStatement statement = plugin.getDatabase().getConnection().prepareStatement("SELECT * FROM `" + plugin.getDatabase().getTableVillage() + "` WHERE `village_name`=?");
            statement.setString(1, village_name);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            String settings = resultSet.getString("settings");

            this.tnt = getValueFromString(settings, "tnt");
            this.pvp = getValueFromString(settings, "pvp");
            this.protection = getValueFromString(settings, "protection");
        } catch(SQLException e) {
			String settings = getDefaultString();
			this.tnt = getValueFromString(settings, "tnt");
			this.pvp = getValueFromString(settings, "pvp");
			this.protection = getValueFromString(settings, "protection");
        }
    }

	public VillageSettings(String village_name, boolean tnt, boolean pvp, boolean protection) {
		this.village_name = village_name;
		this.plugin = Wioski.getInstance();

		this.tnt = tnt;
		this.pvp = pvp;
		this.protection = protection;
	}

    public void setTnt(boolean interact) {
        this.tnt = interact;
        save();
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
        save();
    }

    public void setProtection(boolean protection) {
        this.protection = protection;
        save();
    }

    public String convertToString() {
        return "tnt:" + tnt + ";pvp:" + pvp + ";protection:" + protection+";";
    }

    public void save() {
		String sql = "SET `settings`='" + this.convertToString() + "'\n" +
				"WHERE `village_name`='" + this.village_name + "';";
        this.plugin.getDatabase().update(sql);
    }

    public static boolean getValueFromString(String string, String flag) {
        String[] array = string.split(";");
        for(String text : array) {
            String[] text_array = text.split(":");
            if(text_array[0].equals(flag))
                return Boolean.parseBoolean(text_array[1]);
        }
        return false;
    }

    public static String getDefaultString() {
        return "tnt:" + true + ";pvp:" + true + ";protection:" + false + ";";
    }
}