package org.rr.common.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * @see http://www.java2s.com/Code/Java/Swing-JFC/Showhowaglasspanecanbeusedtoblockmouseandkeyevents.htm
 *
 */
public class ShadowPanel extends JPanel implements MouseListener, MouseMotionListener, FocusListener {

	Toolkit toolkit;

	JMenuBar menuBar;

	Container contentPane;

	boolean inDrag = false;

	// trigger for redispatching (allows external control)
	boolean needToRedispatch = false;

	public ShadowPanel() {
		setOpaque(false);
		addMouseListener(this);
		addMouseMotionListener(this);
		addFocusListener(this);
	}

	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
		g2d.setColor(Color.BLACK);

		g2d.fillRect(0, 0, getWidth(), getHeight());
	}

	public void setVisible(boolean v) {
		// Make sure we grab the focus so that key events don't go astray.
		if (v)
			requestFocus();
		super.setVisible(v);
	}

	// Once we have focus, keep it if we're visible
	public void focusLost(FocusEvent fe) {
		if (isVisible())
			requestFocus();
	}

	public void focusGained(FocusEvent fe) {
	}

	// We only need to redispatch if we're not visible, but having full control
	// over this might prove handy.
	public void setNeedToRedispatch(boolean need) {
		needToRedispatch = need;
	}

	/*
	 * (Based on code from the Java Tutorial) We must forward at least the mouse drags that started with mouse presses over the check box. Otherwise, when the
	 * user presses the check box then drags off, the check box isn't disarmed -- it keeps its dark gray background or whatever its L&F uses to indicate that
	 * the button is currently being pressed.
	 */
	public void mouseDragged(MouseEvent e) {
		if (needToRedispatch)
			redispatchMouseEvent(e);
	}

	public void mouseMoved(MouseEvent e) {
		if (needToRedispatch)
			redispatchMouseEvent(e);
	}

	public void mouseClicked(MouseEvent e) {
		if (needToRedispatch)
			redispatchMouseEvent(e);
	}

	public void mouseEntered(MouseEvent e) {
		if (needToRedispatch)
			redispatchMouseEvent(e);
	}

	public void mouseExited(MouseEvent e) {
		if (needToRedispatch)
			redispatchMouseEvent(e);
	}

	public void mousePressed(MouseEvent e) {
		if (needToRedispatch)
			redispatchMouseEvent(e);
	}

	public void mouseReleased(MouseEvent e) {
		if (needToRedispatch) {
			redispatchMouseEvent(e);
			inDrag = false;
		}
	}

	private void redispatchMouseEvent(MouseEvent e) {
		boolean inButton = false;
		boolean inMenuBar = false;
		Point glassPanePoint = e.getPoint();
		Component component = null;
		Container container = contentPane;
		Point containerPoint = SwingUtilities.convertPoint(this, glassPanePoint, contentPane);
		int eventID = e.getID();

		if (containerPoint.y < 0) {
			inMenuBar = true;
			container = menuBar;
			containerPoint = SwingUtilities.convertPoint(this, glassPanePoint, menuBar);
			testForDrag(eventID);
		}

		// XXX: If the event is from a component in a popped-up menu,
		// XXX: then the container should probably be the menu's
		// XXX: JPopupMenu, and containerPoint should be adjusted
		// XXX: accordingly.
		component = SwingUtilities.getDeepestComponentAt(container, containerPoint.x, containerPoint.y);

		if (component == null) {
			return;
		} else {
			inButton = true;
			testForDrag(eventID);
		}

		if (inMenuBar || inButton || inDrag) {
			Point componentPoint = SwingUtilities.convertPoint(this, glassPanePoint, component);
			component.dispatchEvent(new MouseEvent(component, eventID, e.getWhen(), e.getModifiers(), componentPoint.x, componentPoint.y, e.getClickCount(), e
					.isPopupTrigger()));
		}
	}

	private void testForDrag(int eventID) {
		if (eventID == MouseEvent.MOUSE_PRESSED) {
			inDrag = true;
		}
	}

}
