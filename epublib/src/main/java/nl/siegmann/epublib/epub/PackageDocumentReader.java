package nl.siegmann.epublib.epub;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Guide;
import nl.siegmann.epublib.domain.GuideReference;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Resources;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.service.MediatypeService;
import nl.siegmann.epublib.util.ResourceUtil;

/**
 * Reads the opf package document as defined by namespace http://www.idpf.org/2007/opf
 *
 * @author paul
 *
 */
public class PackageDocumentReader extends PackageDocumentBase {
	
	private static final Logger log = java.util.logging.Logger.getLogger(PackageDocumentReader.class.getName());
	private static final String[] POSSIBLE_NCX_ITEM_IDS = new String[] {"toc", "ncx"};
	
	
	public static void read(Resource packageResource, EpubReader epubReader, Book book, Resources resources) throws UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException {
		Document packageDocument = ResourceUtil.getAsDocument(packageResource);
		String packageHref = packageResource.getHref();
		resources = setPackageResources(resources, packageHref);
//		resources = fixHrefs(packageHref, resources);
		readGuide(packageDocument, epubReader, book, resources);
		
		// Books sometimes use non-identifier ids. We map these here to legal ones
		Map<String, String> idMapping = new HashMap<String, String>();
		
		resources = readManifest(book, packageDocument, packageHref, epubReader, resources, idMapping);
		book.setResources(resources);
		readCover(packageDocument, book);
		book.setMetadata(PackageDocumentMetadataReader.readMetadata(packageDocument, book.getResources()));
		book.setSpine(readSpine(packageDocument, epubReader, book.getResources(), idMapping));
		
		// if we did not find a cover page then we make the first page of the book the cover page
		if (book.getCoverPage() == null && book.getSpine().size() > 0) {
			book.setCoverPage(book.getSpine().getResource(0));
		}
	}
	
	private static Resources setPackageResources(Resources resources, String packageHref) {
		Resources result = new Resources();
		Collection<Resource> all = resources.getAll();
		for(Resource resource : all) {
			resource.setPackageHref(packageHref);
			result.add(resource);
		}
		return result;
	}
	
	/**
	 * Reads the manifest containing the resource ids, hrefs and mediatypes.
	 *
	 * @param packageDocument
	 * @param packageHref
	 * @param epubReader
	 * @param book
	 * @param resourcesByHref
	 * @return a Map with resources, with their id's as key.
	 */
	private static Resources readManifest(Book book, Document packageDocument, String packageHref,
			EpubReader epubReader, Resources resources, Map<String, String> idMapping) {
		Element manifestElement = DOMUtil.getFirstElementByTagNameNS(packageDocument.getDocumentElement(), NAMESPACE_OPF, OPFTags.manifest);
		Resources result = new Resources();
		if(manifestElement == null) {
			log.warning("Package document does not contain element " + OPFTags.manifest);
			return result;
		}
		NodeList itemElements = manifestElement.getElementsByTagNameNS(NAMESPACE_OPF, OPFTags.item);
		for(int i = 0; i < itemElements.getLength(); i++) {
			Element itemElement = (Element) itemElements.item(i);
			String id = DOMUtil.getAttribute(itemElement, NAMESPACE_OPF, OPFAttributes.id);
			String href = DOMUtil.getAttribute(itemElement, NAMESPACE_OPF, OPFAttributes.href);
			try {
				href = URLDecoder.decode(href, Constants.ENCODING);
			} catch (UnsupportedEncodingException e) {
				log.warning(e.getMessage());
			}
			String mediaTypeName = DOMUtil.getAttribute(itemElement, NAMESPACE_OPF, OPFAttributes.media_type);
			Resource resource = resources.remove(href);
			if(resource == null) {
				//Possibly any charset could be used to store file names in zip files. Try all available ones
				//to find the referring resource.
				resource = findHrefResource(href, resources);
				if(resource != null) {
					resources.remove(resource.getHref());
					resource.setHref(href);
				}

				if(resource == null) {
					log.warning("resource with href '" + href + "' not found in " + book.getName());
					continue;
				}
			}
			resource.setId(id);
			MediaType mediaType = MediatypeService.getMediaTypeByName(mediaTypeName);
			if(mediaType != null) {
				resource.setMediaType(mediaType);
			}
			result.add(resource);
			idMapping.put(id, resource.getId());
		}
		return result;
	}

