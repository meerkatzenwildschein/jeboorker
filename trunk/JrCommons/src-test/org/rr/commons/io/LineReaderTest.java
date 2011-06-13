package org.rr.commons.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class LineReaderTest extends TestCase {

	public void testLineReader() {
		StringBuilder b = new StringBuilder("erste\nzweite\ndritte");
		LineReader reader = new LineReader(new ByteArrayInputStream(b.toString().getBytes()));
		
		StringBuilder result = new StringBuilder();
		try {
			reader.readLine(result);

			assertEquals("erste", result.toString());
			reader.readLine(result);
			assertEquals("erstezweite", result.toString());
			reader.readLine(result);
			assertEquals("erstezweitedritte", result.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
