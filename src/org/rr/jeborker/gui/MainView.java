package org.rr.jeborker.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.TreePath;

import org.japura.gui.CheckComboBox;
import org.rr.common.swing.ShadowPanel;
import org.rr.common.swing.button.JMenuButton;
import org.rr.common.swing.dnd.FileTransferable;
import org.rr.common.swing.dnd.URIListTransferable;
import org.rr.common.swing.image.SimpleImageViewer;
import org.rr.common.swing.table.JRTable;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.gui.action.PasteFromClipboardAction;
import org.rr.jeborker.gui.model.BasePathTreeModel;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;
import org.rr.jeborker.gui.model.EbookPropertyDBTableSelectionModel;
import org.rr.jeborker.gui.model.EbookSheetPropertyModel;
import org.rr.jeborker.gui.renderer.DatePropertyCellEditor;
import org.rr.jeborker.gui.renderer.DatePropertyCellRenderer;
import org.rr.jeborker.gui.renderer.DefaultPropertyCellEditor;
import org.rr.jeborker.gui.renderer.DefaultPropertyRenderer;
import org.rr.jeborker.gui.renderer.EbookTableCellEditor;
import org.rr.jeborker.gui.renderer.EbookTableCellRenderer;
import org.rr.jeborker.gui.renderer.MultiListPropertyEditor;
import org.rr.jeborker.gui.renderer.MultiListPropertyRenderer;
import org.rr.jeborker.gui.renderer.ResourceHandlerTreeCellEditor;
import org.rr.jeborker.gui.renderer.ResourceHandlerTreeCellRenderer;
import org.rr.jeborker.gui.renderer.StarRatingPropertyEditor;
import org.rr.jeborker.gui.renderer.StarRatingPropertyRenderer;

import skt.swing.StringConvertor;

