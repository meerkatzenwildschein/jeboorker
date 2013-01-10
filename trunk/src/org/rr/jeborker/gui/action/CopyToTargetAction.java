package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.gui.MainController;

public class CopyToTargetAction extends AbstractAction {
	    
	//source file to copy
	String source;

	CopyToTargetAction(String text) {
		this.source = text;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		IResourceHandler sourceResource = ResourceHandlerFactory.getResourceLoader(source);
		Object target = getValue(Action.NAME);
        try {
        	String message = Bundle.getFormattedString("CopyToTargetAction.copy", sourceResource.getName(), StringUtils.toString(target));
        	MainController.getController().getProgressMonitor().monitorProgressStart(message);
        	if(target instanceof IResourceHandler) {
        		this.copy(sourceResource, (IResourceHandler) target);
        	} else {
        		IResourceHandler targetResource = ResourceHandlerFactory.getResourceLoader(StringUtils.toString(target));
        		this.copy(sourceResource, targetResource);
        	}
		} catch (Exception ex) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "Copy " + this.source + " to " + StringUtils.toString(target) + " failed", ex);
		} finally {
			MainController.getController().getProgressMonitor().monitorProgressStop();
		}
	}

	private void copy(IResourceHandler source, IResourceHandler target) throws IOException {
		if(target.isDirectoryResource()) {
			target = ResourceHandlerFactory.getResourceLoader(target.toString() + File.separator + source.getName());
		}
		source.copyTo(target, false);
	}
}
