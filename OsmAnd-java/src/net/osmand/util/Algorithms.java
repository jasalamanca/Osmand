package net.osmand.util;

import net.osmand.IProgress;
import net.osmand.PlatformUtil;

import org.apache.commons.logging.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * Basic algorithms that are not in jdk
 */
public class Algorithms {
	private static final int BUFFER_SIZE = 1024;
	private static final Log log = PlatformUtil.getLog(Algorithms.class);

	public static boolean isEmpty(Collection c) {
		return c == null || c.size() == 0;
	}

	public static boolean isEmpty(Map map) {
		return map == null || map.size() == 0;
	}

	public static boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}

	public static boolean isBlank(String s) {
		return s == null || s.trim().length() == 0;
	}

	public static boolean stringsEqual(String s1, String s2) {
		if (s1 == null && s2 == null) {
			return true;
		} else if (s1 == null) {
			return false;
		} else if (s2 == null) {
			return false;
		}
		return s2.equals(s1);
	}

	public static long parseLongSilently(String input, long def) {
		if (input != null && input.length() > 0) {
			try {
				return Long.parseLong(input);
			} catch (NumberFormatException e) {
				return def;
			}
		}
		return def;
	}
	
	public static int parseIntSilently(String input, int def) {
		if (input != null && input.length() > 0) {
			try {
				return Integer.parseInt(input);
			} catch (NumberFormatException e) {
				return def;
			}
		}
		return def;
	}


	public static String getFileNameWithoutExtension(File f) {
		String name = f.getName();
		int i = name.indexOf('.');
		if (i >= 0) {
			name = name.substring(0, i);
		}
		return name;
	}

    public static Comparator<File> getFileVersionComparator() {
		return new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return -simplifyFileName(o1.getName()).compareTo(simplifyFileName(o2.getName()));
			}

		};
	}

    private static String simplifyFileName(String fn) {
        String lc = fn.toLowerCase();
        if (lc.contains(".")) {
            lc = lc.substring(0, lc.indexOf("."));
        }
        if (lc.endsWith("_2")) {
            lc = lc.substring(0, lc.length() - "_2".length());
        }
        boolean hasTimestampEnd = false;
        for (int i = 0; i < lc.length(); i++) {
            if (lc.charAt(i) >= '0' && lc.charAt(i) <= '9') {
                hasTimestampEnd = true;
                break;
            }
        }
        if (!hasTimestampEnd) {
            lc += "_00_00_00";
        }
        return lc;
    }

    public static Comparator<String> getStringVersionComparator() {
        return new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return -simplifyFileName(o1).compareTo(simplifyFileName(o2));
            }
        };
    }

	private static final char CHAR_TOSPLIT = 0x01;

	public static Map<String, String> decodeMap(String s) {
		if (isEmpty(s)) {
			return Collections.emptyMap();
		}
		Map<String, String> names = new HashMap<>();
		String[] split = s.split(CHAR_TOSPLIT + "");
		// last split is an empty string
		for (int i = 1; i < split.length; i += 2) {
			names.put(split[i - 1], split[i]);
		}
		return names;
	}

    public static int findFirstNumberEndIndex(String value) {
		int i = 0;
		boolean valid = false;
		if (value.length() > 0 && value.charAt(0) == '-') {
			i++;
		}
		while (i < value.length() && (isDigit(value.charAt(i)) || value.charAt(i) == '.')) {
			i++;
			valid = true;
		}
		if (valid) {
			return i;
		} else {
			return -1;
		}
	}

	public static boolean isDigit(char charAt) {
		return charAt >= '0' && charAt <= '9';
	}

    public static String capitalizeFirstLetterAndLowercase(String s) {
		if (s != null && s.length() > 1) {
			// not very efficient algorithm
			return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
		} else {
			return s;
		}
	}

	public static String capitalizeFirstLetter(String s) {
		if (s != null && s.length() > 0) {
			return Character.toUpperCase(s.charAt(0)) + (s.length() > 1 ? s.substring(1) : "");
		} else {
			return s;
		}
	}

	public static boolean objectEquals(Object a, Object b) {
		if (a == null) {
			return b == null;
		} else {
			return a.equals(b);
		}
	}


	/**
	 * Parse the color string, and return the corresponding color-int.
	 * If the string cannot be parsed, throws an IllegalArgumentException
	 * exception. Supported formats are:
	 * #RRGGBB
	 * #AARRGGBB
	 * 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta',
	 * 'yellow', 'lightgray', 'darkgray'
	 */
	public static int parseColor(String colorString) {
		if (colorString.charAt(0) == '#') {
			// Use a long to avoid rollovers on #ffXXXXXX
			if (colorString.length() == 4) {
				colorString = "#" +
						colorString.charAt(1) + colorString.charAt(1) +
						colorString.charAt(2) + colorString.charAt(2) +
						colorString.charAt(3) + colorString.charAt(3);
			}
			long color = Long.parseLong(colorString.substring(1), 16);
			if (colorString.length() == 7) {
				// Set the alpha value
				color |= 0x00000000ff000000;
			} else if (colorString.length() != 9) {
				throw new IllegalArgumentException("Unknown color " + colorString); //$NON-NLS-1$
			}
			return (int) color;
		}
		throw new IllegalArgumentException("Unknown color " + colorString); //$NON-NLS-1$
	}


	public static int extractFirstIntegerNumber(String s) {
		int i = 0;
		for (int k = 0; k < s.length(); k++) {
			if (isDigit(s.charAt(k))) {
				i = i * 10 + (s.charAt(k) - '0');
			} else {
				break;
			}
		}
		return i;
	}

	public static int extractIntegerNumber(String s) {
		int i = 0;
		int k;
		for (k = 0; k < s.length(); k++) {
			if (isDigit(s.charAt(k))) {
				break;
			}
		}
		for (; k < s.length(); k++) {
			if (isDigit(s.charAt(k))) {
				i = i * 10 + (s.charAt(k) - '0');
			} else {
				break;
			}
		}
		return i;
	}

	public static String extractIntegerPrefix(String s) {
		int k = 0;
		for (; k < s.length(); k++) {
			if (Character.isDigit(s.charAt(k))) {
				return s.substring(0, k);
			}
		}
		return "";
	}

    public static String extractIntegerSuffix(String s) {
		int k = 0;
		for (; k < s.length(); k++) {
			if (!Character.isDigit(s.charAt(k))) {
				return s.substring(k);
			}
		}
		return "";
	}

	public static void fileCopy(File src, File dst) throws IOException {
		FileOutputStream fout = new FileOutputStream(dst);
		try {
			FileInputStream fin = new FileInputStream(src);
			try {
				Algorithms.streamCopy(fin, fout);
			} finally {
				fin.close();
			}
		} finally {
			fout.close();
		}
	}

	public static void streamCopy(InputStream in, OutputStream out) throws IOException {
		byte[] b = new byte[BUFFER_SIZE];
		int read;
		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}
	}


	public static void streamCopy(InputStream in, OutputStream out, IProgress pg, int bytesDivisor) throws IOException {
		byte[] b = new byte[BUFFER_SIZE];
		int read;
		int cp = 0;
		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
			cp += read;
			if (pg != null && cp > bytesDivisor) {
				pg.progress(cp / bytesDivisor);
				cp = cp % bytesDivisor;
			}
		}
	}

    public static void closeStream(Closeable stream) {
		try {
			if (stream != null) {
				stream.close();
			}
		} catch (IOException e) {
			log.warn("Closing stream warn", e); //$NON-NLS-1$
		}
	}

    public static StringBuilder readFromInputStream(InputStream i) throws IOException {
		StringBuilder responseBody = new StringBuilder();
		responseBody.setLength(0);
		if (i != null) {
			BufferedReader in = new BufferedReader(new InputStreamReader(i, "UTF-8"), 256); //$NON-NLS-1$
			String s;
			boolean f = true;
			while ((s = in.readLine()) != null) {
				if (!f) {
					responseBody.append("\n"); //$NON-NLS-1$
				} else {
					f = false;
				}
				responseBody.append(s);
			}
		}
		return responseBody;
	}

	public static boolean removeAllFiles(File f) {
		if (f == null) {
			return false;
		}
		if (f.isDirectory()) {
			File[] fs = f.listFiles();
			if (fs != null) {
				for (File c : fs) {
					removeAllFiles(c);
				}
			}
			return f.delete();
		} else {
			return f.delete();
		}
	}


    public static void writeInt(OutputStream stream, int l) throws IOException {
		stream.write(l & 0xff);
		l >>= 8;
		stream.write(l & 0xff);
		l >>= 8;
		stream.write(l & 0xff);
		l >>= 8;
		stream.write(l & 0xff);
	}


    public static String formatDuration(int seconds, boolean fullForm) {
		String sec;
		if (seconds % 60 < 10) {
			sec = "0" + (seconds % 60);
		} else {
			sec = (seconds % 60) + "";
		}
		int minutes = seconds / 60;
		if ((!fullForm) && (minutes < 60)) {
			return minutes + ":" + sec;
		} else {
			String min;
			if (minutes % 60 < 10) {
				min = "0" + (minutes % 60);
			} else {
				min = (minutes % 60) + "";
			}
			int hours = minutes / 60;
			return hours + ":" + min + ":" + sec;
		}
	}

	public static String formatMinutesDuration(int minutes) {
		if (minutes < 60) {
			return String.valueOf(minutes);
		} else {
			int min = minutes % 60;
			int hours = minutes / 60;
			return String.format(Locale.UK, "%02d:%02d", hours, min);
		}
	}

	public static <T extends Enum<T>> T parseEnumValue(T[] cl, String val, T defaultValue) {
		for (T aCl : cl) {
			if (aCl.name().equalsIgnoreCase(val)) {
				return aCl;
			}
		}
		return defaultValue;
	}

	public static String colorToString(int color) {
		if ((0xFF000000 & color) == 0xFF000000) {
			return "#" + format(6, Integer.toHexString(color & 0x00FFFFFF)); //$NON-NLS-1$
		} else {
			return "#" + format(8, Integer.toHexString(color)); //$NON-NLS-1$
		}
	}

	private static String format(int i, String hexString) {
		while (hexString.length() < i) {
			hexString = "0" + hexString;
		}
		return hexString;
	}

    public static int compare(int x, int y) {
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}
	
	public static int compare(long x, long y) {
		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}

}