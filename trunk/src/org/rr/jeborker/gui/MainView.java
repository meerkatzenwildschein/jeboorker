package org.rr.jeborker.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.japura.gui.CheckComboBox;
import org.rr.common.swing.ShadowPanel;
import org.rr.common.swing.SwingUtils;
import org.rr.common.swing.button.JMenuButton;
import org.rr.common.swing.components.JRScrollPane;
import org.rr.common.swing.dnd.DragAndDropUtils;
import org.rr.common.swing.dnd.FileTransferable;
import org.rr.common.swing.dnd.URIListTransferable;
import org.rr.common.swing.image.SimpleImageViewer;
import org.rr.common.swing.table.JRTable;
import org.rr.common.swing.tree.JRTree;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.layout.EqualsLayout;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.JeboorkerPreferenceListener;
import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.gui.action.PasteFromClipboardAction;
import org.rr.jeborker.gui.model.BasePathTreeModel;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;
import org.rr.jeborker.gui.model.EbookPropertyDBTableSelectionModel;
import org.rr.jeborker.gui.model.EbookSheetPropertyModel;
import org.rr.jeborker.gui.model.FileSystemNode;
import org.rr.jeborker.gui.model.FileSystemTreeModel;
import org.rr.jeborker.gui.renderer.BasePathTreeCellEditor;
import org.rr.jeborker.gui.renderer.BasePathTreeCellRenderer;
import org.rr.jeborker.gui.renderer.DatePropertyCellEditor;
import org.rr.jeborker.gui.renderer.DatePropertyCellRenderer;
import org.rr.jeborker.gui.renderer.DefaultPropertyCellEditor;
import org.rr.jeborker.gui.renderer.DefaultPropertyRenderer;
import org.rr.jeborker.gui.renderer.EbookTableCellEditor;
import org.rr.jeborker.gui.renderer.EbookTableCellRenderer;
import org.rr.jeborker.gui.renderer.FileSystemTreeCellEditor;
import org.rr.jeborker.gui.renderer.FileSystemTreeCellRenderer;
import org.rr.jeborker.gui.renderer.MultiListPropertyEditor;
import org.rr.jeborker.gui.renderer.MultiListPropertyRenderer;
import org.rr.jeborker.gui.renderer.StarRatingPropertyEditor;
import org.rr.jeborker.gui.renderer.StarRatingPropertyRenderer;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

import skt.swing.StringConvertor;

import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheet;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

class MainView extends JFrame {
	
	private static final long serialVersionUID = 6837919427429399376L;

	protected static final Object EbookPropertyItem = null;
	
	JRTable mainTable;
	
	JProgressBar progressBar;
	
	JDialog blockingDialog;
	
	JSplitPane mainSplitPane;
	
	JSplitPane propertySheetImageSplitPane;
	
	SimpleImageViewer imageViewer;
	
	PropertySheetPanel propertySheet;
	
	JMenuButton addMetadataButton;
	
	JButton removeMetadataButton;
	
	JButton saveMetadataButton;
	
	JPanel rootPanel;
	
	private JPanel sortPanel;
	
	private JLabel sortLabel;
	
	CheckComboBox<Field> sortColumnComboBox;
	
	JToggleButton sortOrderAscButton;
	
	JToggleButton sortOrderDescButton;
	
	JSplitPane treeMainTableSplitPane;
	
	JRTree basePathTree;
	
	JRTree fileSystemTree;
	
	JRScrollPane mainTableScrollPane;

	JTabbedPane treeTabbedPane;
	
	private JPanel buttonPanel;

	/**
	 * Create the application.
	 */
	public MainView() {
		initialize();
		initializeGlobalKeystrokes();
		JeboorkerPreferences.addPreferenceChangeListener(new MainViewPreferenceListener());
	}

	private void initializeGlobalKeystrokes() {
		final InputMap inputMap = this.getRootPane().getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW);
		final ActionMap actionMap = this.getRootPane().getActionMap();
		
