package org.rr.jeborker.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.PlainDocument;

import org.bounce.text.LineNumberMargin;
import org.bounce.text.xml.XMLEditorKit;
import org.bounce.text.xml.XMLFoldingMargin;
import org.rr.commons.swing.layout.EqualsLayout;


public class PlainMetadataEditorView extends JDialog {

	private static final long serialVersionUID = -5833977607733981288L;

	JEditorPane editor;
	
	JButton btnAbort;
	
	JButton btnSave;
	
	JButton btnFormat;
	
	XMLFoldingMargin xmlFoldingMargin;
	private JPanel leftBottomPanel;
	private JPanel rightBottomPanel;
	
	PlainMetadataEditorView(JFrame invoker) throws IOException {
		super(invoker);
		setTitle(Bundle.getString("PlainMetadataEditorView.title"));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JScrollPane scroller = new JScrollPane();
		
		editor = new JEditorPane() {
			private static final long serialVersionUID = 3319037743254259329L;

			public void paintComponent(Graphics g) {
				//enable antialisasing for the JEditorPane.
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				super.paintComponent(g2);
			}
		};
		scroller.setViewportView(editor);
		editor.setOpaque(false);
		editor.setBackground(Color.WHITE);
		
		XMLEditorKit kit = new XMLEditorKit();
		editor.setEditorKitForContentType("text/xml", kit);
		
		// Set the font style.
		editor.setFont(new Font("Courier", Font.PLAIN, 12));
		
		// Set the tab size
		editor.getDocument().putProperty(PlainDocument.tabSizeAttribute, new Integer(4));
		// Enable auto indentation.
		kit.setAutoIndentation(true);

		// Enable error highlighting.
		editor.getDocument().putProperty(XMLEditorKit.ERROR_HIGHLIGHTING_ATTRIBUTE, new Boolean(true));

		// Enable tag completion.
		kit.setTagCompletion(true);

		// Set a style
//		kit.setStyle(XMLStyleConstants.ATTRIBUTE_NAME, new Color(255, 0, 0), Font.BOLD);
		
		// Add the number margin and folding margin as a Row Header View
		JPanel rowHeader = new JPanel(new BorderLayout());
		xmlFoldingMargin = new XMLFoldingMargin(editor);
		rowHeader.add(xmlFoldingMargin, BorderLayout.EAST);
		rowHeader.add(new LineNumberMargin(editor), BorderLayout.WEST);
		scroller.setRowHeaderView(rowHeader);
		
		GridBagConstraints gbc_editorPane = new GridBagConstraints();
		gbc_editorPane.insets = new Insets(0, 0, 5, 0);
		gbc_editorPane.gridwidth = 2;
		gbc_editorPane.fill = GridBagConstraints.BOTH;
		gbc_editorPane.gridx = 0;
		gbc_editorPane.gridy = 0;
		getContentPane().add(scroller, gbc_editorPane);
		
		leftBottomPanel = new JPanel();
		GridBagConstraints gbc_leftBottomPanel = new GridBagConstraints();
		gbc_leftBottomPanel.fill = GridBagConstraints.BOTH;
		gbc_leftBottomPanel.insets = new Insets(0, 5, 5, 0);
		gbc_leftBottomPanel.gridx = 0;
		gbc_leftBottomPanel.gridy = 1;
		getContentPane().add(leftBottomPanel, gbc_leftBottomPanel);
		leftBottomPanel.setLayout(new EqualsLayout(EqualsLayout.LEFT, 0, new Insets(10,10,10,10)));
		
		btnFormat = new JButton(Bundle.getString("PlainMetadataEditorView.format"));
		leftBottomPanel.add(btnFormat);
		
		rightBottomPanel = new JPanel();
		GridBagConstraints gbc_rightBottomPanel = new GridBagConstraints();
		gbc_rightBottomPanel.insets = new Insets(0, 0, 5, 5);
		gbc_rightBottomPanel.fill = GridBagConstraints.BOTH;
		gbc_rightBottomPanel.gridx = 1;
		gbc_rightBottomPanel.gridy = 1;
		getContentPane().add(rightBottomPanel, gbc_rightBottomPanel);
		rightBottomPanel.setLayout(new EqualsLayout(3));
		
		btnAbort = new JButton(Bundle.getString("PlainMetadataEditorView.abort"));
		rightBottomPanel.add(btnAbort);
		
		btnSave = new JButton(Bundle.getString("PlainMetadataEditorView.save"));
		rightBottomPanel.add(btnSave);
	}

}
