package org.rr.jeborker.gui;

import static org.apache.commons.lang.ObjectUtils.notEqual;
import static org.rr.commons.utils.StringUtil.EMPTY;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
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
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;
import org.rr.commons.collection.FilterList;
import org.rr.commons.collection.FilterList.Filter;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.mufs.VirtualStaticResourceDataLoader;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.swing.components.JRScrollPane;
import org.rr.commons.swing.components.JRTable;
import org.rr.commons.swing.components.button.JMenuButton;
import org.rr.commons.swing.components.container.ShadowPanel;
import org.rr.commons.swing.components.tree.JRTree;
import org.rr.commons.swing.components.tree.TreeUtil;
import org.rr.commons.swing.components.util.EnablePropertyChangeHighlighterSupport;
import org.rr.commons.swing.dnd.DragAndDropUtils;
import org.rr.commons.swing.dnd.FileTransferable;
import org.rr.commons.swing.dnd.URIListTransferable;
import org.rr.commons.swing.image.SimpleImageViewer;
import org.rr.commons.swing.layout.EqualsLayout;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.JeboorkerPreferenceListener;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.event.ApplicationEvent;
import org.rr.jeborker.event.EventManager;
import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.gui.action.ActionUtils;
import org.rr.jeborker.gui.action.PasteFromClipboardAction;
import org.rr.jeborker.gui.cell.BasePathTreeCellEditor;
import org.rr.jeborker.gui.cell.BasePathTreeCellRenderer;
import org.rr.jeborker.gui.cell.DatePropertyCellEditor;
import org.rr.jeborker.gui.cell.DatePropertyCellRenderer;
import org.rr.jeborker.gui.cell.DefaultPropertyCellEditor;
import org.rr.jeborker.gui.cell.DefaultPropertyRenderer;
import org.rr.jeborker.gui.cell.EbookTableCellEditor;
import org.rr.jeborker.gui.cell.EbookTableCellRenderer;
import org.rr.jeborker.gui.cell.FileSystemTreeCellEditor;
import org.rr.jeborker.gui.cell.FileSystemTreeCellRenderer;
import org.rr.jeborker.gui.cell.MultiListPropertyEditor;
import org.rr.jeborker.gui.cell.MultiListPropertyRenderer;
import org.rr.jeborker.gui.cell.StarRatingPropertyEditor;
import org.rr.jeborker.gui.cell.StarRatingPropertyRenderer;
import org.rr.jeborker.gui.model.BasePathTreeModel;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;
import org.rr.jeborker.gui.model.EbookSheetPropertyModel;
import org.rr.jeborker.gui.model.EbookSheetPropertyMultiSelectionModel;
import org.rr.jeborker.gui.model.EmptyListModel;
import org.rr.jeborker.gui.model.FileSystemNode;
import org.rr.jeborker.gui.model.FileSystemTreeModel;
import org.rr.jeborker.gui.model.MetadataAddListModel;
import org.rr.jeborker.gui.resources.ImageResourceBundle;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.MetadataProperty;

import com.j256.ormlite.stmt.Where;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheet;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;

import net.miginfocom.swing.MigLayout;
import skt.swing.StringConvertor;


class MainView extends JFrame {

	private static final long serialVersionUID = 6837919427429399376L;

	private class MainViewPreferenceListener extends JeboorkerPreferenceListener {

		@Override
		public void treeAutoScrollingChanged(boolean value) {
			Component[] allComponents = SwingUtils.getAllComponents(JRTree.class, MainView.this.getRootPane());
			for(Component c : allComponents) {
				((JRTree)c).setAutoMoveHorizontalSliders(value);
			}
		}
	}

