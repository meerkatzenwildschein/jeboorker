package org.rr.jeborker.metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.io.IOUtils;
import org.apache.jempbox.xmp.XMPUtils;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.CommonUtils;
import org.xml.sax.SAXException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfStream;

abstract class PDFCommonDocument {

	public static final int ITEXT = 0;
	
	public static final int PDFBOX = 1;
	
	private static final double MIN_IMAGE_COVER_WIDTH = 1.4d;
	
	private static final double MAX_IMAGE_COVER_WIDTH = 1.7d;
	
	private IResourceHandler pdfFile;
	
	/**
	 * Store sthe PDF key/value info. If it's null, the Info in the
	 * pdf file didn't get touched.
	 */
	protected Map<String, String> moreInfo;
	
	protected byte[] xmpMetadata;
	
	public static PDFCommonDocument getInstance(int type, IResourceHandler pdfFile) {
		PDFCommonDocument result = null;
		switch(type) {
		case ITEXT:
			result = new PDFCommonDocument.ItextPDFDocument(pdfFile);
			break;
		case PDFBOX:
			result = new PDFCommonDocument.PDFBoxPDFDocument(pdfFile);
			break;			
		}
		result.setResourceHandler(pdfFile);
		return result;
	}
	
	/**
	 * Set the pdf file containing the pdf data used for the {@link PDFCommonDocument} instance.
	 * @param pdfFile The pdf file to be used.
	 */
	protected void setResourceHandler(IResourceHandler pdfFile)  {
		this.pdfFile = pdfFile;
	}
	
	/**
	 * get the pdf file for this {@link PDFCommonDocument} instance.
	 * @return The desired {@link PDFCommonDocument}.
	 */
	public IResourceHandler getResourceHandler() {
		return this.pdfFile;
	}
	
	/**
	 * Read the xmp metadata as byte array.
	 * @return The desired xmp bytes.
	 */
	public abstract byte[] getXMPMetadata() throws IOException;
	
	/**
	 * Get a map with values contained in the pdf info block.
	 * @return The desired info values.
	 */
	public abstract Map<String, String> getInfo() throws IOException;
	
	/**
	 * Set the map values for the pdf. 
	 * @param info The metadata key/values to be written to the pdf.
	 */
	public void setInfo(Map<String, String> info) {
		this.moreInfo = info;
	}
	
	public void setXMPMetadata(byte[] xmpMetadata) {
		this.xmpMetadata = xmpMetadata;
	}
	
	public abstract byte[] fetchCoverFromPDFContent() throws IOException;
	
	/**
	 * Write the previously set metadata to the file.
	 * @throws IOException
	 */
	public abstract void write() throws IOException;
	
	/**
	 * Dispose this {@link PDFCommonDocument} instance. It could no longer be used
	 * after invoking dispose.
	 */
	public abstract void dispose();
	
	
	private static class ItextPDFDocument extends PDFCommonDocument {

		private PdfReader pdfReader;
		
		ItextPDFDocument(IResourceHandler pdfFile) {
			System.gc();
			InputStream pdfInputStream = null;
			try {
				pdfInputStream = pdfFile.getContentInputStream();
				this.pdfReader = new PdfReader(pdfInputStream);
			} catch(Exception e) {
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(pdfInputStream);
			}				
		}
		
		@Override
		public byte[] getXMPMetadata() throws IOException {
			if(this.xmpMetadata == null) {
				final byte[] xmpMetadataBytes = pdfReader.getMetadata();
				if(XMPUtils.isValidXMP(xmpMetadataBytes)) {
					this.xmpMetadata = xmpMetadataBytes;
				}
			}
			return this.xmpMetadata;
		}

		@Override
		public Map<String, String> getInfo() throws IOException {
			if(moreInfo == null) {
				moreInfo = pdfReader.getInfo();
				return moreInfo;
			}
			return moreInfo;
		}

