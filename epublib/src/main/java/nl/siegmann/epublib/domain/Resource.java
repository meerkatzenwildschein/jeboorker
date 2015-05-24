package nl.siegmann.epublib.domain;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.service.MediatypeService;
import nl.siegmann.epublib.util.IOUtil;
import nl.siegmann.epublib.util.commons.io.XmlStreamReader;

import org.rr.commons.utils.StringUtil;

/**
 * Represents a resource that is part of the epub.
 * A resource can be a html file, image, xml, etc.
 *
 * @author paul
 *
 */
public class Resource implements Serializable {

	private static final long serialVersionUID = 1043946707835004037L;
	private String id;
	private String title;
	private String href;
	private byte[] rawHref;
	private MediaType mediaType;
	private String inputEncoding = Constants.ENCODING;
	private byte[] data;
	private InputStream in;
		
	private String fileName;
	private long cachedSize;
	private String packageHref;
	
	private static final Logger log = Logger.getLogger(Resource.class.getName());
	
	/**
	 * Creates an empty Resource with the given href.
	 *
	 * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
	 *
	 * @param href The location of the resource within the epub. Example: "chapter1.html".
	 */
	public Resource(String href) {
		this(null, new byte[0], href, MediatypeService.determineMediaType(href));
	}
	
	/**
	 * Creates a Resource with the given data and MediaType.
	 * The href will be automatically generated.
	 *
	 * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
	 *
	 * @param data The Resource's contents
	 * @param mediaType The MediaType of the Resource
	 */
	public Resource(byte[] data, MediaType mediaType) {
		this(null, data, null, mediaType);
	}
	
	/**
	 * Creates a resource with the given data at the specified href.
	 * The MediaType will be determined based on the href extension.
	 *
	 * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
	 *
	 * @see nl.siegmann.epublib.service.MediatypeService.determineMediaType(String)
	 *
	 * @param data The Resource's contents
	 * @param href The location of the resource within the epub. Example: "chapter1.html".
	 */
	public Resource(byte[] data, String href) {
		this(null, data, href, MediatypeService.determineMediaType(href), Constants.ENCODING);
	}
	
	/**
	 * Creates a resource with the given data at the specified href.
	 * The MediaType will be determined based on the href extension.
	 *
	 * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
	 *
	 * @see nl.siegmann.epublib.service.MediatypeService.determineMediaType(String)
	 *
	 * @param data The Resource's contents
	 * @param href The location of the resource within the epub. Example: "chapter1.html".
	 */
	public Resource(byte[] data, byte[] rawHref) {
		this(null, data, new String(rawHref), MediatypeService.determineMediaType(new String(rawHref)), Constants.ENCODING);
		this.rawHref = rawHref;
	}
	
	/**
	 * Creates a resource with the data from the given Reader at the specified href.
	 * The MediaType will be determined based on the href extension.
	 * @see nl.siegmann.epublib.service.MediatypeService.determineMediaType(String)
	 *
	 * @param in The Resource's contents
	 * @param href The location of the resource within the epub. Example: "cover.jpg".
	 */
	public Resource(Reader in, String href) throws IOException {
		this(null, IOUtil.toByteArray(in, Constants.ENCODING), href, MediatypeService.determineMediaType(href), Constants.ENCODING);
	}
	
	/**
	 * Creates a resource with the data from the given InputStream at the specified href.
	 * The MediaType will be determined based on the href extension.
	 * @see nl.siegmann.epublib.service.MediatypeService.determineMediaType(String)
	 *
	 * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
	 *
	 * It is recommended to us the
	 * @see nl.siegmann.epublib.domain.Resource.Resource(Reader, String)
	 * method for creating textual (html/css/etc) resources to prevent encoding problems.
	 * Use this method only for binary Resources like images, fonts, etc.
	 *
	 *
	 * @param in The Resource's contents
	 * @param href The location of the resource within the epub. Example: "cover.jpg".
	 */
	public Resource(InputStream in, String href) throws IOException {
		this(null, (byte[]) null, href, MediatypeService.determineMediaType(href));
		setInputStream(in);
	}
	