		KeyStroke quitKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK);
		inputMap.put(quitKeyStroke, "QUIT");
		actionMap.put("QUIT", ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.QUIT_ACTION, null));
		
		KeyStroke saveKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK);
		inputMap.put(saveKeyStroke, "SAVE");
		actionMap.put("SAVE", ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SAVE_METADATA_ACTION, null));	
		
		KeyStroke find = KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK, false);
		inputMap.put(find, "FIND");
		actionMap.put("FIND", ActionFactory.getTableFindAction(mainTable));
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		this.setTitle(Jeboorker.app + " " + Jeboorker.version);
		this.setIconImage(ImageResourceBundle.getResourceAsImageIcon("logo_16.png").getImage());
		this.setBounds(100, 100, 792, 622);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.QUIT_ACTION, null).invokeAction();
			}
		});
		
		this.setGlassPane(new ShadowPanel());	
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{489};
		gridBagLayout.rowHeights = new int[]{350, 25, 30};
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 4.9E-324};
		this.getContentPane().setLayout(gridBagLayout);
		
		
		mainSplitPane = new JSplitPane();
		mainSplitPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
		mainSplitPane.setOneTouchExpandable(true);
		mainSplitPane.setResizeWeight(0.9);

		GridBagConstraints gbc_mainSplitPane = new GridBagConstraints();
		gbc_mainSplitPane.insets = new Insets(0, 3, 5, 0);
		gbc_mainSplitPane.fill = GridBagConstraints.BOTH;
		gbc_mainSplitPane.gridx = 0;
		gbc_mainSplitPane.gridy = 0;
		getContentPane().add(mainSplitPane, gbc_mainSplitPane);
		KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
		KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
		KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
		KeyStroke refresh = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false);
		
		JPanel propertyContentPanel = new JPanel();
		GridBagLayout gbl_propertyContentPanel = new GridBagLayout();
		gbl_propertyContentPanel.columnWidths = new int[]{0};
		gbl_propertyContentPanel.rowHeights = new int[]{25, 0, 0};
		gbl_propertyContentPanel.columnWeights = new double[]{1.0};
		gbl_propertyContentPanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		propertyContentPanel.setLayout(gbl_propertyContentPanel);
		mainSplitPane.setLeftComponent(propertyContentPanel);
				
				sortPanel = new JPanel();
				GridBagConstraints gbc_sortPanel = new GridBagConstraints();
				gbc_sortPanel.insets = new Insets(5, 0, 5, 0);
				gbc_sortPanel.fill = GridBagConstraints.BOTH;
				gbc_sortPanel.gridx = 0;
				gbc_sortPanel.gridy = 0;
				propertyContentPanel.add(sortPanel, gbc_sortPanel);
				GridBagLayout gbl_sortPanel = new GridBagLayout();
				gbl_sortPanel.columnWidths = new int[]{110, 30, 30, 1, 0};
				gbl_sortPanel.rowHeights = new int[]{25, 0};
				gbl_sortPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
				gbl_sortPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
				sortPanel.setLayout(gbl_sortPanel);
				
				sortLabel = new JLabel(Bundle.getString("EborkerMainView.sortby"));
				GridBagConstraints gbc_sortLabel = new GridBagConstraints();
				gbc_sortLabel.fill = GridBagConstraints.VERTICAL;
				gbc_sortLabel.anchor = GridBagConstraints.WEST;
				gbc_sortLabel.insets = new Insets(0, 5, 0, 5);
				gbc_sortLabel.gridx = 0;
				gbc_sortLabel.gridy = 0;
				sortPanel.add(sortLabel, gbc_sortLabel);
				
				sortColumnComboBox = new CheckComboBox<Field>();
				sortColumnComboBox.setPreferredSize(new Dimension(0, 25));
				GridBagConstraints gbc_sortColumnComboBox = new GridBagConstraints();
				gbc_sortColumnComboBox.fill = GridBagConstraints.BOTH;
				gbc_sortColumnComboBox.gridx = 3;
				gbc_sortColumnComboBox.gridy = 0;
				sortPanel.add(sortColumnComboBox, gbc_sortColumnComboBox);
				
				final Icon ascOrderIcon =  new ImageIcon(MainView.class.getResource("resources/sort_asc.gif"));
				final Icon descOrderIcon = new ImageIcon(MainView.class.getResource("resources/sort_desc.gif"));

				sortOrderAscButton = new JToggleButton();
				sortOrderAscButton.setIcon(ascOrderIcon);
				sortOrderAscButton.setPreferredSize(new Dimension(0, 25));
				sortOrderAscButton.setMinimumSize(new Dimension(0, 25));
				GridBagConstraints gbc_sortOrderAscButton = new GridBagConstraints();
				gbc_sortOrderAscButton.fill = GridBagConstraints.BOTH;
				gbc_sortOrderAscButton.insets = new Insets(0, 0, 0, 5);
				gbc_sortOrderAscButton.gridx = 1;
				gbc_sortOrderAscButton.gridy = 0;
				sortPanel.add(sortOrderAscButton, gbc_sortOrderAscButton);
				
				sortOrderDescButton = new JToggleButton();
				sortOrderDescButton.setIcon(descOrderIcon);
				sortOrderDescButton.setPreferredSize(new Dimension(0, 25));
				sortOrderDescButton.setMinimumSize(new Dimension(0, 25));
				GridBagConstraints gbc_sortOrderDescButton = new GridBagConstraints();
				gbc_sortOrderDescButton.fill = GridBagConstraints.BOTH;
				gbc_sortOrderDescButton.insets = new Insets(0, 0, 0, 5);
				gbc_sortOrderDescButton.gridx = 2;
				gbc_sortOrderDescButton.gridy = 0;
				sortPanel.add(sortOrderDescButton, gbc_sortOrderDescButton);
				
				treeMainTableSplitPane = new JSplitPane();
				treeMainTableSplitPane.setDividerLocation(220);
				GridBagConstraints gbc_treeMainTableSplitPane = new GridBagConstraints();
				gbc_treeMainTableSplitPane.fill = GridBagConstraints.BOTH;
				gbc_treeMainTableSplitPane.gridx = 0;
				gbc_treeMainTableSplitPane.gridy = 1;
				propertyContentPanel.add(treeMainTableSplitPane, gbc_treeMainTableSplitPane);
				
				createMainTable(copy, paste, delete, refresh);
				
				mainTableScrollPane = new JRScrollPane();
				treeMainTableSplitPane.setRightComponent(mainTableScrollPane);
				mainTableScrollPane.setViewportView(mainTable);
				
				treeTabbedPane = new JTabbedPane();
				treeTabbedPane.setDropTarget(new DropTarget(treeTabbedPane, new DropTargetAdapter() {
					
					@Override
					public void dragOver(DropTargetDragEvent dtde) {
						Point location = dtde.getLocation();
						int indexAtLocation = treeTabbedPane.indexAtLocation(location.x, location.y);
						if(indexAtLocation >= 0) {
							treeTabbedPane.setSelectedIndex(indexAtLocation);
						}
					}

					@Override
					public void drop(DropTargetDropEvent dtde) {
					}
				
				}));
				
				JComponent basePathTreeComp = createBasePathTree();
				treeTabbedPane.addTab(Bundle.getString("EborkerMainView.tabbedPane.basePath"), basePathTreeComp);
				
				JComponent fileSystemTreeComp = createFileSystemTree(copy, paste, delete, refresh);
				treeTabbedPane.addTab(Bundle.getString("EborkerMainView.tabbedPane.fileSystem"), fileSystemTreeComp);
				
				treeMainTableSplitPane.setLeftComponent(treeTabbedPane);
				treeMainTableSplitPane.setOneTouchExpandable(true);
				
				JPanel sheetPanel = new JPanel();
				GridBagLayout gbl_sheetPanel = new GridBagLayout();
				gbl_sheetPanel.columnWidths = new int[]{0, 0};
				gbl_sheetPanel.rowHeights = new int[]{0, 0};
				gbl_sheetPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
				gbl_sheetPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
				sheetPanel.setLayout(gbl_sheetPanel);
				
				propertySheet = new PropertySheetPanel(new EbookSheetPropertyModel());
				propertySheet.setMode(PropertySheet.VIEW_AS_FLAT_LIST);
				propertySheet.setDescriptionVisible(true);
				propertySheet.setShowCategoryButton(false);
				
				addMetadataButton = new JMenuButton();
				addMetadataButton.setIcon(new ImageIcon(Bundle.getResource("add_metadata_16.png")));
				propertySheet.addToolbarComponent(addMetadataButton);
				
				removeMetadataButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REMOVE_METADATA_ENTRY_ACTION, null));
				propertySheet.addToolbarComponent(removeMetadataButton);
				
				saveMetadataButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SAVE_METADATA_ACTION, null));
				saveMetadataButton.setText("");
				propertySheet.addToolbarComponent(saveMetadataButton);				
				
				((PropertyRendererRegistry)propertySheet.getRendererFactory()).registerRenderer((Class<?>)null, DefaultPropertyRenderer.class);
				((PropertyEditorRegistry)propertySheet.getEditorFactory()).registerEditor((Class<?>)null, DefaultPropertyCellEditor.class);
				
				DatePropertyCellRenderer calendarDatePropertyRenderer = new DatePropertyCellRenderer(((SimpleDateFormat) SimpleDateFormat.getDateInstance()).toPattern());
		        ((PropertyEditorRegistry)propertySheet.getEditorFactory()).registerEditor(Date.class, new DatePropertyCellEditor());
		        ((PropertyRendererRegistry)propertySheet.getRendererFactory()).registerRenderer(Date.class, calendarDatePropertyRenderer);
		        
		        ((PropertyEditorRegistry)propertySheet.getEditorFactory()).registerEditor("rating", StarRatingPropertyEditor.class);
		        ((PropertyRendererRegistry)propertySheet.getRendererFactory()).registerRenderer("rating", StarRatingPropertyRenderer.class);
		        ((PropertyEditorRegistry)propertySheet.getEditorFactory()).registerEditor("calibre:rating", StarRatingPropertyEditor.class);
		        ((PropertyRendererRegistry)propertySheet.getRendererFactory()).registerRenderer("calibre:rating", StarRatingPropertyRenderer.class);
		        
		        ((PropertyEditorRegistry)propertySheet.getEditorFactory()).registerEditor(java.util.List.class, MultiListPropertyEditor.class);
		        ((PropertyRendererRegistry)propertySheet.getRendererFactory()).registerRenderer(java.util.List.class, MultiListPropertyRenderer.class);
		        
				GridBagConstraints gbc_propertySheet = new GridBagConstraints();
				gbc_propertySheet.fill = GridBagConstraints.BOTH;
				gbc_propertySheet.gridx = 0;
				gbc_propertySheet.gridy = 0;
				sheetPanel.add(propertySheet, gbc_propertySheet);
				
				propertySheetImageSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
				propertySheetImageSplitPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
				propertySheetImageSplitPane.setOneTouchExpandable(true);
				mainSplitPane.setRightComponent(propertySheetImageSplitPane);

				JPanel imageViewerPanel = new JPanel();
				imageViewerPanel.setBorder(new EmptyBorder(3,3,3,3));
				imageViewerPanel.setLayout(new BorderLayout());
				imageViewer = new SimpleImageViewer();
				GridBagConstraints gbc_imageViewer = new GridBagConstraints();
				gbc_imageViewer.fill = GridBagConstraints.BOTH;
				gbc_imageViewer.gridx = 0;
				gbc_imageViewer.gridy = 1;
				imageViewerPanel.add(imageViewer, BorderLayout.CENTER);
				propertySheetImageSplitPane.setRightComponent(imageViewerPanel);
				propertySheetImageSplitPane.setLeftComponent(sheetPanel);
				propertySheetImageSplitPane.setDividerLocation(getSize().height / 2);
				
				mainSplitPane.setDividerLocation(getSize().width - 220);
				
				
		JPanel filterPanel = FilterPanelController.getView();
		GridBagConstraints gbc_searchPanel = new GridBagConstraints();
		gbc_searchPanel.insets = new Insets(0, 3, 5, 3);
		gbc_searchPanel.anchor = GridBagConstraints.NORTH;
		gbc_searchPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchPanel.gridx = 0;
		gbc_searchPanel.gridy = 1;
		getContentPane().add(filterPanel, gbc_searchPanel);
				
		JPanel statusPanel = new JPanel();
		GridBagConstraints gbc_statusPanel = new GridBagConstraints();
		gbc_statusPanel.insets = new Insets(0, 3, 3, 3);
		gbc_statusPanel.fill = GridBagConstraints.BOTH;
		gbc_statusPanel.gridx = 0;
		gbc_statusPanel.gridy = 2;
		getContentPane().add(statusPanel, gbc_statusPanel);
		GridBagLayout gbl_statusPanel = new GridBagLayout();
		gbl_statusPanel.columnWidths = new int[]{0, 0, 0};
		gbl_statusPanel.rowHeights = new int[]{14, 0};
		gbl_statusPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_statusPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		statusPanel.setLayout(gbl_statusPanel);
		
		JLabel label = new JLabel(Bundle.getString("EborkerMainView.status"));
		Dimension statusLabelSize = new Dimension(55, label.getPreferredSize().height);
		label.setPreferredSize(statusLabelSize);
		label.setMinimumSize(statusLabelSize);			
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.WEST;
		gbc_label.fill = GridBagConstraints.NONE;
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		statusPanel.add(label, gbc_label);
		
		progressBar = new JProgressBar();
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.weighty = 1.0;
		gbc_progressBar.weightx = 1.0;
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridx = 1;
		gbc_progressBar.gridy = 0;
		statusPanel.add(progressBar, gbc_progressBar);
		
		this.setJMenuBar(MainMenuBarController.getController().getView());
	}

	private void createMainTable(KeyStroke copy, KeyStroke paste, KeyStroke delete, KeyStroke refresh) {
		mainTable = new JRTable();
		mainTable.setName("MainTable");
		mainTable.setRowHeight(74);
		mainTable.setModel(new EbookPropertyDBTableModel(true));
		mainTable.setDefaultRenderer(Object.class, new EbookTableCellRenderer());
		mainTable.setDefaultEditor(Object.class, new EbookTableCellEditor(new EbookTableCellEditor.EditListener() {
			
			@Override
			public void editingStoped() {
			}
			
			@Override
			public void editingStarted() {
				fileSystemTree.stopEditing();
				fileSystemTree.clearSelection();
				
				basePathTree.stopEditing();
				basePathTree.clearSelection();				
			}
			
			@Override
			public void editingCanceled() {
			}
		}));
		mainTable.setTableHeader(null);
		mainTable.setSelectionModel(new EbookPropertyDBTableSelectionModel());
		mainTable.setDragEnabled(true);
		mainTable.setStopEditOnSelectionChange(true);
		mainTable.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.COPY_TO_CLIPBOARD_ACTION, null), "Copy", copy, JComponent.WHEN_FOCUSED);
		mainTable.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.PASTE_FROM_CLIPBOARD_ACTION, null), "Paste", paste, JComponent.WHEN_FOCUSED);		
		mainTable.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.DELETE_FILE_ACTION, null), "DeleteFile", delete, JComponent.WHEN_FOCUSED);
		mainTable.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REFRESH_ENTRY_ACTION, null), "RefreshEntry", refresh, JComponent.WHEN_FOCUSED);
		mainTable.putClientProperty(StringConvertor.class, new StringConvertor() {
			
			@Override
			public String toString(Object obj) {
				if(obj instanceof EbookPropertyItem) {
					EbookPropertyItem item = (EbookPropertyItem) obj;
					return new StringBuilder().append(item.getResourceHandler().getName())
							.append(" ").append(item.getAuthor())
							.append(" ").append(item.getTitle())
							.append(" ").append(item.getSeriesName())
							.toString();
				}
				return StringUtils.toString(obj);
			}
		});
		
		mainTable.setTransferHandler(new TransferHandler() {

			private static final long serialVersionUID = -371360766111031218L;

			public boolean canImport(TransferHandler.TransferSupport info) {
		        //only import Strings
		        if (!(info.isDataFlavorSupported(DataFlavor.stringFlavor) || info.isDataFlavorSupported(DataFlavor.javaFileListFlavor))) {
		            return false;
		        }

		        JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
		        if (dl.getRow() == -1) {
		            return false;
		        }
		        
		        return true;
		    }

		    public boolean importData(TransferHandler.TransferSupport info) {
		        if (!info.isDrop()) {
		            return false;
		        }
		        
		        // Check for String flavor
		        if (!(info.isDataFlavorSupported(DataFlavor.stringFlavor) || info.isDataFlavorSupported(DataFlavor.javaFileListFlavor))) {
		        	LoggerFactory.getLogger().log(Level.INFO, "List doesn't accept a drop of this type.");
		            return false;
		        }

		        JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
		        int dropRow = dl.getRow();
		        return PasteFromClipboardAction.importEbookFromClipboard(info.getTransferable(), dropRow);
		    }
		    
		    public int getSourceActions(JComponent c) {
		        return COPY;
		    }
		    
		    /**
		     * Create a new Transferable that is used to drag files from jeboorker to a native application.
		     */
		    protected Transferable createTransferable(JComponent c) {
		        final JTable list = (JTable) c;
		        final int[] selectedRows = list.getSelectedRows();
		        final List<URI> uriList = new ArrayList<URI>();
		        final List<String> files = new ArrayList<String>();
		        
		        for (int i = 0; i < selectedRows.length; i++) {
		        	EbookPropertyItem val = (EbookPropertyItem) mainTable.getModel().getValueAt(selectedRows[i], 0);
		        	try {
		        		uriList.add(new File(val.getFile()).toURI());
		        		files.add(new File(val.getFile()).getPath());
							} catch (Exception e) {
								LoggerFactory.getLogger().log(Level.WARNING, "Failed to encode " + val.getResourceHandler().toString(), e);
							}
		        }    
		        
		        if(CommonUtils.isLinux()) {
		        	if(ReflectionUtils.javaVersion() == 16) {
		        		return new URIListTransferable(uriList, null);
		        	} else {
		        		return new FileTransferable(files);
		        	}
		        } else {
		        	return new FileTransferable(files);
		        }
		    }
		});
	}

	private JComponent createFileSystemTree(final KeyStroke copy, final KeyStroke paste, final KeyStroke delete, final KeyStroke refresh) {
		final String fileSystemTreeName = "FileSystemTree";
		
		JPanel fileSystemTreePanel = new JPanel();
		fileSystemTreePanel.setBackground(SwingUtils.getBackgroundColor());
		fileSystemTreePanel.setOpaque(true);
		
		GridBagLayout gbl_fileSystemTreePanel = new GridBagLayout();
		gbl_fileSystemTreePanel.columnWidths = new int[]{76, 0};
		gbl_fileSystemTreePanel.rowHeights = new int[]{25, 0, 0};
		gbl_fileSystemTreePanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_fileSystemTreePanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		fileSystemTreePanel.setLayout(gbl_fileSystemTreePanel);
		
		buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);
		
		GridBagConstraints gbc_buttonPanel = new GridBagConstraints();
		gbc_buttonPanel.insets = new Insets(3, 0, 3, 0);
		gbc_buttonPanel.fill = GridBagConstraints.BOTH;
		gbc_buttonPanel.gridx = 0;
		gbc_buttonPanel.gridy = 0;
		fileSystemTreePanel.add(buttonPanel, gbc_buttonPanel);
		buttonPanel.setLayout(new EqualsLayout(EqualsLayout.RIGHT, 3, true));
		
		Dimension buttonDimension = new Dimension(28, 28);
		
		JButton syncButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SYNC_FOLDER_ACTION, null));
		syncButton.setPreferredSize(buttonDimension);
		buttonPanel.add(syncButton);
		
		JButton collapseButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.FILE_SYSTEM_COLLAPSE_ALL_ACTION, fileSystemTreeName));
		collapseButton.setPreferredSize(buttonDimension);
		buttonPanel.add(collapseButton);		
		
		fileSystemTree = new JRTree();
		fileSystemTree.setName(fileSystemTreeName);
		if(Jeboorker.isRuntime) {
			FileSystemTreeModel fileSystemTreeModel = new FileSystemTreeModel(fileSystemTree);
			fileSystemTree.setModel(fileSystemTreeModel);
			fileSystemTree.setAutoMoveHorizontalSliders(JeboorkerPreferences.isTreeAutoScrollingEnabled());
			fileSystemTree.setEditable(true);
			FileSystemTreeCellRenderer fileSystemTreeCellRenderer = new FileSystemTreeCellRenderer();
			fileSystemTree.setCellRenderer(fileSystemTreeCellRenderer);
			fileSystemTree.setCellEditor(new FileSystemTreeCellEditor(fileSystemTree, fileSystemTreeCellRenderer));
			if(((DefaultMutableTreeNode) fileSystemTreeModel.getRoot()).getChildCount() == 1) {
				fileSystemTree.addTreeExpansionListener(new TreeExpansionListener() {
					
					@Override
					public void treeExpanded(TreeExpansionEvent event) {
					}
					
					@Override
					public void treeCollapsed(TreeExpansionEvent event) {
						TreePath path = event.getPath();
						if(path.getPathCount() == 2) {
							fileSystemTree.expandPath(event.getPath());
						}
					}
				});
				//row 0 should always be expanded.
				fileSystemTree.expandRow(0);
			}
		}

		JRScrollPane treeScroller = new JRScrollPane(fileSystemTree);
		treeScroller.setOpaque(false);
		treeScroller.getViewport().setOpaque(false);
		treeScroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		GridBagConstraints gbc_treeScroller = new GridBagConstraints();
		gbc_treeScroller.fill = GridBagConstraints.BOTH;
		gbc_treeScroller.anchor = GridBagConstraints.NORTHWEST;
		gbc_treeScroller.gridx = 0;
		gbc_treeScroller.gridy = 1;
		fileSystemTreePanel.add(treeScroller, gbc_treeScroller);
		
		fileSystemTree.setRootVisible(false);
		fileSystemTree.setRowHeight(25);
		fileSystemTree.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.COPY_TO_CLIPBOARD_ACTION, null), "Copy", copy, JComponent.WHEN_FOCUSED);
		fileSystemTree.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.PASTE_FROM_CLIPBOARD_ACTION, null), "Paste", paste, JComponent.WHEN_FOCUSED);		
		fileSystemTree.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.DELETE_FILE_ACTION, null), "DeleteFile", delete, JComponent.WHEN_FOCUSED);
		fileSystemTree.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.FILE_SYSTEM_REFRESH_ACTION, null), "Refresh", refresh, JComponent.WHEN_FOCUSED);
		
		fileSystemTree.setDragEnabled(true);
		fileSystemTree.setTransferHandler(new TransferHandler() {

			private static final long serialVersionUID = -371360766111031218L;

			public boolean canImport(TransferHandler.TransferSupport info) {
                return DragAndDropUtils.isFileImportRequest(info);
            }

            public boolean importData(TransferHandler.TransferSupport info) {
                if (!info.isDrop()) {
                    return false;
                }
                
                if (!DragAndDropUtils.isFileImportRequest(info)) {
                	LoggerFactory.getLogger().log(Level.INFO, "List doesn't accept a drop of this type.");
                    return false;
                }

                JTree.DropLocation dl = (JTree.DropLocation) info.getDropLocation();
                TreePath dropRow = dl.getPath();
                Object lastPath = dropRow.getLastPathComponent();
                try {
                	IResourceHandler targetPathResource = ((FileSystemNode) lastPath).getResource();
                	boolean reloadParent = false;
                	if(targetPathResource.isFileResource()) {
                		targetPathResource = targetPathResource.getParentResource();
                		reloadParent = true;
                	}
                	Transferable transferable = info.getTransferable();
                	List<IResourceHandler> sourceResourceHandlers = ResourceHandlerFactory.getResourceHandler(transferable);
                	for(IResourceHandler sourceResourceHandler : sourceResourceHandlers) {
                		String basePathFor = JeboorkerPreferences.getBasePathFor(targetPathResource);
                		if(basePathFor != null) {
                			//drop to a folder that is managed by jeboorker.
                			PasteFromClipboardAction.importEbookFromClipboard(transferable, Integer.MIN_VALUE, basePathFor, targetPathResource);
                		} else {
                			//do a simple copy
                			IResourceHandler targetPathResourceFile = targetPathResource.addPathStatement(sourceResourceHandler.getName());
                			IResourceHandler uniqueTargetPathResourceFile = ResourceHandlerFactory.getUniqueResourceHandler(targetPathResourceFile, targetPathResourceFile.getFileExtension());
                			sourceResourceHandler.copyTo(uniqueTargetPathResourceFile, false);
                		}
                		if(reloadParent) {
                			TreeNode node = (TreeNode) dropRow.getLastPathComponent();
                			TreeNode parentNode = node.getParent();
                			if(parentNode != null) {
                				((DefaultTreeModel) fileSystemTree.getModel()).reload(parentNode);
                			} else {
                				((DefaultTreeModel) fileSystemTree.getModel()).reload(node);
                			}
                		} else {
                			((DefaultTreeModel) fileSystemTree.getModel()).reload((TreeNode) dropRow.getLastPathComponent());
                		}
                	}
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				} 
                return true;
            }
            
            public int getSourceActions(JComponent c) {
                return COPY;
            }
            
            /**
             * Create a new Transferable that is used to drag files from jeboorker to a native application.
             */
            protected Transferable createTransferable(JComponent c) {
            	List<IResourceHandler> selectedTreeItems = MainController.getController().getMainTreeController().getSelectedTreeItems();
		        final List<URI> uriList = new ArrayList<URI>(selectedTreeItems.size());
		        final List<String> files = new ArrayList<String>(selectedTreeItems.size());
		        
				for (int i = 0; i < selectedTreeItems.size(); i++) {
					IResourceHandler selectedTreeItem = selectedTreeItems.get(i);
					try {

						uriList.add(selectedTreeItem.toFile().toURI());
						files.add(selectedTreeItem.toFile().getPath());
					} catch (Exception e) {
						LoggerFactory.getLogger().log(Level.WARNING, "Failed to encode " + selectedTreeItem.toString(), e);
					}
				}
		        
		        if(CommonUtils.isLinux()) {
		        	if(ReflectionUtils.javaVersion() == 16) {
		        		return new URIListTransferable(uriList, null);
		        	} else {
		        		return new FileTransferable(files);
		        	}
		        } else {
		        	return new FileTransferable(files);
		        }
	        }
        });
		
		fileSystemTree.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				mainTable.stopEdit();
				mainTable.getSelectionModel().clearSelection();
			}
			
		});
		
		return fileSystemTreePanel;		
	}
	
	private JComponent createBasePathTree() {
		final String basePathTreeName = "BasePathTree";
		
		JPanel basePathTreePanel = new JPanel();
		basePathTreePanel.setBackground(SwingUtils.getBackgroundColor());
		basePathTreePanel.setOpaque(true);
		
		GridBagLayout gbl_basePathTreePanel = new GridBagLayout();
		gbl_basePathTreePanel.columnWidths = new int[]{76, 0};
		gbl_basePathTreePanel.rowHeights = new int[]{25, 0, 0};
		gbl_basePathTreePanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_basePathTreePanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		basePathTreePanel.setLayout(gbl_basePathTreePanel);
		
		buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);
		
		GridBagConstraints gbc_buttonPanel = new GridBagConstraints();
		gbc_buttonPanel.insets = new Insets(3, 0, 3, 0);
		gbc_buttonPanel.fill = GridBagConstraints.BOTH;
		gbc_buttonPanel.gridx = 0;
		gbc_buttonPanel.gridy = 0;
		basePathTreePanel.add(buttonPanel, gbc_buttonPanel);
		buttonPanel.setLayout(new EqualsLayout(EqualsLayout.RIGHT, 3, true));
		
		Dimension buttonDimension = new Dimension(28, 28);
		
		JButton syncButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SYNC_FOLDER_ACTION, null));
		syncButton.setPreferredSize(buttonDimension);
		buttonPanel.add(syncButton);
		
		JButton addButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.ADD_BASE_PATH_ACTION, null));
		addButton.setPreferredSize(buttonDimension);
		addButton.setText("");
		buttonPanel.add(addButton);
		
		JButton collapseButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.FILE_SYSTEM_COLLAPSE_ALL_ACTION, basePathTreeName));
		collapseButton.setPreferredSize(buttonDimension);
		buttonPanel.add(collapseButton);
		
		basePathTree = new JRTree();
		basePathTree.setName(basePathTreeName);
		if(Jeboorker.isRuntime) {
			basePathTree.setModel(new BasePathTreeModel());
			basePathTree.setEditable(true);
			BasePathTreeCellRenderer basePathTreeCellRenderer = new BasePathTreeCellRenderer(basePathTree);
			basePathTree.setCellRenderer(basePathTreeCellRenderer);
			basePathTree.setCellEditor(new BasePathTreeCellEditor(basePathTree));
			basePathTree.setToggleExpandOnDoubleClick(true);
			basePathTree.setAutoMoveHorizontalSliders(JeboorkerPreferences.isTreeAutoScrollingEnabled());
			basePathTree.setRepaintAllOnChange(true);
			basePathTree.setEditable(true);
		}
		JRScrollPane basePathTreeScroller = new JRScrollPane(basePathTree);
		basePathTreeScroller.setOpaque(false);
		basePathTreeScroller.getViewport().setOpaque(false);
		GridBagConstraints gbc_basePathTreeScroller = new GridBagConstraints();
		gbc_basePathTreeScroller.fill = GridBagConstraints.BOTH;
		gbc_basePathTreeScroller.anchor = GridBagConstraints.WEST;
		gbc_basePathTreeScroller.gridx = 0;
		gbc_basePathTreeScroller.gridy = 1;
		basePathTreePanel.add(basePathTreeScroller, gbc_basePathTreeScroller);
		
		basePathTree.setRootVisible(false);
		basePathTree.setRowHeight(25);
		
		basePathTree.setTransferHandler(new TransferHandler() {

			private static final long serialVersionUID = -371360766111031218L;

			public boolean canImport(TransferHandler.TransferSupport info) {
				return DragAndDropUtils.isFileImportRequest(info);
			}

            public boolean importData(TransferHandler.TransferSupport info) {
                if (!info.isDrop()) {
                    return false;
                }
                
                // Check for String flavor
                if (!DragAndDropUtils.isFileImportRequest(info)) {
                	LoggerFactory.getLogger().log(Level.INFO, "List doesn't accept a drop of this type.");
                    return false;
                }

                JTree.DropLocation dl = (JTree.DropLocation) info.getDropLocation();
                TreePath dropRow = dl.getPath();
                Object lastPath = dropRow.getLastPathComponent();
                Object firstPath = dropRow.getPath()[1]; //is the base path
                IResourceHandler firstPathResource = ((FileSystemNode) firstPath).getResource();
                IResourceHandler lastPathPathResource = ((FileSystemNode) lastPath).getResource();
                try {
					PasteFromClipboardAction.importEbookFromClipboard(info.getTransferable(), Integer.MIN_VALUE, firstPathResource.toString(), lastPathPathResource);
					basePathTree.startEditingAtPath(dropRow);
				} catch (Exception e) {
					return false;
				} 
                return true;
            }
            
            public int getSourceActions(JComponent c) {
                return COPY;
            }
            
            /**
             * Create a new Transferable that is used to drag files from jeboorker to a native application.
             */
            protected Transferable createTransferable(JComponent c) {
            	return null;
            }
        });
		
		basePathTree.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				mainTable.stopEdit();
				mainTable.getSelectionModel().clearSelection();

				fileSystemTree.stopEditing();
				fileSystemTree.clearSelection();
			}
			
		});		
		
		GridBagConstraints gbc_tree = new GridBagConstraints();
		gbc_tree.fill = GridBagConstraints.BOTH;
		gbc_tree.gridx = 0;
		gbc_tree.gridy = 0;
		return basePathTreePanel;
	}
	
	/**
	 * Shows the cover popup menu for the selected entries.
	 * @param location The locaten where the popup should appears.
	 * @param invoker The invoker for the popup menu.
	 */	
	void showCoverPopupMenu(Point location, Component invoker) {
		List<EbookPropertyItem> selectedItems = MainController.getController().getSelectedEbookPropertyItems();
		JPopupMenu menu = createCoverPopupMenu(selectedItems);
		
		//setup and show popup
		if(menu.getComponentCount() > 0) {
			menu.setLocation(location);
			menu.show(invoker, location.x, location.y);
		}		
	}
	
	/**
	 * Shows the cover popup menu for the selected entries.
	 * @param location The locaten where the popup should appears.
	 * @param invoker The invoker for the popup menu.
	 */	
	void showTreePopupMenu(Point location, Component invoker) {
		final JPopupMenu menu = new JPopupMenu();
		
        //int selRow = tree.getRowForLocation((int)location.getX(), (int)location.getY());
        TreePath selPath = basePathTree.getPathForLocation((int)location.getX(), (int)location.getY());
        
		if(treeTabbedPane.getSelectedIndex() == 0) {
			addBasePathTreeMenuItems(menu, selPath);
		}
			
		//setup and show popup
		if(menu.getComponentCount() > 0) {
			menu.setLocation(location);
			menu.show(invoker, location.x, location.y);
		}		
	}	
	
	void addBasePathTreeMenuItems(JComponent menu, TreePath selPath) {
		Action action;
		
		FileSystemNode pathNode = (FileSystemNode) selPath.getLastPathComponent();
		
		action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.PASTE_FROM_CLIPBOARD_ACTION, pathNode.getResource().toString());
		menu.add(new JMenuItem(action));	
	}	
	
	/**
	 * Create the popup menu containing the cover actions.  
	 * @param items The items to be tested if they're matching against the menu entries.
	 * @return The desired {@link JPopupMenu}. Never returns <code>null</code>.
	 */
	private static JPopupMenu createCoverPopupMenu(List<EbookPropertyItem> items) {
		//create and fill popup menu
		final MainController controller = MainController.getController();
		int[] selectedEbookPropertyItemRows = controller.getSelectedEbookPropertyItemRows();
		final JPopupMenu menu = new JPopupMenu();
		
		addCoverMenuItems(menu, items, selectedEbookPropertyItemRows);
		return menu;
	}
	
	static void addCoverMenuItems(JComponent menu, List<EbookPropertyItem> items, int[] rowsToRefreshAfter) {
		if(!items.isEmpty()) {
			Action action;
			
			action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.SAVE_COVER_TO_CLIPBOARD_ACTION, items, rowsToRefreshAfter);
			menu.add(new JMenuItem(action));	
	
			action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.SET_COVER_FROM_CLIPBOARD_ACTION, items, rowsToRefreshAfter);
			menu.add(new JMenuItem(action));	
			
			menu.add(new JSeparator());
			
			action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.SET_COVER_FROM_FILE_ACTION, items, rowsToRefreshAfter);
			menu.add(new JMenuItem(action));
			
			action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.SET_COVER_FROM_DOWNLOAD_ACTION, items, rowsToRefreshAfter);
			menu.add(new JMenuItem(action));
			
			action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.SET_COVER_FROM_EBOOK_ACTION, items, rowsToRefreshAfter);
			menu.add(new JMenuItem(action));		
			
			action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.SAVE_COVER_TO_FILE_ACTION, items, rowsToRefreshAfter);
			menu.add(new JMenuItem(action));
		}
	}
	
	/**
	 * Shows a dialog to the user.
	 * @param message The message of the dialog
	 * @param title The dialog title.
	 * @param option The dialog option: JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.YES_NO_OPTION, JOptionPane.OK_CANCEL_OPTION
	 * @return 0: yes/ok, 1: no, 2:cancel, -1 none
	 */
	int showMessageBox(String message, String title, int option, String showAgainKey, int defaultValue) {
		Number showAgain = JeboorkerPreferences.getGenericEntryAsNumber(showAgainKey);
		if(showAgain == null) {
		    int n;
		    boolean dontShowAgain;
		    if(showAgainKey != null) {
			    JCheckBox checkbox = new JCheckBox(Bundle.getString("EborkerMainView.messagebox.showAgainMessage"));  
			    Object[] params = {message, checkbox};  
		    	n = JOptionPane.showConfirmDialog(this, params, title, option);
		    	dontShowAgain = checkbox.isSelected();  
		    } else {
		    	n = JOptionPane.showConfirmDialog(this, message, title, option);
		    	dontShowAgain = false;  		    	
		    }
		    
		    if(dontShowAgain) {
		    	if(defaultValue >= 0) {
		    		JeboorkerPreferences.addGenericEntryAsNumber(showAgainKey, defaultValue);
		    	} else {
		    		JeboorkerPreferences.addGenericEntryAsNumber(showAgainKey, n);
		    	}
		    }
			return n;
		} else {
			return showAgain.intValue();
		}
	}

	JTree getSelectedTreePathComponent() {
		JTree selectedComponent = (JTree) SwingUtils.getAllComponents(JTree.class, (Container) treeTabbedPane.getSelectedComponent())[0];
		return selectedComponent;
	}
	
	JTree getFileSystemTree() {
		return fileSystemTree;
	}
	
	private class MainViewPreferenceListener extends JeboorkerPreferenceListener {

		@Override
		public void treeAutoScrollingChanged(boolean value) {
			Component[] allComponents = SwingUtils.getAllComponents(JRTree.class, MainView.this.getRootPane());
			for(Component c : allComponents) {
				((JRTree)c).setAutoMoveHorizontalSliders(value);
			}
		}
		
	}
}
