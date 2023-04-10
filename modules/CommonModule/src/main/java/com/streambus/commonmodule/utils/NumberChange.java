package com.streambus.commonmodule.utils;

public class NumberChange {
	
	/**
	 * 在进制表示中的字符集合。
	 */
	final static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
			'9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
			'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
			'z' };

	/**
	 * 将十进制的数字转换为指定进制的字符串。
	 * 
	 * @param i
	 *            十进制的数字。
	 * @param system
	 *            指定的进制，常见的2/8/16。
	 * @return 转换后的字符串。
	 */
	public static String toCustomNumericString(long i, int system) {
		long num = 0;
		if (i < 0) {
			num = ((long) 2 * 0x7fffffff) + i + 2;
		} else {
			num = i;
		}
		char[] buf = new char[32];
		int charPos = 32;
		while ((num / system) > 0) {
			buf[--charPos] = digits[(int) (num % system)];
			num /= system;
		}
		buf[--charPos] = digits[(int) (num % system)];
		return new String(buf, charPos, (32 - charPos));
	}
}
