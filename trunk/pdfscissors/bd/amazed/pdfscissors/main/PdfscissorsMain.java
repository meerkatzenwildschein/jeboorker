/*
 * Copyright (c) 2004-2010, P. Simon Tuffs (simon@simontuffs.com)
 * All rights reserved.
 *
 * See the full license at http://one-jar.sourceforge.net/one-jar-license.html
 * This license is also included in the distributions of this software
 * under doc/one-jar-license.txt
 */
package bd.amazed.pdfscissors.main;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import bd.amazed.pdfscissors.view.PdfScissorsMainFrame;

public class PdfscissorsMain {

	public static void main(String args[]) {
		if (args == null)
			args = new String[0];
		new PdfscissorsMain().run();
	}

	public void run() {
		setLookAndFeel();
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new PdfScissorsMainFrame().setVisible(true);
			}
		});
	}

	private static void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(PdfscissorsMain.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			Logger.getLogger(PdfscissorsMain.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(PdfscissorsMain.class.getName()).log(Level.SEVERE, null, ex);
		} catch (UnsupportedLookAndFeelException ex) {
			Logger.getLogger(PdfscissorsMain.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
