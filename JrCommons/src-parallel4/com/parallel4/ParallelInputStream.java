package com.parallel4;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reads stream in another thread. For CPU intensive streams (e.g. GZIP) this
 * can speed up processing by around 50%.
 * 
 *  (C) Copyright 2008 Markus Junginger.
 * 
 * @author Markus Junginger
 */
/*
 * This file is part of Parallel4.
 * 
 * Parallel4 is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Parallel4 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Parallel4. If not, see <http://www.gnu.org/licenses/>.
 */
public class ParallelInputStream extends InputStream {
	/** Read bytes are stored here */
	protected byte[] buffer;

	/** The pointer of content filled in the <code>buffer</code>. */
	protected volatile int filled;

	/** The current position within the byte array <code>buffer</code>. */
	protected volatile int pos;

	private InputStream in;
	private final int maxChunkSize;
	private final int blockingChunkSize;

	private ParallelReader parallelReader;
	private Throwable parallelException;
	private volatile int eofPos = -1;
	private volatile boolean notifyMe;
	private volatile boolean notifyReader;

	private final int size;

	public ParallelInputStream(InputStream in) {
		this(in, 256 * 1024, 16 * 1024, 8 * 1024);
	}

	public ParallelInputStream(InputStream in, int size, int maxChunkSize, int blockingChunkSize) {
		this.size = size;
		this.blockingChunkSize = blockingChunkSize;
		if (size <= 0 || maxChunkSize <= 0 || blockingChunkSize <= 0) {
			throw new IllegalArgumentException("size must be > 0");
		}
		this.in = in;
		this.maxChunkSize = maxChunkSize;

		buffer = new byte[size];
		parallelReader = new ParallelReader();
		Parallel.executeWithFreePool(parallelReader);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized int available() throws IOException {
		if (buffer == null) {
			throw new IOException("Stream is closed");
		}
		return checkAvailable();
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void close() throws IOException {
		// TODO do this in parallel reader
		if (null != in) {
			super.close();
			in = null;
		}
		buffer = null;
	}

	/** {@inheritDoc} */
	@Override
	public int read() throws IOException {
		if (in == null) {
			throw new IOException("Stream is closed");
		}
		while (checkAvailable() == 0) {
			checkParallelException();
			if (pos == eofPos) {
				return -1;
			}
			waitABitForParallelReader();
		}
		return buffer[pos++] & 0xFF;
	}

	/** {@inheritDoc} */
	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		if (in == null || buffer == null) {
			throw new IOException("Stream is closed");
		}
		int maxLeftToFill = length;
		int copied = 0;
		while (true) {
			int filledAtStart = filled;
			int available = checkAvailable();
			if (available > 0) {
				int copyLen = available < maxLeftToFill ? available : maxLeftToFill;
				System.arraycopy(buffer, pos, buffer, offset + copied, copyLen);
				pos += copyLen;
				copied += copyLen;
				maxLeftToFill -= copyLen;
				if (notifyReader) {
					synchronized (this) {
						notify();
					}
				}
				if (copied == length) {
					return copied;
				}
			} else {
				if (copied > 0) {
					return copied;
				}
				if (pos == eofPos) {
					return -1;
				}
				// done in waitForParallelReader: checkParallelException();
				if (filled == filledAtStart) {
					waitForParallelReader(filledAtStart);
				}
			}
		}
	}

	private void waitABitForParallelReader() throws IOException {
		notifyMe = true;
		synchronized (this) {
			try {
				wait(10);
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
		}
		notifyMe = false;
	}

	private void waitForParallelReader(int filledToChange) throws IOException {
		notifyMe = true;
		synchronized (this) {
			try {
				while (filled == filledToChange && pos != eofPos) {
					checkParallelException();
					wait(1);
				}
			} catch (InterruptedException e) {
				throw new IOException(e);
			} finally {
				notifyMe = false;
			}
		}
	}

	private int checkAvailable() {
		if (pos == size && filled < pos) {
			pos = 0;
		}
		int limit = filled;
		if (limit < pos) {
			limit = size;
		}
		int available = limit - pos;
		return available;
	}

	private void checkParallelException() throws IOException {
		if (parallelException != null) {
			if (parallelException instanceof IOException) {
				throw (IOException) parallelException;
			} else if (parallelException instanceof RuntimeException) {
				throw (RuntimeException) parallelException;
			} else if (parallelException instanceof Error) {
				throw (Error) parallelException;
			} else {
				throw new IOException("Unexpected parallel exception", parallelException);
			}
		}

	}

	private class ParallelReader implements Runnable {

		@Override
		public void run() {

			try {
				while (eofPos == -1 || pos != eofPos) {

					if (filled == size) {
						waitForConsumer(0);
						filled = 0;
					} else if (filled != 0) {
						// filled is one before pos?
						waitForConsumer(filled + 1);
					}

					if (readAhead()) {
						if (notifyMe) {
							synchronized (ParallelInputStream.this) {
								ParallelInputStream.this.notify();
							}
						}
					}
				}
			} catch (IOException e) {
				parallelException = e;
			} catch (RuntimeException rex) {
				parallelException = rex;
			} catch (Error er) {
				parallelException = er;
			} catch (InterruptedException e) {
				parallelException = e;
			}
		}

		private void waitForConsumer(int posToChange) throws InterruptedException {
			while (pos == posToChange) {
				notifyReader = true;
				synchronized (ParallelInputStream.this) {
					while (pos == posToChange) {
						ParallelInputStream.this.wait(1);
					}
				}
				notifyReader = false;
			}
		}

		private boolean readAhead() throws IOException {
			int available = in.available();
			int posSnapshot = pos;

			int lenToRead;
			if (filled >= posSnapshot) {
				lenToRead = size - filled;
			} else {
				lenToRead = posSnapshot - filled - 1;
			}
			if (lenToRead == 0) {
				return false;
			}
			if (available > 1) {
				if (available < lenToRead) {
					lenToRead = available < maxChunkSize ? available : maxChunkSize;
				}
			} else {
				if (lenToRead > blockingChunkSize) {
					lenToRead = blockingChunkSize;
				}
			}
			int lastRead = in.read(buffer, filled, lenToRead);
			if (lastRead < 0) {
				eofPos = filled;
			} else {
				filled += lastRead;
			}
			return true;
		}

	}

}
