package org.rr.jeborker.gui.cell;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
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
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.app.JeboorkerConstants;
import org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES;
import org.rr.jeborker.db.IDBObject;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

public class EbookTableCellRenderer extends JPanel implements TableCellRenderer, Serializable  {

	private static final long serialVersionUID = -4684790158985895647L;
	
	public EbookTableCellRenderer() {
		init();
	}
	

	JLabel imageLabel;
	
	JLabel firstLineLabel;
	
	JLabel secondLineLabel;
	
	JLabel thirdLineLabel;
	
	JLabel dataFormatLabel;
	
	StarRater starRater;
	
	private Dimension thumbnailDimension;
	
	private static final VolatileHashMap<String, ImageIcon> thumbnailCache = new VolatileHashMap<String, ImageIcon>(20, 20);
	
	/**
	 * A flag that tells where must be something to do with the labels.  
	 */
	private boolean labelSetupComplete = false;

	public void init() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0,  Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		imageLabel = new JLabel("");
		imageLabel.setOpaque(false);
		imageLabel.setVerticalAlignment(SwingConstants.TOP);
		GridBagConstraints gbc_imageLabel = new GridBagConstraints();
		gbc_imageLabel.insets = new Insets(0, 0, 0, 0);
		gbc_imageLabel.gridheight = 5;
		gbc_imageLabel.gridx = 0;
		gbc_imageLabel.gridy = 1;
		add(imageLabel, gbc_imageLabel);
		
		firstLineLabel = new JLabel("");
		firstLineLabel.setOpaque(false);
		firstLineLabel.setVerticalAlignment(SwingConstants.TOP);
		GridBagConstraints gbc_firstLineLabel = new GridBagConstraints();
		gbc_firstLineLabel.insets = new Insets(0, 0, 0, 0);
		gbc_firstLineLabel.anchor = GridBagConstraints.WEST;
		gbc_firstLineLabel.gridx = 1;
		gbc_firstLineLabel.gridy = 1;
		add(firstLineLabel, gbc_firstLineLabel);
		
