/*
 * $Id: QButton.java,v 1.5 2008/01/28 21:28:37 edankert Exp $
 *
 * Copyright (c) 2002 - 2008, Edwin Dankert
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, 
 *	 this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright 
 * 	 notice, this list of conditions and the following disclaimer in the 
 *	 documentation and/or other materials provided with the distribution. 
 * * Neither the name of 'Edwin Dankert' nor the names of its contributors 
 *	 may  be used to endorse or promote products derived from this software 
 *	 without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.bounce;

import org.bounce.plaf.BounceButtonUI;

import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.border.Border;
import javax.swing.Icon;
import javax.swing.ButtonModel;
import javax.swing.Action;
import javax.swing.UIManager;
import javax.swing.JButton;

/**
 * A generic button that has more component state specific color, font 
 * and border attributes than the JButton.
 *
 * @version	$Revision: 1.5 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class QButton extends JButton {
	private static final long serialVersionUID = 3689348810430494260L;
	/** Do not have a focus rectangle */
	public static final int FOCUS_NONE 					= 0;
	/** focus rectangle around text */
	public static final int FOCUS_AROUND_TEXT			= 1;
	/** focus rectangle around icon */
	public static final int FOCUS_AROUND_ICON			= 2;
	/** focus rectangle around icon and text */
	public static final int FOCUS_AROUND_TEXT_AND_ICON	= 3;
	
	private Color pressedBackground		= null;
	private Color pressedForeground		= null;
	private Border pressedBorder		= null;
	private Font pressedFont			= null;

	private Color selectedBackground	= null;
	private Color selectedForeground	= null;
	private Border selectedBorder 		= null;
	private Font selectedFont	 		= null;

	private Color disabledBackground 	= null;
	private Color disabledForeground 	= null;
	private Border disabledBorder 		= null;
	private Font disabledFont	 		= null;

	private Color disabledSelectedBackground 	= null;
	private Color disabledSelectedForeground 	= null;
	private Border disabledSelectedBorder 		= null;
	private Font disabledSelectedFont	 		= null;

	private Color rolloverBackground 	= null;
	private Color rolloverForeground 	= null;
	private Border rolloverBorder 		= null;
	private Font rolloverFont	 		= null;

	private Color rolloverSelectedBackground 	= null;
	private Color rolloverSelectedForeground 	= null;
	private Border rolloverSelectedBorder 		= null;
	private Font rolloverSelectedFont	 		= null;

	private Color focusedBackground 	= null;
	private Color focusedForeground 	= null;
	private Border focusedBorder 		= null;
	private Font focusedFont	 		= null;
	private Icon focusedIcon	 		= null;
	
	private Color focusedSelectedBackground 	= null;
	private Color focusedSelectedForeground 	= null;
	private Border focusedSelectedBorder 		= null;
	private Font focusedSelectedFont	 		= null;
	private Icon focusedSelectedIcon	 		= null;
	
	private Color focusedRectangleColor	= null;
	private int focusedRectangleMode = FOCUS_AROUND_TEXT_AND_ICON;
	private boolean doubleClickEnabled = false;

	private int maxLines = 1;
	private int minLines = 1;

	/**
	 * Constructor without a set text or icon.
	 */
	public QButton() {
		super();
		init();
	}

	/**
	 * Constructor with properties take from the action supplied.
	 * 
	 * @param a the associated action.
	 */
	public QButton( Action a) {
		super( a);
		init();
	}

	/**
	 * Constructor for a button with an Icon.
	 *
	 * @param icon the icon for the button.
	 */
	public QButton( Icon icon) {
		super( icon);
		init();
	}

	/**
	 * Constructor for a button with text.
	 *
	 * @param text the text for the button.
	 */
	public QButton( String text) {
		super( text);
		init();
	}

	/**
	 * Constructor for a button with an icon and text.
	 *
	 * @param text the text for the button.
	 * @param icon the icon for the button.
	 */
	public QButton( String text, Icon icon) {
		super( text, icon);
		init();
	}

	/**
	 * Initialises the default colors.
	 */
	private void init() {
		focusedRectangleColor = UIManager.getColor( "Button.focus");
		pressedBackground = UIManager.getColor( "Button.select");
		disabledForeground = UIManager.getColor( "Button.disabledText");
		// focusedSelectedForeground = UIManager.getColor( "Button.focusedSelected");
	}

	/**
	 * Sets the look and feel to the Bounce Button UI look and feel.
	 * Override this method if you want to install a different UI.
	 */
	public void updateUI() {
	    setUI( BounceButtonUI.createUI( this));
	}
	
	/**
	 * Paints the QButton's border, this border needs to be selected from all 
	 * component state specific borders.
	 *
	 * @param g the graphics.
	 */
	protected void paintBorder(Graphics g) {
	    ButtonModel model	= getModel();
	    Border border 		= getBorder();
	    Border tmpBorder	= null;

		// Disabled
	    if( !model.isEnabled()) {
	    	if( model.isSelected()) {
	    		tmpBorder = getDisabledSelectedBorder();
	    	} else {
	    		tmpBorder = getDisabledBorder();
	    	}

    	// Pressed
	    } else if( model.isPressed() && model.isArmed()) {
	    	tmpBorder = getPressedBorder();

	    // In Focus but not pressed.
	    } else if( hasFocus()) {
	    	if( model.isSelected()) {
	    		tmpBorder = getFocusedSelectedBorder();
	    	} else {
		    	tmpBorder = getFocusedBorder();
	    	}

	    // Rollover but not pressed or in focus.  
	    } else if( isRolloverEnabled() && model.isRollover()) {
	    	if( model.isSelected()) {
	    		tmpBorder = getRolloverSelectedBorder();
	    	} else {
	    		tmpBorder = getRolloverBorder();
	    	}

	    // Selected but not pressed or in focus rollover.
	    } else if( model.isSelected()) {
	    	tmpBorder = getSelectedBorder();
	    }

	    if( tmpBorder != null) {
	    	border = tmpBorder;
	    }

	    if ( border != null) {
	        border.paintBorder(this, g, 0, 0, getWidth(), getHeight());
	    }
	}
	
