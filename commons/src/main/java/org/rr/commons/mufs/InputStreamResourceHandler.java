package org.rr.commons.mufs;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.rr.commons.utils.ReflectionUtils;

class InputStreamResourceHandler extends AResourceHandler {
	
	private ResourceHandlerInputStream inputStream;
	
	InputStreamResourceHandler(InputStream inputStream) {
		if(inputStream instanceof BufferedInputStream) {
			try {
				InputStream in = (InputStream) ReflectionUtils.getFieldValue(inputStream, "in", false);
				this.inputStream = new ResourceHandlerInputStream(this, in);
			} catch (Exception e) {
				this.inputStream = new ResourceHandlerInputStream(this, inputStream);
			}
		} else {
			this.inputStream = new ResourceHandlerInputStream(this, inputStream);
		}
	}
	
	@Override
	public IResourceHandler createInstance(String resource) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Delete is not supported at {@link InputStream}.
	 * @return <code>false</code> in any case.
	 */
	@Override
	public void delete() {
	}
	
	@Override
	public boolean moveToTrash() throws IOException {
		return ResourceHandlerUtils.moveToTrash(this);
	}

	@Override
	public void dispose() {
		IOUtils.closeQuietly(this.inputStream);
		this.inputStream = null;
	}

	@Override
	public boolean exists() {
		return this.inputStream != null;
	}

	@Override
	public synchronized byte[] getContent() throws IOException {
		byte[] byteArray = IOUtils.toByteArray(this.inputStream);
		this.inputStream.reset();
		return byteArray;
	}

	@Override
	public ResourceHandlerInputStream getContentInputStream() throws IOException {
		return this.inputStream;
	}

	@Override
	public OutputStream getContentOutputStream(boolean append)
			throws IOException {
		return null;
	}

	@Override
	public String getName() {
		return "InputStreamResource";
	}

	@Override
	public IResourceHandler getParentResource() {
		return null;
	}

	@Override
	public String getResourceString() {
		return "InputStreamResource://";
	}

	@Override
	public boolean isDirectoryResource() {
		return false;
	}

	@Override
	public boolean isValidResource(String resource) {
		return false;
	}
	
	@Override
	public IResourceHandler[] listDirectoryResources(ResourceNameFilter filter) {
		return new IResourceHandler[0];
	}

	@Override
	public IResourceHandler[] listFileResources() {
		return new IResourceHandler[0];
	}

	@Override
	public IResourceHandler[] listResources(ResourceNameFilter filter) {
		return new IResourceHandler[0];
	}

	@Override
	public boolean mkdirs() {
		return false;
	}

	@Override
	public void writeStringContent(String content, String codepage)
			throws IOException {
	}

	/**
	 * Size could not be determined on {@link InputStream}s
	 */
	@Override
	public long size() {
		return -1;
	}

	/**
	 * There is no modification date available at the {@link InputStreamResourceHandler}.
	 * @return always <code>null</code>.
	 */
	@Override
	public Date getModifiedAt() {
		return null;
	}

	@Override
	public IResourceHandler addPathStatement(String statement) throws ResourceHandlerException {
		return this;
	}
	
	/**
	 * @return <code>false</code> in any case because it could not be surly determined if the
	 * {@link InputStream} handled by this {@link InputStreamResourceHandler} instance
	 * is a local one or not.
	 */
	public boolean isRemoteResource() {
		return false;
	}

	/**
	 * The {@link InputStreamResourceHandler} could not created by the user.
	 * The {@link InputStreamResourceHandler} is only be used internally to provide
	 * some data which are for example be fetched from the database.
	 */
	@Override
	public RESOURCE_HANDLER_USER_TYPES getType() {
		return null;
	}
}
