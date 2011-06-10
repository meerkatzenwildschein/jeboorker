package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;

public class RefreshEntryAction extends AbstractAction {

	private static final long serialVersionUID = -8907068823573668230L;
	
	String text;
	
	public RefreshEntryAction(String text) {
		this.text = text;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceLoader(text);
		final DefaultDBManager defaultDBManager = DefaultDBManager.getInstance();
		final Iterable<EbookPropertyItem> items = defaultDBManager.getObject(EbookPropertyItem.class, "file", text);
		Iterator<EbookPropertyItem> iterator = items.iterator();
		if(iterator.hasNext()) {
			EbookPropertyItem item = iterator.next();
			RefreshBasePathAction.refreshEbookPropertyItem(item, resourceLoader);
		}
	}

}