/*
 * PRESSED METHODS
 */

	/**
	 * Sets the background color that is painted when the button is pressed.
	 * Setting the color to null, changes the color to the default background color.
	 * 
	 * @param color the pressed background color.
	 */
	public void setPressedBackground( Color color) {
		pressedBackground = color;
	}

	/**
	 * Returns the background color that is painted when the button is pressed.
	 * 
	 * @return the pressed background color.
	 */
	public Color getPressedBackground() {
		return pressedBackground;
	}
	
	/**
	 * Sets the foreground color that is painted when the button is pressed.
	 * Setting the color to null, changes the color to the default foreground color.
	 * 
	 * @param color the pressed foreground color.
	 */
	public void setPressedForeground( Color color) {
		pressedForeground = color;
	}
	
	/**
	 * Returns the foreground color that is painted when the button is pressed.
	 * 
	 * @return the pressed foreground color.
	 */
	public Color getPressedForeground() {
		return pressedForeground;
	}

	/**
	 * Sets the border that is painted when the button is pressed.
	 * Setting the border to null, changes the border to the default border.
	 * 
	 * @param border the pressed border.
	 */
	public void setPressedBorder( Border border) {
		pressedBorder = border;
	}

	/**
	 * Returns the border that is painted when the button is pressed.
	 * 
	 * @return the pressed border.
	 */
	public Border getPressedBorder() {
		return pressedBorder;
	}

	/**
	 * Sets the Font that is painted when the button is pressed.
	 * Setting the Font to null, changes the font to the default font.
	 * 
	 * @param font the pressed font.
	 */
	public void setPressedFont( Font font) {
		pressedFont = font;
	}

	/**
	 * Returns the font that is painted when the button is pressed.
	 * 
	 * @return the pressed font.
	 */
	public Font getPressedFont() {
		return pressedFont;
	}

