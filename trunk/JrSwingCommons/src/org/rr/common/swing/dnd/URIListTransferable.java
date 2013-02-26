package org.rr.common.swing.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.rr.commons.utils.StringUtils;

public class URIListTransferable implements Transferable {
	
	public static DataFlavor uriListDataFlavor = null;
	
	public static DataFlavor gnomeCopiedFilesDataFlavor = null;
	
	private static DataFlavor[] allSupportedDataFlavors = null;
	
	static {
		try {
			uriListDataFlavor = new DataFlavor("text/uri-list");
			gnomeCopiedFilesDataFlavor = new DataFlavor("x-special/gnome-copied-files");
			allSupportedDataFlavors = new DataFlavor[] { uriListDataFlavor, gnomeCopiedFilesDataFlavor };
		} catch (ClassNotFoundException e) {
		}
	}
	
	private List<URI> uris;
	
	private String action;
	
	private static String LINE_BREAK = "\n";
	
	public URIListTransferable(List<URI> uris, String action) {
		this.uris = uris;
		this.action = action;
	}

    public DataFlavor[] getTransferDataFlavors() {
		return allSupportedDataFlavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
      	 boolean result = flavor.getMimeType().indexOf("x-special/gnome-copied-files") != -1 ||
        			flavor.getMimeType().indexOf("text/uri-list") != -1;                 	 
        	 return result;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        StringBuilder buffer = new StringBuilder();
        for(URI uri : uris) {
        	String uriString = uri.toString();
        	uriString = StringUtils.replace(uriString, "file:", "file://");
        	buffer.append(uriString).append(LINE_BREAK);
        }
        String transferString = buffer.toString();
        if(transferString.endsWith(LINE_BREAK)) {
        	transferString = transferString.substring(0, transferString.length() - LINE_BREAK.length());
        }
        
        String addAction = addAction(transferString);
        return new ByteArrayInputStream(addAction.getBytes());
    }
    
    private String addAction(String transferString) {
    	if(action != null && action.length() > 0) {
    		return "copy\n" + transferString;
    	}
    	return transferString;
    }
}
