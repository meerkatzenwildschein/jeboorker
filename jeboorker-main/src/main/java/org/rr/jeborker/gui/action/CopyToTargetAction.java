package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.AbstractAction;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.gui.MainController;

public class CopyToTargetAction extends AbstractAction {
	    
	//source file to copy
	String source;

	CopyToTargetAction(String text) {
		this.source = text;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		IResourceHandler sourceResource = ResourceHandlerFactory.getResourceHandler(source);
		IResourceHandler target = (IResourceHandler) getValue("TARGET");
        try {
        	String message = Bundle.getFormattedString("CopyToTargetAction.copy", sourceResource.getName(), StringUtil.toString(target));
        	MainController.getController().getProgressMonitor().monitorProgressStart(message);
        	
        	this.copy(sourceResource, (IResourceHandler) target);
		} catch (Exception ex) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "Copy " + this.source + " to " + StringUtil.toString(target) + " failed", ex);
		} finally {
			MainController.getController().getProgressMonitor().monitorProgressStop();
		}
	}

	private void copy(IResourceHandler source, IResourceHandler target) throws IOException {
		if(target.isDirectoryResource()) {
			target = ResourceHandlerFactory.getResourceHandler(target.toString() + File.separator + source.getName());
		}
		source.copyTo(target, false);
	}
}