	/**
	 * Creates a resource with the data from the given InputStream at the specified href.
	 * The MediaType will be determined based on the href extension.
	 * @see nl.siegmann.epublib.service.MediatypeService.determineMediaType(String)
	 *
	 * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
	 *
	 * It is recommended to us the
	 * @see nl.siegmann.epublib.domain.Resource.Resource(Reader, String)
	 * method for creating textual (html/css/etc) resources to prevent encoding problems.
	 * Use this method only for binary Resources like images, fonts, etc.
	 *
	 *
	 * @param in The Resource's contents
	 * @param href The location of the resource within the epub. Example: "cover.jpg".
	 */
	public Resource(InputStream in, byte[] rawHref) throws IOException {
		this(null, (byte[]) null, new String(rawHref), MediatypeService.determineMediaType(new String(rawHref)));
		setInputStream(in);
		this.rawHref = rawHref;
	}
	
	/**
	 * Creates a Lazy resource, by not actually loading the data for this entry.
	 *
	 * The data will be loaded on the first call to getData()
	 *
	 * @param fileName the fileName for the epub we're created from.
	 * @param size the size of this resource.
	 * @param href The resource's href within the epub.
	 */
	public Resource( String fileName, long size, String href) {
		this( null, null, href, MediatypeService.determineMediaType(href));
		this.fileName = fileName;
		this.cachedSize = size;
	}
	
	/**
	 * Creates a resource with the given id, data, mediatype at the specified href.
	 * Assumes that if the data is of a text type (html/css/etc) then the encoding will be UTF-8
	 *
	 * @param id The id of the Resource. Internal use only. Will be auto-generated if it has a null-value.
	 * @param data The Resource's contents
	 * @param href The location of the resource within the epub. Example: "chapter1.html".
	 * @param mediaType The resources MediaType
	 */
	public Resource(String id, byte[] data, String href, MediaType mediaType) {
		this(id, data, href, mediaType, Constants.ENCODING);
	}
	
	/**
	 * Creates a resource with the given id, data, mediatype at the specified href.
	 * If the data is of a text type (html/css/etc) then it will use the given inputEncoding.
	 *
	 * @param id The id of the Resource. Internal use only. Will be auto-generated if it has a null-value.
	 * @param data The Resource's contents
	 * @param href The location of the resource within the epub. Example: "chapter1.html".
	 * @param mediaType The resources MediaType
	 * @param inputEncoding If the data is of a text type (html/css/etc) then it will use the given inputEncoding.
	 */
	public Resource(String id, byte[] data, String href, MediaType mediaType, String inputEncoding) {
		this.id = id;
		this.href = href;
		this.mediaType = mediaType;
		this.inputEncoding = inputEncoding;
		this.data = data;
	}
	
	private void setInputStream(InputStream in) {
		if(in.markSupported()) {
			//mark the start of the InputStream
			in.mark(Integer.MAX_VALUE);
		}
		this.in = in;
	}
	
