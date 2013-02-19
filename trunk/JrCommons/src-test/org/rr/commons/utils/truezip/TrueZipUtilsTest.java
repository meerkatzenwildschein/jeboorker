package org.rr.commons.utils.truezip;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;

import junit.framework.TestCase;

public class TrueZipUtilsTest extends TestCase{

	public void test1() throws Exception {
		byte[] data = "INHALT_NEU".getBytes();
		File file = new File("/tmp/test1.cbz");
		IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(file);
		TrueZipUtils.add(resourceHandler, "eintragPÄ2.txt", new ByteArrayInputStream(data));
		
		List<String> list = TrueZipUtils.list(resourceHandler);
		System.out.println(list);
		
//		FileInputStream fileInputStream = new FileInputStream(file);
//		byte[] readFileToByteArray = FileUtils.readFileToByteArray(file);
//		list = SevenZipUtils.list(readFileToByteArray);
//		fileInputStream.close();
//		System.out.println(list);
		
		
//		file.delete();
	}
}
