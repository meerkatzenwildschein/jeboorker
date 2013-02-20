package org.rr.jeborker.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.epub.EpubWriter;

import org.apache.commons.io.IOUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.compression.truezip.LazyTrueZipEntryStream;
import org.rr.commons.utils.compression.truezip.TrueZipUtils;
import org.rr.jeborker.JeboorkerConstants;
import org.rr.jeborker.JeboorkerConstants.SUPPORTED_MIMES;

/**
 * A converter for image archives to epub 
 */
abstract class ACompressedImageToEpubConverter implements IEBookConverter {

	protected IResourceHandler comicBookResource;
	
	public ACompressedImageToEpubConverter(IResourceHandler cbzResource) {
		this.comicBookResource = cbzResource;
	}
	
	@Override
	public IResourceHandler convert() throws IOException {
		final List<String> cbzEntries = listEntries(this.comicBookResource);
		final Book epub = this.createEpub(cbzEntries);
		final EpubWriter writer = new EpubWriter();
		
		IResourceHandler targetEpubResource = ResourceHandlerFactory.getUniqueResourceHandler(this.comicBookResource, "epub");
		OutputStream contentOutputStream = targetEpubResource.getContentOutputStream(false);
		try {
			writer.write(epub, contentOutputStream);
		} finally {
			contentOutputStream.flush();
			contentOutputStream.close();
		}
		
		ConverterUtils.transferMetadata(this.comicBookResource, targetEpubResource);
		
		return targetEpubResource;
	}
	
	private Book createEpub(List<String> cbzEntries) throws IOException {
		final Book epub = new Book();
		final Spine spine = new Spine();
		
		List<Resource> resources = new ArrayList<Resource>(cbzEntries.size());
		for(int i = 0; i < cbzEntries.size(); i++) {
			final String cbzEntry = cbzEntries.get(i);
			if(isImage(cbzEntry)) {
				final String cbzHrefEntry = createHrefEntry(cbzEntry, epub);
				final Resource imageResource = new Resource(getCompressionEntryStream(this.comicBookResource, cbzEntry), cbzHrefEntry);
				
				resources.add(imageResource);	
				epub.addResource(imageResource);
				
				this.attachSpineEntry(epub, spine, imageResource);
				
				//the first image from the cbz is the cover image.
				if(i == 0) {
					epub.setCoverImage(imageResource);
				}
			}
		}
		epub.setSpine(spine);
		return epub;
	}
	
	/**
	 * Creates a xhtml doc for the given image and add it as spine to the given epub.
	 * @throws IOException 
	 */
	private void attachSpineEntry(final Book epub, final Spine spine, final Resource imageResource) throws IOException {
		String imageName = imageResource.getHref();
		if(isImage(imageName)) {
			if(imageName.lastIndexOf('.') != -1) {
				imageName = imageName.substring(0, imageName.lastIndexOf('.'));
			}
			Resource spineResource = new Resource(imageName + ".xhtml");
			InputStream spineTemplateIn = ACompressedImageToEpubConverter.class.getResourceAsStream("CbzToEpubSpineImageTemplate");
			String spineTemplate = IOUtils.toString(spineTemplateIn);
			String spineDoc = MessageFormat.format(spineTemplate, new Object[] {imageResource.getHref()});
			spineResource.setData(spineDoc.getBytes("UTF-8"));
			spine.addResource(spineResource);
			epub.addResource(spineResource);
		}
	}
	
	/**
	 * Test if the given name have an image file extension.
	 */
	private boolean isImage(String imageName) {
		if(imageName.endsWith(".jpg") || imageName.endsWith(".jpeg") || imageName.endsWith(".png") || imageName.endsWith(".gif")) {
			return true;
		}
		return false;
	}
	
	private String createHrefEntry(final String cbzEntry, final Book epub) {
		try {
			StringBuilder result = new StringBuilder();
			for(int i = 0; i < cbzEntry.length(); i++) {
				char c = cbzEntry.charAt(i);
				if(Character.isWhitespace(c)) {
					result.append('_');
				} else if(Character.isDigit(c)) {
					result.append(c);
				} else if(c >= 'A' || c <= 'z') {
					result.append(c);
				} else if(c == '/') {
					result.setLength(0); //remove path segement
				}
			}
			String href = result.toString();
			while(epub.getResources().getByHref(href) != null) {
				if(href.lastIndexOf('.') != -1) {
					href = href.substring(0, href.lastIndexOf('.')) + "_" + href.substring(href.lastIndexOf('.'));
				}
			}
			
			return href;
		} catch(Exception e) {
			return cbzEntry;
		}
	}

	protected abstract InputStream getCompressionEntryStream(IResourceHandler resourceHandler, String entry);
	
	protected abstract List<String> listEntries(IResourceHandler cbzResource);
}
