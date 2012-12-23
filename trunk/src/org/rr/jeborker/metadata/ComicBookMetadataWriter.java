package org.rr.jeborker.metadata;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.metadata.comicbook.ComicBookDocument;
import org.rr.jeborker.metadata.comicbook.ComicBookReader;
import org.rr.jeborker.metadata.comicbook.ComicBookWriter;

class ComicBookMetadataWriter implements IMetadataWriter {

	private IResourceHandler resource;
	
	ComicBookMetadataWriter(IResourceHandler resource) {
		this.resource = resource;
	}
	
	@Override
	public void writeMetadata(List<MetadataProperty> props) {
		try {
			final ComicBookReader reader = new ComicBookReader(resource);
			final ComicBookDocument doc = reader.getDocument();
			final ComicBookWriter writer = new ComicBookWriter(doc, resource);
			final HashMap<String, Object> docInfo = doc.getInfo();
//			final List<ComicBookPageInfo> docPages = doc.getPages();
			
			docInfo.clear();
			
			for(MetadataProperty prop : props) {
				String name = prop.getName();
				String value = prop.getValueAsString();
				docInfo.put(name, value);
			}
			writer.writeDocument();
		} catch (IOException e) {
			LoggerFactory.getLogger().log(Level.WARNING, "Failed to store metadata to " + resource, e);
		}
	}

	@Override
	public void setCover(byte[] cover) {
		//not supported with comic book
	}

	@Override
	public void storePlainMetadata(byte[] plainMetadata) {
		ComicBookWriter writer = new ComicBookWriter(null, resource);
		try {
			writer.writePlainXML(plainMetadata);
		} catch (IOException e) {
			LoggerFactory.getLogger().log(Level.WARNING, "Failed to store plain metadata to " + resource, e);
		}
	}

}
