// Created on 22.06.2008
package com.parallel4;

import java.awt.image.WritableRaster;

public class Mandelbrot {

	private final int width;

	private final int height;

	private long time;

	private WritableRaster raster;

	private final int maxIter;

	private final double pixelSize;

	public Mandelbrot(WritableRaster raster, int w, int h, int maxIter, double pixelSize) {
		this.raster = raster;
		this.width = w;
		this.height = h;
		this.maxIter = maxIter;
		this.pixelSize = pixelSize;
	}

	public int checkC(double reC, double imC) {
		double reZ = 0, imZ = 0, reZMinus1 = 0, imZMinus1 = 0;
		int i;
		for (i = 0; i < maxIter; i++) {
			imZ = 2 * reZMinus1 * imZMinus1 + imC;
			reZ = reZMinus1 * reZMinus1 - imZMinus1 * imZMinus1 + reC;
			if (reZ * reZ + imZ * imZ > 4) {
				return i;
			}
			reZMinus1 = reZ;
			imZMinus1 = imZ;
		}
		return i;
	}

	public void calcMandelbrot(boolean parallel) {
		long start = System.nanoTime();
		if (parallel) {
			new ParallelForInt(height).loop(new IterationInt() {
				public void iteration(int y) {
					calcLine(y);
				}
			});
		} else {
			for (int y = 0; y < height; y++) {
				calcLine(y);
			}
		}
		time = System.nanoTime() - start;
	}

	private void calcLine(int y) {
		double imC = -1.35 + pixelSize * y; // oberer Rand
		double reC = -2; // linker Rand
		int[] pixel = new int[3];
		for (int x = 0; x < width; x++) {
			int iterationenC = checkC(reC, imC);
			if (iterationenC != maxIter) {
				int base = 255 * iterationenC / maxIter;
				pixel[0] = 255 - base;
				pixel[1] = 255 - base % 20 * 20;
				pixel[2] = base;
				raster.setPixel(x, y, pixel);
				// image.setRGB is synchronized !!
			}
			reC = reC + pixelSize;
		}
	}

	public long getTime() {
		return time;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
}
