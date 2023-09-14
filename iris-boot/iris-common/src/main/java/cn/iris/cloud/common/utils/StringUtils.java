package cn.iris.cloud.common.utils;

import cn.iris.cloud.common.constants.CommonConstants;
import cn.iris.cloud.common.io.UnsafeStringWriter;

import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StringUtils
 */

public final class StringUtils extends org.apache.commons.lang3.StringUtils {

	public static final String EMPTY_STRING = "";
	public static final int INDEX_NOT_FOUND = -1;
	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	private static final Pattern KVP_PATTERN = Pattern.compile("([_.a-zA-Z0-9][-_.a-zA-Z0-9]*)[=](.*)"); //key value pair pattern.
	private static final Pattern INT_PATTERN = Pattern.compile("^\\d+$");
	private static final int PAD_LIMIT = 8192;
	private static final byte[] HEX2B;


	static {
		HEX2B = new byte[128];
		Arrays.fill(HEX2B, (byte) -1);
		HEX2B['0'] = (byte) 0;
		HEX2B['1'] = (byte) 1;
		HEX2B['2'] = (byte) 2;
		HEX2B['3'] = (byte) 3;
		HEX2B['4'] = (byte) 4;
		HEX2B['5'] = (byte) 5;
		HEX2B['6'] = (byte) 6;
		HEX2B['7'] = (byte) 7;
		HEX2B['8'] = (byte) 8;
		HEX2B['9'] = (byte) 9;
		HEX2B['A'] = (byte) 10;
		HEX2B['B'] = (byte) 11;
		HEX2B['C'] = (byte) 12;
		HEX2B['D'] = (byte) 13;
		HEX2B['E'] = (byte) 14;
		HEX2B['F'] = (byte) 15;
		HEX2B['a'] = (byte) 10;
		HEX2B['b'] = (byte) 11;
		HEX2B['c'] = (byte) 12;
		HEX2B['d'] = (byte) 13;
		HEX2B['e'] = (byte) 14;
		HEX2B['f'] = (byte) 15;
	}

	private StringUtils() {
	}




	/**
	 * @param e
	 * @return string
	 */
	public static String toString(Throwable e) {
		UnsafeStringWriter w = new UnsafeStringWriter();
		PrintWriter p = new PrintWriter(w);
		p.print(e.getClass().getName());
		if (e.getMessage() != null) {
			p.print(": " + e.getMessage());
		}
		p.println();
		try {
			e.printStackTrace(p);
			return w.toString();
		} finally {
			p.close();
		}
	}

	/**
	 * split.
	 *
	 * @param ch char.
	 * @return string array.
	 */
	public static String[] split(String str, char ch) {
		if (isEmpty(str)) {
			return EMPTY_STRING_ARRAY;
		}
		return splitToList0(str, ch).toArray(EMPTY_STRING_ARRAY);
	}

	private static List<String> splitToList0(String str, char ch) {
		List<String> result = new ArrayList<>();
		int ix = 0, len = str.length();
		for (int i = 0; i < len; i++) {
			if (str.charAt(i) == ch) {
				result.add(str.substring(ix, i));
				ix = i + 1;
			}
		}

		if (ix >= 0) {
			result.add(str.substring(ix));
		}
		return result;
	}

	/**
	 * Splits String around matches of the given character.
	 * <p>
	 * Note: Compare with {@link StringUtils#split(String, char)}, this method reduce memory copy.
	 */
	public static List<String> splitToList(String str, char ch) {
		if (isEmpty(str)) {
			return Collections.emptyList();
		}
		return splitToList0(str, ch);
	}

