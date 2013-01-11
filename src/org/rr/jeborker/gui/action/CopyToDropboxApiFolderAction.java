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
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.gui.MainController;

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
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("copy_dropbox_16.gif")));
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(Bundle.getResource("copy_dropbox_22.gif")));		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		IResourceHandler resource = ResourceHandlerFactory.getResourceLoader(source);
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
		AppKeyPair appKey = new AppKeyPair("xxx", "xxx");
		
        WebAuthSession session = new WebAuthSession(appKey, ACCESS_TYPE);
        WebAuthInfo authInfo = session.getAuthInfo();
        RequestTokenPair pair = authInfo.requestTokenPair;
        mDBApi = new DropboxAPI<WebAuthSession>(session);
        InputStream inputStream = resource.getContentInputStream();
        @SuppressWarnings("unused") Entry newEntry;
        
        if(JeboorkerPreferences.getEntryString(DROPBOX_AUTH_KEY) != null && !JeboorkerPreferences.getEntryString(DROPBOX_AUTH_KEY).isEmpty()) {
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
	}

	/**
	 * Use a previously auth to connect to Dropbox.
	 * @throws DropboxUnlinkedException if the auth has failed.
	 */
	private void reauthToDropbox() throws DropboxUnlinkedException {
		String key = JeboorkerPreferences.getEntryString(DROPBOX_AUTH_KEY);
		String secret = JeboorkerPreferences.getEntryString(DROPBOX_AUTH_SECRET);
		AccessTokenPair reAuthTokens = new AccessTokenPair(key, secret);
		mDBApi.getSession().setAccessTokenPair(reAuthTokens);
	}

	/**
	 * Authenticates Jeboorker at Dropbox.
	 */
	private void authToDropbox(WebAuthSession session, WebAuthInfo authInfo, RequestTokenPair pair) throws IOException, URISyntaxException,
			MalformedURLException, DropboxException {
		Desktop.getDesktop().browse(new URL(authInfo.url).toURI());
		
		JOptionPane.showMessageDialog(MainController.getController().getMainWindow(), Bundle.getString("CopyToDropboxAction.auth"));
		session.retrieveWebAccessToken(pair);
    
		AccessTokenPair tokens = session.getAccessTokenPair();
		
		// Use this token pair in future so you don't have to re-authenticate each time:
		JeboorkerPreferences.addEntryString(DROPBOX_AUTH_KEY, tokens.key);
		JeboorkerPreferences.addEntryString(DROPBOX_AUTH_SECRET, tokens.secret);
	}	
	  
}
