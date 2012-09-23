package org.rr.commons.mufs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.rr.commons.collection.VolatileHashMap;


public class ResourceHandlerFactory {
	
	/**
	 * cache a limited number of {@link IResourceHandler} instances.
	 */
	private static final VolatileHashMap<String, IResourceHandler> resourceHandlerCache = new VolatileHashMap<String, IResourceHandler>(100,100);
	
	private static IResourceHandler userHome = null;
	
	/**
	 * reference instances.
	 */
	private static final IResourceHandler[] resourceLoader = new IResourceHandler[] {
		new FileResourceHandler(), new FTPResourceHandler()
	};
	
	/**
	 * Creates a virtual resource handler which handles only the given childs.
	 * @param name The name of the virtual resource
	 * @param childs The childs to be provided by the virtual resource loader.
	 * @return The desired virtual resource handler instance.
	 */
	public static IResourceHandler getVirtualResourceLoader(String name, final IResourceHandler[] childs) {
		return new VirtualStaticResourceHandler(name, childs);
	}
	
	/**
	 * Creates a virtual resource handler which handles only the given childs.
	 * @param name The name of the virtual resource
	 * @param childs The childs to be provided by the virtual resource loader.
	 * @return The desired virtual resource handler instance.
	 */
	public static IResourceHandler getVirtualResourceLoader(String name, final byte[] content) {
		return new VirtualStaticResourceHandler(name, new VirtualStaticResourceDataLoader() {
			
			@Override
			public InputStream getContentInputStream() {
				return new ByteArrayInputStream(content);
			}
		});
	}	
	
	/**
	 * Creates a virtual resource handler which handles only the given childs.
	 * @param name The name of the virtual resource
	 * @param childs The childs to be provided by the virtual resource loader.
	 * @return The desired virtual resource handler instance.
	 */
	public static IResourceHandler getVirtualResourceLoader(String name, final VirtualStaticResourceDataLoader content) {
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
	public static IResourceHandler getResourceLoader(InputStream inputStream) {
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
	public static IResourceHandler getResourceLoader(final File file) {
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
	public static IResourceHandler getResourceLoader(URL url) {
		return getResourceLoader(url.toString());
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
		IResourceHandler resourceLoader = getResourceLoader(sibling);
		return getTemporaryResourceLoader(resourceLoader, extension);
	}

	/**
	 * Get a {@link IResourceHandler} instance with the given, additional extension. The
	 * result {@link IResourceHandler} will not exists. if the sibling the the extension already
	 * exists a number will be added at the end of the result {@link IResourceHandler}.
	 * 
	 * @param sibling the sibling {@link IResourceHandler}.
	 * @param extension the extension for the sibling {@link IResourceHandler}.
	 * @return The sibling {@link IResourceHandler}.
	 */
	public static IResourceHandler getTemporaryResourceLoader(IResourceHandler sibling, final String extension) {
		final String tmpFileName = sibling.toString() + "." + extension;
		
		int extensionNum = 0;
		IResourceHandler result = null;
		while( (result = getResourceLoader(tmpFileName + (extensionNum != 0 ? extensionNum : "") )).exists() ) {
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
	public static IResourceHandler getResourceLoader(final String resource) {
		if(resource == null || resource.length()==0) {
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
	public static boolean hasResourceLoader(final String resource) {
		IResourceHandler loader = getResourceLoader(resource);
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
			userHome = getResourceLoader(userHomeProp);
		}
		return userHome;
	}
	
}
