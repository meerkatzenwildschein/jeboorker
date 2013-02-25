package org.rr.common.swing.dnd;

import java.awt.datatransfer.DataFlavor;

import javax.swing.JTree;
import javax.swing.TransferHandler;

public class DragAndDropUtils {
	
	public static boolean isFileImportRequest(TransferHandler.TransferSupport info) {
		//only import Strings
        if (!(info.isDataFlavorSupported(DataFlavor.stringFlavor) || info.isDataFlavorSupported(DataFlavor.javaFileListFlavor))) {
            return false;
        }

        JTree.DropLocation dl = (JTree.DropLocation) info.getDropLocation();
        if (dl.getPath() != null && dl.getPath().getPathCount() <= 1) {
            return false;
        }
        
        return true;
	}
}
