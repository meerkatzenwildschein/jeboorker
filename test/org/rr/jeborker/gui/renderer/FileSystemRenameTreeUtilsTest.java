package org.rr.jeborker.gui.renderer;

import java.util.List;

import junit.framework.TestCase;

public class FileSystemRenameTreeUtilsTest extends TestCase {

	public void testGetCommaChangeOffers1() {
		List<String> commaChangeOffers = FileSystemRenameTreeUtils.getCommaChangeOffers("Nachname, Vorname - Titel des Buches.epub");
		assertTrue(commaChangeOffers.contains("Vorname Nachname - Titel des Buches.epub"));
	}

	public void testGetCommaChangeOffers2() {
		List<String> commaChangeOffers = FileSystemRenameTreeUtils.getCommaChangeOffers("Nachname ZweiterNachname, Vorname - Titel des Buches.epub");
		assertTrue(commaChangeOffers.contains("Vorname Nachname ZweiterNachname - Titel des Buches.epub"));
//		System.out.println(commaChangeOffers);	
	}
	
	public void testGetCommaChangeOffers3() {
		List<String> commaChangeOffers = FileSystemRenameTreeUtils.getCommaChangeOffers("Nachname ZweiterNachname, Vorname ErsterVorname - Titel des Buches.epub");
		assertTrue(commaChangeOffers.contains("Vorname ErsterVorname Nachname ZweiterNachname - Titel des Buches.epub"));
//		System.out.println(commaChangeOffers);	
	}
	
	public void testGetCommaChangeOffers4() {
		List<String> commaChangeOffers = FileSystemRenameTreeUtils.getCommaChangeOffers("ZweiterNachname, J.R.R. Vorname - Titel des Buches.epub");
		assertTrue(commaChangeOffers.contains("J.R.R. Vorname ZweiterNachname - Titel des Buches.epub"));
//		System.out.println(commaChangeOffers);	
	}
}
