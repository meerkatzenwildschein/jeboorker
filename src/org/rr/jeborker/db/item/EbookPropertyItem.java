package org.rr.jeborker.db.item;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.CRC32;

import javax.persistence.Transient;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.db.IDBObject;

public class EbookPropertyItem implements IDBObject, Serializable {
	
	private static final long serialVersionUID = -4301328577306625467L;
	
	@ViewField(name = "Created at", orderPriority = 0)
	@ProtectedField
	private Date createdAt;

	/**
	 * File name and path from the ebook file.
	 */
	@ViewField(name = "file name", orderPriority = 0)
	@ProtectedField
	@Index(type= "FULLTEXT")
	private String file;
	
	/**
	 * The base path of the ebook file.
	 */
	@ViewField(name = "Base Path", orderPriority = 0)
	@ProtectedField
	private String basePath;
	
	/**
	 * The mime type of the ebook file.
	 */
	@ViewField(name = "Mime type", orderPriority = 0)
	@ProtectedField
	@Index(type= "FULLTEXT")
	private String mimeType;
	
	/**
	 * Title of the ebook. This property is ready from the ebook meta data.
	 */
	@ViewField(name = "Title", orderPriority = 99)
	@Index(type= "FULLTEXT")
	private String title;
	
	/**
	 * Language of the ebook
	 */
	@ViewField(name = "Language", orderPriority = 30)
	@Index(type= "FULLTEXT")
	private String language;
	
	/**
	 * The publishing / release date of the ebook.
	 */
	@ViewField(name = "Publishing Date", orderPriority = 50)
	private Date publishingDate;
	
	/**
	 * The date when the ebook was created.
	 */
	@ViewField(name = "Creation Date", orderPriority = 50)
	private Date creationDate;	

	/**
	 * The author of the ebook.
	 */
	@ViewField(name = "Author", orderPriority = 101)
	@Index(type= "FULLTEXT")
	private String author;
	
	/**
	 * The author's name in a good sortable manner
	 */
	@ViewField(name = "Author Sort", orderPriority = 100)
	@Index(type= "FULLTEXT")
	private String authorSort;	

	/**
	 * Epub Identifier. This is a UUID value
	 */
	private String uuid;
	
	/**
	 * ISBN number of the ebook
	 */
	@ViewField(name = "ISBN", orderPriority = 50)
	@Index(type= "FULLTEXT")
	private String isbn;
	
	/**
	 * Description / summary of the book.
	 */
	@ViewField(name = "Description", orderPriority = 20)
	@Index(type= "FULLTEXT")
	private String description;
	
	/**
	 * Just some keywords for the book. Primary used with pdf.
	 */
	@ViewField(name = "Keywords", orderPriority = 0)
	@Index(type= "FULLTEXT")
	private List<String> keywords;
	
	/**
	 * publisher of the ebook.
	 */
	@ViewField(name = "Publisher", orderPriority = 80)
	@Index(type= "FULLTEXT")
	private String publisher;
	
	/**
	 * The subject is for example "Belletristik/Krimis, Thriller, Spionage"
	 */
	@ViewField(name = "Genre", orderPriority = 90)
	@Index(type= "FULLTEXT")
	private String genre;
	
	/**
	 * If the ebook is part of a series like a trilogy, the name of the serie could be stored here.
	 */
	@ViewField(name = "Series name", orderPriority = 90)
	@Index(type= "FULLTEXT")
	private String seriesName;
	
	/**
	 * If the ebook is part of a series like a trilogy, the number of the serie could be stored here.
	 */
	@ViewField(name = "Series index", orderPriority = 89)
	@Index(type= "FULLTEXT")
	private String seriesIndex;
	
	/**
	 * The book rating. We use a 0.00 digit schema here. 
	 */
	@ViewField(name = "Rating", orderPriority = 95)
	private Integer rating;
	
	/**
	 * Something like "All rights reserved" 
	 */
	@ViewField(name = "Rights", orderPriority = 10)
	@Index(type= "FULLTEXT")
	private String rights;
	
	/**
	 * The release scope for the book. For example "Germany"
	 */
	@ViewField(name = "Coverage", orderPriority = 10)
	@Index(type= "FULLTEXT")
	private String coverage;
	
