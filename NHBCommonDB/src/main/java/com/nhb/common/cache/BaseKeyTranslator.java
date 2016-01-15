
package com.nhb.common.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.redisson.Redisson;

public class BaseKeyTranslator implements KeyTranslator {

	private static final String REFERENCE_SUBFIX = "*";
	private static final String LIST_SUBFIX = "~";
	private static final String MAP_SUBFIX = "@";
	private static final String SEPARATOR = ":";

	public boolean isNumeric(String s) {
		return s.matches("[-+]?\\d*\\.?\\d+");
	}

	private String _translate(String keyPath, String nextElement, final Redisson redisson) throws Exception {
		String keyVal = redisson.getBucket(keyPath).get().toString();
		String subfix = Character.toString(keyVal.charAt((keyVal.length() - 1)));

		if (subfix.equals(REFERENCE_SUBFIX)) {
			return keyVal.substring(0, keyVal.length() - 1);

		} else if (subfix.equals(MAP_SUBFIX)) {
			return keyVal.substring(0, keyVal.length() - 1);
		} else if (subfix.equals(LIST_SUBFIX)) {
			if (nextElement != null) {
				if (isNumeric(nextElement)) {
					String valueList = keyVal.replace(" ", "");
					int startIndex = valueList.indexOf('[') + 1;
					int endIndex = valueList.indexOf(']');
					List<String> keyRange = new ArrayList<String>(
							Arrays.asList(valueList.substring(startIndex, endIndex).split(",")));
					String value = keyRange.get(Integer.valueOf(nextElement));
					if (value.contains(REFERENCE_SUBFIX) || value.contains(MAP_SUBFIX) || value.contains(LIST_SUBFIX)) {
						return value.substring(0, value.length() - 1);
					} else {
						return keyPath;
					}
				}
			}
		} else {
			return keyPath;
		}

		return null;
	}

	@Override
	public String translate(String key, String path, final Redisson redisson) {

		String result = "";
		result = key;
		if (path != null) {

			String[] elements = path.split(":");

			// result = key + S
			for (int i = 0; i < elements.length; i++) {
				try {
					if (isNumeric(elements[i]))
						continue;
					result = result + SEPARATOR + elements[i];
					if (i <= elements.length - 1) {
						result = _translate(result, elements[i + 1], redisson);
					} else {
						result = _translate(result, null, redisson);
					}
				} catch (Exception ex) {

				}

			}
		}
		return result;
		// TODO Auto-generated method stub

	}

}
