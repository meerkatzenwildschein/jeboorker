package org.rr.commons.mufs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.swing.filechooser.FileSystemView;


public interface IResourceHandler extends Comparable<IResourceHandler> {

	/**
	 * An interface which allows to add a method to the {@link RESOURCE_HANDLER_USER_TYPES} enum.
	 */
	public interface ResourceHandlerType {
		/**
		 * Tells if the {@link ResourceHandlerType} is a user type which is visible to the user
		 * or an internal {@link ResourceHandlerType} which is only be used by the application code.
		 * @return
		 */
		public boolean isUserType();
	}

	/**
	 * All resource handler types which can be created by the user.
	 */
	public static enum RESOURCE_HANDLER_USER_TYPES implements ResourceHandlerType {
		FILESYSTEM  {
			@Override
			public boolean isUserType() {
				return true;
			}},
		FTP   {
			@Override
			public boolean isUserType() {
				return true;
			}},
		STREAM {
			@Override
			public boolean isUserType() {
				return false;
			}},
		STATIC {
			@Override
			public boolean isUserType() {
				return false;
			}},
		URL {
			@Override
			public boolean isUserType() {
				return false;
			}}
	};


	/**
	 * Tests if the given resource string is valid for the resource loader instance.
	 * @param resource The resource string for the resource to be loaded.
	 * @return <code>true</code> if this {@link IResourceHandler} instance is abler to handle
	 * 	the given resource.
	 */
	public boolean isValidResource(String resource);

	/**
	 * Creates a new Instance of the {@link IResourceHandler} with the given resource.
	 * Better using {@link ResourceHandlerFactory#getResourceHandler(String)} but
	 * if you're shure you have the right {@link IResourceHandler} instance, it's possible
	 * to create a new one of the same type.
	 *
	 * @param resource The resource to be handled by teh resource Loader.
	 * @return A new {@link IResourceHandler} instance of the same type.
	 * @throws IOException
	 */
	public IResourceHandler createInstance(String resource) throws IOException;

	/**
	 * Gets the resource String which identifies the resource to be loaded.
	 * This can be for example the file path or the url to the resource
	 *
	 * @return The {@link IResourceHandler} string.
	 */
	public String getResourceString();

	/**
	 * Gets the content of the resource handled by this {@link IResourceHandler} instance.
	 *
	 * @return The content of the resource.
	 */
	public byte[] getContent() throws IOException;

	/**
	 * Gets the content of the resource handled by this {@link IResourceHandler} instance.
	 *
	 * @param length the number of bytes to be read and returned.
	 * @return The content of the resource.
	 */
	public byte[] getContent(int length) throws IOException;

	/**
	 * Writes the content of the resource handled by this {@link IResourceHandler} instance.
	 *
	 * @return The content for the resource.
	 */
	public void setContent(byte[] content) throws IOException;
	
	/**
	 * Writes the content of the resource handled by this {@link IResourceHandler} instance.
	 *
	 * @return The content for the resource.
	 */
	public void setContent(CharSequence content) throws IOException;

	/**
	 * Gets an {@link InputStream} for the resource handled by this {@link IResourceHandler} instance.
	 *
	 * @return The an {@link InputStream} to the resource.
	 */
	public ResourceHandlerInputStream getContentInputStream() throws IOException;

	/**
	 * Gets an {@link OutputStream} for the resource handled by this {@link IResourceHandler} instance.
	 *
	 * @param append Tells if the data written to the {@link OutputStream} is to be appended.
	 * @return The an {@link OutputStream} to the resource.
	 */
	public OutputStream getContentOutputStream(boolean append) throws IOException;

	/**
	 * Tells if the resource handled by this {@link IResourceHandler} exists.
	 * @return <code>true</code> if the resource exists and <code>false</code> otherwise.
	 */
	public boolean exists();

	/**
	 * Gets the parent {@link IResourceHandler} . This is the {@link IResourceHandler}
	 * for the next upper path instance.
	 *
	 * @return The parent {@link IResourceHandler} or <code>null</code> if no parent is present.
	 */
	public IResourceHandler getParentResource();

