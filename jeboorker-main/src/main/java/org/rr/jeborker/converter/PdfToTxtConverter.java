package org.rr.jeborker.converter;

import static org.rr.commons.utils.StringUtil.EMPTY;
import static org.rr.commons.utils.StringUtil.isNotEmpty;
import static org.rr.commons.utils.StringUtil.replace;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.rr.commons.io.PrintWriterFilter;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.BooleanUtils;
import org.rr.commons.utils.MathUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.app.JeboorkerConstants;
import org.rr.jeborker.app.JeboorkerConstants.SUPPORTED_MIMES;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.gui.ConverterPreferenceController;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.metadata.pdf.PDFUtils;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

public class PdfToTxtConverter implements IEBookConverter {
	
	private static final String EXTRACTION_MODE_LABEL = Bundle.getString("PdfToTxtConverter.extractionMode.label");
	
	private static final String SIMPLE_TEXT_EXTRACTION = Bundle.getString("PdfToTxtConverter.extractionMode.simple");
	
	private static final String LOCATION_BASED_TEXT_EXTRACTION = Bundle.getString("PdfToTxtConverter.extractionMode.location");

	private static final String REMOVE_PAGE_NUMBERS_LABEL = Bundle.getString("PdfToTxtConverter.removePageNumbers.label");
	
	private static final String REMOVE_HYPHEN_LABEL = Bundle.getString("PdfToTxtConverter.removeHyphen.label");
	
	private static final String TEXT_CODE_PAGE_LABEL = Bundle.getString("PdfToTxtConverter.codePage.label");

	private APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
	
	private ConverterPreferenceController converterPreferenceController;

	private IResourceHandler pdfResource;

	PdfToTxtConverter(IResourceHandler pdfSource) {
		this.pdfResource = pdfSource;
	}

	@Override
	public IResourceHandler convert() throws IOException {
		ConverterPreferenceController converterPreferenceDialog = getConverterPreferenceController();
		if (!converterPreferenceDialog.isConfirmed()) {
			return null;
		}
		Document document = new Document();
		IResourceHandler targetTxtResource = ResourceHandlerFactory.getUniqueResourceHandler(this.pdfResource, "txt");

		PdfReader reader = null;
		try (OutputStream txtOutputStream = targetTxtResource.getContentOutputStream(false)) {
			reader = PDFUtils.getReader(this.pdfResource.toFile());
			PrintWriterFilter printWriter = new PrintWriterFilter(new PrintWriter(new OutputStreamWriter(txtOutputStream, getSelectedCodePage())), PrintWriterFilter.getAcceptAllLineFilter());

			if (isRemovePageNumersEnabled()) {
				printWriter = createNumberFilterPrintWriter(printWriter);
			}
			
			if(isRemoveHypenEnabled()) {
				printWriter = createHypenRemovePrintWriter(printWriter);
			}
			
			extractTextFromPdf(document, reader, printWriter);
			printWriter.flush();
		} catch (IOException e) {
			throw e;
		} catch (Throwable e) {
			throw new IOException("Failed to convert PDF " + pdfResource.getName(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					LoggerFactory.getLogger().log(Level.WARNING, "Failed to close pdf reader", e);
				}
			}
		}
		
		storeCheckboxValue(REMOVE_PAGE_NUMBERS_LABEL, isRemovePageNumersEnabled());
		storeCheckboxValue(REMOVE_HYPHEN_LABEL, isRemoveHypenEnabled());
		storeComboboxValue(EXTRACTION_MODE_LABEL, getSelectedExtractionMode());
		storeComboboxValue(TEXT_CODE_PAGE_LABEL, getSelectedCodePage());
		
		return targetTxtResource;
	}

	private PrintWriterFilter createNumberFilterPrintWriter(PrintWriterFilter printWriter) {
		return new PrintWriterFilter(printWriter, new PrintWriterFilter.LineFilter() {

			private Pattern numberPattern = Pattern.compile("(\\d*)");
			
			@Override
			public String filter(String text, int page) {
				List<Integer> numbers = getNumbers(text);
				for (Integer num : numbers) {
					if(num > 0 && MathUtils.between(num, page -1, page + 1)) {
						text = replace(text, num.toString(), EMPTY);
					}
				}
				return text;
			}
			
			private List<Integer> getNumbers(String line) {
				List<Integer> result = new ArrayList<>();
				Matcher matcher = numberPattern.matcher(line);
				while (matcher.find()) {
					String group = matcher.group(1);
					if(isNotEmpty(group)) {
						result.add(NumberUtils.toInt(group));
					}
				}
				return result;
			}
		});
	}

