package org.rr.commons.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.net.imagefetcher.IImageFetcher;
import org.rr.commons.net.imagefetcher.IImageFetcherEntry;
import org.rr.commons.net.imagefetcher.IImageFetcherFactory;
import org.rr.commons.net.imagefetcher.ImageWebSearchFetcherFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.swing.components.JRScrollPane;
import org.rr.commons.swing.components.container.ShadowPanel;
import org.rr.commons.swing.layout.EqualsLayout;
import org.rr.commons.utils.ThreadUtils;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

public class JImageDownloadDialog extends JDialog {
	
	private static int previousImageFetcherComboboxEntry = 0;
	
	private IResourceHandler selectedImage;
	
	private JTextField searchTextField;
	
	private JComboBox searchProviderComboBox;
	
	private JRScrollPane scrollPane;
	
	private JButton okButton;
	
	private static Dimension cellSize = new Dimension(100, 200);
	
	private IImageFetcherFactory factory;
	
	/**
	 * Number of images to be loaded into the dialog.
	 */
	private int resultCount = 20;

	private Color selectedFgColor;

	private Color selectedBgColor;

	private Color bgColor;

	private Color fgColor;
	
	private final ActionListener cancelAction = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			closeDialog(null);
		}
	};
	
	private final ActionListener okAction = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			storeSelectionAndCloseDialog();
		}
	};

	public JImageDownloadDialog(JFrame owner, IImageFetcherFactory factory) {
		super(owner);
		this.factory = factory;
		init(owner, factory);
	}
	
	public JImageDownloadDialog(IImageFetcherFactory factory) {
		super();
		this.factory = factory;
		init(null, factory);
	}

	protected void init(final Frame owner, final IImageFetcherFactory factory) {
		if(factory.searchTermSupport()) {
			this.setSize(800, 375);
		} else {
			this.setSize(800, 330);
		}
		if(owner != null) {
			//center over the owner frame
			this.setLocation(owner.getBounds().x + owner.getBounds().width/2 - this.getSize().width/2, owner.getBounds().y + 50);
		}
		this.setTitle(Bundle.getString("ImageDownloadDialog.title"));
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setGlassPane(new ShadowPanel());
		getGlassPane().setVisible(false);
		
	  ((JComponent)getContentPane()).registerKeyboardAction(cancelAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		//workaround for a swing bug. The first time, the editor is used, the
		//ui color instance draws the wrong color but have the right rgb values.
		Color color;
		color = SwingUtils.getSelectionForegroundColor();
		selectedFgColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
		
		color = SwingUtils.getSelectionBackgroundColor();
		selectedBgColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
		
		color = SwingUtils.getBackgroundColor();
		bgColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
		
		color = SwingUtils.getForegroundColor();
		fgColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel borderPanel = new JPanel();
		borderPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
		GridBagConstraints gbc_borderPanel = new GridBagConstraints();
		gbc_borderPanel.fill = GridBagConstraints.BOTH;
		gbc_borderPanel.gridx = 0;
		gbc_borderPanel.gridy = 0;
		getContentPane().add(borderPanel, gbc_borderPanel);
		GridBagLayout gbl_borderPanel = new GridBagLayout();
		gbl_borderPanel.columnWidths = new int[]{0, 340, 0, 0};
		gbl_borderPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_borderPanel.columnWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_borderPanel.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		borderPanel.setLayout(gbl_borderPanel);
		
		if(factory.searchTermSupport()) {
			searchProviderComboBox = new JComboBox();
			GridBagConstraints gbc_searchProviderComboBox = new GridBagConstraints();
			gbc_searchProviderComboBox.insets = new Insets(0, 0, 5, 5);
			gbc_searchProviderComboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_searchProviderComboBox.gridx = 0;
			gbc_searchProviderComboBox.gridy = 0;
			borderPanel.add(searchProviderComboBox, gbc_searchProviderComboBox);
			searchProviderComboBox.setModel(new DefaultComboBoxModel(factory.getFetcherNames().toArray()));
			searchProviderComboBox.setSelectedIndex(previousImageFetcherComboboxEntry);
			
			searchTextField = new JTextField();
			GridBagConstraints gbc_searchTextField = new GridBagConstraints();
			gbc_searchTextField.fill = GridBagConstraints.BOTH;
			gbc_searchTextField.insets = new Insets(0, 0, 5, 5);
			gbc_searchTextField.gridx = 1;
			gbc_searchTextField.gridy = 0;
			borderPanel.add(searchTextField, gbc_searchTextField);
			searchTextField.setColumns(10);
			searchTextField.addKeyListener(new KeyAdapter() {
	
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ENTER) {
						startSearch();
					}
				}
			});
			
			JButton searchButton = new JButton(new SearchAction());
			searchButton.setMargin(new Insets(0, 8, 0, 8));
			GridBagConstraints gbc_searchButton = new GridBagConstraints();
			gbc_searchButton.fill = GridBagConstraints.VERTICAL;
			gbc_searchButton.insets = new Insets(0, 0, 5, 0);
			gbc_searchButton.gridx = 2;
			gbc_searchButton.gridy = 0;
			borderPanel.add(searchButton, gbc_searchButton);
		}
		scrollPane = new JRScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		borderPanel.add(scrollPane, gbc_scrollPane);
		
		JPanel panel = new JPanel();
		panel.setLayout(new EqualsLayout(3));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 3;
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		borderPanel.add(panel, gbc_panel);
		
		JButton abortButton = new JButton(Bundle.getString("ImageDownloadDialog.Action.Cancel"));
		abortButton.setMargin(new Insets(2, 8, 2, 8));
		panel.add(abortButton);
		abortButton.addActionListener(cancelAction);
		
		okButton = new JButton(Bundle.getString("ImageDownloadDialog.Action.OK"));
		panel.add(okButton);
		okButton.addActionListener(okAction);
	}
	
	/**
	 * Stores the selection for providing it for the {@link #getSelectedImage()} method
	 * and closes this {@link JImageDownloadDialog} instance.
	 * @see #closeDialog(IResourceHandler)
	 */
	private void storeSelectionAndCloseDialog() {
		SearchResultPanel view = (SearchResultPanel) scrollPane.getViewport().getView();
		if(view != null) {
			final int selectedColumn = view.getSelectedColumn();
			final IImageFetcherEntry imageFetcher = (IImageFetcherEntry) view.getModel().getValueAt(0, selectedColumn);
			if(imageFetcher != null) {
				try {
					byte[] imageBytes = imageFetcher.getImageBytes();
					String path = imageFetcher.getImageURL().toString();
					IResourceHandler image = ResourceHandlerFactory.getVirtualResourceHandler(path, imageBytes);
					closeDialog(image);
				} catch (Exception e1) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "Could not fetch image from " + imageFetcher.getImageURL());
				}
			}
		}
	}
	
	/**
	 * Closes and disposes this {@link JImageDownloadDialog} instance.
	 * @param selectedImage The image to be set as result.
	 */
	private void closeDialog(IResourceHandler selectedImage) {
		this.selectedImage = selectedImage;
		setVisible(false);
		dispose();
	}
	
    public void setVisible(boolean b) {
    	if(b && !factory.searchTermSupport()) {
    		startSearch();
    	}
        super.setVisible(b);
    }
	
	private void startSearch() {
		getGlassPane().setVisible(true);
		new SwingWorker<SearchResultPanel, SearchResultPanel>() {
			
			@Override
			protected SearchResultPanel doInBackground() throws Exception {
				String searchText = searchTextField != null ? searchTextField.getText() : null;
				String selectedItem = searchProviderComboBox != null ? searchProviderComboBox.getSelectedItem().toString() : null;
				previousImageFetcherComboboxEntry = searchProviderComboBox != null ? searchProviderComboBox.getSelectedIndex() : 0;
				SearchResultPanel searchResultPanel = new SearchResultPanel(searchText, selectedItem);
				return searchResultPanel;
			}
	
			@Override
			protected void done() {
				try {
					SearchResultPanel searchResultPanel = get();
					scrollPane.setViewportView(searchResultPanel);
					getGlassPane().setVisible(false);
				} catch (Exception e) {
					LoggerFactory.getLogger().log(Level.WARNING, "Error while setting search reuslts.", e);
				}
			}
		}.execute();
	}

	private class SearchAction extends AbstractAction {

		private SearchAction() {
			final URL resource = JImageDownloadDialog.class.getResource("resources/play_16.gif");
			putValue(Action.SMALL_ICON, new ImageIcon(resource));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			startSearch();
		}
		
	}
	
	private class SearchResultPanel extends JTable {
		
		private Map<URL, ImageIcon> imageCache = Collections.synchronizedMap(new HashMap<URL, ImageIcon>(30));
		
		private TableCellRenderer renderer = new SearchResultTableRenderer();
		
		private SearchResultPanel(String searchTerm, String searchProviderName) {
			this.setLayout(new FlowLayout(FlowLayout.LEFT));
			this.setRowHeight(cellSize.height);
			this.setTableHeader(null);
			this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			this.setShowGrid(false);
		    this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		    
			this.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						storeSelectionAndCloseDialog();
					}
				}
			});
			
			this.setModel(new SearchResultTableModel(searchTerm, searchProviderName));
			this.setDefaultRenderer(Object.class, renderer);
			
			int columnCount = this.getModel().getColumnCount();
			for (int i = 0; i < columnCount; i++) {
				TableColumn column = this.getColumnModel().getColumn(i);
				column.setPreferredWidth(cellSize.width);
			}
		}
		
		/**
		 * Creates an {@link ImageIcon} from the given {@link IImageFetcherEntry}.
		 * @param entry The {@link IImageFetcherEntry} containing the url for the thumbnail image.
		 * @param thumbnailURL
		 * @return
		 * @throws IOException
		 */
		private ImageIcon createThumbnail(IImageFetcherEntry entry) throws IOException {
			final URL thumbnailURL = entry.getThumbnailURL();

			ImageIcon imageIcon = imageCache.get(thumbnailURL);
			if(imageIcon == null) {
				byte[] thumbnailImageBytes = entry.getThumbnailImageBytes();
				if(thumbnailImageBytes != null && thumbnailImageBytes.length > 0) {
					IImageProvider imageProvider = ImageProviderFactory.getImageProvider(ResourceHandlerFactory.getResourceHandler(new ByteArrayInputStream(thumbnailImageBytes)));
					BufferedImage image = imageProvider.getImage();
					if(image != null) {
						BufferedImage scaleToMatch = ImageUtils.scaleToMatch(image, cellSize, true);
						if(scaleToMatch != null) {
							imageIcon = new ImageIcon(scaleToMatch);
							imageCache.put(thumbnailURL, imageIcon);
						}
					}
				}
			}
			return imageIcon;
		}
		
		private class SearchResultTableRenderer extends JPanel implements TableCellRenderer {

			private JLabel sizeLabel;
			
			private JLabel imageLabel;
			
			private SearchResultTableRenderer() {
				//init
				this.setLayout(new BorderLayout());
				this.setBorder(new EmptyBorder(0, 3, 0, 3));
				
				sizeLabel = new JLabel();
				sizeLabel.setOpaque(false);
				sizeLabel.setHorizontalAlignment(SwingConstants.CENTER);
				this.add(sizeLabel, BorderLayout.SOUTH);
				
				imageLabel = new JLabel();
				imageLabel.setOpaque(false);
				imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
				this.add(imageLabel, BorderLayout.CENTER);
			}

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				if(table.getSelectedColumn() == column) {
					setBackground(selectedBgColor);
					sizeLabel.setForeground(selectedFgColor);
					imageLabel.setForeground(selectedFgColor);
				} else {
					setBackground(bgColor);
					sizeLabel.setForeground(fgColor);
					imageLabel.setForeground(fgColor);
				}
				
				int imageHeight = ((IImageFetcherEntry)value).getImageHeight();
				int imageWidth = ((IImageFetcherEntry)value).getImageWidth();
				if(imageHeight >= 0 && imageWidth >= 0) {
					sizeLabel.setText(imageWidth + "x" + imageHeight);
				} else {
					try {
						String path = ((IImageFetcherEntry)value).getImageURL().getPath();
						sizeLabel.setText(new File(path).getName());
					} catch(Exception e) {
						//no label in case something goes wrong
					}
				}
				try {
					ImageIcon imageIcon = createThumbnail((IImageFetcherEntry) value);
					if(table.getSelectedColumn() == column) {
						BufferedImage invertedThumbnail = ImageUtils.invertImage((BufferedImage) imageIcon.getImage());
						imageIcon = new ImageIcon(invertedThumbnail);
					}
					imageLabel.setIcon(imageIcon);
				} catch (IOException e) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "Images could not be retrieved.", e);
				}
				return this;
			}
		}
		
		/**
		 * Model loading and providing the images from the selected search provider.
		 */
		private class SearchResultTableModel extends AbstractTableModel {
			
			final List<IImageFetcherEntry> thumbnailEntries = Collections.synchronizedList(new ArrayList<IImageFetcherEntry>());
			
			private SearchResultTableModel(String searchTerm, String searchProviderName) {
				try { //init
					final IImageFetcher imageFetcher = factory.getImageFetcher(searchProviderName, searchTerm);
					final List<IImageFetcherEntry> imageFetcherEntries = this.createImageFetcherEntries(imageFetcher);
					
					thumbnailEntries.addAll(imageFetcherEntries);
					ThreadUtils.RunnableImpl<IImageFetcherEntry, Void> each = new ThreadUtils.RunnableImpl<IImageFetcherEntry, Void>() {
						
						@Override
						public Void run(IImageFetcherEntry entry) {
							try {
								createThumbnail(entry);
							} catch (IOException e) {
								LoggerFactory.getLogger(this).log(Level.INFO, "Failed fetching " + entry.getImageURL(), e);
							}
							return null;
						}
					};
					ThreadUtils.loopAndWait(imageFetcherEntries, each, 8);
				} catch (Exception e) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "Images could not be retrieved.", e);
				}
			}

			private List<IImageFetcherEntry> createImageFetcherEntries(final IImageFetcher imageFetcher) {
				final int max = getMaxDisplayedThumbnails();
				final Iterator<IImageFetcherEntry> entriesIterator = imageFetcher.getEntriesIterator();
				List<IImageFetcherEntry> imageFetcherEntries = new ArrayList<>(max);
				while(true) {
					try {
						for(int i = 0; i < max; i++) {
							IImageFetcherEntry entry = entriesIterator.next();
							imageFetcherEntries.add(entry);
						}
					} catch(ArrayIndexOutOfBoundsException e) {
						//no more entries
					} catch(NoSuchElementException e) {
						//no more entries
					}
					break;
				}
				return imageFetcherEntries;
			}

			@Override
			public int getRowCount() {
				return 1;
			}

			@Override
			public int getColumnCount() {
				return thumbnailEntries.size();
			}


			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return thumbnailEntries.get(columnIndex);
			}
		}
	}

	public String getSearchPhrase() {
		return this.searchTextField.getText();
	}

	public void setSearchPhrase(String searchPhrase) {
		this.searchTextField.setText(searchPhrase);
	}
	
	public IResourceHandler getSelectedImage() {
		return selectedImage;
	}

	public int getMaxDisplayedThumbnails() {
		return resultCount;
	}

	public void setResultCount(int resultCount) {
		this.resultCount = resultCount;
	}
	
	public static void main(String[] args) {
		JImageDownloadDialog imageDownloadDialog = new JImageDownloadDialog(ImageWebSearchFetcherFactory.getInstance());
		imageDownloadDialog.setVisible(true);
		System.out.println(imageDownloadDialog.getSelectedImage());
		System.exit(0);
	}
}
