package org.rr.jeborker.gui.cell;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.rr.commons.collection.VolatileHashMap;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.swing.components.StarRater;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.HTMLEntityConverter;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.ReflectionFailureException;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.app.BasePathList;
import org.rr.jeborker.app.JeboorkerConstants;
import org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.IDBObject;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

public class EbookTableCellRenderer implements TableCellRenderer, Serializable  {

	private static final long serialVersionUID = -4684790158985895647L;
	
	class RendererComponent extends JPanel {
		
		public RendererComponent() {
			init();
		}
		
		private JLabel imageLabel;
		
		private JLabel firstLineLabel;
		
		private JLabel secondLineLabel;
		
		private JTextArea thirdLineTextArea;
		
		private JLabel dataFormatLabel;
		
		private StarRater starRater;
		
		private JPanel basePathColorIndicator;
		
		private boolean labelSetupComplete = false;
		
		public String toString() {
			return new ToStringBuilder(this)
					.append("label1", firstLineLabel.getText())
					.append("label2", secondLineLabel.getText())
					.toString();
		}
		
		public void init() {
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0};
			gridBagLayout.rowHeights = new int[]{0, 0, 0};
			gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
			gridBagLayout.rowWeights = new double[]{0.0, 1.0,  Double.MIN_VALUE};
			setLayout(gridBagLayout);
			
			imageLabel = new JLabel(EMPTY);
			imageLabel.setOpaque(false);
			imageLabel.setVerticalAlignment(SwingConstants.TOP);
			GridBagConstraints gbc_imageLabel = new GridBagConstraints();
			gbc_imageLabel.insets = new Insets(0, 0, 0, 0);
			gbc_imageLabel.gridheight = 5;
			gbc_imageLabel.gridx = 0;
			gbc_imageLabel.gridy = 1;
			add(imageLabel, gbc_imageLabel);
			
			firstLineLabel = new JLabel(EMPTY);
			firstLineLabel.setOpaque(false);
			firstLineLabel.setVerticalAlignment(SwingConstants.TOP);
			GridBagConstraints gbc_firstLineLabel = new GridBagConstraints();
			gbc_firstLineLabel.insets = new Insets(0, 0, 0, 0);
			gbc_firstLineLabel.anchor = GridBagConstraints.WEST;
			gbc_firstLineLabel.gridx = 1;
			gbc_firstLineLabel.gridy = 1;
			add(firstLineLabel, gbc_firstLineLabel);
			
			dataFormatLabel = new JLabel(EMPTY);
			dataFormatLabel.setOpaque(false);
			dataFormatLabel.setVerticalAlignment(SwingConstants.TOP);
			GridBagConstraints gbc_label = new GridBagConstraints();
			gbc_label.anchor = GridBagConstraints.NORTHEAST;
			gbc_label.insets = new Insets(2, 0, 0, 0);
			gbc_label.gridx = 2;
			gbc_label.gridy = 2;
			add(dataFormatLabel, gbc_label);
			
