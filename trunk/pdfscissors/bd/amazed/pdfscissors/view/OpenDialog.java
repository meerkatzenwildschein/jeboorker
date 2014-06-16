package bd.amazed.pdfscissors.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;

import bd.amazed.pdfscissors.model.Model;
import bd.amazed.pdfscissors.model.PageGroup;

public class OpenDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField filePath;
	private PdfScissorsMainFrame mainFrame;
	protected IResourceHandler file;
	private Vector<Component> advancedOptions;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			OpenDialog dialog = new OpenDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public OpenDialog() {
		advancedOptions = new Vector<Component>();
		setTitle("Open pdf");
		setBounds(100, 100, 509, 299);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{427, 56, 0};
		gbl_contentPanel.rowHeights = new int[]{14, 23, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblFileLocation = new JLabel("File location");
			GridBagConstraints gbc_lblFileLocation = new GridBagConstraints();
			gbc_lblFileLocation.anchor = GridBagConstraints.NORTHWEST;
			gbc_lblFileLocation.insets = new Insets(0, 0, 5, 5);
			gbc_lblFileLocation.gridx = 0;
			gbc_lblFileLocation.gridy = 0;
			contentPanel.add(lblFileLocation, gbc_lblFileLocation);
		}
		{
			filePath = new JTextField();
			filePath.setEditable(false);
			GridBagConstraints gbc_filePath = new GridBagConstraints();
			gbc_filePath.fill = GridBagConstraints.HORIZONTAL;
			gbc_filePath.insets = new Insets(0, 0, 5, 5);
			gbc_filePath.gridx = 0;
			gbc_filePath.gridy = 1;
			contentPanel.add(filePath, gbc_filePath);
			filePath.setColumns(10);
		}

		final ButtonGroup stackGroupTypeChoices = new ButtonGroup();
		String lastSelectionOption = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).getGenericEntryAsString(Model.PROPERTY_LAST_STACK_TYPE);
		{
			JButton btnBrowse = new JButton("Open");
			GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
			gbc_btnBrowse.insets = new Insets(0, 0, 5, 0);
			gbc_btnBrowse.anchor = GridBagConstraints.NORTHEAST;
			gbc_btnBrowse.gridx = 1;
			gbc_btnBrowse.gridy = 1;
			contentPanel.add(btnBrowse, gbc_btnBrowse);
			btnBrowse.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					showFileChooserDialog();
				}
			});
		}
		{
			JLabel lblHowDoYou = new JLabel("How do you want to crop?");
			GridBagConstraints gbc_lblHowDoYou = new GridBagConstraints();
			gbc_lblHowDoYou.anchor = GridBagConstraints.WEST;
			gbc_lblHowDoYou.insets = new Insets(0, 0, 5, 5);
			gbc_lblHowDoYou.gridx = 0;
			gbc_lblHowDoYou.gridy = 2;
			contentPanel.add(lblHowDoYou, gbc_lblHowDoYou);
		}
		{
			JRadioButton rdbtnAllPagesTogether = new JRadioButton("All pages together (Easiest way)");
			stackGroupTypeChoices.add(rdbtnAllPagesTogether);
			stackGroupTypeChoices.setSelected(rdbtnAllPagesTogether.getModel(), true);
			rdbtnAllPagesTogether.setActionCommand(String.valueOf(PageGroup.GROUP_TYPE_ALL));
			GridBagConstraints gbc_rdbtnAllPagesTogether = new GridBagConstraints();
			gbc_rdbtnAllPagesTogether.anchor = GridBagConstraints.WEST;
			gbc_rdbtnAllPagesTogether.insets = new Insets(0, 0, 5, 5);
			gbc_rdbtnAllPagesTogether.gridx = 0;
			gbc_rdbtnAllPagesTogether.gridy = 3;
			contentPanel.add(rdbtnAllPagesTogether, gbc_rdbtnAllPagesTogether);
		}
		{
			JRadioButton rdbtnOddAndEven = new JRadioButton("Odd and even pages separately");
			stackGroupTypeChoices.add(rdbtnOddAndEven);
			rdbtnOddAndEven.setActionCommand(String.valueOf(PageGroup.GROUP_TYPE_ODD_EVEN));
			if (rdbtnOddAndEven.getActionCommand().equals(lastSelectionOption)) {
				stackGroupTypeChoices.setSelected(rdbtnOddAndEven.getModel(), true);
			}
			GridBagConstraints gbc_rdbtnOddAndEven = new GridBagConstraints();
			gbc_rdbtnOddAndEven.anchor = GridBagConstraints.WEST;
			gbc_rdbtnOddAndEven.insets = new Insets(0, 0, 5, 5);
			gbc_rdbtnOddAndEven.gridx = 0;
			gbc_rdbtnOddAndEven.gridy = 4;
			contentPanel.add(rdbtnOddAndEven, gbc_rdbtnOddAndEven);
		}
		{
			JRadioButton rdbtnAllPagesSeparately = new JRadioButton("Every page separately ");
			stackGroupTypeChoices.add(rdbtnAllPagesSeparately);
			rdbtnAllPagesSeparately.setActionCommand(String.valueOf(PageGroup.GROUP_TYPE_INDIVIDUAL));
			if (rdbtnAllPagesSeparately.getActionCommand().equals(lastSelectionOption)) {
				stackGroupTypeChoices.setSelected(rdbtnAllPagesSeparately.getModel(), true);
			}
			GridBagConstraints gbc_rdbtnAllPagesSeparately = new GridBagConstraints();
			gbc_rdbtnAllPagesSeparately.anchor = GridBagConstraints.WEST;
			gbc_rdbtnAllPagesSeparately.insets = new Insets(0, 0, 5, 5);
			gbc_rdbtnAllPagesSeparately.gridx = 0;
			gbc_rdbtnAllPagesSeparately.gridy = 5;
			contentPanel.add(rdbtnAllPagesSeparately, gbc_rdbtnAllPagesSeparately);
		}
		{
			final String showText = "Show advanced options";
			final String hideText = "Hide advanced options";
			final JButton btnShowAdvancedOptions = new JButton(showText);
			btnShowAdvancedOptions.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					boolean showNow = showText.equals(btnShowAdvancedOptions.getText());
					if (showNow) {
						btnShowAdvancedOptions.setText(hideText);
					} else {
						btnShowAdvancedOptions.setText(showText);
					}
					for (Component component : advancedOptions) {
						component.setVisible(showNow);
					}
				}
			});
			GridBagConstraints gbc_btnShowAdvancedOptions = new GridBagConstraints();
			gbc_btnShowAdvancedOptions.anchor = GridBagConstraints.WEST;
			gbc_btnShowAdvancedOptions.insets = new Insets(0, 0, 5, 5);
			gbc_btnShowAdvancedOptions.gridx = 0;
			gbc_btnShowAdvancedOptions.gridy = 7;
			contentPanel.add(btnShowAdvancedOptions, gbc_btnShowAdvancedOptions);
		}
		final JCheckBox chckbxCreateStackedView = new JCheckBox("Create stacked view that helps cropping");
		{
			chckbxCreateStackedView.setSelected(true);
			GridBagConstraints gbc_chckbxCreateStackedView = new GridBagConstraints();
			gbc_chckbxCreateStackedView.anchor = GridBagConstraints.WEST;
			gbc_chckbxCreateStackedView.insets = new Insets(0, 0, 0, 5);
			gbc_chckbxCreateStackedView.gridx = 0;
			gbc_chckbxCreateStackedView.gridy = 8;
			advancedOptions.add(chckbxCreateStackedView);
			chckbxCreateStackedView.setVisible(false);
			contentPanel.add(chckbxCreateStackedView, gbc_chckbxCreateStackedView);
		}


		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if (file == null) {
							JOptionPane.showMessageDialog(OpenDialog.this, "Select a pdf file first");
							return;
						}
						int type = Integer.valueOf(stackGroupTypeChoices.getSelection().getActionCommand());
						PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).addGenericEntryAsString(Model.PROPERTY_LAST_STACK_TYPE, stackGroupTypeChoices.getSelection().getActionCommand());
						OpenDialog.this.dispose();
						mainFrame.openFile(file, type, chckbxCreateStackedView.isSelected());
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						OpenDialog.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}

	}

	public void showFileChooserDialog() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(mainFrame.createFileFilter());
		String lastFile = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).getGenericEntryAsString(Model.PROPERTY_LAST_FILE);
		if (lastFile != null) {
			fileChooser.setCurrentDirectory(new File(lastFile));
		}

		int retval = fileChooser.showOpenDialog(OpenDialog.this);
		if (retval == JFileChooser.APPROVE_OPTION) {
			file = ResourceHandlerFactory.getResourceHandler(fileChooser.getSelectedFile());
			if (file != null) {
				PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).addGenericEntryAsString(Model.PROPERTY_LAST_FILE, file.toFile().getAbsolutePath());
				filePath.setText(file.toFile().getAbsolutePath());
			} else {
				filePath.setText("");
			}
		}
	}

	public void seMainFrame(PdfScissorsMainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

}
