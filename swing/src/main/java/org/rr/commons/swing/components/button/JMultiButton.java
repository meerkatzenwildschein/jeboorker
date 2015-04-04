/*
 * This Class was taken from the swing-bug project. https://swing-bug.dev.java.net/
 * 
 * JMultiButton.java
 *
 * Created on March 23, 2007, 11:52 PM
 *
 * Copyright 2006-2007 Nigel Hughes
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/
 * licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.rr.commons.swing.components.button;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPopupMenu;

/**
 * A normal JButton which can have multiple actions added to it. When more than one action is added an arrow is added to the right hand side. If the user clicks
 * on the left, the action is performed. If the user clicks on the right, a list of all of the actions is displayed.
 * 
 * @author nigel
 */
public class JMultiButton extends JButton implements MouseListener {

	private static final long serialVersionUID = -2990339964071073830L;

	/**
	 * A list of all of the actions available on the button.
	 */
	private LinkedList<Action> actions = new LinkedList<>();
	/**
	 * The action currently selected
	 */
	private Action currentAction = null;

	/**
	 * Tracks if the mouse is inside the button or not.
	 */
	private boolean mouseInside = false;
	/**
	 * Filled in by the paint method it records where the line is drawn and enables the fireActionPerformed method to determine if the list of options should be
	 * displayed or the current action fired
	 */
	private int boundary = 0;
	/**
	 * True if all the actions are enabled, false if they are not.
	 */
	private boolean enabled = true;

	private Color hoverColor = new Color(60, 60, 190, 164);
	
	/**
	 * A flag that tells if a click to the button area should also toggle 
	 * the popup menu.
	 */
	private boolean fullSizePopupActionArea = false;

	public boolean isFullSizePopupActionArea() {
		return fullSizePopupActionArea;
	}

	public void setFullSizePopupActionArea(boolean fullSizePopupActionArea) {
		this.fullSizePopupActionArea = fullSizePopupActionArea;
	}

	/**
	 * Creates a new instance, no actions are added.
	 */
	public JMultiButton() {
		super("GUI Editor Only");
		initialize();
	}

	/**
	 * Creates a new instance of JComboButton, with the specified action added
	 * 
	 * @param action The initial action
	 */
	public JMultiButton(Action action) {
		initialize();
		addAction(action);
		setCurrentAction(action);
	}

	/**
	 * Sets the button up ready for use, must be called by any constructor.
	 */
	protected void initialize() {
		setHorizontalAlignment(JButton.LEFT);
		addMouseListener(this);
	}

	/**
	 * Gets a linked list with all of the current actions in it.
	 * 
	 * @return A list containing all of the action.
	 */
	public LinkedList<Action> getActions() {
		return (LinkedList<Action>) actions.clone();
	}

	/**
	 * Get the color of the rounded rectangle drawn when the mouse is hovering over the arrow.
	 * 
	 * @return The color
	 */
	public Color getHoverColor() {
		return hoverColor;
	}

	/**
	 * Change the color of the rounded rectangle drawn when the mouse is hovering over the arrow.
	 * 
	 * @param hoverColor
	 *            The chosen color
	 */
	public void setHoverColor(Color hoverColor) {
		this.hoverColor = hoverColor;
	}

	/**
	 * Gets the currently active action
	 * 
	 * @return The currently selected action
	 */
	public Action getCurrentAction() {
		return currentAction;
	}

	/**
	 * Adds multiple actions to the button in a single operation
	 * 
	 * @param actions
	 *            A linked list full of actions
	 */
	public void setActions(LinkedList<Action> actions) {
		this.actions = (LinkedList<Action>) actions.clone();
		setCurrentAction(actions.getFirst());
	}

	/**
	 * Adds an action to those available for the button.
	 * 
	 * @param action
	 *            The action to add
	 */
	public void addAction(Action action) {
		actions.add(action);
		if (actions.size() == 1) {
			setCurrentAction(action);
		}
	}

	/**
	 * Removes an action from the list of those availble from the button
	 * 
	 * @param action
	 *            The action to remove
	 */
	public void removeAction(Action action) {
		actions.remove(action);
	}

	/**
	 * Sets the specified action to be the current action
	 * 
	 * @param action
	 *            The action to make current
	 */
	public void setCurrentAction(Action action) {
		if (actions.contains(action)) {
			currentAction = action;
			super.setAction(currentAction);
			currentAction.setEnabled(this.enabled);
		}
	}

	/**
	 * Overridden so that a check can be made to see if the current action should be fired, or the list of available actions should be shown.
	 * 
	 * @param actionEvent
	 *            The action event
	 */
	protected void fireActionPerformed(ActionEvent actionEvent) {
		Point p = getMousePosition();
		if (p == null) {
			return;
		}
		if (isFullSizePopupActionArea() || p.x > boundary) {
			JPopupMenu.setDefaultLightWeightPopupEnabled(false);
			
			JPopupMenu popup = new JMediumWeightPopupMenu();
			popup.setLightWeightPopupEnabled(false);
			
			for (Action action : actions) {
				popup.add(new ActionOptionWrapper(action));
			}
			popup.setMinimumSize(new Dimension(getWidth(), getHeight()));
			popup.show(this, 0, getHeight());
		} else {
			super.fireActionPerformed(actionEvent);
		}
	}

