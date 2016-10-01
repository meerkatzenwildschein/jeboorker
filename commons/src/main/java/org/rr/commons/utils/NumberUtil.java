package org.rr.commons.utils;

import java.nio.ByteBuffer;

public class NumberUtil {

	private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static byte[] toByteArray(long l) {
		return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(l).array();
	}

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

}
