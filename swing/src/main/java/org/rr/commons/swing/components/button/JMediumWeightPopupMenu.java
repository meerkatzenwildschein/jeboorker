package org.rr.commons.swing.components.button;

import java.awt.Point;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.Popup;
import javax.swing.PopupFactory;

/**
 * This is a workaround for the JPopupMenu for using heavy weight popups
 * only. Ubunutu 8.10 and 9.04 did not show light weight popups.      
 */
public class JMediumWeightPopupMenu extends JPopupMenu {
    
	private static final long serialVersionUID = 3651666432810622912L;
	private int desiredLocationX;
	private int desiredLocationY;

    /**
     * Key used to indicate a light weight popup should be used.
     */
    static final int LIGHT_WEIGHT_POPUP   = 0;

    /**
     * Key used to indicate a medium weight Popup should be used.
     */
    static final int MEDIUM_WEIGHT_POPUP  = 1;

    /*
     * Key used to indicate a heavy weight Popup should be used.
     */
    static final int HEAVY_WEIGHT_POPUP   = 2;
    
    /**
     * Sets the location of the upper left corner of the
     * popup menu using x, y coordinates.
     *
     * @param x the x coordinate of the popup's new position
     *          in the screen's coordinate space
     * @param y the y coordinate of the popup's new position
     *          in the screen's coordinate space
     * @beaninfo
     * description: The location of the popup menu.
     */
    public void setLocation(int x, int y) {
        desiredLocationX = x;
        desiredLocationY = y;
        super.setLocation(x, y);
    }
    
	/**
     * Sets the visibility of the popup menu.
     * 
     * @param b true to make the popup visible, or false to
     *          hide it
     * @beaninfo
     *           bound: true
     *     description: Makes the popup visible
     */
    public void setVisible(boolean b) {

        // Is it a no-op?
        if (b == isVisible())
            return;

        // if closing, first close all Submenus
        if (b == false) {

	    // 4234793: This is a workaround because JPopupMenu.firePopupMenuCanceled is
	    // a protected method and cannot be called from BasicPopupMenuUI directly
	    // The real solution could be to make 
	    // firePopupMenuCanceled public and call it directly.
	    Boolean doCanceled = (Boolean)getClientProperty("JPopupMenu.firePopupMenuCanceled");
	    if (doCanceled != null && doCanceled == Boolean.TRUE) {
		putClientProperty("JPopupMenu.firePopupMenuCanceled", Boolean.FALSE);
		firePopupMenuCanceled();
	    }
            getSelectionModel().clearSelection();
	    
        } else {
            // This is a popup menu with MenuElement children,
            // set selection path before popping up!
            if (isPopupMenu()) {
                MenuElement me[] = new MenuElement[1];
                me[0] = (MenuElement) this;
                MenuSelectionManager.defaultManager().setSelectedPath(me);
            }
        }

        if(b) {
            firePopupMenuWillBecomeVisible();
            Popup popup = getPopup();
            setPopupField("popup", popup);
	    firePropertyChange("visible", Boolean.FALSE, Boolean.TRUE);

	   
	} else {
		super.setVisible(b);
	}
    }
    
    private void setPopupField(String fieldName, Popup popup) {
    	try {
			Field field = JPopupMenu.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(this, popup);
		}catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private Popup getPopupField(String fieldName) {
    	try {
			Field field = JPopupMenu.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.get(this);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }
    
    /**
     * Returns true if the popup menu is a standalone popup menu
     * rather than the submenu of a <code>JMenu</code>.
     *
     * @return true if this menu is a standalone popup menu, otherwise false
     */
    private boolean isPopupMenu() {
    	try {
	    	Method method = JPopupMenu.class.getDeclaredMethod("isPopupMenu", new Class[0]);
	    	method.setAccessible(true);
	    	return ((Boolean)method.invoke(this, new Object[0])).booleanValue();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return false;
    }
    
    /**
     * Returns an point which has been adjusted to take into account of the 
     * desktop bounds, taskbar and multi-monitor configuration.
     * <p>
     * This adustment may be cancelled by invoking the application with
     * -Djavax.swing.adjustPopupLocationToFit=false
     */
    Point adjustPopupLocationToFitScreen2(int xposition, int yposition) {
    	try {
    		Method method = JPopupMenu.class.getDeclaredMethod("adjustPopupLocationToFitScreen", new Class[] {int.class, int.class});
    		method.setAccessible(true);
    		return (Point)method.invoke(this, new Object[] {Integer.valueOf(xposition), Integer.valueOf(yposition)});
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
    /**
     * Provides a hint as to the type of <code>Popup</code> that should
     * be created.
     */
    void setPopupType(int type, PopupFactory popupFactory) {
    	try {
    		Method method = PopupFactory.class.getDeclaredMethod("setPopupType", new Class[] {int.class});
    		method.setAccessible(true);
    		method.invoke(popupFactory, new Object[] {Integer.valueOf(type)});
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    /**
     * Returns a <code>Popup</code> instance from the
     * <code>PopupMenuUI</code> that has had <code>show</code> invoked on
     * it. If the current <code>popup</code> is non-null,
     * this will invoke <code>dispose</code> of it, and then
     * <code>show</code> the new one.
     * <p>
     * This does NOT fire any events, it is up the caller to dispatch
     * the necessary events.
     */
    private Popup getPopup() {
        Popup oldPopup = getPopupField("popup");

        if (oldPopup != null) {
            oldPopup.hide();
        }
        PopupFactory popupFactory = PopupFactory.getSharedInstance();
        setPopupType(HEAVY_WEIGHT_POPUP, popupFactory);

        // adjust the location of the popup
        Point p = adjustPopupLocationToFitScreen2(desiredLocationX,desiredLocationY);
        
        
	desiredLocationX = p.x;
	desiredLocationY = p.y;

        Popup newPopup = getUI().getPopup(this, desiredLocationX,
                                          desiredLocationY);

//        popupFactory.setPopupType(PopupFactory.LIGHT_WEIGHT_POPUP);
        
        newPopup.show();
        return newPopup;
    }

}
