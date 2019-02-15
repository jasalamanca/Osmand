package net.osmand.osm;

/**
 * reference : https://wiki.openstreetmap.org/wiki/Map_Features
 */
public abstract class MapRenderingTypes {
    static final String[] langs = new String[] { "af", "als", "ar", "az", "be", "bg", "bn", "bpy", "br", "bs", "ca", "ceb", "cs", "cy", "da", "de", "el", "eo", "es", "et", "eu", "fa", "fi", "fr", "fy", "ga", "gl", "he", "hi", "hsb",
		"hr", "ht", "hu", "hy", "id", "is", "it", "ja", "ka", "ko", "ku", "la", "lb", "lo", "lt", "lv", "mk", "ml", "mr", "ms", "nds", "new", "nl", "nn", "no", "nv", "os", "pl", "pms", "pt", "ro", "ru", "sc", "sh", "sk", "sl", "sq", "sr", "sv", "sw", "ta", "te", "th", "tl", "tr", "uk", "vi", "vo", "zh" };
	
	public final static byte RESTRICTION_NO_RIGHT_TURN = 1;
	public final static byte RESTRICTION_NO_LEFT_TURN = 2;
	public final static byte RESTRICTION_NO_U_TURN = 3;
	public final static byte RESTRICTION_NO_STRAIGHT_ON = 4;
	public final static byte RESTRICTION_ONLY_RIGHT_TURN = 5;
	public final static byte RESTRICTION_ONLY_LEFT_TURN = 6;
	public final static byte RESTRICTION_ONLY_STRAIGHT_ON = 7;

    public static String getRestrictionValue(int i) {
		switch (i) {
		case RESTRICTION_NO_RIGHT_TURN:
			return "NO_RIGHT_TURN".toLowerCase();
		case RESTRICTION_NO_LEFT_TURN:
			return "NO_LEFT_TURN".toLowerCase();
		case RESTRICTION_NO_U_TURN:
			return "NO_U_TURN".toLowerCase();
		case RESTRICTION_NO_STRAIGHT_ON:
			return "NO_STRAIGHT_ON".toLowerCase();
		case RESTRICTION_ONLY_RIGHT_TURN:
			return "ONLY_RIGHT_TURN".toLowerCase();
		case RESTRICTION_ONLY_LEFT_TURN:
			return "ONLY_LEFT_TURN".toLowerCase();
		case RESTRICTION_ONLY_STRAIGHT_ON:
			return "ONLY_STRAIGHT_ON".toLowerCase();
		}
		return "unkonwn";
    }
}