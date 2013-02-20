package org.rr.commons.mufs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import org.rr.commons.collection.VolatileHashMap;


public class ResourceHandlerFactory {
	
	/**
	 * cache a limited number of {@link IResourceHandler} instances.
	 */
	private static final VolatileHashMap<String, IResourceHandler> resourceHandlerCache = new VolatileHashMap<String, IResourceHandler>(100,100);
	
	private static IResourceHandler userHome = null;
	
	private static final ArrayList<IResourceHandler> temporaryResourceLoader = new ArrayList<IResourceHandler>();
	
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
	static void deleteTemporaryResources() {
		shutdownThread.run();
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
	 * result {@link IResourceHandler} will not exists. if the sibling the the extension already
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

	/**
	 * Get a {@link IResourceHandler} instance with the given, additional extension. If the sibling 
	 * with the desired extension already exists a number will be attached at the end of the filename.
	 * 
	 * @param sibling the sibling {@link IResourceHandler}.
	 * @param extension the extension for the sibling {@link IResourceHandler}.
	 * @return The sibling {@link IResourceHandler}.
	 */
	public static IResourceHandler getUniqueResourceHandler(IResourceHandler sibling, final String extension) {
		String siblingString = sibling.toString();
		if(siblingString.lastIndexOf('.') != -1) {
			siblingString = siblingString.substring(0, siblingString.lastIndexOf('.'));
		}
		
		int extensionNum = 0;
		IResourceHandler result = null;
		while( (result = getResourceHandler(siblingString + (extensionNum != 0 ? "_" + extensionNum : "") + "." + extension)).exists() ) {
			extensionNum ++;
		}
		return result;
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
	 * Gets the {@link IResourceHandler} instance for the users home directory.
	 * @return The desired user home {@link IResourceHandler}
	 */
	public static IResourceHandler getUserHomeResourceLoader() {
		if(userHome==null) {
			String userHomeProp = System.getProperty("user.name");
			userHome = getResourceHandler(userHomeProp);
		}
		return userHome;
	}
	
}
