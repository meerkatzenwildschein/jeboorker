package org.rr.jeborker.gui.action;

import javax.swing.Action;

public interface IFinalizeAction {

	/**
	 * Is invoked after {@link Action#actionPerformed(java.awt.event.ActionEvent)}
	 * @param count The number of the actions previously invoked. 
	 */
	public void finalizeAction(int count);
	
}