	private void resetInputStream() {
		if(in.markSupported()) {
			//go to the begin of the InputStream
			try {
				in.reset();
			} catch (IOException e) {
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "InputStream reset for resource " + this.fileName + " has failed.", e);
			}
		}
	}
	
	/**
	 * Gets the contents of the Resource as an InputStream.
	 *
	 * @return The contents of the Resource.
	 *
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException {
		if(this.in != null) {
			resetInputStream();
			return in;
		}
		return new ByteArrayInputStream(getData());
	}

	/**
	 * The contents of the resource as a byte[]
	 *
	 * If this resource was lazy-loaded and the data was not yet loaded,
	 * it will be loaded into memory at this point.
	 *  This included opening the zip file, so expect a first load to be slow.
	 *
	 * @return The contents of the resource
	 */
	public byte[] getData() throws IOException {
		
		if ( data == null ) {
			if( in != null ) {
				resetInputStream();
				data = IOUtil.toByteArray(in);
				in.close();
				in = null;
			} else {
				log.info("Initializing lazy resource " + fileName + "#" + this.href );
				
				ZipInputStream zipIn = new ZipInputStream(new FileInputStream(this.fileName));
				
				for(ZipEntry zipEntry = zipIn.getNextEntry(); zipEntry != null; zipEntry = zipIn.getNextEntry()) {
					if(zipEntry.isDirectory()) {
						continue;
					}
					
					if ( zipEntry.getName().endsWith(this.href)) {
						this.data = IOUtil.toByteArray(zipIn);
					}
				}
				
				zipIn.close();
			}
		}
		
		return data;
	}
	
	/**
	 * Tells this resource to release its cached data.
	 *
	 * If this resource was not lazy-loaded, this is a no-op.
	 */
	public void close() {
		if ( this.fileName != null ) {
			this.data = null;
		}
	}

	/**
	 * Sets the data of the Resource.
	 * If the data is a of a different type then the original data then make sure to change the MediaType.
	 *
	 * @param data
	 */
	public void setData(byte[] data) {
		this.data = data;
		this.in = null;
	}
	
	/**
	 * Returns if the data for this resource has been loaded into memory.
	 *
	 * @return true if data was loaded.
	 */
	public boolean isInitialized() {
		return data != null;
	}

	/**
	 * Returns the size of this resource in bytes.
	 *
	 * @return the size.
	 */
	public long getSize() {
		if ( data != null ) {
			return data.length;
		}
		
		return cachedSize;
	}
	
	/**
	 * If the title is found by scanning the underlying html document then it is cached here.
	 *
	 * @return
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Sets the Resource's id: Make sure it is unique and a valid identifier.
	 *
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * The resources Id.
	 *
	 * Must be both unique within all the resources of this book and a valid identifier.
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * The location of the resource within the contents folder of the epub file.
	 *
	 * Example:<br/>
	 * images/cover.jpg<br/>
	 * content/chapter1.xhtml<br/>
	 *
	 * @return
	 */
	public String getHref() {
		return fixHref();
	}

	/**
	 * Sets the Resource's href.
	 *
	 * @param href
	 */
	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * The character encoding of the resource.
	 * Is allowed to be null for non-text resources like images.
	 *
	 * @return
	 */
	public String getInputEncoding() {
		return inputEncoding;
	}
	
	/**
	 * Sets the Resource's input character encoding.
	 *
	 * @param encoding
	 */
	public void setInputEncoding(String encoding) {
		this.inputEncoding = encoding;
	}
	
	/**
	 * Gets the contents of the Resource as Reader.
	 *
	 * Does all sorts of smart things (courtesy of apache commons io XMLStreamREader) to handle encodings, byte order markers, etc.
	 *
	 * @param resource
	 * @return
	 * @throws IOException
	 */
	public Reader getReader() throws IOException {
		return new XmlStreamReader(new ByteArrayInputStream(getData()), inputEncoding);
	}
	
	/**
	 * Gets the hashCode of the Resource's href.
	 *
	 */
	public int hashCode() {
		return href.hashCode();
	}
	
	/**
	 * Checks to see of the given resourceObject is a resource and whether its href is equal to this one.
	 *
	 */
	public boolean equals(Object resourceObject) {
		if (! (resourceObject instanceof Resource)) {
			return false;
		}
		return href.equals(((Resource) resourceObject).getHref());
	}
	
	/**
	 * This resource's mediaType.
	 *
	 * @return
	 */
	public MediaType getMediaType() {
		return mediaType;
	}
	
	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String toString() {
		return StringUtil.toString("id", id,
				"title", title,
				"encoding", inputEncoding,
				"mediaType", mediaType,
				"href", href,
				"size", (data == null ? 0 : data.length));
	}

	public String getPackageHref() {
		return packageHref;
	}

	public void setPackageHref(String packageHref) {
		this.packageHref = packageHref;
	}
	
	/**
	 * Strips off the package prefixes up to the href of the packageHref.
	 *
	 * Example:
	 * If the packageHref is "OEBPS/content.opf" then a resource href like "OEBPS/foo/bar.html" will be turned into "foo/bar.html"
	 *
	 * @param packageHref
	 * @param resourcesByHref
	 * @return
	 */
	private String fixHref() {
		int lastSlashPos = packageHref != null ? packageHref.lastIndexOf('/') : -1;
		if (lastSlashPos < 0) {
			return this.href;
		}
		String packagePath = packageHref.substring(0, lastSlashPos + 1);
		if (StringUtil.isNotEmpty(this.href) || this.href.length() > lastSlashPos) {
			if (this.href.startsWith(packagePath)) {
				// fix only entries within the given packageHref. The entry could be
				// destroyed in the case that the ref is not in the given package.
				return this.href.substring(lastSlashPos + 1);
			}
		}
		return this.href;

	}

	public byte[] getRawHref() {
		return this.rawHref;
	}
}