	/**
	 * age suggestion. Something like '12-13' or simple '12'.
	 */
	@ViewField(name = "Age suggestion", orderPriority = 80)
	@Index(type= "FULLTEXT")
	private String ageSuggestion;
	
	/**
	 * A small thumbnail of the cover.
	 */
	@Transient
	private byte[] coverThumbnail;
	
	public EbookPropertyItem() {
		super();
	}
	
	@Override
	public String toString() {
		return file;
	}
	
	/**
	 * Creates an {@link IResourceHandler} for the ebook file. This is just 
	 * a convenience method for getting a IResourceHandler for this {@link EbookPropertyItem} instance.
	 * @return The desired {@link IResourceHandler}.
	 */
	public IResourceHandler getResourceHandler() {
		return ResourceHandlerFactory.getResourceLoader(this.file);
	}
	
	/**
	 * Get the file name without the path. The file name is created from the
	 * {@link #getFile()} property.
	 * 
	 * @return the file name without path statement.
	 */
	public String getFileName() {
		if(file!=null && file.indexOf('/')!=-1) {
			return file.substring(file.lastIndexOf('/')+1);
		} else if(file!=null && file.indexOf('\\')!=-1) {
			return file.substring(file.lastIndexOf('\\')+1);
		} else if(file!=null){
			return this.file;
		} else {
			return "";
		}
	}	
	
    public boolean equals(Object obj) {
    	if(file==null) {
    		return false;
    	}
    	
    	if(obj instanceof EbookPropertyItem) {
    		return file.equals(((EbookPropertyItem)obj).file);
    	}
    	return false;
    }	
    
   
    
	/**
	 * Clears all metadata excepting this ones which have a {@link ProtectedField} annotation.
	 */
	public void clearMetadata() {
		List<Field> dbViewFields = EbookPropertyItemUtils.getFieldsByAnnotation(ViewField.class, EbookPropertyItem.class);
		for (Field field : dbViewFields) {
			try {
				if(field.getAnnotation(ProtectedField.class) == null) {
					field.set(this, null);
				}
			} catch (Exception e) {
				LoggerFactory.log(Level.SEVERE, this, "could not clear EbookPropertyItem field " + field.getName(), e);
			}
		}		
	}  
	
	/**
	 * Get the CRC32 checksum for the cover thumbnail.
	 * @return The desired checksum or 0 if no thumbnail is set.
	 */
	public long getCoverThumbnailCRC32() {
		CRC32 crc32 = new CRC32();
		if(getCoverThumbnail() != null) {
			crc32.update(getCoverThumbnail());
			return crc32.getValue();
		} else {
			return 0;
		}
	}

	public String getFile() {
		return file;
	}
	
	public void setFile(String file) {
		this.file = file;
	}

	public String getTitle() {
		return title!=null?title.trim():title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}		
	
	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getIsbn() {
		//urn:isbn:
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSeriesName() {
		return seriesName;
	}

	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}

	public String getSeriesIndex() {
		return seriesIndex;
	}

	public void setSeriesIndex(String seriesIndex) {
		this.seriesIndex = seriesIndex;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public String getRights() {
		return rights;
	}

	public void setRights(String rights) {
		this.rights = rights;
	}

	public String getCoverage() {
		return coverage;
	}

	public void setCoverage(String coverage) {
		this.coverage = coverage;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		if(keywords != null) {
			for (int i = 0; i < keywords.size(); i++) {
				keywords.set(i, keywords.get(i).trim());
			}
		}
		this.keywords = keywords;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getAgeSuggestion() {
		return ageSuggestion;
	}

	public void setAgeSuggestion(String ageSuggestion) {
		this.ageSuggestion = ageSuggestion;
	}

	public Date getPublishingDate() {
		return publishingDate;
	}

	public void setPublishingDate(Date publishingDate) {
		this.publishingDate = publishingDate;
	}

	public String getAuthorSort() {
		return authorSort;
	}

	public void setAuthorSort(String authorSort) {
		this.authorSort = authorSort;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public byte[] getCoverThumbnail() {
		return coverThumbnail;
	}
	
	public void setCoverThumbnail(byte[] coverThumbnail) {
		this.coverThumbnail = coverThumbnail;
	}

}
