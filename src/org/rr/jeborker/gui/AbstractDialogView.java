package org.rr.jeborker.gui;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.rr.commons.swing.SwingUtils;
import org.rr.commons.swing.layout.EqualsLayout;

public abstract class AbstractDialogView extends JDialog {

	private JFrame mainWindow;

	private List<JButton> buttons;

	public AbstractDialogView(JFrame mainWindow) {
		super(mainWindow);
	}

	protected JFrame getMainWindow() {
		return this.mainWindow;
	}

	protected void initialize() {
		SwingUtils.setEscapeWindowAction(this, getAbortActionListener());
		if(mainWindow != null) {
			SwingUtils.centerOnWindow(mainWindow, this);
		}

		Dimension defaultDialogSize = getDefaultDialogSize();
		if(defaultDialogSize != null) {
			setSize(defaultDialogSize);
		}

		String dialogTitle = getDialogTitle();
		if(dialogTitle != null) {
			setTitle(dialogTitle);
		}

		Dimension dialogMinimumSize = getDialogMinimumSize();
		if(dialogMinimumSize != null) {
			setMinimumSize(dialogMinimumSize);
		}
	}

	private ActionListener getAbortActionListener() {
		for(int i = 0; i < getBottomButtonCount(); i++) {
			if(isAbortAction(i)) {
				return getBottomButtonAction(i);
			}
		}
		return null;
	}

	/**
	 * Creates a panel with generated buttons using the {@link #getBottomButtonLabel(int)} and
	 * {@link #getBottomButtonAction(int)} methods.
	 * @return The desired panel.
	 */
	protected JPanel createBottomButtonPanel() {
		int bottomButtonCount = getBottomButtonCount();
		buttons = new ArrayList<JButton>(bottomButtonCount);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new EqualsLayout(3));
		for(int i = 0; i < bottomButtonCount; i++) {
			JButton btn = new JButton(getBottomButtonLabel(i));
			btn.addActionListener(getBottomButtonAction(i));
			bottomPanel.add(btn);
			buttons.add(btn);
			if(isDefaultButtonAction(i)) {
				getRootPane().setDefaultButton(btn);
			}
		}

		return bottomPanel;
	}

	protected JButton getButtonAt(int index) {
		return buttons.get(index);
	}

	/**
	 * @return The number of buttons for the bottom panel.
	 */
	protected abstract int getBottomButtonCount();

	/**
	 * Get the {@link ActionListener} for the button of the given index.
	 * @param index The index for the button which should get the returned {@link ActionListener}.
	 * @return The {@link ActionListener} or <code>null</code> if there is no action for the given index.
	 */
	protected abstract ActionListener getBottomButtonAction(int index);

	/**
	 * Get the label for the button of the given index.
	 * @param index The index for the button which should get the returned label.
	 * @return The label or <code>null</code> if there is no label for the given index.
	 */
	protected abstract String getBottomButtonLabel(int index);

	/**
	 * Tells if the action with the given index is the abort action.
	 * @param idx Index which is supposed to be a abort action.
	 * @return <code>true</code> if the action at the given index is the abort action.
	 */
	protected abstract boolean isAbortAction(int idx);

	/**
	 * Get the default dimension for the dialog window.
	 * @return The desired default dialog window size. <code>null</code> if no default size is desired.
	 */
	protected abstract Dimension getDefaultDialogSize();

	/**
	 * Get the title for the dialog window.
	 * @return The desired title or <code>null</code> if no title should be set.
	 */
	protected abstract String getDialogTitle();

	/**
	 * Get the minimum size for the dialog window
	 * @return The desired dialog min size or <code>null</code> if no minimum should be set.
	 */
	protected abstract Dimension getDialogMinimumSize();

	/**
	 * Tells if the button at the given index should be the default button.
	 * @param idx The index for the button to be tested as default button.
	 * @return <code>true</code> if the button at the given index should be the default button.
	 */
	protected abstract boolean isDefaultButtonAction(int idx);
}
