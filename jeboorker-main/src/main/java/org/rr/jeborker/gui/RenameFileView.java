package org.rr.jeborker.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.commons.lang.math.NumberUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.mufs.ResourceHandlerUtils;
import org.rr.commons.swing.components.JREditableHistoryComboBox;
import org.rr.commons.swing.components.JRTable;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.NumberUtil;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.db.item.EbookPropertyItem;

public class RenameFileView extends AbstractDialogView {

	public static final int ACTION_RESULT_OK = 0;

	private static final int ABORT_BUTTON_INDEX = 0;

	private static final int OK_BUTTON_INDEX = 1;

	private int actionResult = -1;

	private RenameFileController controller;

	private List<EbookPropertyItem> toRename;

	private String[] cachedValues;

	private JRTable previewTable;

	JCheckBox overwriteCheckbox;

	private final ActionListener abortAction = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			controller.close();
		}
	};

	private final ActionListener okAction = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			actionResult = ACTION_RESULT_OK;
			addCurrentPatternToHistory();
			controller.close();
		}
	};

	private final TableModel previewTableModel = new TableModel() {

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(cachedValues[rowIndex] != null) {
				return cachedValues[rowIndex];
			}
			EbookPropertyItem ebookPropertyItem = toRename.get(rowIndex);
			cachedValues[rowIndex] = ResourceHandlerUtils.removeInvalidCharacters(formatFileName(getFileNamePattern(), ebookPropertyItem, rowIndex));
			cachedValues[rowIndex] = StringUtil.removeMultipleWhiteSpaces(cachedValues[rowIndex]);
			return cachedValues[rowIndex];
		}

		@Override
		public int getRowCount() {
			return toRename.size();
		}

		@Override
		public String getColumnName(int columnIndex) {
			return null;
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
		}
	};

	DocumentListener textFieldFilePatternChangeListener = new DocumentListener() {

		@Override
		public void removeUpdate(DocumentEvent e) {
			fireValuesChanged();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			fireValuesChanged();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
		}
	};

	private JREditableHistoryComboBox textFieldFilePattern;

	public RenameFileView(RenameFileController controller, List<EbookPropertyItem> list, JFrame mainWindow) {
		super(mainWindow);
		setModal(true);
		this.controller = controller;
		this.toRename = list != null ? list : Collections.<EbookPropertyItem>emptyList();
		this.cachedValues = new String[this.toRename.size()];
		this.initialize();
	}

	protected void initialize() {
		super.initialize();

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);

		// File pattern text field components
		JLabel lblFilePattern = new JLabel(Bundle.getString("RenameFileView.pattern"));
		GridBagConstraints gbc_lblFilePattern = new GridBagConstraints();
		gbc_lblFilePattern.fill = GridBagConstraints.VERTICAL;
		gbc_lblFilePattern.insets = new Insets(5, 5, 3, 5);
		gbc_lblFilePattern.anchor = GridBagConstraints.EAST;
		gbc_lblFilePattern.gridx = 0;
		gbc_lblFilePattern.gridy = 0;
		getContentPane().add(lblFilePattern, gbc_lblFilePattern);

		textFieldFilePattern = new JREditableHistoryComboBox();
		textFieldFilePattern.addDocumentListener(textFieldFilePatternChangeListener);
		textFieldFilePattern.setItem("%a - %t");
		GridBagConstraints gbc_textFieldFilePattern = new GridBagConstraints();
		gbc_textFieldFilePattern.insets = new Insets(5, 0, 3, 5);
		gbc_textFieldFilePattern.fill = GridBagConstraints.BOTH;
		gbc_textFieldFilePattern.gridx = 1;
		gbc_textFieldFilePattern.gridy = 0;
		getContentPane().add(textFieldFilePattern, gbc_textFieldFilePattern);

		//info label
		JLabel infoLabel = new JLabel(Bundle.getString("RenameFileView.patternInfo"));
		GridBagConstraints gbc_infoLabel = new GridBagConstraints();
		gbc_infoLabel.insets = new Insets(5, 5, 10, 5);
		gbc_infoLabel.fill = GridBagConstraints.BOTH;
		gbc_infoLabel.gridx = 0;
		gbc_infoLabel.gridy = 1;
		gbc_infoLabel.gridwidth = 2;
		getContentPane().add(infoLabel, gbc_infoLabel);

		// Middle file name preview
		JPanel borderPanel = new JPanel();
		borderPanel.setBorder(new TitledBorder(null, Bundle.getString("RenameFileView.preview"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		borderPanel.setLayout(new BorderLayout());
		GridBagConstraints gbc_middle = new GridBagConstraints();
		gbc_middle.insets = new Insets(5, 3, 5, 3);
		gbc_middle.fill = GridBagConstraints.BOTH;
		gbc_middle.gridx = 0;
		gbc_middle.gridy = 2;
		gbc_middle.gridwidth = 2;
		gbc_middle.weighty = 1.0;
		getContentPane().add(borderPanel, gbc_middle);

		previewTable = new JRTable();
		previewTable.setModel(previewTableModel);
		previewTable.setEnabled(false);
		previewTable.setTableHeader(null);
		borderPanel.add(new JScrollPane(previewTable), BorderLayout.CENTER);

		//Overwrite checkbox
		overwriteCheckbox = new JCheckBox(Bundle.getString("RenameFileView.overwrite"));
		GridBagConstraints gbc_overwriteCheckbox = new GridBagConstraints();
		gbc_overwriteCheckbox.insets = new Insets(5, 3, 5, 3);
		gbc_overwriteCheckbox.fill = GridBagConstraints.BOTH;
		gbc_overwriteCheckbox.gridx = 0;
		gbc_overwriteCheckbox.gridy = 3;
		gbc_overwriteCheckbox.gridwidth = 2;
		getContentPane().add(overwriteCheckbox, gbc_overwriteCheckbox);

		// Bottom OK and Abort
		JPanel bottomPanel = createBottomButtonPanel();
		GridBagConstraints gbc_bottom = new GridBagConstraints();
		gbc_bottom.insets = new Insets(5, 3, 5, 3);
		gbc_bottom.fill = GridBagConstraints.BOTH;
		gbc_bottom.gridx = 0;
		gbc_bottom.gridy = 4;
		gbc_bottom.gridwidth = 2;
		getContentPane().add(bottomPanel, gbc_bottom);
	}

	public int getActionResult() {
		return actionResult;
	}

	/**
	 * Tells if existing target files should be overwritten.
	 * @return <code>true</code> if existing files should be overwritten and <code>false</code> otherwise.
	 */
	public boolean isOverwriteExistingFiles() {
		return overwriteCheckbox.isSelected();
	}

	/**
	 * Get the rename result with the origin {@link EbookPropertyItem} at the left and the target {@link IResourceHandler}
	 * at the right in the {@link Entry}.
	 * @return The renamed files with it's original {@link EbookPropertyItem}s. Never returns <code>null</code>.
	 */
	public List<Entry<EbookPropertyItem, IResourceHandler>> getValues() {
		int rowCount = previewTableModel.getRowCount();
		List<Entry<EbookPropertyItem, IResourceHandler>> result = new ArrayList<Entry<EbookPropertyItem, IResourceHandler>>(rowCount);
		for (int i = 0; i < rowCount; i++) {
			final EbookPropertyItem ebookPropertyItem = toRename.get(i);
			String renamedFileName = StringUtil.toString(previewTableModel.getValueAt(i, 0));
			final IResourceHandler targetResourceHandler = ResourceHandlerFactory.getResourceHandler(ebookPropertyItem.getResourceHandler().getParentResource(), renamedFileName);
			result.add(new Map.Entry<EbookPropertyItem, IResourceHandler>() {

				@Override
				public EbookPropertyItem getKey() {
					return ebookPropertyItem;
				}

				@Override
				public IResourceHandler getValue() {
					return targetResourceHandler;
				}

				@Override
				public IResourceHandler setValue(IResourceHandler value) {
					return null;
				}});
		}
		return result;
	}

	/**
	 * Replaces the placeholder with the value from the given {@link EbookPropertyItem}.
	 * @param pattern The file name pattern including placeholder to replace.
	 * @param item The replacement data.
	 * @param num The number for the %n parameter
	 * @return The formatted file name.
	 */
	private String formatFileName(String pattern, EbookPropertyItem item, int num) {
		pattern = pattern.replaceAll("%a", StringUtil.toString(item.getAuthor()));
		pattern = pattern.replaceAll("%t", StringUtil.toString(item.getTitle()));
		pattern = pattern.replaceAll("%g", StringUtil.toString(item.getGenre()));
		pattern = formatNumberPattern(pattern, "%i", CommonUtils.toNumber(item.getSeriesIndex()).intValue());
		pattern = pattern.replaceAll("%s", StringUtil.toString(item.getSeriesName()));
		pattern = formatNumberPattern(pattern, "%n", num);
		
		pattern = StringUtil.removeMultipleWhiteSpaces(pattern);
		String fileExtension = item.getResourceHandler().getFileExtension();
		if(StringUtil.isNotEmpty(fileExtension)) {
			return pattern + "." + fileExtension;
		}
		return pattern;
	}

	private String formatNumberPattern(String pattern, String identifier, int num) {
		if(pattern.contains(identifier)) {
			int numIndex = pattern.indexOf(identifier) + 2;
			int digits = 0;
			while(pattern.length() > numIndex + digits && pattern.charAt(numIndex + digits) == '#') {
				digits++;
			}
			String formattedNum = new DecimalFormat(StringUtil.string(digits, '0')).format(num);
			pattern = pattern.replaceAll(identifier + "#*", formattedNum);
		}
		return pattern;
	}

	/**
	 * Get the file name pattern entered into the text field.
	 * @return The desired file name. Never returns <code>null</code>.
	 */
	String getFileNamePattern() {
		return textFieldFilePattern.getEditorValue();
	}

	/**
	 * Sets the file name displayed in the text field.
	 * @param pattern The string to be displayed in the file name field.
	 */
	void setFileNamePattern(String pattern) {
		if(StringUtil.isNotEmpty(pattern)) {
			textFieldFilePattern.setItem(pattern);
		}
	}
	
	/**
	 * Sets the file name history values displayed in the combobox popup.
	 * @param history The history values.
	 */
	void setFileNameHistory(String history) {
		if(StringUtil.isNotEmpty(history)) {
			textFieldFilePattern.setHistoryValues(history);
		}
	}
	
	/**
	 * Gets the file name history values displayed in the combobox popup.
	 * @return The file history list.
	 */
	String getFileNameHistory() {
		return textFieldFilePattern.getHistoryValues();
	}

	/**
	 * Can be invoked if the preview table needs to be renewed.
	 */
	private void fireValuesChanged() {
		for (int i = 0; i < cachedValues.length; i++) {
			cachedValues[i] = null;
		}
		if(previewTable != null) {
			previewTable.repaint();
			handleInvalidRenameTargets();
		}
	}

	/**
	 * Disables the rename button if there are duplicate entries in the {@link #previewTableModel} or enables
	 * them if they're all distinct.
	 */
	private void handleInvalidRenameTargets() {
		int rowCount = previewTableModel.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			String value = StringUtil.toString(previewTableModel.getValueAt(i, 0));
			for (int j = i + 1; j < rowCount; j++) {
				if(value.equals(previewTableModel.getValueAt(j, 0))) {
					getButtonAt(OK_BUTTON_INDEX).setEnabled(false);
					return;
				} else if(StringUtil.isEmpty(value)) {
					getButtonAt(OK_BUTTON_INDEX).setEnabled(false);
					return;
				} else if(value.equals(".")) {
					getButtonAt(OK_BUTTON_INDEX).setEnabled(false);
					return;
				}
			}
		}
		getButtonAt(OK_BUTTON_INDEX).setEnabled(true);
	}

	/**
	 * Adds the current editor value to the history of the pattern field.
	 */
	private void addCurrentPatternToHistory() {
		String selectedItem = (String) textFieldFilePattern.getSelectedItem();
		if(StringUtil.isNotBlank(selectedItem)) {
			textFieldFilePattern.addHistoryValue(selectedItem);
		}
	}

	@Override
	protected int getBottomButtonCount() {
		return 2;
	}

	@Override
	protected ActionListener getBottomButtonAction(int index) {
		switch(index) {
			case ABORT_BUTTON_INDEX:
				return abortAction;
			case OK_BUTTON_INDEX:
				return okAction;

		}
		return null;
	}

	@Override
	protected String getBottomButtonLabel(int index) {
		switch(index) {
			case 0:
				return Bundle.getString("RenameFileView.Abort");
			case 1:
				return Bundle.getString("RenameFileView.OK");
		}
		return null;
	}

	@Override
	protected Dimension getDefaultDialogSize() {
		return new Dimension(700, 500);
	}

	@Override
	protected String getDialogTitle() {
		return Bundle.getString("RenameFileView.title");
	}

	@Override
	protected Dimension getDialogMinimumSize() {
		return new Dimension(300, 200);
	}

	@Override
	protected boolean isDefaultButtonAction(int idx) {
		return idx == OK_BUTTON_INDEX;
	}

	@Override
	protected boolean isAbortAction(int idx) {
		return idx == ABORT_BUTTON_INDEX;
	}

}