	/**
	 * Writes the given content to the resource.
	 * @throws IOException
	 */
	public void writeStringContent(String content, String codepage) throws IOException;

	/**
	 * Creates all directories described by this {@link IResourceHandler} instance.
	 * @throws IOException
	 */
	public boolean mkdirs() throws IOException;

	/**
	 * Delete the resource handled by this {@link IResourceHandler} instance.
	 * @throws IOException if the resource could not be deleted.
	 */
	public void delete() throws IOException;

	/**
	 * Moves this {@link IResourceHandler} into the user trash.
	 * @return <code>true</code> if moving to trash was successful and <code>false</code> otherwise.
	 * @throws IOException
	 */
	public boolean moveToTrash() throws IOException;

	/**
	 * Tells this {@link IResourceHandler} instance can have child resources.
	 * @return <code>true</code> if it's possible that this {@link IResourceHandler} can have child resources.
	 */
	public boolean isDirectoryResource();

	/**
	 * Tells this {@link IResourceHandler} instance is a file resource.
	 * @return <code>true</code> if it's a file resource or <code>false</code> otherwise.
	 */
	public boolean isFileResource();

	/**
	 * Lists all child resources of this {@link IResourceHandler} instance.
	 * @param filter A filter to be used for filter the result child resources.
	 * 	set it to <code>null</code> for no filter
	 * @return All child resources.
	 * @throws IOException
	 */
	public IResourceHandler[] listResources(ResourceNameFilter filter) throws IOException;

	/**
	 * Lists all resources which could have child resources.
	 *
	 * @return All these {@link IResourceHandler} which have this {@link IResourceHandler} as parent.
	 */
	public IResourceHandler[] listDirectoryResources() throws IOException;

	public IResourceHandler[] listDirectoryResources(boolean hidden) throws IOException;

	/**
	 * Lists all resources which could have child resources.
	 *
	 * @param filter A filter to be used for filter the result child resources.
	 * 	set it to <code>null</code> for no filter
	 * @return All these {@link IResourceHandler} which have this {@link IResourceHandler} as parent.
	 */
	public IResourceHandler[] listDirectoryResources(ResourceNameFilter filter) throws IOException;

	/**
	 * Lists all resources which could not have child resources.
	 *
	 * @return All these {@link IResourceHandler} which have this {@link IResourceHandler} as parent.
	 * @throws IOException
	 */
	public IResourceHandler[] listFileResources() throws IOException;

	/**
	 * Gets the list of shown (i.e. not hidden) files.
	 *
	 * @param showHidden <code>true</code> if hidden files should be also shown and <code>false</code> otherwise.
	 * @return All these {@link IResourceHandler} which have this {@link IResourceHandler} as parent.
	 * @throws IOException
	 */
	public IResourceHandler[] listFileResources(boolean showHidden) throws IOException;

	/**
	 * Gets the name of the resource without the path statement.
	 * @return The name of the resource
	 */
	public String getName();

	/**
	 * Gets the file extension of the file resource. The returned file extension did not contains the dot!
	 * @return The file extension. If no extension is detected, an empty String is returned.
	 */
	public String getFileExtension();

	/**
	 * Gets the file format of the resource handled by this {@link IResourceHandler} instance.
	 * @param force <code>true</code> for taking a look to the magic bytes of the file to detect the file name if necessary.
	 * @return The file format.
	 */
	public String getMimeType(boolean force);

	/**
	 * Tries to determine the byte size of the resource.
	 * @return size in bytes or 0 if the file did not exists.
	 */
	public long size();

	/**
	 * The modification date of the resource.
	 * @return The modification date or <code>null</code> if no modification date is avaiable or supported by the {@link IResourceHandler} implementation.
	 */
	public Date getModifiedAt();

	/**
	 * Moves this {@link IResourceHandler} data to the targetRecourceLoader one.
	 * The source {@link IResourceHandler} will be deleted.
	 *
	 * @param targetRecourceLoader The target {@link IResourceHandler}.
	 * @return <code>true</code> if the movement was successfull and <code>false</code> otherwise.
	 * @throws IOException
	 */
	public void moveTo(IResourceHandler targetRecourceLoader, boolean overwrite) throws IOException;

