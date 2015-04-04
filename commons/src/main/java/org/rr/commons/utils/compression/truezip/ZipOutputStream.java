package org.rr.commons.utils.compression.truezip;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;

import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.fs.FsSyncException;

public class ZipOutputStream extends OutputStream {

	private de.schlichtherle.truezip.zip.ZipOutputStream zipOut;
	
	public ZipOutputStream(OutputStream out) {
		zipOut = new de.schlichtherle.truezip.zip.ZipOutputStream(out);
	}
	
    /**
     * Constructs a ZIP output stream which decorates the given output stream
     * using the given charset.
     *
     * @param  out The output stream to write the ZIP file to.
     * @param  charset the character set to use.
     */
    public ZipOutputStream(OutputStream out, Charset charset) {
    	zipOut = new de.schlichtherle.truezip.zip.ZipOutputStream(out, charset);
    }
    
    @Override
    public void close() throws IOException {
    	zipOut.flush();
    	zipOut.close();
		try {
			TVFS.umount();//commit changes
		} catch (FsSyncException e) {
			LoggerFactory.getLogger().log(Level.WARNING, "Failed to unmount TVFS", e);
		} 
    }

	public void putNextEntry(ZipEntry zipEntry) throws IOException {
		de.schlichtherle.truezip.zip.ZipEntry zipEntry2 = zipEntry.getZipEntry();
		if(zipEntry2.getMethod() == ZipEntry.STORED) {
			zipEntry2.setCompressedSize(zipEntry2.getSize());
		}
		zipOut.putNextEntry(zipEntry2);
	}

	@Override
	public void write(int b) throws IOException {
		zipOut.write(b);
	}
}
