package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;

class RefreshEntryAction extends AbstractAction {

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
		ActionUtils.refreshEntry(handler);
	}
}
