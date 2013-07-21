package org.rr.jeborker.metadata;

import java.io.IOException;
import java.util.HashMap;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchema;

abstract class APDFCommonMetadataHandler extends AMetadataHandler {
	
	private HashMap<String, XMPSchema> schemas = new HashMap<String, XMPSchema>();
	
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
}
