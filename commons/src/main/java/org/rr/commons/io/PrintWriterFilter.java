package org.rr.commons.io;

import java.io.PrintWriter;

public class PrintWriterFilter {

	public static interface LineFilter {
		public String filter(String line, int page);
	}

	private PrintWriter writer;
	private PrintWriterFilter filterWriter;
	private LineFilter filter;

	public PrintWriterFilter(PrintWriter writer, LineFilter filter) {
		this.filter = filter;
		this.writer = writer;
	}

	public PrintWriterFilter(PrintWriterFilter filterWriter, LineFilter filter) {
		this.filter = filter;
		this.filterWriter = filterWriter;
	}

	public void println(String line, int page) {
		String filtered = filter.filter(line, page);
		if (writer != null) {
			writer.write(filtered);
		} else if (filterWriter != null) {
			filterWriter.println(filtered, page);
		}
	}

	public static LineFilter getAcceptAllLineFilter() {
		return new LineFilter() {

			@Override
			public String filter(String line, int page) {
				return line;
			}
		};
	}

	public void flush() {
		if (writer != null) {
			writer.flush();
		} else if (filterWriter != null) {
			filterWriter.flush();
		}
	}
}
