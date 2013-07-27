package org.rr.jeborker.metadata.comicbook;

import static org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES.MIME_CBR;
import static org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES.MIME_CBZ;

import org.rr.commons.mufs.IResourceHandler;

public class ArchiveHandlerFactory {

	public static IArchiveHandler getHandler(IResourceHandler resource) {
		String mimeType = resource.getMimeType(true);
		if(MIME_CBZ.getMime().equals(mimeType)) {
			return new CBZArchiveHandler(resource);
		} else if(MIME_CBR.getMime().equals(mimeType)) {
			return new CBRArchiveHandler(resource);
		}
		return null;
	}
}
