package org.rr.jeborker.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.rr.commons.swing.SwingUtils;
import org.rr.commons.swing.components.JRScrollPane;
import org.rr.commons.swing.layout.EqualsLayout;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.app.JeboorkerLogger;

class LoggerView extends JDialog implements ClipboardOwner {
	
	private static final long serialVersionUID = -8486417277805201337L;

	private final ActionListener closeAction = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			logMonitorController.close();
		}
	};
	
	private final ActionListener copyClipboardAction = new ActionListener()  {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			StringSelection stringSelection = new StringSelection( textArea.getText() );
		    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		    clipboard.setContents(stringSelection, LoggerView.this );				
		}
	};
	
	private LoggerController logMonitorController;
	
	private JButton btnCopy;
	private JTextArea textArea;
	private JButton btnClose;
	private JRScrollPane scrollPane;
	private JPanel bottomPanel;
	
	LoggerView(JFrame mainWindow, LoggerController logMonitorController) {
		super(mainWindow);
		this.logMonitorController = logMonitorController;
		initialize();
		setModal(true);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);		
	}
	
	private void initialize() {
		setTitle(Jeboorker.APP + " " + " Logfile");
		setSize(800, 600);
		SwingUtils.centerOnScreen(this);
		SwingUtils.setEscapeWindowAction(this, closeAction);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		scrollPane = new JRScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 0, 0, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		getContentPane().add(scrollPane, gbc_scrollPane);
		
		textArea = new JTextArea();
		textArea.setMargin(new Insets(3, 3, 3, 3));
		scrollPane.setViewportView(textArea);
		textArea.setEditable(false);
		textArea.setOpaque(false);
		
		bottomPanel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(5, 0, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		getContentPane().add(bottomPanel, gbc_panel);
		bottomPanel.setLayout(new EqualsLayout(3));
		
		btnCopy = new JButton(Bundle.getString("LogMonitorView.copy"));
		bottomPanel.add(btnCopy);
		
		btnClose = new JButton(Bundle.getString("LogMonitorView.close"));
		bottomPanel.add(btnClose);
		btnClose.addActionListener(closeAction);
		btnCopy.addActionListener(copyClipboardAction);
		
		this.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				setLogText();
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				setLogText();
			}
			
			private void setLogText() {
				textArea.setText(JeboorkerLogger.getLogFilePrint());
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				MainController.getController().getLoggerController().close();
			}
			
		});
	}

	@Override
	/**
	 * Empty implementation of the ClipboardOwner interface.
	 */
	public void lostOwnership(Clipboard aClipboard, Transferable aContents) {
		// do nothing
	}
	
	
}
