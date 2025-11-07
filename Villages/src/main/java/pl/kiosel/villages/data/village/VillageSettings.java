package pl.kiosel.villages.data.village;

import lombok.Getter;
import pl.kiosel.villages.AdvancedVillages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VillageSettings {

    @Getter
    private boolean tnt, pvp, protection;
    private final AdvancedVillages plugin;
    private final Village village;

    public VillageSettings(Village village) {
        this.village = village;
        this.plugin = AdvancedVillages.getInstance();

        try {
            PreparedStatement statement = plugin.getDataManager().getDatabaseConnector().getConnection().prepareStatement("SELECT * FROM `" + plugin.getDataManager().getTablePrefix() + "` WHERE `village_name`=?");
            statement.setString(1, village.getVillageName());
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

	public VillageSettings(Village village, boolean tnt, boolean pvp, boolean protection) {
		this.village = village;
		this.plugin = AdvancedVillages.getInstance();

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
		this.plugin.getDataHelper().updateSettings(this.village.getVillageName(), this.convertToString());
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