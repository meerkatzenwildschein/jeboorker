package org.rr.commons.utils.rar;

import java.io.ByteArrayInputStream;
import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;

public class RarUtilsTest extends TestCase {

	public void test1() {
		String file = FileUtils.getTempDirectory() + File.separator + "test.rar";
		IResourceHandler rarFileHandler = ResourceHandlerFactory.getResourceHandler(file);
		for(int i=0; i<10; i++) {
			RarUtils.add(rarFileHandler, "dir to/file "+i+".test", new ByteArrayInputStream(("BLÖDER_INHALT" + i).getBytes()));
		}
		
//		List<String> list = RarUtils.list(rarFileHandler);
//		System.out.println(list);
	}
}