	/**
	 * Enables the button
	 * 
	 * @param enabled
	 *            True if the button is enabled, false if it isn't
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		super.setEnabled(enabled);
	}

	/**
	 * Returns true if the button is enabled, false if it isn't
	 * 
	 * @return See above
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

//	private final void debugPaintBounds(Graphics graphics) {
//		Rectangle inner = getInternalBounds();
//		Rectangle arrow = getArrowBounds();
//
//		graphics.setColor(Color.RED);
//		graphics.drawRect(inner.x, inner.y, inner.x + inner.width, inner.y + inner.height);
//		graphics.drawRect(arrow.x, arrow.y, arrow.x + arrow.width, arrow.y + arrow.height);
//	}

	/**
	 * Paints the component
	 * 
	 * @param graphics
	 *            The graphics context
	 */
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		// debugPaintBounds(graphics);
		Graphics2D g2d = (Graphics2D) graphics;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		boundary = getArrowBounds().x;
		drawArrow(g2d);
	}

	/**
	 * This function can be over-ridden to draw any kind of arrow (with any kind of hover effect) the over-riding class wants.
	 * 
	 * @param graphics
	 *            The graphics context
	 */
	protected void drawArrow(Graphics2D graphics) {
		Rectangle arrowBounds = getArrowBounds();
		int hShift = arrowBounds.width / 4;
		int vShift = arrowBounds.height / 3;

		if (!isEnabled()) {
			graphics.setColor(new Color(0, 0, 0, 128));
		} else if (mouseInside) {
			graphics.setColor(hoverColor);
			graphics.fillRoundRect(arrowBounds.x + 1, arrowBounds.y, arrowBounds.width, arrowBounds.height, hShift, vShift);
			graphics.setColor(getForeground());
		} else {
			graphics.setColor(getForeground());
		}

		arrowBounds.x += hShift;
		arrowBounds.width -= hShift * 2 - 1;
		arrowBounds.y += vShift;
		arrowBounds.height -= vShift * 2;
		Polygon poly = new Polygon(new int[] { arrowBounds.x, arrowBounds.x + arrowBounds.width, arrowBounds.x + arrowBounds.width / 2 }, new int[] {
				arrowBounds.y, arrowBounds.y, arrowBounds.y + arrowBounds.height }, 3);

		graphics.fillPolygon(poly);

	}

	/**
	 * Gets the internal area of the button, useful for determining the draw size of the seperator etc.
	 * 
	 * @return The internal bounds in component co-ordinates
	 */
	private Rectangle getInternalBounds() {
		Insets insets = getInsets();
		Rectangle rect = new Rectangle(insets.left, insets.top, getWidth() - (insets.left + insets.right + 1), getHeight() - (insets.top + insets.bottom + 1));

		return rect;
	}

	/**
	 * Gets the area (in component co-ordinates) of the selection image used to bring up the choices for the options
	 * 
	 * @return The area of the arrow bounds box
	 */
	private Rectangle getArrowBounds() {
		Rectangle rect = getInternalBounds();

		rect.x = rect.x + (rect.width - rect.height) + 1;
		rect.width = rect.height;

		return rect;
	}

	/**
	 * Returns a preferred size a bit bigger than it needs to be so there is room for the arrow.
	 * 
	 * @return The minimum width and height of the button
	 */
	public Dimension getPreferredSize() {
		return adjustDimension(super.getPreferredSize(), true);
	}

	/**
	 * Utilitity method allowing the upsizing or downsizing of a dimension by the amount requied by the arrow
	 * 
	 * @param current
	 *            The dimension to adjust
	 * @param enlarge
	 *            Upsize or downsize
	 * 
	 * @return The adjusted dimension
	 */
	protected Dimension adjustDimension(Dimension current, boolean enlarge) {
		if (enlarge) {
			current.width += getInternalBounds().height + 1;
		} else {
			current.width -= getInternalBounds().height + 1;
		}

		return current;
	}

	/**
	 * Returns a minimum size a bit bigger than it needs to be so there is room for the arrow.
	 * 
	 * @return The minimum width and height of the button
	 */
	public Dimension getMinimumSize() {
		return adjustDimension(super.getMinimumSize(), true);
	}

	/**
	 * Ignored
	 * 
	 * @param mouseEvent The mouse event
	 */
	public void mouseClicked(MouseEvent mouseEvent) {
	}

	/**
	 * Ignored
	 * 
	 * @param mouseEvent The mouse event
	 */
	public void mousePressed(MouseEvent mouseEvent) {
	}

	/**
	 * Ignored
	 * 
	 * @param mouseEvent The mouse event
	 */
	public void mouseReleased(MouseEvent mouseEvent) {
	}

	/**
	 * Records the mouse is inside the area
	 * 
	 * @param mouseEvent The mouse event
	 */
	public void mouseEntered(MouseEvent mouseEvent) {
		mouseInside = true;
		repaint();
	}

	/**
	 * Records the mouse has moved outside
	 * 
	 * @param mouseEvent The mouse event
	 */
	public void mouseExited(MouseEvent mouseEvent) {
		mouseInside = false;
		repaint();
	}

	/**
	 * A wrapper for the actions to be dislayed when the pop-up is drawn.
	 */
	protected class ActionOptionWrapper extends AbstractAction {
		private static final long serialVersionUID = -7161990723874074407L;
		
		Action internalAction = null;

		/**
		 * Creates a new instance of a wrapper action
		 * 
		 * @param wrapAction
		 *            The action to be wrapped
		 */
		public ActionOptionWrapper(Action wrapAction) {
			internalAction = wrapAction;
			putValue(Action.NAME, wrapAction.getValue(Action.NAME));
			putValue(Action.SMALL_ICON, wrapAction.getValue(Action.SMALL_ICON));
		}

		/**
		 * Fired when the action is performed
		 * 
		 * @param actionEvent
		 *            The action event
		 */
		public void actionPerformed(ActionEvent actionEvent) {
			setCurrentAction(internalAction);
			internalAction.actionPerformed(actionEvent);
		}

	}
}
