package org.rr.commons.mufs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.ListUtils;


/**
 * The {@link FileResourceHandler} is able to handle all resources
 * located in the file system.
 */
class FileResourceHandler extends AResourceHandler {

	private static final String[] PATH_SEGMENT_SEPARATORS = new String[] { "/", "\\", File.separator};

	/**
	 * The file url identifier.
	 */
	private static final String FILE_URL = "file:";
	
	/**
	 * The resource String parsed into a file.
	 */
	private String resourceString;
	
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
		this.resourceString = normalizeDirectoryResourceString(f.getPath(), f);
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
	public boolean isValidResource(final String resourceString) {
		if(resourceString.startsWith(FILE_URL)) {
			try {
				URLDecoder.decode(resourceString, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return false;
			}
			return true;
		}
		
		try {
			if(isUnixFilePath(resourceString)) {
				return true;
			} else if (isWindowsFilePath(resourceString)) {
				return true;
			} else if (new File(resourceString).exists()) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Test if the given resource string match to a unix file- or file system path.
	 * @param resourceString A resource string to be tested if it look like a unix path
	 * @return <code>true</code> if the given resourceString is a unix path or <code>false</code> otherwise.
	 */
	private boolean isUnixFilePath(String resourceString) {
		return resourceString.startsWith("/") && resourceString.indexOf("//")==-1 && resourceString.indexOf("\\\\")==-1 && !resourceString.startsWith("\\");
	}
	
	/**
	 * Tells if the given resource string matches to a valid windows path or file name.
	 * @param resourceString The resource string to be tested. Following examples matches with <code>true</code>.
	 *	c:
	 *	c:\
	 *	c:\nv6vsa76A5v
	 *	c:\nv6vsa76A5v\
	 *	c:\nv6vsa76A5v\hvsdav
	 *	c:\nv6vsa76A5v\hvsdav\hvsdav\hvsdav\hvsdav\
	 *	c:\nv6vsa76A5v\hvsdav\hvsdav\hvsdav\hvsdav\web.config
	 *	C:\abc\aabc6675bnvs.thgcsdcbsd
	 *	d:\folder1\folder1\web.config
	 *	d:\folder1\folder1\1.txt
	 *
	 *	Not Accept
	 *	C
	 *	C::
	 *	C:\\
	 *	C:\abc\\
	 *	C:\abc\ab%g
	 *	C:\abc\aabc.txt\
	 *	C:\abc\aabc.txt\ab
	 * @return <code>true</code> if the given resource matches to a windows file and <code>false</code> otherwise.
	 */
	private boolean isWindowsFilePath(String resourceString) {
		return Pattern.matches("^[a-zA-Z]:\\\\.*", resourceString);
	}

	/**
	 * Creates a new {@link FileResourceHandler} instance for the given
	 * resource.
	 *
	 * @param resourceString The resource to be loaded.
	 */
	@Override
	public IResourceHandler createInstance(String resourceString) {
		final FileResourceHandler result = new FileResourceHandler();
		if(resourceString.startsWith(FILE_URL)) {
			String fileUrlResource = resourceString.substring(FILE_URL.length());
			if(fileUrlResource.indexOf('%') != -1) {
				try {
					fileUrlResource = URLDecoder.decode(fileUrlResource, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					LoggerFactory.getLogger().log(Level.SEVERE, "Failed to decode file string " + fileUrlResource);
				}
			}
			result.setFile(new File(fileUrlResource));
		} else {
			result.setFile(new File(resourceString));
		}
		
		result.resourceString = normalizeDirectoryResourceString(resourceString, result.file);
		return result;
	}

	private String normalizeDirectoryResourceString(String resource, File file) {
		if(file.isDirectory()) {
			//normalize that a directory resource is always returned with a trailing slash / backslash
			if(!StringUtils.endsWithAny(resource, PATH_SEGMENT_SEPARATORS)) {
				resource = resource + File.separator;
			}
		}
		return resource;
	}
	
	public String getResourceString() {
		return resourceString;
	}

	/**
	 * Reads the file specified for this {@link FileResourceHandler} instance into
	 * a byte[].
	 *
	 * @return The byte content of the file.
	 */
	@Override
	public synchronized byte[] getContent() throws IOException {
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
	public ResourceHandlerInputStream getContentInputStream() throws IOException {
		this.cleanHeapIfNeeded(this.file.length());
		try {
			FileInputStream fIn = new FileInputStream(this.file);
			ResourceHandlerInputStream buffIn = new ResourceHandlerInputStream(this, fIn) ;
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
		resetIsDirectoryEvaluation();
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
		if(this.isFileResource() && this.exists()) {
			Path path = Paths.get(file.getAbsolutePath());
			Files.delete(path);
		} else {
			FileUtils.deleteDirectory(this.file);
		}
		
		if(this.exists()) {
			throw new IOException("could not delete resource " + String.valueOf(this.file));
		}
		
		//no need to delete this later. It's already done.
		ResourceHandlerFactory.removeTemporaryResource(this);
		resetIsDirectoryEvaluation();
	}

	private void resetIsDirectoryEvaluation() {
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
	
	public IResourceHandler[] listDirectoryResources(final boolean showHidden) throws IOException {
		return listDirectoryResources(new ResourceNameFilter() {
			
			@Override
			public boolean accept(IResourceHandler loader) {
				if(!showHidden) {
					if(loader.getName().startsWith(".")) {
						return false;
					} else if(((FileResourceHandler)loader).file.isHidden()) {
						return false;
					}
				}
				return true;
			}
		});
	}

	/**
	 * Lists all files and folders which are children of this {@link IResourceHandler} instance.
	 *
	 * @return all child {@link IResourceHandler} instances.
	 */
	@Override
	public IResourceHandler[] listResources(final ResourceNameFilter filter) throws IOException {
		final ArrayList<IResourceHandler> resourceResult = new ArrayList<>();
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
	 * Lists all {@link File}s which are children of this {@link IResourceHandler} instance
	 * and which are directories.
	 *
	 * @return all child {@link IResourceHandler} instances.
	 */
	@Override
	public IResourceHandler[] listDirectoryResources(ResourceNameFilter filter) {
		final ArrayList<IResourceHandler> result = new ArrayList<>();
		
		synchronized(fileSystemViewInstance) {
			File[] files = FileResourceHandler.fileSystemViewInstance.getFiles(this.file, false);
			for (int i = 0; i < files.length; i++) {
				if(files[i].isDirectory()) {
					IResourceHandler resourceLoader;
					resourceLoader = ResourceHandlerFactory.getResourceHandler(files[i].getPath());
					if(resourceLoader != null) {
						if(filter != null && filter.accept(resourceLoader)) {
							result.add(resourceLoader);
						} else if(filter == null) {
							result.add(resourceLoader);
						}
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
		final ArrayList<IResourceHandler> result = new ArrayList<>();

		synchronized(fileSystemViewInstance) {
//			File[] files = this.file.listFiles();
			//did not list files with invalid charset under ubuntu
			File[] files = FileResourceHandler.fileSystemViewInstance.getFiles(this.file, false);
			for (int i = 0; i < files.length; i++) {
				if(!files[i].isDirectory()) {
					IResourceHandler resourceLoader;
					resourceLoader = ResourceHandlerFactory.getResourceHandler(files[i].getPath());
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
			List<IResourceHandler> resultResources = new ArrayList<>(files.length);
			for (int i = 0; i < files.length; i++) {
				if(files[i].isFile()) {
					resultResources.add(ResourceHandlerFactory.getResourceHandler(files[i]));
				}
			}
			return resultResources.toArray(new IResourceHandler[resultResources.size()]);
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
	 * Sets some local fields to <code>null</code>. It's not really important
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
	public boolean copyTo(IResourceHandler targetRecourceLoader, boolean overwrite) throws IOException {
		if(targetRecourceLoader instanceof FileResourceHandler) {
			if(this.isDirectoryResource() && !targetRecourceLoader.exists()) {
				//copy the source directory to the not existing target directory
				targetRecourceLoader.mkdirs();
				FileUtils.copyDirectory(this.file, ((FileResourceHandler)targetRecourceLoader).file);
				return true;
			} if(this.isDirectoryResource() && targetRecourceLoader.isDirectoryResource()) {
				//copy the source directory to the target directory
				FileUtils.copyDirectory(this.file, ((FileResourceHandler)targetRecourceLoader).file);
				return true;
			} else if(!this.isDirectoryResource()) {
				//test if the target file already exists.
				if(!overwrite && targetRecourceLoader.exists() && !targetRecourceLoader.isDirectoryResource()) {
					throw new IOException("file already exists");
				}
				
				//try to copy using fast nio copy.
				try {
					return this.nioCopyFile(this.file, ((FileResourceHandler)targetRecourceLoader).file, overwrite);
				} catch (IOException e) {
					//copy the file to the target directory resource
					FileUtils.copyFile(this.file, ((FileResourceHandler)targetRecourceLoader).file);
					return true;
				}
			} else {
				throw new IOException("could not copy the directory "+this.getResourceString()+" over the file " + targetRecourceLoader.getResourceString());
			}
		}
		
		//perform a slow stream copy.
		OutputStream contentOutputStream = null;
		try {
			contentOutputStream = targetRecourceLoader.getContentOutputStream(false);
			IOUtils.write(this.getContent(), contentOutputStream);
			return true;
		} finally {
			IOUtils.closeQuietly(contentOutputStream);
		}
		
	}


	@Override
	public void moveTo(IResourceHandler targetRecourceLoader, boolean overwrite) throws IOException {
		resetIsDirectoryEvaluation();
		
		if(targetRecourceLoader instanceof FileResourceHandler) {
			if(this.equals(targetRecourceLoader)) {
				return;
			}
			
			if(!this.isDirectoryResource()) {
				//test if the target file already exists.
				if(!overwrite && targetRecourceLoader.exists() && !targetRecourceLoader.isDirectoryResource()) {
					throw new IOException("file already exists " + targetRecourceLoader.getResourceString());
				} else if(targetRecourceLoader.isDirectoryResource()) {
					throw new IOException("target is not a file " + targetRecourceLoader.getResourceString());
				}
				boolean deleted = FileUtils.deleteQuietly(((FileResourceHandler) targetRecourceLoader).file);
				if(!deleted && ((FileResourceHandler) targetRecourceLoader).file.exists()) {
					throw new IOException("Deleting file " + targetRecourceLoader + " has failed.");
				}
				FileUtils.moveFile(this.file, ((FileResourceHandler) targetRecourceLoader).file);
				return;
			}
			super.moveTo(targetRecourceLoader, overwrite);
			return;
		}
		super.moveTo(targetRecourceLoader, overwrite);
		return;
	}
	
	/**
	 * Fast copy using nio.
	 *
	 * @param sourceFile source file
	 * @param destFile target file
	 * @param overwrite <code>true</code> if overwriting exiting target.
	 * @throws IOException
	 */
	public boolean nioCopyFile(File sourceFile, File destFile, boolean overwrite) throws IOException {
		long position = 0;
		if(!overwrite && destFile.exists()) {
			return false;
		}
		
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		try (FileInputStream in = new FileInputStream(sourceFile);
				FileChannel source = in.getChannel();
				FileOutputStream out = new FileOutputStream(destFile);
				FileChannel destination = out.getChannel()) {
			destination.transferFrom(source, position, source.size());
			return true;
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
		return ResourceHandlerFactory.getResourceHandler(file);
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
			return ResourceHandlerFactory.getResourceHandler(fileSystemViewInstance.createNewFolder(this.file));
		}
	}

	@Override
	public File toFile() {
		return getFile();
	}

	@Override
	public List<String> getPathSegments() {
		final File file = getFile();
		List<String> result = Collections.emptyList();
		if(file != null) {
			result = ListUtils.split(file.toString(), File.separator);
			if(!result.isEmpty() && result.get(0).isEmpty()) {
				result.set(0, "/");
			}
		}
		return result;
	}

	@Override
	public boolean isHidden() {
		return this.file.isHidden() || this.file.getName().startsWith(".");
	}
	
}