		@Override
		public void write() throws IOException {
			final IResourceHandler ebookResource = getResourceHandler();
			final IResourceHandler tmpEbookResourceLoader = ResourceHandlerFactory.getTemporaryResourceLoader(ebookResource, "tmp");
			PdfStamper stamper = null;
			OutputStream ebookResourceOutputStream = null;
			
			try {
				ebookResourceOutputStream = tmpEbookResourceLoader.getContentOutputStream(false);
				stamper = new PdfStamper(pdfReader, ebookResourceOutputStream);
				byte[] xmp = this.xmpMetadata != null ? this.xmpMetadata : getXMPMetadata();
				stamper.setXmpMetadata(XMPUtils.handleMissingXMPRootTag(xmp));
				Map<String, String> info = this.moreInfo != null ? this.moreInfo : getInfo();
				if(this.moreInfo != null) {
					//to delete old entries, itext need to null them.
					HashMap<String, String> oldInfo = pdfReader.getInfo();
					HashMap<String, String> newInfo = new HashMap<String, String>(oldInfo.size() + this.moreInfo.size());
					for (Iterator<String> it = oldInfo.keySet().iterator(); it.hasNext();) {
						newInfo.put(it.next(), null); 
					}
					newInfo.putAll(info);
					
					stamper.setMoreInfo(newInfo);
				}
			} catch(Exception e) {
				throw new IOException(e);
			} finally {
				if (stamper != null) {
					try {
						stamper.close();
					} catch (DocumentException e) {
						LoggerFactory.logWarning(this, "Could not close pdf stamper for " + ebookResource, e);
					} catch (IOException e) {
						LoggerFactory.logWarning(this, "Could not close pdf stamper for " + ebookResource, e);
					}
				}
				if (ebookResourceOutputStream != null) {
					try {
						ebookResourceOutputStream.flush();
					} catch (IOException e) {
					}
					IOUtils.closeQuietly(ebookResourceOutputStream);
				}
				if(tmpEbookResourceLoader.size() > 0) {
					//new temp pdf looks good. Move the new temp one over the old one. 
					tmpEbookResourceLoader.moveTo(ebookResource, true);
				} else {
					tmpEbookResourceLoader.delete();
				}
			}			
		}

		@Override
		public void dispose() {
			if(this.pdfReader != null) {
				this.pdfReader.close();
			}
		}