			starRater = new StarRater();
			starRater.setMinimumSize(new Dimension(85,27));
			GridBagConstraints gbc_starRater = new GridBagConstraints();
			gbc_starRater.insets = new Insets(3, 0, 0, 0);
			gbc_starRater.gridx = 2;
			gbc_starRater.gridy = 1;
			starRater.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if(starRater.getSelection() > 0) {
						MainController.getController().setRatingToSelectedEntry(starRater.getSelection() * 2);
					}
				}
				
			});
			add(starRater, gbc_starRater);
			
			secondLineLabel = new JLabel(EMPTY);
			secondLineLabel.setOpaque(false);
			secondLineLabel.setVerticalAlignment(SwingConstants.TOP);
			GridBagConstraints gbc_secondLineLabel = new GridBagConstraints();
			gbc_secondLineLabel.insets = new Insets(0, 0, 0, 0);
			gbc_secondLineLabel.gridwidth = 3;
			gbc_secondLineLabel.anchor = GridBagConstraints.WEST;
			gbc_secondLineLabel.gridx = 1;
			gbc_secondLineLabel.gridy = 2;
			add(secondLineLabel, gbc_secondLineLabel);
			
			thirdLineTextArea = new JTextArea(EMPTY);
			thirdLineTextArea.setOpaque(false);
			thirdLineTextArea.setLineWrap(true);
			thirdLineTextArea.setWrapStyleWord(true);
			thirdLineTextArea.setEnabled(false);
			thirdLineTextArea.setBorder(BorderFactory.createEmptyBorder());
			GridBagConstraints gbc_thirdLineTextArea = new GridBagConstraints();
			gbc_thirdLineTextArea.insets = new Insets(0, 0, 0, 0);
			gbc_thirdLineTextArea.gridwidth = 3;
			gbc_thirdLineTextArea.anchor = GridBagConstraints.WEST;
			gbc_thirdLineTextArea.gridx = 1;
			gbc_thirdLineTextArea.gridy = 3;
			add(thirdLineTextArea, gbc_thirdLineTextArea);

			basePathColorIndicator = new JPanel();
			basePathColorIndicator.setOpaque(true);
			GridBagConstraints gbc_color = new GridBagConstraints();
			gbc_color.insets = new Insets(0, 0, 0, 0);
			gbc_color.gridheight = 5;
			gbc_color.gridx = 4;
			gbc_color.gridy = 1;
			add(basePathColorIndicator, gbc_color);

			this.setOpaque(true);
		}
		
		/**
		 * take sure that the labels have a constant allocation.
		 */
		void completeLabelSetup(final JTable table, RendererComponent renderer) {
			if(!labelSetupComplete) {
				Font f = renderer.firstLineLabel.getFont();
				renderer.firstLineLabel.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
				
				final int oneLineHeight = 19;
				final int lastLabelHeight = table.getRowHeight() - oneLineHeight - oneLineHeight;
				
				renderer.firstLineLabel.setBorder(new EmptyBorder(0,0,0,0));
				renderer.firstLineLabel.setMinimumSize(new Dimension(table.getWidth(), oneLineHeight));
				
				renderer.dataFormatLabel.setBorder(new EmptyBorder(0,0,0,5));
				renderer.dataFormatLabel.setForeground(Color.GRAY);
				
				renderer.secondLineLabel.setMinimumSize(new Dimension(table.getWidth(), oneLineHeight));
				renderer.secondLineLabel.setBorder(new EmptyBorder(0,0,0,0));
				
				renderer.thirdLineTextArea.setMinimumSize(new Dimension(table.getWidth(), lastLabelHeight));
				renderer.thirdLineTextArea.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseReleased(MouseEvent e) {
						// dispatch the mouse event in the text field to the table's popup menu.
						popupMouseListener.mouseReleased(SwingUtilities.convertMouseEvent(thirdLineTextArea, e, table));
					}
				});
				
				renderer.imageLabel.setMinimumSize(new Dimension(50, table.getRowHeight()));
				renderer.imageLabel.setMaximumSize(new Dimension(50, table.getRowHeight()));
				renderer.imageLabel.setSize(new Dimension(50, table.getRowHeight()));
				renderer.imageLabel.setPreferredSize(new Dimension(50, table.getRowHeight()));
				
				renderer.basePathColorIndicator.setMinimumSize(new Dimension(5, table.getRowHeight()));
				labelSetupComplete = true;
			}
		}		
		
		void setStarRaterSelection(int sel) {
			starRater.setSelection(sel);
		}
		
		int getStarRaterSelection() {
			return starRater.getSelection();
		}
	}
	
	private static final VolatileHashMap<String, ImageIcon> thumbnailCache = new VolatileHashMap<String, ImageIcon>(20, 20);
	
	private final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
	
	private Dimension thumbnailDimension;
	
	private final MouseListener popupMouseListener;
	
	private boolean singletonComponent = false;

	private RendererComponent rendererComponent;
	

	public EbookTableCellRenderer(MouseListener popupMouseListener) {
		this.popupMouseListener = popupMouseListener;
	}
	
	public EbookTableCellRenderer(MouseListener popupMouseListener, boolean singletonComponent) {
		this.popupMouseListener = popupMouseListener;
		this.singletonComponent = singletonComponent;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		RendererComponent renderer = this.getTableCellComponent(table, value, isSelected, hasFocus, row, column);
		setStripedColorSetupToRenderer(isSelected, row, renderer);
		return renderer;
	}

	private void setStripedColorSetupToRenderer(boolean isSelected, int row, RendererComponent renderer) {
		if(isSelected) {
			renderer.setBackground(SwingUtils.getBrighterColor(SwingUtils.getSelectionBackgroundColor(), 20));
			renderer.setForeground(SwingUtils.getSelectionForegroundColor());
		} else {
			if(row % 2 == 0) {
				renderer.setBackground(SwingUtils.getStripeBackgroundColor());
			} else {
				renderer.setBackground(SwingUtils.getBackgroundColor());
			}
			renderer.setForeground(SwingUtils.getForegroundColor());
		}
	}

	RendererComponent getTableCellComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, final int column) {
		final EbookPropertyItem item = (EbookPropertyItem) value;
		final Color foregroundColor = SwingUtils.getForegroundColor();
		final Color selectionForegroundColor = SwingUtils.getSelectionForegroundColor();
		final Color brighterColor = SwingUtils.getBrighterColor(SwingUtils.getSelectionBackgroundColor(), 20);
		final Color backgroundColor = SwingUtils.getBackgroundColor();

		// seems there is an openjdk issue with linux which cause getTableCellComponent could be invoked 
		// before the painting process is finished. 
		// Avoid these and create always a new renderer instance.
		RendererComponent renderer = createTableCellComponent(item);
		setCommonColorRendererComponentSetup(isSelected, foregroundColor, selectionForegroundColor, brighterColor, backgroundColor, renderer);
		
		renderer.imageLabel.setIcon(getImageIconCover(table, item));
		renderer.completeLabelSetup(table, renderer);
		
		//title
		renderer.firstLineLabel.setText(this.getTitle(item));
	         
		//gray light file format
		renderer.dataFormatLabel.setText(getDataFormat(item));
		
		setStarRatingRendererComponentSetup(item, renderer);
		
		//second line author and order values.
		renderer.secondLineLabel.setText(getAuthorAndOrderValues(item));
		
		//third line description
		setThirdLineRendererComponentSetup(item, renderer);
		
		//base path color hint
		setBasePathColorIndicator(item, renderer);
		return renderer;
	}

	protected void setBasePathColorIndicator(final EbookPropertyItem item, RendererComponent renderer) {
		BasePathList basePath = preferenceStore.getBasePath();
		Color color = basePath != null && item != null ? basePath.getColor(item.getBasePath()) : null;
		if(color != null) {
			renderer.basePathColorIndicator.setBackground(preferenceStore.getBasePath().getColor(item.getBasePath()));
		} else {
			renderer.basePathColorIndicator.setBackground(SwingUtils.getBackgroundColor());
		}
	}

	private void setThirdLineRendererComponentSetup(final EbookPropertyItem item, RendererComponent renderer) {
		if(item != null && item.getDescription() != null) {
			//attach html for a multiline label but previously strip all possible html from the description.
			String strippedDescription = cleanString(item != null ? item.getDescription() : EMPTY);
			renderer.thirdLineTextArea.setText(strippedDescription);
		} else {
			renderer.thirdLineTextArea.setText(EMPTY);
		}
	}

	private void setStarRatingRendererComponentSetup(final EbookPropertyItem item, RendererComponent renderer) {
		final float starRatingValue = this.getStarRatingValue(item);
		if(starRatingValue < 0) {
			renderer.starRater.setRating(0);
		} else {
			renderer.starRater.setRating(starRatingValue);
		}
	}

	private void setCommonColorRendererComponentSetup(boolean isSelected, final Color foregroundColor, final Color selectionForegroundColor,
			final Color brighterColor, final Color backgroundColor, RendererComponent renderer) {
		if(isSelected) {
			renderer.firstLineLabel.setForeground(selectionForegroundColor);
			renderer.secondLineLabel.setForeground(selectionForegroundColor);
			renderer.thirdLineTextArea.setForeground(selectionForegroundColor);
			renderer.setForeground(selectionForegroundColor);
			renderer.setBackground(brighterColor);
		} else {
			renderer.firstLineLabel.setForeground(foregroundColor);
			renderer.secondLineLabel.setForeground(foregroundColor);
			renderer.thirdLineTextArea.setForeground(foregroundColor);
			renderer.setForeground(foregroundColor);
			renderer.setBackground(backgroundColor);
		}
	}

	protected RendererComponent createTableCellComponent(EbookPropertyItem item) {
		if(singletonComponent) {
			if(rendererComponent == null) {
				rendererComponent = new RendererComponent();
			}
			return rendererComponent;
		}
		return new RendererComponent();
	}
	
	/**
	 * Get the five star rating value or -1 if not rating is available.
	 * @param item The item containing the rating.
	 * @return The rating value.
	 */
	private float getStarRatingValue(final EbookPropertyItem item) {
		if(item == null) {
			return 0f;
		}
		
		final Integer rating = item.getRating();
		if(rating == null || rating.intValue() < 0) {
			return -1f;
		} else {
			return ((float)rating) / 2f;
		}
	}

	/**
	 * Gets the thumbnail image to be displayed in the renderer.
	 * @param table The JTable instance.
	 * @param item The item to be rendered.
	 * @return The thumbnail image to be displayed in the renderer.
	 */
	private ImageIcon getImageIconCover(final JTable table, final EbookPropertyItem item) {
		if(item == null) {
			return null;
		}
		
		byte[] coverThumbnail = EbookPropertyItemUtils.getCoverThumbnailBytes(item.getResourceHandler());
		if(item != null && coverThumbnail != null && coverThumbnail.length > 0) {
			final String coverThumbnailCRC32 = String.valueOf(CommonUtils.calculateCrc(coverThumbnail));
			if(thumbnailCache.containsKey(coverThumbnailCRC32)) {
				return thumbnailCache.get(coverThumbnailCRC32);
			}
			
			try {
				final byte[] coverData = coverThumbnail;
				if(coverData != null) {
					final IResourceHandler virtualImageResourceLoader = ResourceHandlerFactory.getVirtualResourceHandler("TableCellRendererImageData", coverData);
					final IImageProvider imageProvider = ImageProviderFactory.getImageProvider(virtualImageResourceLoader);
					final BufferedImage image = imageProvider.getImage();
					if(image != null) {
						BufferedImage scaleToMatch = ImageUtils.scaleToMatch(imageProvider.getImage(), getThumbnailDimension(table), false);
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
	
	/**
	 * Gets the dimension for the thumbnail in the view.
	 * @param table The tabel which shows the thumbnail.
	 * @return The dimension for the thumbnail.
	 */
	private Dimension getThumbnailDimension(final JTable table) {
		if(thumbnailDimension == null) {
			thumbnailDimension = new Dimension((int) (table.getRowHeight()*0.7), table.getRowHeight());
		}
		return thumbnailDimension;
	}
	
	/**
	 * Get the author and the sort order column values.
	 * @param item The item containing the desired values.
	 * @return The author and order string. Never returns <code>null</code>.
	 */
	private String getAuthorAndOrderValues(EbookPropertyItem item) {
		if(item == null) {
			return EMPTY;
		}

		final StringBuilder result = new StringBuilder();
		final List<Field> selectedFields = MainController.getController().getSelectedSortColumnFields();
		
		for (Field field : selectedFields) {
			//do not add the folowing ones.
			if(field.getName().equalsIgnoreCase("author")) {
				continue;
			}  else if(field.getName().equalsIgnoreCase("authorSort")) {
				continue;
			} else if(field.getName().equalsIgnoreCase("title")) {
				continue;
			} else if(field.getName().equalsIgnoreCase("description")) {
				continue;
			}
			
			try {
				Object fieldValueObject = ReflectionUtils.getFieldValue(item, field.getName(), true);
				if(field.getName().equals("file")) {
					fieldValueObject = item.getResourceHandler().getName();
				}
				final String fieldValueString = StringUtil.toString(fieldValueObject);
			
				if(StringUtil.isNotEmpty(fieldValueString)) {
					if(StringUtil.isNotEmpty(result)) {
						result.append(", ");
					}
					
					if(fieldValueObject instanceof Date) {
						result.append(SimpleDateFormat.getDateInstance().format(fieldValueObject));
					} else {
						result.append(fieldValueString);
					}
				}
			} catch (ReflectionFailureException e) {
				LoggerFactory.logWarning(this, "No field named " + field.getName(), e);
			}
		}
		
		//prepend the author to the result string
		if(StringUtil.isNotEmpty(result)) {
			result.insert(0, ", ");
		}
		
		List<String> authors;
		if(item.getAuthor() == null) {
			authors = Collections.emptyList();
		} else {
			authors = item.getAuthor() != null ? ListUtils.split(item.getAuthor(), IDBObject.LIST_SEPARATOR_CHAR) : new ArrayList<String>();
		}
		if(!authors.isEmpty()) {
			StringBuilder b = new StringBuilder();
			for(String author : authors) {
				if(b.length() != 0) {
					b.append(", ");
				}
				b.append(author);
			}
			if(StringUtil.isNotEmpty(b)) {
				result.insert(0, b);
			} else {
				result.insert(0, "<"+Bundle.getString("EbookTableCellComponent.noAuthor")+">");
			}
		} else {
			result.insert(0, "<"+Bundle.getString("EbookTableCellComponent.noAuthor")+">");
		}
		return result.toString();
	}
	
	/**
	 * Creates the title of the document.
	 * @param item The item where the title should be created from.
	 * @return The desired book title.
	 */
	private String getTitle(EbookPropertyItem item) {
		if(item != null && StringUtil.isEmpty(item.getTitle())) {
			//if there is no title, just use the file name but without file extension
			final String fileName = StringUtil.substringBefore(item.getFileName(), ".", false);
			return fileName;
		} else {
			return cleanString(item != null ? item.getTitle() : EMPTY);
		}
	}
	
	/**
	 * Remove all html tags an decode entities.
	 * @param toClean The text to be cleaned.
	 * @return
	 */
	private String cleanString(String toClean) {
		if(toClean == null) {
			return EMPTY;
		}
		if(toClean.indexOf('&')!=-1) {
			toClean = new HTMLEntityConverter(toClean, HTMLEntityConverter.ENCODE_EIGHT_BIT_ASCII).decodeEntities();
		}
		
		if(toClean.indexOf('<')!=-1) {
			toClean = Jsoup.clean(toClean, Safelist.none());
		}
		return toClean.trim();
	}
	
	/**
	 * Gets the string value for the file format to be displayed in the renderer.
	 * For example "pdf" if it's a pdf file.
	 * @param item The item where the string should be evaluated for.
	 * @return The detected file format string or an empty string if the format could not
	 * be detected. Never returns <code>null</code>.
	 */
	private String getDataFormat(EbookPropertyItem item) {
		if (item != null) {
			for(SUPPORTED_MIMES mime : JeboorkerConstants.SUPPORTED_MIMES.values()) {
				if(mime.getMime().equals(item.getMimeType())) {
					return mime.getName();
				}
			}
		
			IResourceHandler resourceHandler = item.getResourceHandler();
			String mimeType = resourceHandler != null ? resourceHandler.getMimeType(true) : null;
			if(mimeType != null) {
				return mimeType;
			}
		}
		return EMPTY;
	}
	
}
