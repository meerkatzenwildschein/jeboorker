package org.rr.commons.swing;


import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.rr.commons.utils.MathUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.utils.UtilConstants;

public class SwingUtils {
	private static JLabel defaultLabel = new JLabel(); 
	
	private static Color selectionForegroundColor;
	private static Color selectionBackgroundColor;
	private static Color foregroundColor;
	private static Color backgroundColor;
	private static Color stripeBackgroundColor;
	
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
	 *            Container that should be searched for The Component
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
		if(selectionBackgroundColor != null) {
			return selectionBackgroundColor;
		}
		
		selectionBackgroundColor = UIManager.getColor("Table.selectionBackground");
		if(selectionBackgroundColor == null) {
			selectionBackgroundColor = UIManager.getColor("Table[Enabled+Selected].textBackground");
			if(selectionBackgroundColor == null) {
				selectionBackgroundColor = new JList().getSelectionBackground();
			}
			
			//sometimes the UIManager color won't work 
			selectionBackgroundColor = new Color(selectionBackgroundColor.getRed(), selectionBackgroundColor.getGreen(), selectionBackgroundColor.getBlue());
		}
		
		//sometimes the UIManager color won't work 
		return selectionBackgroundColor;
	}	
	
	/**
	 * Get the foreground color for the current UI.
	 * @return The desired foreground color.
	 */
	public static Color getSelectionForegroundColor() {
		if(selectionForegroundColor != null) {
			return selectionForegroundColor;
		}
		
		//for windows
		selectionForegroundColor = (Color) Toolkit.getDefaultToolkit().getDesktopProperty("win.item.highlightTextColor");
		if(selectionForegroundColor == null) {
			selectionForegroundColor = UIManager.getColor("Table.selectionForeground");
			if(selectionForegroundColor == null) {
				selectionForegroundColor = UIManager.getColor("Table[Enabled+Selected].textForeground");
				if(selectionForegroundColor == null) {
					selectionForegroundColor = new JList().getSelectionForeground();
				}
			}
			
			//sometimes the UIManager color won't work 
			selectionForegroundColor = new Color(selectionForegroundColor.getRed(), selectionForegroundColor.getGreen(), selectionForegroundColor.getBlue());
		}
		
		return selectionForegroundColor;
	}	
	
	/**
	 * Creates a new color instance which is brighter. 
	 * @param color The base color to be made brighter.
	 * @param brighter The rgb value by which the given base color should be made brighter.
	 * @return The brighter color.
	 */
	public static Color getBrighterColor(Color color, int brighter) {
		Color c = new Color(MathUtils.range(color.getRed() + brighter, 0, 255) , MathUtils.range(color.getGreen() + brighter, 0, 255), MathUtils.range(color.getBlue() + brighter, 0, 255));
		return c;
	}
	
	public static Color getForegroundColor() {
		if(foregroundColor != null) {
			return foregroundColor;
		}
		
		foregroundColor = UIManager.getColor("Table.foreground");
		if(foregroundColor == null) {
			foregroundColor = new JList().getForeground();
		}
		
		//sometimes the UIManager color won't work 
		foregroundColor = new Color(foregroundColor.getRed(), foregroundColor.getGreen(), foregroundColor.getBlue());
		
		//sometimes the UIManager color won't work 
		return foregroundColor;
	}
	
	public static Color getBackgroundColor() {
		if(backgroundColor != null) {
			return backgroundColor;
		}
		
		backgroundColor = UIManager.getColor("Table.background");
		if(backgroundColor == null) {
			backgroundColor = new JList().getBackground();
		}
		//sometimes the UIManager color won't work
		backgroundColor = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue());
		
		return backgroundColor;
	}	
	
	public static Color getStripeBackgroundColor() {
		if(stripeBackgroundColor != null) {
			return stripeBackgroundColor;
		}
		
		stripeBackgroundColor = getBackgroundColor();
		
		//sometimes the UIManager color won't work
		int addDarkness = stripeBackgroundColor.getRed() > 128 ? 10 : -1;
		stripeBackgroundColor = new Color(stripeBackgroundColor.getRed() - addDarkness, stripeBackgroundColor.getGreen() - addDarkness, stripeBackgroundColor.getBlue() - addDarkness);
		return stripeBackgroundColor;
	}	
	
	/**
	 * Reset the stored fore- and background colors for
	 * {@link #getBackgroundColor()}, {@link #getForegroundColor()} {@link #getSelectionBackgroundColor()}
	 * and {@link #getSelectionForegroundColor()}.
	 */
	public static void resetColors() {
		backgroundColor = null;
		foregroundColor = null;
		selectionBackgroundColor = null;
		selectionForegroundColor = null;
		stripeBackgroundColor = null;
	}
	
	public static void centerOnScreen(Window window) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension size = window.getSize();
		window.setLocation((screenSize.width - size.width) / 2, (screenSize.height - size.height) / 2);
	}

	/**
	 * Sets the given action so it's invoked if the user hits the escape key. 
	 * @param dialog The dialog to attach the escape key.
	 * @param abortAction The action that is invoked if the escape key is pressed.
	 */
	public static void setEscapeWindowAction(JDialog dialog, ActionListener abortAction) {
		((JComponent)dialog.getContentPane()).registerKeyboardAction(abortAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
	
	/**
	 * Get the mnemonic from the given text. The mnemonic is marked by a leading & character.
	 * @param text The mnemonic search text. 
	 * @return The mnemonic or -1 if no mnemonic could be found.
	 */
	public static int getMnemonicKeyCode(String text) {
		for(int i = 0 ; i < text.length(); i++) {
			char c = text.charAt(i);
			if(c == '&') {
				try {
					return (int) text.charAt(i + 1);
				} catch(IndexOutOfBoundsException e) {
					return -1;
				}
			}
		}
		return -1;
	}
	
	/**
	 * Removes the mnemonic marker from the given string and returns the result.
	 * @param text The text where the marker should be removed from.
	 * @return The text where the first mnemonic char is removed.
	 */
	public static String removeMnemonicMarker(String text) {
		return StringUtils.replace(text, "&", "", 0, 1, UtilConstants.COMPARE_BINARY);
	}
	
	/**
	 * Get the {@link JScrollPane} that surrounds the given component. 
	 * @return The desired {@link JScrollPane} or <code>null</code> if no {@link JScrollPane} could be found.
	 */
	public static JScrollPane getSurroundingScrollPane(Component comp) {
    	JScrollPane fScrollPane = null;
    	while(comp != null) {
    		comp = comp.getParent();
    		if(comp instanceof JScrollPane) {
    			fScrollPane = (JScrollPane) comp;
    			break;
    		}
    	}
		return fScrollPane;
	}
	
	public static Dimension getTextDimension(final String text, final Font font) {
		if(defaultLabel == null) {
			defaultLabel = new JLabel();
		}
		
		// get metrics from the graphics
		FontMetrics fontMetrics = defaultLabel.getFontMetrics(font);

		// get the height of a line of text in this
		// font and render context
		int hgt = fontMetrics.getHeight();
		// get the advance of my text in this font
		// and render context
		int adv = fontMetrics.stringWidth(text);
		// calculate the size of a box to hold the text
		Dimension size = new Dimension(adv, hgt);

		return size;
	}
}
