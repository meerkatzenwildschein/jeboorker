package org.rr.jeborker.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.epub.EpubWriter;

import org.apache.commons.io.IOUtils;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.zip.LazyZipEntryStream;
import org.rr.commons.utils.zip.ZipUtils;
import org.rr.jeborker.JeboorkerConstants;
import org.rr.jeborker.JeboorkerConstants.SUPPORTED_MIMES;
import org.rr.jeborker.metadata.IMetadataReader;
import org.rr.jeborker.metadata.MetadataHandlerFactory;
import org.rr.jeborker.metadata.MetadataProperty;

/**
 * A converter for comic cbz files to epub 
 */
class CbzToEpubConverter implements IEBookConverter {

	private IResourceHandler cbzResource;
	
	public CbzToEpubConverter(IResourceHandler cbzResource) {
		this.cbzResource = cbzResource;
	}
	
	@Override
	public IResourceHandler convert() throws IOException {
		final List<String> cbzEntries = ZipUtils.list(this.cbzResource.getContentInputStream());
		final Book epub = this.createEpub(cbzEntries);
		final EpubWriter writer = new EpubWriter();
		
		IResourceHandler targetEpubResource = ResourceHandlerFactory.getUniqueResourceHandler(this.cbzResource, "epub");
		OutputStream contentOutputStream = targetEpubResource.getContentOutputStream(false);
		try {
			writer.write(epub, contentOutputStream);
		} finally {
			contentOutputStream.flush();
			contentOutputStream.close();
		}
		
		ConverterUtils.transferMetadata(this.cbzResource, targetEpubResource);
		
		return targetEpubResource;
	}
	
	private Book createEpub(List<String> cbzEntries) throws IOException {
		final Book epub = new Book();
		final Spine spine = new Spine();
		
		List<Resource> resources = new ArrayList<Resource>(cbzEntries.size());
		for(int i = 0; i < cbzEntries.size(); i++) {
			final String cbzEntry = cbzEntries.get(i);
			final Resource imageResource = new Resource(new LazyZipEntryStream(this.cbzResource, cbzEntry), cbzEntry);
			
			resources.add(imageResource);	
			epub.addResource(imageResource);
			
			this.attachSpineEntry(epub, spine, imageResource);
			
			//the first image from the cbz is the cover image.
			if(i == 0) {
				epub.setCoverImage(imageResource);
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
			InputStream spineTemplateIn = CbzToEpubConverter.class.getResourceAsStream("CbzToEpubSpineImageTemplate");
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

	@Override
	public SUPPORTED_MIMES getConversionSourceType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_CBZ;
	}

	@Override
	public SUPPORTED_MIMES getConversionTargetType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_EPUB;
	}
	
}
