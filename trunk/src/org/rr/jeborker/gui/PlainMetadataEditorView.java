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
import javax.swing.text.PlainDocument;

import org.bounce.text.LineNumberMargin;
import org.bounce.text.xml.XMLEditorKit;
import org.bounce.text.xml.XMLFoldingMargin;
import org.rr.commons.swing.components.JRScrollPane;
import org.rr.commons.swing.layout.EqualsLayout;


class PlainMetadataEditorView extends JDialog {

	private static final long serialVersionUID = -5833977607733981288L;

	JEditorPane editor;
	
	JButton btnAbort;
	
	JButton btnSave;
	
	JButton btnFormat;
	
	XMLFoldingMargin xmlFoldingMargin;
	private JPanel leftBottomPanel;
	private JPanel rightBottomPanel;
	
	PlainMetadataEditorView(final JFrame invoker, final String metadataMime) throws IOException {
		super(invoker);
		setTitle(Bundle.getString("PlainMetadataEditorView.title"));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JRScrollPane scroller = new JRScrollPane();
		
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
		
		if(metadataMime.contains("/xml")) {
			XMLEditorKit xmlKit = new XMLEditorKit();
			xmlKit.setAutoIndentation(true);
			xmlKit.setTagCompletion(true);
//			kit.setStyle(XMLStyleConstants.ATTRIBUTE_NAME, new Color(255, 0, 0), Font.BOLD);			
			editor.setEditorKitForContentType("text/xml", xmlKit);
			editor.setFont(new Font("Courier", Font.PLAIN, 12));
			editor.getDocument().putProperty(PlainDocument.tabSizeAttribute, new Integer(4));
			editor.getDocument().putProperty(XMLEditorKit.ERROR_HIGHLIGHTING_ATTRIBUTE, new Boolean(true));		
			
			// Add the number margin and folding margin as a Row Header View
			JPanel rowHeader = new JPanel(new BorderLayout());
			xmlFoldingMargin = new XMLFoldingMargin(editor);
			rowHeader.add(xmlFoldingMargin, BorderLayout.EAST);
			rowHeader.add(new LineNumberMargin(editor), BorderLayout.WEST);
			scroller.setRowHeaderView(rowHeader);			
		} else if(metadataMime.contains("/html")) {
			XMLEditorKit htmlKit = new XMLEditorKit();
			editor.setEditorKitForContentType("text/html", htmlKit);
			editor.setFont(new Font("Courier", Font.PLAIN, 12));
			editor.getDocument().putProperty(PlainDocument.tabSizeAttribute, new Integer(4));
			editor.getDocument().putProperty(XMLEditorKit.ERROR_HIGHLIGHTING_ATTRIBUTE, new Boolean(true));				
		} else {
			XMLEditorKit htmlKit = new XMLEditorKit();
			editor.setEditorKitForContentType("text/plain", htmlKit);
			editor.setFont(new Font("Courier", Font.PLAIN, 12));
			editor.getDocument().putProperty(PlainDocument.tabSizeAttribute, new Integer(4));
			editor.getDocument().putProperty(XMLEditorKit.ERROR_HIGHLIGHTING_ATTRIBUTE, new Boolean(true));				
		}
		
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
		leftBottomPanel.setLayout(new EqualsLayout(EqualsLayout.LEFT, 0));
		
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
