package org.rr.commons.swing.dialogs.chooser;

public class ChooserDialogFactory {
	
	public static IFileChooser getFileChooser() {
		if(ZenityFileChooser.isSupported()) {
			return new ZenityFileChooser();
		}
		return new DefaultFileChooser();
	}
}
