package org.rr.commons.utils.truezip;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.compression.truezip.TrueZipUtils;

public class TrueZipUtilsTest extends TestCase{

	public void test1() throws Exception {
		byte[] data = "INHALT_NEU".getBytes();
		File file = new File(System.getProperty("java.io.tmpdir"), "test1.cbz");
		IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(file);
		TrueZipUtils.add(resourceHandler, "eintragPÄ2.txt", new ByteArrayInputStream(data));
		
		List<String> list = TrueZipUtils.list(resourceHandler);
		System.out.println(list);

		file.delete();
	}

}
