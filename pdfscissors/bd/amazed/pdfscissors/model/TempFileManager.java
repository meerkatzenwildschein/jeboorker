package bd.amazed.pdfscissors.model;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 * Use this class to create Temp file, instead of calling {@link File#createTempFile(String, String)}. Looks like there
 * are some bugs to clean up temp files. See http://www.devx.com/java/Article/22018/0/page/2. So we will do some
 * housekeeping in this class.
 *
 * @author Amazed
 *
 */
public class TempFileManager {

	private static TempFileManager instance;

	private Vector<File> tempFiles = new Vector<File>();

	private TempFileManager() {

	}

	public static TempFileManager getInstance() {
		if (instance == null) {
			instance = new TempFileManager();
		}
		return instance;
	}

	public File createTempFile(String prefix, String suffix, boolean shouldDeleteOnExit) throws IOException {
		File tempFile = File.createTempFile(prefix, suffix);
		delete(tempFile);
		if (shouldDeleteOnExit) {
			tempFile.deleteOnExit();
			tempFiles.add(tempFile);
		}
		return tempFile;
	}

	private void delete(File file) {
		if (file.exists()) {
			boolean deleted = file.delete();
			if (!deleted) {
				System.err.println("Failed to delete " + file);
			}
		}
	}

	public void clean() {
		File file = null;
		for (int i = tempFiles.size() - 1; i >= 0; i--) {
			file = tempFiles.elementAt(i);
		}
		tempFiles.removeAllElements();
	}

}
