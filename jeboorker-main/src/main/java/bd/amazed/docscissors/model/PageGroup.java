package bd.amazed.docscissors.model;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import bd.amazed.docscissors.view.Rect;

/**
 * A group of pages that are cropped together.
 *
 */
public class PageGroup {

	public static final int GROUP_TYPE_ODD_EVEN = 0;
	public static final int GROUP_TYPE_ALL = 1;
	public static final int GROUP_TYPE_INDIVIDUAL = 2;

	private java.util.Vector<Integer> pages = new Vector<>();
	private ArrayList<Rect> rects = new ArrayList<>();
	private String name;
	private BufferedImage stackImage;

	public PageGroup(String name) {
		this.name = name;
	}

	public java.util.Vector<Integer> getPages() {
		return pages;
	}

	public void setPages(java.util.Vector<Integer> pages) {
		this.pages = pages;
	}

	public void addPage(int pageNumber) {
		pages.add(Integer.valueOf(pageNumber));
	}

	public ArrayList<Rectangle> getRectangles() {
		ArrayList<Rectangle> rectangles = new ArrayList<>();
		for (Rect rect : rects) {
			rectangles.add(rect.getRectangleBound()); // TODO rename rect by CropCell or something
		}
		return rectangles;
	}

	public ArrayList<Rect> getRects() {
		return rects;
	}

	public void setRects(ArrayList<Rect> rects) {
		this.rects = rects;
	}


	public static List<PageGroup> createGroupForAllPages(int pageCount) {
		PageGroup allPages = new PageGroup(Bundle.getString("PageGroup.allPages"));
		for (int i = 1; i <= pageCount; i++ ) {
			allPages.addPage(i);
		}
		List<PageGroup> pageGroups = new ArrayList<>();
		pageGroups.add(allPages);
		return pageGroups;
	}

	public static List<PageGroup> createGroupForIndividualPage(int pageCount) {
		List<PageGroup> pageGroups = new ArrayList<>(pageCount);
		for (int i = 1; i <= pageCount; i++ ) {
			PageGroup group = new PageGroup(Bundle.getString("PageGroup.Page") + " " + i);
			group.addPage(i);
			pageGroups.add(group);
		}
		return pageGroups;
	}


	public static List<PageGroup> createGroupForOddEven(int pageCount) {
		PageGroup oddPages = new PageGroup(Bundle.getString("PageGroup.oddPages"));
		PageGroup evenPages = new PageGroup(Bundle.getString("PageGroup.evenPages"));
		for (int i = 1; i <= pageCount; i++ ) {
			if (i % 2 == 0) {
				evenPages.addPage(i);
			} else {
				oddPages.addPage(i);
			}
		}
		List<PageGroup> pageGroups = new ArrayList<>();
		pageGroups.add(oddPages);
		pageGroups.add(evenPages);
		return pageGroups;
	}



	public BufferedImage getStackImage() {
		return stackImage;
	}

	public void setStackImage(BufferedImage stackImage) {
		this.stackImage = stackImage;
	}

	@Override
	public String toString() {
		if (name != null) {
			return name;
		} else {
			return super.toString();
		}
	}

	public int getLastPage() {
		return pages.lastElement();
	}

	public int getPageCount() {
		return pages.size();
	}

	public int getPageNumberAt(int index) {
		return pages.elementAt(index);
	}

	public static List<PageGroup> createGroup(int groupType, int pageCount) {
		switch (groupType) {
		case GROUP_TYPE_ALL:
			return PageGroup.createGroupForAllPages(pageCount);
		case GROUP_TYPE_ODD_EVEN:
			return PageGroup.createGroupForOddEven(pageCount);
		case GROUP_TYPE_INDIVIDUAL:
			return PageGroup.createGroupForIndividualPage(pageCount);
		default:
			throw new IllegalArgumentException("Unknown type : " + groupType);
		}
	}
}