	/**
	 * Copies this {@link IResourceHandler} data to the targetRecourceLoader one.
	 *
	 * @param targetRecourceLoader The target {@link IResourceHandler}.
	 * @return <code>true</code> if the copy was successfull and <code>false</code> otherwise.
	 * @throws IOException
	 */
	public boolean copyTo(IResourceHandler targetRecourceLoader, boolean overwrite) throws IOException;

	/**
	 * Frees all resources
	 */
	public void dispose();

	/**
	 * clears all caches. This should be invoked if something in the file system has been changed.
	 */
	public void refresh();

	/**
	 * Adds the given statement to the path. The given statement should be formatted in a valid
	 * relative path statement.
	 *
	 * @param statement The path statement to be added
	 * @return A new {@link IResourceHandler} with the given path statement at the end.
	 * @throws ResourceHandlerException if the pathstatement could not be added.
	 */
	public IResourceHandler addPathStatement(String statement) throws ResourceHandlerException;

	/**
	 * Tells if this {@link IResourceHandler} instance file format is an image.
	 * @return <code>true</code> if the resource is an image or <code>false</code> otherwise.
	 */
	public boolean isImageFormat();

	/**
	 * Tells if this {@link IResourceHandler} instance is a remote one, which
	 * does an access outside og this computer.
	 * @return <code>true</code> if it's a remote resource or <code>false</code> otherwise.
	 */
	public boolean isRemoteResource();

    /**
     * Determines if the given file is a root in the navigatable tree(s).
     * Examples: Windows 98 has one root, the Desktop folder. DOS has one root
     * per drive letter, <code>C:\</code>, <code>D:\</code>, etc. Unix has one root,
     * the <code>"/"</code> directory.
     *
     * The default implementation gets information from the <code>ShellFolder</code> class.
     *
     * @return <code>true</code> if this is a root in the navigatable tree.
     * @see FileSystemView#isFileSystemRoot
     */
	public boolean isRoot();

    /**
     * Used by UI classes to decide whether to display a special icon
     * for a floppy disk. Implies isDrive(dir).
     *
     * The default implementation has no way of knowing, so always returns false.
     *
     * @return <code>false</code> always
     */
	public boolean isFloppyDrive();

    /**
     * Returns all root partitions on this system. For example, on
     * Windows, this would be the "Desktop" folder, while on DOS this
     * would be the A: through Z: drives.
     */
	public IResourceHandler[] getRoots();

    /**
     * Name of a file, directory, or folder as it would be displayed in
     * a system file browser. Example from Windows: the "M:\" directory
     * displays as "CD-ROM (M:)"
     *
     * The default implementation gets information from the ShellFolder class.
     *
     * @return the file name as it would be displayed by a native file chooser
     */
	public String getSystemDisplayName();

    /**
     * Creates a new folder with a default folder name.
     * @throws IOException
     */
	public IResourceHandler createNewFolder() throws IOException;

	/**
	 * Gets the type of this {@link IResourceHandler} instance. Only these {@link IResourceHandler} instance
	 * should return a valid type which are selectable
	 *
	 * The available types are listed at
	 * {@link RESOURCE_HANDLER_USER_TYPES}
	 * @return The type.
	 */
	public RESOURCE_HANDLER_USER_TYPES getType();

	/**
	 * Gets a local file for the given ResourceHandler.
	 */
	public File toFile();

	/**
	 * Get all path segments including the file name as the last segment.
	 * @return The path segments. If no path segmentation is supported an empty list is returned.
	 */
	public List<String> getPathSegments();

	/**
	 * Tells if this {@link IResourceHandler} instance is a hidden file
	 */
	public boolean isHidden();

	public void deleteOnExit();

	/**
	 * If this {@link IResourceHandler} instance is a directory it tells if there are any
	 * childs in the directory. If it's a file it tells if the file is empty.
	 * @return <code>true</code> if the directory or file is empty. <code>false</code> if the
	 * directory have childs, or the file has a length larger than 0. Also <code>false</code>
	 * if returned if the {@link IResourceHandler} did not exists.
	 */
	public boolean isEmpty();

}
