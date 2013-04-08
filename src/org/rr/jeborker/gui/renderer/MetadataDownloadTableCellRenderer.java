package org.rr.jeborker.gui.renderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.rr.common.swing.SwingUtils;
import org.rr.commons.collection.VolatileHashMap;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.layout.VerticalLayout;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.ListUtils;
import org.rr.jeborker.remote.metadata.MetadataDownloadEntry;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

public class MetadataDownloadTableCellRenderer extends JPanel implements TableCellRenderer, Serializable  {

	private static final long serialVersionUID = -4684790158985895647L;
	
	private static final int LEFT_WIDTH = 80;
	
	private JLabel imagelabel;
	
	private JPanel mainPanel;
	
	public MetadataDownloadTableCellRenderer() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{LEFT_WIDTH, 0, 0};
		gridBagLayout.rowHeights = new int[]{344, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		imagelabel = new JLabel();
		imagelabel.setHorizontalAlignment(JLabel.LEFT);
		GridBagConstraints gbc_imagelabel = new GridBagConstraints();
		gbc_imagelabel.insets = new Insets(0, 0, 0, 5);
		gbc_imagelabel.fill = GridBagConstraints.BOTH;
		gbc_imagelabel.gridx = 0;
		gbc_imagelabel.gridy = 0;
		add(imagelabel, gbc_imagelabel);
		
		mainPanel = new JPanel();
		mainPanel.setOpaque(true);
		GridBagConstraints gbc_mainPanel = new GridBagConstraints();
		gbc_mainPanel.fill = GridBagConstraints.BOTH;
		gbc_mainPanel.gridx = 1;
		gbc_mainPanel.gridy = 0;
		add(mainPanel, gbc_mainPanel);
		mainPanel.setLayout(new VerticalLayout(0, VerticalLayout.LEFT));
		
		
		Color color = SwingUtils.getSelectionBackgroundColor();
		selectedBgColor = SwingUtils.getBrighterColor(color, 20);		
		
//		selectedBgColor = UIManager.getColor("TextField.selectionBackground");	
		selectedFgColor = SwingUtils.getSelectionForegroundColor();
		
		//workaround for a swing bug. The first time, the editor is used, the 
		//ui color instance draws the wrong color but have the right rgb values.
		color = SwingUtils.getBackgroundColor();
		bgColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
		
		color = SwingUtils.getForegroundColor();
		fgColor = new Color(color.getRed(), color.getGreen(), color.getBlue());	
	}

	protected final Color selectedBgColor;
	
	protected final Color selectedFgColor;

	protected final Color bgColor;
	
	protected final Color fgColor;
	
	private static final VolatileHashMap<String, ImageIcon> thumbnailCache = new VolatileHashMap<String, ImageIcon>(20, 20);

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		final Component tableCellComponent = this.getTableCellComponent(table, (MetadataDownloadEntry) value, isSelected, hasFocus, row, column);
		if(isSelected) {
			this.setBackground(selectedBgColor);	
			this.setForeground(selectedFgColor);
		} else {		
			this.setBackground(SwingUtils.getBackgroundColor());
			this.setForeground(SwingUtils.getForegroundColor());			
		}
		
		return tableCellComponent;
	}	

	Component getTableCellComponent(JTable table, MetadataDownloadEntry entry, boolean isSelected, boolean hasFocus, int row, final int column) {
		final int addEntryLabels = this.addEntryLabels(entry);
		final int rowHeight = Math.max(80, addEntryLabels * 25);
		table.setRowHeight(row, rowHeight);
		ImageIcon imageIconCover = getImageIconCover(table, entry, new Dimension(LEFT_WIDTH, rowHeight));
		this.imagelabel.setIcon(imageIconCover);
//		this.imagelabel.setPreferredSize(new Dimension(this.imagelabel.getPreferredSize().width, rowHeight));
		
		
		
		return this;
	}
	
	/**
	 * Add a component for each valid value in the {@link MetadataDownloadEntry}. In common author and title
	 * are two of the values to be present and created.
	 * @return The number of components that was created and added. 
	 */
	private int addEntryLabels(MetadataDownloadEntry entry) {
		int result = 0;
		mainPanel.removeAll();

		JComponent metadataEntryAuthorView = getMetadataEntryView("Authors", ListUtils.join(entry.getAuthors(), ", "));
		mainPanel.add(metadataEntryAuthorView);
		
		return result;
	}
	
	private JComponent getMetadataEntryView(String labelText, String value) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(3, 0));
		
		JLabel lblValue = new JLabel(value);
		panel.add(lblValue, BorderLayout.CENTER);
		
		JLabel lblText = new JLabel(labelText + ": ");
		panel.add(lblText, BorderLayout.WEST);
		
		JCheckBox check = new JCheckBox();
		check.setSelected(true);
		panel.add(check, BorderLayout.EAST);
		
		return panel;
	}
	

	/**
	 * Gets the thumbnail image to be displayed in the renderer.
	 * @param table The JTable instance.
	 * @param entry The item to be rendered.
	 * @return The thumbnail image to be displayed in the renderer.
	 */
	private ImageIcon getImageIconCover(final JTable table, final MetadataDownloadEntry entry, Dimension dim) {
		if(entry == null) {
			return null;
		}
		
		byte[] coverThumbnail = entry.getImage();
		if(entry != null && coverThumbnail != null && coverThumbnail.length > 0) {
			final String coverThumbnailCRC32 = String.valueOf(CommonUtils.calculateCrc(coverThumbnail));
			if(thumbnailCache.containsKey(coverThumbnailCRC32)) {
				return thumbnailCache.get(coverThumbnailCRC32);
			}
			
			try {
				final byte[] coverData = coverThumbnail;
				if(coverData!=null) {
					final IResourceHandler virtualImageResourceLoader = ResourceHandlerFactory.getVirtualResourceHandler("TableCellRendererImageData", coverData);
					final IImageProvider imageProvider = ImageProviderFactory.getImageProvider(virtualImageResourceLoader);
					final BufferedImage image = imageProvider.getImage();
					if(image != null) {	
						BufferedImage scaleToMatch = ImageUtils.scaleToMatch(imageProvider.getImage(), dim, true);
						ImageIcon imageIcon = new ImageIcon(scaleToMatch);
						thumbnailCache.put(coverThumbnailCRC32, imageIcon);
						return imageIcon;
					} else {
						return null;
					}
				}
			} catch (Exception e) {
				LoggerFactory.logInfo(this, "Could not render thumbnail", e);
			} 
		}
		return null;
	}
	
}
