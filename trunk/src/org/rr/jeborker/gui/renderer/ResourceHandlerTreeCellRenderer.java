package org.rr.jeborker.gui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.rr.common.swing.SwingUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.gui.MainMenuBarController;
import org.rr.jeborker.gui.action.ActionUtils;
import org.rr.jeborker.gui.model.BasePathTreeModel;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

public class ResourceHandlerTreeCellRenderer extends JPanel implements TreeCellRenderer {

	private Color selectedBgColor;
	
	private Color selectedFgColor;
	
	private JLabel label;
	
	private JTree tree;
	
	protected JCheckBox checkbox;
	
	private Object value;
	
	private ItemListener checkboxItemListener;

	private boolean isDropCell;
	
	private ImageIcon eyesVisible;
	private ImageIcon eyesInvisible;
	
	public ResourceHandlerTreeCellRenderer(JTree tree) {
		this.tree = tree;
		selectedBgColor = SwingUtils.getSelectionBackgroundColor();
		selectedFgColor = SwingUtils.getSelectionForegroundColor();
		setOpaque(false);
		eyesVisible = ImageResourceBundle.getResourceAsImageIcon("eyes_blue_16.png");
		eyesInvisible = ImageResourceBundle.getResourceAsImageIcon("eyes_gray_16.png");
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 20, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		checkbox = new JCheckBox();
		GridBagConstraints gbc_chckbxCheck = new GridBagConstraints();
		gbc_chckbxCheck.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxCheck.gridx = 0;
		gbc_chckbxCheck.gridy = 0;
		add(checkbox, gbc_chckbxCheck);
		checkbox.addItemListener(getCheckboxItemListener());
		
		checkbox.setRolloverEnabled(false);
		checkbox.setIcon(eyesInvisible); //unselected icon
		checkbox.setSelectedIcon(eyesVisible); //selected icon
		
//		checkbox.setRolloverIcon(eyesVisible); //rollover unselected
		
		checkbox.setDisabledIcon(eyesInvisible);
		checkbox.setDisabledSelectedIcon(eyesInvisible);
		
//		checkbox.setRolloverSelectedIcon(eyesInvisible); //rollover selected
		
		label = new JLabel();
		label.setOpaque(false);
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.fill = GridBagConstraints.BOTH;
		gbc_label.gridx = 1;
		gbc_label.gridy = 0;
		add(label, gbc_label);
	}
	
	private ItemListener getCheckboxItemListener() {
		if(this.checkboxItemListener == null) {
			this.checkboxItemListener = new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					TreePath selectionPath = tree.getSelectionPath();
					Object lastPathComponent = selectionPath.getLastPathComponent();
					String resourceName = null;
					if (lastPathComponent instanceof IResourceHandler) {
						resourceName = ((IResourceHandler) lastPathComponent).toString();
					} else if(lastPathComponent instanceof BasePathTreeModel.PathNode){
						resourceName = ((BasePathTreeModel.PathNode)lastPathComponent).getPathResource().toString();
					}		
					
					if(resourceName != null) {
						ActionUtils.toggleBasePathVisibility(resourceName);
					}
					
				}
			};
		}
		return this.checkboxItemListener;
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		if (value instanceof IResourceHandler) {
			String resourceName = ((IResourceHandler) value).getName();
			label.setText(resourceName);
			setCheckboxCheck(((IResourceHandler) value));
			this.value = value;
		} else if(value instanceof BasePathTreeModel.PathNode){
			IResourceHandler pathResource = ((BasePathTreeModel.PathNode)value).getPathResource();
			label.setText(pathResource.getName());
			setCheckboxCheck(pathResource);
			this.value = pathResource;
		} else {
			label.setText(StringUtils.toString(value));
			checkbox.setVisible(false);
		}
		try {
			JTree.DropLocation dropLocation = tree.getDropLocation();
	        if (dropLocation != null
	                && dropLocation.getChildIndex() == -1
	                && tree.getRowForPath(dropLocation.getPath()) == row) {

				this.setBackground(selectedBgColor);
				this.setForeground(selectedFgColor);
				label.setForeground(selectedFgColor);
	            isDropCell = true;
	        } else if (selected) {
				this.setBackground(selectedBgColor);
				this.setForeground(selectedFgColor);
				label.setForeground(selectedFgColor);
				isDropCell = false;
			} else {
				this.setBackground(SwingUtils.getBackgroundColor());
				this.setForeground(SwingUtils.getForegroundColor());
				label.setForeground(SwingUtils.getForegroundColor());
				isDropCell = false;
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to render Jtree row", e);
		}

		return this;
	}

	private void setCheckboxCheck(IResourceHandler pathResourceHandler) {
		final String pathResourceString = pathResourceHandler.toString();
		final List<String> basePaths = JeboorkerPreferences.getBasePath();
		checkbox.removeItemListener(getCheckboxItemListener());
		try {
			if(basePaths.contains(pathResourceString)) {
				final List<String> basePath = MainMenuBarController.getController().getHiddenBasePathEntries();
				if(basePath.contains(pathResourceString)) {
					//hidden
					checkbox.setSelected(false);
				} else {
					//checked
					checkbox.setSelected(true);
				}
				checkbox.setVisible(true);
			} else {
				//not a base path
				checkbox.setSelected(false);
				checkbox.setVisible(false);
			}
		} finally {
			checkbox.addItemListener(getCheckboxItemListener());
		}
	}

	public Object getValue() {
		return value;
	}

	public boolean isDropCell() {
		return isDropCell;
	}


}