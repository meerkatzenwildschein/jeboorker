package org.rr.commons.mufs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.rr.commons.log.LoggerFactory;

/**
 * The {@link AResourceHandler} provides some provider implementation independant methods.
 * It's not attendant to extends this class for creating new {@link IResourceHandler} types
 * but the {@link IResourceHandler} interface must be implemented.
 */
abstract class AResourceHandler implements IResourceHandler, Comparable<IResourceHandler> {
	
	private static final ArrayList<IResourceHandler> temporaryResourceLoader = new ArrayList<IResourceHandler>();
	
	private HashMap<String, Object> metaMap;
	
	private static final Thread shutdownThread = new Thread(new Runnable() {
		@Override
		public void run() {
			//stores all loaded which could not be deleted.
			final ArrayList<IResourceHandler> newTemporaryResourceLoader = new ArrayList<IResourceHandler>();
			IResourceHandler resourceHandler = null;
			for (int i = 0; i < temporaryResourceLoader.size(); i++) {
				try {
					resourceHandler = temporaryResourceLoader.get(i);
					if(resourceHandler.exists()) {
						resourceHandler.delete();
					}
				} catch (IOException e) {
					//do not abort
					System.err.println("could not delete temporary resource " + String.valueOf(resourceHandler));
					newTemporaryResourceLoader.add(resourceHandler);
				}
			}
			temporaryResourceLoader.clear();
			temporaryResourceLoader.addAll(newTemporaryResourceLoader);
		}
	});
	
	static {
		Runtime.getRuntime().addShutdownHook(shutdownThread);
	}

	/**
	 * The file format. This is a cached value, so the format should not be determeined each time.
	 */
	private String mime = null;
	
