package org.rr.jeborker.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import org.japura.gui.CheckComboBox;
import org.rr.common.swing.ShadowPanel;
import org.rr.common.swing.button.JMenuButton;
import org.rr.common.swing.image.SimpleImageViewer;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.gui.model.EbookPropertyDBTableModel;
import org.rr.jeborker.gui.model.EbookPropertyDBTableSelectionModel;
import org.rr.jeborker.gui.model.EbookSheetPropertyModel;
import org.rr.jeborker.gui.renderer.DatePropertyCellEditor;
import org.rr.jeborker.gui.renderer.DatePropertyCellRenderer;
import org.rr.jeborker.gui.renderer.DefaultPropertyRenderer;
import org.rr.jeborker.gui.renderer.EbookTableCellEditor;
import org.rr.jeborker.gui.renderer.EbookTableCellRenderer;
import org.rr.jeborker.gui.renderer.StarRatingPropertyEditor;
import org.rr.jeborker.gui.renderer.StarRatingPropertyRenderer;

import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheet;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class MainView extends JFrame{
	
	private static final long serialVersionUID = 6837919427429399376L;
	
	JTable table;
	
	JProgressBar progressBar;
	
	JDialog blockingDialog;
	
	JSplitPane mainSplitPane;
	
	JSplitPane propertySheetImageSplitPane;
	
	SimpleImageViewer imageViewer;
	
	PropertySheetPanel propertySheet;
	
	CheckComboBox<Field>  sortColumnComboBox;
	
	JToggleButton sortOrderAscButton;
	
	JToggleButton sortOrderDescButton;
	
	JMenuButton addMetadataButton;
	
	JButton removeMetadataButton;
	
	JButton saveMetadataButton;
	
	JLabel lblSortBy;
	
	JPanel rootPanel;
	
	/**
	 * Create the application.
	 */
	public MainView() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		this.setTitle("Jeboorker v" + Jeboorker.version);
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
		gridBagLayout.rowHeights = new int[]{4, 0, 350, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		this.getContentPane().setLayout(gridBagLayout);
		
		JPanel filterPanel = FilterPanelController.getView();
		GridBagConstraints gbc_searchPanel = new GridBagConstraints();
		gbc_searchPanel.insets = new Insets(0, 3, 5, 3);
		gbc_searchPanel.anchor = GridBagConstraints.NORTH;
		gbc_searchPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchPanel.gridx = 0;
		gbc_searchPanel.gridy = 3;
		getContentPane().add(filterPanel, gbc_searchPanel);
		

		
		
		mainSplitPane = new JSplitPane();
		mainSplitPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
		mainSplitPane.setOneTouchExpandable(true);
		mainSplitPane.setResizeWeight(0.9);

		GridBagConstraints gbc_mainSplitPane = new GridBagConstraints();
		gbc_mainSplitPane.insets = new Insets(0, 3, 5, 0);
		gbc_mainSplitPane.fill = GridBagConstraints.BOTH;
		gbc_mainSplitPane.gridx = 0;
		gbc_mainSplitPane.gridy = 2;
		getContentPane().add(mainSplitPane, gbc_mainSplitPane);
		
		table = new JTable();
		table.setRowHeight(74);
		table.setModel(new EbookPropertyDBTableModel());
		table.setDefaultRenderer(Object.class, new EbookTableCellRenderer());
		table.setDefaultEditor(Object.class, new EbookTableCellEditor());
		table.setTableHeader(null);
		table.setSelectionModel(new EbookPropertyDBTableSelectionModel());
				
		JPanel propertyContentPanel = new JPanel();
		GridBagLayout gbl_propertyContentPanel = new GridBagLayout();
		gbl_propertyContentPanel.columnWidths = new int[]{0, 25, 25, 0};
		gbl_propertyContentPanel.rowHeights = new int[]{25, 0, 0, 0};
		gbl_propertyContentPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0};
		gbl_propertyContentPanel.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		propertyContentPanel.setLayout(gbl_propertyContentPanel);
		mainSplitPane.setLeftComponent(propertyContentPanel);
				
				lblSortBy = new JLabel(Bundle.getString("EborkerMainView.sortby"));
				GridBagConstraints gbc_lblSortBy = new GridBagConstraints();
				gbc_lblSortBy.insets = new Insets(0, 0, 5, 5);
				gbc_lblSortBy.gridx = 0;
				gbc_lblSortBy.gridy = 0;
				propertyContentPanel.add(lblSortBy, gbc_lblSortBy);
				
				JScrollPane scrollPane = new JScrollPane();
				GridBagConstraints gbc_scrollPane = new GridBagConstraints();
				gbc_scrollPane.gridwidth = 5;
				gbc_scrollPane.weightx = 2.0;
				gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
				gbc_scrollPane.fill = GridBagConstraints.BOTH;
				gbc_scrollPane.anchor = GridBagConstraints.NORTHWEST;
				gbc_scrollPane.gridx = 0;
				gbc_scrollPane.gridy = 1;
				propertyContentPanel.add(scrollPane, gbc_scrollPane);
				scrollPane.setViewportView(table);
				
				sortColumnComboBox = new CheckComboBox<Field>();
				sortColumnComboBox.setPreferredSize(new Dimension(0,25));
				GridBagConstraints gbc_sortColumnComboBox = new GridBagConstraints();
				gbc_sortColumnComboBox.insets = new Insets(0, 0, 5, 5);
				gbc_sortColumnComboBox.anchor = GridBagConstraints.NORTH;
				gbc_sortColumnComboBox.fill = GridBagConstraints.HORIZONTAL;
				gbc_sortColumnComboBox.gridx = 3;
				gbc_sortColumnComboBox.gridy = 0;
				propertyContentPanel.add(sortColumnComboBox, gbc_sortColumnComboBox);
				
				sortOrderAscButton = new JToggleButton();
				final Icon ascOrderIcon =  new ImageIcon(MainView.class.getResource("resources/sort_asc.gif"));
				sortOrderAscButton.setIcon(ascOrderIcon);
				sortOrderAscButton.setPreferredSize(new Dimension(0,25));
				sortOrderAscButton.setMinimumSize(new Dimension(0,25));
				GridBagConstraints gbc_sortOrderComboBox = new GridBagConstraints();
				gbc_sortOrderComboBox.insets = new Insets(0, 0, 5, 5);
				gbc_sortOrderComboBox.anchor = GridBagConstraints.NORTH;
				gbc_sortOrderComboBox.fill = GridBagConstraints.HORIZONTAL;
				gbc_sortOrderComboBox.gridx = 1;
				gbc_sortOrderComboBox.gridy = 0;
				propertyContentPanel.add(sortOrderAscButton, gbc_sortOrderComboBox);
				
				sortOrderDescButton = new JToggleButton();
				final Icon descOrderIcon = new ImageIcon(MainView.class.getResource("resources/sort_desc.gif"));
				sortOrderDescButton.setIcon(descOrderIcon);
				sortOrderDescButton.setPreferredSize(new Dimension(0,25));
				sortOrderDescButton.setMinimumSize(new Dimension(0,25));
				GridBagConstraints gbc_toggleButton = new GridBagConstraints();
				gbc_toggleButton.anchor = GridBagConstraints.NORTH;
				gbc_toggleButton.fill = GridBagConstraints.HORIZONTAL;				
				gbc_toggleButton.insets = new Insets(0, 0, 5, 5);
				gbc_toggleButton.gridx = 2;
				gbc_toggleButton.gridy = 0;
				propertyContentPanel.add(sortOrderDescButton, gbc_toggleButton);				
				
				JPanel sheetPanel = new JPanel();
				GridBagLayout gbl_panel = new GridBagLayout();
				gbl_panel.columnWidths = new int[]{0, 0};
				gbl_panel.rowHeights = new int[]{0, 0};
				gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
				gbl_panel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
				sheetPanel.setLayout(gbl_panel);
				
				propertySheet = new PropertySheetPanel(new EbookSheetPropertyModel());
				propertySheet.setMode(PropertySheet.VIEW_AS_FLAT_LIST);
				propertySheet.setDescriptionVisible(true);
				propertySheet.setShowCategoryButton(false);
				
				addMetadataButton = new JMenuButton();
				addMetadataButton.setIcon(new ImageIcon(Bundle.getResource("add_metadata_16.gif")));
				propertySheet.addToolbarComponent(addMetadataButton);
				
				removeMetadataButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REMOVE_METADATA_ENTRY_ACTION, null));
				propertySheet.addToolbarComponent(removeMetadataButton);
				
				saveMetadataButton = new JButton(ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SAVE_METADATA_ACTION, null));
				saveMetadataButton.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK), "PRESSED");
				saveMetadataButton.getActionMap().put("PRESSED", ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SAVE_METADATA_ACTION, null));
				
				propertySheet.addToolbarComponent(saveMetadataButton);				
				
				((PropertyRendererRegistry)propertySheet.getRendererFactory()).registerRenderer((Class<?>)null, DefaultPropertyRenderer.class);
				
				DatePropertyCellRenderer calendarDatePropertyRenderer = new DatePropertyCellRenderer(((SimpleDateFormat) SimpleDateFormat.getDateInstance()).toPattern());
		        ((PropertyEditorRegistry)propertySheet.getEditorFactory()).registerEditor(Date.class, new DatePropertyCellEditor());
		        ((PropertyRendererRegistry)propertySheet.getRendererFactory()).registerRenderer(Date.class, calendarDatePropertyRenderer);
		        
		        ((PropertyEditorRegistry)propertySheet.getEditorFactory()).registerEditor("rating", StarRatingPropertyEditor.class);
		        ((PropertyRendererRegistry)propertySheet.getRendererFactory()).registerRenderer("rating", StarRatingPropertyRenderer.class);
		        ((PropertyEditorRegistry)propertySheet.getEditorFactory()).registerEditor("calibre:rating", StarRatingPropertyEditor.class);
		        ((PropertyRendererRegistry)propertySheet.getRendererFactory()).registerRenderer("calibre:rating", StarRatingPropertyRenderer.class);
		        
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
				
				mainSplitPane.setDividerLocation(getSize().width - 220);
				
		JPanel statusPanel = new JPanel();
		GridBagConstraints gbc_statusPanel = new GridBagConstraints();
		gbc_statusPanel.anchor = GridBagConstraints.EAST;
		gbc_statusPanel.insets = new Insets(3, 3, 3, 3);
		gbc_statusPanel.fill = GridBagConstraints.BOTH;
		gbc_statusPanel.gridx = 0;
		gbc_statusPanel.gridy = 4;
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

}
