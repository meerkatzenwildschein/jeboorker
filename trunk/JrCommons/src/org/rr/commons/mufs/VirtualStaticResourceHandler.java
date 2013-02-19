package org.rr.commons.mufs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

/**
 * This is an {@link IResourceHandler} implementation which just provide some
 * childs, specified with the constructor. The {@link VirtualStaticResourceHandler} 
 * is always a directory, which did not exists, only providing some childs. 
 * 
 */
public class VirtualStaticResourceHandler extends AResourceHandler{

	private String name;
	private IResourceHandler[] children;
	private VirtualStaticResourceDataLoader data;
	
	VirtualStaticResourceHandler(String name, IResourceHandler[] children) {
		this.name = name;
		this.children = children;
	}
	
	VirtualStaticResourceHandler(String name, VirtualStaticResourceDataLoader data) {
		this.name = name;
		this.data = data;
	}	
	
	@Override
	public IResourceHandler addPathStatement(String statement)
			throws ResourceHandlerException {
		throw new UnsupportedOperationException("Not Supported");
	}

	@Override
	public IResourceHandler createInstance(String resource) throws IOException {
		throw new UnsupportedOperationException("Not Supported");
	}

	@Override
	public void delete() throws IOException {
	}
	
	@Override
	public boolean moveToTrash() throws IOException {
		return ResourceHandlerUtils.moveToTrash(this);
	}		

	@Override
	public void dispose() {
		
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public ResourceHandlerInputStream getContentInputStream() throws IOException {
		this.cleanHeapIfNeeded(this.data.length());
		ResourceHandlerInputStream in;
		if(this.data != null) {
			in = new ResourceHandlerInputStream(this, this.data.getContentInputStream(), (int) this.data.length());
		} else {
			in = new ResourceHandlerInputStream(this, new ByteArrayInputStream(new byte[0]), 0);
		}
		return in;
	}

	@Override
	public OutputStream getContentOutputStream(boolean append)
			throws IOException {
		throw new UnsupportedOperationException("Not Supported");
	}

	@Override
	public Date getModifiedAt() {
		throw new UnsupportedOperationException("Not Supported");
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public IResourceHandler getParentResource() {
		return null;
	}

	@Override
	public String getResourceString() {
		return this.name;
	}

	@Override
	public RESOURCE_HANDLER_USER_TYPES getType() {
		return RESOURCE_HANDLER_USER_TYPES.STATIC;
	}

	@Override
	public boolean isDirectoryResource() {
		return data==null;
	}

	@Override
	public boolean isRemoteResource() {
		return false;
	}

	@Override
	public boolean isValidResource(String resource) {
		return false;
	}

	@Override
	public IResourceHandler[] listDirectoryResources(ResourceNameFilter filter) throws IOException {
		if(this.children==null) {
			return new IResourceHandler[0];
		}
		
		ArrayList<IResourceHandler> directoryChilds = new ArrayList<IResourceHandler>();
		for (int i = 0; i < children.length; i++) {
			if(children[i].isDirectoryResource()) {
				if(filter != null && filter.accept(children[i])) {
					directoryChilds.add(children[i]);
				} else if(filter == null) {
					directoryChilds.add(children[i]);
				}				
			}
		}
		return directoryChilds.toArray(new IResourceHandler[directoryChilds.size()]);
	}

	@Override
	public IResourceHandler[] listFileResources() throws IOException {
		if(this.children==null) {
			return new IResourceHandler[0];
		}
		
		ArrayList<IResourceHandler> directoryChilds = new ArrayList<IResourceHandler>();
		for (int i = 0; i < children.length; i++) {
			if(children[i].isFileResource()) {
				directoryChilds.add(children[i]);
			}
		}
		return directoryChilds.toArray(new IResourceHandler[directoryChilds.size()]);
	}

	@Override
	public boolean mkdirs() throws IOException {
		return false;
	}

	@Override
	public long size() {
		return 0;
	}

	@Override
	public void writeStringContent(String content, String codepage)
			throws IOException {
	}

}
