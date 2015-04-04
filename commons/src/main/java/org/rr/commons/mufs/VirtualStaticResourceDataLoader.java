package org.rr.commons.mufs;

import java.io.InputStream;

public interface VirtualStaticResourceDataLoader {

	public InputStream getContentInputStream();
	
	public long length();
}
