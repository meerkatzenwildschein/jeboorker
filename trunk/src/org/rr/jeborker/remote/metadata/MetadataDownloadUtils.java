package org.rr.jeborker.remote.metadata;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ThreadUtils;

class MetadataDownloadUtils {

	static List<byte[]> loadPages(Iterable<URL> url, int threads) throws IOException {
		return ThreadUtils.loopAndWait(url, new ThreadUtils.RunnableImpl<URL, byte[]>() {

			@Override
			public byte[] run(URL url) {
				try {
					LoggerFactory.getLogger().log(Level.INFO, "Downloading " + url);
					IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceHandler(url);
					return resourceLoader.getContent();
				} catch (IOException e) {
					LoggerFactory.getLogger(this).log(Level.INFO, "Failed load " + url, e);
				}
				return null;
			}
		}, threads);
	}
}