/*
 * SELECTED METHODS
 */

	/**
	 * Sets the background color that is painted when the button is selected.
	 * Setting the color to null, changes the color to the default background color.
	 * 
	 * @param color the selected background color.
	 */
	public void setSelectedBackground( Color color) {
		selectedBackground = color;
	}

	/**
	 * Returns the background color that is painted when the button is selected.
	 * 
	 * @return the selected background color.
	 */
	public Color getSelectedBackground() {
		return selectedBackground;
	}
	
	/**
	 * Sets the foreground color that is painted when the button is selected.
	 * Setting the color to null, changes the color to the default foreground color.
	 * 
	 * @param color the selected foreground color.
	 */
	public void setSelectedForeground( Color color) {
		selectedForeground = color;
	}
	
	/**
	 * Returns the foreground color that is painted when the button is selected.
	 * 
	 * @return the selected foreground color.
	 */
	public Color getSelectedForeground() {
		return selectedForeground;
	}

	/**
	 * Sets the border that is painted when the button is selected.
	 * Setting the border to null, changes the border to the default border.
	 * 
	 * @param border the selected border.
	 */
	public void setSelectedBorder( Border border) {
		selectedBorder = border;
	}

	/**
	 * Returns the border that is painted when the button is selected.
	 * 
	 * @return the selected border.
	 */
	public Border getSelectedBorder() {
		return selectedBorder;
	}

	/**
	 * Sets the Font that is painted when the button is selected.
	 * Setting the Font to null, changes the font to the default font.
	 * 
	 * @param font the selected font.
	 */
	public void setSelectedFont( Font font) {
		selectedFont = font;
	}

	/**
	 * Returns the font that is painted when the button is selected.
	 * 
	 * @return the selected font.
	 */
	public Font getSelectedFont() {
		return selectedFont;
	}

/*
 * DISABLED METHODS
 */

	/**
	 * Sets the background color that is painted when the button is disabled.
	 * Setting the color to null, changes the color to the default background color.
	 * 
	 * @param color the disabled background color.
	 */
	public void setDisabledBackground( Color color) {
		disabledBackground = color;
	}

	/**
	 * Returns the background color that is painted when the button is disabled.
	 * 
	 * @return the disabled background color.
	 */
	public Color getDisabledBackground() {
		return disabledBackground;
	}
	
	/**
	 * Sets the foreground color that is painted when the button is disabled.
	 * Setting the color to null, changes the color to the default foreground color.
	 * 
	 * @param color the disabled foreground color.
	 */
	public void setDisabledForeground( Color color) {
		disabledForeground = color;
	}
	
	/**
	 * Returns the foreground color that is painted when the button is disabled.
	 * 
	 * @return the disabled foreground color.
	 */
	public Color getDisabledForeground() {
		return disabledForeground;
	}

	/**
	 * Sets the border that is painted when the button is disabled.
	 * Setting the border to null, changes the border to the default border.
	 * 
	 * @param border the disabled border.
	 */
	public void setDisabledBorder( Border border) {
		disabledBorder = border;
	}

	/**
	 * Returns the border that is painted when the button is disabled.
	 * 
	 * @return the disabled border.
	 */
	public Border getDisabledBorder() {
		return disabledBorder;
	}

	/**
	 * Sets the Font that is painted when the button is disabled.
	 * Setting the Font to null, changes the font to the default font.
	 * 
	 * @param font the disabled font.
	 */
	public void setDisabledFont( Font font) {
		disabledFont = font;
	}

	/**
	 * Returns the font that is painted when the button is disabled.
	 * 
	 * @return the disabled font.
	 */
	public Font getDisabledFont() {
		return disabledFont;
	}

