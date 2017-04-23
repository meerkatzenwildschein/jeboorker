package org.rr.commons.mufs;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.rr.commons.collection.VolatileHashMap;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtil;

public class ResourceHandlerFactory {

	/**
	 * cache a limited number of {@link IResourceHandler} instances.
	 */
	private static final VolatileHashMap<String, IResourceHandler> resourceHandlerCache = new VolatileHashMap<String, IResourceHandler>(100,100);

	private static IResourceHandler userHome = null;

	private static final ArrayList<IResourceHandler> temporaryResourceLoader = new ArrayList<>();

	private static final Thread shutdownThread = new Thread(new Runnable() {
		@Override
		public void run() {
			//stores all loaded which could not be deleted.
			final ArrayList<IResourceHandler> newTemporaryResourceLoader = new ArrayList<>();
			IResourceHandler resourceHandler = null;
			for (int i = 0; i < temporaryResourceLoader.size(); i++) {
				try {
					resourceHandler = temporaryResourceLoader.get(i);

					//asking for existence throws an exception during shutdown under windows.
					resourceHandler.delete();
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
	
	public static IResourceHandler getTemporaryResourceFolder(String extension) {
		File tmp = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString() + (extension != null ? "." + extension : ".tmp"));
		if (tmp.mkdirs()) {
			IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceHandler(tmp);
			temporaryResourceLoader.add(resourceLoader);
			return resourceLoader;
		}
		throw new RuntimeException("Failed to create dir " + tmp.getAbsolutePath());
	}

	/**
	 * Creates a {@link IResourceHandler} file which points to a temporary file which is automatically deleted
	 * at application end.
	 *
	 * @param extension The file extension of the temporary file without the dot.
	 */
	public static IResourceHandler getTemporaryResource(String extension) {
		try {
			File tmp = File.createTempFile(UUID.randomUUID().toString(), extension != null ? "." + extension : ".tmp");
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
	static void removeTemporaryResource(IResourceHandler handler) {
		temporaryResourceLoader.remove(handler);
	}

	/**
	 * reference instances.
	 */
	private static final IResourceHandler[] resourceLoader = new IResourceHandler[] {
		new FileResourceHandler(), new FTPResourceHandler(), new URLResourceHandler()
	};

	/**
	 * Creates a virtual resource handler which handles only the given childs.
	 * @param name The name of the virtual resource
	 * @param childs The childs to be provided by the virtual resource loader.
	 * @return The desired virtual resource handler instance.
	 */
	public static IResourceHandler getVirtualResourceHandler(String name, final IResourceHandler[] childs) {
		return new VirtualStaticResourceHandler(name, childs);
	}

	/**
	 * Creates a virtual resource handler which handles only the given childs.
	 * @param name The name of the virtual resource
	 * @param childs The childs to be provided by the virtual resource loader.
	 * @return The desired virtual resource handler instance.
	 */
	public static IResourceHandler getVirtualResourceHandler(String name, final byte[] content) {
		return new VirtualStaticResourceHandler(name, new VirtualStaticResourceDataLoader() {

			@Override
			public InputStream getContentInputStream() {
				return new ByteArrayInputStream(content);
			}

			@Override
			public long length() {
				return content.length;
			}
		});
	}

	/**
	 * Creates a virtual resource handler which handles only the given childs.
	 * @param name The name of the virtual resource
	 * @param childs The childs to be provided by the virtual resource loader.
	 * @return The desired virtual resource handler instance.
	 */
	public static IResourceHandler getVirtualResourceHandler(String name, final VirtualStaticResourceDataLoader content) {
		return new VirtualStaticResourceHandler(name, content);
	}

	/**
	 * Gets a resource loader for the given {@link InputStream}. This is helpful if there is only
	 * an {@link InputStream} available which contains data to be shown to components working with {@link IResourceHandler}
	 * objects.
	 *
	 * @param inputStream The resource handled by the {@link IResourceHandler}
	 * @return The desired {@link IResourceHandler} instance.
	 */
	public static IResourceHandler getResourceHandler(InputStream inputStream) {
		if(inputStream == null) {
			throw new NullPointerException("could not load null resource");
		}

		return new InputStreamResourceHandler(inputStream);
	}
	
	/**
	 * Gets a resource loader for the given <code>bytes</code>. This is helpful if there is only
	 * bytes available which contains data to be shown to components working with {@link IResourceHandler}
	 * objects.
	 *
	 * @param bytes The bytes handled by the {@link IResourceHandler}
	 * @return The desired {@link IResourceHandler} instance.
	 */
	public static IResourceHandler getResourceHandler(byte[] bytes) {
		if(bytes == null) {
			throw new NullPointerException("could not load null resource");
		}

		return new InputStreamResourceHandler(new ByteArrayInputStream(bytes));
	}

	/**
	 * Get a new {@link IResourceHandler} with the given {@link IResourceHandler} as parent and the
	 * <code>file</code> as child.
	 * @param parent The parent {@link IResourceHandler} instance.
	 * @param file The child attached to the parent.
	 * @return The desired {@link IResourceHandler}. Please note that not all kind of {@link IResourceHandler} shall support this.
	 */
	public static IResourceHandler getResourceHandler(IResourceHandler parent, String file) {
		String parentResourceString = parent.getResourceString();
		if(!parentResourceString.endsWith("/") && !parentResourceString.endsWith("\\") &&
				!parentResourceString.endsWith(File.separator)) {
			parentResourceString += File.separator;
		}
		return getResourceHandler(parentResourceString + file);
	}

	/**
	 * Gets a resource loader for the given {@link File}.
	 *
	 * @param file The resource handled by the {@link IResourceHandler}
	 * @return The desired {@link IResourceHandler} instance.
	 */
	public static IResourceHandler getResourceHandler(final File file) {
		if(file == null) {
			throw new NullPointerException("could not load null resource");
		}

		return new FileResourceHandler(file);
	}

	/**
	 * Gets a resource loader for the given resource.
	 *
	 * @param url The URL handled by the {@link IResourceHandler}
	 * @return The desired {@link IResourceHandler} instance or <code>null</code> if no {@link IResourceHandler}
	 * available for the given resource string.
	 */
	public static IResourceHandler getResourceHandler(URL url) {
		return getResourceHandler(url.toString());
	}

	/**
	 * Get a {@link IResourceHandler} instance with the given, additional extension. The
	 * result {@link IResourceHandler} will not exists. If the sibling with the extension already
	 * exists a number will be added at the end of the result {@link IResourceHandler}.
	 *
	 * @param sibling the sibling path.
	 * @param extension the extension for the sibling {@link IResourceHandler}.
	 * @return The sibling {@link IResourceHandler}.
	 */
	public static IResourceHandler getTemporaryResourceLoader(String sibling, final String extension) {
		IResourceHandler resourceLoader = getResourceHandler(sibling);
		return getUniqueResourceHandler(resourceLoader, extension);
	}

	public static IResourceHandler getUniqueResourceHandler(final IResourceHandler sibling, String extension) {
		return getUniqueResourceHandler(sibling, null, extension);
	}
	
	/**
	 * Get a {@link IResourceHandler} instance with the given, additional extension. If the sibling
	 * with the desired extension already exists a number will be attached at the end of the filename.
	 *
	 * @param sibling the sibling {@link IResourceHandler}.
	 * @param extension the extension for the sibling {@link IResourceHandler} without the dot separator.
	 * @return The sibling {@link IResourceHandler}.
	 */
	public static IResourceHandler getUniqueResourceHandler(final IResourceHandler sibling, String addition, String extension) {
		if(extension == null) {
			extension = sibling.getFileExtension();
		}

		String siblingString = sibling.toString();
		if(siblingString.lastIndexOf('.') != -1) {
			siblingString = siblingString.substring(0, siblingString.lastIndexOf('.'));
		}
		
		//remove existing counter to avoid chained counter extensions.
		siblingString = siblingString.replaceAll("_\\d*$", EMPTY);

		int extensionNum = 0;
		IResourceHandler result = null;
		while( (result = getResourceHandler(siblingString + (addition != null ? "_" + addition : EMPTY) + (extensionNum != 0 ? "_" + extensionNum : EMPTY) + "." + extension)).exists() ) {
			extensionNum ++;
		}
		return result;
	}

	/**
	 * Get those {@link IResourceHandler} which are previously created with the {@link #getUniqueResourceHandler(IResourceHandler, String)}
	 * method.
	 *
	 * @param sibling the sibling {@link IResourceHandler}.
	 * @param extension the extension for the sibling {@link IResourceHandler}.
	 * @return The {@link IResourceHandler} found. Never returns null.
	 */
	public static List<IResourceHandler> getExistingUniqueResourceHandler(IResourceHandler sibling, final String extension) {
		String siblingString = sibling.toString();
		if(siblingString.lastIndexOf('.') != -1) {
			siblingString = siblingString.substring(0, siblingString.lastIndexOf('.'));
		}

		int extensionNum = 0;
		List<IResourceHandler> resultList = new ArrayList<>();
		IResourceHandler result = null;
		while( (result = getResourceHandler(siblingString + (extensionNum != 0 ? "_" + extensionNum : EMPTY) + "." + extension)).exists() ) {
			resultList.add(result);
			extensionNum ++;
		}
		return resultList;
	}

	@SuppressWarnings("unchecked")
	public static List<IResourceHandler> getResourceHandler(final Transferable t) throws IOException, ClassNotFoundException {
		List<Object> transferedFiles = Collections.emptyList();

		if(t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			String data;
			try {
				data = (String) t.getTransferData(DataFlavor.stringFlavor);
				transferedFiles = getFileList(data, 100);
			} catch (UnsupportedFlavorException e) {
			}
		} else if(t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			try {
				transferedFiles = (List<Object>) t.getTransferData(DataFlavor.javaFileListFlavor);
			} catch (UnsupportedFlavorException e) {
			}
		} else if(t.isDataFlavorSupported(new DataFlavor("text/uri-list"))) {
			try {
				ByteArrayInputStream in = (ByteArrayInputStream) t.getTransferData(new DataFlavor("text/uri-list"));
				String data = IOUtils.toString(in);
				String[] uriList = data.split("\n");
				transferedFiles = new ArrayList<>(uriList.length - 1);
				for(int i = 0; i < uriList.length; i++) {
					if(uriList[i].equals("copy") || uriList[i].equals("move")) {
						continue;
					}
					transferedFiles.add(uriList[i]);
				}
			} catch (UnsupportedFlavorException e) {
			}
		}
		ArrayList<IResourceHandler> result = new ArrayList<>();

		for(Object file : transferedFiles) {
			if(file instanceof File) {
				result.add(getResourceHandler((File) file));
			} else if(file instanceof String) {
				result.add(getResourceHandler((String) file));
			}
		}
		return result;
	}

	/**
	 * Tests if there is someting in the given {@link Transferable} that could be
	 * fetched as {@link IResourceHandler}.
	 * @return <code>true</code> if there is something that could be handled by an {@link IResourceHandler} instance
	 * and <code>false</code> otherwise.
	 */
	public static boolean hasResourceHandler(final Transferable contents) {
		if(contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			return true;
		} else if(contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				String data = (String) contents.getTransferData(DataFlavor.stringFlavor);
				List<Object> fileList = getFileList(data, 10);
				return !fileList.isEmpty();
			} catch (Exception e) {
				return false;
			}
		} else
			try {
				if(contents.isDataFlavorSupported(new DataFlavor("text/uri-list"))) {
					return true;
				}
			} catch (Exception e) {
			}
		return false;
	}

	/**
	 * Gets a resource loader for the given resource.
	 *
	 * @param resource The resource handled by the {@link IResourceHandler}
	 * @return The desired {@link IResourceHandler} instance or <code>null</code> if no {@link IResourceHandler}
	 * available for the given resource string.
	 */
	public static IResourceHandler getResourceHandler(final String resource) {
		if(resource == null || resource.isEmpty()) {
			return null;
		}

		synchronized(resourceHandlerCache) {
			IResourceHandler resourceHandler = resourceHandlerCache.get(resource);
			if(resourceHandler!=null) {
				return resourceHandler;
			}

			for (int i = 0; i < resourceLoader.length; i++) {
				if(resourceLoader[i].isValidResource(resource)) {
					try {
						IResourceHandler createdResourcehandlerInstance = resourceLoader[i].createInstance(resource);
						resourceHandlerCache.put(resource, createdResourcehandlerInstance);
						return createdResourcehandlerInstance;
					} catch (Exception e) {
						return null;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Tests if a loader is available for the given resource.
	 * @param resource The resource string to be tested.
	 * @return <code>true</code> if a resourceloader could be created from the given string and <code>false</code> otherwise.
	 */
	public static boolean hasResourceHandler(final String resource) {
		IResourceHandler loader = getResourceHandler(resource);
		return loader!=null;
	}

	public static void clearCache() {
		resourceHandlerCache.clear();
	}

	/**
	 * The given {@link IResourceHandler} instance will be deleted on application exit.
	 * @param resourceHandler The {@link IResourceHandler} that should be deleted on application shutdown.
	 */
	public static void deleteOnExit(IResourceHandler resourceHandler) {
		temporaryResourceLoader.add(resourceHandler);
	}

	/**
	 * Gets the {@link IResourceHandler} instance for the users home directory.
	 * @return The desired user home {@link IResourceHandler}
	 */
	public static IResourceHandler getUserHomeResourceLoader() {
		if(userHome == null) {
			String userHomeProp = System.getProperty("user.name");
			userHome = getResourceHandler(userHomeProp);
		}
		return userHome;
	}

	/**
	 * Splits the given Data string into a list of files.
	 * @param data The data to be splitted into a file list.
	 * @param invalid The number of non existing file entries before the list creation gets aborted.
	 * @return The list of files. Never returns <code>null</code>.
	 */
	private static List<Object> getFileList(String data, int invalid) {
		int invalidCount = 0;
		ArrayList<Object> result = new ArrayList<>();
		data = data.replace("\r", EMPTY);
		List<String> splitData = ListUtils.split(data, '\n');
		for (String splitDataItem : splitData) {
			if (!StringUtil.toString(splitDataItem).trim().isEmpty()) {
				try {
					File file;
					if(splitDataItem.indexOf(":") != -1) {
						file = new File(new URI(splitDataItem));
					} else {
						file = new File(splitDataItem);
					}

					if (file.isFile()) {
						result.add(file);
					} else if(!file.isDirectory()) {
						invalidCount ++;
						if(invalidCount > invalid) {
							break;
						}
					}
				} catch (URISyntaxException e) {
//					LoggerFactory.getLogger().log(Level.INFO, "No valid file " + splitDataItem);
				}
			}
		}
		return result;
	}

}