		dataFormatLabel = new JLabel("");
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
		GridBagConstraints gbc_test = new GridBagConstraints();
		gbc_test.insets = new Insets(3, 0, 0, 0);
		gbc_test.gridx = 2;
		gbc_test.gridy = 1;
		starRater.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if(starRater.getSelection() > 0) {
					MainController.getController().setRatingToSelectedEntry(starRater.getSelection() * 2);
				}				
			}
			
		});
		add(starRater, gbc_test);		
		
		secondLineLabel = new JLabel("");
		secondLineLabel.setOpaque(false);
		secondLineLabel.setVerticalAlignment(SwingConstants.TOP);
		GridBagConstraints gbc_secondLineLabel = new GridBagConstraints();
		gbc_secondLineLabel.insets = new Insets(0, 0, 0, 0);
		gbc_secondLineLabel.gridwidth = 3;
		gbc_secondLineLabel.anchor = GridBagConstraints.WEST;
		gbc_secondLineLabel.gridx = 1;
		gbc_secondLineLabel.gridy = 2;
		add(secondLineLabel, gbc_secondLineLabel);
		
		thirdLineLabel = new JLabel("");
		thirdLineLabel.setOpaque(false);
		thirdLineLabel.setVerticalAlignment(SwingConstants.TOP);
		GridBagConstraints gbc_thirdLineLabel = new GridBagConstraints();
		gbc_thirdLineLabel.insets = new Insets(0, 0, 0, 0);
		gbc_thirdLineLabel.gridwidth = 3;
		gbc_thirdLineLabel.anchor = GridBagConstraints.WEST;
		gbc_thirdLineLabel.gridx = 1;
		gbc_thirdLineLabel.gridy = 3;
		add(thirdLineLabel, gbc_thirdLineLabel);
		
		this.setOpaque(true);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		final Component tableCellComponent = this.getTableCellComponent(table, value, isSelected, hasFocus, row, column);
		if(isSelected) {
			this.setBackground(SwingUtils.getBrighterColor(SwingUtils.getSelectionBackgroundColor(), 20));	
			this.setForeground(SwingUtils.getSelectionForegroundColor());
		} else {	
			if(row % 2 == 0) {
				this.setBackground(SwingUtils.getStripeBackgroundColor());
			} else {
				this.setBackground(SwingUtils.getBackgroundColor());
			}
			this.setForeground(SwingUtils.getForegroundColor());			
		}
		
		return tableCellComponent;
	}	

	Component getTableCellComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, final int column) {
		//can happens that one row is rendered several times. Test here if the same row has already been rendered. 
		//no need to do it twice.
		final EbookPropertyItem item = (EbookPropertyItem) value;
		final Color foregroundColor = SwingUtils.getForegroundColor();
		final Color selectionForegroundColor = SwingUtils.getSelectionForegroundColor();
		final Color brighterColor = SwingUtils.getBrighterColor(SwingUtils.getSelectionBackgroundColor(), 20);
		final Color backgroundColor = SwingUtils.getBackgroundColor();
		
		if(isSelected) {
			firstLineLabel.setForeground(selectionForegroundColor);
			secondLineLabel.setForeground(selectionForegroundColor);
			thirdLineLabel.setForeground(selectionForegroundColor);
			this.setForeground(selectionForegroundColor);
			this.setBackground(brighterColor);
		} else {
			firstLineLabel.setForeground(foregroundColor);
			secondLineLabel.setForeground(foregroundColor);
			thirdLineLabel.setForeground(foregroundColor);
			this.setForeground(foregroundColor);
			this.setBackground(backgroundColor);
		}
		
		switch(column) {
			case 0:
				imageLabel.setIcon(this.getImageIconCover(table, item));
				this.completeLabelSetup(table);
				
				//title
				firstLineLabel.setText(this.getTitle(item));
			         
				//gray light file format
				dataFormatLabel.setText(getDataFormat(item));
				
				//star rating
				final float starRatingValue = this.getStarRatingValue(item);
				if(starRatingValue < 0) {
					this.starRater.setRating(0);
				} else {
					this.starRater.setRating(starRatingValue);
				}
				
				//second line author and order values.
				secondLineLabel.setText(getAuthorAndOrderValues(item));
				
				//third line description
				if(item!=null && item.getDescription() != null) {
					//attach html for a multiline label but previously strip all possible html from the description. 
					String strippedDescription = cleanString(item != null ? item.getDescription() : "");
					thirdLineLabel.setText("<html>" + strippedDescription + "</html>");
				} else {
					thirdLineLabel.setText("");
				}			
				break;
			case 1:
			case 2:
			case 3:
			default:
				break;	
		}
		
		return this;
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
				if(coverData!=null) {
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
		if(thumbnailDimension==null) {
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
			return "";
		}

		final StringBuilder result = new StringBuilder();
		final List<Field> selectedFields = MainController.getController().getSortColumnComponentController().getSelectedFields();
		
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
				final String fieldValueString = StringUtils.toString(fieldValueObject);
			
				if(StringUtils.isNotEmpty(fieldValueString)) {
					if(StringUtils.isNotEmpty(result)) {
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
		if(StringUtils.isNotEmpty(result)) {
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
			if(StringUtils.isNotEmpty(b)) { 
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
		if(item!=null && StringUtils.isEmpty(item.getTitle())) {
			//if there is no title, just use the file name but without file extension
			final String fileName = StringUtils.substringBefore(item.getFileName(), ".", false);
			return fileName;
		} else {
			return cleanString(item != null ? item.getTitle() : "");
		}		
	}
	
	/**
	 * Remove all html tags an decode entities.
	 * @param toClean The text to be cleaned.
	 * @return
	 */
	private String cleanString(String toClean) {
		if(toClean==null) {
			return "";
		}
		if(toClean.indexOf('&')!=-1) {
			toClean = new HTMLEntityConverter(toClean, HTMLEntityConverter.ENCODE_EIGHT_BIT_ASCII).decodeEntities();
		}
		
		if(toClean.indexOf('<')!=-1) {
			toClean = Jsoup.clean(toClean, Whitelist.none());
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
		}
		String mimeType = item.getResourceHandler().getMimeType(true);
		if(mimeType != null) {
			return mimeType;
		}
		return "";
	}

	/**
	 * take sure that the labels have a constant allocation.
	 */
	private void completeLabelSetup(JTable table) {
		if(!labelSetupComplete) {
			java.awt.Font f = firstLineLabel.getFont();
			firstLineLabel.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
			
			final int oneLineHeight = 19;
			final int lastLabelHeight = table.getRowHeight() - oneLineHeight - oneLineHeight;
			
			firstLineLabel.setBorder(new EmptyBorder(0,0,0,0));
			firstLineLabel.setMinimumSize(new Dimension(table.getWidth(), oneLineHeight));
			
			dataFormatLabel.setBorder(new EmptyBorder(0,0,0,5));
			dataFormatLabel.setForeground(Color.GRAY);
			
			secondLineLabel.setMinimumSize(new Dimension(table.getWidth(), oneLineHeight));
			secondLineLabel.setBorder(new EmptyBorder(0,0,0,0));
			
			thirdLineLabel.setMinimumSize(new Dimension(table.getWidth(), lastLabelHeight));
			
			imageLabel.setMinimumSize(new Dimension(50, table.getRowHeight()));
			imageLabel.setMaximumSize(new Dimension(50, table.getRowHeight()));
			imageLabel.setSize(new Dimension(50, table.getRowHeight()));
			imageLabel.setPreferredSize(new Dimension(50, table.getRowHeight()));
			labelSetupComplete = true;
		}		
	}
	
	
}