/*
 * DISABLED SELECTED METHODS
 */

	/**
	 * Sets the background color that is painted when the button is disabledSelected.
	 * Setting the color to null, changes the color to the default background color.
	 * 
	 * @param color the disabledSelected background color.
	 */
	public void setDisabledSelectedBackground( Color color) {
		disabledSelectedBackground = color;
	}

	/**
	 * Returns the background color that is painted when the button is disabledSelected.
	 * 
	 * @return the disabledSelected background color.
	 */
	public Color getDisabledSelectedBackground() {
		return disabledSelectedBackground;
	}
	
	/**
	 * Sets the foreground color that is painted when the button is disabledSelected.
	 * Setting the color to null, changes the color to the default foreground color.
	 * 
	 * @param color the disabledSelected foreground color.
	 */
	public void setDisabledSelectedForeground( Color color) {
		disabledSelectedForeground = color;
	}
	
	/**
	 * Returns the foreground color that is painted when the button is disabledSelected.
	 * 
	 * @return the disabledSelected foreground color.
	 */
	public Color getDisabledSelectedForeground() {
		return disabledSelectedForeground;
	}

	/**
	 * Sets the border that is painted when the button is disabledSelected.
	 * Setting the border to null, changes the border to the default border.
	 * 
	 * @param border the disabledSelected border.
	 */
	public void setDisabledSelectedBorder( Border border) {
		disabledSelectedBorder = border;
	}

	/**
	 * Returns the border that is painted when the button is disabledSelected.
	 * 
	 * @return the disabledSelected border.
	 */
	public Border getDisabledSelectedBorder() {
		return disabledSelectedBorder;
	}

	/**
	 * Sets the Font that is painted when the button is disabledSelected.
	 * Setting the Font to null, changes the font to the default font.
	 * 
	 * @param font the disabledSelected font.
	 */
	public void setDisabledSelectedFont( Font font) {
		disabledSelectedFont = font;
	}

	/**
	 * Returns the font that is painted when the button is disabledSelected.
	 * 
	 * @return the disabledSelected font.
	 */
	public Font getDisabledSelectedFont() {
		return disabledSelectedFont;
	}
	
/*
 * ROLLOVER METHODS
 */

	/**
	 * Sets the background color that is painted when the button is rollover.
	 * Setting the color to null, changes the color to the default background color.
	 * 
	 * @param color the rollover background color.
	 */
	public void setRolloverBackground( Color color) {
		rolloverBackground = color;
	}

	/**
	 * Returns the background color that is painted when the button is rollover.
	 * 
	 * @return the rollover background color.
	 */
	public Color getRolloverBackground() {
		return rolloverBackground;
	}
	
	/**
	 * Sets the foreground color that is painted when the button is rollover.
	 * Setting the color to null, changes the color to the default foreground color.
	 * 
	 * @param color the rollover foreground color.
	 */
	public void setRolloverForeground( Color color) {
		rolloverForeground = color;
	}
	
	/**
	 * Returns the foreground color that is painted when the button is rollover.
	 * 
	 * @return the rollover foreground color.
	 */
	public Color getRolloverForeground() {
		return rolloverForeground;
	}

	/**
	 * Sets the border that is painted when the button is rollover.
	 * Setting the border to null, changes the border to the default border.
	 * 
	 * @param border the rollover border.
	 */
	public void setRolloverBorder( Border border) {
		rolloverBorder = border;
	}

	/**
	 * Returns the border that is painted when the button is rollover.
	 * 
	 * @return the rollover border.
	 */
	public Border getRolloverBorder() {
		return rolloverBorder;
	}

	/**
	 * Sets the Font that is painted when the button is rollover.
	 * Setting the Font to null, changes the font to the default font.
	 * 
	 * @param font the rollover font.
	 */
	public void setRolloverFont( Font font) {
		rolloverFont = font;
	}

	/**
	 * Returns the font that is painted when the button is rollover.
	 * 
	 * @return the rollover font.
	 */
	public Font getRolloverFont() {
		return rolloverFont;
	}
	
