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
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;

public class EXTHRecord {

	/**
	 * @see http://wiki.mobileread.com/wiki/MOBI
	 */
	private static HashMap<Integer, String> typeHash = new HashMap<Integer, String>() {
		{
			put(100, "author");
			put(101, "publisher");
			put(102, "imprint");
			put(103, "description");
			put(104, "ISBN");
			put(105, "subject"); // Could appear multiple times
			put(106, "publishing date");
			put(107, "review");
			put(108, "contributor");
			put(109, "rights");
			put(110, "subjectcode");
			put(111, "type");
			put(112, "source");
			put(113, "ASIN"); // Kindle Paperwhite labels books with "Personal" if they don't have this record.
			put(114, "version number");
			put(115, "sample"); // 0x0001 if the book content is only a sample of the full book
			put(116, "startreading"); // Position (4-byte offset) in file at which to open when first opened
			put(117, "adult"); // Mobipocket Creator adds this if Adult only is checked on its GUI; contents: "yes"
			put(118, "retail price"); // As text, e.g. "4.99"
			put(119, "retail price currency"); // As text, e.g. "USD"
			put(121, "KF8 BOUNDARY Offset");
			put(125, "count of resources");
			put(129, "KF8 cover URI");
			put(131, "Unknown 131");
			put(200, "dictionary short name"); // As text
			put(201, "coveroffset"); // Add to first image field in Mobi Header to find PDB record containing the cover image
			put(202, "thumboffset"); // Add to first image field in Mobi Header to find PDB record containing the thumbnail cover image
			put(203, "hasfakecover");
			put(204, "Creator Software"); // Known Values: 1=mobigen, 2=Mobipocket Creator, 200=kindlegen (Windows), 201=kindlegen (Linux),
																		// 202=kindlegen (Mac).
																		// Warning: Calibre creates fake creator entries, pretending to be a Linux kindlegen 1.2 (201, 1, 2,
																		// 33307) for normal ebooks and a non-public Linux kindlegen 2.0 (201, 2, 0, 101) for periodicals.
			put(205, "Creator Major Version");
			put(206, "Creator Minor Version");
			put(207, "Creator Build Number");
			put(208, "watermark");
			put(209, "Tamper proof keys"); // Used by the Kindle (and Android app) for generating book-specific PIDs.
			put(300, "Font signature");
			put(401, "Clipping limit"); // Integer percentage of the text allowed to be clipped. Usually 10.
			put(402, "Publisher limit");
			put(403, "Unknown 403");
			put(404, "TTS off"); // 1 - Text to Speech disabled; 0 - Text to Speech enabled
			put(405, "Borrowed"); // 1 in this field seems to indicate a rental book
			put(406, "Borrowed expiration"); // If this field is removed from a rental, the book says it expired in 1969
			put(407, "Unknown 407");
			put(450, "Unknown 450");
			put(451, "Unknown 451");
			put(452, "Unknown 452");
			put(453, "Unknown 453");
			put(501, "CDE type"); // PDOC - Personal Doc; EBOK - ebook; EBSP - ebook sample;
			put(502, "Last update time");
			put(503, "Updated Title");
			put(504, "ASIN");
			put(524, "language");
			put(525, "alignment");
			put(535, "Creator Build Number"); // I found 1019-d6e4792 in this record, which is a build number of Kindlegen 2.7
			put(547, "InMemory"); // String 'I\x00n\x00M\x00e\x00m\x00o\x00r\x00y\x00' found in this record, for KindleGen V2.9 build 1029-0897292
		}
	};
	
	// if a type exists in booleanTypes, then it is assumed to have boolean
	// values
	// if a type exists in knownTypes but not in booleanTypes, then it is
	// assumed to have string values
	private static HashSet<Integer> booleanTypesSet = new HashSet<Integer>(){{
		add(404);
	}};
	
	private static HashSet<Integer> dateTypesSet = new HashSet<Integer>(){{
		add(406);
		add(106);
	}};

	private byte[] recordType = { 0, 0, 0, 0 };
	private byte[] recordLength = { 0, 0, 0, 0 };
	private byte[] recordData = null;

	public static boolean isBooleanType(int type) {
		return booleanTypesSet.contains(Integer.valueOf(type));
	}
	
	public static boolean isDateType(int type) {
		return dateTypesSet.contains(Integer.valueOf(type));
	}

	public static boolean isKnownType(int type) {
		return typeHash.containsKey(Integer.valueOf(type));
	}

	public static String getDescriptionForType(int type) {
		return typeHash.get(Integer.valueOf(type));
	}

	public EXTHRecord(int recType, String data, String characterEncoding) {
		this(recType, StreamUtils.stringToByteArray(data, characterEncoding));
	}

	public EXTHRecord(int recType, boolean data) {
		StreamUtils.intToByteArray(recType, recordType);
		recordData = new byte[1];
		recordData[0] = data ? (byte) 1 : 0;
		StreamUtils.intToByteArray(size(), recordLength);
	}

	public EXTHRecord(int recType, byte[] data) {
		StreamUtils.intToByteArray(recType, recordType);
		int len = (data == null) ? 0 : data.length;
		StreamUtils.intToByteArray(len + 8, recordLength);
		recordData = new byte[len];
		if (len > 0) {
			System.arraycopy(data, 0, recordData, 0, len);
		}
	}

	public EXTHRecord(InputStream in) throws IOException {
		MobiCommon.logMessage("*** EXTHRecord ***");

		StreamUtils.readByteArray(in, recordType);
		StreamUtils.readByteArray(in, recordLength);

		int len = StreamUtils.byteArrayToInt(recordLength);
		if (len < 8)
			throw new IOException("Invalid EXTH record length");

		recordData = new byte[len - 8];
		StreamUtils.readByteArray(in, recordData);
	}

	public int getRecordType() {
		return StreamUtils.byteArrayToInt(recordType);
	}

	public byte[] getData() {
		return recordData;
	}

	public int getDataLength() {
		return recordData.length;
	}

	public int size() {
		return getDataLength() + 8;
	}

	public void setData(String s, String encoding) {
		recordData = StreamUtils.stringToByteArray(s, encoding);
		StreamUtils.intToByteArray(size(), recordLength);
	}

	public void setData(byte[] value) {
		recordData = value;
	}
	
	public void setData(int value) {
		if (recordData == null) {
			recordData = new byte[4];
			StreamUtils.intToByteArray(size(), recordLength);
		}

		StreamUtils.intToByteArray(value, recordData);
	}

	public void setData(boolean value) {
		if (recordData == null) {
			recordData = new byte[1];
			StreamUtils.intToByteArray(size(), recordLength);
		}

		StreamUtils.intToByteArray(value ? 1 : 0, recordData);
	}

	public EXTHRecord copy() {
		return new EXTHRecord(StreamUtils.byteArrayToInt(recordType), recordData);
	}

	public boolean isKnownType() {
		return isKnownType(StreamUtils.byteArrayToInt(recordType));
	}

	public String getTypeDescription() {
		return getDescriptionForType(StreamUtils.byteArrayToInt(recordType));
	}

	public void write(OutputStream out) throws IOException {
		if (MobiCommon.debug) {
			MobiCommon.logMessage("*** Write EXTHRecord ***");
			MobiCommon.logMessage(StreamUtils.dumpByteArray(recordType));
			MobiCommon.logMessage(StreamUtils.dumpByteArray(recordLength));
			MobiCommon.logMessage(StreamUtils.dumpByteArray(recordData));
			MobiCommon.logMessage("************************");
		}
		out.write(recordType);
		out.write(recordLength);
		out.write(recordData);
	}
}