	/**
	 * ListSelectionListener which is invoked by changing the selection in the main table. It saves and sets the metadata properties of
	 * the {@link PropertySheet}.
	 */
	private class PropertySheetListSelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(!e.getValueIsAdjusting()) {
				Property selectedMetadataProperty = getSelectedMetadataProperty();
				refreshSheetProperties();
				setSelectedMetadataProperty(selectedMetadataProperty);
			}
		}
	}

	/**
	 * Mouse listener which handles the right click / popup menu on the main table.
	 */
	private class MainTablePopupMouseListener extends MouseAdapter {

		public void mouseReleased(MouseEvent event) {
			if (event.getButton() == MouseEvent.BUTTON3) {
				final int rowAtPoint = mainTable.rowAtPoint(event.getPoint());

				//set selection for the right click
				if(mainTable.getSelectedRowCount() <= 1 ) {
					mainTable.getSelectionModel().setSelectionInterval(rowAtPoint, rowAtPoint);
				}

				showMainTablePopupMenu(event.getPoint(), mainTable);
			}
		}
	}

	/**
	 * Mouse listener which handles the right click / popup menu on the main table.
	 */
	private class FileSystemTreePopupMouseListener extends MouseAdapter {

		public void mouseReleased(MouseEvent event) {

			if (event.getButton() == MouseEvent.BUTTON3) {
				final TreePath rowAtPoint = fileSystemTree.getPathForLocation(event.getX(), event.getY());

				//set selection for the right click
				if(fileSystemTree.getSelectionCount() <= 1 ) {
					fileSystemTree.setSelectionPath(rowAtPoint);
				}

				showFileSystemTreePopupMenu(event.getPoint(), fileSystemTree);
			}
		}
	}

	/**
	 * Mouse listener which handles the right click / popup menu on the main table.
	 */
	private class CoverPopupMouseListener extends MouseAdapter {

		public void mouseReleased(MouseEvent event) {
			if (event.getButton() == MouseEvent.BUTTON3) {
				showCoverPopupMenu(event.getPoint(), imageViewer);
			}
		}
	}

	/**
	 * Mouse listener which handles the right click / popup menu on the main table.
	 */
	private class BasePathTreePopupMouseListener extends MouseAdapter {

		public void mouseReleased(MouseEvent event) {
			if (event.getButton() == MouseEvent.BUTTON3) {
				Point location = event.getPoint();
				int row = basePathTree.getRowForLocation((int)location.getX(), (int)location.getY());
				if(row >= 0) {
					basePathTree.setSelectionRow(row);
					showBasePathTreePopupMenu(event.getPoint(), basePathTree);
				}
			}
		}
	}

	private final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);

	private JRTable mainTable;

	private JXLayer<JRTable> mainTableLayer;

	private JProgressBar progressBar;

	private JSplitPane mainSplitPane;

	private JSplitPane propertySheetImageSplitPane;

	private SimpleImageViewer imageViewer;

	private PropertySheetPanel propertySheet;

	private JMenuButton addMetadataButton;

	private JButton removeMetadataButton;

	private JButton saveMetadataButton;

	private SortColumnComponent sortColumnComponent;

	private FilterPanelComponent filterFieldComponent;

	private JSplitPane treeMainTableSplitPane;

	private JRTree basePathTree;

	private JRTree fileSystemTree;

	private JRScrollPane mainTableScrollPane;

	private JTabbedPane treeTabbedPane;

	private JPanel buttonPanel;

	private MainViewTreeComponentHandler treeComponentHandler;

	private MainViewPropertySheetHandler propertySheetHandler;

	private MainViewEbookTableComponentHandler ebookTableHandler;
	
	private MainTablePopupMouseListener mainTablePopupMouseListener;

	private MainController controller;

	/**
	 * Create the application.
	 */
	public MainView(MainController controller) {
		this.controller = controller;
	}

	private void initializeGlobalKeystrokes() {
		final InputMap inputMap = this.getRootPane().getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW);
		final ActionMap actionMap = this.getRootPane().getActionMap();

		inputMap.put(MainViewMenuUtils.QUIT_KEY, "QUIT");
		actionMap.put("QUIT", ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.QUIT_ACTION, null));

		inputMap.put(MainViewMenuUtils.SAVE_KEY, "SAVE");
		actionMap.put("SAVE", ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SAVE_METADATA_ACTION, null));

		inputMap.put(MainViewMenuUtils.FIND_KEY, "FIND");
		actionMap.put("FIND", ActionFactory.getTableFindAction(mainTable));
	}

	/**
	 * Initialize the contents of the frame.
	 */
	void initialize() {
		this.setTitle(Jeboorker.APP + " " + Jeboorker.getAppVersion());
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

		JPanel contentPane = new JPanel(new MigLayout("insets 3 5 3 5")); // T, L, B, R.
		contentPane.setOpaque(true);

		mainSplitPane = new JSplitPane();
		mainSplitPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
		mainSplitPane.setOneTouchExpandable(true);
		mainSplitPane.setResizeWeight(0.9);

		contentPane.add(mainSplitPane, "w 100%, h 100%, wrap");

		JPanel propertyContentPanel = new JPanel(new MigLayout("insets 0 0 0 0"));
		mainSplitPane.setLeftComponent(propertyContentPanel);

			sortColumnComponent = new SortColumnComponent();
			propertyContentPanel.add(sortColumnComponent, "h 25!, w 100%, wrap");

			treeMainTableSplitPane = new JSplitPane();
			treeMainTableSplitPane.setDividerLocation(220);
			propertyContentPanel.add(treeMainTableSplitPane, "h 100%, w 100%");

			createMainTable();

			mainTableScrollPane = new JRScrollPane();
			treeMainTableSplitPane.setRightComponent(mainTableScrollPane);
			mainTableLayer = new JXLayer<JRTable>(mainTable, new AbstractLayerUI<JRTable>() {

				@Override
				protected void processMouseEvent(final MouseEvent e, final JXLayer<? extends JRTable> l) {
					if(preferenceStore.getEntryAsBoolean(PreferenceStoreFactory.PREFERENCE_KEYS.MAIN_TABLE_AUTO_SAVE_METADATA_ENABLED)) {
						transferFocusOnClick(e, l);

						//save meta data and dispatch the mouse event to the jtable so it changes the selection
						if(saveMetadataButton.isEnabled()) {
							if(e.getID() == MouseEvent.MOUSE_PRESSED && e.getSource() == mainTable ) {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SAVE_METADATA_ACTION, null)
										.invokeAction(null, new Runnable() {
											public void run() {
												SwingUtilities.invokeLater(new Runnable() {

													@Override
													public void run() {
														MouseEvent click = new MouseEvent((Component) e.getSource(), MouseEvent.MOUSE_CLICKED, e.getWhen(),
																e.getModifiers(), e.getX(), e.getY(), e.getXOnScreen(), e.getYOnScreen(),
																e.getClickCount(), e.isPopupTrigger(), e.getButton());
														for(MouseListener ml: l.getView().getMouseListeners()){
														    ml.mousePressed(click);
														    ml.mouseReleased(click);
														    ml.mouseClicked(click);
														}
													}
												});
											}
										});
									}
								});

							}
							e.consume();
						}
					}
				}

				private void transferFocusOnClick(final MouseEvent e, final JXLayer<? extends JRTable> l) {
					if(e.getID() != MouseEvent.MOUSE_DRAGGED && e.getID() != MouseEvent.MOUSE_MOVED
							&& e.getID() != MouseEvent.MOUSE_ENTERED && e.getID() != MouseEvent.MOUSE_EXITED) {
						//transfer the focus cause that the edit mode in the meta data sheet
						l.getView().requestFocus();
					}
				}

				public long getLayerEventMask() {
					//fix for mouse wheel scrolling @see https://www.java.net//node/696371
					return AWTEvent.MOUSE_EVENT_MASK;
				}
			});
			mainTableScrollPane.setViewportView(mainTableLayer);

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

			JComponent fileSystemTreeComp = createFileSystemTree();
			treeTabbedPane.addTab(Bundle.getString("EborkerMainView.tabbedPane.fileSystem"), fileSystemTreeComp);

			JComponent basePathTreeComp = createBasePathTree();
			treeTabbedPane.addTab(Bundle.getString("EborkerMainView.tabbedPane.basePath"), basePathTreeComp);

			treeMainTableSplitPane.setLeftComponent(treeTabbedPane);
			treeMainTableSplitPane.setOneTouchExpandable(true);

			JPanel sheetPanel = new JPanel(new MigLayout("insets 0 0 0 0"));

			propertySheet = new PropertySheetPanel(new EbookSheetPropertyModel());
			propertySheet.setMode(PropertySheet.VIEW_AS_FLAT_LIST);
			propertySheet.setDescriptionVisible(true);
			propertySheet.setShowCategoryButton(false);

			addMetadataButton = new JMenuButton();
			addMetadataButton.setIcon(new ImageIcon(Bundle.getResource("add_metadata_16.png")));
			addMetadataButton.setText(EMPTY);
			addMetadataButton.setWidth(50);
			EmptyListModel<Action> emptyListModel = EmptyListModel.getSharedInstance();
			addMetadataButton.setListModel(emptyListModel);
			propertySheet.addToolbarComponent(addMetadataButton);

			removeMetadataButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REMOVE_METADATA_ENTRY_ACTION, null));
			propertySheet.addToolbarComponent(removeMetadataButton);

			saveMetadataButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SAVE_METADATA_ACTION, null));
			saveMetadataButton.setText(EMPTY);
			new EnablePropertyChangeHighlighterSupport(saveMetadataButton, Color.RED, 3);

			propertySheet.addToolbarComponent(saveMetadataButton);

			((PropertyRendererRegistry)propertySheet.getRendererFactory()).registerRenderer((Class<?>) null, DefaultPropertyRenderer.class);
			((PropertyEditorRegistry)propertySheet.getEditorFactory()).registerEditor((Class<?>) null, DefaultPropertyCellEditor.class);
			((PropertyRendererRegistry)propertySheet.getRendererFactory()).registerRenderer(String.class, DefaultPropertyRenderer.class);
			((PropertyEditorRegistry)propertySheet.getEditorFactory()).registerEditor(String.class, DefaultPropertyCellEditor.class);

			DatePropertyCellRenderer calendarDatePropertyRenderer = new DatePropertyCellRenderer(((SimpleDateFormat) SimpleDateFormat.getDateInstance()).toPattern());
	        ((PropertyEditorRegistry)propertySheet.getEditorFactory()).registerEditor(Date.class, new DatePropertyCellEditor());
	        ((PropertyRendererRegistry)propertySheet.getRendererFactory()).registerRenderer(Date.class, calendarDatePropertyRenderer);

	        ((PropertyEditorRegistry)propertySheet.getEditorFactory()).registerEditor("rating", StarRatingPropertyEditor.class);
	        ((PropertyRendererRegistry)propertySheet.getRendererFactory()).registerRenderer("rating", StarRatingPropertyRenderer.class);
	        ((PropertyEditorRegistry)propertySheet.getEditorFactory()).registerEditor("calibre:rating", StarRatingPropertyEditor.class);
	        ((PropertyRendererRegistry)propertySheet.getRendererFactory()).registerRenderer("calibre:rating", StarRatingPropertyRenderer.class);

	        ((PropertyEditorRegistry)propertySheet.getEditorFactory()).registerEditor(java.util.List.class, MultiListPropertyEditor.class);
	        ((PropertyRendererRegistry)propertySheet.getRendererFactory()).registerRenderer(java.util.List.class, MultiListPropertyRenderer.class);

			sheetPanel.add(propertySheet, "w 100%, h 100%");

			propertySheetImageSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			propertySheetImageSplitPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
			propertySheetImageSplitPane.setOneTouchExpandable(true);
			mainSplitPane.setRightComponent(propertySheetImageSplitPane);

			JPanel imageViewerPanel = new JPanel(new MigLayout("insets 0 0 0 0"));
			imageViewerPanel.setBorder(new EmptyBorder(3,3,3,3));
			imageViewer = new SimpleImageViewer();
			imageViewerPanel.add(imageViewer, "w 100%, h 100%");
			propertySheetImageSplitPane.setRightComponent(imageViewerPanel);
			propertySheetImageSplitPane.setLeftComponent(sheetPanel);
			propertySheetImageSplitPane.setDividerLocation(getSize().height / 2);

			mainSplitPane.setDividerLocation(getSize().width - 220);


		filterFieldComponent = new FilterPanelComponent();
		contentPane.add(filterFieldComponent, "w 100%, wrap");

		JPanel statusPanel = new JPanel(new MigLayout("insets 0 5 0 0"));
		contentPane.add(statusPanel, "w 100%, wrap");

		JLabel statusLabel = new JLabel(Bundle.getString("EborkerMainView.status"));
		statusPanel.add(statusLabel, "w 55!");

		progressBar = new JProgressBar();
		statusPanel.add(progressBar, "w 100%");

		this.setContentPane(contentPane);
		this.setJMenuBar(MainMenuBarController.getController().getView());

		initializeGlobalKeystrokes();

		treeComponentHandler = new MainViewTreeComponentHandler(basePathTree, fileSystemTree, this);
		propertySheetHandler = new MainViewPropertySheetHandler(propertySheet, this);
		ebookTableHandler = new MainViewEbookTableComponentHandler(mainTable, mainTableScrollPane);
	}

	/**
	 * Attach all needed listeners to the view
	 */
	void initListeners() {
		mainTable.getSelectionModel().addListSelectionListener(new PropertySheetListSelectionListener());
		mainTable.addMouseListener(getMainTablePopupMouseListener());

		imageViewer.addMouseListener(new CoverPopupMouseListener());
		basePathTree.addMouseListener(new BasePathTreePopupMouseListener());
		fileSystemTree.addMouseListener(new FileSystemTreePopupMouseListener());
		propertySheet.addPropertySheetChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if("value".equals(e.getPropertyName())) {
					//sheet has been edited
					EventManager.fireEvent(EventManager.EVENT_TYPES.METADATA_SHEET_CONTENT_CHANGE, new ApplicationEvent(getEbookTableHandler().getSelectedEbookPropertyItems(), getSelectedMetadataProperty(), e.getSource()));
				}
			}
		});

		getPropertySheetHandler().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()) {
					EventManager.fireEvent(EventManager.EVENT_TYPES.METADATA_SHEET_SELECTION_CHANGE, new ApplicationEvent(getEbookTableHandler().getSelectedEbookPropertyItems(), getSelectedMetadataProperty(), e.getSource()));
				}
			}
		});

		mainTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()) {
					EventManager.fireEvent(EventManager.EVENT_TYPES.EBOOK_ITEM_SELECTION_CHANGE, new ApplicationEvent(getEbookTableHandler().getSelectedEbookPropertyItems(), getSelectedMetadataProperty(), e.getSource()));
				}
			}
		});

		treeTabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				EventManager.fireEvent(EventManager.EVENT_TYPES.MAIN_TREE_VISIBILITY_CHANGED, new ApplicationEvent(getEbookTableHandler().getSelectedEbookPropertyItems(), getSelectedMetadataProperty(), e.getSource()));
			}
		});
		
		filterFieldComponent.initListeners();
		preferenceStore.addPreferenceChangeListener(new MainViewPreferenceListener());
	}

	private MouseListener getMainTablePopupMouseListener() {
		if(mainTablePopupMouseListener == null) {
			mainTablePopupMouseListener = new MainTablePopupMouseListener();
		}
		return mainTablePopupMouseListener;
	}

	private void createMainTable() {
		mainTable = new JRTable();
		mainTable.setName("MainTable");
		mainTable.setRowHeight(74);
		mainTable.setModel(new EbookPropertyDBTableModel(true));
		mainTable.setDefaultRenderer(Object.class, new EbookTableCellRenderer(getMainTablePopupMouseListener()));
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
		}, getMainTablePopupMouseListener()));
		mainTable.setTableHeader(null);
		DefaultListSelectionModel defaultListSelectionModel = new DefaultListSelectionModel();
		defaultListSelectionModel.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		mainTable.setSelectionModel(defaultListSelectionModel);
		mainTable.setDragEnabled(true);
		mainTable.setStopEditOnSelectionChange(true);

		MainViewMenuUtils.registerCopyToClipboardKeyAction(mainTable);
		MainViewMenuUtils.registerPasteFromClipboardKeyAction(mainTable);
		MainViewMenuUtils.registerDeleteKeyAction(mainTable);
		MainViewMenuUtils.registerRefreshEntryKeyAction(mainTable);
		MainViewMenuUtils.registerRenameFileKeyAction(mainTable);

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
				return StringUtil.toString(obj);
			}
		});

		mainTable.setTransferHandler(new TransferHandler() {

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
		        final List<URI> uriList = new ArrayList<>();
		        final List<String> files = new ArrayList<>();

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

	private JComponent createFileSystemTree() {
		final String fileSystemTreeName = "FileSystemTree";

		JPanel fileSystemTreePanel = new JPanel(new MigLayout("insets 3 0 0 0")); // T, L, B, R.
		fileSystemTreePanel.setBackground(SwingUtils.getBackgroundColor());
		fileSystemTreePanel.setOpaque(true);

		buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);

		fileSystemTreePanel.add(buttonPanel, "w 100%, wrap");
		buttonPanel.setLayout(new EqualsLayout(EqualsLayout.RIGHT, 3, true));

		Dimension buttonDimension = new Dimension(28, 28);

		JButton syncButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SYNC_FOLDER_ACTION, null));
		syncButton.setPreferredSize(buttonDimension);
		buttonPanel.add(syncButton);

		JButton collapseButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.FILE_SYSTEM_COLLAPSE_ALL_ACTION, fileSystemTreeName));
		collapseButton.setPreferredSize(buttonDimension);
		buttonPanel.add(collapseButton);

		fileSystemTree = new JRTree();
		setupTree(fileSystemTree);

		fileSystemTree.setName(fileSystemTreeName);
		fileSystemTree.setSelectionModel(new DefaultTreeSelectionModel());
		FileSystemTreeModel fileSystemTreeModel = new FileSystemTreeModel();
		fileSystemTree.setModel(fileSystemTreeModel);
		fileSystemTree.setAutoMoveHorizontalSliders(preferenceStore.isTreeAutoScrollingEnabled());
		fileSystemTree.setEditable(true);
		FileSystemTreeCellRenderer fileSystemTreeCellRenderer = new FileSystemTreeCellRenderer();
		fileSystemTree.setCellRenderer(fileSystemTreeCellRenderer);
		fileSystemTree.setCellEditor(new FileSystemTreeCellEditor(fileSystemTree, fileSystemTreeCellRenderer));
		fileSystemTree.addTreeExpansionListener(new TreeExpansionListener() {

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				// reset the file node cache before open to take changed folders under account
				TreePath path = event.getPath();
				Object collapsedNode = path.getLastPathComponent();
				if(collapsedNode instanceof FileSystemNode) {
					((FileSystemNode) collapsedNode).reset();
				}
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {}
		});
		
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
		fileSystemTree.registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath[] selectionPaths = fileSystemTree.getSelectionPaths();
				if(selectionPaths.length == 0) {
					return;
				} else if(selectionPaths.length == 1) {
					Object lastPathComponent = selectionPaths[0].getLastPathComponent();
					if(lastPathComponent instanceof FileSystemNode && ((FileSystemNode)lastPathComponent).getResource().isFileResource()) {
						fileSystemTree.startEditingAtPath(selectionPaths[0]);
					}
				} else {
					for(TreePath selectionPath : selectionPaths) {
						Object lastPathComponent = selectionPath.getLastPathComponent();
						if(lastPathComponent instanceof FileSystemNode && ((FileSystemNode)lastPathComponent).getResource().isDirectoryResource()) {
							return;
						}
					}
					ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.RENAME_FILE_ACTION, null).actionPerformed(e);
				}

			}
		}, "RenameFile", MainViewMenuUtils.RENAME_KEY, JComponent.WHEN_FOCUSED);

		JRScrollPane treeScroller = new JRScrollPane(fileSystemTree);
		treeScroller.setOpaque(false);
		treeScroller.getViewport().setOpaque(false);

		fileSystemTreePanel.add(treeScroller, "w 100%, h 100%");

		fileSystemTree.setRootVisible(false);
		fileSystemTree.setRowHeight(25);

		MainViewMenuUtils.registerCopyToClipboardKeyAction(fileSystemTree);
		MainViewMenuUtils.registerPasteFromClipboardKeyAction(fileSystemTree);
		MainViewMenuUtils.registerDeleteKeyAction(fileSystemTree);
		MainViewMenuUtils.registerFileSystemRefreshKeyAction(fileSystemTree);

		fileSystemTree.setDragEnabled(true);
		fileSystemTree.setTransferHandler(new TransferHandler() {

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
					if (targetPathResource.isFileResource()) {
						targetPathResource = targetPathResource.getParentResource();
						reloadParent = true;
					}
					Transferable transferable = info.getTransferable();
					List<IResourceHandler> sourceResourceHandlers = ResourceHandlerFactory.getResourceHandler(transferable);
					for (IResourceHandler sourceResourceHandler : sourceResourceHandlers) {
						String basePathFor = preferenceStore.getBasePathFor(targetPathResource);
						if (basePathFor != null) {
							// drop to a folder that is managed by jeboorker.
							PasteFromClipboardAction.importEbookFromClipboard(transferable, Integer.MIN_VALUE, basePathFor, targetPathResource);
						} else {
							// do a simple copy
							IResourceHandler targetPathResourceFile = targetPathResource.addPathStatement(sourceResourceHandler.getName());
							if(notEqual(sourceResourceHandler, targetPathResourceFile)) {
								IResourceHandler uniqueTargetPathResourceFile = ResourceHandlerFactory.getUniqueResourceHandler(targetPathResourceFile,
										targetPathResourceFile.getFileExtension());
								sourceResourceHandler.copyTo(uniqueTargetPathResourceFile, false);
							}
						}
						if (reloadParent) {
							TreeNode node = (TreeNode) dropRow.getLastPathComponent();
							TreeNode parentNode = node.getParent();
							if (parentNode != null) {
								((DefaultTreeModel) fileSystemTree.getModel()).reload(parentNode);
							} else {
								((DefaultTreeModel) fileSystemTree.getModel()).reload(node);
							}
						} else {
							((DefaultTreeModel) fileSystemTree.getModel()).reload((TreeNode) dropRow.getLastPathComponent());
						}
					}
				} catch (Exception e) {
					LoggerFactory.getLogger(this).log(Level.WARNING, e.getMessage(), e);
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
				List<IResourceHandler> selectedTreeItems = MainController.getController().getMainTreeHandler().getSelectedTreeItems();
				final List<URI> uriList = new ArrayList<>(selectedTreeItems.size());
				final List<String> files = new ArrayList<>(selectedTreeItems.size());

				for (int i = 0; i < selectedTreeItems.size(); i++) {
					IResourceHandler selectedTreeItem = selectedTreeItems.get(i);
					try {

						uriList.add(selectedTreeItem.toFile().toURI());
						files.add(selectedTreeItem.toFile().getPath());
					} catch (Exception e) {
						LoggerFactory.getLogger().log(Level.WARNING, "Failed to encode " + selectedTreeItem.toString(), e);
					}
				}

				if (CommonUtils.isLinux()) {
					if (ReflectionUtils.javaVersion() == 16) {
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
		
		fileSystemTree.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				TreePath[] selectionPaths = fileSystemTree.getSelectionPaths();
				List<IResourceHandler> resources = new FilterList<>(toIResourceHandler(selectionPaths), new Filter<IResourceHandler>() {

					@Override
					public boolean isFiltered(IResourceHandler resource) {
						return resource == null || !ActionUtils.isSupportedEbookFormat(resource, false);
					}
				});
				
				if(!resources.isEmpty() || controller.isEbookPropertyFileTableModel()) {
					controller.changeToFileModel(resources);
				}
			}
			
			private List<IResourceHandler> toIResourceHandler(TreePath[] path) {
				if(path != null) {
					List<IResourceHandler> result = new ArrayList<>(path != null ? path.length : 0);
					for (TreePath value : path) {
						Object node = value.getLastPathComponent();
						if(node instanceof FileSystemNode) {
							result.add(((FileSystemNode) node).getResource());
						}
					}
					return result;
				}
				return Collections.emptyList();
			}
			
		});

		return fileSystemTreePanel;
	}

	private JComponent createBasePathTree() {
		final String basePathTreeName = "BasePathTree";

		JPanel basePathTreePanel = new JPanel(new MigLayout("insets 3 0 0 0")); // T, L, B, R.
		basePathTreePanel.setBackground(SwingUtils.getBackgroundColor());
		basePathTreePanel.setOpaque(true);

		buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);

		basePathTreePanel.add(buttonPanel, "w 100%, wrap");
		buttonPanel.setLayout(new EqualsLayout(EqualsLayout.RIGHT, 3, true));

		Dimension buttonDimension = new Dimension(28, 28);

		JButton syncButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SYNC_FOLDER_ACTION, null));
		syncButton.setPreferredSize(buttonDimension);
		buttonPanel.add(syncButton);

		JButton addButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.ADD_BASE_PATH_ACTION, null));
		addButton.setPreferredSize(buttonDimension);
		addButton.setText(EMPTY);
		buttonPanel.add(addButton);

		JButton collapseButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.FILE_SYSTEM_COLLAPSE_ALL_ACTION, basePathTreeName));
		collapseButton.setPreferredSize(buttonDimension);
		buttonPanel.add(collapseButton);

		basePathTree = new JRTree();
		setupTree(basePathTree);
		basePathTree.setName(basePathTreeName);
		basePathTree.setModel(new BasePathTreeModel());
		basePathTree.setEditable(true);
		BasePathTreeCellRenderer basePathTreeCellRenderer = new BasePathTreeCellRenderer(basePathTree);
		basePathTree.setCellRenderer(basePathTreeCellRenderer);
		basePathTree.setCellEditor(new BasePathTreeCellEditor(basePathTree));
		basePathTree.setToggleExpandOnDoubleClick(true);
		basePathTree.setAutoMoveHorizontalSliders(preferenceStore.isTreeAutoScrollingEnabled());
		basePathTree.setRepaintAllOnChange(true);
		basePathTree.setEditable(true);
		JRScrollPane basePathTreeScroller = new JRScrollPane(basePathTree);
		basePathTreeScroller.setOpaque(false);
		basePathTreeScroller.getViewport().setOpaque(false);
		basePathTreePanel.add(basePathTreeScroller, "w 100%, h 100%");

		basePathTree.setRootVisible(false);
		basePathTree.setRowHeight(25);

		MainViewMenuUtils.registerDeleteKeyAction(basePathTree);

		basePathTree.addMouseListener(new MouseAdapter() {

			private static final String QUERY_IDENTIFER = "BASE_PATH_MOUSE_LISTENER";

			private Object previousEditorValue;

			@Override
			public void mousePressed(MouseEvent e) {
				final int row = basePathTree.getRowForLocation(e.getPoint().x, e.getPoint().y);
				final TreePath filterTreePath = basePathTree.getPathForRow(row);

				if(filterTreePath != null) {
					Object cellEditorValue = filterTreePath.getLastPathComponent();
					if(cellEditorValue == null || !cellEditorValue.equals(previousEditorValue)) {
						if(cellEditorValue instanceof FileSystemNode) {
							setPathFilter(((FileSystemNode)cellEditorValue).getName());
							((BasePathTreeModel)basePathTree.getModel()).setFilterTreePath(filterTreePath);
							MainController.getController().getEbookTableHandler().refreshTable();
						} else {
							boolean remove = MainController.getController().changeToDatabaseModel().removeWhereCondition(QUERY_IDENTIFER);
							((BasePathTreeModel)basePathTree.getModel()).setFilterTreePath(null);
							if(remove) {
								MainController.getController().getEbookTableHandler().refreshTable();
							}
						}
					}
					previousEditorValue = cellEditorValue;
				}
			}


			private void setPathFilter(final String fullResourceFilterPath) {
				MainController.getController().changeToDatabaseModel().addWhereCondition(new EbookPropertyDBTableModel.EbookPropertyDBTableModelQuery() {

					@Override
					public String getIdentifier() {
						return QUERY_IDENTIFER;
					}

					@Override
					public void appendQuery(Where<EbookPropertyItem, EbookPropertyItem> where) throws SQLException {
						String fullResourceFilterPathStatement = StringUtil.replace(fullResourceFilterPath, "\\", "\\\\");
						where.like("file", fullResourceFilterPathStatement + "%");
					}
				});
			}
		});

		basePathTree.setTransferHandler(new TransferHandler() {

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
					try {
						LoggerFactory.getLogger().log(Level.WARNING, "Failed to import " + ResourceHandlerFactory.getResourceHandler(info.getTransferable()) + " to " + lastPathPathResource, e);
					} catch (Exception e1) {}
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

		return basePathTreePanel;
	}

	private void setupTree(JRTree tree) {
		tree.setLargeModel(true);
		tree.setRowHeight(0);
		tree.setShowsRootHandles(true);
	}

	/**
	 * Shows the cover popup menu for the selected entries.
	 * @param location The location where the popup should appears.
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
	 * Shows the base path tree popup menu for the selected entries.
	 * @param location The location where the popup should appears.
	 * @param invoker The invoker for the popup menu.
	 */
	void showBasePathTreePopupMenu(Point location, Component invoker) {
		JPopupMenu menu = new JPopupMenu();
        TreePath selPath = basePathTree.getPathForLocation((int)location.getX(), (int)location.getY());
        if(selPath.getLastPathComponent() instanceof FileSystemNode) {
	        final FileSystemNode pathNode = (FileSystemNode) selPath.getLastPathComponent();
	        List<IResourceHandler> items = controller.getMainTreeHandler().getSelectedTreeItems();

			Action action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.PASTE_FROM_CLIPBOARD_ACTION, pathNode.getResource().toString());
			menu.add(new JMenuItem(action));

			menu.add(MainViewMenuUtils.createOpenFolderMenuItem(items));
			menu.add(MainViewMenuUtils.createNewFolderMenuItem(basePathTree, fileSystemTree, pathNode));
			
			JMenuItem deleteMenuItem = MainViewMenuUtils.createDeleteMenuItem(items);
			if(PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).getBasePath().isOneEntryABasePathMember(items)) {
				deleteMenuItem.setEnabled(false);
			}
			menu.add(deleteMenuItem);

			//setup and show popup
			if(menu.getComponentCount() > 0) {
				menu.setLocation(location);
				menu.show(invoker, location.x, location.y);
			}
        }
	}

	/**
	 * Shows the popup menu for the selected entries.
	 * @param location The locaten where the popup should appears.
	 * @param invoker The invoker for the popup menu.
	 */
	void showFileSystemTreePopupMenu(Point location, Component invoker) {
		TreePath selPath = fileSystemTree.getPathForLocation((int)location.getX(), (int)location.getY());
		JPopupMenu menu = createFileSystemTreePopupMenu(selPath);

		//setup and show popup
		if(menu.getComponentCount() > 0) {
			menu.setLocation(location);
			menu.show(invoker, location.x, location.y);
		}
	}

	private JPopupMenu createFileSystemTreePopupMenu(final TreePath selPath) {
		final MainController controller = MainController.getController();
		final List<IResourceHandler> items = controller.getMainTreeHandler().getSelectedTreeItems();
		final FileSystemNode pathNode = (FileSystemNode) selPath.getLastPathComponent();
		final JPopupMenu menu = new JPopupMenu();

		if(items.size() == 1) {
			// only visible to single selections
			if(items.get(0).isDirectoryResource()) {
				menu.add(MainViewMenuUtils.createFileSystemRefreshMenuItem(items));
			}
			menu.add(MainViewMenuUtils.createOpenFileMenuItem(items));
			menu.add(MainViewMenuUtils.createOpenFolderMenuItem(items));
			menu.add(MainViewMenuUtils.createNewFolderMenuItem(basePathTree, fileSystemTree, pathNode));
		}
		if(items.size() >= 1) {
			menu.add(MainViewMenuUtils.createImportToMenu(items, Bundle.getString("MainMenuBarController.import"), ActionFactory.COMMON_ACTION_TYPES.FILE_SYSTEM_IMPORT_ACTION));
		}

		menu.add(MainViewMenuUtils.createSendToMenu());
		menu.add(MainViewMenuUtils.createDeleteMenuItem(items));

		return menu;
	}

	/**
	 * Shows the popup menu for the selected entries.
	 * @param location The locaten where the popup should appears.
	 * @param invoker The invoker for the popup menu.
	 */
	void showMainTablePopupMenu(Point location, Component invoker) {
		JPopupMenu menu = createMainTablePopupMenu();

		//setup and show popup
		if(menu.getComponentCount() > 0) {
			menu.setLocation(location);
			menu.show(invoker, location.x, location.y);
		}
	}

	/**
	 * Creates the popup menu for the main table having only these entries inside
	 * that can be processed with the given {@link EbookPropertyItem} list.
	 * @param items The items to be tested if they're matching against the menu entries.
	 * @return The desired {@link JPopupMenu}. Never returns <code>null</code>.
	 */
	private static JPopupMenu createMainTablePopupMenu() {
		final MainController controller = MainController.getController();
		final List<EbookPropertyItem> items = MainController.getController().getSelectedEbookPropertyItems();
		final List<IResourceHandler> selectedResources = EbookPropertyItemUtils.createIResourceHandlerList(items);
		final int[] selectedEbookPropertyItemRows = controller.getSelectedEbookPropertyItemRows();
		final JPopupMenu menu = new JPopupMenu();

		Action action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.EDIT_PLAIN_METADATA_ACTION, items, selectedEbookPropertyItemRows);
		if(action.isEnabled()) {
			menu.add(action);
		}

		action = ActionFactory.getActionForItems(ActionFactory.DYNAMIC_ACTION_TYPES.REFRESH_ENTRY_ACTION, items, selectedEbookPropertyItemRows);
		JMenuItem item = new JMenuItem(action);
		item.setAccelerator(MainViewMenuUtils.REFRESH_KEY);
		if(action.isEnabled()) {
			menu.add(item);
		}

		menu.add(MainViewMenuUtils.createRenameFileMenuItem());

		if(items.size() == 1) {
			//only visible to single selections
			menu.add(MainViewMenuUtils.createOpenFileMenuItem(selectedResources));
			menu.add(MainViewMenuUtils.createOpenFolderMenuItem(selectedResources));
		}

		menu.add(MainViewMenuUtils.createImportToMenu(selectedResources, Bundle.getString("MainMenuBarController.move"), ActionFactory.COMMON_ACTION_TYPES.MOVE_BETWEEN_BASE_FOLDER_ACTION));
		menu.add(MainViewMenuUtils.createSendToMenu());
		menu.add(MainViewMenuUtils.createDeleteMenuItem(selectedResources));

		return menu;
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
	int showMessageBox(String message, String title, int option, String showAgainKey, int defaultValue, boolean isConfirmDialog) {
		Number showAgain = preferenceStore.getGenericEntryAsNumber(showAgainKey);
		if(showAgain == null) {
		    int n = defaultValue;
		    boolean dontShowAgain;

		    if(showAgainKey != null) {
			    JCheckBox checkbox = new JCheckBox(Bundle.getString("EborkerMainView.messagebox.showAgainMessage"));
			    Object[] params = {message, checkbox};
			    if(isConfirmDialog) {
			    	n = JOptionPane.showConfirmDialog(this, params, title, option);
			    } else {
			    	JOptionPane.showMessageDialog(this, params, title, option);
			    }
		    	dontShowAgain = checkbox.isSelected();
		    } else {
		    	if(isConfirmDialog) {
		    		n = JOptionPane.showConfirmDialog(this, message, title, option);
		    	} else {
		    		JOptionPane.showMessageDialog(this, message, title, option);
		    	}
		    	dontShowAgain = false;
		    }

		    if(dontShowAgain) {
		    	if(defaultValue >= 0) {
		    		preferenceStore.addGenericEntryAsNumber(showAgainKey, defaultValue);
		    	} else {
		    		preferenceStore.addGenericEntryAsNumber(showAgainKey, n);
		    	}
		    }
			return n;
		} else {
			return showAgain.intValue();
		}
	}

	JTree getSelectedTreePathComponent() {
		Component[] allComponents = SwingUtils.getAllComponents(JTree.class, (Container) treeTabbedPane.getSelectedComponent());
		if(allComponents.length >= 1) {
			return (JTree) allComponents[0];
		}
		return null;
	}
	
	JTree getFileSystemTree() {
		return fileSystemTree;
	}

	/**
	 * Tells the text filter field to display it self in and active filter color.
	 */
	public void setFilterColorEnabled(boolean enable) {
		filterFieldComponent.enableFilterColor(enable);
	}

	public void refreshUI() {
		// don't know why but otherwise the renderer won't work after changing the look and feel
		if(!(mainTable.getDefaultRenderer(Object.class) instanceof EbookTableCellRenderer)) {
			mainTable.setDefaultRenderer(Object.class, new EbookTableCellRenderer(getMainTablePopupMouseListener()));
		}
	}

	/**
	 * Get the currently selected metadata property from the metadata sheet.
	 * @return The desired {@link Property} instance or <code>null</code> if no selection is made.
	 */
	public Property getSelectedMetadataProperty() {
		final int selectedRow = propertySheet.getTable().getSelectedRow();

		if(selectedRow >= 0) {
			final EbookSheetPropertyModel model = (EbookSheetPropertyModel) propertySheet.getModel();
			final PropertySheetTableModel.Item item = (Item) model.getObject(selectedRow);
			final Property property = item.getProperty();

			return property;
		}
		return null;
	}

	/**
	 * Set the given property as selected one in the metadata sheet.
	 * @param property The property to be set as selected.
	 */
	public void setSelectedMetadataProperty(final Property property) {
		if(property != null) {
			final EbookSheetPropertyModel model = getPropertySheetHandler().getModel();
			final int rowCount = model.getRowCount();

			for (int i = 0; i < rowCount; i++) {
				final PropertySheetTableModel.Item item = (Item) model.getObject(i);

				if(item != null && item.getName() != null && item.getName().equals(model.getDisplayName(property))) {
					propertySheet.getTable().getSelectionModel().setSelectionInterval(i, i);
					break;
				} else {
					if(property != null && item != null && item.getProperty() != null && item.getProperty().getName() != null && item.getProperty().getName().equals(property.getName())) {
						propertySheet.getTable().getSelectionModel().setSelectionInterval(i, i);
						break;
					}
				}
			}
		}
	}

	/**
	 * Rereads the metadata properties and set them to the sheet.
	 */
	public void refreshSheetProperties() {
		try {
			if(MainViewSelectionUtils.isMainTableSelection()) {
				List<EbookPropertyItem> items = getEbookTableHandler().getSelectedEbookPropertyItems();

				if(items.size() > 1) {
					//multiple selection
					EbookSheetPropertyMultiSelectionModel model = new EbookSheetPropertyMultiSelectionModel();
					getPropertySheetHandler().setModel(model);

					model.loadProperties(items);

					clearImage();
					EmptyListModel<Action> emptyListModel = EmptyListModel.getSharedInstance();
					addMetadataButton.setListModel(emptyListModel);
				} else if (items.size() == 1) {
					//single selection
					EbookSheetPropertyModel model = new EbookSheetPropertyModel();
					getPropertySheetHandler().setModel(model);

					if(items.get(0) != null) {
						EbookPropertyItem ebookPropertyItem = items.get(0);
						model.loadProperties(ebookPropertyItem);
						byte[] cover = model.getCover();
						if(cover != null && cover.length > 0) {
							setImage(cover, ebookPropertyItem);
						} else {
							clearImage();
						}

						IMetadataReader reader = model.getMetadataReader();
						if(reader != null) {
							List<MetadataProperty> allMetadata = model.getAllMetadata();
							MetadataAddListModel metadataAddListModel = new MetadataAddListModel(reader.getSupportedMetadata(), allMetadata, ebookPropertyItem);
							addMetadataButton.setListModel(metadataAddListModel);
						}
					}
				}
			} else {
				//no selection
				getPropertySheetHandler().setModel(new EbookSheetPropertyMultiSelectionModel());
				clearImage();
				EmptyListModel<Action> emptyListModel = EmptyListModel.getSharedInstance();
				addMetadataButton.setListModel(emptyListModel);
			}
		} catch (Exception e) {
			LoggerFactory.getLogger().log(Level.WARNING, "Refresh property sheet has failed.", e);
		}
	}

	/**
	 * Clears the image in the image viewer.
	 */
	private void clearImage() {
		setImage(null, null);
	}

	/**
	 * Shows the image given with the <code>cover</code> parameter in the simple image viewer.
	 * The image viewer is set to black if the given <code>cover</code> is <code>null</code>.
	 */
	private void setImage(final byte[] cover, final EbookPropertyItem ebookPropertyItem) {
		if (cover != null && ebookPropertyItem != null) {
			//remove file extension by removing the separation dot because an image file name is expected.
			final String coverFileName = StringUtil.replace(ebookPropertyItem.getResourceHandler().getResourceString(), new String[] {".", "/", "\\"}, "_");
			imageViewer.setImageViewerResource(ResourceHandlerFactory.getVirtualResourceHandler(coverFileName, new VirtualStaticResourceDataLoader() {

			ByteArrayInputStream byteArrayInputStream = null;

			@Override
			public InputStream getContentInputStream() {
				if(byteArrayInputStream == null) {
					byteArrayInputStream = new ByteArrayInputStream(cover);
				}
				byteArrayInputStream.reset();
				return byteArrayInputStream;
			}

			@Override
			public long length() {
				return cover.length;
			}
			}));
		} else {
			imageViewer.setImageViewerResource(null);
		}
	}

	/**
	 * Writes the application properties to the preference file
	 */
	void storeApplicationProperties() {
		APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		preferenceStore.addGenericEntryAsNumber("mainWindowSizeWidth", getSize().width);
		preferenceStore.addGenericEntryAsNumber("mainWindowSizeHeight", getSize().height);
		preferenceStore.addGenericEntryAsNumber("mainWindowLocationX", getLocation().x);
		preferenceStore.addGenericEntryAsNumber("mainWindowLocationY", getLocation().y);
		preferenceStore.addGenericEntryAsNumber("mainWindowDividerLocation", CommonUtils.toNumber(mainSplitPane.getDividerLocation()));
		preferenceStore.addGenericEntryAsNumber("lastRowCount", Integer.valueOf(mainTable.getRowCount()));
		preferenceStore.addGenericEntryAsNumber("descriptionDividerLocation", Integer.valueOf(propertySheet.getDescriptionDividerLocation()));
		preferenceStore.addGenericEntryAsNumber("treeMainTableDividerLocation", Integer.valueOf(treeMainTableSplitPane.getDividerLocation()));
		preferenceStore.addGenericEntryAsNumber("propertySheetImageSplitPaneDividerLocation", Integer.valueOf(propertySheetImageSplitPane.getDividerLocation()));
		preferenceStore.addGenericEntryAsString("basePathTreeSelection", TreeUtil.getExpansionStates(basePathTree));
		preferenceStore.addGenericEntryAsString("fileSystemTreeSelection", TreeUtil.getExpansionStates(fileSystemTree));

		sortColumnComponent.storeApplicationProperties();
		filterFieldComponent.storeApplicationHistory();
	}

	/**
	 * Restores the application properties
	 */
	void restoreComponentProperties() {
		APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);

		//restore the window size from the preferences.
		Number mainWindowSizeWidth = preferenceStore.getGenericEntryAsNumber("mainWindowSizeWidth");
		Number mainWindowSizeHeight = preferenceStore.getGenericEntryAsNumber("mainWindowSizeHeight");
		if(mainWindowSizeWidth!=null && mainWindowSizeHeight!=null) {
			setSize(mainWindowSizeWidth.intValue(), mainWindowSizeHeight.intValue());
		}

		//restore window location
		Point entryAsScreenLocation = preferenceStore.getGenericEntryAsScreenLocation("mainWindowLocationX", "mainWindowLocationY");
		if(entryAsScreenLocation != null) {
			setLocation(entryAsScreenLocation);
		}

		//restore the divider location at the main window
		final Number mainWindowDividerLocation = preferenceStore.getGenericEntryAsNumber("mainWindowDividerLocation");
		if(mainWindowDividerLocation != null) {
			int add = 0;
			if(ReflectionUtils.getOS() == ReflectionUtils.OS_LINUX) {
				//however, the splitpane has a difference of 9 between setting and getting the location.
				add = -9;
			}
			mainSplitPane.setDividerLocation(mainWindowDividerLocation.intValue() + add);
		}

		//restore the divider location at the main window
		final Number treeMainTableDividerLocation = preferenceStore.getGenericEntryAsNumber("treeMainTableDividerLocation");
		if(treeMainTableDividerLocation != null) {
			treeMainTableSplitPane.setDividerLocation(treeMainTableDividerLocation.intValue());
		}

		//restore the divider location in the property sheet
		final Number descriptionDividerLocation = preferenceStore.getGenericEntryAsNumber("descriptionDividerLocation");
		if(descriptionDividerLocation != null) {
			propertySheet.setDescriptionDividerLocation(descriptionDividerLocation.intValue());
		}

		final Number propertySheetImageSplitPaneDividerLocation = preferenceStore.getGenericEntryAsNumber("propertySheetImageSplitPaneDividerLocation");
		if (propertySheetImageSplitPaneDividerLocation != null) {
			propertySheetImageSplitPane.setDividerLocation(propertySheetImageSplitPaneDividerLocation.intValue());
		}

		final String basePathTreeSelection = preferenceStore.getGenericEntryAsString("basePathTreeSelection");
		if(basePathTreeSelection != null) {
			TreeUtil.restoreExpanstionState(basePathTree, basePathTreeSelection);
		}

		final String fileSystemTreeSelection = preferenceStore.getGenericEntryAsString("fileSystemTreeSelection");
		if(fileSystemTreeSelection != null) {
			TreeUtil.restoreExpanstionState(fileSystemTree, fileSystemTreeSelection);
		}

		filterFieldComponent.restoreComponentProperties();
		sortColumnComponent.restoreComponentProperties();
	}

	public List<Field> getSelectedFilterFields() {
		return filterFieldComponent.getSelectedFilterFields();
	}

	public String getFilterText() {
		return filterFieldComponent.getFilterText();
	}

	public void addFilterFieldSearch(String filterText) {
		filterFieldComponent.addFilterFieldSearch(filterText);
	}

	public List<Field> getSortColumnSelectedFields() {
		return sortColumnComponent.getSelectedFields();
	}

	public MainViewTreeComponentHandler getTreeComponentHandler() {
		return treeComponentHandler;
	}

	public MainViewPropertySheetHandler getPropertySheetHandler() {
		return propertySheetHandler;
	}

	/**
	 * Sets the image which is provided by the given {@link IResourceHandler} to the
	 * image viewer in the main view.
	 * @param imageResource The {@link IResourceHandler} instance providing the image
	 * data to be displayed. <code>null</code>if no image should be displayed.
	 */
	public void setImageViewerResource(IResourceHandler imageResource) {
		imageViewer.setImageViewerResource(imageResource);
	}

	/**
	 * Gets the {@link IResourceHandler} for the image which is displayed in the image viewer.
	 * @return The desired {@link IResourceHandler} or <code>null</code>.
	 */
	public IResourceHandler getImageViewerResource() {
		return imageViewer.getImageResource();
	}

	/**
	 * Gets the {@link BufferedImage} for the image which is displayed in the image viewer.
	 * @return The desired {@link BufferedImage} or <code>null</code>.
	 */
	public BufferedImage getImageViewerImage() {
		return imageViewer.getImage();
	}

	/**
	 * Gets the progress indicator.
	 * @return The desired monitor instance of <code>null</code> if the monitor is not ready to use.
	 */
	public MainMonitor getProgressMonitor() {
		if(progressBar != null) {
			return MainMonitor.getInstance(progressBar);
		}
		return null;
	}

	public MainViewEbookTableComponentHandler getEbookTableHandler() {
		return ebookTableHandler;
	}

}
