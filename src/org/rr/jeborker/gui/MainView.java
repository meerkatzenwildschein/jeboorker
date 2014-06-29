package org.rr.jeborker.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
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
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
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
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.japura.gui.CheckComboBox;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.mufs.ResourceHandlerUtils;
import org.rr.commons.mufs.VirtualStaticResourceDataLoader;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.swing.components.JRButton;
import org.rr.commons.swing.components.JRScrollPane;
import org.rr.commons.swing.components.JRTable;
import org.rr.commons.swing.components.button.JMenuButton;
import org.rr.commons.swing.components.container.ShadowPanel;
import org.rr.commons.swing.components.tree.JRTree;
import org.rr.commons.swing.components.util.EnablePropertyChangeHighlighterSupport;
import org.rr.commons.swing.dnd.DragAndDropUtils;
import org.rr.commons.swing.dnd.FileTransferable;
import org.rr.commons.swing.dnd.URIListTransferable;
import org.rr.commons.swing.image.SimpleImageViewer;
import org.rr.commons.swing.layout.EqualsLayout;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.JeboorkerPreferenceListener;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.event.ApplicationEvent;
import org.rr.jeborker.event.EventManager;
import org.rr.jeborker.gui.action.ActionCallback;
import org.rr.jeborker.gui.action.ActionFactory;
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
import org.rr.jeborker.gui.cell.FilterFieldComboboxEditor;
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

import skt.swing.StringConvertor;

import com.j256.ormlite.stmt.Where;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheet;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;


class MainView extends JFrame {

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

				MainMenuBarController.getController().showMainPopupMenu(event.getPoint(), mainTable);
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

	private static final long serialVersionUID = 6837919427429399376L;

