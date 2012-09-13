package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;

public class RefreshEntryAction extends AbstractAction {

	private static final long serialVersionUID = -8907068823573668230L;
	
	final IResourceHandler handler;
	
	public RefreshEntryAction(final String file) {
		this(ResourceHandlerFactory.getResourceLoader(file));
	}
	
	public RefreshEntryAction(final IResourceHandler handler) {
		this.handler = handler;
		putValue(Action.NAME, Bundle.getString("RefreshEntryAction.name"));
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("refresh_16.gif")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		refreshEntry(handler);
	}
	
	/**
	 * Refreshes the entries for the given handler.
	 * @param handler The handler of the entry to be refreshed.
	 */
	public static void refreshEntry(IResourceHandler handler) {	
		final DefaultDBManager defaultDBManager = DefaultDBManager.getInstance();
		final Iterable<EbookPropertyItem> items = defaultDBManager.getObject(EbookPropertyItem.class, "file", handler.toString());
		Iterator<EbookPropertyItem> iterator = items.iterator();
		if(iterator.hasNext()) {
			EbookPropertyItem item = iterator.next();
			RefreshBasePathAction.refreshEbookPropertyItem(item, handler);
		}		
	}

}
