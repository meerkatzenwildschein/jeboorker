package bd.amazed.pdfscissors.view;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AboutView extends JDialog {

	private static final long serialVersionUID = 1L;
	private String url = "www.pdfscissors.com"; // @jve:decl-index=0:
	private String versionValue = "0.0.2 beta";
	private String homePageValue = "<html><a href=\"" + url + "\">" + url + "</a></html>";
	private String sourceForget = "<html><a href=\"https://sourceforge.net/projects/pdfscissors/\">sourceforge.net/projects/pdfscissors</a></html>";
	private String appDesc = "<HTML>" + "<B>PDF Scissors </B>" + versionValue + "<BR><BR>" +

	"<B>Author: </B> Abdullah Al Mazed (Gagan)" + "<BR><BR>" +

	"<B>Code contributor: </B> Sergio Gragera Camino" + "<BR><BR>" +

	"<B> License: </B>This is a free open source software.<BR>" + "Use it for free respecting Affero General Public License.<BR> <BR>" +

	"<B>If this software was any useful, please drop a comment to inspire!<br>Enjoy! :) </B>" + "</HTML>";

	private JPanel jContentPane = null;
	private JLabel image = null;
	private JPanel centerPanel = null;
	private JLabel description = null;
	private JPanel bottomPanel = null;
	private JButton button = null;
	private JButton sourceForgeButton = null;

	/**
	 * This is the default constructor
	 */
	public AboutView(Frame owner) {
		super(owner);
		initialize();
		Dimension screen = getToolkit().getScreenSize();
		this.setBounds((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2, getWidth(), getHeight());
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(621, 283);
		this.setContentPane(getJContentPane());
		this.setTitle("About PDF Scissors");
		this.setResizable(false);
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			image = new JLabel();
			URL imageURL = PdfScissorsMainFrame.class.getResource("/logo.png");
			if (imageURL != null) { // image found
				image.setIcon(new ImageIcon(imageURL));
			}
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(image, BorderLayout.WEST);
			jContentPane.add(getCenterPanel(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes centerPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getCenterPanel() {
		if (centerPanel == null) {

			centerPanel = new JPanel();
			centerPanel.setLayout(new BorderLayout());
			centerPanel.add(getDescription(), BorderLayout.CENTER);
			centerPanel.add(getBottomPanel(), BorderLayout.SOUTH);
		}
		return centerPanel;
	}

	/**
	 * This method initializes description
	 *
	 * @return javax.swing.JTextArea
	 */
	private JLabel getDescription() {
		if (description == null) {
			description = new JLabel(appDesc);
		}
		return description;
	}

	/**
	 * This method initializes bottomPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getBottomPanel() {
		if (bottomPanel == null) {
			bottomPanel = new JPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			bottomPanel.add(getButton());
			bottomPanel.add(getSourceButton());
		}
		return bottomPanel;
	}

	/**
	 * This method initializes button
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getButton() {
		if (button == null) {
			button = new JButton("url");
			button.setText(homePageValue);
			button.setBorderPainted(false);
			button.setContentAreaFilled(false);
			button.setRolloverEnabled(true);
			button.setFocusPainted(false);
			button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						if (Desktop.isDesktopSupported()) {
							Desktop.getDesktop().browse(new java.net.URI(url));
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
		}
		return button;
	}

	/**
	 * This method initializes button
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getSourceButton() {
		if (sourceForgeButton == null) {
			sourceForgeButton = new JButton("url");
			sourceForgeButton.setText(sourceForget);
			sourceForgeButton.setBorderPainted(false);
			sourceForgeButton.setContentAreaFilled(false);
			sourceForgeButton.setRolloverEnabled(true);
			sourceForgeButton.setFocusPainted(false);
			sourceForgeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						if (Desktop.isDesktopSupported()) {
							Desktop.getDesktop().browse(new java.net.URI(sourceForget));
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
		}
		return sourceForgeButton;
	}

}
