package org.rr.common.swing;


import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.JList;
import javax.swing.UIManager;

public class SwingUtils {
	/**
	 * Fetch all components from a specified type out of a Window. <BR>
	 * 
	 * @param className
	 *            Type of the component that should fetched from the given
	 *            Window. If the given {@link Window} is <code>null</code>,
	 *            all opened <code>{@link Window}</code> instanced will be searched for
	 *            a component matching to the given class name.  
	 * @param window
	 *            The Window which contains the components that should be
	 *            fetched.
	 * @return All <code>{@link Component}s</code> of the specified class type
	 *         contained by the given <code>{@link Window}</code>.
	 */
	public static Component[] getAllComponents(Class<? extends Component> className, Window window) {
		final ArrayList<Component> comps = new ArrayList<Component>();
		
		comps.addAll(getAllComponentsRecursive(window, new Class[] {className}, null));
		
		return comps.toArray(new Component[comps.size()]);
	}
	
	/**
	 * Fetch all components from a specified type out of a <code>{@link Container}</code>. <BR>
	 * 
	 * @param className
	 *            Type of the component that should fetched from the given <code>{@link Container}</code>.
	 * @param container
	 *            The <code>{@link Container}</code> which contains the components that should be
	 *            fetched. If the given {@link Container} is <code>null</code>, all 
	 *            opened forms will be searched for a component matching to the
	 *            given class name.
	 * @return All <code>{@link Component}s</code> of the specified class type
	 *         contained by the given <code>{@link Container}</code>.
	 */
	public static Component[] getAllComponents(Class<? extends Component> className, Container container) {
		Component[] result;
		
		if (container == null) {
			result =  getAllComponents(className, (Window)null);
		} else {
			result = getAllComponentsRecursive(container, new Class[] {className}, null).toArray(new Component[0]);			
		}
		return result;
	}	
	
	/**
	 * Fills up the given ArrayList with all Components, matching to the given
	 * class file, found downwards the given Container. The Container should be
	 * a JRootPane for example.
	 * 
	 * @param container
	 *            Container that sould be searched for The Component
	 * @param recursive
	 *            An empty ArrayList which will be filled up or null if a new
	 *            ArrayList should be returned.
	 * @return An array of the type specified with the className will be
	 *         returned. If className is <code>null</code> an array over
	 *         {@link Component} will be returned.
	 */
	private static ArrayList<Component> getAllComponentsRecursive(Container container, Class<? extends Component> classNames[], ArrayList<Component> recursive) {
		//is there no class name defined, just setup the default value.
		if (classNames == null || classNames.length == 0) {
			classNames = new Class[] {Component.class};
		}
		
		// No ArrayList, just create a new one.
		if (recursive == null) {
			// recursive = new Object[0];
			recursive = new ArrayList<Component>(100);
		}
		
		// No Container, nothing to do.
		if (container == null) {
			return recursive;
		}
		
		// Search for the component which is an instance of the Class specified
		// with the className parameter
		for (int i = 0; i < container.getComponentCount(); i++) {
			try {
				Component comp = container.getComponent(i);
	
				if (comp instanceof Container) {
					getAllComponentsRecursive((Container) comp, classNames, recursive);
				} 
	
				for (int j = 0; j < classNames.length; j++) {
					if (classNames[j]==null || classNames[j].isInstance(comp)) {
						recursive.add(comp);
						break;
					}
				}
			} catch (Exception e) {
				// container.getComponent(i); can fail if it was removed.
			}
		}
		return recursive;
	}

	/**
	 * Get the background color for the current UI.
	 * @return The desired background color.
	 */
	public static Color getSelectionBackgroundColor() {
		Color color = UIManager.getColor("Table.selectionBackground");
		if(color == null) {
			color = new JList().getSelectionBackground(); 
		}
		return color;
	}
	
	/**
	 * Get the foreground color for the current UI.
	 * @return The desired foreground color.
	 */
	public static Color getSelectionForegroundColor() {
		Color color = UIManager.getColor("Table.selectionForeground");
		if(color == null) {
			color = new JList().getSelectionForeground();
		}
		return color;
	}	
	
}