import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheet;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class MainView extends JFrame{
	
	private static final long serialVersionUID = 6837919427429399376L;
	
	JRTable table;
	
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
	JTree tree;
	JScrollPane mainTableScrollPane;
	
	/**
	 * Create the application.
	 */
	public MainView() {
		initialize();
		initializeGlobalKeystrokes();
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
		actionMap.put("FIND", ActionFactory.getTableFindAction(table));
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		this.setTitle(Jeboorker.app + " " + Jeboorker.version);
		this.setBounds(100, 100, 792, 622);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.QUIT_ACTION, null).invokeAction();
			}
		});
		
		this.setGlassPane(new ShadowPanel());	
		getGlassPane().setVisible(false);
		
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
				
				sortLabel = new JLabel("Sortieren nach:");
				GridBagConstraints gbc_sortLabel = new GridBagConstraints();
				gbc_sortLabel.anchor = GridBagConstraints.WEST;
				gbc_sortLabel.insets = new Insets(0, 0, 0, 5);
				gbc_sortLabel.gridx = 0;
				gbc_sortLabel.gridy = 0;
				sortPanel.add(sortLabel, gbc_sortLabel);
				
				sortColumnComboBox = new CheckComboBox<Field>();
				sortColumnComboBox.setPreferredSize(new Dimension(0, 25));
				GridBagConstraints gbc_sortColumnComboBox = new GridBagConstraints();
				gbc_sortColumnComboBox.fill = GridBagConstraints.HORIZONTAL;
				gbc_sortColumnComboBox.anchor = GridBagConstraints.NORTH;
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
				gbc_sortOrderAscButton.fill = GridBagConstraints.HORIZONTAL;
				gbc_sortOrderAscButton.anchor = GridBagConstraints.NORTH;
				gbc_sortOrderAscButton.insets = new Insets(0, 0, 0, 5);
				gbc_sortOrderAscButton.gridx = 1;
				gbc_sortOrderAscButton.gridy = 0;
				sortPanel.add(sortOrderAscButton, gbc_sortOrderAscButton);
				
				sortOrderDescButton = new JToggleButton();
				sortOrderDescButton.setIcon(descOrderIcon);
				sortOrderDescButton.setPreferredSize(new Dimension(0, 25));
				sortOrderDescButton.setMinimumSize(new Dimension(0, 25));
				GridBagConstraints gbc_sortOrderDescButton = new GridBagConstraints();
				gbc_sortOrderDescButton.fill = GridBagConstraints.HORIZONTAL;
				gbc_sortOrderDescButton.insets = new Insets(0, 0, 0, 5);
				gbc_sortOrderDescButton.anchor = GridBagConstraints.NORTH;
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
				
				table = new JRTable();
				table.setName("MainTable");
				table.setRowHeight(74);
				table.setModel(new EbookPropertyDBTableModel());
				table.setDefaultRenderer(Object.class, new EbookTableCellRenderer());
				table.setDefaultEditor(Object.class, new EbookTableCellEditor());
				table.setTableHeader(null);
				table.setSelectionModel(new EbookPropertyDBTableSelectionModel());
				table.setDragEnabled(true);
				table.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.COPY_TO_CLIPBOARD_ACTION, null), "Copy", copy, JComponent.WHEN_FOCUSED);
				table.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.PASTE_FROM_CLIPBOARD_ACTION, null), "Paste", paste, JComponent.WHEN_FOCUSED);		
				table.registerKeyboardAction(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.DELETE_FILE_ACTION, null), "DeleteFile", delete, JComponent.WHEN_FOCUSED);
				table.putClientProperty(StringConvertor.class, new StringConvertor() {
					
					@Override
					public String toString(Object obj) {
						if(obj instanceof EbookPropertyItem) {
							return ((EbookPropertyItem)obj).getResourceHandler().getName();
						}
						return StringUtils.toString(obj);
					}
				});
				
				table.setTransferHandler(new TransferHandler() {

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
		                	EbookPropertyItem val = (EbookPropertyItem) table.getModel().getValueAt(selectedRows[i], 0);
		                	try {
		                		uriList.add(new File(val.getFile()).toURI());
		                		files.add(new File(val.getFile()).getPath());
									} catch (Exception e) {
										LoggerFactory.getLogger().log(Level.WARNING, "Failed to encode " + val.getResourceHandler().toString(), e);
									}
		                }    
		                
		                if(CommonUtils.isLinux()) {
		                	return new URIListTransferable(uriList, null);
		                } else {
		                	return new FileTransferable(files);
		                }
		            }
		        });
				
				mainTableScrollPane = new JScrollPane();
				treeMainTableSplitPane.setRightComponent(mainTableScrollPane);
				mainTableScrollPane.setViewportView(table);
				
				JTabbedPane leftTabbedPane = new JTabbedPane();
				
				JScrollPane treeScroller = createBasePathTree();
				leftTabbedPane.addTab(Bundle.getString("EborkerMainView.tabbedPane.basePath"), treeScroller);
				
				treeMainTableSplitPane.setLeftComponent(leftTabbedPane);
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
		gbc_statusPanel.insets = new Insets(3, 3, 3, 3);
		gbc_statusPanel.fill = GridBagConstraints.BOTH;
		gbc_statusPanel.gridx = 0;
		gbc_statusPanel.gridy = 2;
		getContentPane().add(statusPanel, gbc_statusPanel);
		GridBagLayout gbl_statusPanel = new GridBagLayout();
		gbl_statusPanel.columnWidths = new int[]{62, 0, 0};
		gbl_statusPanel.rowHeights = new int[]{14, 0};
		gbl_statusPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_statusPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		statusPanel.setLayout(gbl_statusPanel);
		
		progressBar = new JProgressBar();
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.weighty = 1.0;
		gbc_progressBar.weightx = 1.0;
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridx = 1;
		gbc_progressBar.gridy = 0;
		statusPanel.add(progressBar, gbc_progressBar);
		
		JLabel label = new JLabel(Bundle.getString("EborkerMainView.status"));
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 0, 5);
		gbc_label.anchor = GridBagConstraints.EAST;
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		statusPanel.add(label, gbc_label);
		this.setJMenuBar(MainMenuBarController.getController().getView());
	}

	private JScrollPane createBasePathTree() {
		tree = new JTree();
		JScrollPane treeScroller = new JScrollPane(tree);
		treeScroller.setOpaque(false);
		treeScroller.getViewport().setOpaque(false);
		
		tree.setRootVisible(false);
		tree.setModel(new BasePathTreeModel());
		tree.setEditable(true);
		tree.setCellRenderer(new ResourceHandlerTreeCellRenderer(tree));
		tree.setCellEditor(new ResourceHandlerTreeCellEditor(tree));
		tree.setRowHeight(25);
		
		tree.setTransferHandler(new TransferHandler() {

			private static final long serialVersionUID = -371360766111031218L;

			public boolean canImport(TransferHandler.TransferSupport info) {
                //only import Strings
                if (!(info.isDataFlavorSupported(DataFlavor.stringFlavor) || info.isDataFlavorSupported(DataFlavor.javaFileListFlavor))) {
                    return false;
                }

                JTree.DropLocation dl = (JTree.DropLocation) info.getDropLocation();
                if (dl.getPath() != null && dl.getPath().getPathCount() <= 1) {
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

                JTree.DropLocation dl = (JTree.DropLocation) info.getDropLocation();
                TreePath dropRow = dl.getPath();
                Object lastPath = dropRow.getLastPathComponent();
                Object firstPath = dropRow.getPath()[1]; //is the base path
                IResourceHandler firstPathResource = ((BasePathTreeModel.PathNode) firstPath).getPathResource();
                IResourceHandler lastPathPathResource = ((BasePathTreeModel.PathNode) lastPath).getPathResource();
                try {
					PasteFromClipboardAction.importEbookFromClipboard(info.getTransferable(), Integer.MIN_VALUE, firstPathResource.toString(), lastPathPathResource);
					tree.startEditingAtPath(dropRow);
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
		
		GridBagConstraints gbc_tree = new GridBagConstraints();
		gbc_tree.fill = GridBagConstraints.BOTH;
		gbc_tree.gridx = 0;
		gbc_tree.gridy = 0;
		return treeScroller;
	}

	/**
	 * Shows a dialog to the user.
	 * @param message The message of the dialog
	 * @param title The dialog title.
	 * @param option The dialog option: JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.YES_NO_OPTION, JOptionPane.OK_CANCEL_OPTION
	 * @return 0: yes/ok, 1: no, 2:cancel, -1 none
	 */
	int showMessageBox(String message, String title, int option, String showAgainKey, int defaultValue) {
		Number showAgain = JeboorkerPreferences.getEntryAsNumber(showAgainKey);
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
		    		JeboorkerPreferences.addEntryNumber(showAgainKey, defaultValue);
		    	} else {
		    		JeboorkerPreferences.addEntryNumber(showAgainKey, n);
		    	}
		    }
			return n;
		} else {
			return showAgain.intValue();
		}
	}
}
