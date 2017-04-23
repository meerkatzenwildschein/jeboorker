package org.rr.commons.mufs;


public class MimeUtils {

	public static final String MIME_PDF = "application/pdf";
	public static final String MIME_CBZ = "application/x-cbz";
	public static final String MIME_CBR = "application/x-cbr";
	public static final String MIME_EPUB = "application/epub+zip";
	public static final String MIME_TEXT = "text/plain";
	public static final String MIME_GIF = "image/gif";
	public static final String MIME_PNG = "image/png";
	public static final String MIME_JPEG = "image/jpeg";
	public static final String MIME_HTML = "text/html";
	public static final String MIME_XML = "text/xml";
	public static final String MIME_RTF = "application/rtf";
	public static final String MIME_MOBI = "application/x-mobipocket-ebook";
	public static final String MIME_AZW = "application/vnd.amazon.ebook";
	public static final String MIME_FB2 = "application/x-fictionbook+xml";
	public static final String MIME_LIT = "application/x-ms-reader";
	public static final String MIME_PKG = "application/x-newton-compatible-pkg";
	public static final String MIME_RB = "application/x-rocketbook";
	public static final String MIME_DJVU = "image/vnd.djvu";
	public static final String MIME_DOC = "application/msword";
	public static final String MIME_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

	public static boolean isPdf(IResourceHandler resourceHandler, boolean force) {
		return MIME_PDF.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isCbz(IResourceHandler resourceHandler, boolean force) {
		return MIME_CBZ.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isCbr(IResourceHandler resourceHandler, boolean force) {
		return MIME_CBR.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isEpub(IResourceHandler resourceHandler, boolean force) {
		return MIME_EPUB.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isText(IResourceHandler resourceHandler, boolean force) {
		return MIME_TEXT.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isGif(IResourceHandler resourceHandler, boolean force) {
		return MIME_GIF.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isPng(IResourceHandler resourceHandler, boolean force) {
		return MIME_PNG.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isJpeg(IResourceHandler resourceHandler, boolean force) {
		return MIME_JPEG.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isHtml(IResourceHandler resourceHandler, boolean force) {
		return MIME_HTML.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isXml(IResourceHandler resourceHandler, boolean force) {
		return MIME_XML.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isRtf(IResourceHandler resourceHandler, boolean force) {
		return MIME_RTF.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isMobi(IResourceHandler resourceHandler, boolean force) {
		return MIME_MOBI.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isAzw(IResourceHandler resourceHandler, boolean force) {
		return MIME_AZW.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isFb2(IResourceHandler resourceHandler, boolean force) {
		return MIME_FB2.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isLit(IResourceHandler resourceHandler, boolean force) {
		return MIME_LIT.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isPkg(IResourceHandler resourceHandler, boolean force) {
		return MIME_PKG.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isRb(IResourceHandler resourceHandler, boolean force) {
		return MIME_RB.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isDjvu(IResourceHandler resourceHandler, boolean force) {
		return MIME_DJVU.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isDoc(IResourceHandler resourceHandler, boolean force) {
		return MIME_DOC.equals(resourceHandler.getMimeType(force));
	}

	public static boolean isDocx(IResourceHandler resourceHandler, boolean force) {
		return MIME_DOCX.equals(resourceHandler.getMimeType(force));
	}
	
	public static boolean isImageMime(String mime) {
		return mime != null && mime.startsWith("image/");
	}
	
	public static boolean isJpegMime(String mime) {
		return MIME_JPEG.equals(mime); 
	}
	
	/**
	 * Evaluates the mime type from a file name with file name extension. Foe example a file named <code>picture.gif</code> will cause a
	 * mime <code>image/gif</code>. If no mime could be detected, the given <code>defaultMime</code> is returned.
	 * 
	 * @param imageName The name of the image.
	 * @param defaultMime The mime to be returned if no file extension could be detected.
	 * @return A mime format string.
	 */
	public static String getImageMimeFromFileName(String imageName, String defaultMime) {
		if(imageName == null) {
			return defaultMime;
		}
		
		imageName = imageName.toLowerCase();
		if(imageName.endsWith(".jpg") || imageName.endsWith(".jpeg")) {
			return MIME_JPEG;
		} else if(imageName.endsWith(".gif")) {
			return MIME_GIF;
		} else if(imageName.endsWith(".png")) {
			return MIME_PNG;
		}
		
		return defaultMime;
	}
}
