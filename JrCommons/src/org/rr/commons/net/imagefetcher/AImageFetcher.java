package org.rr.commons.net.imagefetcher;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;

abstract class AImageFetcher implements IImageFetcher {
	
	protected String searchTerm = null;
	
	/**
	 * HTTP IP echo services.
	 */
	private static final String[] IP_SERVICES = new String[] {"http://api.externalip.net/ip/", "http://ipecho.net/plain", "http://icanhazip.com/"};

	/**
	 * Tries to fetch the external ip address.
	 * @return The external ip adress.
	 * @throws IOException
	 */
	protected static String getExternalIP() throws IOException {
		for (String service : IP_SERVICES) {
			try {
				final IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceLoader(service);
				byte[] content = resourceLoader.getContent();
				if(content != null) {
					String ip = new String(content);
					if(validate(ip)) {
						return ip;
					}
				}
			} catch (Exception e) {
				//continue
			}
		}
		return null;
	}
 
   /**
    * Validate the given ip address by trying to solve it's host name.
    * @param ip ip address to be validated.
    * @return <code>true</code> for a valid ip address, <code>false</code> otherwise.
    */
	protected static boolean validate(final String ip) {
		if(ip == null || ip.trim().isEmpty()) {
			return false;
		}
		
    	try {
    		InetAddress byName = InetAddress.getByName(ip);
    		return byName != null && byName.getHostAddress() != null;
		} catch (UnknownHostException e) {
			return false;
		}    	
    }
	
	@Override
	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}	
	
	public String getSearchTerm() {
		return this.searchTerm;
	}
}
