package org.rr.commons.mufs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.IOUtils;

public class URLResourceHandler extends AResourceHandler {
	
	URL url;
	
	ArrayList<InputStream> inStream = new ArrayList<InputStream>(2);
	
	ArrayList<OutputStream> outStream = new ArrayList<OutputStream>(2);
	
	URLResourceHandler() {
		super();
	}
	
	private URLResourceHandler(URL url) {
		this.url = url;
	}

	@Override
	public boolean isValidResource(String resource) {
		if(resource != null && resource.startsWith("http://")) {
			try {
				new URL(resource);
				return true;
			} catch (Exception e) {
			}
		}
		return false;
	}

	@Override
	public IResourceHandler createInstance(String resource) throws IOException {
		return new URLResourceHandler(new URL(resource));
	}

	@Override
	public String getResourceString() {
		return url.toString();
	}

	@Override
	public InputStream getContentInputStream() throws IOException {
		URLConnection connection = this.url.openConnection();
		if(connection instanceof HttpURLConnection) {
			((HttpURLConnection) connection).setConnectTimeout(10 * 1000); //10 sec timeout
			((HttpURLConnection) connection).setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
		}
		connection.connect();
		InputStream inputStream = connection.getInputStream();
		inStream.add(inputStream);
		return inputStream;
	}

	@Override
	public OutputStream getContentOutputStream(boolean append) throws IOException {
		URLConnection openConnection = this.url.openConnection();
		OutputStream outputStream = openConnection.getOutputStream();
		this.outStream.add(outputStream);
		return outputStream;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public IResourceHandler getParentResource() {
		return null;
	}

	@Override
	public void writeStringContent(String content, String codepage) throws IOException {
		IOUtils.write(content.getBytes(codepage), this.getContentOutputStream(false));
	}

	@Override
	public boolean mkdirs() throws IOException {
		return false;
	}

	@Override
	public void delete() throws IOException {
	}

	@Override
	public boolean moveToTrash() throws IOException {
		return ResourceHandlerUtils.moveToTrash(this);
	}

	@Override
	public boolean isDirectoryResource() {
		return false;
	}
	
	@Override
	public IResourceHandler[] listDirectoryResources(ResourceNameFilter filter) throws IOException {
		return new IResourceHandler[0];
	}	

	@Override
	public IResourceHandler[] listFileResources() throws IOException {
		return new IResourceHandler[0];
	}

	@Override
	public String getName() {
		return new File(this.url.getFile()).getName();
	}

	@Override
	public long size() {
		return 0;
	}

	@Override
	public Date getModifiedAt() {
		return new Date();
	}

	@Override
	public void dispose() {
		while(!inStream.isEmpty()) {
			IOUtils.closeQuietly(inStream.remove(0));
		}
		while(!outStream.isEmpty()) {
			IOUtils.closeQuietly(outStream.remove(0));
		}
	}

	@Override
	public IResourceHandler addPathStatement(String statement) throws ResourceHandlerException {
		return null;
	}

	@Override
	public boolean isRemoteResource() {
		return false;
	}

	@Override
	public RESOURCE_HANDLER_USER_TYPES getType() {
		return RESOURCE_HANDLER_USER_TYPES.URL;
	}

}
