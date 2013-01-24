package org.rr.commons.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link FilterInputStream} that filters bytes.  
 */
public class ByteFilterInputStream extends FilterInputStream {

	private final byte[] filterBytes;
	
	/**
	 * @param in {@link InputStream} that should be filtered.
	 * @param filterBytes The bytes which should be filtered. Each single byte in the array will match to the filter.
	 */
	public ByteFilterInputStream(final InputStream in, final byte[] filterBytes) {
		super(in);
		this.filterBytes = filterBytes;
	}

	@Override
	public int read(byte[] buf, int off, int len) throws IOException {
		int read = super.read(buf, off, len);
		int readResult = read;
		if(read > 0) {
			int o = 0;
			for(int i = 0 ; i < read; i++) {
				byte b = buf[i];
				if(isFilter(b)) {
					readResult--;
					continue;
				}
				buf[o] = buf[i];
				o++;
			}
		}
		return readResult;
	}

	private boolean isFilter(byte b) { 
		for(byte filterbyte : filterBytes) {
			if(filterbyte == b) {
				return true;
			}
		}
		return false;
	}
}
