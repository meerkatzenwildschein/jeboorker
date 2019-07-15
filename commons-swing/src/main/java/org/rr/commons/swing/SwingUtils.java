package org.rr.commons.swing;


import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalTheme;

import org.rr.commons.collection.CompoundList;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.MathUtils;
import org.rr.commons.utils.ReflectionFailureException;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.commons.utils.UtilConstants;

public class SwingUtils {
	
	// A maximally-contrasting palette of colors designed by Kenneth Kelly, but without white, black, or gray.
	private static final List<Color> KENNETH_KELLY_COLORS = Collections.unmodifiableList(new ArrayList<Color>() {{
		add(new Color(0xEAD846));
		add(new Color(0x6F0989));
		add(new Color(0xD9712A));
		add(new Color(0x97C9E4));
		add(new Color(0xB82035));
		add(new Color(0xC3C385));
		add(new Color(0x62AC49));
		add(new Color(0xCE81AD));
		add(new Color(0x476CB3));
		add(new Color(0xDB8963));
		add(new Color(0x491093));
		add(new Color(0xDFAA36));
		add(new Color(0x8F0189));
		add(new Color(0xE7F45E));
		add(new Color(0x7C1B15));
		add(new Color(0x94B741));
		add(new Color(0x6C3715));
		add(new Color(0xD03227));
		add(new Color(0x2B3916));
	}});
	
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
		final ArrayList<Component> comps = new ArrayList<>();

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
			result = (Component[]) getAllComponentsRecursive(container, new Class[] {className}, null).toArray(new Component[0]);
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
			recursive = new ArrayList<>(100);
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
	 * Center the given <code>windows</code> in relative to the given <code>invoker</code>.
	 * @param invoker Reference window for the window to center.
	 * @param window The window instance to center.
 	 */
	public static void centerOnWindow(Window invoker, Window window) {
		Point invokerLocationOnScreen = invoker.getLocationOnScreen();
		Dimension invokerSize = invoker.getSize();
		Dimension windowSize = window.getSize();

		window.setLocation(invokerLocationOnScreen.x + (invokerSize.width / 2) - (windowSize.width / 2),
				invokerLocationOnScreen.y + ((invokerSize.height / 2) - (windowSize.height / 2) / 2));
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
		if(abortAction != null) {
			((JComponent)dialog.getContentPane()).registerKeyboardAction(abortAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		}
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
		return StringUtil.replace(text, "&", "", 0, 1, UtilConstants.COMPARE_BINARY);
	}

	/**
	 * Get the surrounding component of the given type.
	 * @param comp The component child of the component to search
	 * @param type The parent component class.
	 * @return The desired component or <code>null</code> if no component could be found.
	 */
	public static Component getSurroundingComponent(Component comp, Class<? extends Component> type) {
    	Component fScrollPane = null;
    	while(comp != null) {
    		comp = comp.getParent();
    		if(comp != null && ReflectionUtils.isAssignable(type, comp.getClass())) {
    			fScrollPane = comp;
    			break;
    		}
    	}
		return fScrollPane;
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

	/**
	 * Open the given url in the default system web browser.
	 * @param url The url to browse-
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void openURL(String url) throws MalformedURLException, IOException, URISyntaxException {
		Desktop.getDesktop().browse(new URL(url).toURI());
	}

	/**
	 * Sets the given look and feel.
	 *
	 * @param lafClassName The look and feel class name. Should also be a class name and a theme name which
	 *   are separated by a ; char.
	 * @throws ReflectionFailureException
	 * @throws UnsupportedLookAndFeelException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static void setLookAndFeel(String lafClassName) throws ReflectionFailureException, UnsupportedLookAndFeelException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		if (lafClassName.indexOf(';') != -1) {
			List<String> split = ListUtils.split(lafClassName, ";");
			String className = split.get(0);
			String themeClassName = split.get(1);
			LookAndFeel lafInstance = (LookAndFeel) ReflectionUtils.getObjectInstance(className, null);
			if (ReflectionUtils.containsMethod(className, "setCurrentTheme", new Class<?>[] { MetalTheme.class },
					ReflectionUtils.VISIBILITY_VISIBLE_ACCESSIBLE_ONLY)) {
				MetalTheme theme = (MetalTheme) ReflectionUtils.getObjectInstance(themeClassName, null);
				ReflectionUtils.invokeMethod(lafInstance, "setCurrentTheme", theme);
			}

			UIManager.setLookAndFeel(lafInstance);
		} else {
			UIManager.setLookAndFeel(lafClassName);
		}
	}

	
	public static void setColor(Component comp, boolean isSelected) {
		if(isSelected) {
			comp.setBackground(SwingUtils.getSelectionBackgroundColor());
			comp.setForeground(SwingUtils.getSelectionForegroundColor());
		} else {
			comp.setBackground(SwingUtils.getBackgroundColor());
			comp.setForeground(SwingUtils.getForegroundColor());
		}
	}
	
	/**
	 * Generates a list of colors. The result is always identical.
	 * 
	 * @param amount The number of colors to calculate.
	 * @return A list of colors. Never returns <code>null</code>.
	 * @see https://stackoverflow.com/questions/3403826/how-to-dynamically-compute-a-list-of-colors
	 */
	private static List<Color> calculateUniqueColors(int amount) {
		final int lowerLimit = 0x10;
		final int upperLimit = 0xE0;
		final int colorStep = (int) ((upperLimit - lowerLimit) / Math.pow(amount, 1f / 3));

		final List<Color> colors = new ArrayList<>(amount);

		for (int R = lowerLimit; R < upperLimit; R += colorStep)
			for (int G = lowerLimit; G < upperLimit; G += colorStep)
				for (int B = lowerLimit; B < upperLimit; B += colorStep) {
					if (colors.size() >= amount) { // The calculated step is not very precise, so this safeguard is appropriate
						return colors;
					} else {
						int color = (R << 16) + (G << 8) + (B);
						colors.add(new Color(color));
					}
				}
		return colors;
	}
	
	/**
	 * Generates a list of colors. The result is always identical.
	 * 
	 * @param amount The number of colors to calculate.
	 * @return A list of colors. Never returns <code>null</code>.
	 */
	public static List<Color> getUniqueColors(int amount) {
		if(amount <= KENNETH_KELLY_COLORS.size()) {
			return KENNETH_KELLY_COLORS;
		}
		return new CompoundList<>(KENNETH_KELLY_COLORS, calculateUniqueColors(amount - KENNETH_KELLY_COLORS.size()));
	}

}
