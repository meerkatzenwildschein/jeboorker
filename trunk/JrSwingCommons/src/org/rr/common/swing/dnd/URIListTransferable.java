package org.rr.common.swing.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class URIListTransferable implements Transferable {
	
	private List<URI> uris;
	
	public URIListTransferable(List<URI> uris) {
		this.uris = uris;
	}

    public DataFlavor[] getTransferDataFlavors() {
        try {
			return new DataFlavor[] { new DataFlavor("text/uri-list") };
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
        return new DataFlavor[0];
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
   	 boolean result = flavor.getMimeType().indexOf("text/uri-list") != -1;                 	 
   	 return result;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        StringBuilder result = new StringBuilder();
        for(URI uri : uris) {
        	result.append(uri.toString()).append("\r\n");
        }
        
        return new ByteArrayInputStream(result.toString().getBytes());
    }
}
