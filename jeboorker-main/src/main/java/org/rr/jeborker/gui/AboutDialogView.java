package org.rr.jeborker.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.swing.layout.VerticalLayout;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class AboutDialogView extends JDialog {

	private static final long serialVersionUID = -5833977607733981288L;
	
	AboutDialogView(final JFrame invoker, final AboutDialogController controller) {
		super(invoker);
		this.setResizable(false);
		this.setSize(400, 200);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				controller.close();
				AboutDialogView.this.dispose();
			}
			
		});
		
		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5,5,5,5));
		setContentPane(contentPanel);
		
		SwingUtils.centerOnWindow(invoker, this);
		setTitle(Bundle.getString("PlainMetadataEditorView.about"));
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JLabel applicationLogo = new JLabel(" ");
		applicationLogo.setVerticalAlignment(JLabel.TOP);
		applicationLogo.setIcon(ImageResourceBundle.getResourceAsImageIcon("logo_64.png"));
		getContentPane().add(applicationLogo, BorderLayout.WEST);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new VerticalLayout(3, VerticalLayout.CENTER));
		
		JLabel lblVersion = new JLabel(Jeboorker.APP + " v." + Jeboorker.VERSION + " (GPL v2+)");
		panel.add(lblVersion);
		
		JLabel lblUrl = new JLabel();
		lblUrl.setForeground(Color.BLUE);
		lblUrl.setText(Jeboorker.URL);
		lblUrl.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				try {
					SwingUtils.openURL(Jeboorker.URL);
				} catch (Exception e1) {
					LoggerFactory.getLogger().log(Level.WARNING, "could not browse url " + Jeboorker.URL, e);
				}
			}
			
		});
		panel.add(lblUrl);
		
		JLabel lblMe = new JLabel("By Rüdiger Rüttelstein");
		panel.add(lblMe);
		
		JButton buttonOK = new JButton();
		buttonOK.setAction(new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.close();
				AboutDialogView.this.dispose();
			}
		});
		buttonOK.setText("OK");
		getContentPane().add(buttonOK, BorderLayout.SOUTH);
		
	}

}
