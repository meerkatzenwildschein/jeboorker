package org.rr.commons.swing.dialogs.chooser;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

/**
 * Multi platform {@link IFileChooser} implementation.
 */
public class DefaultFileChooser implements IFileChooser {

	private JFileChooser c;
	
	private DIALOG_TPYE type;
	
	private RETURN_OPTION returnValue;
	
	public DefaultFileChooser() {
		this.c = new JFileChooser();
	}
	
	public void setSelectedFile(File file) {
		c.setSelectedFile(file);
	}
	
	@Override
	public File getSelectedFile() {
		if(c.getSelectedFile() != null) {
			return new File(c.getSelectedFile().getName());
		}
		return null;
	}	

	@Override
	public void setCurrentDirectory(File dir) {
		c.setCurrentDirectory(dir);
	}
	
	@Override
	public File getCurrentDirectory() {
		return c.getCurrentDirectory();
	}	

	@Override
	public void setDialogType(DIALOG_TPYE type) {
		this.type = type;
		if(type.equals(DIALOG_TPYE.OPEN)) {
			c.setDialogType(JFileChooser.OPEN_DIALOG);
		} else if(type.equals(DIALOG_TPYE.SAVE)) {
			c.setDialogType(JFileChooser.SAVE_DIALOG);
		}
	}

	@Override
	public RETURN_OPTION showDialog(Component parent) {
		int result = -1;
		if(type == null) {
			result = c.showDialog(parent, "OK");
		} else if(type.equals(DIALOG_TPYE.OPEN)) {
			result = c.showOpenDialog(parent);
		} else if(type.equals(DIALOG_TPYE.SAVE)) {
			result = c.showSaveDialog(parent);
		} 
		
		if(result == JFileChooser.APPROVE_OPTION) {
			return this.returnValue = RETURN_OPTION.APPROVE;
		} else if(result == JFileChooser.CANCEL_OPTION) {
			return this.returnValue = RETURN_OPTION.CANCEL;
		} else {
			return this.returnValue = RETURN_OPTION.ERROR;
		}
	}

	@Override
	public RETURN_OPTION getReturnValue() {
		return this.returnValue;
	}

	@Override
	public void setTitle(String title) {
		c.setDialogTitle(title);
	}

}
