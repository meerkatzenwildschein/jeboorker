package org.rr.commons.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class HttpInputStream extends InputStream {
	
	private final static ExecutorService pool = Executors.newCachedThreadPool();

	private Future<byte[]> futureContentBytes;
	
	private ByteArrayInputStream in;
	
	public HttpInputStream(URL url) {
		futureContentBytes = getFutureContent(url);
	}

	private Future<byte[]> getFutureContent(final URL url) {
		Callable<byte[]> futureBytes = new Callable<byte[]>() {

			@Override
			public byte[] call() throws Exception {
				return getContent(url);
			}
		};
		Future<byte[]> submit = pool.submit(futureBytes);
		return submit;
	}
	
	@Override
	public int read() throws IOException {
		try {
			if(in == null) {
				in = new ByteArrayInputStream(futureContentBytes.get());
			}
			return in.read();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	private byte[] getContent(URL url) throws IOException {
		try {
			return getContent(url.toURI());
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}
	
	private byte[] getContent(URI url) throws IOException {
		
		RequestConfig.Builder requestBuilder = RequestConfig.custom();
		requestBuilder = requestBuilder.setConnectTimeout(5000);
		requestBuilder = requestBuilder.setConnectionRequestTimeout(5000);
		requestBuilder = requestBuilder.setSocketTimeout(5000);
		
		HttpClientBuilder builder = HttpClientBuilder.create();     
		builder.setDefaultRequestConfig(requestBuilder.build());
		HttpClient httpclient = builder.build();
		
		HttpResponse response1 = httpclient.execute(new HttpGet(url));
		
		// The underlying HTTP connection is still held by the response object
		// to allow the response content to be streamed directly from the network socket.
		// In order to ensure correct deallocation of system resources
		// the user MUST either fully consume the response content  or abort request
		// execution by calling HttpGet#releaseConnection().

		if(response1.getStatusLine().getStatusCode() != 200) {
			throw new IOException(response1.getStatusLine().toString());
		}
	    HttpEntity entity1 = response1.getEntity();
	    
	    // do something useful with the response body
	    // and ensure it is fully consumed
	    byte[] byteArray = EntityUtils.toByteArray(entity1);
	    entity1.consumeContent();
	    return byteArray;
	}

}