	/**
	 * join string.
	 *
	 * @param array String array.
	 * @return String.
	 */
	public static String join(String[] array) {
		if (ArrayUtils.isEmpty(array)) {
			return EMPTY_STRING;
		}
		StringBuilder sb = new StringBuilder();
		for (String s : array) {
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * parse key-value pair.
	 *
	 * @param str           string.
	 * @param itemSeparator item separator.
	 * @return key-value map;
	 */
	private static Map<String, String> parseKeyValuePair(String str, String itemSeparator) {
		String[] tmp = str.split(itemSeparator);
		Map<String, String> map = new HashMap<String, String>(tmp.length);
		for (int i = 0; i < tmp.length; i++) {
			Matcher matcher = KVP_PATTERN.matcher(tmp[i]);
			if (!matcher.matches()) {
				continue;
			}
			map.put(matcher.group(1), matcher.group(2));
		}
		return map;
	}

	/**
	 * parse query string to Parameters.
	 *
	 * @param qs query string.
	 * @return Parameters instance.
	 */
	public static Map<String, String> parseQueryString(String qs) {
		if (isEmpty(qs)) {
			return new HashMap<String, String>();
		}
		return parseKeyValuePair(qs, "\\&");
	}

	public static String camelToSplitName(String camelName, String split) {
		if (isEmpty(camelName)) {
			return camelName;
		}
		StringBuilder buf = null;
		for (int i = 0; i < camelName.length(); i++) {
			char ch = camelName.charAt(i);
			if (ch >= 'A' && ch <= 'Z') {
				if (buf == null) {
					buf = new StringBuilder();
					if (i > 0) {
						buf.append(camelName, 0, i);
					}
				}
				if (i > 0) {
					buf.append(split);
				}
				buf.append(Character.toLowerCase(ch));
			} else if (buf != null) {
				buf.append(ch);
			}
		}
		return buf == null ? camelName : buf.toString();
	}

//    public static String toArgumentString(Object[] args) {
//        StringBuilder buf = new StringBuilder();
//        for (Object arg : args) {
//            if (buf.length() > 0) {
//                buf.append(COMMA_SEPARATOR);
//            }
//            if (arg == null || ReflectUtils.isPrimitives(arg.getClass())) {
//                buf.append(arg);
//            } else {
//                try {
//                    buf.append(JSON.toJSONString(arg));
//                } catch (Exception e) {
//                    logger.warn(e.getMessage(), e);
//                    buf.append(arg);
//                }
//            }
//        }
//        return buf.toString();
//    }

	public static String toOSStyleKey(String key) {
		key = key.toUpperCase().replaceAll(CommonConstants.DOT_REGEX, CommonConstants.UNDERLINE_SEPARATOR);
		if (!key.startsWith("iris_")) {
			key = "iris_" + key;
		}
		return key;
	}

	public static String deleteAny(String inString, String charsToDelete) {
		if (isNotEmpty(inString) && isNotEmpty(charsToDelete)) {
			StringBuilder sb = new StringBuilder(inString.length());

			for (int i = 0; i < inString.length(); ++i) {
				char c = inString.charAt(i);
				if (charsToDelete.indexOf(c) == -1) {
					sb.append(c);
				}
			}

			return sb.toString();
		} else {
			return inString;
		}
	}

	public static String[] toStringArray(Collection<String> collection) {
		return (String[]) collection.toArray(new String[0]);
	}

	public static String nullSafeToString(Object obj) {
		if (obj == null) {
			return "null";
		} else if (obj instanceof String) {
			return (String) obj;
		} else {
			String str = obj.toString();
			return str != null ? str : "";
		}
	}

	public static int decodeHexNibble(final char c) {
		// Character.digit() is not used here, as it addresses a larger
		// set of characters (both ASCII and full-width latin letters).
		byte[] hex2b = HEX2B;
		return c < hex2b.length ? hex2b[c] : -1;
	}

	/**
	 * Decode a 2-digit hex byte from within a string.
	 */
	public static byte decodeHexByte(CharSequence s, int pos) {
		int hi = decodeHexNibble(s.charAt(pos));
		int lo = decodeHexNibble(s.charAt(pos + 1));
		if (hi == -1 || lo == -1) {
			throw new IllegalArgumentException(String.format(
					"invalid hex byte '%s' at index %d of '%s'", s.subSequence(pos, pos + 2), pos, s));
		}
		return (byte) ((hi << 4) + lo);
	}

}
