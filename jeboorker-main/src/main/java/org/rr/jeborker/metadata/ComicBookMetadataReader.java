package org.rr.jeborker.metadata;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.CommonUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.cell.DatePropertyCellRenderer;
import org.rr.jeborker.gui.cell.DefaultPropertyRenderer;
import org.rr.jeborker.gui.cell.MandatorySelectionPropertyEditor;
import org.rr.jeborker.gui.cell.StarRatingPropertyEditor;
import org.rr.jeborker.gui.cell.StarRatingPropertyRenderer;
import org.rr.jeborker.metadata.comicbook.ComicBookDocument;
import org.rr.jeborker.metadata.comicbook.ComicBookReader;
import org.rr.jeborker.metadata.comicbook.YeyNoType;

import com.l2fprod.common.beans.editor.IntegerPropertyEditor;

class ComicBookMetadataReader implements IMetadataReader {

	private IResourceHandler ebookResourceHandler;

	private ComicBookDocument doc;

	private ComicBookReader comicBookReader;

	ComicBookMetadataReader(IResourceHandler resource) {
		ebookResourceHandler = resource;
	}

	@Override
	public List<IResourceHandler> getEbookResource() {
		return Collections.singletonList(ebookResourceHandler);
	}

	@Override
	public List<MetadataProperty> readMetadata() {
		final ArrayList<MetadataProperty> result = new ArrayList<>();
		try {
			final ComicBookDocument document = getDocument();

			HashMap<String, Object> docInfo = document.getInfo();
			for (Entry<String, Object> entry : docInfo.entrySet()) {
				final String key = entry.getKey();
				final Object value = entry.getValue();
				try {
					MetadataProperty metadataProperty;
					COMICBOOK_METADATA_TYPES type = getTypeByName(key);
					if(type != null) {
						metadataProperty = createMetadataProperty(type, value);
					} else {
						metadataProperty = new MetadataProperty(key, value);
					}
					result.add(metadataProperty);
				} catch(Exception e) {
					LoggerFactory.logWarning(this, "could not handle property " + key + " and value " + value, e);
				}
			}
			byte[] cover = doc.getCover();
			if(cover != null) {
				result.add(new MetadataProperty(IMetadataReader.COMMON_METADATA_TYPES.COVER.getName(), cover));
			}
		} catch (Throwable e) {
			LoggerFactory.logWarning(getClass(), "Could not read metadata for comicbook " + ebookResourceHandler, e);
		}
		return result;
	}

	@Override
	public List<MetadataProperty> getSupportedMetadata() {
		final ArrayList<MetadataProperty> result = new ArrayList<>();
		for(COMICBOOK_METADATA_TYPES type : COMICBOOK_METADATA_TYPES.values()) {
			MetadataProperty metadataProperty = createMetadataProperty(type, EMPTY);
			result.add(metadataProperty);
		}
		return result;
	}

	@Override
	public void fillEbookPropertyItem(List<MetadataProperty> metadataProperties, EbookPropertyItem item) {
		item.clearMetadata();
		for(MetadataProperty metadataProperty : metadataProperties) {
			for(COMICBOOK_METADATA_TYPES type : COMICBOOK_METADATA_TYPES.values()) {
				if(type.getName().equals(metadataProperty.getName())) {
					type.fillItem(metadataProperty, item);
					break;
				}
			}
		}
	}

	@Override
	public String getPlainMetadata() {
		return new String(doc.getComicInfoXml());
	}

	@Override
	public String getPlainMetadataMime() {
		return "text/xml";
	}

