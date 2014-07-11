package bd.amazed.pdfscissors.model;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * A map containing crop rectangle for each page.
 *
 */
public class PageRectsMap {

	/**
	 * The Y coordinate is oriented to the top (swing/cbz)
	 */
	public static final int ORIENTATION_Y_TOP = 0;

	/**
	 * Y coordinate is oriented to the page bottom (pdf)
	 */
	public static final int ORIENTATION_Y_BOTTOM = 1;

	private int orientationMode = ORIENTATION_Y_BOTTOM;

	private HashMap<Integer, ArrayList<Rectangle>> pageRectsMap;

	public PageRectsMap() {
		pageRectsMap = new HashMap<Integer, ArrayList<Rectangle>>();
	}

	public void putRects(int pageNumber, ArrayList<Rectangle> cropRects) {
		Integer page = Integer.valueOf(pageNumber);
		pageRectsMap.put(page, cropRects);
	}

	public void putRects(Integer pageNumber, ArrayList<Rectangle> cropRects) {
		pageRectsMap.put(pageNumber, cropRects);
	}

	public ArrayList<Rectangle> getRects(int pageNumber) {
		return pageRectsMap.get(Integer.valueOf(pageNumber));
	}

	/*
	 *@return Returns the converted awt rectangles ready to use for cropping. null means there is no cropping rect, keep the whole page
	 */
	public ArrayList<Rectangle> getConvertedRectsForCropping(int pageNumber, int viewWidth, int viewHeight, float docWidth, float docHeight) {
		ArrayList<Rectangle> rects = getRects(pageNumber);
		if (rects == null) {
			return null;
		}
		ArrayList<java.awt.Rectangle> cropRectsInIDocCoords = new ArrayList<java.awt.Rectangle>(rects.size());
		double widthRatio = docWidth / viewWidth;
		double heightRatio = docHeight / viewHeight;
		if (widthRatio != heightRatio) {
			System.err.println("WARNING>>> RATION NOT SAME ?! " + widthRatio + " " + heightRatio);
		}
		for (Rectangle rect : rects) {
			Rectangle covertedRect = new Rectangle();
			covertedRect.x = (int) (widthRatio * rect.x);
			if(orientationMode == ORIENTATION_Y_BOTTOM) {
				covertedRect.y = (int) (widthRatio * (viewHeight - rect.y - rect.height));
				covertedRect.y = covertedRect.y - ((int)viewHeight - (int) docHeight); // move y for different page heights
			} else if (orientationMode == ORIENTATION_Y_TOP) {
				covertedRect.y = (int) (widthRatio * (rect.y));
			} else {
				throw new IllegalArgumentException("Illegal orientation mode" + orientationMode);
			}
			covertedRect.width = (int) (widthRatio * rect.width);
			covertedRect.height = (int) (widthRatio * rect.height);
			cropRectsInIDocCoords.add(covertedRect);
		}

		return cropRectsInIDocCoords;

	}

	@Override
	public String toString() {
		return pageRectsMap.toString();
	}

	public void setOriantationMode(int mode) {
		this.orientationMode = mode;
	}
}
