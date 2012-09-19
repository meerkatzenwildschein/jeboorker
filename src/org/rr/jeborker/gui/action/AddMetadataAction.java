package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.metadata.MetadataProperty;

class AddMetadataAction extends AbstractAction {

	private static final long serialVersionUID = 1208674185052606967L;
	
	private final MetadataProperty property;
	
	AddMetadataAction(MetadataProperty property) {
		this.property = property;
		putValue(Action.NAME, MainController.getController().getLocalizedString(property.getName()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ActionUtils.addMetadataItem(property);
	}
}