		@Override
		public byte[] fetchCoverFromPDFContent() throws IOException {
			for (int i = 0; i < pdfReader.getXrefSize(); i++) {
				PdfObject pdfobj = pdfReader.getPdfObject(i);
				if(pdfobj != null) {
					if (pdfobj.isStream()) {
						PdfStream stream = (PdfStream) pdfobj;
						
						PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);
						if (pdfsubtype == null) {
							//throw new Exception("Not an image stream");
							continue;
						}
						if (!pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
							//throw new Exception("Not an image stream");
							continue;
						}
		
						// now you have a PDF stream object with an image
						byte[] img = PdfReader.getStreamBytesRaw((PRStream) stream);		
						if(img.length > 1000) {
							int width = 0;
							int height = 0;
							try {
								width = Integer.parseInt(stream.get(PdfName.WIDTH).toString());
								height = Integer.parseInt(stream.get(PdfName.HEIGHT).toString());
								
								if(width<=0 || height<=0) {
									continue;
								}
								
								PdfObject bitspercomponent = stream.get(PdfName.BITSPERCOMPONENT);
								if(bitspercomponent!=null) {
									Number bitspercomponentNum = CommonUtils.toNumber(bitspercomponent.toString());
									if(bitspercomponentNum!=null && bitspercomponentNum.intValue()==1) {
										//no b/w images
										continue;
									}
								}							
							} catch(Exception e) {}
							
							double aspectRatio = ((double)height) / ((double)width);
							if(width > 150 && aspectRatio > MIN_IMAGE_COVER_WIDTH && aspectRatio < MAX_IMAGE_COVER_WIDTH) {
								return img;
							}
						}
					}
				}
			}
			return null;
		}
		
	}
	
	private static class PDFBoxPDFDocument extends PDFCommonDocument {
		
		private static final List<String> DCT_FILTERS = new ArrayList<String>() {{
			add( COSName.DCT_DECODE.getName() );
			add( COSName.DCT_DECODE_ABBREVIATION.getName() );			
		}};			
		
		private PDDocument doc;
		
		PDFBoxPDFDocument(IResourceHandler pdfFile) {
			System.gc();
			InputStream pdfInputStream = null;
			try {
				pdfInputStream = pdfFile.getContentInputStream();
				this.doc = PDDocument.load(pdfInputStream);
			} catch(Exception e) {
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(pdfInputStream);
			}			
		}
		
		/**
		 * Get the xmp metadata bytes from the given doc.
		 * @param doc The doc which contains the desired metadata.
		 * @return The metadata bytes or <code>null</code> if there're no metadata to return.
		 * @throws IOException
		 */		
		@Override
		public byte[] getXMPMetadata() throws IOException {
			if(this.xmpMetadata == null) {
				final PDDocumentCatalog catalog = doc.getDocumentCatalog();
				final PDMetadata metadata = catalog.getMetadata();
	
				// to read the XML metadata
				if(metadata != null) {
					final InputStream xmlInputStream = metadata.createInputStream();
					try {
						byte[] xmpMetadataBytes = IOUtils.toByteArray(xmlInputStream);
						if(XMPUtils.isValidXMP(xmpMetadataBytes)) {
							this.xmpMetadata = xmpMetadataBytes;
						}
					} finally {
						IOUtils.closeQuietly(xmlInputStream);
					}
				}
			}
			return this.xmpMetadata;
		}
		
		private PDDocumentInformation convertInfo(Map<String, String> moreInfo) {
			if(moreInfo != null) {
				final PDDocumentInformation pdfInfo = new PDDocumentInformation();
				for (Entry<String, String> entry : moreInfo.entrySet()) {
					final String key = entry.getKey();
					final String value = entry.getValue();
					
					pdfInfo.setCustomMetadataValue(key, value);
				}
				return pdfInfo;
			}		
			return null;
		}

		@Override
		public Map<String, String> getInfo() throws IOException {
			if(moreInfo == null) {
				moreInfo = new HashMap<String, String>();
				final PDDocumentInformation pdfInfo = doc.getDocumentInformation();
				if(pdfInfo != null) {
					final Set<String> metadataKeys = pdfInfo.getMetadataKeys();
					for (String key : metadataKeys) {					
						final String value = pdfInfo.getCustomMetadataValue(key);
						moreInfo.put(key, value);
					}				
				}
			}
				
			return moreInfo;
		}

		@Override
		public void write() throws IOException {
			final IResourceHandler ebookResource = getResourceHandler();
			final IResourceHandler tmpEbookResourceLoader = ResourceHandlerFactory.getTemporaryResourceLoader(ebookResource, "tmp");
			OutputStream ebookResourceOutputStream = null;
			
			try {
				ebookResourceOutputStream = tmpEbookResourceLoader.getContentOutputStream(false);
				byte[] xmp = this.xmpMetadata;
				if(XMPUtils.isValidXMP(xmp)) {
					byte[] handledXMP = XMPUtils.handleMissingXMPRootTag(xmp);
				    PDMetadata newMetadata = new PDMetadata(doc, new ByteArrayInputStream(handledXMP), false);
				    PDDocumentCatalog catalog = doc.getDocumentCatalog();
				    catalog.setMetadata( newMetadata );
				}
				
				final PDDocumentInformation pdInfo = convertInfo(this.moreInfo);		
				if(this.moreInfo != null) {
					doc.setDocumentInformation(pdInfo);
				}
		        
				doc.save(ebookResourceOutputStream);
			} catch (COSVisitorException e) {
				throw new IOException(e);
			} catch (ParserConfigurationException e) {
				throw new IOException(e);
			} catch (SAXException e) {
				throw new IOException(e);
			} catch (TransformerFactoryConfigurationError e) {
				throw new IOException(e);
			} catch (TransformerException e) {
				throw new IOException(e);
			} finally {
				if (ebookResourceOutputStream != null) {
					try {
						ebookResourceOutputStream.flush();
					} catch (IOException e) {
					}
					IOUtils.closeQuietly(ebookResourceOutputStream);
				}
				if(tmpEbookResourceLoader.size() > 0) {
					//new temp pdf looks good. Move the new temp one over the old one. 
					tmpEbookResourceLoader.moveTo(ebookResource, true);
				} else {
					tmpEbookResourceLoader.delete();
				}
			}			
		}	

		@Override
		public void dispose() {
			if(this.doc != null) {
				try {this.doc.close();} catch(Exception e) {}
				this.doc = null;
			}
		}

		@Override
		/**
		 * Tries to extract the cover by looking for the embedded images of the pdf. The first
		 * image which seems to be a cover will be returned.
		 *  
		 * @param pdfReader The reader for accessing the pdf content.
		 * @return The desired image or <code>null</code> if there is no image found.
		 * @throws IOException
		 */
		public byte[] fetchCoverFromPDFContent() throws IOException {
			@SuppressWarnings("unchecked")
			List<PDPage> pages = doc.getDocumentCatalog().getAllPages();
		    Iterator<PDPage> pagesIter = pages.iterator(); 

		    for(int i = 1; pagesIter.hasNext(); i++) {
				final PDPage page = pagesIter.next();
				final PDResources resources = page.getResources();
				Map<String, PDXObjectImage> pageImages = resources.getImages();
				if (pageImages != null) {
					Iterator<String> imageIter = pageImages.keySet().iterator();
					while (imageIter.hasNext()) {
						String key = imageIter.next();
						PDXObjectImage image = (PDXObjectImage) pageImages.get(key);

						int width = image.getWidth();
						int height = image.getHeight();
						double aspectRatio = ((double) height) / ((double) width);
						boolean take = false;
						if(i == 1 && height > 500) {
							take = true;
						} else if(width > 150 && aspectRatio > MIN_IMAGE_COVER_WIDTH && aspectRatio < MAX_IMAGE_COVER_WIDTH) {
							take = true;
						}
						
						if (width > 150 && take) {
							if (image.getBitsPerComponent() != 1) { // no b/w images
								InputStream partiallyFilteredStream = image.getPDStream().getPartiallyFilteredStream( DCT_FILTERS );
								byte[] byteArray = IOUtils.toByteArray(partiallyFilteredStream);
								if(byteArray != null) {
									return byteArray;
								}
							}
						}
					}
				}
				
				if(i > 5) {
					//check first five pages only
					return null;
				}
			}
			return null;
		}
	}	
	
}
