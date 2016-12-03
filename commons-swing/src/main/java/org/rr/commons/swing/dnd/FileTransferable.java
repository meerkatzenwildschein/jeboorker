package org.rr.commons.swing.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.LinkedList;
import java.util.List;

public class FileTransferable implements Transferable {
	private List<String> fileList;

	public FileTransferable(List<String> files) {
		fileList = new LinkedList<>(files);
	}

	// Returns an object which represents the data to be transferred.
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (flavor.equals(DataFlavor.javaFileListFlavor)) {
			return fileList;
		}

		throw new UnsupportedFlavorException(flavor);
	}

	// Returns an array of DataFlavor objects indicating the flavors
	// the data can be provided in.
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.javaFileListFlavor };
	}

	// Returns whether or not the specified data flavor is supported for this object.
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(DataFlavor.javaFileListFlavor);
	}

}
