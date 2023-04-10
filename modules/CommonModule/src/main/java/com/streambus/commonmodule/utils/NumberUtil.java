package com.streambus.commonmodule.utils;

public class NumberUtil {

	public static int parseInt(String str, int def) {
		try {
			return Integer.parseInt(str);
		} catch (Exception ignore) {
		}
		return def;
	}

	public static boolean isNumber(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (Exception ignore) {
		}
		return false;
	}

	public static double parseDouble(String str) {
		try {
			return Double.parseDouble(str);
		} catch (Exception ignore) {
		}
		return 0;
	}
}
