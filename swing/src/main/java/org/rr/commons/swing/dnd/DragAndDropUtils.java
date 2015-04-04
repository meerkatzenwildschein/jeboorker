package org.rr.commons.swing.dnd;

import java.awt.datatransfer.DataFlavor;

import javax.swing.JTree;
import javax.swing.TransferHandler;

public class DragAndDropUtils {
	
	public static boolean isFileImportRequest(TransferHandler.TransferSupport info) {
        if (!(info.isDataFlavorSupported(DataFlavor.stringFlavor) || info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
        		|| info.isDataFlavorSupported(URIListTransferable.uriListDataFlavor) || info.isDataFlavorSupported(URIListTransferable.gnomeCopiedFilesDataFlavor) )) {
            return false;
        }
        JTree.DropLocation dl = (JTree.DropLocation) info.getDropLocation();
        if (dl.getPath() != null && dl.getPath().getPathCount() <= 1) {
            return false;
        }
        
        return true;
	}
}
