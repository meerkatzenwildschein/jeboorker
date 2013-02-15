package org.rr.jeborker.gui.action;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.common.swing.SwingUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

public class PasteFromClipboardAction extends AbstractAction implements ClipboardOwner {
	    
	//source file to copy
	String source;

	PasteFromClipboardAction(String text) {
		this.source = text;
		String name = Bundle.getString("PasteFromClipboardAction.name");
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(Action.SMALL_ICON, new ImageIcon(ImageResourceBundle.getResource("paste_16.png")));
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(ImageResourceBundle.getResource("paste_22.png")));
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(hasValidClipboardContent()) {
			final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			final Transferable contents = clipboard.getContents(null);
			final boolean hasTransferableText = contents.isDataFlavorSupported(DataFlavor.stringFlavor) || contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
			if (hasTransferableText) {
				try {
					final MainController controller = MainController.getController();
					final int[] selectedEbookPropertyItemRows = controller.getSelectedEbookPropertyItemRows();
					
					if(selectedEbookPropertyItemRows.length > 0) {
						importEbookFromClipboard(contents, selectedEbookPropertyItemRows[0]);
					}
				} catch (Exception ex) {
					LoggerFactory.log(Level.WARNING, this, "Failed to import file from Clipboard", ex);
				} 
			}
		}
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}
	
	public static boolean hasValidClipboardContent() {
		final boolean mainTableFocused = MainController.getController().isMainTableFocused();
		if(!mainTableFocused) {
			return false;
		}
		
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		final Transferable contents = clipboard.getContents(null);
		if(contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			return MainController.getController().isMainTableFocused();
		} else if(contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				String data = (String) contents.getTransferData(DataFlavor.stringFlavor);
				List<File> fileList = getFileList(data);
				return !fileList.isEmpty() && MainController.getController().isMainTableFocused();
			} catch (Exception e) {
				return false;
			} 
		}

		return false;
	}

	/**
	 * Splits the given Data string into a list of files. 
	 * @param data The data to be splitted into a file list.
	 * @return The list of files. Never returns <code>null</code>.
	 */
	public static List<File> getFileList(String data) {
		ArrayList<File> result = new ArrayList<File>();
		data = data.replace("\r", "");
		List<String> splitData = ListUtils.split(data, '\n');
		for (String splitDataItem : splitData) {
			if (!StringUtils.toString(splitDataItem).trim().isEmpty()) {
				try {
					File file;
					if(splitDataItem.indexOf(":") != -1) {
						file = new File(new URI(splitDataItem));
					} else {
						file = new File(splitDataItem);
					}
					
					if (file.isFile()) {
						result.add(file);
					}
				} catch (URISyntaxException e) {
					LoggerFactory.getLogger().log(Level.INFO, "No valid file " + splitDataItem);
				}
			}
		}
		return result;
	}	
	
	/**
	 * Imports the file behind the given Transferable into JEboorker.
	 * @param t The transferable containing the data for the file import.
	 * @param row The row for the import target. 
	 */	
	@SuppressWarnings("unchecked")
	public static boolean importEbookFromClipboard(Transferable t, int dropRow) {
		EbookPropertyDBTableModel listModel = MainController.getController().getTableModel();
		
		// Get the current string under the drop.
		EbookPropertyItem value = (EbookPropertyItem) listModel.getValueAt(dropRow, 0);

		// Get the string that is being dropped.
		try {
			IResourceHandler targetRecourceDirectory = value.getResourceHandler().getParentResource();
			List<File> transferedFiles = Collections.emptyList();
			if(t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				String data = (String) t.getTransferData(DataFlavor.stringFlavor);
				transferedFiles = PasteFromClipboardAction.getFileList(data);
			} else if(t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				transferedFiles = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);                		
			}
			for(File splitDataItem : transferedFiles) {
				IResourceHandler sourceResource = ResourceHandlerFactory.getResourceHandler(splitDataItem);
				IResourceHandler targetResource = ResourceHandlerFactory.getResourceHandler(targetRecourceDirectory.toString() + "/" + sourceResource.getName());
				if(sourceResource != null && ActionUtils.isSupportedEbookFormat(sourceResource) && !targetResource.exists()) {
					sourceResource.copyTo(targetResource, false);
					EbookPropertyItem newItem = EbookPropertyItemUtils.createEbookPropertyItem(targetResource, ResourceHandlerFactory.getResourceHandler(value.getBasePath()));
					ActionUtils.addEbookPropertyItem(newItem, dropRow + 1);
				} else {
					if(!ActionUtils.isSupportedEbookFormat(sourceResource)) {
						LoggerFactory.getLogger().log(Level.INFO, "Could not drop " + splitDataItem + ". It's not a supported ebook format.");
					} else {
						LoggerFactory.getLogger().log(Level.INFO, "Could not drop " + splitDataItem);	                				
					}
				}
		}
		} 
		catch (Exception e) { return false; }

		return true;
	}	

}