	/**
	 * Creates a {@link IResourceHandler} file which points to a temporary file which is automatically deleted
	 * at application end.
	 */
	@Override
	public IResourceHandler getTemporaryResource() {
		try {
			File tmp = File.createTempFile(this.getName(), ".tmp");
			IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceHandler(tmp);
			
			temporaryResourceLoader.add(resourceLoader);
			return resourceLoader;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Deletes all temporary resources previously created with the {@link #getTemporaryResource()}
	 * method.
	 */
	public static void deleteTemporaryResources() {
		shutdownThread.run();
	}

	/**
	 * An empty implementation because it's not needed for all {@link IResourceHandler} implementations.
	 */
	@Override
	public void refresh() {
		this.mime = null;
	}

	/**
	 * Tries to determine the format of the resource handled by this {@link IResourceHandler} instance.
	 * The file format will firstly be determined by the resource extension and afterwards by it's content.
	 */
	@Override
	public String getMimeType() {
		if (this.mime != null) {
			if(this.mime.length() > 0) {
				return this.mime;
			}
			return null;
		} else if(this.isDirectoryResource()) {
			this.mime = "";
			return null;
		}
		
		final String resourceString = this.getResourceString();
		if(resourceString!=null && resourceString.lastIndexOf('.')!=-1) {
			final String lowerCasedResourceString = resourceString.toLowerCase();
			if(lowerCasedResourceString.endsWith(".jpg") || lowerCasedResourceString.endsWith(".jpeg")) {
				return (this.mime = "image/jpeg");
			} else if (lowerCasedResourceString.endsWith(".png")) {
				return (this.mime = "image/png");
			} else if (lowerCasedResourceString.endsWith(".gif")) {
				return (this.mime = "image/gif");
			} else if (lowerCasedResourceString.endsWith(".txt")) {
				return (this.mime = "text/plain");
			} else if(lowerCasedResourceString.endsWith(".epub")) {
				return (this.mime = "application/epub+zip");
			} else if(lowerCasedResourceString.endsWith(".pdf")) {
				return (this.mime = "application/pdf");
			} else if(lowerCasedResourceString.endsWith(".cbz")) {
				return (this.mime = "application/x-cbz");
			} else if(lowerCasedResourceString.endsWith(".html") || lowerCasedResourceString.endsWith(".htm")) {
				return (this.mime = "text/html");
			} else if(lowerCasedResourceString.endsWith(".xml")) {
				return (this.mime = "text/xml");
			}
		}
		
		try {
			//this is a simple but pretty good working method which detects jpeg 
			//data which mime-util not did.
			final String guessedMime = ResourceHandlerUtils.guessFormat(this);
			if(guessedMime!=null) {
				return this.mime = guessedMime;
			}
		} catch(FileNotFoundException e) { 
			LoggerFactory.logInfo(this, "File not found " + this, e);
			return null; //No file, no reason to continue.
		} catch (IOException e1) {
//			LoggerFactory.logInfo(this, "Could not guess format for " + this, e1);
			return null; //IO is not good. No reason to continue.
		}		
		
		return null;
	}
	

	/**
	 * Tells if this {@link AResourceHandler} instance file format is an image.
	 * @return <code>true</code> if the resource is an image or <code>false</code> otherwise.
	 */
	@Override
	public boolean isImageFormat() {
		return getMimeType()!=null && getMimeType().startsWith("image/");
	}
	
	/**
	 * Gets the file extension of the file resource. The last three characters behind the dot must not 
	 * really be the file extension! The format of the file will be determined and compared to
	 * the file system file extension. Only of the format and the file system file extension
	 * matches to each other, the extension of the file is returned. 
	 * 
	 * @return The file extension. If no extension is detected, an empty String is returned.
	 */
	public String getFileExtension() {
		final String fileName = this.getName().toLowerCase();
		try {
			//test if a file extension was specified.
			if(fileName.indexOf('.') == -1 || isDirectoryResource()) {
				return "";
			}
			
			//test if the file ends with a default file extension. If the
			//format and the extension did not match, the string behind the dot
			//belong to the file name.
			final String mime = this.getMimeType();
			if(mime !=null && mime.length()>0 && mime.indexOf('/')!=-1) {
				final String mimeFormatPart =  mime.substring(mime.indexOf('/')+1);
				if(mimeFormatPart.equals("jpg") || mimeFormatPart.equals("jpeg")) {
					if(!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
						return ""; //no jpeg extension. all after the dot belongs to the file name.
					}
				} else if(mimeFormatPart.equals("png")) {
					if(!fileName.endsWith(".png")) {
						return ""; //no jpeg extension. all after the dot belongs to the file name.
					}
				} else if(mimeFormatPart.equals("gif")) {
					if(!fileName.endsWith(".gif")) {
						return ""; //no jpeg extension. all after the dot belongs to the file name.
					}
				}
			}
			
			//return with the chars behind the last dot. 
			return fileName.substring(this.getName().lastIndexOf('.')+1);
		} catch (Exception e) {
			return "";
		}
	}
	
	/**
	 * Tells if the reosurce handled by this {@link AResourceHandler} is
	 * a file resource.
	 * 
	 * @return <code>true</code> if it's a file resource or <code>false</code> otherwise.
	 */
	public boolean isFileResource() {
		if(!this.exists()) {
			return false;
		}
		return !this.isDirectoryResource();
	}
	

	/**
	 * Reads the content of the {@link InputStream} provided by this {@link InputStreamResourceHandler}
	 * and puts it into the target {@link IResourceHandler}. 
	 */
	@Override
	public void copyTo(IResourceHandler targetRecourceLoader, boolean overwrite) throws IOException {
		//handle overwrite
		if(!overwrite && targetRecourceLoader.exists()) {
			return;
		}
		
		//perform a slow stream copy.
		OutputStream contentOutputStream = null;
		try {
			contentOutputStream = targetRecourceLoader.getContentOutputStream(false);
			IOUtils.write(this.getContent(), contentOutputStream);
		} finally {
			IOUtils.closeQuietly(contentOutputStream);
		}
	}

	/**
	 * Perfrom also a {@link #copyTo(IResourceHandler, boolean)} because a stream could not be moved.
	 */
	@Override
	public void moveTo(IResourceHandler targetRecourceLoader, boolean overwrite) throws IOException {
		copyTo(targetRecourceLoader, overwrite);
		delete();
	}	
	
	/**
	 * A general filter listResources method. Should be reimplemented if a {@link IResourceHandler} implementation
	 * is able to perform a more performant way.
	 * @param filter A filter to be used for filter the result child resources.
	 * 	set it to <code>null</code> for no filter
	 * @return all files and diretories matching to the given {@link ResourceNameFilter}.
	 */
	@Override
	public IResourceHandler[] listResources(final ResourceNameFilter filter) throws IOException {
		//get files and directories
		List<IResourceHandler> listFileResources = Arrays.asList(this.listFileResources());
		List<IResourceHandler> listDirectoryResources = Arrays.asList(this.listDirectoryResources());
		
		//create the result containing both, directories and files with the right size, so the list must ne be resized while copying into it.
		ArrayList<IResourceHandler> resultFileResources = new ArrayList<IResourceHandler>(listFileResources.size()+listDirectoryResources.size()); 
		
		//loop directory resources
		for (int i = 0; i < listDirectoryResources.size(); i++) {
			final IResourceHandler resourceHandler = listDirectoryResources.get(i);
			if(filter==null || filter.accept(resourceHandler)) {
				//add the resource.
				resultFileResources.add(resourceHandler);
			}
		}		
		
		//loop file resources
		for (int i = 0; i < listFileResources.size(); i++) {
			final IResourceHandler resourceHandler = listFileResources.get(i);
			if(filter==null || filter.accept(resourceHandler)) {
				//add the resource.
				resultFileResources.add(resourceHandler);
			}
		}
		
		final IResourceHandler[] result = resultFileResources.toArray(new IResourceHandler[resultFileResources.size()]);
		return result;
	}	

	/**
	 * Gets the resource string as toString output. Use
	 * {@link #getResourceString()} instead of {@link #toString()} if the
	 * path for this {@link AResourceHandler} instance is needed.
	 * @return The strign representation for this {@link AResourceHandler} instance. 
	 */
	public String toString() {
		return this.getResourceString();
	}
	
	/**
	 * Gets the content of the resource handled by this {@link IResourceHandler} instance.
	 * 
	 * @return The content of the resource.
	 */
	@Override
	public byte[] getContent() throws IOException {
		InputStream contentInputStream = this.getContentInputStream();
		byte[] byteArray = IOUtils.toByteArray(contentInputStream);
		IOUtils.closeQuietly(contentInputStream);
		return byteArray;
	}
	
	/**
	 * Sometimes, on heavy IO, the garbage collector isn't fast enough to free the heap.
	 * To prevent this, the garbage collector is triggered if not enough space is 
	 * present.
	 * @param heapRequired The amount of heap needed in the near future.
	 * @throws IOException 
	 */
	protected void cleanHeapIfNeeded(long heapRequired) throws IOException {
		final MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		final long heapFreeSize = heapMemoryUsage.getCommitted() - heapMemoryUsage.getUsed();

		if(heapFreeSize < (heapRequired * 1.2)) {
//			LoggerFactory.getLogger().log(Level.INFO , "Garbage collector triggered manually. " + heapFreeSize + " bytes remaining but " + heapRequired + " required for " + getName());
			System.gc();
		}
	}
	
	/**
	 * Gets the content of the resource handled by this {@link IResourceHandler} instance.
	 * 
	 * @return The content of the resource.
	 */
	@Override
	public byte[] getContent(int length) throws IOException {
		final InputStream contentInputStream = this.getContentInputStream();
		final ByteArrayOutputStream output = new ByteArrayOutputStream(length);
		ResourceHandlerUtils.copy(contentInputStream, output, length);
		IOUtils.closeQuietly(contentInputStream);
		return output.toByteArray();
	}	
	
	public void setContent(byte[] content) throws IOException {
		OutputStream contentOutputStream = this.getContentOutputStream(false);
		IOUtils.write(content, contentOutputStream);
		IOUtils.closeQuietly(contentOutputStream);
	}
	
	/**
	 * Tests the resource string of the given object with this
	 * instance for equalness.
	 * @return <code>true</code> if the resources are equal und <code>false</code> otherwise.
	 */
	public boolean equals(Object o) {
		if(o instanceof IResourceHandler) {
			return ((IResourceHandler)o).getResourceString().equals(this.getResourceString());
		}
		
		return false;
	}
	
	/**
	 * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * 
     * This is an alphanumeric comperator. 
     * 
     * @param   o the object to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     *		is less than, equal to, or greater than the specified objec
     *
     * @see Comparable#compareTo(Object)
	 */
	public int compareTo(IResourceHandler o) {
	        return ResourceHandlerUtils.compareTo(this, o, ResourceHandlerUtils.SORT_BY_NAME);
	}
	
	/**
	 * Tests if this {@link AResourceHandler} instance is a root instance
	 * by getting the parent resource. If the parent resource is <code>null</code>
	 * this {@link AResourceHandler} instance is a root instance.
	 * <br><br>
	 * Implement a more effective way by overriding this method.
	 */
	public boolean isRoot() {
		return this.getParentResource()==null;
	}
	
	/**
	 * Always returns <code>false</code>. Should overriden for local 
	 * file system implementations.
	 */
	public boolean isFloppyDrive() {
		return false;
	}
	
	/**
	 * @return the root node for this {@link AResourceHandler} instance.
	 * If multiple roots supported, this method should be overriden.
	 */
	public IResourceHandler[] getRoots() {
		IResourceHandler parent = this;
		while(!parent.isRoot()) {
			parent = parent.getParentResource();
		}
		return new IResourceHandler[] {parent};
	}
	
	/**
	 * This default implementation uses the {@link FilenameFilter} to filter
	 * these files starting with a '.'.
	 */
	public IResourceHandler[] listFileResources(boolean showHidden) throws IOException {
		IResourceHandler[] listResources = this.listResources(new ResourceNameFilter() {
		
			@Override
			public boolean accept(IResourceHandler loader) {
				if(loader.getName().startsWith(".")) {
					return false;
				} 
				return true;
			}
		});
		return listResources;
	}
	
	public final IResourceHandler[] listDirectoryResources() throws IOException {
		return listDirectoryResources(null);
	}
	
	/**
	 * The default implementation did not support these feature.
	 * The {@link #getName()} result is returned.
	 */
	@Override
	public String getSystemDisplayName() {
		return this.getName();
	}	
	
	/**
	 * Creates a new folder with the name "New Folder".
	 */
	@Override
	public IResourceHandler createNewFolder() throws IOException {
		try {
			IResourceHandler addPathStatement = this.addPathStatement("New Folder");
			addPathStatement.mkdirs();
			return addPathStatement;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public void addMeta(String key, Object value) {
		if(metaMap == null) {
			metaMap = new HashMap<String, Object>();
		}
		metaMap.put(key, value);
	}

	@Override
	public Object getMeta(String key) {
		if(metaMap == null) {
			return null;
		}
		return metaMap.get(key);
	}
}
