package org.rr.jeborker.metadata;

import java.io.IOException;
import java.util.HashMap;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchema;

public class APDFCommonMetadataHandler extends AMetadataHandler {
	
	protected static final String PDF_KEYWORDS = "Keywords";
	protected static final String PDF_PRODUCER = "PDF Producer";
	protected static final String PDF_VERSION = "PDF Version";
	
	protected static final String BASIC_CREATE_DATE = "Create Date";
	protected static final String BASIC_MODIFY_DATE = "Modify Date";
	protected static final String BASIC_CREATOR_TOOL= "Creator Tool";
	
	protected static final String DUBLIN_CORE_TITLE = "Title";
	protected static final String DUBLIN_CORE_DESCRIPTION = "Description";
	protected static final String DUBLIN_CORE_CREATOR = "Creator";
	protected static final String DUBLIN_CORE_DATE = "Date";
	protected static final String DUBLIN_CORE_COVERAGE = "Coverage";
	protected static final String DUBLIN_CORE_IDENTIFIER = "Identifier";
	protected static final String DUBLIN_CORE_RIGHTS = "Rights";
	protected static final String DUBLIN_CORE_CONTRIBUTORS = "Contributors";
	protected static final String DUBLIN_CORE_LANGUAGES = "Languages";
	protected static final String DUBLIN_CORE_PUBLISHERS = "Publishers";
	protected static final String DUBLIN_CORE_RELATIONSHIPS = "Relationships";
	//protected static final String DUBLIN_CORE_RIGHTSLANGUAGES = "RightsLanguages";
	protected static final String DUBLIN_CORE_SUBJECTS = "Subjects";
	//protected static final String DUBLIN_CORE_TITLELANGUAGES = "TitleLanguages";
	//protected static final String DUBLIN_CORE_TYPES = "Types";
	
	
	protected static final String SUBJECT = "Subject";
	protected static final String TITLE = "Title";
	protected static final String AUTHOR = "Author";
	protected static final String KEYWORDS = "Keywords";
	
	protected HashMap<String, XMPSchema> schemas = new HashMap<String, XMPSchema>();
	
	protected XMPSchema getXMPSchema(final String namespace, final XMPMetadata xmpMetadata) throws IOException {
		XMPSchema xmpSchema = schemas.get(namespace);
		if (xmpSchema == null) {
			if (namespace.equals("dc")) {
				xmpSchema = xmpMetadata.getDublinCoreSchema();
				if(xmpSchema==null) {
					xmpSchema = xmpMetadata.addDublinCoreSchema();
				}
			} else if (namespace.equals("xap")) {
				xmpSchema = xmpMetadata.getBasicSchema();
				if(xmpSchema==null) {
					xmpSchema = xmpMetadata.addBasicSchema();
				}
			} else if (namespace.equals("pdf")) {
				xmpSchema = xmpMetadata.getPDFSchema();
				if(xmpSchema==null) {
					xmpSchema = xmpMetadata.addPDFSchema();
				}
			}  else if (namespace.equals("pdfx")) {
				xmpSchema = xmpMetadata.getPDFXSchema();
				if(xmpSchema==null) {
					xmpSchema = xmpMetadata.addPDFXSchema();
				}
			} else if (namespace.equals("xmp")) {
				xmpSchema = xmpMetadata.getXMPSchema();
				if(xmpSchema==null) {
					xmpSchema = xmpMetadata.addXMPSchema();
				}
			} else if (namespace.equals("xapMM") || namespace.equals("xmpMM")) {
				xmpSchema = xmpMetadata.getMediaManagementSchema();
				if(xmpSchema==null) {
					xmpSchema = xmpMetadata.addMediaManagementSchema();
				}
			} else if (namespace.equals("photoshop")) {
				xmpSchema = xmpMetadata.getPhotoshopSchema();
				if(xmpSchema==null) {
					xmpSchema = xmpMetadata.addPhotoshopSchema();
				}
			}
			schemas.put(namespace, xmpSchema);
		}

		return xmpSchema;
	}	
	
	public void dispose() {
		if(this.schemas != null) {
			this.schemas.clear();
			this.schemas = null;
		}
	}	
}