	private PrintWriterFilter createHypenRemovePrintWriter(PrintWriterFilter printWriter) {
		return new PrintWriterFilter(printWriter, new PrintWriterFilter.LineFilter() {
			
			@Override
			public String filter(String text, int page) {
				return StringUtils.removeAll(text, "-\\s*\\n\\s*");
			}
		});
	}
	
	private boolean isRemovePageNumersEnabled() {
		return getConverterPreferenceController().getCommonValueAsBoolean(REMOVE_PAGE_NUMBERS_LABEL);
	}
	
	private boolean isRemoveHypenEnabled() {
		return getConverterPreferenceController().getCommonValueAsBoolean(REMOVE_HYPHEN_LABEL);
	}

	private String getSelectedExtractionMode() {
		return getConverterPreferenceController().getCommonValueAsString(EXTRACTION_MODE_LABEL);
	}
	
	private String getSelectedCodePage() {
		return getConverterPreferenceController().getCommonValueAsString(TEXT_CODE_PAGE_LABEL);
	}

	private void extractTextFromPdf(Document document, PdfReader reader, PrintWriterFilter out) throws IOException {
		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			String textFromPage = PdfTextExtractor.getTextFromPage(reader, i, getExtractionStrategy());
			out.println(textFromPage, i);
		}
	}

	private TextExtractionStrategy getExtractionStrategy() {
		String extractionMode = getSelectedExtractionMode();
		TextExtractionStrategy extractionStrategy;
		if (StringUtils.equals(extractionMode, SIMPLE_TEXT_EXTRACTION)) {
			extractionStrategy = new SimpleTextExtractionStrategy();
		} else if (StringUtils.equals(extractionMode, LOCATION_BASED_TEXT_EXTRACTION)) {
			extractionStrategy = new LocationTextExtractionStrategy();
		} else {
			throw new RuntimeException("Undefined extraction strategy.");
		}
		return extractionStrategy;
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
		return JeboorkerConstants.SUPPORTED_MIMES.MIME_PDF;
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
		controller.setShowLandscapePageEntries(false);
		controller.addCommonListSelection(EXTRACTION_MODE_LABEL, Arrays.asList(LOCATION_BASED_TEXT_EXTRACTION, SIMPLE_TEXT_EXTRACTION), getRestoredComboboxValue(EXTRACTION_MODE_LABEL, LOCATION_BASED_TEXT_EXTRACTION));
		controller.addCommonCheckBox(REMOVE_PAGE_NUMBERS_LABEL, getRestoredCheckboxValue(REMOVE_PAGE_NUMBERS_LABEL));
		controller.addCommonCheckBox(REMOVE_HYPHEN_LABEL, getRestoredCheckboxValue(REMOVE_HYPHEN_LABEL));
		controller.addCommonListSelection(TEXT_CODE_PAGE_LABEL, Arrays.asList(StringUtil.UTF_8, StringUtil.UTF_16, StringUtil.ISO_8859_1, StringUtil.US_ASCII), getRestoredComboboxValue(TEXT_CODE_PAGE_LABEL, StringUtil.UTF_8));
		return controller;
	}
	
	private String getRestoredComboboxValue(String key, String defaultValue) {
		return preferenceStore.getGenericEntryAsString(key, defaultValue);
	}

	private void storeComboboxValue(String key, String value) {
		preferenceStore.addGenericEntryAsString(key, value);
	}

	private Boolean getRestoredCheckboxValue(String key) {
		return BooleanUtils.toBoolean(preferenceStore.getGenericEntryAsString(key, Boolean.TRUE.toString()));
	}
	
	private void storeCheckboxValue(String key, Boolean value) {
		preferenceStore.addGenericEntryAsString(key, value.toString());
	}
}