/*
 * ROLLOVER SELECTED METHODS
 */

	/**
	 * Sets the background color that is painted when the button is rolloverSelected.
	 * Setting the color to null, changes the color to the default background color.
	 * 
	 * @param color the rolloverSelected background color.
	 */
	public void setRolloverSelectedBackground( Color color) {
		rolloverSelectedBackground = color;
	}

	/**
	 * Returns the background color that is painted when the button is rolloverSelected.
	 * 
	 * @return the rolloverSelected background color.
	 */
	public Color getRolloverSelectedBackground() {
		return rolloverSelectedBackground;
	}
	
	/**
	 * Sets the foreground color that is painted when the button is rolloverSelected.
	 * Setting the color to null, changes the color to the default foreground color.
	 * 
	 * @param color the rolloverSelected foreground color.
	 */
	public void setRolloverSelectedForeground( Color color) {
		rolloverSelectedForeground = color;
	}
	
	/**
	 * Returns the foreground color that is painted when the button is rolloverSelected.
	 * 
	 * @return the rolloverSelected foreground color.
	 */
	public Color getRolloverSelectedForeground() {
		return rolloverSelectedForeground;
	}

	/**
	 * Sets the border that is painted when the button is rolloverSelected.
	 * Setting the border to null, changes the border to the default border.
	 * 
	 * @param border the rolloverSelected border.
	 */
	public void setRolloverSelectedBorder( Border border) {
		rolloverSelectedBorder = border;
	}

	/**
	 * Returns the border that is painted when the button is rolloverSelected.
	 * 
	 * @return the rolloverSelected border.
	 */
	public Border getRolloverSelectedBorder() {
		return rolloverSelectedBorder;
	}

	/**
	 * Sets the Font that is painted when the button is rolloverSelected.
	 * Setting the Font to null, changes the font to the default font.
	 * 
	 * @param font the rolloverSelected font.
	 */
	public void setRolloverSelectedFont( Font font) {
		rolloverSelectedFont = font;
	}

	/**
	 * Returns the font that is painted when the button is rolloverSelected.
	 * 
	 * @return the rolloverSelected font.
	 */
	public Font getRolloverSelectedFont() {
		return rolloverSelectedFont;
	}

/*
 * FOCUSED METHODS
 */

	/**
	 * Sets the background color that is painted when the button is focused.
	 * Setting the color to null, changes the color to the default background color.
	 * 
	 * @param color the focused background color.
	 */
	public void setFocusedBackground( Color color) {
		focusedBackground = color;
	}

	/**
	 * Returns the background color that is painted when the button is focused.
	 * 
	 * @return the focused background color.
	 */
	public Color getFocusedBackground() {
		return focusedBackground;
	}
	
	/**
	 * Sets the foreground color that is painted when the button is focused.
	 * Setting the color to null, changes the color to the default foreground color.
	 * 
	 * @param color the focused foreground color.
	 */
	public void setFocusedForeground( Color color) {
		focusedForeground = color;
	}
	
	/**
	 * Returns the foreground color that is painted when the button is focused.
	 * 
	 * @return the focused foreground color.
	 */
	public Color getFocusedForeground() {
		return focusedForeground;
	}

	/**
	 * Sets the border that is painted when the button is focused.
	 * Setting the border to null, changes the border to the default border.
	 * 
	 * @param border the focused border.
	 */
	public void setFocusedBorder( Border border) {
		focusedBorder = border;
	}

	/**
	 * Returns the border that is painted when the button is focused.
	 * 
	 * @return the focused border.
	 */
	public Border getFocusedBorder() {
		return focusedBorder;
	}

	/**
	 * Sets the Font that is painted when the button is focused.
	 * Setting the Font to null, changes the font to the default font.
	 * 
	 * @param font the focused font.
	 */
	public void setFocusedFont( Font font) {
		focusedFont = font;
	}

	/**
	 * Returns the font that is painted when the button is focused.
	 * 
	 * @return the focused font.
	 */
	public Font getFocusedFont() {
		return focusedFont;
	}

	/**
	 * Returns the icon that is painted when the button is focused.
	 * 
	 * @return the focused icon.
	 */
	public Icon getFocusedIcon() {
		return focusedIcon;
	}

	/**
	 * Sets the Icon that is painted when the button is focused.
	 * Setting the Icon to null, changes the icon to the default icon.
	 * 
	 * @param icon the focused icon.
	 */
	public void setFocusedIcon( Icon icon) {
		focusedIcon = icon;
	}

