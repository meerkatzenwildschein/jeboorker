package org.rr.jeborker.metadata.pdf;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.net.URI;

import org.rr.commons.utils.ReflectionUtils;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

class PopplerPDFReader {

	private static final int IMAGE_DPI = 150;

	private static final PopplerInterface poppler = PopplerInterface.INSTANCE;

	private static final Cairo cairo = Cairo.INSTANCE;

	private PopplerDocument doc;

	public PopplerPDFReader(File file, String password) {
		this.init(file.toURI().toString(), password != null ? password : "");
	}
	
	public PopplerPDFReader(URI uri, String password) {
		this.init(uri.toString(), password != null ? password : "");
	}

	private void init(String uri, String password) {
		// init glib
		poppler.g_type_init();

		// open doc
		GError errorObj = new GError();
		this.doc = poppler.poppler_document_new_from_file(uri, password, errorObj);

		if (doc == null) {
			throw new RuntimeException(errorObj.toString());
		}
	}

	public void dispose() {
		// close doc
		poppler.g_object_unref(doc);
	}

	public int getPageCount() {
		final int numPages = poppler.poppler_document_get_n_pages(doc);
		return numPages;
	}
	
	public String getXMPMetadata() {
		String xmp = poppler.poppler_document_get_metadata(doc);
		return xmp;
	}

	public String getPageText(final Pointer page) {
		Memory widthM = new Memory(8);
		Memory heightM = new Memory(8);
		poppler.poppler_page_get_size(page, widthM, heightM);
		int pageWidth = (int) widthM.getDouble(0);
		int pageHeight = (int) heightM.getDouble(0);

		PopplerRectangle selection = new PopplerRectangle(0, 0, pageWidth, pageHeight);
		String content = poppler.poppler_page_get_text(page, PopplerSelectionStyle.POPPLER_SELECTION_GLYPH, selection);
		return content;
	}

	public Pointer getPage(int num) {
		final Pointer page = poppler.poppler_document_get_page(doc, num);
		return page;
	}

	public void disposePage(final Pointer page) {
		poppler.g_object_unref(page);
	}

	public BufferedImage renderPage(final Pointer page) {
		Memory widthM = new Memory(8);
		Memory heightM = new Memory(8);
		poppler.poppler_page_get_size(page, widthM, heightM);
		int pageWidth = (int) widthM.getDouble(0);
		int pageHeight = (int) heightM.getDouble(0);

		Pointer surf = cairo.cairo_image_surface_create(CairoFormats.CAIRO_FORMAT_RGB24, IMAGE_DPI * pageWidth / 72, IMAGE_DPI * pageHeight / 72);
		Pointer cr = cairo.cairo_create(surf);

		cairo.cairo_scale(cr, IMAGE_DPI / 72.0, IMAGE_DPI / 72.0);
		cairo.cairo_save(cr);

		poppler.poppler_page_render(page, cr);

		Pointer data = cairo.cairo_image_surface_get_data(surf);
		int width = cairo.cairo_image_surface_get_width(surf);
		int height = cairo.cairo_image_surface_get_height(surf);
		int stride = cairo.cairo_image_surface_get_stride(surf);

		
		BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[] bufferedImageData = ((DataBufferInt) newImage.getRaster().getDataBuffer()).getData();
		int size = stride * height;
		int jump = stride / width;
		for (int i = 0, j = 0; i < size; i += jump, j++) {
			int value = (((int) data.getByte(i + 2) & 0xFF) << 16) | // red
					(((int) data.getByte(i + 1) & 0xFF) << 8) | // green
					(((int) data.getByte(i + 0) & 0xFF) << 0); // blue
			bufferedImageData[j] = value;
		}
		
		cairo.cairo_destroy(cr);
		return newImage;
	}

	public interface Cairo extends Library {
		
		final Cairo INSTANCE = (Cairo) Native.loadLibrary(ReflectionUtils.is64bit() ? "/usr/lib/x86_64-linux-gnu/libcairo.so.2" : "/usr/lib/i386-linux-gnu/libcairo.so.2", Cairo.class);

		void cairo_destroy(Pointer cr);

		void cairo_restore(Pointer cr);

		void cairo_set_source(Pointer cr, Pointer source);

		void cairo_fill(Pointer cr);

		void cairo_rectangle(Pointer cr, double x, double y, double width, double heigth);

		// pattern
		Pointer cairo_pattern_create_rgb(double red, double green, double blue);

