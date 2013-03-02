package org.rr.jeborker.gui.action;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.common.swing.SwingUtils;
import org.rr.common.swing.dnd.FileTransferable;
import org.rr.common.swing.dnd.URIListTransferable;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

public class CopyToClipboardAction extends AbstractAction implements ClipboardOwner {

	CopyToClipboardAction() {
		String name = Bundle.getString("CopyToClipboardAction.name");
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("copy_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("copy_22.png"));	
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final MainController controller = MainController.getController();
		final ArrayList<String> files = new ArrayList<String>();
		final ArrayList<URI> uriList = new ArrayList<URI>();

		List<EbookPropertyItem> selectedEbookPropertyItems = controller.getSelectedEbookPropertyItems();
		if(!selectedEbookPropertyItems.isEmpty()) {
	        for (EbookPropertyItem item : selectedEbookPropertyItems) {
	    		uriList.add(new File(item.getFile()).toURI());
	    		files.add(new File(item.getFile()).getPath());
	        }
		} else {
			List<IResourceHandler> selectedTreeItems = controller.getMainTreeController().getSelectedTreeItems();
			for(IResourceHandler selectedTreeItem : selectedTreeItems) {
	    		uriList.add(selectedTreeItem.toFile().toURI());
	    		files.add(selectedTreeItem.toFile().getPath());				
			}
		}
        
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable trans;
        if(CommonUtils.isLinux()) {
        	if(ReflectionUtils.javaVersion() == 16) {
        		trans = new URIListTransferable(uriList, "copy");
        	} else {
        		trans = new FileTransferable(files);
        	}
        } else {
        	trans = new FileTransferable(files);
        }		
        c.setContents( trans, this );
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

}
