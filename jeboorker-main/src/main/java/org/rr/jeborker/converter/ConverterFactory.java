package org.rr.jeborker.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.jeborker.app.JeboorkerConstants;

public class ConverterFactory {

	public static List<IEBookConverter> getConverter(IResourceHandler resource) {
		if(resource != null && JeboorkerConstants.SUPPORTED_MIMES.MIME_CBZ.getMime().equals(resource.getMimeType(true))) {
			ArrayList<IEBookConverter> result = new ArrayList<>();
			result.add(new CbzToEpubConverter(resource));
			result.add(new CbzToPdfConverter(resource));
			result.add(new CbzToCbrConverter(resource));
			result.add(new CbzToCbzConverter(resource));
			return result;
		} else if(resource != null && JeboorkerConstants.SUPPORTED_MIMES.MIME_CBR.getMime().equals(resource.getMimeType(true))) {
			ArrayList<IEBookConverter> result = new ArrayList<>();
			result.add(new CbrToEpubConverter(resource));
			result.add(new CbrToPdfConverter(resource));
			result.add(new CbrToCbzConverter(resource));
			result.add(new CbrToCbrConverter(resource));
			return result;
		} else if(resource != null && JeboorkerConstants.SUPPORTED_MIMES.MIME_PDF.getMime().equals(resource.getMimeType(true))) {
			ArrayList<IEBookConverter> result = new ArrayList<>();
			result.add(new PdfToCBZConverter(resource));
			result.add(new PdfToTxtConverter(resource));
			result.add(new PdfToPdfConverter(resource));
			return result;
		} else if(resource != null && JeboorkerConstants.SUPPORTED_MIMES.MIME_MOBI.getMime().equals(resource.getMimeType(true))) {
			ArrayList<IEBookConverter> result = new ArrayList<>();
			result.add(new MobiToTxtConverter(resource));
			return result;
		}
		return Collections.emptyList();
	}
	
	public static IEBookConverter getConverterbyClass(Class<?> converterClass, IResourceHandler resource) {
		IEBookConverter converter = (IEBookConverter) ReflectionUtils.getObjectInstance(converterClass, new Object[] {resource});
		return converter;
	}
}
