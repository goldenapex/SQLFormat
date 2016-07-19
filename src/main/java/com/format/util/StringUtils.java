package com.format.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.format.base.Position;
import com.format.common.Key;

public class StringUtils {

	/**
	 * 単語単位として検索の正規表現キー、または通常文字列での検索用のキーを求める。
	 * @param key
	 * @return
	 */
	public static String getKeyDependsWord(String key) {
		String newKey = "";

		// 指定した検索文字列が単語の場合は、単語単位で検索する正規表現のキーを求める。
		// さもないと、通常の文字列として検索する。
		String regex = "\\b" + key + "\\b";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(key);
		if (m.find()) {
			newKey = regex;
		}
		else {
			newKey = key;
		}

		return newKey;
	}

	public static void getIndexesWithKey(List<Integer> indexes, String str, int start, String key) {

		// init
		if (indexes == null) {
			indexes = new ArrayList<Integer>();
		} else {
			indexes.clear();
		}

		// keyを単語単位で、且つ大文字／小文字を無視で検索する
		String regex = getKeyDependsWord(key);

		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		m.region(start, str.length());
		while (m.find()) {
			indexes.add(m.start());
		}
	}


	public static int getStartIndexOfKeyByPosition(String str, String key, int start) {
		int startIdx = -1;

		String regex = getKeyDependsWord(key);

		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(regex);
		m.region(start, str.length());
		while (m.find()) {
			startIdx = m.start();
			break;
		}

		return startIdx;
	}


	public static int getLastIndexWithKey(String str, String key) {
		int lastIdx = -1;

		String regex = getKeyDependsWord(key);

		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(regex);
		while (m.find()) {
			lastIdx = m.start();
		}

		return lastIdx;
	}


	public static int getEndIndexOfKeyByPosition(String str, String key, int position) {
		int endIdx = -1;

		String regex = "";

		regex = getKeyDependsWord(key);

		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(str);
		m.region(position,  str.length());
		while (m.find()) {
			endIdx = m.end();
			break;
		}

		return endIdx;
	}


	public static String getSubString(String str, int start, int end, boolean isTrim) {
		String ret = "";

		if (isTrim) {
			ret = str.substring(start, end).trim();
		}
		else {
			ret = str.substring(start, end);
		}

		return ret;
	}


	public static String getSubStringUntilKey(String str, String key, boolean isTrim) {

		if (isEmpty(str)) {
			return null;
		}

		int idx = getStartIndexOfKeyByPosition(str, key, 0);
		if (idx == -1) {
			return null;
		}

		return getSubString(str, 0, idx, isTrim);
	}

	public static String getSubStringAfterKey(String str, String key, boolean isTrim) {
		if (isEmpty(str)) {
			return null;
		}

		int idx = getEndIndexOfKeyByPosition(str, key, 0);
		if (idx == -1) {
			return null;
		}

		return getSubString(str, idx, isTrim);
	}


	public static String getSubString(String str, int start, boolean isTrim) {
		String ret = null;

		if (start > str.length()) {
			return null;
		}

		if (isTrim) {
			ret = str.substring(start).trim();
		} else {
			ret = str.substring(start);
		}

		return ret;
	}



	public static Position getFirstPosByKeyRegion(String str, String key, int start, int end) {

		Position pos = null;

		String regex = getKeyDependsWord(key);
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(regex);
		m.region(start, end);
		if (m.find()) {
			pos = new Position(m.start(), m.end());
		}

		return pos;
	}


	public static String trimParenthesis(String str) {
		str = str.trim();

		if (str.length() < 2) {
			return str;
		}

		if (str.startsWith(Key.CHAR_OPEN_PARENTHESIS) && str.endsWith(Key.CHAR_CLOSE_PARENTHESIS)) {
			if (str.length() == 2) {
				str = "";
			} else {
				str = getSubString(str, 1, str.length()-1, true);
			}
		}

		return str;
	}

	public static boolean isEmpty(String str) {
		boolean bRet = false;

		String regex = "^\\s+$";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(regex);
		if (str==null || "".equals(str) || m.find()) {
			bRet = true;
		}

		return bRet;
	}

	public static int countTillDesignatedPosition(List<Integer> indexes, int position) {
		int cnt = 0;

		for (Integer idx : indexes) {

			if (idx > position) {
				break;
			}

			cnt++;
		}

		return cnt;
	}



	public static int countKeyInRegion(String str, String key, int start, int end) {
		int cnt = 0;

		String regex = getKeyDependsWord(key);
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(regex);
		m.region(start, end);
		while (m.find()) {
			cnt++;
		}

		return cnt;
	}


}
