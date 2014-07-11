package org.rr.jeborker.metadata.comicbook;

import java.io.IOException;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.MimeUtils;

import com.itextpdf.text.log.LoggerFactory;

public class ArchiveHandlerFactory {

	public static IArchiveHandler getHandlerInitialized(IResourceHandler resourceHandler) {
		IArchiveHandler handler = getHandler(resourceHandler);
		try {
			handler.readArchive();
		} catch (IOException e) {
			LoggerFactory.getLogger(ArchiveHandlerFactory.class).error("Failed to read archive", e);
		}
		return handler;
	}

	public static IArchiveHandler getHandler(IResourceHandler resourceHandler) {
		if(MimeUtils.isCbz(resourceHandler, true)) {
			return new CBZArchiveHandler(resourceHandler);
		} else if(MimeUtils.isCbr(resourceHandler, true)) {
			return new CBRArchiveHandler(resourceHandler);
		}
		return null;
	}
}
