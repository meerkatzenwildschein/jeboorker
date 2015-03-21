package org.rr.jeborker.gui.action;

import static org.rr.commons.utils.BooleanUtils.not;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.JFileChooser;

import org.apache.commons.io.IOUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.MimeUtils;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.jeborker.gui.resources.ImageResourceBundle;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

class SetCoverFromClipboardAction extends SetCoverFrom<ByteArrayInputStream> implements ClipboardOwner {

	private static final long serialVersionUID = 4772310971481868593L;
	
	ByteArrayInputStream image;
	
	SetCoverFromClipboardAction(IResourceHandler resourceHandler) {
		super(resourceHandler);
		String name = Bundle.getString("SetCoverFromClipboardAction.name");
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("paste_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("paste_22.png"));
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

	@Override
	public ByteArrayInputStream doOnce() {
		if(image == null) {
			Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable contents = c.getContents(this);
			try {
				//Using only the imageDataFlavor cause sometimes the following exception with java > 1.7
				//java.io.IOException: Owner failed to convert data 
				IOException ex = null;
				DataFlavor[] transferDataFlavors = contents.getTransferDataFlavors();
				for(DataFlavor transferDataFlavor : transferDataFlavors) {
					try {
						if(not(MimeUtils.isImageMime(transferDataFlavor.getMimeType()))) {
							continue;
						}
						Object transferData = contents.getTransferData(transferDataFlavor);
						if(transferData instanceof BufferedImage) {
							byte[] encodeJpeg = ImageUtils.getImageBytes((BufferedImage) transferData, MimeUtils.MIME_JPEG);
							image = new ByteArrayInputStream(encodeJpeg);
							setDialogOption(JFileChooser.APPROVE_OPTION);
							setDialogResult(ResourceHandlerFactory.getVirtualResourceHandler("cover.jpg", encodeJpeg));
						} else if(transferData instanceof InputStream) {
							byte[] encodeJpeg = IOUtils.toByteArray((InputStream) transferData);
							image = new ByteArrayInputStream(encodeJpeg);
							IResourceHandler imageDataResourceHandler = ResourceHandlerFactory.getVirtualResourceHandler("cover.jpg", encodeJpeg);
							if(imageDataResourceHandler.getMimeType(true).equals(MimeUtils.MIME_JPEG)) {
								setDialogOption(JFileChooser.APPROVE_OPTION);
								setDialogResult(imageDataResourceHandler);		
								ex = null;
								break;
							} else {
								IImageProvider imageProvider = ImageProviderFactory.getImageProvider(imageDataResourceHandler);
								BufferedImage bufferedImage = imageProvider.getImage();
								byte[] imageBytes = ImageUtils.getImageBytes(bufferedImage, MimeUtils.MIME_JPEG);
								image = new ByteArrayInputStream(imageBytes);
								
								setDialogOption(JFileChooser.APPROVE_OPTION);
								setDialogResult(ResourceHandlerFactory.getVirtualResourceHandler("cover.jpg", imageBytes));
								ex = null;
								break;
							}
						} else {
							continue;
						}
					} catch(IOException e) {
						ex = e;
						continue;
					}
				}
				
				if(ex != null) {
					throw ex;
				}
			} catch (Exception e1) {
				LoggerFactory.getLogger().log(Level.INFO, "Failed accessing Clipboard", e1);
			}
		}
		return image;
	}

	@Override
	public void setDoOnceResult(ByteArrayInputStream image) {
		this.image = image;
		try {
			if(image != null) {
				setDialogResult(ResourceHandlerFactory.getVirtualResourceHandler("cover.jpg", IOUtils.toByteArray(image)));
			}
		} catch (IOException e) {
			LoggerFactory.getLogger().log(Level.WARNING, "Failed to copy data", e);
		}
	}
	
}
