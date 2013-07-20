package org.rr.jeborker.metadata;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerInputStream;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.utils.UtilConstants;
import org.rr.jeborker.db.item.EbookPropertyItem;

class HTMLMetadataReader implements IMetadataReader {

	private IResourceHandler ebookResourceHandler;
	
	HTMLMetadataReader(IResourceHandler resource) {
		this.ebookResourceHandler = resource;
	}
	
	@Override
	public List<IResourceHandler> getEbookResource() {
		return Collections.singletonList(this.ebookResourceHandler);
	}

	@Override
	public List<MetadataProperty> readMetaData() {
		try {
			final String htmlHead = getHTMLHead();
			if(!htmlHead.isEmpty()) {
				List<MetadataProperty> extractMetadata = extractMetadata(htmlHead);
				return extractMetadata;
			}
		} catch (IOException e) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to read metadata for " + ebookResourceHandler, e);
		}
		return Collections.emptyList();
	}
	
	/**
	 * Extracts the head of the html document. Supports only reading the header and not the whole html file.
	 * @throws IOException
	 */
	private String getHTMLHead() throws IOException {
		final ResourceHandlerInputStream contentInputStream = this.ebookResourceHandler.getContentInputStream();
		try {
			final byte[] buf = new byte[512];
			final StringBuilder content = new StringBuilder();
			final String body = "<body>";
			int len;
			int bodyIndex = -1;
			String charset = "UTF-8";
			while((len = contentInputStream.read(buf)) != -1) {
				String html = new String(buf, 0, len, charset);
				if(html.indexOf('\ufffd') != -1) {
					String charsetLocationString = "text/html; charset=";
					int charsetStart = html.indexOf(charsetLocationString);
					int charsetEnd = html.indexOf('\"', charsetStart);
					if(charsetStart != -1 && charsetEnd != -1) {
						charset = html.substring(charsetStart + charsetLocationString.length(), charsetEnd);
						html = new String(buf, 0, len, charset);
					}
				}
				content.append(html);
				
				if((bodyIndex = StringUtils.find(content, body, content.length() - len - body.length(), UtilConstants.COMPARE_TEXT)) != -1) {
					break;
				}
			}
			
			if(bodyIndex != -1) {
				String metadata = content.toString().substring(0, bodyIndex);
				metadata = StringUtils.replace(metadata, new String[] {"<html>"}, "", UtilConstants.COMPARE_TEXT);
				metadata = StringUtils.ltrim(metadata, '\r', '\n');
				return metadata;
			}
		} finally {
			IOUtils.closeQuietly(contentInputStream);
		}
		return "";
	}
	
	/**
	 * Extracts the meta data from the given <code>content</code>.
	 * @param content The html content containing some meta data.
	 * @param bodyIndex The index of the body tag.
	 * @return The extracted meta data. Never returns <code>null</code>. 
	 * @throws IOException
	 */
	private List<MetadataProperty> extractMetadata(final String content) throws IOException {
		final List<MetadataProperty> result = new ArrayList<MetadataProperty>();
		final HtmlCleaner cleaner = new HtmlCleaner();
		final TagNode rootNode = cleaner.clean(new StringReader(content));
		
		//add meta tags
		final TagNode[] metaElements = rootNode.getElementsByName("meta", true);
		for (int i = 0; i < metaElements.length; i++) {
			String metaName = metaElements[i].getAttributeByName("name");
			String metaContent = metaElements[i].getAttributeByName("content");
			if(metaName == null) {
				Map<String, String> attributes = metaElements[i].getAttributes();
				for(String att : attributes.values()) {
					if(att != null && !att.equals(metaContent)) {
						metaName = att;
					}
				}
			}
			result.add(new MetadataProperty(metaName, metaContent));
		}
		
		//add title tag
		final TagNode[] titleElements = rootNode.getElementsByName("title", true);
		for (int i = 0; i < titleElements.length; i++) {
			StringBuffer text = titleElements[i].getText();
			result.add(new MetadataProperty("title", text));
		}
		return result;
	}


	@Override
	public List<MetadataProperty> getSupportedMetaData() {
		return Collections.emptyList();
	}

	@Override
	public void fillEbookPropertyItem(List<MetadataProperty> metadataProperties, EbookPropertyItem item) {
	}

	@Override
	public String getPlainMetaData() {
		try {
			final String htmlHead = getHTMLHead();
			return htmlHead;
		} catch (IOException e) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to read metadata for " + ebookResourceHandler, e);
		}
		return "";
	}

	@Override
	public String getPlainMetaDataMime() {
		return "text/html";
	}

	@Override
	public List<MetadataProperty> getMetadataByType(boolean create, List<MetadataProperty> props, METADATA_TYPES type) {
		return Collections.emptyList();
	}

}
