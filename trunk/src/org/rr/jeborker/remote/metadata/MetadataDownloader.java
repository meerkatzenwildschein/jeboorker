package org.rr.jeborker.remote.metadata;

import java.util.List;

public interface MetadataDownloader {

	public List<MetadataDownloadEntry> search(String phrase);

}