	@Override
	public List<MetadataProperty> getMetadataByType(boolean create, List<MetadataProperty> props, COMMON_METADATA_TYPES type) {
		MetadataProperty newProperty;
		switch(type) {
		case AUTHOR:
			newProperty = new MetadataProperty(COMICBOOK_METADATA_TYPES.WRITER.getName(), EMPTY);
			break;
		case TITLE:
			newProperty = new MetadataProperty(COMICBOOK_METADATA_TYPES.TITLE.getName(), EMPTY);
			break;
		case GENRE:
			newProperty = new MetadataProperty(COMICBOOK_METADATA_TYPES.GENRE.getName(), EMPTY);
			break;
		case SERIES_NAME:
			newProperty = new MetadataProperty(COMICBOOK_METADATA_TYPES.SERIES.getName(), EMPTY);
			break;
		case RATING:
			newProperty = new MetadataProperty(COMICBOOK_METADATA_TYPES.RATING.getName(), EMPTY);
			break;
		case AGE_SUGGESTION:
			newProperty = new MetadataProperty(COMICBOOK_METADATA_TYPES.AGE_SUGGESTION.getName(), EMPTY);
			break;
		case DESCRIPTION:
			newProperty = new MetadataProperty(COMICBOOK_METADATA_TYPES.SUMMARY.getName(), EMPTY);
			break;
		case ISBN:
			newProperty = new MetadataProperty(COMICBOOK_METADATA_TYPES.ISBN.getName(), EMPTY);
			break;
		case LANGUAGE:
			newProperty = new MetadataProperty(COMICBOOK_METADATA_TYPES.LANGUAGE.getName(), EMPTY);
			break;
		case COVER:
			newProperty = new MetadataProperty(COMMON_METADATA_TYPES.COVER.getName(), null);
			break;
		default: newProperty = null;
		}

		final ArrayList<MetadataProperty> result = new ArrayList<>();
		if(newProperty != null) {
			for(MetadataProperty prop : props) {
				if(prop.getName().equalsIgnoreCase(newProperty.getName())) {
					result.add(prop);
				}
			}
		}

		if(create && result.isEmpty() && newProperty != null) {
			result.add(newProperty);
		}
		return result;
	}

	private ComicBookReader getComicBookReader() {
		if(comicBookReader == null) {
			comicBookReader = new ComicBookReader(ebookResourceHandler);
		}
		return comicBookReader;
	}

	ComicBookDocument getDocument() throws IOException {
		if(doc == null) {
			doc = getComicBookReader().getDocument();
		}
		return doc;
	}

	/**
	 * Get the {@link COMICBOOK_METADATA_TYPES} with the name specified with the name parameter.
	 * @param name The name for the {@link COMICBOOK_METADATA_TYPES} to be searched.
	 * @return The desired {@link COMICBOOK_METADATA_TYPES} or <code>null</code> if no type with the
	 * given name was found..
	 */
	private COMICBOOK_METADATA_TYPES getTypeByName(String name) {
		for(COMICBOOK_METADATA_TYPES type : COMICBOOK_METADATA_TYPES.values()) {
			if(type.getName().equals(name)) {
				return type;
			}
		}
		return null;
	}

	private MetadataProperty createMetadataProperty(COMICBOOK_METADATA_TYPES type, Object value) {
		MetadataProperty metadataProperty = new MetadataProperty(type.getName(), value);
		metadataProperty.addValidValues(type.getValidValues());
		metadataProperty.setPropertyClass(type.getPropertyClass());
		metadataProperty.setPropertyEditorClass(type.getPropertyEditorClass());
		metadataProperty.setPropertyRendererClass(type.getPropertyRendererClass());
		return metadataProperty;
	}

	protected static interface MetadataEntryType {
		public String getName();

		public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item);

		public List<String> getValidValues();

		public Class<?> getPropertyClass();

		public Class<?> getPropertyEditorClass();

