package org.rr.jeborker.gui.model;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.rr.jeborker.remote.metadata.MetadataDownloadEntry;
import org.rr.jeborker.remote.metadata.MetadataDownloader;

public class MetadataDownloadModel extends AbstractTableModel {

	private MetadataDownloader downloader;
	
	private String searchPhrase;
	
	private List<MetadataDownloadEntry> searchEntries;
	
	public MetadataDownloadModel(MetadataDownloader downloader, String searchPhrase) {
		this.downloader = downloader;
		this.searchPhrase = searchPhrase;
	}

	/**
	 * Invokes the {@link MetadataDownloader} and provide it's entries with this {@link MetadataDownloadModel}
	 * instance. This method blocks as long as all data for the model is loaded. 
	 * 
	 * This Method isn't be invoked automatically by the {@link MetadataDownloadModel} instance.
	 */
	public void loadSearchResult() {
		this.searchEntries = this.downloader.search(this.searchPhrase);
		for(MetadataDownloadEntry searchEntry : this.searchEntries) {
			searchEntry.getThumbnailImageBytes(); //lazy loading
			searchEntry.getDescription(); //lazy loading
		}
	}
	
	@Override
	public int getRowCount() {
		if(searchEntries != null) {
			return searchEntries.size();
		}
		return 0;
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return MetadataDownloadEntry.class.getName();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return MetadataDownloadEntry.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return searchEntries.get(rowIndex);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		searchEntries.set(rowIndex, (MetadataDownloadEntry) aValue);
	}

}
