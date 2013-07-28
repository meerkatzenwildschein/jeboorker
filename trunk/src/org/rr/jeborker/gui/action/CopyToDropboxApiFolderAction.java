package org.rr.jeborker.gui.action;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.RequestTokenPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo;

public class CopyToDropboxApiFolderAction extends AbstractAction {
	
	private static final String DROPBOX_AUTH_SECRET = "dropboxAuthSecret";
	private static final String DROPBOX_AUTH_KEY = "dropboxAuthKey";
	private static final AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	private static DropboxAPI<WebAuthSession> mDBApi;
	    
	//source file to copy
	String source;

	CopyToDropboxApiFolderAction(String text) {
		this.source = text;
		putValue(Action.NAME, Bundle.getString("CopyToDropboxAction.name"));
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("copy_dropbox_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("copy_dropbox_22.png"));		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		IResourceHandler resource = ResourceHandlerFactory.getResourceHandler(source);
        try {
        	String message = Bundle.getFormattedString("CopyToDropboxAction.uploading", resource.getName());
        	MainController.getController().getProgressMonitor().monitorProgressStart(message);
        	
			doUpload(resource);
		} catch (Exception ex) {
			LoggerFactory.getLogger(this).log(Level.WARNING, "Upload failed", ex);
		} finally {
			MainController.getController().getProgressMonitor().monitorProgressStop();
		}
	}

	/**
	 * Uploads the given {@link IResourceHandler} to the dropbox folder of the application.
	 * @param resource The resource to be uploaded.
	 * 
	 * @see http://aaka.sh/patel/2011/12/20/authenticating-dropbox-java-api/
	 * @see http://berry120.blogspot.de/2012/02/dropbox-java-api.html
	 */
	private void doUpload(IResourceHandler resource) throws DropboxException, MalformedURLException, IOException, URISyntaxException {
		// https://www.dropbox.com/developers
		AppKeyPair appKey = new AppKeyPair("z8mvgnac9a1c5ad", "tm585kkw5tc98dr");
		
        WebAuthSession session = new WebAuthSession(appKey, ACCESS_TYPE);
        WebAuthInfo authInfo = session.getAuthInfo();
        RequestTokenPair pair = authInfo.requestTokenPair;
        mDBApi = new DropboxAPI<WebAuthSession>(session);
        InputStream inputStream = resource.getContentInputStream();
        @SuppressWarnings("unused") Entry newEntry;
        
        try {
        	final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
	        if(preferenceStore.getGenericEntryAsString(DROPBOX_AUTH_KEY) != null && !preferenceStore.getGenericEntryAsString(DROPBOX_AUTH_KEY).isEmpty()) {
	        	// re-auth specific stuff
	        	try {
	        		reauthToDropbox();
	        		
	        		//throws the DropboxUnlinkedException if auth was detracted.
	        		newEntry = mDBApi.putFile(resource.getName(), inputStream, resource.size(), null, null); 
	        	} catch(DropboxUnlinkedException e) {
	        		// retry with an auth if the old auth are no longer be valid.
	        		authToDropbox(session, authInfo, pair);
	        		newEntry = mDBApi.putFile(resource.getName(), inputStream, resource.size(), null, null);
	        	}
	        } else {
	            authToDropbox(session, authInfo, pair);
	            newEntry = mDBApi.putFile(resource.getName(), inputStream, resource.size(), null, null);
	        }
        } finally {
        	IOUtils.closeQuietly(inputStream);
	    }
	}

	/**
	 * Use a previously auth to connect to Dropbox.
	 * @throws DropboxUnlinkedException if the auth has failed.
	 */
	private void reauthToDropbox() throws DropboxUnlinkedException {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		String key = preferenceStore.getGenericEntryAsString(DROPBOX_AUTH_KEY);
		String secret = preferenceStore.getGenericEntryAsString(DROPBOX_AUTH_SECRET);
		AccessTokenPair reAuthTokens = new AccessTokenPair(key, secret);
		mDBApi.getSession().setAccessTokenPair(reAuthTokens);
	}

	/**
	 * Authenticates Jeboorker at Dropbox.
	 */
	private void authToDropbox(WebAuthSession session, WebAuthInfo authInfo, RequestTokenPair pair) throws IOException, URISyntaxException,
			MalformedURLException, DropboxException {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		SwingUtils.openURL(authInfo.url);
		
		JOptionPane.showMessageDialog(MainController.getController().getMainWindow(), Bundle.getString("CopyToDropboxAction.auth"));
		session.retrieveWebAccessToken(pair);
    
		AccessTokenPair tokens = session.getAccessTokenPair();
		
		// Use this token pair in future so you don't have to re-authenticate each time:
		preferenceStore.addGenericEntryAsString(DROPBOX_AUTH_KEY, tokens.key);
		preferenceStore.addGenericEntryAsString(DROPBOX_AUTH_SECRET, tokens.secret);
	}	
	
}
