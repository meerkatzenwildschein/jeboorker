package org.rr.commons.mufs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;


/**
 * The {@link FileResourceHandler} is able to handle all resources
 * located in the file system.
 */
class FileResourceHandler extends AResourceHandler {

	/**
	 * The file url identifier.
	 */
	private static final String FILE_URL = "file://";
	
	/**
	 * The resource String parsed into a file.
	 */
	private String resource;
	
	/**
	 * the file to be handled with this {@link FileResourceHandler} instance.
	 */
	private File file;
	
	/**
	 * The {@link FileResourceHandler} for the parent directory.
	 */
	private FileResourceHandler parentFileresourceLoader = null;
	
	private Boolean isFloppyDrive = null;
	
	private Boolean isDirectory = null;
	
	/**
	 * Provide a shared FileSystemView but note that it's not synchronized.
	 */
	private static final FileSystemView fileSystemViewInstance = FileSystemView.getFileSystemView();
	
	FileResourceHandler() {
		super();
	}
	
	
	FileResourceHandler(File f) {
		super();
		this.setFile(f);
		this.resource  = f.getPath();
	}
	
	/**
	 * Gets the file which is set to this {@link FileResourceHandler} instance. 
	 * 
	 * @return The desired file or <code>null</code> if no file was set.
	 */
	File getFile() {
		return file;
	}

	/**
	 * Sets the {@link File} to be handled to the resource loader.
	 * @param file The file to be set.
	 */
	void setFile(File file) {
		this.file = file;
	}	
	

	@Override
	public void refresh() {
		super.refresh();
	}	

