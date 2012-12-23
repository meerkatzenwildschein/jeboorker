package org.rr.jeborker.metadata.comicbook;

import static org.rr.jeborker.JeboorkerConstants.MIME_CBZ;

import org.rr.commons.mufs.IResourceHandler;

public class ArchiveHandlerFactory {

	public static IArchiveHandler getHandler(IResourceHandler resource) {
		String mimeType = resource.getMimeType();
		if(MIME_CBZ.equals(mimeType)) {
			return new CBZArchiveHandler(resource);
		}
		return null;
	}
}
