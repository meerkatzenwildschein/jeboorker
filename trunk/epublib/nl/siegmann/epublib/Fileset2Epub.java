package nl.siegmann.epublib;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.bookprocessor.CoverpageBookProcessor;
import nl.siegmann.epublib.bookprocessor.DefaultBookProcessorPipeline;
import nl.siegmann.epublib.bookprocessor.XslBookProcessor;
import nl.siegmann.epublib.chm.ChmParser;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.BookProcessor;
import nl.siegmann.epublib.epub.BookProcessorPipeline;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.epub.EpubWriter;
import nl.siegmann.epublib.fileset.FilesetBookCreator;
import nl.siegmann.epublib.util.VFSUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;

public class Fileset2Epub {

	public static void main(String[] args) throws Exception {
		String inputLocation = "";
		String outLocation = "";
		String xslFile = "";
		String coverImage = "";
		String title = "";
		List<String> authorNames = new ArrayList<String>();
		String type = "";
		String isbn = "";
		String inputEncoding = Constants.ENCODING;
		List<String> bookProcessorClassNames = new ArrayList<String>();
		
		for(int i = 0; i < args.length; i++) {
			if(args[i].equalsIgnoreCase("--in")) {
				inputLocation = args[++i];
			} else if(args[i].equalsIgnoreCase("--out")) {
				outLocation = args[++i];
			} else if(args[i].equalsIgnoreCase("--input-encoding")) {
				inputEncoding = args[++i];
			} else if(args[i].equalsIgnoreCase("--xsl")) {
				xslFile = args[++i];
			} else if(args[i].equalsIgnoreCase("--book-processor-class")) {
				bookProcessorClassNames.add(args[++i]);
			} else if(args[i].equalsIgnoreCase("--cover-image")) {
				coverImage = args[++i];
			} else if(args[i].equalsIgnoreCase("--author")) {
				authorNames.add(args[++i]);
			} else if(args[i].equalsIgnoreCase("--title")) {
				title = args[++i];
			} else if(args[i].equalsIgnoreCase("--isbn")) {
				isbn = args[++i];
			} else if(args[i].equalsIgnoreCase("--type")) {
				type = args[++i];
			}
		}
		if(StringUtils.isBlank(inputLocation) || StringUtils.isBlank(outLocation)) {
			usage();
		}
		BookProcessorPipeline epubCleaner = new DefaultBookProcessorPipeline();
		epubCleaner.addBookProcessors(createBookProcessors(bookProcessorClassNames));
		EpubWriter epubWriter = new EpubWriter(epubCleaner);
		if(! StringUtils.isBlank(xslFile)) {
			epubCleaner.addBookProcessor(new XslBookProcessor(xslFile));
		}
		
		if (StringUtils.isBlank(inputEncoding)) {
			inputEncoding = Constants.ENCODING;
		}
		
		Book book;
		if("chm".equals(type)) {
			book = ChmParser.parseChm(VFSUtil.resolveFileObject(inputLocation), inputEncoding);
		} else if ("epub".equals(type)) {
			book = new EpubReader().readEpub(VFSUtil.resolveInputStream(inputLocation), inputEncoding);
		} else {
			book = FilesetBookCreator.createBookFromDirectory(VFSUtil.resolveFileObject(inputLocation), inputEncoding);
		}
		
		if(StringUtils.isNotBlank(coverImage)) {
//			book.getResourceByHref(book.getCoverImage());
			book.setCoverImage(new Resource(VFSUtil.resolveInputStream(coverImage), coverImage));
			epubCleaner.getBookProcessors().add(new CoverpageBookProcessor());
		}
		
		if(StringUtils.isNotBlank(title)) {
			List<String> titles = new ArrayList<String>();
			titles.add(title);
			book.getMetadata().setTitles(titles);
		}
		
		if(StringUtils.isNotBlank(isbn)) {
			book.getMetadata().addIdentifier(new Identifier(Identifier.Scheme.ISBN, isbn));
		}
		
		initAuthors(authorNames, book);
		
		OutputStream result;
		try {
			result = VFS.getManager().resolveFile(outLocation).getContent().getOutputStream();
		} catch(FileSystemException e) {
			result = new FileOutputStream(outLocation);
		}
		epubWriter.write(book, result);
	}

	private static void initAuthors(List<String> authorNames, Book book) {
		if(authorNames == null || authorNames.isEmpty()) {
			return;
		}
		List<Author> authorObjects = new ArrayList<Author>();
		for(String authorName: authorNames) {
			String[] authorNameParts = authorName.split(",");
			Author authorObject = null;
			if(authorNameParts.length > 1) {
				authorObject = new Author(authorNameParts[1], authorNameParts[0]);
			} else if(authorNameParts.length > 0) {
				authorObject = new Author(authorNameParts[0]);
			}
			authorObjects.add(authorObject);
		}
		book.getMetadata().setAuthors(authorObjects);
	}


	private static List<BookProcessor> createBookProcessors(List<String> bookProcessorNames) {
		List<BookProcessor> result = new ArrayList<BookProcessor>(bookProcessorNames.size());
		for (String bookProcessorName: bookProcessorNames) {
			BookProcessor bookProcessor = null;
			try {
				bookProcessor = (BookProcessor) Class.forName(bookProcessorName).newInstance();
				result.add(bookProcessor);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	private static void usage() {
		System.out.println("usage: " + Fileset2Epub.class.getName() 
				+ "\n  --author [lastname,firstname]"
				+ "\n  --cover-image [image to use as cover]"
				+ "\n  --input-ecoding [text encoding]  # The encoding of the input html files. If funny characters show"
				+ "\n                             # up in the result try 'iso-8859-1', 'windows-1252' or 'utf-8'"
				+ "\n                             # If that doesn't work try to find an appropriate one from"
				+ "\n                             # this list: http://en.wikipedia.org/wiki/Character_encoding"
				+ "\n  --in [input directory]"
				+ "\n  --isbn [isbn number]"
				+ "\n  --out [output epub file]"
				+ "\n  --title [book title]"
				+ "\n  --type [input type, can be 'epub', 'chm' or empty]"
				+ "\n  --xsl [html post processing file]"
				);
		System.exit(0);
	}
}