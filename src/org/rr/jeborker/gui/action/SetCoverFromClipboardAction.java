package org.rr.jeborker.gui.action;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.JFileChooser;

import org.apache.commons.io.IOUtils;
import org.rr.common.swing.SwingUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.gui.resources.ImageResourceBundle;
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
				Object transferData = contents.getTransferData(DataFlavor.imageFlavor);
				if(transferData instanceof BufferedImage) {
					byte[] encodeJpeg = ImageUtils.getImageBytes((BufferedImage) transferData, "image/jpeg");
					image = new ByteArrayInputStream(encodeJpeg);
					setDialogOption(JFileChooser.APPROVE_OPTION);
					setDialogResult(ResourceHandlerFactory.getVirtualResourceHandler("cover.jpg", encodeJpeg));
				} else {
					LoggerFactory.getLogger().log(Level.INFO, "No image data in Clipboard");
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
			setDialogResult(ResourceHandlerFactory.getVirtualResourceHandler("cover.jpg", IOUtils.toByteArray(image)));
		} catch (IOException e) {
			LoggerFactory.getLogger().log(Level.WARNING, "Failed to copy data", e);
		}
	}
	
}
