package com.shakethat.jpushbullet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.shakethat.jpushbullet.net.PushbulletDevice;

public class PushbulletClient {

	CredentialsProvider	credsProvider	= new BasicCredentialsProvider();
	DefaultHttpClient	client;
	Gson				gson;

	public PushbulletClient(String api_key) {
		client = new DefaultHttpClient();
		client.setCredentialsProvider(credsProvider);

//		client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
		credsProvider.setCredentials(new AuthScope("api.pushbullet.com", 443), new UsernamePasswordCredentials(api_key,
				null));
		gson = new Gson();
	}

	public PushbulletDevice getDevices() throws IllegalStateException, IOException {
		HttpGet httpget = new HttpGet("https://api.pushbullet.com/api/devices");
		HttpResponse response = client.execute(httpget);
		StringBuffer result = new StringBuffer();
		try {
			System.out.println(response.getStatusLine());
			try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
				for (String line; (line = br.readLine()) != null;) {
					result.append(line);
				}
				br.close();
			}
		} finally {
//			response.close();

		}
		return gson.fromJson(result.toString(), PushbulletDevice.class);
	}

	public void sendNote(String iden, String title, String body) {
		HttpPost post = new HttpPost("https://api.pushbullet.com/api/pushes");
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("type", "note"));
			nameValuePairs.add(new BasicNameValuePair("device_iden", iden));
			nameValuePairs.add(new BasicNameValuePair("title", title));
			nameValuePairs.add(new BasicNameValuePair("body", body));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = client.execute(post);
			System.out.println(response.getStatusLine());
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendLink(String iden, String title, String url) {
		HttpPost post = new HttpPost("https://api.pushbullet.com/api/pushes");
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("type", "link"));
			nameValuePairs.add(new BasicNameValuePair("device_iden", iden));
			nameValuePairs.add(new BasicNameValuePair("title", title));
			nameValuePairs.add(new BasicNameValuePair("url", url));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = client.execute(post);
			System.out.println(response.getStatusLine());
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendList(String iden, String title, ArrayList<String> list) {
		HttpPost post = new HttpPost("https://api.pushbullet.com/api/pushes");
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("type", "list"));
			nameValuePairs.add(new BasicNameValuePair("device_iden", iden));
			nameValuePairs.add(new BasicNameValuePair("title", title));
			for(String s : list) {
				nameValuePairs.add(new BasicNameValuePair("items", s));
			}
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = client.execute(post);
			System.out.println(response.getStatusLine());
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendList(String iden, String title, String... list) {
		HttpPost post = new HttpPost("https://api.pushbullet.com/api/pushes");
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("type", "list"));
			nameValuePairs.add(new BasicNameValuePair("device_iden", iden));
			nameValuePairs.add(new BasicNameValuePair("title", title));
			for(String s : list) {
				nameValuePairs.add(new BasicNameValuePair("items", s));
			}
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = client.execute(post);
			System.out.println(response.getStatusLine());
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendAddress(String iden, String name, String address) {
		HttpPost post = new HttpPost("https://api.pushbullet.com/api/pushes");
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("type", "addess"));
			nameValuePairs.add(new BasicNameValuePair("device_iden", iden));
			nameValuePairs.add(new BasicNameValuePair("name", name));
			nameValuePairs.add(new BasicNameValuePair("address", address));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = client.execute(post);
			System.out.println(response.getStatusLine());
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendFile(String iden, File file) throws IllegalStateException, IOException {
		HttpPost post = new HttpPost("https://api.pushbullet.com/api/pushes");
		try {
			MultipartEntity entity = new MultipartEntity();
			entity.addPart("file", new FileBody(file));
			entity.addPart("device_iden", new StringBody(iden));
			entity.addPart("type", new StringBody("file"));
			post.setEntity(entity);
			
			HttpResponse response = client.execute(post);
			System.out.println(response.getStatusLine());
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}