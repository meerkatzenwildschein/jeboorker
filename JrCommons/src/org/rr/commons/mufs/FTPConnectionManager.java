package org.rr.commons.mufs;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;


public class FTPConnectionManager {

	private static final HashMap<String, FTPConnectionManager> sharedinstances = new HashMap<String, FTPConnectionManager>(); 
	
	private final URL url;
	
	private final Vector<FTPClient> inPoolconnections = new Vector<>();
	
	private final Vector<FTPClient> inUseConnections = new Vector<>();
	
	/**
	 * specifies the maxmimal number of connection to be established to one host.
	 */
	static final int MAX_CONNECTIONS = 10;	
	
	private int connectionHighWaterMark = MAX_CONNECTIONS;
	
	private FTPConnectionManager(final URL url) {
		this.url = url;
	}

	/**
	 * Get a new instance for the given URL. All {@link FTPConnectionManager} 
	 * having the same host in the url, will get the same shared instance.
	 * 
	 * @param url The url for which the instance should be fetched.
	 * @return The shared {@link FTPConnectionManager} instance.
	 */
	public static FTPConnectionManager getInstance(final URL url) {
		FTPConnectionManager connectionManager = sharedinstances.get(url.getHost().toLowerCase());
		if(connectionManager==null) {
			connectionManager = new FTPConnectionManager(url);
			sharedinstances.put(url.getHost().toLowerCase(), connectionManager);
		}
		return connectionManager;
	}
	
	/**
	 * Gets an connection which can be used exclusivly. This method can be used
	 * for multithreaded tasks. Take sure that the connection is returned using
	 * the {@link #releaseConnection(FTPClient)} method, so it can be reused.
	 *  
	 * @return The connection.
	 * @throws UnknownHostException
	 * @throws ResourceHandlerException 
	 * @thorws RuntimeException
	 */
	public synchronized FTPClient getRegisteredConnection() throws UnknownHostException, ResourceHandlerException {
		FTPClient result = null;
		ArrayList<Exception> failcount = new ArrayList<>();
		
		while(true) {
			//return an existing connection from the pool
			if(inPoolconnections.size() > 0) {
				//remove connection from pool and put it into the used list.
				result = inPoolconnections.remove(0);
				inUseConnections.add(result);
				try {
					initConnection(result, url);
				} catch (UnknownHostException e) {
					//host address seems to be invalid or unreachable. 
					//no sense to retry
					disposeConnection(result);
					throw e;
				}  catch (ResourceHandlerException e) {
					//login has failed, no sense to continue
					disposeConnection(result);
					throw e;
				} catch (Exception e) {
					//max connection limit reached?
					connectionHighWaterMark = inUseConnections.size();
					failcount.add(e);					
				}
				return result;
			} else if (inUseConnections.size() < connectionHighWaterMark) {
				//try to create a new connection if the highwatermark is not reached
				FTPClient newConnection = new FTPClient();
				try {
					//init the new connection.
					this.initConnection(newConnection, url);
				} catch (UnknownHostException e) {
					//host address seems to be invalid or unreachable. 
					//no sense to retry
					disposeConnection(result);
					throw e;
				}  catch (ResourceHandlerException e) {
					//login has failed, no sense to continue
					disposeConnection(result);
					throw e;
				} catch (Exception e) {
					//max connection limit reached?
					connectionHighWaterMark = inUseConnections.size();
					failcount.add(e);
				}
				this.inUseConnections.add(newConnection);
				return newConnection;
			}
			
			//test if the highwatermatk is 0. No sense to continue.
			if(connectionHighWaterMark == 0) {
				throw new RuntimeException("could not establish connection to " + String.valueOf(this.url));
			}
			
			//test if there are too many errors occured and abort with the last 
			//exception.
			if(failcount.size()>10) {
				throw new RuntimeException(failcount.get(failcount.size()-1));
			}
			
			//wait a moment and retry.
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {}
		}
	}
	
	/**
	 * Gets the highwatermark for the connections in the pool.
	 * @return the highwater mark value.
	 */
	public int getConnectionHighWaterMark() {
		return this.connectionHighWaterMark;
	}
	
	/**
	 * Give a connection, previously fetched using the {@link #getRegisteredConnection()} method, back,
	 * so it can be provided again.
	 * @param connection The connection to be given back.
	 */
	public void releaseConnection(final FTPClient connection) {
		this.inUseConnections.remove(connection);
		this.inPoolconnections.add(connection);	
	}
	
	/**
	 * Remove and close the given connection.
	 * @param connection The connection to be removed.
	 */
	public void disposeConnection(final FTPClient connection) {
		//remove the connection from the caches. It's no longer provided.
		this.inUseConnections.remove(connection);
		this.inPoolconnections.remove(connection);
		
		//do the dispose in a new thread. It's not needed to 
		//let the application wait till the dispose process has been finished.
		new Thread(new Runnable() {
		
			@Override
			public void run() {
				try {
					connection.logout();
				} catch (Exception e) {}				
				try {
					connection.disconnect();
				} catch (Exception e) {}
			}
		}).start();
		
	}
	
	/**
	 * Initializes the given connection if not already done.
	 * @param connection The connection to be initialized
	 * @param url The url containing the host to be connected to.
	 * @throws UnknownHostException 
	 * @throws IOException
	 * @throws ResourceHandlerException if the login fails
	 */
	private void initConnection(final FTPClient connection, final URL url) throws UnknownHostException, IOException, ResourceHandlerException {
		connection.setDefaultTimeout(3000);
		if(!connection.isConnected()) {
			int port = 21;
			if(url.getPort() > 0) {
				port = url.getPort();
			}
			
			connection.connect(url.getHost(), port);
			
			String userName = "anonymous";
			String pass = "anonymous";
			
			String userInfo = url.getUserInfo();
			if(userInfo!=null && userInfo.length()>0) {
				String[] split = StringUtils.split(userInfo, ':');
				if(split!=null) {
					if(split.length >= 1) {
						userName = split[0];
					}
					if(split.length >= 2) {
						pass = split[1];
					}
				}
			}
			
			boolean result = connection.login(userName, pass);
			if(!result) {
				throw new ResourceHandlerException("login for " + String.valueOf(userName) + "/" + String.valueOf(pass) + " has failed.");
			}
		}
		
		//reset to ascii 
		connection.setFileType(FTP.ASCII_FILE_TYPE);
	}	
	
}
