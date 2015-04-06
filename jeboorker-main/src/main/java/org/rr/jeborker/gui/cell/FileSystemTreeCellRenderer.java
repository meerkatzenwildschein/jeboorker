package org.rr.jeborker.gui.cell;

import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.swing.icon.DecoratedIcon;
import org.rr.commons.swing.icon.TextIcon;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.gui.model.FileSystemNode;
import org.rr.jeborker.gui.resources.ImageResourceBundle;


public class FileSystemTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -7057675192468615801L;

	private static final Map<String, Icon> CACHE = new HashMap<>();

	public Component getTreeCellRendererComponent(final JTree tree, final Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if(isFileSystemNode(value)) {
			IResourceHandler file = ((FileSystemNode) value).getResource();
			if(isDirectory(file)) {
				if(isUserHome(file)) {
					setIcon(ImageResourceBundle.getResourceAsImageIcon("folder_home_16.png"));
				} else if(tree.isExpanded(row)) {
					setIcon(ImageResourceBundle.FOLDER_OPEN_16_ICON);
				} else {
					setIcon(ImageResourceBundle.FOLDER_CLOSE_16_ICON);
				}
			} else {
				setIcon(createFileIcon(file));
			}
		}

		return this;
	}

	private boolean isUserHome(IResourceHandler file) {
		return StringUtils.equals(file.toString(), System.getProperty("user.home"));
	}

	private boolean isDirectory(IResourceHandler file) {
		return file != null && file.isDirectoryResource();
	}

	private boolean isFileSystemNode(final Object value) {
		return value instanceof FileSystemNode;
	}

	private Icon createFileIcon(IResourceHandler file) {
		String fileExtension = StringUtils.right(file.getFileExtension(), 4);
		if(CACHE.containsKey(fileExtension)) {
			return CACHE.get(fileExtension);
		}
		return CACHE.put(fileExtension, new DecoratedIcon(ImageResourceBundle.FILE_16_ICON, createTextIcon(fileExtension)));
	}

	private Icon createTextIcon(String fileExtension) {
		return new TextIcon(16, 16, fileExtension, getTextIconFont(), TextIcon.Location.LOWER_RIGHT, new Insets(2, 2, 2, 2));
	}

	private Font getTextIconFont() {
		Font font = UIManager.getDefaults().getFont("Label.font");
		return new Font(font.getFontName(), Font.BOLD, 6);
	}
}