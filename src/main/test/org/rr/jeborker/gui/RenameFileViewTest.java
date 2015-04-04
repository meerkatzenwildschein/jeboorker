package org.rr.jeborker.gui;

import java.util.ArrayList;
import java.util.List;

import org.rr.commons.swing.SwingUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;

import junit.framework.TestCase;

public class RenameFileViewTest extends TestCase {

	public void testSimpleRun() {
		prepareItems();

		RenameFileView renameFileView = new RenameFileView(null, prepareItems(), null);
		renameFileView.setSize(700, 350);
		SwingUtils.centerOnScreen(renameFileView);
		renameFileView.setFileNamePattern("%n## %a - %t");
		renameFileView.setVisible(true);
	}

	private List<EbookPropertyItem> prepareItems() {
		List<EbookPropertyItem> toRename = new ArrayList<>();
		EbookPropertyItem ebookPropertyItem = new EbookPropertyItem();
		ebookPropertyItem.setAuthor("Hans Testmeister");
		ebookPropertyItem.setTitle("Musterbuch Nummer 1");
		ebookPropertyItem.setGenre("Ausgedachtes");
		ebookPropertyItem.setFile("/tmp/something1.epub");
		toRename.add(ebookPropertyItem);

		ebookPropertyItem = new EbookPropertyItem();
		ebookPropertyItem.setAuthor("Peter Müller");
		ebookPropertyItem.setTitle("Das Mahlen des Mehles");
		ebookPropertyItem.setGenre("Ausgedachtes");
		ebookPropertyItem.setFile("/tmp/something2.epub");
		toRename.add(ebookPropertyItem);
		return toRename;
	}
}
