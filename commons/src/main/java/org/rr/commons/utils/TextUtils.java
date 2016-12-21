package org.rr.commons.utils;

import static org.rr.commons.utils.StringUtil.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.lang.math.NumberUtils;

public class TextUtils {

	public static StringBuilder removePageNumbers(String text) {
		StringBuilder result = new StringBuilder();
		try (BufferedReader textReader = new BufferedReader(new StringReader(text))) {
			String line;
			for (int i = 1; (line = trim(textReader.readLine())) != null;) {
				int num = NumberUtils.toInt(line, -1);
				if(num == i) {
					i++;
					continue;
				}
				result.append(line).append(StringUtil.NEW_LINE);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
}
