package org.rr.jeborker.gui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.rr.commons.io.LineReader;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.xml.XMLUtils;
import org.rr.jeborker.FileRefreshBackground;
import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.IMetadataWriter;
import org.rr.jeborker.metadata.MetadataHandlerFactory;

public class PlainMetadataEditorController {
	
	private IMetadataReader reader;
	
	private PlainMetadataEditorView xmlMetadataView = null;
	
	private IResourceHandler resourceHandler;
	
	private static int locationOffset = 0;
	
	private int[] rowsToRefresh;
	
	private PlainMetadataEditorController(IResourceHandler resourceHandler, int[] rowsToRefresh) {
		this.rowsToRefresh = rowsToRefresh;
		this.reader = MetadataHandlerFactory.getReader(resourceHandler);
		this.resourceHandler = resourceHandler;
	}	
	
	public static PlainMetadataEditorController getInstance(final IResourceHandler resourceHandler, int[] rowsToRefresh) {
		PlainMetadataEditorController controller = new PlainMetadataEditorController(resourceHandler, rowsToRefresh);
		return controller;
	}	
	
	public void showXMLMetadataDialog() {
		PlainMetadataEditorView view = getView();
		view.editor.setContentType(reader.getPlainMetaDataMime());
		String plainMetaData = reader.getPlainMetaData();
		view.editor.setText(plainMetaData);
		toggleFolds(plainMetaData);
		view.setVisible(true);
	}
	
	/**
	 * Close the folds which have large data values.  
	 * @param xml The xml which is shown with the view.
	 */
	private void toggleFolds(String xml) {
		if(xml == null || xml.length() == 0) {
			return;
		}
		
		LineReader lineReader = new LineReader(new ByteArrayInputStream(xml.getBytes()));
		StringBuilder buffer = new StringBuilder();
		try {
			int lastOpenTag = 0;
			int cDataCount = 0;
			for(int i=0;;i++) {
				buffer.setLength(0);
				if(lineReader.readLine(buffer) > 0) {
					if(buffer.length() > 0) {
						if(buffer.charAt(buffer.length()-1) == '>') {
							lastOpenTag = i;
							cDataCount = 0;
						} else {
							cDataCount++;
						}
						
						if(lastOpenTag > 0 && cDataCount > 10) {
							//after a minimum of 10 data lines we close the fold.  
							PlainMetadataEditorView view = getView();
							view.xmlFoldingMargin.toggleFold(lastOpenTag);
							lastOpenTag = -1;
						}
					}
				} else {
					break;
				}
			}
		} catch (IOException e) {
			LoggerFactory.logWarning(this, "could not toggle large folds.", e);
		}
	}
	
	private PlainMetadataEditorView getView() {
		if(xmlMetadataView==null) {
			JFrame mainWindow = MainController.getController().getMainWindow();
			try {
				xmlMetadataView = new PlainMetadataEditorView(mainWindow);
			} catch (IOException e) {
				LoggerFactory.logWarning(this, "could not create XML editor component for " + reader.getEbookResource(), e);
			}
			this.initialize();
		}
		return xmlMetadataView;
	}
	
	private void initialize() {
		JFrame mainWindow = MainController.getController().getMainWindow();
		locationOffset = locationOffset + 10;
		xmlMetadataView.setLocation(mainWindow.getLocation().x + locationOffset, mainWindow.getLocation().y + locationOffset);
		xmlMetadataView.setSize(800, 600);
		xmlMetadataView.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		restorePropeties();
		
		initListeners();		
	}

	private void initListeners() {
		xmlMetadataView.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				storeProperties();
				locationOffset -= 10;
			}
			
		});
		
		xmlMetadataView.btnAbort.setAction(new AbstractAction() {
			private static final long serialVersionUID = -2551783359830548125L;

			{
				putValue(Action.NAME, Bundle.getString("PlainMetadataEditorView.abort"));
			}
			
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		
		xmlMetadataView.btnSave.setAction(new AbstractAction() {
			private static final long serialVersionUID = -2551783359830548125L;

			{
				putValue(Action.NAME, Bundle.getString("PlainMetadataEditorView.save"));
			}
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final IMetadataWriter writer = MetadataHandlerFactory.getWriter(resourceHandler);
				final String metadataContent = xmlMetadataView.editor.getText();
				try {
					writer.storePlainMetadata(metadataContent.getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					LoggerFactory.logWarning(this, "Could not encode data to UTF-8 " + resourceHandler, e1);
				}

				close();
				
				ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REFRESH_ENTRY_ACTION, resourceHandler.toString()).invokeAction();
				MainController.getController().refreshTableRows(rowsToRefresh, true);
			}
		});
		
		xmlMetadataView.btnFormat.setAction(new AbstractAction() {
			private static final long serialVersionUID = -2551783359830548125L;

			{
				putValue(Action.NAME, Bundle.getString("PlainMetadataEditorView.format"));
			}
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final String metadataContent = xmlMetadataView.editor.getText();
				final String formattedMetadataContent = XMLUtils.formatXML(metadataContent, 4, 160);
				//make it invisible because the editor should not show the cover data 
				//in an opened fold because this can is very slow and possibly can hang the app
				//for a while.
				xmlMetadataView.editor.setVisible(false);
				try {
					xmlMetadataView.editor.setText(formattedMetadataContent);
					toggleFolds(formattedMetadataContent);
				} catch(Exception ex) {
					LoggerFactory.logWarning(this, "could not format metadata.", ex);
				} finally {
					xmlMetadataView.editor.setVisible(true);
				}
			}
		});		
	}
	
	public void close() {
		storeProperties();
		locationOffset -= 10;
		
		xmlMetadataView.setVisible(false);
		xmlMetadataView.dispose();
		if(this.reader != null) {
			this.reader = null;
		}
	}
	
	private void storeProperties() {
		JeboorkerPreferences.addGenericEntryAsNumber("metadataDialogSizeWidth", getView().getSize().width);
		JeboorkerPreferences.addGenericEntryAsNumber("metadataDialogSizeHeight", getView().getSize().height);
		JeboorkerPreferences.addGenericEntryAsNumber("metadataDialogLocationX", getView().getLocation().x - locationOffset);
		JeboorkerPreferences.addGenericEntryAsNumber("metadataDialogLocationY", getView().getLocation().y - locationOffset);
	}
	
	private void restorePropeties() {
		//restore the window size from the preferences.
		Number metadataDialogSizeWidth = JeboorkerPreferences.getGenericEntryAsNumber("metadataDialogSizeWidth");
		Number metadataDialogSizeHeight = JeboorkerPreferences.getGenericEntryAsNumber("metadataDialogSizeHeight");
		if(metadataDialogSizeWidth!=null && metadataDialogSizeHeight!=null) {
			getView().setSize(metadataDialogSizeWidth.intValue(), metadataDialogSizeHeight.intValue());
		}
		
		//restore window location
		Point entryAsScreenLocation = JeboorkerPreferences.getGenericEntryAsScreenLocation("metadataDialogLocationX", "metadataDialogLocationY");
		if(entryAsScreenLocation != null) {
			getView().setLocation(entryAsScreenLocation);
		}		
	}
}
