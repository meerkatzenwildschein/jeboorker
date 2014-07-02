package org.rr.jeborker.gui.cell;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.swing.components.JRCheckBox;
import org.rr.commons.swing.components.tree.TreeUtil;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.app.BasePathList;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.gui.MainMenuBarController;
import org.rr.jeborker.gui.action.ActionUtils;
import org.rr.jeborker.gui.model.BasePathTreeModel;
import org.rr.jeborker.gui.model.FileSystemNode;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

public class BasePathTreeCellRenderer extends JPanel implements TreeCellRenderer {

	private static final Color filteredForegroundColor = Color.DARK_GRAY.brighter().brighter();

	private JLabel label;

	private JTree tree;

	private JRCheckBox checkbox;

	private Object value;

	private ItemListener checkboxItemListener;

	private boolean isDropCell;

	private Icon eyesVisible;

	private Icon eyesInvisible;

	private Icon eyesTreeState;

	private Font labelNormalFont;

	private Font labelBoldFont;

	public BasePathTreeCellRenderer(JTree tree) {
		this.tree = tree;
		setOpaque(false);
		eyesVisible = ImageResourceBundle.getResourceAsImageIcon("eyes_blue_16.png");
		eyesInvisible = ImageResourceBundle.getResourceAsImageIcon("eyes_gray_16.png");
		eyesTreeState = ImageResourceBundle.getResourceAsImageIcon("eyes_between_16.png");

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 20, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		checkbox = new JRCheckBox();
		GridBagConstraints gbc_chckbxCheck = new GridBagConstraints();
		gbc_chckbxCheck.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxCheck.gridx = 0;
		gbc_chckbxCheck.gridy = 0;
		add(checkbox, gbc_chckbxCheck);
		checkbox.addItemListener(getCheckboxItemListener());

		checkbox.setRolloverEnabled(false);
		checkbox.setTriStateIcon(eyesTreeState);
		checkbox.setIcon(eyesInvisible); //unselected icon
		checkbox.setSelectedIcon(eyesVisible); //selected icon

//		checkbox.setRolloverIcon(eyesVisible); //rollover unselected

		checkbox.setDisabledIcon(eyesInvisible);
		checkbox.setDisabledSelectedIcon(eyesInvisible);
		checkbox.setOpaque(false);
//		checkbox.setRolloverSelectedIcon(eyesInvisible); //rollover selected

		label = new JLabel();
		setupLabelFont();
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
					final TreePath selectionPath = tree.getSelectionPath();

					if(selectionPath != null) {
						Object lastPathComponent = selectionPath.getLastPathComponent();
						String resourceName = null;
						if (lastPathComponent instanceof IResourceHandler) {
							resourceName = ((IResourceHandler) lastPathComponent).toString();
						} else if(lastPathComponent instanceof FileSystemNode){
							resourceName = ((FileSystemNode)lastPathComponent).getResource().toString();
						}

						if(resourceName != null) {
							ActionUtils.toggleBasePathVisibility(resourceName);
						} else {
							final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
							final BasePathList basePaths = preferenceStore.getBasePath();
							final boolean isVisible = basePaths.isAllPathElementsVisible();
							for(String basePath : basePaths) {
								ActionUtils.setBasePathVisibility(basePath, !isVisible);
							}
						}

						((DefaultTreeModel)tree.getModel()).reload((TreeNode) selectionPath.getLastPathComponent());
					}
				}
			};
		}
		return this.checkboxItemListener;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		final int rowCount = tree.getRowCount();
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		final BasePathList basePaths = preferenceStore.getBasePath();
		IResourceHandler pathResource;

		//font change may happens with look and feel change
		if(!label.getFont().equals(this.labelNormalFont) && !label.getFont().equals(this.labelBoldFont)) {
			setupLabelFont();
		}

		if (value instanceof IResourceHandler) {
			pathResource = (IResourceHandler) value;
			String resourceName = pathResource.getName();
			label.setText(resourceName);
			setCheckboxCheck(pathResource, basePaths);
			this.value = value;
		} else if(value instanceof FileSystemNode){
			pathResource = ((FileSystemNode)value).getResource();
			label.setText(pathResource.getName());
			setCheckboxCheck(pathResource, basePaths);
			this.value = pathResource;
		} else {
			pathResource = ResourceHandlerFactory.getResourceHandler(StringUtils.toString(value));
			label.setText(StringUtils.toString(value));
			checkbox.setVisible(true);
			setCheckboxCheck(pathResource, basePaths);
			this.value = null;
		}

		//setup label dimensions
		Dimension textDimension = SwingUtils.getTextDimension(label.getText(), label.getFont());
		textDimension.width += 5;
		label.setSize(textDimension);
		label.setPreferredSize(textDimension);

		if(pathResource != null) {
			String pathresourceString = pathResource.toString();
			label.setEnabled(true);
			for(String basePath : basePaths) {
				if(pathresourceString.startsWith(basePath)) {
					boolean basePathVisible = preferenceStore.isBasePathVisible(basePath);
					if(!basePathVisible) {
						label.setEnabled(false);
					}
				}
			}
		}

		try {
			if(label.getFont() != labelNormalFont) {
				//reset font
				label.setFont(labelNormalFont);
			}
			JTree.DropLocation dropLocation = tree.getDropLocation();
	        if (dropLocation != null
	                && dropLocation.getChildIndex() == -1
	                && tree.getRowForPath(dropLocation.getPath()) == row) {

				this.setBackground(SwingUtils.getSelectionBackgroundColor());
				this.setForeground(SwingUtils.getSelectionForegroundColor());
				label.setForeground(SwingUtils.getSelectionForegroundColor());
	            isDropCell = true;
	        } else if (selected) {
				this.setBackground(SwingUtils.getSelectionBackgroundColor());

				this.setForeground(SwingUtils.getSelectionForegroundColor());
				label.setForeground(SwingUtils.getSelectionForegroundColor());
				isDropCell = false;
			} else {
				TreePath filterTreePath = ((BasePathTreeModel)tree.getModel()).getFilterTreePath();
				if(filterTreePath == null) {
					//no filter set
					label.setForeground(SwingUtils.getForegroundColor());
				} else {
					TreePath rowPath = tree.getPathForRow(row);
					if(filterTreePath.equals(rowPath)) {
						label.setForeground(SwingUtils.getForegroundColor());
					} else if(TreeUtil.isChild(filterTreePath, rowPath)) {
						label.setForeground(SwingUtils.getForegroundColor());
					} else {
						label.setForeground(filteredForegroundColor);
						if(rowCount == row + 1) {
							//set the last tree entry bold if there is a click filter.
							label.setFont(labelBoldFont);
						}
					}
				}
				this.setBackground(SwingUtils.getBackgroundColor());
				this.setForeground(SwingUtils.getForegroundColor());

				isDropCell = false;
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to render Jtree row", e);
		}

		return this;
	}



	private void setCheckboxCheck(IResourceHandler pathResourceHandler, final BasePathList basePaths) {
		final String pathResourceString = StringUtils.toString(pathResourceHandler);
		checkbox.removeItemListener(getCheckboxItemListener());
		try {
			checkbox.setShowTriStateIcon(false);
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
			} else if(pathResourceString.indexOf(File.separatorChar) != -1) {
				//a path but not a base path
				checkbox.setSelected(false);
				checkbox.setVisible(false);
			} else {
				//Some other item that have an eye icon.
				boolean allPathElementsVisible = basePaths.isAllPathElementsVisible();
				boolean noPathElementsVisible = basePaths.isNoPathElementsVisible();
				if(!allPathElementsVisible && !noPathElementsVisible) {
					checkbox.setShowTriStateIcon(true);
					checkbox.setSelected(false);
				} else if(allPathElementsVisible) {
					checkbox.setSelected(true);
				} else {
					checkbox.setSelected(false);
				}
				checkbox.setVisible(true);
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

	private void setupLabelFont() {
		this.labelNormalFont = label.getFont();
		this.labelBoldFont = new Font(this.labelNormalFont.getName(), Font.BOLD, this.labelNormalFont.getSize());
	}

}