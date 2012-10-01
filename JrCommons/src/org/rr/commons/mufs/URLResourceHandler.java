package org.rr.commons.mufs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import org.apache.commons.io.IOUtils;

public class URLResourceHandler extends AResourceHandler {
	
	URL url;
	
	InputStream inStream = null;
	
	OutputStream outStream = null;
	
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
		if(inStream == null) {
			this.inStream = this.url.openStream();
		}
		return this.inStream;
	}

	@Override
	public OutputStream getContentOutputStream(boolean append) throws IOException {
		if(this.outStream == null) {
			URLConnection openConnection = this.url.openConnection();
			this.outStream = openConnection.getOutputStream();
		}
		return this.outStream;
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
	public IResourceHandler[] listDirectoryResources() throws IOException {
		return new IResourceHandler[0];
	}

	@Override
	public IResourceHandler[] listFileResources() throws IOException {
		return new IResourceHandler[0];
	}

	@Override
	public String getName() {
		return this.url.getFile();
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
		if(this.inStream != null) {
			IOUtils.closeQuietly(this.inStream);
		}
		if(this.outStream != null) {
			IOUtils.closeQuietly(this.outStream);
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
