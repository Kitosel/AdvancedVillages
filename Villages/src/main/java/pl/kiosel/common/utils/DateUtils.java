package pl.kiosel.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

	protected static String v = ".";

	public static String getStringDate(long time) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		return simpleDateFormat.format(new Date(time));
	}
}
