package org.rr.common.swing.components;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.rr.commons.utils.ReflectionUtils;

public class JRScrollPane extends JScrollPane {

	public JRScrollPane() {
		this(null);
		initialize();
	}

	public JRScrollPane(Component viewComponent) {
		super(viewComponent);
		initialize();
	}
	
	private void initialize() {
		if(ReflectionUtils.getOS() == ReflectionUtils.OS_LINUX && System.getProperty("java.version").startsWith("1.7")) {
			//Openjdk7 bug with linux. See http://stackoverflow.com/questions/12781179/moving-jscrollpane-horizontally-results-in-blured-text 
			getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		}		
	}
}
