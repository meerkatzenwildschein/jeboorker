package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.common.swing.SwingUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class RefreshEntryAction extends AbstractAction {

	private static final long serialVersionUID = -8907068823573668230L;
	
	final IResourceHandler handler;
	
	public RefreshEntryAction(final String file) {
		this(ResourceHandlerFactory.getResourceLoader(file));
	}
	
	public RefreshEntryAction(final IResourceHandler handler) {
		this.handler = handler;
		String name = Bundle.getString("RefreshEntryAction.name");
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(Action.SMALL_ICON, new ImageIcon(ImageResourceBundle.getResource("refresh_16.png")));
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(ImageResourceBundle.getResource("refresh_22.png")));
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ActionUtils.refreshEntry(handler);
	}
}