	/**
	 * Tests if the given resource is a valid file system file.
	 */
	@Override
	public boolean isValidResource(final String resource) {
		if(resource.startsWith(FILE_URL)) {
			return true;
		}
		
		try {
			if(resource.startsWith("/") && resource.indexOf("//")==-1 && resource.indexOf("\\\\")==-1 && !resource.startsWith("\\")) {
				return true;
			} else if (new File(resource).exists()) {
				return true;
			} else if (Pattern.matches("^[a-zA-Z]:\\\\.*", resource)){
				//windows pattern match .. 
				/*
				c:
				c:\
				c:\nv6vsa76A5v
				c:\nv6vsa76A5v\
				c:\nv6vsa76A5v\hvsdav
				c:\nv6vsa76A5v\hvsdav\hvsdav\hvsdav\hvsdav\
				c:\nv6vsa76A5v\hvsdav\hvsdav\hvsdav\hvsdav\web.config
				C:\abc\aabc6675bnvs.thgcsdcbsd
				d:\folder1\folder1\web.config
				d:\folder1\folder1\1.txt

				Not Accept
				C
				C::
				C:\\
				C:\abc\\
				C:\abc\ab%g
				C:\abc\aabc.txt\
				C:\abc\aabc.txt\ab	*/			
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Creates a new {@link FileResourceHandler} instance for the given
	 * resource.
	 * 
	 * @param resource The resource to be loaded.
	 */
	@Override
	public IResourceHandler createInstance(final String resource) {
		final FileResourceHandler result = new FileResourceHandler();
		if(resource.startsWith(FILE_URL)) {
			result.setFile(new File(resource.substring(FILE_URL.length())));
		} else {
			result.setFile(new File(resource));
		}
		result.resource = resource;
		
		return result;
	}
	
	public String getResourceString() {
		return this.resource;
	}

	/**
	 * Reads the file specified for this {@link FileResourceHandler} instance into
	 * a byte[].
	 * 
	 * @return The byte content of the file.
	 */
	@Override
	public byte[] getContent() throws IOException {
		try {
			this.cleanHeapIfNeeded(this.file.length());
			return FileUtils.readFileToByteArray(this.file);
		} catch(Error e) {
			if(e instanceof OutOfMemoryError) {
				System.gc();
				try {Thread.sleep(100);} catch (InterruptedException e1) {}
			}
		}
		return FileUtils.readFileToByteArray(this.file);
	}

	/**
	 * Creates a new {@link BufferedInputStream} for the file specified for this {@link FileResourceHandler} instance.
	 * 
	 * @return The desired {@link InputStream}.
	 */
	@Override
	public InputStream getContentInputStream() throws IOException {
		this.cleanHeapIfNeeded(this.file.length());
		try {
			FileInputStream fIn = new FileInputStream(this.file);
			BufferedInputStream buffIn = new BufferedInputStream(fIn) ;
			return buffIn;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * Creates a new {@link BufferedOutputStream} for the file specified for this {@link FileResourceHandler} instance.
	 * 
	 * @param append Tells if the data written to the {@link OutputStream} is appended to the file or the file is overwritten.
	 * @return The desired {@link OutputStream}
	 */
	@Override
	public OutputStream getContentOutputStream(boolean append) throws IOException {
		final FileOutputStream fOut = new FileOutputStream(this.file, append);
		final BufferedOutputStream buffOut = new BufferedOutputStream(fOut);
		
		return buffOut;
	}

	/**
	 * Tells if the file exists
	 */
	@Override
	public boolean exists() {
		if(this.isFloppyDrive()) {
			return true;
		}
		if(this.file.exists()) {
			return true;
		}
		
		synchronized(fileSystemViewInstance) {
			return fileSystemViewInstance.isDrive(this.file);
		}
	}

	@Override
	public IResourceHandler getParentResource() {
		//must not be synchronized because it's not intendant if the 
		//cached parent FileResourceLoader is overwritten by another invokement
		//at the nearly same time.
		File parentFile = this.file.getParentFile();
		if(parentFile==null) {
			return null;
		}
		
		if(this.parentFileresourceLoader == null) {
			this.parentFileresourceLoader = (FileResourceHandler) this.createInstance(parentFile.getPath());
		}
		return this.parentFileresourceLoader;
	}

	@Override
	public void writeStringContent(String content, String encoding) throws IOException {
		FileUtils.writeStringToFile(file, content, encoding);
	}

	@Override
	public boolean mkdirs() {
		this.isDirectory = null;
		return this.file.mkdirs();
		
	}

	/**
	 * Deletes the file handled with this {@link FileResourceHandler} instance.
	 * @return <code>true</code> if and only if the file or directory is
     *          successfully deleted; <code>false</code> otherwise
	 * @throws IOException 
	 */
	@Override
	public void delete() throws IOException {
		boolean success = this.file.delete();
		if(!success) {
			throw new IOException("could not delete resource " + String.valueOf(this.file));
		}
		this.isDirectory = null;
	}
	
	@Override
	public boolean moveToTrash() throws IOException {
		try {
			if(!ResourceHandlerUtils.moveToTrash(this)) {
				com.sun.jna.platform.FileUtils.getInstance().moveToTrash(new File[] { new File(this.toString()) });
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return !exists();
	}	

	public String toString() {
		return this.getResourceString();
	}

	/**
	 * Lists all files and folders which are children of this {@link IResourceHandler} instance.
	 * 
	 * @return all child {@link IResourceHandler} instances.
	 */	
	@Override
	public IResourceHandler[] listResources(final ResourceNameFilter filter) {
		final ArrayList<IResourceHandler> resourceResult = new ArrayList<IResourceHandler>();
		IResourceHandler[] listFileResources = this.listFileResources();
		IResourceHandler[] listDirectoryResources = this.listDirectoryResources();
		for (int i = 0; i < listFileResources.length; i++) {
			if(filter==null) {
				resourceResult.add(listFileResources[i]);
			} else if(filter.accept(listFileResources[i])) {
				//attach the accepted resource loader to the result list.
				resourceResult.add(listFileResources[i]);
			}
		}
		
		for (int i = 0; i < listDirectoryResources.length; i++) {
			if(filter==null) {
				resourceResult.add(listDirectoryResources[i]);
			} else if(filter.accept(listDirectoryResources[i])) {
				//attach the accepted resource loader to the result list.
				resourceResult.add(listDirectoryResources[i]);
			}
		}		
		
		IResourceHandler[] sortedFileResourceHandlers = ResourceHandlerUtils.sortResourceHandlers(resourceResult.toArray(new IResourceHandler[resourceResult.size()]), ResourceHandlerUtils.SORT_BY_NAME, true);
		
		return sortedFileResourceHandlers;		
	}

	/**
	 * Lists all {@link File}s which are children of this {@link IResourceHandler} instance.
	 * And which are directories.
	 * 
	 * @return all child {@link IResourceHandler} instances.
	 */	
	@Override
	public IResourceHandler[] listDirectoryResources() {
		final ArrayList<IResourceHandler> result = new ArrayList<IResourceHandler>();
		
		synchronized(fileSystemViewInstance) {
			File[] files = FileResourceHandler.fileSystemViewInstance.getFiles(this.file, false);
			for (int i = 0; i < files.length; i++) {
				if(files[i].isDirectory()) {
					IResourceHandler resourceLoader;
					resourceLoader = ResourceHandlerFactory.getResourceLoader(files[i].getPath());
					if(resourceLoader!=null) {
						result.add(resourceLoader);
					}
				}
			}
		}
		
		return ResourceHandlerUtils.sortResourceHandlers(result.toArray(new IResourceHandler[result.size()]), ResourceHandlerUtils.SORT_BY_NAME, true);
	}

	/**
	 * Lists all {@link File}s which are children of this {@link IResourceHandler} instance.
	 * @return all child {@link IResourceHandler} instances.
	 */
	@Override
	public IResourceHandler[] listFileResources() {
		final ArrayList<IResourceHandler> result = new ArrayList<IResourceHandler>();

		synchronized(fileSystemViewInstance) {
			File[] files = FileResourceHandler.fileSystemViewInstance.getFiles(this.file, false);
			for (int i = 0; i < files.length; i++) {
				if(files[i].isFile()) {
					IResourceHandler resourceLoader;
					resourceLoader = ResourceHandlerFactory.getResourceLoader(files[i].getPath());
					if(resourceLoader!=null) {
						result.add(resourceLoader);
					}
				}
			}
		}
		
		IResourceHandler[] sortedResourceHandlers =  ResourceHandlerUtils.sortResourceHandlers(result.toArray(new IResourceHandler[result.size()]), ResourceHandlerUtils.SORT_BY_NAME, true);

		return sortedResourceHandlers;
	}
	

	/**
	 * Gets the list of shown (i.e. not hidden) files.
	 *  
	 * @param showHidden <code>true</code> if hidden files should be also shown and <code>false</code> otherwise.
	 * @return All these {@link IResourceHandler} which have this {@link IResourceHandler} as parent.
	 * @throws IOException
	 */
	@Override
	public IResourceHandler[] listFileResources(boolean showHidden) throws IOException {
		if(this.file.isFile()) {
			return new IResourceHandler[0];
		}
		synchronized(fileSystemViewInstance) {
			File[] files = fileSystemViewInstance.getFiles(this.file, !showHidden);
			IResourceHandler[] resultResources = new IResourceHandler[files.length];
			for (int i = 0; i < resultResources.length; i++) {
				resultResources[i] = ResourceHandlerFactory.getResourceLoader(files[i]);
			}		
			return resultResources;
		}
	}	

	/**
	 * Tells if the {@link File} hanlded by this {@link IResourceHandler} instance
	 * is a directory or not.
	 * @return <code>true</code> if it's a directory and <code>false</code> otherwise.
	 */
	@Override
	public boolean isDirectoryResource() {
		//use the cached information
		if(this.isDirectory!=null) {
			return this.isDirectory.booleanValue();
		}
		
		//ask if the folder is a dir. File.isDirectory will
		//trigger the floppy motor. This takes time not needed.
		if(this.isFloppyDrive()) {
			return true;
		}
		
		if(this.file.isDirectory()) {
			return (this.isDirectory = Boolean.TRUE);
		}
		if(!this.file.isFile()) {
			synchronized(fileSystemViewInstance) {
				boolean isDrive = fileSystemViewInstance.isDrive(this.file);
				return (this.isDirectory = Boolean.valueOf(isDrive));
			}
		}
		return false;
	}

	/**
	 * Gets the name of the file without any kind of path statement handled by this {@link FileResourceHandler} instance.
	 */
	@Override
	public String getName() {
		final String fileName = this.file.getName();
		
		//Drive a Win32ShellFolder returns an empty String. 
		if(fileName.length()==0) {
			return this.toString();
		}
		return this.file.getName();
	}

	/**
	 * Sets some local fields to <code>null</code>. It's not really importand 
	 * that the dispose is invoked to this {@link FileResourceHandler} instance.
	 */
	@Override
	public void dispose() {
//		this.parentFileresourceLoader = null;
//		this.file = null;
//		this.resource = null;
	}

	/**
	 * Gets the length of the file handled by this {@link FileResourceHandler} instance.
	 * @return size in bytes or 0 if the file did not exists.
	 */
	@Override
	public long size() {
		final File file = this.getFile();
		if(file.isDirectory()) {
			return 0;
		}
		return file.length();
	}


	@Override
	public void copyTo(IResourceHandler targetRecourceLoader, boolean overwrite) throws IOException {
		if(targetRecourceLoader instanceof FileResourceHandler) {
			if(this.isDirectoryResource() && !targetRecourceLoader.exists()) {
				//copy the source directory to the not existing target directory
				targetRecourceLoader.mkdirs();
				FileUtils.copyDirectory(this.file, ((FileResourceHandler)targetRecourceLoader).file);
				return;
			} if(this.isDirectoryResource() && targetRecourceLoader.isDirectoryResource()) {
				//copy the source directory to the target directory
				FileUtils.copyDirectory(this.file, ((FileResourceHandler)targetRecourceLoader).file);
				return;
			} else if(!this.isDirectoryResource()) {
				//test if the target file already exists.
				if(!overwrite && targetRecourceLoader.exists() && !targetRecourceLoader.isDirectoryResource()) {
					throw new IOException("file already exists");
				}
				
				//try to copy using fast nio copy.
				try {
					this.nioCopyFile(this.file, ((FileResourceHandler)targetRecourceLoader).file, overwrite);
				} catch (IOException e) {
					//copy the file to the target directory resource
					FileUtils.copyFile(this.file, ((FileResourceHandler)targetRecourceLoader).file);
				}
			} else {
				throw new IOException("could not copy the directory "+this.getResourceString()+" over the file " + targetRecourceLoader.getResourceString());
			}
		} else {
			//perform a slow stream copy.
			OutputStream contentOutputStream = null;
			try {
				contentOutputStream = targetRecourceLoader.getContentOutputStream(false);
				IOUtils.write(this.getContent(), contentOutputStream);
			} finally {
				IOUtils.closeQuietly(contentOutputStream);
			}
		}
	}


	@Override
	public void moveTo(IResourceHandler targetRecourceLoader, boolean overwrite) throws IOException {
		this.isDirectory = null;
		
		if(targetRecourceLoader instanceof FileResourceHandler) {
			if(!this.isDirectoryResource()) {
				//test if the target file already exists.
				if(!overwrite && targetRecourceLoader.exists() && !targetRecourceLoader.isDirectoryResource()) {
					throw new IOException("file already exists " + targetRecourceLoader.getResourceString());
				}
				FileUtils.deleteQuietly(((FileResourceHandler) targetRecourceLoader).file);
				FileUtils.moveFile(this.file, ((FileResourceHandler) targetRecourceLoader).file);
				return;
			} else {
				super.moveTo(targetRecourceLoader, overwrite);
				return;
			}
		} else {
			super.moveTo(targetRecourceLoader, overwrite);
			return;
		}
	}
	
	/**
	 * Fast copy using nio. The apache {@link FileUtils} did this not.
	 * 
	 * @param sourceFile source file
	 * @param destFile target file
	 * @param overwrite <code>true</code> if overwriting exiting target.
	 * @throws IOException
	 */
	public void nioCopyFile(File sourceFile, File destFile, boolean overwrite) throws IOException {
		//start position of the file data to copy
		long position = 0;
		if(!overwrite && destFile.exists()) {
			position = destFile.length();
		} 
		
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, position, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}
	

	/**
	 * The file or directory modification date.
	 */
	@Override
	public Date getModifiedAt() {
		return new Date(this.file.lastModified());
	}

	/**
	 * Gets a new {@link IResourceHandler} having the given relative path statement attached.
	 * @param statement A relative path statement to be attached.
	 * @return the desired {@link IResourceHandler}.
	 */
	@Override
	public IResourceHandler addPathStatement(String statement) throws ResourceHandlerException {
		final File file = new File(this.file.getPath() + File.separatorChar + statement);
		return ResourceHandlerFactory.getResourceLoader(file);
	}
	
	/**
	 * @return <code>false</code> in any case because this is a local resource handler. 
	 */
	public boolean isRemoteResource() {
		return false;
	}

	public boolean isRoot() {
		synchronized(fileSystemViewInstance) {
			return fileSystemViewInstance.isFloppyDrive(this.file) || fileSystemViewInstance.isRoot(this.file) || fileSystemViewInstance.isDrive(this.file);
		}
	}
	
	public boolean isFloppyDrive() {
		if(this.isFloppyDrive==null) {
			synchronized(fileSystemViewInstance) {
				this.isFloppyDrive = Boolean.valueOf(fileSystemViewInstance.isFloppyDrive(this.file));
			}
		}
		return this.isFloppyDrive.booleanValue();
	}

	@Override
	public RESOURCE_HANDLER_USER_TYPES getType() {
		return RESOURCE_HANDLER_USER_TYPES.FILESYSTEM;
	}


    /**
     * Returns all root partitions on this system. For example, on
     * Windows, this would be the A: through Z: drives.
     */
	@Override
	public IResourceHandler[] getRoots() {
		IResourceHandler[] fileSystemRoots = ResourceHandlerUtils.getFileSystemRoots();
		return fileSystemRoots;
	}

    /**
     * Name of a file, directory, or folder as it would be displayed in
     * a system file browser. Example from Windows: the "M:\" directory
     * displays as "CD-ROM (M:)"
     *
     * The default implementation gets information from the ShellFolder class.
     *
     * @return the file name as it would be displayed by a native file chooser
     */	
	@Override
	public String getSystemDisplayName() {
		synchronized(fileSystemViewInstance) {
			try {
				return fileSystemViewInstance.getSystemDisplayName(this.file);
			} catch (Exception e) {
				return this.getName();
			}
		}
	}

	@Override
	public IResourceHandler createNewFolder() throws IOException {
		synchronized(fileSystemViewInstance) {
			return ResourceHandlerFactory.getResourceLoader(fileSystemViewInstance.createNewFolder(this.file));
		}
	}
	
}
