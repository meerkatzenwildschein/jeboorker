package org.rr.commons.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import junit.framework.TestCase;

public class ByteFilterInputStreamTest extends TestCase {

	public void test1() {
		byte[] buf = new byte[] {0x00, 0x64, 0x00, 0x65, 0x66};
		ByteFilterInputStream nullFilterInputStream = new ByteFilterInputStream(new ByteArrayInputStream(buf), new byte[] {0x00});
		try {
			byte[] byteArray = IOUtils.toByteArray(nullFilterInputStream);
			assertEquals(3, byteArray.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("");
	}
}
