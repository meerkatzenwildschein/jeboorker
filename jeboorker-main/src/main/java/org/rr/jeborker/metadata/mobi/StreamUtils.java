/**
 * Copyright (c) 2012 Kin-Wai Koo
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files ("the Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED ÒAS ISÓ, WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 * @see https://github.com/gluggy/Java-Mobi-Metadata-Editor
 */
package org.rr.jeborker.metadata.mobi;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;

public class StreamUtils {
	public static String readCString(InputStream in, int len) throws IOException {
		byte[] buffer = new byte[len];
		int bytesLeft = len;
		int offset = 0;

		while (bytesLeft > 0) {
			int bytesRead = in.read(buffer, offset, bytesLeft);
			if (bytesRead == -1)
				throw new IOException("Supposed to read a " + len + " byte C string, but could not");
			offset += bytesRead;
			bytesLeft -= bytesRead;
		}

		String s = byteArrayToString(buffer);
		LoggerFactory.log(Level.WARNING, StreamUtils.class, "readCString: " + s);
		return s;
	}

	public static byte readByte(InputStream in) throws IOException {
		int b = in.read();
		if (b == -1)
			throw new IOException("Supposed to read a byte, but could not");
		LoggerFactory.log(Level.WARNING, StreamUtils.class, "readByte: " + b);
		return (byte) (b & 0xff);
	}

	public static void readByteArray(InputStream in, byte[] buffer) throws IOException {
		int len = buffer.length;
		int bytesLeft = len;
		int offset = 0;

		while (bytesLeft > 0) {
			int bytesRead = in.read(buffer, offset, bytesLeft);
			if (bytesRead == -1)
				throw new IOException("Supposed to read a " + len + " byte array, but could not");
			offset += bytesRead;
			bytesLeft -= bytesRead;
		}
	}

	public static String byteArrayToString(byte[] buffer) {
		return byteArrayToString(buffer, null);
	}

	public static String byteArrayToString(byte[] buffer, String encoding) {
		int len = buffer.length;
		int zeroIndex = -1;
		for (int i = 0; i < len; i++) {
			byte b = buffer[i];
			if (b == 0) {
				zeroIndex = i;
				break;
			}
		}

		if (encoding != null) {
			try {
				if (zeroIndex == -1)
					return new String(buffer, encoding);
				else
					return new String(buffer, 0, zeroIndex, encoding);
			} catch (java.io.UnsupportedEncodingException e) {
				// let it fall through and use the default encoding
			}
		}

		if (zeroIndex == -1)
			return new String(buffer);
		else
			return new String(buffer, 0, zeroIndex);
	}

	public static int byteArrayToInt(byte[] buffer) {
		int total = 0;
		int len = buffer.length;
		for (int i = 0; i < len; i++) {
			total = (total << 8) + (buffer[i] & 0xff);
		}

		return total;
	}

	public static long byteArrayToLong(byte[] buffer) {
		long total = 0;
		int len = buffer.length;
		for (int i = 0; i < len; i++) {
			total = (total << 8) + (buffer[i] & 0xff);
		}

		return total;
	}

	public static void intToByteArray(int value, byte[] dest) {
		int lastIndex = dest.length - 1;
		for (int i = lastIndex; i >= 0; i--) {
			dest[i] = (byte) (value & 0xff);
			value = value >> 8;
		}
	}

	public static void longToByteArray(long value, byte[] dest) {
		int lastIndex = dest.length - 1;
		for (int i = lastIndex; i >= 0; i--) {
			dest[i] = (byte) (value & 0xff);
			value = value >> 8;
		}
	}

	public static byte[] stringToByteArray(String s) {
		return stringToByteArray(s, null);
	}

	public static byte[] stringToByteArray(String s, String encoding) {
		if (encoding != null) {
			try {
				return s.getBytes(encoding);
			} catch (UnsupportedEncodingException e) {
				// let if fall through to use the default character encoding
			}
		}

		return s.getBytes();
	}

	public static String dumpByteArray(byte[] buffer) {
		StringBuffer sb = new StringBuffer();
		sb.append("{ ");
		int len = buffer.length;
		for (int i = 0; i < len; i++) {
			if (i > 0)
				sb.append(", ");
			sb.append(buffer[i] & 0xff);
		}
		sb.append(" }");
		return sb.toString();
	}
}