/*
 * FOCUSED SELECTED METHODS
 */

	/**
	 * Sets the background color that is painted when the button is focusedSelected.
	 * Setting the color to null, changes the color to the default background color.
	 * 
	 * @param color the focusedSelected background color.
	 */
	public void setFocusedSelectedBackground( Color color) {
		focusedSelectedBackground = color;
	}

	/**
	 * Returns the background color that is painted when the button is focusedSelected.
	 * 
	 * @return the focusedSelected background color.
	 */
	public Color getFocusedSelectedBackground() {
		return focusedSelectedBackground;
	}
	
	/**
	 * Sets the foreground color that is painted when the button is focusedSelected.
	 * Setting the color to null, changes the color to the default foreground color.
	 * 
	 * @param color the focusedSelected foreground color.
	 */
	public void setFocusedSelectedForeground( Color color) {
		focusedSelectedForeground = color;
	}
	
	/**
	 * Returns the foreground color that is painted when the button is focusedSelected.
	 * 
	 * @return the focusedSelected foreground color.
	 */
	public Color getFocusedSelectedForeground() {
		return focusedSelectedForeground;
	}

	/**
	 * Sets the border that is painted when the button is focusedSelected.
	 * Setting the border to null, changes the border to the default border.
	 * 
	 * @param border the focusedSelected border.
	 */
	public void setFocusedSelectedBorder( Border border) {
		focusedSelectedBorder = border;
	}

	/**
	 * Returns the border that is painted when the button is focusedSelected.
	 * 
	 * @return the focusedSelected border.
	 */
	public Border getFocusedSelectedBorder() {
		return focusedSelectedBorder;
	}

	/**
	 * Sets the Font that is painted when the button is focusedSelected.
	 * Setting the Font to null, changes the font to the default font.
	 * 
	 * @param font the focusedSelected font.
	 */
	public void setFocusedSelectedFont( Font font) {
		focusedSelectedFont = font;
	}

	/**
	 * Returns the font that is painted when the button is focusedSelected.
	 * 
	 * @return the focusedSelected font.
	 */
	public Font getFocusedSelectedFont() {
		return focusedSelectedFont;
	}

	/**
	 * Returns the icon that is painted when the button is focusedSelected.
	 * 
	 * @return the focusedSelected icon.
	 */
	public Icon getFocusedSelectedIcon() {
		return focusedSelectedIcon;
	}

	/**
	 * Sets the Icon that is painted when the button is focusedSelected.
	 * Setting the Icon to null, changes the icon to the default icon.
	 * 
	 * @param icon the focusedSelected icon.
	 */
	public void setFocusedSelectedIcon( Icon icon) {
		focusedSelectedIcon = icon;
	}

	/**
	 * Sets the color for the rectangle that is painted when the button is 
	 * in focus. Setting the color to null, disables the painting of the 
	 * rectangle. To be compatible with the JButton, the painting of the 
	 * rectangle is also disabled when the isFocusPainted method returns false.
	 * 
	 * @param color the focused Rectangle color.
	 */
	public void setFocusedRectangleColor( Color color) {
		focusedRectangleColor = color;
	}
	
	/**
	 * Returns the color for the rectangle that is painted when the button is 
	 * in focus.
	 * 
	 * @return the focused Rectangle color.
	 */
	public Color getFocusedRectangleColor() {
		return focusedRectangleColor;
	}

	/**
	 * Sets the mode for the rectangle that is painted when the button is 
	 * in focus. The mode can be FOCUS_NONE, no focus rectangle is painted,
	 * FOCUS_AROUND_TEXT, Only a rectangle around the text is painted,
	 * FOCUS_AROUND_ICON, Only a rectangle around the icon is painted,
	 * FOCUS_AROUND_TEXT_AND_ICON, a rectangle around the icon and the text 
	 * will be painted. The default value is to have the focus around the 
	 * icon and the text.
	 * 
	 * @param mode the focused mode.
	 */
	public void setFocusedRectangleMode( int mode) {
		focusedRectangleMode = mode;
	}
	
	/**
	 * Returns the mode for the rectangle that is painted when the button is 
	 * in focus. The mode can be FOCUS_NONE, no focus rectangle is painted,
	 * FOCUS_AROUND_TEXT, Only a rectangle around the text is painted,
	 * FOCUS_AROUND_ICON, Only a rectangle around the icon is painted,
	 * FOCUS_AROUND_TEXT_AND_ICON, a rectangle around the icon and the text 
	 * will be painted. The default value is to have the focus around the 
	 * icon and the text.
	 * 
	 * @return the focused mode.
	 */
	public int getFocusedRectangleMode() {
		return focusedRectangleMode;
	}

	/**
	 * Enables the double click, this makes the button no longer returning an
	 * action event when the button has been clicked, only when double clicked.
	 * 
	 * @param enable the double click value, the value is enabled when true.
	 */
	public void setDoubleClick( boolean enable) {
		doubleClickEnabled = enable;
	}

	/**
	 * Call to find out if the double click has been enabled.
	 * 
	 * @return the double click value, the value is enabled when true.
	 */
	public boolean isDoubleClick() {
		return doubleClickEnabled;
	}

	/**
	 * Sets the maximum number of possible lines on this button.
	 * If there is text in the lines the lines will be painted and
	 * included in the preferred-height calculations.
	 * 
	 * @param lines the number of lines.
	 *
	 * @exception java.lang.IllegalArgumentException if <code>lines</code>
	 *            is smaller than the minimum number of lines.
	 */
	public void setMaximumLines( int lines) {
		if ( lines > 0) {
			if ( lines >= minLines) {
				this.maxLines = lines;
			} else {
				this.maxLines = minLines;

				throw new IllegalArgumentException( "Maximum number of lines < Minimum : "+lines+" < "+minLines);
			}
		} else {
			this.maxLines = 1;
		}
	}

	/**
	 * Get the maximum number of lines used for the button text.
	 * 
	 * @return the maximum number of lines.
	 */
	public int getMaximumLines() {
		return maxLines;
	}
	
	/**
	 * Sets the minimum number of possible lines on this button.
	 * The lines will always be included in the preferred-height 
	 * calculations, even if they don't have text.
	 * 
	 * @param lines the number of lines.
	 *
	 * @exception java.lang.IllegalArgumentException if <code>lines</code>
	 *            is bigger than the maximum number of lines.
	 */
	public void setMinimumLines( int lines) {
		if ( lines > 0) {
			if ( lines <= maxLines) {
				this.minLines = lines;
			} else {
				this.minLines = maxLines;
	
				throw new IllegalArgumentException( "Minimum number of lines > Maximum : "+lines+" > "+maxLines);
			}
		} else {
			this.minLines = 1;
		}
	}

	/**
	 * Get the minimum number of lines used for the button text.
	 * 
	 * @return the minimum number of lines.
	 */
	public int getMinimumLines() {
		return minLines;
	}

	/**
	 * Set the number of lines used for the button text.
	 * This forces the preferred height of the button to be 
	 * as high as the height of the lines of text.
	 *
	 * This is equal to setting the maximum and minimum number of 
	 * lines to the same value.
	 * 
	 * @param lines the number of lines.
	 */
	public void setLines( int lines) {
		if ( lines > 0) {
			this.minLines = lines;
			this.maxLines = lines;
		} else {
			this.minLines = 1;
			this.maxLines = 1;
		}
	}
} 