	private final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);

	JRTable mainTable;

	private JXLayer<JRTable> mainTableLayer;

	JProgressBar progressBar;

	JSplitPane mainSplitPane;

	JSplitPane propertySheetImageSplitPane;

	SimpleImageViewer imageViewer;

	PropertySheetPanel propertySheet;

	JMenuButton addMetadataButton;

	private JButton removeMetadataButton;

	private JRButton saveMetadataButton;

	private JPanel sortPanel;

	private JLabel sortLabel;

	CheckComboBox<Field> sortColumnComboBox;

	JToggleButton sortOrderAscButton;

	JToggleButton sortOrderDescButton;

	JSplitPane treeMainTableSplitPane;

	JRTree basePathTree;

	JRTree fileSystemTree;

	JRScrollPane mainTableScrollPane;

	private JTabbedPane treeTabbedPane;

	private JPanel buttonPanel;

	JComboBox<String> filterField;

	CheckComboBox<Field> filterFieldSelection;

	private BasicComboBoxEditor comboboxEditor;

	private MainController controller;

	/**
	 * Create the application.
	 */
	public MainView(MainController controller) {
		this.controller = controller;
		initialize();
		initListeners();
		initializeGlobalKeystrokes();
		preferenceStore.addPreferenceChangeListener(new MainViewPreferenceListener());
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
		this.setTitle(Jeboorker.APP + " " + Jeboorker.VERSION);
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

		JPanel contentPane = new JPanel();
		contentPane.setOpaque(true);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{489};
		gridBagLayout.rowHeights = new int[]{350, 25, 30};
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 4.9E-324};
		contentPane.setLayout(gridBagLayout);

		mainSplitPane = new JSplitPane();
		mainSplitPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
		mainSplitPane.setOneTouchExpandable(true);
		mainSplitPane.setResizeWeight(0.9);

		GridBagConstraints gbc_mainSplitPane = new GridBagConstraints();
		gbc_mainSplitPane.insets = new Insets(0, 3, 5, 0);
		gbc_mainSplitPane.fill = GridBagConstraints.BOTH;
		gbc_mainSplitPane.gridx = 0;
		gbc_mainSplitPane.gridy = 0;
		contentPane.add(mainSplitPane, gbc_mainSplitPane);
		KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
		KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
		KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false);
		KeyStroke refresh = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false);
		KeyStroke rename = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false);

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

				createMainTable(copy, paste, delete, refresh, rename);

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
				addMetadataButton.setText("");
				addMetadataButton.setWidth(50);
				EmptyListModel<Action> emptyListModel = EmptyListModel.getSharedInstance();
				addMetadataButton.setListModel(emptyListModel);
				propertySheet.addToolbarComponent(addMetadataButton);

				removeMetadataButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REMOVE_METADATA_ENTRY_ACTION, null));
				propertySheet.addToolbarComponent(removeMetadataButton);

				saveMetadataButton = new JRButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SAVE_METADATA_ACTION, null));
				saveMetadataButton.setText("");
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


		JPanel filterPanel = createFilterPanel();
		GridBagConstraints gbc_searchPanel = new GridBagConstraints();
		gbc_searchPanel.insets = new Insets(0, 3, 5, 3);
		gbc_searchPanel.anchor = GridBagConstraints.NORTH;
		gbc_searchPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchPanel.gridx = 0;
		gbc_searchPanel.gridy = 1;
		contentPane.add(filterPanel, gbc_searchPanel);

		JPanel statusPanel = new JPanel();
		GridBagConstraints gbc_statusPanel = new GridBagConstraints();
		gbc_statusPanel.insets = new Insets(0, 3, 3, 3);
		gbc_statusPanel.fill = GridBagConstraints.BOTH;
		gbc_statusPanel.gridx = 0;
		gbc_statusPanel.gridy = 2;
		contentPane.add(statusPanel, gbc_statusPanel);
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

		this.setContentPane(contentPane);
		this.setJMenuBar(MainMenuBarController.getController().getView());
	}

	/**
	 * Attach all needed listeners to the view
	 */
	private void initListeners() {
		mainTable.getSelectionModel().addListSelectionListener(new PropertySheetListSelectionListener());
		mainTable.addMouseListener(new MainTablePopupMouseListener());

		imageViewer.addMouseListener(new CoverPopupMouseListener());
		basePathTree.addMouseListener(new BasePathTreePopupMouseListener());
		fileSystemTree.addMouseListener(new FileSystemTreePopupMouseListener());
		propertySheet.addPropertySheetChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if("value".equals(e.getPropertyName())) {
					//sheet has been edited
					EventManager.fireEvent(EventManager.EVENT_TYPES.METADATA_SHEET_CONTENT_CHANGE, new ApplicationEvent(getSelectedEbookPropertyItems(), getSelectedMetadataProperty(), e.getSource()));
				}
			}
		});

		propertySheet.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()) {
					EventManager.fireEvent(EventManager.EVENT_TYPES.METADATA_SHEET_SELECTION_CHANGE, new ApplicationEvent(getSelectedEbookPropertyItems(), getSelectedMetadataProperty(), e.getSource()));
				}
			}
		});

		mainTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()) {
					EventManager.fireEvent(EventManager.EVENT_TYPES.EBOOK_ITEM_SELECTION_CHANGE, new ApplicationEvent(getSelectedEbookPropertyItems(), getSelectedMetadataProperty(), e.getSource()));
				}
			}
		});
	}

	private JPanel createFilterPanel() {
		JPanel filterPanel = new JPanel();

		GridBagLayout gbl_searchPanel = new GridBagLayout();
		gbl_searchPanel.columnWidths = new int[] { 0, 80, 0, 0, 0 };
		gbl_searchPanel.rowHeights = new int[] { 0, 0 };
		gbl_searchPanel.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_searchPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		filterPanel.setLayout(gbl_searchPanel);

		JLabel lblSearch = new JLabel(Bundle.getString("FilterPanelView.label.search"));
		Dimension lblSearchSize = new Dimension(55, lblSearch.getPreferredSize().height);
		lblSearch.setPreferredSize(lblSearchSize);
		lblSearch.setMinimumSize(lblSearchSize);
		GridBagConstraints gbc_lblSearch = new GridBagConstraints();
		gbc_lblSearch.anchor = GridBagConstraints.EAST;
		gbc_lblSearch.gridx = 0;
		gbc_lblSearch.gridy = 0;
		filterPanel.add(lblSearch, gbc_lblSearch);

		filterFieldSelection = new CheckComboBox<Field>();
		Dimension filterFieldSelectionSize = new Dimension(80, filterFieldSelection.getPreferredSize().height);
		filterFieldSelection.setPreferredSize(filterFieldSelectionSize);
		filterFieldSelection.setMinimumSize(filterFieldSelectionSize);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.NONE;
		gbc_comboBox.insets = new Insets(0, 0, 0, 5);
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		filterPanel.add(filterFieldSelection, gbc_comboBox);

		filterField = new JComboBox<String>();
		filterField.setModel(new DefaultComboBoxModel<String>());
		filterField.setEditable(true);
		filterField.setEditor(comboboxEditor = new FilterFieldComboboxEditor());
		((JComponent)comboboxEditor.getEditorComponent()).setBorder(new EmptyBorder(0, 5, 0, 5));
//		((JComponent)comboboxEditor.getEditorComponent()).setOpaque(true);
//		((JComponent)comboboxEditor.getEditorComponent()).setForeground(SwingUtils.getForegroundColor());
//		((JComponent)comboboxEditor.getEditorComponent()).setBackground(SwingUtils.getBackgroundColor());
		GridBagConstraints gbc_searchField = new GridBagConstraints();
		gbc_searchField.insets = new Insets(0, 0, 0, 5);
		gbc_searchField.weightx = 1.0;
		gbc_searchField.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchField.gridx = 2;
		gbc_searchField.gridy = 0;
		filterPanel.add(filterField, gbc_searchField);

		JButton searchButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SEARCH_ACTION, ""));
		searchButton.setPreferredSize(new Dimension(27, 27));
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.weightx = 0;
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 3;
		gbc_textField.gridy = 0;
		filterPanel.add(searchButton, gbc_textField);

		return filterPanel;
	}

	private void createMainTable(KeyStroke copy, KeyStroke paste, KeyStroke delete, KeyStroke refresh, KeyStroke rename) {
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
		DefaultListSelectionModel defaultListSelectionModel = new DefaultListSelectionModel();
		defaultListSelectionModel.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		mainTable.setSelectionModel(defaultListSelectionModel);
		mainTable.setDragEnabled(true);
		mainTable.setStopEditOnSelectionChange(true);
		mainTable.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.COPY_TO_CLIPBOARD_ACTION, null), "Copy", copy, JComponent.WHEN_FOCUSED);
		mainTable.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.PASTE_FROM_CLIPBOARD_ACTION, null), "Paste", paste, JComponent.WHEN_FOCUSED);
		mainTable.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.DELETE_FILE_ACTION, null), "DeleteFile", delete, JComponent.WHEN_FOCUSED);
		mainTable.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REFRESH_ENTRY_ACTION, null), "RefreshEntry", refresh, JComponent.WHEN_FOCUSED);
		mainTable.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.RENAME_FILE_ACTION, null), "RenameFile", rename, JComponent.WHEN_FOCUSED);
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
		fileSystemTree.setShowsRootHandles(true);
		fileSystemTree.setName(fileSystemTreeName);
		fileSystemTree.setSelectionModel(new DefaultTreeSelectionModel());
		if(Jeboorker.isRuntime) {
			FileSystemTreeModel fileSystemTreeModel = new FileSystemTreeModel(fileSystemTree);
			fileSystemTree.setModel(fileSystemTreeModel);
			fileSystemTree.setAutoMoveHorizontalSliders(preferenceStore.isTreeAutoScrollingEnabled());
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
		}, "RenameFile", KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false), JComponent.WHEN_FOCUSED);

		JRScrollPane treeScroller = new JRScrollPane(fileSystemTree);
		treeScroller.setOpaque(false);
		treeScroller.getViewport().setOpaque(false);
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
                		String basePathFor = preferenceStore.getBasePathFor(targetPathResource);
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
		basePathTree.setShowsRootHandles(true);
		basePathTree.setName(basePathTreeName);
		if(Jeboorker.isRuntime) {
			basePathTree.setModel(new BasePathTreeModel(basePathTree));
			basePathTree.setEditable(true);
			BasePathTreeCellRenderer basePathTreeCellRenderer = new BasePathTreeCellRenderer(basePathTree);
			basePathTree.setCellRenderer(basePathTreeCellRenderer);
			basePathTree.setCellEditor(new BasePathTreeCellEditor(basePathTree));
			basePathTree.setToggleExpandOnDoubleClick(true);
			basePathTree.setAutoMoveHorizontalSliders(preferenceStore.isTreeAutoScrollingEnabled());
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
							MainController.getController().refreshTable();
						} else {
							boolean remove = MainController.getController().getTableModel().removeWhereCondition(QUERY_IDENTIFER);
							((BasePathTreeModel)basePathTree.getModel()).setFilterTreePath(null);
							if(remove) {
								MainController.getController().refreshTable();
							}
						}
					}
					previousEditorValue = cellEditorValue;
				}
			}


			private void setPathFilter(final String fullResourceFilterPath) {
				MainController.getController().getTableModel().addWhereCondition(new EbookPropertyDBTableModel.EbookPropertyDBTableModelQuery() {

					@Override
					public String getIdentifier() {
						return QUERY_IDENTIFER;
					}

					@Override
					public void appendQuery(Where<EbookPropertyItem, EbookPropertyItem> where) throws SQLException {
						String fullResourceFilterPathStatement = StringUtils.replace(fullResourceFilterPath, "\\", "\\\\");
						where.like("file", fullResourceFilterPathStatement + "%");
					}
				});
			}

		});

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
        final FileSystemNode pathNode = (FileSystemNode) selPath.getLastPathComponent();
        List<IResourceHandler> items = controller.getMainTreeController().getSelectedTreeItems();

		Action action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.PASTE_FROM_CLIPBOARD_ACTION, pathNode.getResource().toString());
		menu.add(new JMenuItem(action));

		action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.NEW_FOLDER_ACTION, pathNode.getResource().toString(), new ActionCallback() {

			@Override
			public void afterAction() {
				((BasePathTreeModel) basePathTree.getModel()).reload(pathNode);
				((FileSystemTreeModel) fileSystemTree.getModel()).reload(pathNode.getResource());
			}

		});
		menu.add(new JMenuItem(action));

		menu.add(MainViewMenuUtils.createDeleteMenuItem(items));

		//setup and show popup
		if(menu.getComponentCount() > 0) {
			menu.setLocation(location);
			menu.show(invoker, location.x, location.y);
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
		final List<IResourceHandler> items = controller.getMainTreeController().getSelectedTreeItems();
		final FileSystemNode pathNode = (FileSystemNode) selPath.getLastPathComponent();
		final JPopupMenu menu = new JPopupMenu();

		Action action;
		if(items.size() == 1) {
			//only visible to single selections
			if(items.get(0).isDirectoryResource()) {
				action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.FILE_SYSTEM_REFRESH_ACTION, items.get(0).toString());
				JMenuItem item = new JMenuItem(action);
				item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false));
				menu.add(item);
			}

			action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FILE_ACTION, items.get(0).toString());
			menu.add(action);

			action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.OPEN_FOLDER_ACTION, items.get(0).toString());
			menu.add(action);

			action = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.NEW_FOLDER_ACTION, pathNode.getResource().toString(), new ActionCallback() {

				@Override
				public void afterAction() {
					((BasePathTreeModel) basePathTree.getModel()).reload(pathNode.getResource());
					((FileSystemTreeModel) fileSystemTree.getModel()).reload(pathNode);
				}

			});
			menu.add(new JMenuItem(action));
		}
		if(items.size() >= 1) {
			final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
			final List<String> basePath = preferenceStore.getBasePath();
			final String name = Bundle.getString("MainMenuBarController.import");
			final JMenu mnImport = new JMenu(SwingUtils.removeMnemonicMarker(name));

			mnImport.setIcon(ImageResourceBundle.getResourceAsImageIcon("import_16.png"));
			mnImport.setMnemonic(SwingUtils.getMnemonicKeyCode(name));
			for (Iterator<String> iterator = basePath.iterator(); iterator.hasNext();) {
				String path = iterator.next();
				JMenuItem pathItem = new JMenuItem();
				pathItem.setAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.FILE_SYSTEM_IMPORT_ACTION, path));
				mnImport.add(pathItem);
			}
			menu.add(mnImport);
			if(!ResourceHandlerUtils.containFilesOnly(items)) {
				mnImport.setEnabled(false);
			}
		}

		MainMenuBarController.getController();
		JMenu copyToSubMenu = MainMenuBarController.createCopyToMenu();
		menu.add(copyToSubMenu);

		menu.add(MainViewMenuUtils.createDeleteMenuItem(items));

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
	public void enableFilterColor(boolean enable) {
		if (enable) {
			((JTextComponent) comboboxEditor.getEditorComponent()).setBackground(SwingUtils.getSelectionBackgroundColor());
			((JTextComponent) comboboxEditor.getEditorComponent()).setForeground(SwingUtils.getSelectionForegroundColor());
			((JTextComponent) comboboxEditor.getEditorComponent()).setSelectionColor(SwingUtils.getSelectionBackgroundColor().brighter());
		} else {
			((JTextComponent) comboboxEditor.getEditorComponent()).setForeground(SwingUtils.getForegroundColor());
			((JTextComponent) comboboxEditor.getEditorComponent()).setBackground(SwingUtils.getBackgroundColor());
			((JTextComponent) comboboxEditor.getEditorComponent()).setSelectionColor(SwingUtils.getSelectionBackgroundColor());
		}
	}

	public void refreshUI() {
		// don't know why but otherwise the renderer won't work after changing the look and feel
		if(!(mainTable.getDefaultRenderer(Object.class) instanceof EbookTableCellRenderer)) {
			mainTable.setDefaultRenderer(Object.class, new EbookTableCellRenderer());
		}
	}

	/**
	 * Gets all selected items from the main table.
	 * @return The selected items. Never returns <code>null</code>.
	 */
	public List<EbookPropertyItem> getSelectedEbookPropertyItems() {
		final int[] selectedRows = getSelectedEbookPropertyItemRows();
		final ArrayList<EbookPropertyItem> result = new ArrayList<EbookPropertyItem>(selectedRows.length);
		for (int i = 0; i < selectedRows.length; i++) {
			EbookPropertyItem valueAt = (EbookPropertyItem) controller.getTableModel().getValueAt(selectedRows[i], 0);
			result.add(valueAt);
		}

		return result;
	}

	/**
	 * Gets all selected rows from the main table.
	 * @return all selected rows or an empty array if no row is selected. Never returns <code>null</code>.
	 */
	public int[] getSelectedEbookPropertyItemRows() {
		if (mainTable != null) {
			final int[] selectedRows = mainTable.getSelectedRows();
			return selectedRows;
		} else {
			return new int[0];
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
			final EbookSheetPropertyModel model = (EbookSheetPropertyModel) propertySheet.getModel();
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
			if(mainTable.getSelectedRowCount() >= 1) {
				final int rowCount = mainTable.getRowCount();
				final int[] selectedRows = mainTable.getSelectedRows();
				final int[] modelRowsIndex = new int[selectedRows.length];
				final List<EbookPropertyItem> items = new ArrayList<EbookPropertyItem>(selectedRows.length);
				for (int i = 0; i < selectedRows.length; i++) {
					if(mainTable.getRowSorter() != null) {
						modelRowsIndex[i] = mainTable.getRowSorter().convertRowIndexToModel(selectedRows[i]);
					} else {
						modelRowsIndex[i] = selectedRows[i];
					}
					if(modelRowsIndex[i] < rowCount) {
						items.add(((EbookPropertyDBTableModel)mainTable.getModel()).getEbookPropertyItemAt(modelRowsIndex[i]));
					}
				}

				PropertySheetTableModel oldModel = propertySheet.getModel();
				oldModel.dispose();

				if(items.size() > 1) {
					//multiple selection
					final EbookSheetPropertyMultiSelectionModel model = new EbookSheetPropertyMultiSelectionModel();
					propertySheet.setModel(model);

					model.loadProperties(items);

					setImage(null, null);
					EmptyListModel<Action> emptyListModel = EmptyListModel.getSharedInstance();
					addMetadataButton.setListModel(emptyListModel);
				} else if (items.size() == 1) {
					//single selection
					final EbookSheetPropertyModel model = new EbookSheetPropertyModel();
					propertySheet.setModel(model);

					if(items.get(0) != null) {
						EbookPropertyItem ebookPropertyItem = items.get(0);
						model.loadProperties(ebookPropertyItem);
						byte[] cover = model.getCover();
						if(cover != null && cover.length > 0) {
							setImage(cover, ebookPropertyItem);
						} else {
							setImage(null, null);
						}

						IMetadataReader reader = model.getMetadataReader();
						if(reader != null) {
							List<MetadataProperty> allMetaData = model.getAllMetaData();
							MetadataAddListModel metadataAddListModel = new MetadataAddListModel(reader.getSupportedMetaData(), allMetaData, ebookPropertyItem);
							addMetadataButton.setListModel(metadataAddListModel);
						}
					}
				}
			} else {
				//no selection
				propertySheet.setModel(new EbookSheetPropertyMultiSelectionModel());
				setImage(null, null);
				EmptyListModel<Action> emptyListModel = EmptyListModel.getSharedInstance();
				addMetadataButton.setListModel(emptyListModel);
			}
		} catch (Exception e) {
			LoggerFactory.getLogger().log(Level.WARNING, "Refresh property sheet has failed.", e);
		}
	}

	/**
	 * Shows the image given with the <code>cover</code> parameter in the simple image viewer.
	 * The image viewer is set to black if the given <code>cover</code> is <code>null</code>.
	 */
	private void setImage(final byte[] cover, final EbookPropertyItem ebookPropertyItem) {
		if (cover != null && ebookPropertyItem != null) {
			//remove file extension by removing the separation dot because an image file name is expected.
			final String coverFileName = StringUtils.replace(ebookPropertyItem.getResourceHandler().getResourceString(), new String[] {".", "/", "\\"}, "_");
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
}
