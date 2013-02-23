package org.rr.common.swing.icon;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.Icon;

/**
 * {@link Icon} implementation that loads the icon if it's firstly used. 
 */
public class LazyIcon implements Icon {

	private URL url;
	private Image image;
	
	public LazyIcon(URL url) {
		this.url = url;
	}
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.drawImage(image, x, y, c);
	}

	@Override
	public int getIconWidth() {
		return loadIcon().getWidth(null);
	}

	@Override
	public int getIconHeight() {
		return loadIcon().getHeight(null); 
	}
	
	private Image loadIcon() {
		if(image == null) {
			image = Toolkit.getDefaultToolkit().getImage(url);
		}
		return image;
	}

    
}
