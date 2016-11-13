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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @see http://wiki.mobileread.com/wiki/PDB#Palm_Database_Format
 */
public class PDBHeader {
	private byte[] name = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // 0
	private byte[] attributes = { 0, 0 }; // 32
	private byte[] version = { 0, 0 }; // 34
	private byte[] creationDate = { 0, 0, 0, 0 }; // 36
	private byte[] modificationDate = { 0, 0, 0, 0 }; // 40
	private byte[] lastBackupDate = { 0, 0, 0, 0 }; // 44
	private byte[] modificationNumber = { 0, 0, 0, 0 }; // 48
	private byte[] appInfoID = { 0, 0, 0, 0 }; // 52
	private byte[] sortInfoID = { 0, 0, 0, 0 }; // 56
	private byte[] type = { 0, 0, 0, 0 }; // 60
	private byte[] creator = { 0, 0, 0, 0 }; // 64
	private byte[] uniqueIDSeed = { 0, 0, 0, 0 }; // 68
	private byte[] nextRecordListID = { 0, 0, 0, 0 }; // 72
	private byte[] numRecords = { 0, 0 }; // 76
	private List<RecordInfo> recordInfoList; // 78
	private byte[] gapToData = { 0, 0 };

	public PDBHeader(InputStream in) throws IOException {
		MobiCommon.logMessage("*** PDBHeader ***");
		StreamUtils.readByteArray(in, name);
		StreamUtils.readByteArray(in, attributes);
		StreamUtils.readByteArray(in, version);
		StreamUtils.readByteArray(in, creationDate);
		StreamUtils.readByteArray(in, modificationDate);
		StreamUtils.readByteArray(in, lastBackupDate);
		StreamUtils.readByteArray(in, modificationNumber);
		StreamUtils.readByteArray(in, appInfoID);
		StreamUtils.readByteArray(in, sortInfoID);
		StreamUtils.readByteArray(in, type);
		StreamUtils.readByteArray(in, creator);
		StreamUtils.readByteArray(in, uniqueIDSeed);
		StreamUtils.readByteArray(in, nextRecordListID);
		StreamUtils.readByteArray(in, numRecords);

		int recordCount = StreamUtils.byteArrayToInt(numRecords);
		MobiCommon.logMessage("numRecords: " + recordCount);
		recordInfoList = new LinkedList<RecordInfo>();
		for (int i = 0; i < recordCount; i++) {
			recordInfoList.add(new RecordInfo(in));
		}

		StreamUtils.readByteArray(in, gapToData);
	}
	
	public List<RecordInfo> getRecordInfos() {
		return Collections.unmodifiableList(recordInfoList);
	}

	public long getMobiHeaderSize() { // mobi header is stored in the first record
		return (recordInfoList.size() > 1) ? (recordInfoList.get(1).getRecordDataOffset() - recordInfoList.get(0).getRecordDataOffset()) : 0;
	}

	public long getOffsetAfterMobiHeader() {
		return (recordInfoList.size() > 1) ? recordInfoList.get(1).getRecordDataOffset() : 0;
	}

	public void adjustOffsetsAfterMobiHeader(int newMobiHeaderSize) {
		if (recordInfoList.size() < 2)
			return;

		int delta = (int) (newMobiHeaderSize - getMobiHeaderSize());
		int len = recordInfoList.size();
		for (int i = 1; i < len; i++) {
			RecordInfo rec = recordInfoList.get(i);
			long oldOffset = rec.getRecordDataOffset();
			rec.setRecordDataOffset(oldOffset + delta);
		}
	}

	public void write(OutputStream out) throws IOException {
		out.write(name);
		out.write(attributes);
		out.write(version);
		out.write(creationDate);
		out.write(modificationDate);
		out.write(lastBackupDate);
		out.write(modificationNumber);
		out.write(appInfoID);
		out.write(sortInfoID);
		out.write(type);
		out.write(creator);
		out.write(uniqueIDSeed);
		out.write(nextRecordListID);
		out.write(numRecords);
		for (RecordInfo rec : recordInfoList)
			rec.write(out);
		out.write(gapToData);
	}

	public String getName() {
		return StreamUtils.byteArrayToString(name);
	}

	public int getAttributes() {
		return StreamUtils.byteArrayToInt(attributes);
	}

	public int getVersion() {
		return StreamUtils.byteArrayToInt(version);
	}

	public long getCreationDate() {
		return StreamUtils.byteArrayToLong(creationDate);
	}

	public long getModificationDate() {
		return StreamUtils.byteArrayToLong(modificationDate);
	}

	public long getLastBackupDate() {
		return StreamUtils.byteArrayToLong(lastBackupDate);
	}

	public long getModificationNumber() {
		return StreamUtils.byteArrayToLong(modificationNumber);
	}

	public long getAppInfoID() {
		return StreamUtils.byteArrayToLong(appInfoID);
	}

	public long getSortInfoID() {
		return StreamUtils.byteArrayToLong(sortInfoID);
	}

	public long getType() {
		return StreamUtils.byteArrayToLong(type);
	}

	public long getCreator() {
		return StreamUtils.byteArrayToLong(creator);
	}

	public long getUniqueIDSeed() {
		return StreamUtils.byteArrayToLong(uniqueIDSeed);
	}
}