		Pointer cairo_pattern_create_rgba(double red, double green, double blue, double alpha);

		void cairo_pattern_destroy(Pointer pattern);

		// Surface
		void cairo_surface_destroy(Pointer surface);

		Pointer cairo_image_surface_create(int CAIRO_FORMAT_RGB24, int content_width, int content_height);

		Pointer cairo_scale(Pointer cr, double x, double y);

		Pointer cairo_save(Pointer cr);

		// surf is the pointer from #cairo_image_surface_create
		void cairo_surface_write_to_png(Pointer surf, String target);

		int cairo_image_surface_get_width(Pointer cr);

		int cairo_image_surface_get_height(Pointer cr);

		int cairo_image_surface_get_stride(Pointer cr);

		Pointer cairo_image_surface_get_data(Pointer cr);

		// cairo_t* cr_surf = cairo_create(surf);
		Pointer cairo_create(Pointer cairo_image_surface_create);

		// Win32
		Pointer cairo_win32_surface_create(int hdc);

	}

	interface PopplerInterface extends Library {

		PopplerInterface INSTANCE = (PopplerInterface) Native.loadLibrary("poppler-glib", PopplerInterface.class);

		PopplerDocument poppler_document_new_from_file(String uri, String password, GError error);

		int poppler_document_get_n_pages(PopplerDocument doc);

		Pointer poppler_document_get_page(PopplerDocument doc, int index);

		/**
		 * Returns the PDF version of document as a string (e.g. PDF-1.6)
		 */
		String poppler_document_get_pdf_version_string(PopplerDocument doc);

		String poppler_document_get_title(PopplerDocument doc);

		String poppler_document_get_author(PopplerDocument doc);

		String poppler_document_get_subject(PopplerDocument doc);

		/**
		 * Returns the semicolon separated keywords associated to the document
		 */
		String poppler_document_get_keywords(PopplerDocument doc);
		
		/**
		 * XMMP metadata
		 */
		String poppler_document_get_metadata(PopplerDocument doc);

		/**
		 * Returns the creator of the document. If the document was converted from another format, the creator is the name of the product that created the
		 * original document from which it was converted.
		 */
		String poppler_document_get_creator(PopplerDocument doc);

		/**
		 * Returns the producer of the document. If the document was converted from another format, the producer is the name of the product that converted it to
		 * PDF
		 */
		String poppler_document_get_producer(PopplerDocument doc);

		void poppler_page_render(Pointer page, Pointer cairo);

		void poppler_page_finalize(Pointer page);

		String poppler_page_get_text(Pointer page, int style, PopplerRectangle rect);

		Pointer poppler_page_get_text_page(Pointer page);

		void poppler_page_get_size(Pointer page, Memory width, Memory height);

		void g_type_init();

		void g_object_unref(PopplerDocument doc);

		void g_object_unref(Pointer page);
	}

	static interface PopplerSelectionStyle {

		int POPPLER_SELECTION_GLYPH = 0;
		int POPPLER_SELECTION_WORD = 1;
		int POPPLER_SELECTION_LINE = 2;
	}

	static interface CairoFormats {

		int CAIRO_FORMAT_ARGB32 = 0;
		int CAIRO_FORMAT_RGB24 = 1;
		int CAIRO_FORMAT_A8 = 2;
		int CAIRO_FORMAT_A1 = 3;
		/*
		 * The value of 4 is reserved by a deprecated enum value. The next format added must have an explicit value of 5. CAIRO_FORMAT_RGB16_565 = 4,
		 */
	}

	public static class PopplerRectangle extends Structure implements Structure.ByReference {

		public PopplerRectangle(final double x1, final double y1, final double x2, final double y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}

		public double x1;
		public double y1;
		public double x2;
		public double y2;
	}

	public static class PopplerDocument extends Structure implements Structure.ByReference {

		// GObject parent_instance;
		public Pointer parent_instance;

		// PDFDoc *doc;
		public Pointer doc;

		// GList *layers;
		public Pointer layers;

		// GList *layers_rbgroups;
		public Pointer layers_rbgroups;

		// CairoOutputDev *output_dev;
		public Pointer output_dev;
	}

	// public static class PopplerPage extends Structure implements Structure.ByReference {}
	public static class GError extends Structure implements Structure.ByReference {

		public String domain;
		public int code;
		public String message;

		@Override
		public String toString() {

			return "GError{" + "domain='" + domain + '\'' + ", code=" + code + ", " + "message='" + message + '\'' + '}';
		}
	}

}