		public Class<?> getPropertyRendererClass();

	}

	static enum COMICBOOK_METADATA_TYPES implements MetadataEntryType {
		TITLE {
			public String getName() {
				return "Title";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				COMMON_METADATA_TYPES.TITLE.fillItem(metadataProperty, item);
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},SERIES {
			public String getName() {
				return "Series";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				COMMON_METADATA_TYPES.SERIES_NAME.fillItem(metadataProperty, item);
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},NUMBER {
			public String getName() {
				return "Number";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setSeriesIndex(metadataProperty.getValueAsString());
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},COUNT {
			public String getName() {
				return "Count";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return Integer.class;
			}

			public Class<?> getPropertyEditorClass() {
				return IntegerPropertyEditor.class;
			}

			public Class<?> getPropertyRendererClass() {
				return DefaultPropertyRenderer.class;
			}
		},VOLUME {
			public String getName() {
				return "Volume";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return Integer.class;
			}

			public Class<?> getPropertyEditorClass() {
				return IntegerPropertyEditor.class;
			}

			public Class<?> getPropertyRendererClass() {
				return DefaultPropertyRenderer.class;
			}
		},ALTERNATE_SERIES {
			public String getName() {
				return "AlternateSeries";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},ALTERNATE_NUMBER {
			public String getName() {
				return "AlternateNumber";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},ALTERNATE_COUNT {
			public String getName() {
				return "AlternateCount";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return Integer.class;
			}

			public Class<?> getPropertyEditorClass() {
				return IntegerPropertyEditor.class;
			}

			public Class<?> getPropertyRendererClass() {
				return DefaultPropertyRenderer.class;
			}
		},SUMMARY {
			public String getName() {
				return "Summary";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setDescription(metadataProperty.getValueAsString());
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},NOTES {
			public String getName() {
				return "Notes";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},YEAR {
			public String getName() {
				return "Year";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return Integer.class;
			}

			public Class<?> getPropertyEditorClass() {
				return IntegerPropertyEditor.class;
			}

			public Class<?> getPropertyRendererClass() {
				return DefaultPropertyRenderer.class;
			}
		},MONTH {
			public String getName() {
				return "Month";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return Integer.class;
			}

			public Class<?> getPropertyEditorClass() {
				return IntegerPropertyEditor.class;
			}

			public Class<?> getPropertyRendererClass() {
				return DefaultPropertyRenderer.class;
			}
		},WRITER {
			public String getName() {
				return "Writer";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				COMMON_METADATA_TYPES.AUTHOR.fillItem(metadataProperty, item);
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},PENCILLER {
			public String getName() {
				return "Penciller";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},INKER {
			public String getName() {
				return "Inker";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},COLORIST {
			public String getName() {
				return "Colorist";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},LETTERER {
			public String getName() {
				return "Letterer";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},COVERARTIST {
			public String getName() {
				return "CoverArtist";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},EDITOR {
			public String getName() {
				return "Editor";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},PUBLISHER {
			public String getName() {
				return "Publisher";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setPublisher(metadataProperty.getValueAsString());
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},IMPRINT {
			public String getName() {
				return "Imprint";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},GENRE {
			public String getName() {
				return "Genre";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				COMMON_METADATA_TYPES.GENRE.fillItem(metadataProperty, item);
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},WEB {
			public String getName() {
				return "Web";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},PAGECOUNT {
			public String getName() {
				return "PageCount";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return Integer.class;
			}

			public Class<?> getPropertyEditorClass() {
				return IntegerPropertyEditor.class;
			}

			public Class<?> getPropertyRendererClass() {
				return DefaultPropertyRenderer.class;
			}
		},LANGUAGE_ISO {
			public String getName() {
				return "LanguageISO";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setLanguage(metadataProperty.getValueAsString());
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},LANGUAGE {
			public String getName() {
				return "Language";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setLanguage(metadataProperty.getValueAsString());
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},FORMAT {
			public String getName() {
				return "Format";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},BLACK_AND_WHITE {
			List<String> validValues = new ArrayList<>(Arrays.asList(new String[] {YeyNoType.UNKNOWN, YeyNoType.NO, YeyNoType.YES}));

			public String getName() {
				return "BlackAndWhite";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return validValues;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return MandatorySelectionPropertyEditor.class;
			}

			public Class<?> getPropertyRendererClass() {
				return DatePropertyCellRenderer.class;
			}
		},MANGA {
			List<String> validValues = new ArrayList<>(Arrays.asList(new String[] {YeyNoType.UNKNOWN, YeyNoType.NO, YeyNoType.YES}));

			public String getName() {
				return "Manga";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				//no EbookPropertyItem
			}

			public List<String> getValidValues() {
				return validValues;
			}

			public Class<?> getPropertyClass() {
				return null;
			}

			public Class<?> getPropertyEditorClass() {
				return MandatorySelectionPropertyEditor.class;
			}

			public Class<?> getPropertyRendererClass() {
				return DatePropertyCellRenderer.class;
			}
		},RATING {
			public String getName() {
				return "Rating";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				Number number = CommonUtils.toNumber(metadataProperty.getValueAsString());
				item.setRating(number != null ? number.intValue() : null);
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return Integer.class;
			}

			public Class<?> getPropertyEditorClass() {
				return StarRatingPropertyEditor.class;
			}

			public Class<?> getPropertyRendererClass() {
				return StarRatingPropertyRenderer.class;
			}
		},AGE_SUGGESTION {
			public String getName() {
				return "AgeSuggestion";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setAgeSuggestion(metadataProperty.getValueAsString());
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		},ISBN {
			public String getName() {
				return "ISBN";
			}

			public void fillItem(MetadataProperty metadataProperty, EbookPropertyItem item) {
				item.setIsbn(metadataProperty.getValueAsString());
			}

			public List<String> getValidValues() {
				return null;
			}

			public Class<?> getPropertyClass() {
				return String.class;
			}

			public Class<?> getPropertyEditorClass() {
				return null;
			}

			public Class<?> getPropertyRendererClass() {
				return null;
			}
		}
	}

}
