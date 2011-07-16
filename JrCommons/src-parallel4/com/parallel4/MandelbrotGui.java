// Created on 22.06.2008
package com.parallel4;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MandelbrotGui extends JComponent {
	BufferedImage image;

	private static final long serialVersionUID = 8275360913382944680L;

	private WritableRaster raster;

	private final int w;

	private final int h;

	private int maxIter;

	private double pixelSize;

	private Mandelbrot mandelbrot;

	public MandelbrotGui(int w, int h, int maxIter, double pixelSize) {
		this.w = w;
		this.h = h;
		this.maxIter = maxIter;
		this.pixelSize = pixelSize;
		setPreferredSize(new Dimension(w, h));
		image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		raster = image.getRaster();
		mandelbrot = new Mandelbrot(raster, w, h, maxIter, pixelSize);
	}

	/**
	 * @param w
	 * @param h
	 */
	private void clear() {
		Graphics g = image.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, w, h);
		image.flush();
	}

	// Punkte berechnen und setzen
	public void paint(final Graphics g) {
		g.drawImage(image, 0, 0, null);
	}

	public void calc(boolean parallel, JLabel label) {
		clear();
		paintImmediately(0, 0, w, h);
		System.gc();
		System.gc();
		System.gc();
		System.runFinalization();
		System.runFinalization();
		System.runFinalization();
		System.gc();
		System.gc();
		System.gc();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mandelbrot.calcMandelbrot(parallel);
		repaint();
		long time = mandelbrot.getTime() / 1000000;
		label.setText(time + "." + time % 1000000 + " ms");
	}

	public static void main(String[] args) {
		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			System.out.println("Nimbus L&F selected");
		} catch (Exception e) {
			System.out.println("Nimbus L&F not available");
		}

		JFrame frame = new JFrame("Parallel");
		frame.setSize(1000, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container content = frame.getContentPane();

		final JLabel label = new JLabel();
		content.add(label, BorderLayout.NORTH);
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(1, 2));

		final MandelbrotGui mandelbrot = new MandelbrotGui(3000, 1500, 200,
				0.002);
		mandelbrot.calc(true, label);
		JScrollPane scroll = new JScrollPane(mandelbrot);
		content.add(scroll, BorderLayout.CENTER);

		buttons.add(createCalcButton(label, mandelbrot, "Calc", false));
		buttons.add(createCalcButton(label, mandelbrot, "Calc Parallel", true));

		content.add(buttons, BorderLayout.SOUTH);
		frame.setVisible(true);
	}

	private static JButton createCalcButton(final JLabel label,
			final MandelbrotGui mandelbrot, String text, final boolean parallel) {
		JButton b = new JButton(text);
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				mandelbrot.calc(parallel, label);
			}

		});
		return b;
	}
}
