package org.rr.jeborker.metadata.download;

import java.util.List;

public interface MetadataDownloader {

	public List<MetadataDownloadEntry> search(String phrase);

}
