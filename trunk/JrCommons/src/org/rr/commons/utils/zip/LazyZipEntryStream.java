package org.rr.commons.utils.zip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.zip.ZipUtils.ZipDataEntry;

public class LazyZipEntryStream extends InputStream {

	private IResourceHandler zipResource;
	private String entry;
	private InputStream data;
	
	public LazyZipEntryStream(IResourceHandler zipResource, String entry) {
		this.zipResource = zipResource;
		this.entry = entry;
	}
	
	private void load() throws IOException {
		if(data == null) {
			InputStream contentInputStream = null;
			try {
				contentInputStream = zipResource.getContentInputStream();
				ZipDataEntry extract = ZipUtils.extract(contentInputStream, entry);
				data = new ByteArrayInputStream(extract.getBytes());
			} finally {
				if(contentInputStream != null) {
					IOUtils.closeQuietly(contentInputStream);
				}
			}
		}		
	}
	
	@Override
	public int read() throws IOException {
		load();
		return data.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		load();
		return data.read(b, off, len);
	}

	@Override
	public int available() throws IOException {
		load();
		return data.available();
	}

	@Override
	public void close() throws IOException {
		if(data != null) {
			data.close();
			data = null;
		}
	}

}
