package nl.siegmann.epublib.bookprocessor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.BookProcessor;


/**
 * Uses the given xslFile to process all html resources of a Book.
 * 
 * @author paul
 *
 */
public class XslBookProcessor extends HtmlBookProcessor implements BookProcessor {

	private final static Logger log = Logger.getLogger(XslBookProcessor.class.getName()); 

	private Transformer transformer;
	
	public XslBookProcessor(String xslFileName) throws TransformerConfigurationException {
		File xslFile = new File(xslFileName);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformer = transformerFactory.newTransformer(new StreamSource(xslFile));
	}

	@Override
	public byte[] processHtml(Resource resource, Book book, String encoding) throws IOException {
		Source htmlSource = new StreamSource(new InputStreamReader(resource.getInputStream(), resource.getInputEncoding()));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(out,encoding);
		Result streamResult = new StreamResult(writer);
		try {
			transformer.transform(htmlSource, streamResult);
		} catch (TransformerException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			throw new IOException(e);
		}
		byte[] result = out.toByteArray();
		return result;
	}
}
