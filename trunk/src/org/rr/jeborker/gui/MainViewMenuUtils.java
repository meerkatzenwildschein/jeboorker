package org.rr.jeborker.gui;

import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.gui.action.ActionFactory;

public class MainViewMenuUtils {

	static JMenuItem createDeleteMenuItem(List<IResourceHandler> items) {
		Action action = ActionFactory.getActionForResource(ActionFactory.DYNAMIC_ACTION_TYPES.DELETE_FILE_ACTION, items);
		JMenuItem item = new JMenuItem(action);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false));
		return item;
	}
}