	private static Resource findHrefResource(String href, final Resources resources) {
		//Possibly any charset could be used to store file names in zip files.
		try {
			final boolean urlEncoded = href.indexOf('%') != -1;
			final SortedMap<String, Charset> availableCharsets = Charset.availableCharsets();
			final String compareHref = urlEncoded ? URLDecoder.decode(href, StandardCharsets.UTF_8.name()) : href;
			final Collection<Resource> allResources = resources.getAll();
			
			//try charset encodings for wrong encoded content.opf
			for(Charset c : availableCharsets.values()) {
				String newEncodedHref = new String(href.getBytes(), c);
				if(resources.containsByHref(newEncodedHref)) {
					return resources.getByHref(newEncodedHref);
				} else if(urlEncoded) {
					try {
						String urlDecodedHref = URLDecoder.decode(href, c.name());
						if(resources.containsByHref(urlDecodedHref)) {
							return resources.getByHref(urlDecodedHref);
						}
					} catch (UnsupportedEncodingException e) {
					}
				}
			}
			
			//try charset encodings for zip file entries.
			for(Resource resource : allResources) {
				String oldHref = resource.getHref();
				for(Charset c : availableCharsets.values()) {
					byte[] rawHref = resource.getRawHref();
					if(rawHref != null) {
						String newEncodedResourceHref = new String(rawHref, c);
						resource.setHref(newEncodedResourceHref);
						if(resource.getHref().equals(compareHref)) {
							return resource;
						}
					}
				}
				resource.setHref(oldHref);
			}
		} catch (UnsupportedEncodingException e) {
			LoggerFactory.getLogger().log(Level.WARNING, "Failed to encode raw for " + href, e);
		}
		
		return null;
	}
	
	
	/**
	 * Reads the book's guide.
	 * Here some more attempts are made at finding the cover page.
	 *
	 * @param packageDocument
	 * @param epubReader
	 * @param book
	 * @param resources
	 */
	private static void readGuide(Document packageDocument,
			EpubReader epubReader, Book book, Resources resources) {
		Element guideElement = DOMUtil.getFirstElementByTagNameNS(packageDocument.getDocumentElement(), NAMESPACE_OPF, OPFTags.guide);
		if(guideElement == null) {
			return;
		}
		Guide guide = book.getGuide();
		NodeList guideReferences = guideElement.getElementsByTagNameNS(NAMESPACE_OPF, OPFTags.reference);
		for (int i = 0; i < guideReferences.getLength(); i++) {
			Element referenceElement = (Element) guideReferences.item(i);
			String resourceHref = DOMUtil.getAttribute(referenceElement, NAMESPACE_OPF, OPFAttributes.href);
			if (StringUtil.isEmpty(resourceHref)) {
				continue;
			}
			String guideHref = StringUtil.substringBefore(resourceHref, Constants.FRAGMENT_SEPARATOR_CHAR);
			Resource resource = resources.getByHref(guideHref);
			if (resource == null) {
				resource = findHrefResource(guideHref, resources);
				if(resource != null) {
					resource.setHref(guideHref);
				} else {
					log.warning("Guide is referencing resource with href " + resourceHref + " which could not be found");
					continue;
				}

			}
			String type = DOMUtil.getAttribute(referenceElement, NAMESPACE_OPF, OPFAttributes.type);
			if (StringUtil.isEmpty(type)) {
				log.warning("Guide is referencing resource with href " + resourceHref + " which is missing the 'type' attribute");
				continue;
			}
			String title = DOMUtil.getAttribute(referenceElement, NAMESPACE_OPF, OPFAttributes.title);
			if (GuideReference.COVER.equalsIgnoreCase(type)) {
				continue; // cover is handled elsewhere
			}
			GuideReference reference = new GuideReference(resource, type, title, StringUtil.substringAfter(resourceHref, Constants.FRAGMENT_SEPARATOR_CHAR));
			guide.addReference(reference);
		}
	}


	/**
	 * Reads the document's spine, containing all sections in reading order.
	 *
	 * @param packageDocument
	 * @param epubReader
	 * @param book
	 * @param resourcesById
	 * @return
	 */
	private static Spine readSpine(Document packageDocument, EpubReader epubReader, Resources resources, Map<String, String> idMapping) {
		
		Element spineElement = DOMUtil.getFirstElementByTagNameNS(packageDocument.getDocumentElement(), NAMESPACE_OPF, OPFTags.spine);
		if (spineElement == null) {
			log.warning("Element " + OPFTags.spine + " not found in package document, generating one automatically");
			return generateSpineFromResources(resources);
		}
		Spine result = new Spine();
		result.setTocResource(findTableOfContentsResource(spineElement, resources));
		NodeList spineNodes = packageDocument.getElementsByTagNameNS(NAMESPACE_OPF, OPFTags.itemref);
		List<SpineReference> spineReferences = new ArrayList<>(spineNodes.getLength());
		for(int i = 0; i < spineNodes.getLength(); i++) {
			Element spineItem = (Element) spineNodes.item(i);
			String itemref = DOMUtil.getAttribute(spineItem, NAMESPACE_OPF, OPFAttributes.idref);
			if(StringUtil.isEmpty(itemref)) {
				log.warning("itemref with missing or empty idref"); // XXX
				continue;
			}
			String id = idMapping.get(itemref);
			if (id == null) {
				id = itemref;
			}
			Resource resource = resources.getByIdOrHref(id);
			if(resource == null) {
				log.warning("resource with id \'" + id + "\' not found");
				continue;
			}
			
			SpineReference spineReference = new SpineReference(resource);
			if (OPFValues.no.equalsIgnoreCase(DOMUtil.getAttribute(spineItem, NAMESPACE_OPF, OPFAttributes.linear))) {
				spineReference.setLinear(false);
			}
			spineReferences.add(spineReference);
		}
		result.setSpineReferences(spineReferences);
		return result;
	}

