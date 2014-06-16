package bd.amazed.pdfscissors.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

public class StackViewCreationDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JTextArea textArea = null;
	private JButton cancelButton = null;
	private JLabel helpImage = null;
	private JPanel bottomPanel = null;
	private ActionListener actionListener;
	private JProgressBar progressbar = null;

	public StackViewCreationDialog(ActionListener okActionListener) {
		this.actionListener = okActionListener;
		initialize();
	}

	/**
	 * This is the default constructor
	 */
	public StackViewCreationDialog(JFrame owner) {
		super(owner, "Stack creation", true);
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(717, 464);
		this.setContentPane(getJContentPane());
		Dimension screen = getToolkit().getScreenSize();
		this.setBounds((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2, getWidth(), getHeight());
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			helpImage = new JLabel();
			helpImage.setText("");
			helpImage.setHorizontalAlignment(SwingConstants.CENTER);
			helpImage.setIcon(new ImageIcon(getClass().getResource("/stackedpage.png")));

			JPanel textPanel = new JPanel();
			textPanel.add(getTextArea());

			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(textPanel, BorderLayout.NORTH);
			jContentPane.add(helpImage, BorderLayout.CENTER);
			jContentPane.add(getBottomPanel(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes textArea
	 *
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getTextArea() {
		if (textArea == null) {
			textArea = new JTextArea();
			textArea.setEditable(false);
			textArea.setCursor(null);
			textArea.setOpaque(false);
			textArea.setFocusable(false);
			textArea.setText("Creating stack, please wait...\n\nStacking pages with transparency will help you cropping all pages together.\n");
		}
		return textArea;
	}

	/**
	 * This method initializes bottomPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getBottomPanel() {
		if (bottomPanel == null) {
			bottomPanel = new JPanel();

		}
		return bottomPanel;
	}

	/**
	 * This method initializes progressbar
	 *
	 * @return javax.swing.JProgressBar
	 */
	public JProgressBar getProgressbar() {
		if (progressbar == null) {
			progressbar = new JProgressBar();
			progressbar.setToolTipText("Creating page stack. This will help you during cropping.");
			progressbar.setString("Opening pdf...");
			progressbar.setStringPainted(true);
		}
		return progressbar;
	}

	public void enableProgress(SwingWorker worker, ActionListener progressCancelListener) {
		getBottomPanel().removeAll();
		getBottomPanel().add(getProgressbar(), null);
		getBottomPanel().add(getCancelButton());
		getCancelButton().addActionListener(progressCancelListener);
		worker.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("progress".equals(evt.getPropertyName())) {
					int progress = (Integer) evt.getNewValue();
					getProgressbar().setValue(progress);
				} else if ("done".equals(evt.getPropertyName())) {
					dispose();
				} else if ("message".equals(evt.getPropertyName())) {
					getProgressbar().setString((String)evt.getNewValue());
				}
			}
		});
	}

	/**
	 * This method initializes cancelButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton("Cancel");
		}
		return cancelButton;
	}
} // @jve:decl-index=0:visual-constraint="10,10"
