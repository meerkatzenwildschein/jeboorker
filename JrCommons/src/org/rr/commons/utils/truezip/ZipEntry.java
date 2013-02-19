package org.rr.commons.utils.truezip;

public class ZipEntry  {

    /**
     * Method for <em>Stored</em> (uncompressed) entries.
     *
     * @see   #setMethod(int)
     */
    public static final int STORED = 0;

    /**
     * Method for <em>Deflated</em> compressed entries.
     *
     * @see   #setMethod(int)
     */
    public static final int DEFLATED = 8;

    /**
     * Method for <em>BZIP2</em> compressed entries.
     *
     * @see   #setMethod(int)
     * @since TrueZIP 7.3
     */
    public static final int BZIP2 = 12;

    /**
     * Pseudo compression method for WinZip AES encrypted entries.
     *
     * @since TrueZIP 7.3
     */
    static final int WINZIP_AES = 99;
    
    private de.schlichtherle.truezip.zip.ZipEntry zipEntry;

	public ZipEntry(String name) {
		zipEntry = new de.schlichtherle.truezip.zip.ZipEntry(name);
	}
	
	de.schlichtherle.truezip.zip.ZipEntry getZipEntry() {
		return zipEntry;
	}

	public void setMethod(int method) {
		zipEntry.setMethod(method);
	}

	public void setSize(int size) {
		zipEntry.setSize(size);
	}

	public void setCrc(long crc) {
		zipEntry.setCrc(crc);
	}

}
