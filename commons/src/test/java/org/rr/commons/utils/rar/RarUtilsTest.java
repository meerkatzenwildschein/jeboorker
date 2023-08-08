package org.rr.commons.utils.rar;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.compression.CompressedDataEntry;
import org.rr.commons.utils.compression.rar.RarUtils;

public class RarUtilsTest extends TestCase {

	static {
		//Setup the location for the rar executables.
		if(ReflectionUtils.getOS() == ReflectionUtils.OS_WINDOWS) {
			RarUtils.setRarExecFolder(System.getProperties().getProperty("user.dir") + "/../dist/win32/");
		} else {
			RarUtils.setRarExecFolder("/usr/bin");
		}		
	}
	
	public void test1() {
		String contentString = "GDSSAÄÖ";
		String file = FileUtils.getTempDirectory() + File.separator + "te st.rar";
		IResourceHandler rarFileHandler = ResourceHandlerFactory.getResourceHandler(file);
		for(int i=0; i<10; i++) {
			RarUtils.add(rarFileHandler, "dir to/file "+i+".jpg", new ByteArrayInputStream((contentString + i).getBytes()));
		}
		
		List<String> list = RarUtils.list(rarFileHandler);
		
		CompressedDataEntry extract = RarUtils.extract(rarFileHandler, list.get(1));
		
		byte[] bytes = extract.getBytes();
		assertEquals(contentString + "0", new String(bytes));
		
		//test end, rar no longer be needed
		try {
			rarFileHandler.delete();
		} catch (IOException e) {
		}
	}
}
