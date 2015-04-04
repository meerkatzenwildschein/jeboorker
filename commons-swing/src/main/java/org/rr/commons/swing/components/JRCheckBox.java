package org.rr.commons.swing.components;

import javax.swing.Icon;
import javax.swing.JCheckBox;

public class JRCheckBox extends JCheckBox {

	private Icon triStateIcon;

	private Icon defaultIcon;

	private boolean isTriState;

	public JRCheckBox() {
		super();
	}

	public void setShowTriStateIcon(boolean triState) {
		if (triState) {
			// third state
			this.defaultIcon = getIcon();
			if(triStateIcon != null) {
				setIcon(triStateIcon);
			}
		} else {
			//default
			if(defaultIcon == null) {
				this.defaultIcon = getIcon();
			}
			setIcon(defaultIcon);
		}
		this.isTriState = triState;
	}

	public boolean isTriState() {
		return this.isTriState;
	}

	public Icon getTriStateIcon() {
		return triStateIcon;
	}

	public void setTriStateIcon(Icon triStateIcon) {
		this.triStateIcon = triStateIcon;
	}
}
