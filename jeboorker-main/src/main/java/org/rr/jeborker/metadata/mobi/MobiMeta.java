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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class MobiMeta {
	
	private static final byte[] INDX = "INDX".getBytes();
	private PDBHeader pdbHeader;
	private MobiHeader mobiHeader;
	private List<EXTHRecord> exthRecords;
	private byte[] inputBytes;
	
	public MobiMeta readMetaData(File inputFile) throws MobiMetaException {
		try {
			inputBytes = FileUtils.readFileToByteArray(inputFile);

			InputStream in = new ByteArrayInputStream(inputBytes);
			pdbHeader = new PDBHeader(in);
			mobiHeader = new MobiHeader(in, inputBytes, pdbHeader.getMobiHeaderSize());
			exthRecords = mobiHeader.getEXTHRecords();
		} catch(IOException e) {
			throw new MobiMetaException("Could not parse mobi file " + inputFile.getAbsolutePath() + ": " + e.getMessage());
		}
		return this;
	}

	public void saveToNewFile(File outputFile) throws MobiMetaException {
		saveToNewFile(outputFile, true);
	}

	public void saveToNewFile(File outputFile, boolean packHeader) throws MobiMetaException {
		long readOffset = pdbHeader.getOffsetAfterMobiHeader();

		if (!MobiCommon.safeMode && packHeader) {
			mobiHeader.pack();
			pdbHeader.adjustOffsetsAfterMobiHeader(mobiHeader.size());
		}

		InputStream in = null;
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outputFile);
			pdbHeader.write(out);
			mobiHeader.write(out);

			int bytesRead;
			byte[] buffer = new byte[4096];
			in = new ByteArrayInputStream(inputBytes);
			in.skip(readOffset);
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			throw new MobiMetaException("Problems encountered while writing to " + outputFile.getAbsolutePath() + ": " + e.getMessage());
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
				}
		}
	}

	public String getCharacterEncoding() {
		return mobiHeader.getCharacterEncoding();
	}

	public String getFullName() {
		return mobiHeader.getFullName();
	}

	public void setFullName(String s) {
		mobiHeader.setFullName(s);
	}

	public List<EXTHRecord> getEXTHRecords() {
		return exthRecords;
	}

	public void setEXTHRecords() {
		mobiHeader.setEXTHRecords(exthRecords);
	}

	public int getLocale() {
		return mobiHeader.getLocale();
	}

	public int getDictInput() {
		return mobiHeader.getInputLanguage();
	}

	public int getDictOutput() {
		return mobiHeader.getOutputLanguage();
	}

	public void setLanguages(int locale, int dictInput, int dictOutput) {
		mobiHeader.setLocale(locale);
		mobiHeader.setInputLanguage(dictInput);
		mobiHeader.setOutputLanguage(dictOutput);
	}

	public String getMetaInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("PDB Header\n");
		sb.append("----------\n");
		sb.append("Name: ");
		sb.append(pdbHeader.getName());
		sb.append("\n");
		String[] attributes = getPDBHeaderAttributes();
		if (attributes.length > 0) {
			sb.append("Attributes: ");
			for (int i = 0; i < attributes.length; i++) {
				if (i > 0)
					sb.append(", ");
				sb.append(attributes[i]);
			}
			sb.append("\n");
		}
		sb.append("Version: ");
		sb.append(pdbHeader.getVersion());
		sb.append("\n");
		sb.append("Creation Date: ");
		sb.append(pdbHeader.getCreationDate());
		sb.append("\n");
		sb.append("Modification Date: ");
		sb.append(pdbHeader.getModificationDate());
		sb.append("\n");
		sb.append("Last Backup Date: ");
		sb.append(pdbHeader.getLastBackupDate());
		sb.append("\n");
		sb.append("Modification Number: ");
		sb.append(pdbHeader.getModificationNumber());
		sb.append("\n");
		sb.append("App Info ID: ");
		sb.append(pdbHeader.getAppInfoID());
		sb.append("\n");
		sb.append("Sort Info ID: ");
		sb.append(pdbHeader.getSortInfoID());
		sb.append("\n");
		sb.append("Type: ");
		sb.append(pdbHeader.getType());
		sb.append("\n");
		sb.append("Creator: ");
		sb.append(pdbHeader.getCreator());
		sb.append("\n");
		sb.append("Unique ID Seed: ");
		sb.append(pdbHeader.getUniqueIDSeed());
		sb.append("\n\n");

		sb.append("PalmDOC Header\n");
		sb.append("--------------\n");
		sb.append("Compression: ");
		sb.append(mobiHeader.getCompression());
		sb.append("\n");
		sb.append("Text Length: ");
		sb.append(mobiHeader.getTextLength());
		sb.append("\n");
		sb.append("Record Count: ");
		sb.append(mobiHeader.getRecordCount());
		sb.append("\n");
		sb.append("Record Size: ");
		sb.append(mobiHeader.getRecordSize());
		sb.append("\n");
		sb.append("Encryption Type: ");
		sb.append(mobiHeader.getEncryptionType());
		sb.append("\n\n");

		sb.append("MOBI Header\n");
		sb.append("-----------\n");
		sb.append("Header Length: ");
		sb.append(mobiHeader.getHeaderLength());
		sb.append("\n");
		sb.append("Mobi Type: ");
		sb.append(mobiHeader.getMobiType());
		sb.append("\n");
		sb.append("Unique ID: ");
		sb.append(mobiHeader.getUniqueID());
		sb.append("\n");
		sb.append("File Version: ");
		sb.append(mobiHeader.getFileVersion());
		sb.append("\n");
		sb.append("Orthographic Index: ");
		sb.append(mobiHeader.getOrthographicIndex());
		sb.append("\n");
		sb.append("Inflection Index: ");
		sb.append(mobiHeader.getInflectionIndex());
		sb.append("\n");
		sb.append("Index Names: ");
		sb.append(mobiHeader.getIndexNames());
		sb.append("\n");
		sb.append("Index Keys: ");
		sb.append(mobiHeader.getIndexKeys());
		sb.append("\n");
		sb.append("Extra Index 0: ");
		sb.append(mobiHeader.getExtraIndex0());
		sb.append("\n");
		sb.append("Extra Index 1: ");
		sb.append(mobiHeader.getExtraIndex1());
		sb.append("\n");
		sb.append("Extra Index 2: ");
		sb.append(mobiHeader.getExtraIndex2());
		sb.append("\n");
		sb.append("Extra Index 3: ");
		sb.append(mobiHeader.getExtraIndex3());
		sb.append("\n");
		sb.append("Extra Index 4: ");
		sb.append(mobiHeader.getExtraIndex4());
		sb.append("\n");
		sb.append("Extra Index 5: ");
		sb.append(mobiHeader.getExtraIndex5());
		sb.append("\n");
		sb.append("First Non-Book Index: ");
		sb.append(mobiHeader.getFirstNonBookIndex());
		sb.append("\n");
		sb.append("Full Name Offset: ");
		sb.append(mobiHeader.getFullNameOffset());
		sb.append("\n");
		sb.append("Full Name Length: ");
		sb.append(mobiHeader.getFullNameLength());
		sb.append("\n");
		sb.append("Min Version: ");
		sb.append(mobiHeader.getMinVersion());
		sb.append("\n");
		sb.append("Huffman Record Offset: ");
		sb.append(mobiHeader.getHuffmanRecordOffset());
		sb.append("\n");
		sb.append("Huffman Record Count: ");
		sb.append(mobiHeader.getHuffmanRecordCount());
		sb.append("\n");
		sb.append("Huffman Table Offset: ");
		sb.append(mobiHeader.getHuffmanTableOffset());
		sb.append("\n");
		sb.append("Huffman Table Length: ");
		sb.append(mobiHeader.getHuffmanTableLength());
		sb.append("\n");

		return sb.toString();
	}

	private String[] getPDBHeaderAttributes() {
		LinkedList<String> list = new LinkedList<String>();
		int attr = pdbHeader.getAttributes();
		if ((attr & 0x02) != 0)
			list.add("Read-Only");
		if ((attr & 0x04) != 0)
			list.add("Dirty AppInfoArea");
		if ((attr & 0x08) != 0)
			list.add("Backup This Database");
		if ((attr & 0x10) != 0)
			list.add("OK To Install Newer Over Existing Copy");
		if ((attr & 0x20) != 0)
			list.add("Force The PalmPilot To Reset After This Database Is Installed");
		if ((attr & 0x40) != 0)
			list.add("Don't Allow Copy Of File To Be Beamed To Other Pilot");

		String[] ret = new String[list.size()];
		int index = 0;
		for (String s : list)
			ret[index++] = s;

		return ret;
	}
	
	public byte[] getCoverOrThumb() {
		byte[] imgNumber = null;
		EXTHRecord exthRecord;
		if ((exthRecord = mobiHeader.getEXTHRecord(201)) != null) {
			imgNumber = exthRecord.getData();
		} else if ((exthRecord = mobiHeader.getEXTHRecord(202)) != null) {
			imgNumber = exthRecord.getData();
		}

		if (imgNumber != null) {
			int index = StreamUtils.byteArrayToInt(imgNumber);
			return getRecordByIndex(index + mobiHeader.getFirstImageIndex());
		} else {
			for (int i = mobiHeader.getFirstImageIndex(); i < mobiHeader.getLastContentIndex(); i++) {
				byte[] img = getRecordByIndex(i);
				if ((img[0] & 0xff) == 0xFF && (img[1] & 0xff) == 0xD8) {
					return img;
				}
			}
		}
		return null;
	}
	
  public String getTextContent() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		int firstContentIndex = mobiHeader.getFirstContentIndex() == 0 ? 1 : mobiHeader.getFirstContentIndex();
		
		for (int i = firstContentIndex; i < mobiHeader.getLastContentIndex() - 1 && i < mobiHeader.getFirstImageIndex(); i++) {
			int start = (int) pdbHeader.getRecordInfos().get(i).getRecordDataOffset();
			int end = (int) pdbHeader.getRecordInfos().get(i + 1).getRecordDataOffset();
			byte[] coded = Arrays.copyOfRange(inputBytes, start, end);

			byte[] decoded = null;
			if (mobiHeader.getCompressionCode() == 2) { // PalmDOC
				decoded = lz77(coded);
			} else if (mobiHeader.getCompressionCode() == 1) { // None
				decoded = coded;
			} else if (mobiHeader.getCompressionCode() == 17480) { // HUFF/CDIC
				try {
					decoded = coded;
				} catch (Exception e) {
					e.printStackTrace();
					decoded = ("error").getBytes();
				}
			} else {
				decoded = ("Compression not supported " + mobiHeader.getCompressionCode()).getBytes();
			}

			byte[] header = Arrays.copyOfRange(decoded, 0, 4);
			if (Arrays.equals(INDX, header)) {
				continue;
			}

			for (int n = 0; n < decoded.length; n++) {
				if (decoded[n] != 0x00) {
					outputStream.write(decoded[n]);
				}

			}
		}
		try {
			return outputStream.toString(getCharacterEncoding());
		} catch (UnsupportedEncodingException e) {
			return outputStream.toString();
		}
	}

	private static byte[] lz77(byte[] bytes) {
		ByteArrayBuffer out = new ByteArrayBuffer(bytes.length);
		try {
			int i = 0;
			while (i < bytes.length - 4) { // try -2,4,8,10
				int b = bytes[i++] & 0x00FF;
				try {
					if (b == 0x0) {
						out.write(b);
					} else if (b <= 0x08) {
						for (int j = 0; j < b; j++) {
							out.write(bytes[i + j]);
						}
						i += b;
					}
		
					else if (b <= 0x7f) {
						out.write(b);
					} else if (b <= 0xbf) {
						b = b << 8 | bytes[i++] & 0xFF;
						int length = (b & 0x0007) + 3;
						int location = (b >> 3) & 0x7FF;
		
						for (int j = 0; j < length; j++) {
							out.write(out.getRawData()[out.size() - location]);
						}
					} else {
						out.write(' ');
						out.write(b ^ 0x80);
					}
				} catch(Exception e) {
					
				}
			}
			return out.getRawData();
		} finally {
			IOUtils.closeQuietly(out);
		}
	}
	
	private byte[] getRecordByIndex(int index) {
		List<RecordInfo> recordInfos = pdbHeader.getRecordInfos();
		if (index >= recordInfos.size()) {
			return null;
		}
		RecordInfo recordInfo = recordInfos.get(index);
		long from = recordInfo.getRecordDataOffset();
		long to = inputBytes.length;
		
		if (index + 1 < recordInfos.size()) {
			to = recordInfos.get(index + 1).getRecordDataOffset();
		}

		return Arrays.copyOfRange(inputBytes, (int) from, (int) to);
	}

}
