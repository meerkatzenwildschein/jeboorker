package org.rr.commons.mufs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class FTPResourceHandler extends AResourceHandler {

	/**
	 * The url to the resource. examply ftp://username:password@ftp.whatever.com/file.zip;type=i
	 */
	private URL ftpURL;

	/**
	 * Contains the parent directory for this {@link FTPResourceHandler} instance.
	 */
	private FTPResourceHandler parentResourceHandler;

	private FTPFile ftpFile;

	/**
	 * the cached child directories.
	 */
	private IResourceHandler[] childDirectories = null;

	/**
	 * the cached child files.
	 */
	private IResourceHandler[] childFiles = null;

	/**
	 * tells if this resource exists. <code>null</code> if this flag isn't initialized.
	 */
	private Boolean exists;

	private static Boolean apacheFTPAvailable = null;
	
	FTPResourceHandler() {
		super();
	}

	public FTPResourceHandler(final String resource) throws IOException {
		super();
		// ftp://username:password@ftp.whatever.com/file.zip;type=i
		try {
			String resourceString = resource;
			if(resourceString.indexOf(';')!=-1) {
				resourceString = resourceString.substring(0, resourceString.indexOf(';'));
			}
			this.ftpURL = new URL(resourceString);
		} catch (MalformedURLException e) {
			throw new RuntimeException("could not create FTPResourceHandler for " + resource, e);
		}

	}

	public FTPResourceHandler(URL ftpURL) throws IOException {
		super();
		this.ftpURL = ftpURL;
	}

	public FTPResourceHandler(URL ftpURL, FTPResourceHandler parent, FTPFile ftpFile) {
		super();
		this.ftpURL = ftpURL;
		this.ftpFile = ftpFile;
	}

	/**
	 * Creates a new FTPResourceHandler instance.
	 * 
	 * @throws IOException
	 */
	@Override
	public IResourceHandler createInstance(String resource) throws IOException {
		return new FTPResourceHandler(resource);
	}

	/**
	 * Delete the resource handled by this {@link IResourceHandler} instance.
	 * 
	 * @throws IOException
	 *             if the resource could not be deleted.
	 */
	@Override
	public void delete() throws IOException {
		final String path = getURLPath(this.ftpURL);

		try {
			FTPClient connection = this.getConnection();
			try {
				boolean result;
				if (this.isDirectoryResource()) {
					result = connection.removeDirectory(path);
				} else {
					result = connection.deleteFile(path);
				}

				if (result == false) {
					throw new IOException("could not delete resource " + String.valueOf(ftpURL));
				}

				// set the exists flag. The resource is deleted now.
				this.exists = Boolean.FALSE;
			} finally {
				releaseConnection(connection);
			}
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("could not delete resource " + String.valueOf(ftpURL), e);
		}
	}

	/**
	 * Frees all resources
	 */
	@Override
	public void dispose() {
		this.ftpFile = null;
		this.childDirectories = null;
		this.childFiles = null;
		this.exists = null;
	}

	/**
	 * Tells if the resource handled by this {@link IResourceHandler} exists.
	 * 
	 * @return <code>true</code> if the resource exists and <code>false</code> otherwise.
	 */
	@Override
	public boolean exists() {
		if (this.exists != null) {
			return this.exists.booleanValue();
		}

		// the root should always exists
		if (this.getParentResource() == null) {
			this.exists = Boolean.TRUE;
			return true;
		}

		if (this.isDirectoryResource()) {
			this.exists = Boolean.TRUE;
			return true;
		} else {
			try {
				IResourceHandler[] listFileResources = this.getParentResource().listFileResources();
				for (int i = 0; i < listFileResources.length; i++) {
					if (listFileResources[i].getName().equals(this.getName())) {
						this.exists = Boolean.TRUE;
						return true;
					}
				}
			} catch (IOException e) {
				this.exists = Boolean.FALSE;
				return false;
			}
		}

		this.exists = Boolean.FALSE;
		return false;
	}

	/**
	 * Gets an {@link InputStream} for the resource handled by this {@link IResourceHandler} instance.
	 * 
	 * @return The an {@link InputStream} to the resource.
	 */
	@Override
	public InputStream getContentInputStream() throws IOException {
		return new InputStream() {

			private InputStream retrieveFileStream = null;

			private FTPClient connection = null;

			/**
			 * Creates and setup the stream to the ftp socket.
			 */
			{
				IOException lastException = null;
				for (int i = 0; i < FTPConnectionManager.MAX_CONNECTIONS + 1; i++) {
					connection = getConnection();
					connection.setFileType(FTP.BINARY_FILE_TYPE);

					// the result can be null. thats the reason while handle this in a loop.
					try {
						retrieveFileStream = connection.retrieveFileStream(getURLPath(ftpURL));
					} catch (IOException e) {
						retrieveFileStream = null;
						lastException = e;
					}
					if (retrieveFileStream != null) {
						break;
					} else {
						disposeConnection(connection);
						continue;
					}
				}
				if (retrieveFileStream == null && lastException != null) {
					throw lastException;
				} else if (retrieveFileStream == null) {
					throw new IOException("could not fetch input stream for connection " + ftpURL);
				}
			}

			@Override
			public int read() throws IOException {
				return retrieveFileStream.read();
			}

			@Override
			public void close() throws IOException {
				try {
					// close stream
					IOUtils.closeQuietly(retrieveFileStream);

					// complete ftp protocoll transfer
					connection.completePendingCommand();
				} finally {
					// give the connection back for reusing
					releaseConnection(connection);
				}
			}
		};
	}

	@Override
	public OutputStream getContentOutputStream(final boolean append) throws IOException {
		if (!append && this.exists()) {
			this.delete();
		}

		return new OutputStream() {

			private OutputStream storeFileStream = null;

			private FTPClient connection = null;

			/**
			 * Creates and setup the stream to the ftp socket.
			 */
			{
				IOException lastException = null;
				for (int i = 0; i < FTPConnectionManager.MAX_CONNECTIONS + 1; i++) {
					connection = getConnection();
					connection.setFileType(FTP.BINARY_FILE_TYPE);

					try {
						// the result can be null. thats the reason while handle this in a loop.
						if (append) {
							storeFileStream = connection.appendFileStream(getURLPath(ftpURL));
						} else {
							storeFileStream = connection.storeFileStream(getURLPath(ftpURL));
						}
					} catch (IOException e) {
						storeFileStream = null;
						lastException = e;
					}

					if (storeFileStream != null) {
						break;
					} else {
						disposeConnection(connection);
						continue;
					}
				}
				if (storeFileStream == null && lastException != null) {
					throw lastException;
				} else if (storeFileStream == null) {
					throw new IOException("could not fetch output stream for connection " + ftpURL);
				}
			}

			@Override
			public void flush() throws IOException {
				storeFileStream.flush();
			}

			@Override
			public void write(int b) throws IOException {
				storeFileStream.write(b);
			}

			@Override
			public void close() throws IOException {
				try {
					// reset the exists flag. It should be testes again next time if the resource exists.
					exists = null;

					// flush the content
					try {
						storeFileStream.flush();
					} catch (Exception e) {
					}

					// close stream
					IOUtils.closeQuietly(storeFileStream);

					// complete ftp protocoll transfer
					connection.completePendingCommand();
				} finally {
					// give the connection back for reusing
					releaseConnection(connection);
				}
			}
		};
	}

	@Override
	public String getName() {
		String path = getURLPath(this.ftpURL);
		String name = new File(path).getName();
		if(name==null || name.trim().length()==0 && isRoot()) {
			return "/";
		}
		return name;
	}

	@Override
	public IResourceHandler getParentResource() {
		if (this.parentResourceHandler == null) {
			try {
				URL parentURL = getParentURL(this.ftpURL);
				if (parentURL == null) {
					return null;
				}
				this.parentResourceHandler = new FTPResourceHandler(parentURL);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return this.parentResourceHandler;
	}

	/**
	 * Returns the resource String for this ftp resource. for example <code>ftp://BENUTZERNAME:PASSWORT@HOST:PORT/DateiMitPfadangabe</code>
	 */
	@Override
	public String getResourceString() {
		return this.ftpURL.toString();
	}

	/**
	 * Determines if the resource handled by this {@link FTPResourceHandler} is a directory resource which could have children.
	 * 
	 * @return <code>true</code> if it's a dir or <code>false</code> otherwise.
	 */
	@Override
	public boolean isDirectoryResource() {
		if (getParentResource() == null) {
			return true; // it's the main directory
		}

		try {
			FTPFile file = getFTPFile(ftpURL);
			if (file == null) {
				return true;
			}
			return file.isDirectory();
		} catch (Exception e) {
			//not exists
			//Logging.log(Level.WARNING, this, "error occures while find out if the resource is a directory resource for " + String.valueOf(ftpURL), e);
			return false;
		}
	}

	/**
	 * Determines if the given resource string is an ftp resource string. An ftp resource String could look as follow:
	 * <code>ftp://BENUTZERNAME:PASSWORT@HOST:PORT/DateiMitPfadangabe</code>
	 */
	@Override
	public boolean isValidResource(String resource) {
		if (resource.toLowerCase().startsWith("ftp://")) {
			if(!isApacheNetAvailable()) {
				return false;
			}
			
			try {
				new URL(resource);
			} catch (Exception e) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Tests if the apache common net framework is available.
	 * @return <code>true</code> if the framework is available and <code>false</code>
	 * 	otherwise.
	 */
	private boolean isApacheNetAvailable() {
		if(apacheFTPAvailable != null) {
			return apacheFTPAvailable.booleanValue();
		}
		
		try {
			Class.forName("org.apache.commons.net.ftp.FTP");
			apacheFTPAvailable = Boolean.TRUE;
			return true;
		} catch (ClassNotFoundException e1) {
			apacheFTPAvailable = Boolean.FALSE;
			return false;
		}
	}

	@Override
	public IResourceHandler[] listDirectoryResources() throws IOException {
		if (this.childDirectories == null) {
			FTPClient connection = this.getConnection();
			try {
				final FTPFile[] listDirectories = connection.listFiles(getURLPath(this.ftpURL));
				ArrayList<IResourceHandler> result = new ArrayList<IResourceHandler>();
				for (int i = 0; i < listDirectories.length; i++) {
					if (listDirectories[i].isDirectory() && !listDirectories[i].getName().equals(".") && !listDirectories[i].getName().equals("..")) {
						FTPResourceHandler newResourceHandler = new FTPResourceHandler(addURLPath(this.ftpURL, listDirectories[i].getName()), this,
								listDirectories[i]);
						result.add(newResourceHandler);
					}
				}

				this.childDirectories = result.toArray(new IResourceHandler[result.size()]);
			} finally {
				releaseConnection(connection);
			}
		}
		return this.childDirectories;
	}

	@Override
	public IResourceHandler[] listFileResources() throws IOException {
		if (this.childFiles == null) {
			FTPClient connection = this.getConnection();
			try {
				final FTPFile[] listDirectories = connection.listFiles(getURLPath(this.ftpURL));

				ArrayList<IResourceHandler> result = new ArrayList<IResourceHandler>();
				for (int i = 0; i < listDirectories.length; i++) {
					if (listDirectories[i].isFile()) {
						FTPResourceHandler newResourceHandler = new FTPResourceHandler(addURLPath(this.ftpURL, listDirectories[i].getName()), this,
								listDirectories[i]);
						result.add(newResourceHandler);
					}
				}

				this.childFiles = result.toArray(new IResourceHandler[result.size()]);
			} finally {
				releaseConnection(connection);
			}
		}
		return this.childFiles;
	}

	@Override
	public boolean mkdirs() throws IOException {
		ArrayList<FTPResourceHandler> hierarchy = new ArrayList<FTPResourceHandler>();
		FTPResourceHandler parent = (FTPResourceHandler) this.getParentResource();
		while (parent != null) {
			hierarchy.add(parent);
			parent = (FTPResourceHandler) parent.getParentResource();
		}
		
		//reverse
		Arrays.sort(hierarchy.toArray(new FTPResourceHandler[hierarchy.size()]), Collections.reverseOrder());

		FTPClient connection = this.getConnection();
		for (int i = 0; i < hierarchy.size(); i++) {
			if (!hierarchy.get(i).exists()) {
				try {
					connection.makeDirectory(getURLPath(hierarchy.get(i).ftpURL));
				} finally {
					releaseConnection(connection);
				}
			}
		}

		return true;
	}

	@Override
	public long size() {
		if (getParentResource() == null) {
			return 0; // it's the main directory
		}

		try {
			final FTPFile file = getFTPFile(ftpURL);
			if (file == null || file.isDirectory()) {
				return 0;
			}
			return file.getSize();
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public void writeStringContent(String content, String codepage) throws IOException {
		byte[] bytes = content.getBytes(Charset.forName(codepage));
		OutputStream contentOutputStream = this.getContentOutputStream(false);
		IOUtils.write(bytes, contentOutputStream);
		contentOutputStream.flush();
		IOUtils.closeQuietly(contentOutputStream);
	}

	@Override
	public void moveTo(IResourceHandler targetRecourceLoader, boolean overwrite) throws IOException {
		// handle overwrite
		if (!overwrite && targetRecourceLoader.exists()) {
			return;
		}

		if (targetRecourceLoader instanceof FTPResourceHandler) {
			// test if the source and the target are on the same host.
			if (ftpURL.getHost().equals(((FTPResourceHandler) targetRecourceLoader).ftpURL.getHost())) {
				FTPClient connection = this.getConnection();
				try {
					connection.rename(getURLPath(this.ftpURL), getURLPath(((FTPResourceHandler) targetRecourceLoader).ftpURL));
				} finally {
					releaseConnection(connection);
				}
			}
		}

		// use the stream move and delete if the resources are not at the same host
		super.moveTo(targetRecourceLoader, overwrite);
	}

	/**
	 * clears the cache. The file listing must be reread if this is a directory resource.
	 */
	@Override
	public void refresh() {
		super.refresh();
		this.childDirectories = null;
		this.childFiles = null;
		this.exists = null;
	}
	
	/**
	 * @return <code>true</code> in any case because this is clarly a remote resource.
	 */
	public boolean isRemoteResource() {
		return true;
	}
	
	/**
	 * better test for getting the parent url string for the root node
	 * than creating the parent which is not needed.
	 */
	public boolean isRoot() {
		URL parentURL;
		try {
			parentURL = getParentURL(this.ftpURL);

			if (parentURL == null) {
				return true;
			}		
			return false;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	private static URL getParentURL(URL source) throws MalformedURLException {
		String path = getURLPath(source);
		if (path.length() == 0 || path.equals("/")) {
			return null; // no parent available
		}

		String sourceString = source.toString();

		String urlTrailer = "";
		if (sourceString.indexOf(';') != -1) {
			int semikolonIndex = sourceString.indexOf(';');
			urlTrailer = sourceString.substring(semikolonIndex);
			sourceString = sourceString.substring(0, semikolonIndex);
		}

		String parentPath = new File(path).getParent();
		String newUrlString = sourceString.replace(path, parentPath);
		return new URL(newUrlString + urlTrailer);
	}

	/**
	 * add a folder or file to the url path
	 * 
	 * @param source
	 *            The source url
	 * @param add
	 *            The path or file statement to be added.
	 * @return A new URL having the added path
	 * @throws MalformedURLException
	 */
	private static URL addURLPath(URL source, String add) throws MalformedURLException {
		String sourceString = source.toString();

		// remove the trailing string behind the path if there is one.
		String urlTrailer = "";
		if (sourceString.indexOf(';') != -1) {
			int semikolonIndex = sourceString.indexOf(';');
			urlTrailer = sourceString.substring(semikolonIndex);
			sourceString = sourceString.substring(0, semikolonIndex);
		}

		if (sourceString.endsWith("/")) {
			sourceString += add;
		} else {
			sourceString += "/" + add;
		}
		sourceString += urlTrailer;

		return new URL(sourceString);
	}

	/**
	 * Gets the remote path statement for this resource. for example "/pub/etc" from the url statement.
	 * 
	 * @return The path statement.
	 */
	private static String getURLPath(URL url) {
		final String urlString = url.toString();
		int pathStartIndex = 0;
		if(urlString.substring(6).indexOf('/')!=-1) {
			pathStartIndex = urlString.substring(6).indexOf('/') + 6;
		} else {
			pathStartIndex = urlString.length();
		}
		
		int pathEndIndex = urlString.length();

		if (urlString.indexOf(';') != -1) {
			pathEndIndex = urlString.indexOf(';');
		}
		
		//could happens after cutting the ;value away
		if(pathStartIndex >= pathEndIndex) {
			return "/";
		}

		String pathString = urlString.substring(pathStartIndex, pathEndIndex);
		return pathString;
	}

	/**
	 * Give a connection, previously fetched using the {@link #getConnection()} method, back, so it can be provided again.
	 * 
	 * @param connection
	 *            The connection to be given back.
	 */
	private void releaseConnection(final FTPClient connection) {
		if (connection != null) {
			FTPConnectionManager.getInstance(this.ftpURL).releaseConnection(connection);
		}
	}

	/**
	 * Disposes the given connection if it's no longer needed.
	 * 
	 * @param connection
	 *            The connection no longer used.
	 */
	private void disposeConnection(final FTPClient connection) {
		if (connection != null) {
			FTPConnectionManager.getInstance(this.ftpURL).disposeConnection(connection);
		}
	}

	/**
	 * Gets a connection from the connection pool. Use {@link #releaseConnection(FTPClient)} for give it free.
	 * 
	 * @return A ready to use connection.
	 * @throws IOException
	 * 
	 * @throws IOException
	 */
	private FTPClient getConnection() throws IOException {
		// get the connection manager instance
		final FTPConnectionManager ftpConnectionManager = FTPConnectionManager.getInstance(this.ftpURL);

		// find out how large is the pool and calculate the number of loops for getting a valid connection.
		int connectionPoolLoop = ftpConnectionManager.getConnectionHighWaterMark();
		if (connectionPoolLoop <= 0) {
			connectionPoolLoop = FTPConnectionManager.MAX_CONNECTIONS;
		}
		connectionPoolLoop += 2;

		// try to get a valid connection
		FTPClient connectionFromPool = null;
		Exception lastException = null;
		for (int i = 0; i < connectionPoolLoop; i++) {
			try {
				connectionFromPool = FTPConnectionManager.getInstance(this.ftpURL).getRegisteredConnection();
				boolean success = connectionFromPool.setFileType(FTP.ASCII_FILE_TYPE);
				if (!success) {
					throw new RuntimeException("Setting file type to ASCII has failed " + this);
				}
				return connectionFromPool;
			} catch (Exception e) {
				lastException = e;
				this.disposeConnection(connectionFromPool);
				continue;
			}
		}

		throw new IOException(lastException);
	}

	private FTPFile getFTPFile(URL url) throws IOException {
		if (this.ftpFile == null) {
			FTPClient connection = this.getConnection();
			FTPFile[] listFiles = null;
			try {
				listFiles = connection.listFiles(getURLPath(this.ftpURL));
			} finally {
				releaseConnection(connection);
			}

			if (listFiles != null) {
				for (int i = 0; i < listFiles.length; i++) {
					final String fileName = listFiles[i].getName().replaceAll("/", "");
					if (fileName.equals(this.getName())) {
						this.ftpFile = listFiles[i];
						break;
					}
				}
			}
		}

		return this.ftpFile;
	}

	/**
	 * The modification date of the resource.
	 * 
	 * @return The modification date or <code>null</code> if something fails while reading the modification date.
	 */
	@Override
	public Date getModifiedAt() {
		try {
			return this.getFTPFile(this.ftpURL).getTimestamp().getTime();
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public IResourceHandler addPathStatement(String statement) throws ResourceHandlerException {
		try {
			URL resultUrl = addURLPath(this.ftpURL, statement);
			return new FTPResourceHandler(resultUrl);
		} catch (Exception e) {
			throw new ResourceHandlerException(e);
		}
	}

	@Override
	public RESOURCE_HANDLER_USER_TYPES getType() {
		return RESOURCE_HANDLER_USER_TYPES.FTP;
	}

}
