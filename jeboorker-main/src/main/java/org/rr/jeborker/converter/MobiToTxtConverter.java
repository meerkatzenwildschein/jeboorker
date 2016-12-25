package org.rr.jeborker.converter;

import java.io.IOException;
import java.util.Arrays;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.HTMLEntityConverter;

import static org.rr.commons.utils.StringUtil.*;
import org.rr.jeborker.app.JeboorkerConstants;
import org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.gui.ConverterPreferenceController;
import org.rr.jeborker.gui.MainController;
import org.rr.mobi4java.MobiDocument;
import org.rr.mobi4java.MobiReader;

public class MobiToTxtConverter implements IEBookConverter {
	
	private static final String TEXT_CODE_PAGE_LABEL = Bundle.getString("PdfToTxtConverter.codePage.label");

	private APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
	
	private ConverterPreferenceController converterPreferenceController;

	private IResourceHandler mobiResource;

	MobiToTxtConverter(IResourceHandler pdfSource) {
		this.mobiResource = pdfSource;
	}

	@Override
	public IResourceHandler convert() throws IOException {
		ConverterPreferenceController converterPreferenceDialog = getConverterPreferenceController();
		if (!converterPreferenceDialog.isConfirmed()) {
			return null;
		}
		
		try {
			IResourceHandler targetTxtResource = ResourceHandlerFactory.getUniqueResourceHandler(mobiResource, "txt");
			MobiDocument doc = new MobiReader().read(mobiResource.toFile());
			String textContent = removeHtml(doc.getTextContent());
			textContent = new HTMLEntityConverter(textContent, HTMLEntityConverter.ENCODE_EIGHT_BIT_ASCII).decodeEntities();
			targetTxtResource.setContent(textContent.getBytes(getSelectedCodePage()));
			storeComboboxValue(TEXT_CODE_PAGE_LABEL, getSelectedCodePage());
			return targetTxtResource;
		} catch (IOException e) {
			throw e;
		} catch (Throwable e) {
			throw new IOException("Failed to convert Mobi " + mobiResource.getName(), e);
		}
	}
	
	private String removeHtml(String bodyHtml) {
		String result = bodyHtml
				.replaceAll("\\<br\\/?\\>", NEW_LINE)
				.replaceAll("\\<p.*?\\>", NEW_LINE)
				.replaceAll("\\<.*?\\>", "");
		return result;
	}
	
	private String getSelectedCodePage() {
		return getConverterPreferenceController().getCommonValueAsString(TEXT_CODE_PAGE_LABEL);
	}

	/**
	 * Gets the {@link ConverterPreferenceController} for this instance. Creates a new {@link ConverterPreferenceController} if no one is
	 * created previously.
	 * 
	 * @see #createConverterPreferenceController()
	 */
	private ConverterPreferenceController getConverterPreferenceController() {
		if (this.converterPreferenceController == null) {
			this.converterPreferenceController = this.createConverterPreferenceController();
		}

		if (!this.converterPreferenceController.hasShown()) {
			this.converterPreferenceController.showPreferenceDialog();
		}

		return this.converterPreferenceController;
	}

	@Override
	public SUPPORTED_MIMES getConversionSourceType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_MOBI;
	}

	@Override
	public SUPPORTED_MIMES getConversionTargetType() {
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_TXT;
	}

	public void setConverterPreferenceController(ConverterPreferenceController controller) {
		this.converterPreferenceController = controller;
	}

	public ConverterPreferenceController createConverterPreferenceController() {
		ConverterPreferenceController controller = MainController.getController().getConverterPreferenceController();
		controller.addCommonListSelection(TEXT_CODE_PAGE_LABEL, Arrays.asList(UTF_8, UTF_16, ISO_8859_1, US_ASCII), getRestoredComboboxValue(TEXT_CODE_PAGE_LABEL, UTF_8));
		return controller;
	}
	
	private String getRestoredComboboxValue(String key, String defaultValue) {
		return preferenceStore.getGenericEntryAsString(key, defaultValue);
	}

	private void storeComboboxValue(String key, String value) {
		preferenceStore.addGenericEntryAsString(key, value);
	}
}
