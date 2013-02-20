package org.rr.commons.utils.rar;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.compression.CompressedDataEntry;
import org.rr.commons.utils.compression.rar.RarUtils;

public class RarUtilsTest extends TestCase {

	public void test1() {
		String contentString = "GDSSAÄÖ";
		String file = FileUtils.getTempDirectory() + File.separator + "te st.rar";
		IResourceHandler rarFileHandler = ResourceHandlerFactory.getResourceHandler(file);
		for(int i=0; i<10; i++) {
			RarUtils.add(rarFileHandler, "dir to/file "+i+".test", new ByteArrayInputStream((contentString + i).getBytes()));
		}
		
		List<String> list = RarUtils.list(rarFileHandler);
		
		CompressedDataEntry extract = RarUtils.extract(rarFileHandler, list.get(1));
		
		byte[] bytes = extract.getBytes();
		assertEquals(contentString, new String(bytes));
		
		//test end, rar no longer be needed
		try {
			rarFileHandler.delete();
		} catch (IOException e) {
		}
	}
}
