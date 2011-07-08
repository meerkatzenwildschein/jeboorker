package org.rr.jeborker.gui.action;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.rr.commons.utils.CommonUtils;

public class SetLookAndFeelAction extends AbstractAction {

	private static final long serialVersionUID = -2884898180881622573L;
	
	private String lookAndFeelName;
	
	SetLookAndFeelAction(String lookAndFeelName) {
		this.lookAndFeelName = lookAndFeelName;
		putValue(Action.NAME, lookAndFeelName);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if(CommonUtils.compareTo(lookAndFeelName, info.getName()) == 0) {
				try {
					setLookAndFeel(info);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				break;
			}
		}
	}

	private static void setLookAndFeel(LookAndFeelInfo info) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(info.getClassName());
		
		Window[] windows = Window.getWindows();
		for (Window window : windows) {
			for(int i=0; i < window.getComponentCount(); i++) {
				Component component = window.getComponent(i);
				SwingUtilities.updateComponentTreeUI(component);
			}
		}
	}

}