	/**
	 * Creates a spine out of all resources in the resources.
	 * The generated spine consists of all XHTML pages in order of their href.
	 *
	 * @param resources
	 * @return
	 */
	private static Spine generateSpineFromResources(Resources resources) {
		Spine result = new Spine();
		List<String> resourceHrefs = new ArrayList<>();
		resourceHrefs.addAll(resources.getAllHrefs());
		Collections.sort(resourceHrefs, String.CASE_INSENSITIVE_ORDER);
		for (String resourceHref: resourceHrefs) {
			Resource resource = resources.getByHref(resourceHref);
			if (resource.getMediaType() == MediatypeService.NCX) {
				result.setTocResource(resource);
			} else if (resource.getMediaType() == MediatypeService.XHTML) {
				result.addSpineReference(new SpineReference(resource));
			}
		}
		return result;
	}

	
	/**
	 * The spine tag should contain a 'toc' attribute with as value the resource id of the table of contents resource.
	 *
	 * Here we try several ways of finding this table of contents resource.
	 * We try the given attribute value, some often-used ones and finally look through all resources for the first resource with the table of contents mimetype.
	 *
	 * @param spineElement
	 * @param resourcesById
	 * @return
	 */
	private static Resource findTableOfContentsResource(Element spineElement, Resources resources) {
		String tocResourceId = DOMUtil.getAttribute(spineElement, NAMESPACE_OPF, OPFAttributes.toc);
		Resource tocResource = null;
		if (StringUtil.isNotEmpty(tocResourceId)) {
			tocResource = resources.getByIdOrHref(tocResourceId);
		}
		
		if (tocResource != null) {
			return tocResource;
		}
		
		for (int i = 0; i < POSSIBLE_NCX_ITEM_IDS.length; i++) {
			tocResource = resources.getByIdOrHref(POSSIBLE_NCX_ITEM_IDS[i]);
			if (tocResource != null) {
				return tocResource;
			}
			tocResource = resources.getByIdOrHref(POSSIBLE_NCX_ITEM_IDS[i].toUpperCase());
			if (tocResource != null) {
				return tocResource;
			}
		}
		
		// get the first resource with the NCX mediatype
		tocResource = resources.findFirstResourceByMediaType(MediatypeService.NCX);

		if (tocResource == null) {
			log.warning("Could not find table of contents resource. Tried resource with id '" + tocResourceId + "', " + Constants.DEFAULT_TOC_ID + ", " + Constants.DEFAULT_TOC_ID.toUpperCase() + " and any NCX resource.");
		}
		return tocResource;
	}


	/**
	 * Find all resources that have something to do with the coverpage and the cover image.
	 * Search the meta tags and the guide references
	 *
	 * @param packageDocument
	 * @return
	 */
	// package
	static Set<String> findCoverHrefs(Document packageDocument) {
		
		Set<String> result = new HashSet<>();
		
		// try and find a meta tag with name = 'cover' and a non-blank id
		String coverResourceId = DOMUtil.getFindAttributeValue(packageDocument, NAMESPACE_OPF,
											OPFTags.meta, OPFAttributes.name, OPFValues.meta_cover,
											OPFAttributes.content);

		if (StringUtil.isNotEmpty(coverResourceId)) {
			String coverHref = DOMUtil.getFindAttributeValue(packageDocument, NAMESPACE_OPF,
					OPFTags.item, OPFAttributes.id, coverResourceId,
					OPFAttributes.href);
			if (StringUtil.isNotEmpty(coverHref)) {
				result.add(coverHref);
			} else {
				result.add(coverResourceId); // maybe there was a cover href put in the cover id attribute
			}
		}
		// try and find a reference tag with type is 'cover' and reference is not blank
		String coverHref = DOMUtil.getFindAttributeValue(packageDocument, NAMESPACE_OPF,
											OPFTags.reference, OPFAttributes.type, OPFValues.reference_cover,
											OPFAttributes.href);
		if (StringUtil.isNotEmpty(coverHref)) {
			result.add(coverHref);
		}
		return result;
	}

	/**
	 * Finds the cover resource in the packageDocument and adds it to the book if found.
	 * Keeps the cover resource in the resources map
	 * @param packageDocument
	 * @param book
	 * @param resources
	 * @return
	 */
	private static void readCover(Document packageDocument, Book book) {
		Collection<String> coverHrefs = findCoverHrefs(packageDocument);
		for (String coverHref: coverHrefs) {
			Resources resources = book.getResources();
			Resource resource = resources.getByHref(coverHref);
			if (resource == null) {
				resource = findHrefResource(coverHref, resources);
				if(resource != null) {
					resource.setHref(coverHref);
				}
				log.warning("Cover resource " + coverHref + " not found in '" + book.getName() + "'");
				continue;
			}
			if (resource.getMediaType() == MediatypeService.XHTML) {
				book.setCoverPage(resource);
			} else if (MediatypeService.isBitmapImage(resource.getMediaType())) {
				book.setCoverImage(resource);
			}
		}
	}
	

}