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
import javax.swing.JTextArea;

import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.JeboorkerLogger;
import javax.swing.JScrollPane;

public class LogMonitorView extends JDialog implements ClipboardOwner {
	
	private static final long serialVersionUID = -8486417277805201337L;

	private static final int MAX_LOG_DISPLAY = 10000;
	
	private JButton btnCopy;
	private JTextArea textArea;
	private JButton btnClose;
	private JScrollPane scrollPane;
	
	public LogMonitorView(JFrame invoker, StringBuilder logContent) {
		super(invoker);
		initialize();
	}
	
	private void initialize() {
		setTitle(Jeboorker.app + " " + " Logfile");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		getContentPane().add(scrollPane, gbc_scrollPane);
		
		textArea = new JTextArea();
		textArea.setMargin(new Insets(3, 3, 3, 3));
		scrollPane.setViewportView(textArea);
		textArea.setEditable(false);
		
		btnCopy = new JButton(Bundle.getString("LogMonitorView.copy"));
		GridBagConstraints gbc_btnCopy = new GridBagConstraints();
		gbc_btnCopy.insets = new Insets(0, 0, 0, 5);
		gbc_btnCopy.gridx = 1;
		gbc_btnCopy.gridy = 1;
		getContentPane().add(btnCopy, gbc_btnCopy);
		btnCopy.addActionListener(new ActionListener()  {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				StringSelection stringSelection = new StringSelection( textArea.getText() );
			    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			    clipboard.setContents(stringSelection, LogMonitorView.this );				
			}
		});
		
		btnClose = new JButton(Bundle.getString("LogMonitorView.close"));
		GridBagConstraints gbc_btnClose = new GridBagConstraints();
		gbc_btnClose.gridx = 2;
		gbc_btnClose.gridy = 1;
		getContentPane().add(btnClose, gbc_btnClose);
		btnClose.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				LogMonitorController.getInstance().close();
			}
		});
		
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
				int end = JeboorkerLogger.log.length();
				int start = end > MAX_LOG_DISPLAY ? end - MAX_LOG_DISPLAY : 0;
				int length = end - start;
				char[] c = new char[length];
				JeboorkerLogger.log.getChars(start, end, c, 0);
				String text = new String(c);
				if(length == MAX_LOG_DISPLAY) {
					text = "..." + text;
				}
				textArea.setText(text);
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
