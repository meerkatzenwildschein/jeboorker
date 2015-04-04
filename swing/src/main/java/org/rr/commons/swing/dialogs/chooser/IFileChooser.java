package org.rr.commons.swing.dialogs.chooser;

import java.awt.Component;
import java.io.File;

public interface IFileChooser {
	
	public static enum DIALOG_TPYE {
		SAVE, OPEN
	}	
	
	public static enum RETURN_OPTION {
		APPROVE, CANCEL, ERROR
	}	

	public void setSelectedFile(File file);
	
	public File getSelectedFile();
	
	public void setCurrentDirectory(File dir);
	
	public File getCurrentDirectory();
	
	public void setDialogType(DIALOG_TPYE type);
	
	public RETURN_OPTION showDialog(Component parent);

	public RETURN_OPTION getReturnValue();
	
	public void setTitle(String title);
}
