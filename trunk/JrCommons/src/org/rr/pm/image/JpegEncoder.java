package org.rr.pm.image;

//Version 1.0a
//Copyright (C) 1998, James R. Weeks and BioElectroMech.
//Visit BioElectroMech at www.obrador.com.  Email James@obrador.com.

//This program is edited by Pooya Amini Behbahani and is adapted to be used as a J2ME Application.

//See license.txt (included below - search for LICENSE.TXT) for details
//about the allowed used of this software.
//This software is based in part on the work of the Independent JPEG Group.
//See IJGreadme.txt (included below - search for IJGREADME.TXT)
//for details about the Independent JPEG Group's license.

//This encoder is inspired by the Java Jpeg encoder by Florian Raemy,
//studwww.eurecom.fr/~raemy.
//It borrows a great deal of code and structure from the Independent
//Jpeg Group's Jpeg 6a library, Copyright Thomas G. Lane.
//See license.txt for details.

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/*
 * JpegEncoder - The JPEG main program which performs a jpeg compression of
 * an image.
 */

public class JpegEncoder {

	DataOutputStream outStream;
	JpegInfo JpegObj;
	Huffman Huf;
	DCT dct;
	int imageHeight, imageWidth;
	int Quality;

	public static int[] jpegNaturalOrder = { 0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5, 12, 19, 26, 33, 40, 48, 41, 34, 27, 20, 13, 6, 7, 14, 21,
			28, 35, 42, 49, 56, 57, 50, 43, 36, 29, 22, 15, 23, 30, 37, 44, 51, 58, 59, 52, 45, 38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62, 63, };

	public JpegEncoder(BufferedImage image, int quality, OutputStream out) {
		/*
		 * MediaTracker tracker = new MediaTracker(this); tracker.addImage(image, 0); try { tracker.waitForID(0); } catch (InterruptedException e) { //Got to do
		 * something? }
		 */
		
		/*
		 * Quality of the image. 0 to 100 and from bad image quality, high compression to good image quality low compression
		 */
		Quality = quality;

		/*
		 * Getting picture information It takes the Width, Height and RGB scans of the image.
		 */
		JpegObj = new JpegInfo(image);

		imageHeight = JpegObj.imageHeight;
		imageWidth = JpegObj.imageWidth;
		outStream = new DataOutputStream(out);
		dct = new DCT(Quality);
		Huf = new Huffman(imageWidth, imageHeight);
		Compress();
	}

	public void setQuality(int quality) {
		dct = new DCT(quality);
	}

	public int getQuality() {
		return Quality;
	}

	public void Compress() {
		WriteHeaders(outStream);
		WriteCompressedData(outStream);
		WriteEOI(outStream);
		try {
			outStream.flush();
		} catch (IOException e) {
			System.out.println("IO Error: " + e.getMessage());
		}
	}

	public void WriteCompressedData(DataOutputStream outStream) {
		int i, j, r, c, a, b = 0;
		int comp, xpos, ypos, xblockoffset, yblockoffset;
		float inputArray[][];
		float dctArray1[][] = new float[8][8];
		double dctArray2[][] = new double[8][8];
		int dctArray3[] = new int[8 * 8];

		/*
		 * This method controls the compression of the image. Starting at the upper left of the image, it compresses 8x8 blocks of data until the entire image
		 * has been compressed.
		 */

		int lastDCvalue[] = new int[JpegObj.NumberOfComponents];
		int MinBlockWidth, MinBlockHeight;
		// This initial setting of MinBlockWidth and MinBlockHeight is done to
		// ensure they start with values larger than will actually be the case.
		MinBlockWidth = ((imageWidth % 8 != 0) ? (int) (Math.floor((double) imageWidth / 8.0) + 1) * 8 : imageWidth);
		MinBlockHeight = ((imageHeight % 8 != 0) ? (int) (Math.floor((double) imageHeight / 8.0) + 1) * 8 : imageHeight);
		for (comp = 0; comp < JpegObj.NumberOfComponents; comp++) {
			MinBlockWidth = Math.min(MinBlockWidth, JpegObj.BlockWidth[comp]);
			MinBlockHeight = Math.min(MinBlockHeight, JpegObj.BlockHeight[comp]);
		}
		xpos = 0;
		for (r = 0; r < MinBlockHeight; r++) {
			for (c = 0; c < MinBlockWidth; c++) {
				xpos = c * 8;
				ypos = r * 8;
				for (comp = 0; comp < JpegObj.NumberOfComponents; comp++) {
					inputArray = (float[][]) JpegObj.Components[comp];

					for (i = 0; i < JpegObj.VsampFactor[comp]; i++) {
						for (j = 0; j < JpegObj.HsampFactor[comp]; j++) {
							xblockoffset = j * 8;
							yblockoffset = i * 8;
							for (a = 0; a < 8; a++) {
								for (b = 0; b < 8; b++) {

									// I believe this is where the dirty line at the bottom of the image is
									// coming from. I need to do a check here to make sure I'm not reading past
									// image data.
									// This seems to not be a big issue right now. (04/04/98)

									dctArray1[a][b] = inputArray[ypos + yblockoffset + a][xpos + xblockoffset + b];
								}
							}
							// The following code commented out because on some images this technique
							// results in poor right and bottom borders.
							// if ((!JpegObj.lastColumnIsDummy[comp] || c < Width - 1) && (!JpegObj.lastRowIsDummy[comp] || r < Height - 1)) {
							dctArray2 = dct.forwardDCT(dctArray1);
							dctArray3 = dct.quantizeBlock(dctArray2, JpegObj.QtableNumber[comp]);
							// }
							// else {
							// zeroArray[0] = dctArray3[0];
							// zeroArray[0] = lastDCvalue[comp];
							// dctArray3 = zeroArray;
							// }
							Huf.HuffmanBlockEncoder(outStream, dctArray3, lastDCvalue[comp], JpegObj.DCtableNumber[comp], JpegObj.ACtableNumber[comp]);
							lastDCvalue[comp] = dctArray3[0];
						}
					}
				}
			}
		}
		Huf.flushBuffer(outStream);
	}

	public void WriteEOI(DataOutputStream out) {
		byte[] EOI = { (byte) 0xFF, (byte) 0xD9 };
		WriteMarker(EOI, out);
	}

	public void WriteHeaders(DataOutputStream out) {
		int i, j, index, offset, length;
		int tempArray[];

		// the SOI marker
		byte[] SOI = { (byte) 0xFF, (byte) 0xD8 };
		WriteMarker(SOI, out);

		// The order of the following headers is quiet inconsequential.
		// the JFIF header
		byte JFIF[] = new byte[18];
		JFIF[0] = (byte) 0xff;
		JFIF[1] = (byte) 0xe0;
		JFIF[2] = (byte) 0x00;
		JFIF[3] = (byte) 0x10;
		JFIF[4] = (byte) 0x4a;
		JFIF[5] = (byte) 0x46;
		JFIF[6] = (byte) 0x49;
		JFIF[7] = (byte) 0x46;
		JFIF[8] = (byte) 0x00;
		JFIF[9] = (byte) 0x01;
		JFIF[10] = (byte) 0x00;
		JFIF[11] = (byte) 0x00;
		JFIF[12] = (byte) 0x00;
		JFIF[13] = (byte) 0x01;
		JFIF[14] = (byte) 0x00;
		JFIF[15] = (byte) 0x01;
		JFIF[16] = (byte) 0x00;
		JFIF[17] = (byte) 0x00;
		WriteArray(JFIF, out);

		// Comment Header
		String comment = new String();
		comment = JpegObj.getComment();
		length = comment.length();
		byte COM[] = new byte[length + 4];
		COM[0] = (byte) 0xFF;
		COM[1] = (byte) 0xFE;
		COM[2] = (byte) ((length >> 8) & 0xFF);
		COM[3] = (byte) (length & 0xFF);
		java.lang.System.arraycopy(JpegObj.Comment.getBytes(), 0, COM, 4, JpegObj.Comment.length());
		WriteArray(COM, out);

		// The DQT header
		// 0 is the luminance index and 1 is the chrominance index
		byte DQT[] = new byte[134];
		DQT[0] = (byte) 0xFF;
		DQT[1] = (byte) 0xDB;
		DQT[2] = (byte) 0x00;
		DQT[3] = (byte) 0x84;
		offset = 4;
		for (i = 0; i < 2; i++) {
			DQT[offset++] = (byte) ((0 << 4) + i);
			tempArray = (int[]) dct.quantum[i];
			for (j = 0; j < 64; j++) {
				DQT[offset++] = (byte) tempArray[jpegNaturalOrder[j]];
			}
		}
		WriteArray(DQT, out);

		// Start of Frame Header
		byte SOF[] = new byte[19];
		SOF[0] = (byte) 0xFF;
		SOF[1] = (byte) 0xC0;
		SOF[2] = (byte) 0x00;
		SOF[3] = (byte) 17;
		SOF[4] = (byte) JpegObj.Precision;
		SOF[5] = (byte) ((JpegObj.imageHeight >> 8) & 0xFF);
		SOF[6] = (byte) ((JpegObj.imageHeight) & 0xFF);
		SOF[7] = (byte) ((JpegObj.imageWidth >> 8) & 0xFF);
		SOF[8] = (byte) ((JpegObj.imageWidth) & 0xFF);
		SOF[9] = (byte) JpegObj.NumberOfComponents;
		index = 10;
		for (i = 0; i < SOF[9]; i++) {
			SOF[index++] = (byte) JpegObj.CompID[i];
			SOF[index++] = (byte) ((JpegObj.HsampFactor[i] << 4) + JpegObj.VsampFactor[i]);
			SOF[index++] = (byte) JpegObj.QtableNumber[i];
		}
		WriteArray(SOF, out);

		// The DHT Header
		byte DHT1[], DHT2[], DHT3[], DHT4[];
		int bytes, temp, oldindex, intermediateindex;
		length = 2;
		index = 4;
		oldindex = 4;
		DHT1 = new byte[17];
		DHT4 = new byte[4];
		DHT4[0] = (byte) 0xFF;
		DHT4[1] = (byte) 0xC4;
		for (i = 0; i < 4; i++) {
			bytes = 0;
			DHT1[index++ - oldindex] = (byte) ((int[]) Huf.bits.get(i))[0];
			for (j = 1; j < 17; j++) {
				temp = Huf.bits.get(i)[j];
				DHT1[index++ - oldindex] = (byte) temp;
				bytes += temp;
			}
			intermediateindex = index;
			DHT2 = new byte[bytes];
			for (j = 0; j < bytes; j++) {
				DHT2[index++ - intermediateindex] = (byte) Huf.val.get(i)[j];
			}
			DHT3 = new byte[index];
			java.lang.System.arraycopy(DHT4, 0, DHT3, 0, oldindex);
			java.lang.System.arraycopy(DHT1, 0, DHT3, oldindex, 17);
			java.lang.System.arraycopy(DHT2, 0, DHT3, oldindex + 17, bytes);
			DHT4 = DHT3;
			oldindex = index;
		}
		DHT4[2] = (byte) (((index - 2) >> 8) & 0xFF);
		DHT4[3] = (byte) ((index - 2) & 0xFF);
		WriteArray(DHT4, out);

		// Start of Scan Header
		byte SOS[] = new byte[14];
		SOS[0] = (byte) 0xFF;
		SOS[1] = (byte) 0xDA;
		SOS[2] = (byte) 0x00;
		SOS[3] = (byte) 12;
		SOS[4] = (byte) JpegObj.NumberOfComponents;
		index = 5;
		for (i = 0; i < SOS[4]; i++) {
			SOS[index++] = (byte) JpegObj.CompID[i];
			SOS[index++] = (byte) ((JpegObj.DCtableNumber[i] << 4) + JpegObj.ACtableNumber[i]);
		}
		SOS[index++] = (byte) JpegObj.Ss;
		SOS[index++] = (byte) JpegObj.Se;
		SOS[index++] = (byte) ((JpegObj.Ah << 4) + JpegObj.Al);
		WriteArray(SOS, out);

	}

	void WriteMarker(byte[] data, DataOutputStream out) {
		try {
			out.write(data, 0, 2);
		} catch (IOException e) {
			System.out.println("IO Error: " + e.getMessage());
		}
	}

	void WriteArray(byte[] data, DataOutputStream out) {
		int length;
		try {
			length = (((int) (data[2] & 0xFF)) << 8) + (int) (data[3] & 0xFF) + 2;
			out.write(data, 0, length);
		} catch (IOException e) {
			System.out.println("IO Error: " + e.getMessage());
		}
	}

	// This class incorporates quality scaling as implemented in the JPEG-6a
	// library.

	/*
	 * DCT - A Java implementation of the Discreet Cosine Transform
	 */

	class DCT {
		/**
		 * DCT Block Size - default 8
		 */
		public int N = 8;

		/**
		 * Image Quality (0-100) - default 80 (good image / good compression)
		 */
		public int QUALITY = 80;

		public Object quantum[] = new Object[2];
		public Object Divisors[] = new Object[2];

		/**
		 * Quantitization Matrix for luminace.
		 */
		public int quantum_luminance[] = new int[N * N];
		public double DivisorsLuminance[] = new double[N * N];

		/**
		 * Quantitization Matrix for chrominance.
		 */
		public int quantum_chrominance[] = new int[N * N];
		public double DivisorsChrominance[] = new double[N * N];

		/**
		 * Constructs a new DCT object. Initializes the cosine transform matrix these are used when computing the DCT and it's inverse. This also initializes
		 * the run length counters and the ZigZag sequence. Note that the image quality can be worse than 25 however the image will be extemely pixelated,
		 * usually to a block size of N.
		 * 
		 * @param QUALITY
		 *            The quality of the image (0 worst - 100 best)
		 * 
		 */
		public DCT(int QUALITY) {
			initMatrix(QUALITY);
		}

		/*
		 * This method sets up the quantization matrix for luminance and chrominance using the Quality parameter.
		 */
		private void initMatrix(int quality) {
			double[] AANscaleFactor = { 1.0, 1.387039845, 1.306562965, 1.175875602, 1.0, 0.785694958, 0.541196100, 0.275899379 };
			int i;
			int j;
			int index;
			int Quality;
			int temp;

			// converting quality setting to that specified in the jpeg_quality_scaling
			// method in the IJG Jpeg-6a C libraries

			Quality = quality;
			if (Quality <= 0)
				Quality = 1;
			if (Quality > 100)
				Quality = 100;
			if (Quality < 50)
				Quality = 5000 / Quality;
			else
				Quality = 200 - Quality * 2;

			// Creating the luminance matrix

			quantum_luminance[0] = 16;
			quantum_luminance[1] = 11;
			quantum_luminance[2] = 10;
			quantum_luminance[3] = 16;
			quantum_luminance[4] = 24;
			quantum_luminance[5] = 40;
			quantum_luminance[6] = 51;
			quantum_luminance[7] = 61;
			quantum_luminance[8] = 12;
			quantum_luminance[9] = 12;
			quantum_luminance[10] = 14;
			quantum_luminance[11] = 19;
			quantum_luminance[12] = 26;
			quantum_luminance[13] = 58;
			quantum_luminance[14] = 60;
			quantum_luminance[15] = 55;
			quantum_luminance[16] = 14;
			quantum_luminance[17] = 13;
			quantum_luminance[18] = 16;
			quantum_luminance[19] = 24;
			quantum_luminance[20] = 40;
			quantum_luminance[21] = 57;
			quantum_luminance[22] = 69;
			quantum_luminance[23] = 56;
			quantum_luminance[24] = 14;
			quantum_luminance[25] = 17;
			quantum_luminance[26] = 22;
			quantum_luminance[27] = 29;
			quantum_luminance[28] = 51;
			quantum_luminance[29] = 87;
			quantum_luminance[30] = 80;
			quantum_luminance[31] = 62;
			quantum_luminance[32] = 18;
			quantum_luminance[33] = 22;
			quantum_luminance[34] = 37;
			quantum_luminance[35] = 56;
			quantum_luminance[36] = 68;
			quantum_luminance[37] = 109;
			quantum_luminance[38] = 103;
			quantum_luminance[39] = 77;
			quantum_luminance[40] = 24;
			quantum_luminance[41] = 35;
			quantum_luminance[42] = 55;
			quantum_luminance[43] = 64;
			quantum_luminance[44] = 81;
			quantum_luminance[45] = 104;
			quantum_luminance[46] = 113;
			quantum_luminance[47] = 92;
			quantum_luminance[48] = 49;
			quantum_luminance[49] = 64;
			quantum_luminance[50] = 78;
			quantum_luminance[51] = 87;
			quantum_luminance[52] = 103;
			quantum_luminance[53] = 121;
			quantum_luminance[54] = 120;
			quantum_luminance[55] = 101;
			quantum_luminance[56] = 72;
			quantum_luminance[57] = 92;
			quantum_luminance[58] = 95;
			quantum_luminance[59] = 98;
			quantum_luminance[60] = 112;
			quantum_luminance[61] = 100;
			quantum_luminance[62] = 103;
			quantum_luminance[63] = 99;

			for (j = 0; j < 64; j++) {
				temp = (quantum_luminance[j] * Quality + 50) / 100;
				if (temp <= 0)
					temp = 1;
				if (temp > 255)
					temp = 255;
				quantum_luminance[j] = temp;
			}
			index = 0;
			for (i = 0; i < 8; i++) {
				for (j = 0; j < 8; j++) {
					// The divisors for the LL&M method (the slow integer method used in
					// jpeg 6a library). This method is currently (04/04/98) incompletely
					// implemented.
					// DivisorsLuminance[index] = ((double) quantum_luminance[index]) << 3;
					// The divisors for the AAN method (the float method used in jpeg 6a library.
					DivisorsLuminance[index] = (double) ((double) 1.0 / ((double) quantum_luminance[index] * AANscaleFactor[i] * AANscaleFactor[j] * (double) 8.0));
					index++;
				}
			}

			// Creating the chrominance matrix

			quantum_chrominance[0] = 17;
			quantum_chrominance[1] = 18;
			quantum_chrominance[2] = 24;
			quantum_chrominance[3] = 47;
			quantum_chrominance[4] = 99;
			quantum_chrominance[5] = 99;
			quantum_chrominance[6] = 99;
			quantum_chrominance[7] = 99;
			quantum_chrominance[8] = 18;
			quantum_chrominance[9] = 21;
			quantum_chrominance[10] = 26;
			quantum_chrominance[11] = 66;
			quantum_chrominance[12] = 99;
			quantum_chrominance[13] = 99;
			quantum_chrominance[14] = 99;
			quantum_chrominance[15] = 99;
			quantum_chrominance[16] = 24;
			quantum_chrominance[17] = 26;
			quantum_chrominance[18] = 56;
			quantum_chrominance[19] = 99;
			quantum_chrominance[20] = 99;
			quantum_chrominance[21] = 99;
			quantum_chrominance[22] = 99;
			quantum_chrominance[23] = 99;
			quantum_chrominance[24] = 47;
			quantum_chrominance[25] = 66;
			quantum_chrominance[26] = 99;
			quantum_chrominance[27] = 99;
			quantum_chrominance[28] = 99;
			quantum_chrominance[29] = 99;
			quantum_chrominance[30] = 99;
			quantum_chrominance[31] = 99;
			quantum_chrominance[32] = 99;
			quantum_chrominance[33] = 99;
			quantum_chrominance[34] = 99;
			quantum_chrominance[35] = 99;
			quantum_chrominance[36] = 99;
			quantum_chrominance[37] = 99;
			quantum_chrominance[38] = 99;
			quantum_chrominance[39] = 99;
			quantum_chrominance[40] = 99;
			quantum_chrominance[41] = 99;
			quantum_chrominance[42] = 99;
			quantum_chrominance[43] = 99;
			quantum_chrominance[44] = 99;
			quantum_chrominance[45] = 99;
			quantum_chrominance[46] = 99;
			quantum_chrominance[47] = 99;
			quantum_chrominance[48] = 99;
			quantum_chrominance[49] = 99;
			quantum_chrominance[50] = 99;
			quantum_chrominance[51] = 99;
			quantum_chrominance[52] = 99;
			quantum_chrominance[53] = 99;
			quantum_chrominance[54] = 99;
			quantum_chrominance[55] = 99;
			quantum_chrominance[56] = 99;
			quantum_chrominance[57] = 99;
			quantum_chrominance[58] = 99;
			quantum_chrominance[59] = 99;
			quantum_chrominance[60] = 99;
			quantum_chrominance[61] = 99;
			quantum_chrominance[62] = 99;
			quantum_chrominance[63] = 99;

			for (j = 0; j < 64; j++) {
				temp = (quantum_chrominance[j] * Quality + 50) / 100;
				if (temp <= 0)
					temp = 1;
				if (temp >= 255)
					temp = 255;
				quantum_chrominance[j] = temp;
			}
			index = 0;
			for (i = 0; i < 8; i++) {
				for (j = 0; j < 8; j++) {
					// The divisors for the LL&M method (the slow integer method used in
					// jpeg 6a library). This method is currently (04/04/98) incompletely
					// implemented.
					// DivisorsChrominance[index] = ((double) quantum_chrominance[index]) << 3;
					// The divisors for the AAN method (the float method used in jpeg 6a library.
					DivisorsChrominance[index] = (double) ((double) 1.0 / ((double) quantum_chrominance[index] * AANscaleFactor[i] * AANscaleFactor[j] * (double) 8.0));
					index++;
				}
			}

			// quantum and Divisors are objects used to hold the appropriate matices

			quantum[0] = quantum_luminance;
			Divisors[0] = DivisorsLuminance;
			quantum[1] = quantum_chrominance;
			Divisors[1] = DivisorsChrominance;

		}

		/*
		 * This method preforms forward DCT on a block of image data using the literal method specified for a 2-D Discrete Cosine Transform. It is included as a
		 * curiosity and can give you an idea of the difference in the compression result (the resulting image quality) by comparing its output to the output of
		 * the AAN method below. It is ridiculously inefficient.
		 */

		// For now the final output is unusable. The associated quantization step
		// needs some tweaking. If you get this part working, please let me know.

		public double[][] forwardDCTExtreme(float input[][]) {
			double output[][] = new double[N][N];

			int v, u, x, y;
			for (v = 0; v < 8; v++) {
				for (u = 0; u < 8; u++) {
					for (x = 0; x < 8; x++) {
						for (y = 0; y < 8; y++) {
							output[v][u] += ((double) input[x][y]) * Math.cos(((double) (2 * x + 1) * (double) u * Math.PI) / (double) 16)
									* Math.cos(((double) (2 * y + 1) * (double) v * Math.PI) / (double) 16);
						}
					}
					output[v][u] *= (double) (0.25) * ((u == 0) ? ((double) 1.0 / Math.sqrt(2)) : (double) 1.0)
							* ((v == 0) ? ((double) 1.0 / Math.sqrt(2)) : (double) 1.0);
				}
			}
			return output;
		}

		/*
		 * This method preforms a DCT on a block of image data using the AAN method as implemented in the IJG Jpeg-6a library.
		 */
		public double[][] forwardDCT(float input[][]) {
			double output[][] = new double[N][N];
			double tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;
			double tmp10, tmp11, tmp12, tmp13;
			double z1, z2, z3, z4, z5, z11, z13;
			int i;
			int j;

			// Subtracts 128 from the input values
			for (i = 0; i < 8; i++) {
				for (j = 0; j < 8; j++) {
					output[i][j] = ((double) input[i][j] - (double) 128.0);
					// input[i][j] -= 128;

				}
			}

			for (i = 0; i < 8; i++) {
				tmp0 = output[i][0] + output[i][7];
				tmp7 = output[i][0] - output[i][7];
				tmp1 = output[i][1] + output[i][6];
				tmp6 = output[i][1] - output[i][6];
				tmp2 = output[i][2] + output[i][5];
				tmp5 = output[i][2] - output[i][5];
				tmp3 = output[i][3] + output[i][4];
				tmp4 = output[i][3] - output[i][4];

				tmp10 = tmp0 + tmp3;
				tmp13 = tmp0 - tmp3;
				tmp11 = tmp1 + tmp2;
				tmp12 = tmp1 - tmp2;

				output[i][0] = tmp10 + tmp11;
				output[i][4] = tmp10 - tmp11;

				z1 = (tmp12 + tmp13) * (double) 0.707106781;
				output[i][2] = tmp13 + z1;
				output[i][6] = tmp13 - z1;

				tmp10 = tmp4 + tmp5;
				tmp11 = tmp5 + tmp6;
				tmp12 = tmp6 + tmp7;

				z5 = (tmp10 - tmp12) * (double) 0.382683433;
				z2 = ((double) 0.541196100) * tmp10 + z5;
				z4 = ((double) 1.306562965) * tmp12 + z5;
				z3 = tmp11 * ((double) 0.707106781);

				z11 = tmp7 + z3;
				z13 = tmp7 - z3;

				output[i][5] = z13 + z2;
				output[i][3] = z13 - z2;
				output[i][1] = z11 + z4;
				output[i][7] = z11 - z4;
			}

			for (i = 0; i < 8; i++) {
				tmp0 = output[0][i] + output[7][i];
				tmp7 = output[0][i] - output[7][i];
				tmp1 = output[1][i] + output[6][i];
				tmp6 = output[1][i] - output[6][i];
				tmp2 = output[2][i] + output[5][i];
				tmp5 = output[2][i] - output[5][i];
				tmp3 = output[3][i] + output[4][i];
				tmp4 = output[3][i] - output[4][i];

				tmp10 = tmp0 + tmp3;
				tmp13 = tmp0 - tmp3;
				tmp11 = tmp1 + tmp2;
				tmp12 = tmp1 - tmp2;

				output[0][i] = tmp10 + tmp11;
				output[4][i] = tmp10 - tmp11;

				z1 = (tmp12 + tmp13) * (double) 0.707106781;
				output[2][i] = tmp13 + z1;
				output[6][i] = tmp13 - z1;

				tmp10 = tmp4 + tmp5;
				tmp11 = tmp5 + tmp6;
				tmp12 = tmp6 + tmp7;

				z5 = (tmp10 - tmp12) * (double) 0.382683433;
				z2 = ((double) 0.541196100) * tmp10 + z5;
				z4 = ((double) 1.306562965) * tmp12 + z5;
				z3 = tmp11 * ((double) 0.707106781);

				z11 = tmp7 + z3;
				z13 = tmp7 - z3;

				output[5][i] = z13 + z2;
				output[3][i] = z13 - z2;
				output[1][i] = z11 + z4;
				output[7][i] = z11 - z4;
			}

			return output;
		}

		/*
		 * This method quantitizes data and rounds it to the nearest integer.
		 */
		public int[] quantizeBlock(double inputData[][], int code) {
			int outputData[] = new int[N * N];
			int i, j;
			int index;
			index = 0;
			for (i = 0; i < 8; i++) {
				for (j = 0; j < 8; j++) {
					// The second line results in significantly better compression.
					outputData[index] = (int) ((inputData[i][j] * (((double[]) (Divisors[code]))[index])));
					// outputData[index] = (int)( Math.round(inputData[i][j] * (((double[]) (Divisors[code]))[index])) );
					// outputData[index] = (int)(((inputData[i][j] * (((double[]) (Divisors[code]))[index])) + 16384.5) -16384);
					index++;
				}
			}

			return outputData;
		}

		/*
		 * This is the method for quantizing a block DCT'ed with forwardDCTExtreme This method quantitizes data and rounds it to the nearest integer.
		 */
		public int[] quantizeBlockExtreme(double inputData[][], int code) {
			int outputData[] = new int[N * N];
			int i, j;
			int index;
			index = 0;
			for (i = 0; i < 8; i++) {
				for (j = 0; j < 8; j++) {
					// outputData[index] = (int)( (inputData[i][j] / (double)(((int[]) (quantum[code]))[index])) );
					outputData[index] = (int) (Math.floor(inputData[i][j] / (double) (((int[]) (quantum[code]))[index])));
					index++;
				}
			}

			return outputData;
		}
	}

	// This class was modified by James R. Weeks on 3/27/98.
	// It now incorporates Huffman table derivation as in the C jpeg library
	// from the IJG, Jpeg-6a.

	class Huffman {
		int bufferPutBits, bufferPutBuffer;
		public int ImageHeight;
		public int ImageWidth;
		public int DC_matrix0[][];
		public int AC_matrix0[][];
		public int DC_matrix1[][];
		public int AC_matrix1[][];
		public Object DC_matrix[];
		public Object AC_matrix[];
		public int code;
		public int NumOfDCTables;
		public int NumOfACTables;
		public int[] bitsDCluminance = { 0x00, 0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0 };
		public int[] valDCluminance = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
		public int[] bitsDCchrominance = { 0x01, 0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 };
		public int[] valDCchrominance = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
		public int[] bitsACluminance = { 0x10, 0, 2, 1, 3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 0x7d };
		public int[] valACluminance = { 0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12, 0x21, 0x31, 0x41, 0x06, 0x13, 0x51, 0x61, 0x07, 0x22, 0x71, 0x14, 0x32,
				0x81, 0x91, 0xa1, 0x08, 0x23, 0x42, 0xb1, 0xc1, 0x15, 0x52, 0xd1, 0xf0, 0x24, 0x33, 0x62, 0x72, 0x82, 0x09, 0x0a, 0x16, 0x17, 0x18, 0x19, 0x1a,
				0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53, 0x54, 0x55,
				0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x83, 0x84, 0x85,
				0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2,
				0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5, 0xd6, 0xd7, 0xd8,
				0xd9, 0xda, 0xe1, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf1, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8, 0xf9, 0xfa };
		public int[] bitsACchrominance = { 0x11, 0, 2, 1, 2, 4, 4, 3, 4, 7, 5, 4, 4, 0, 1, 2, 0x77 };;
		public int[] valACchrominance = { 0x00, 0x01, 0x02, 0x03, 0x11, 0x04, 0x05, 0x21, 0x31, 0x06, 0x12, 0x41, 0x51, 0x07, 0x61, 0x71, 0x13, 0x22, 0x32,
				0x81, 0x08, 0x14, 0x42, 0x91, 0xa1, 0xb1, 0xc1, 0x09, 0x23, 0x33, 0x52, 0xf0, 0x15, 0x62, 0x72, 0xd1, 0x0a, 0x16, 0x24, 0x34, 0xe1, 0x25, 0xf1,
				0x17, 0x18, 0x19, 0x1a, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53,
				0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x82,
				0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8,
				0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5,
				0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8, 0xf9, 0xfa };
		public ArrayList<int[]> bits;
		public ArrayList<int[]> val;

		/*
		 * The Huffman class constructor
		 */
		public Huffman(int Width, int Height) {

			bits = new ArrayList<int[]>(4);
			bits.add(bitsDCluminance);
			bits.add(bitsACluminance);
			bits.add(bitsDCchrominance);
			bits.add(bitsACchrominance);
			val = new ArrayList<int[]>(4);
			val.add(valDCluminance);
			val.add(valACluminance);
			val.add(valDCchrominance);
			val.add(valACchrominance);
			initHuf();
			// code=code;
			ImageWidth = Width;
			ImageHeight = Height;

		}

		/**
		 * HuffmanBlockEncoder run length encodes and Huffman encodes the quantized data.
		 **/

		public void HuffmanBlockEncoder(DataOutputStream outStream, int zigzag[], int prec, int DCcode, int ACcode) {
			int temp, temp2, nbits, k, r, i;

			NumOfDCTables = 2;
			NumOfACTables = 2;

			// The DC portion

			temp = temp2 = zigzag[0] - prec;
			if (temp < 0) {
				temp = -temp;
				temp2--;
			}
			nbits = 0;
			while (temp != 0) {
				nbits++;
				temp >>= 1;
			}
			// if (nbits > 11) nbits = 11;
			bufferIt(outStream, ((int[][]) DC_matrix[DCcode])[nbits][0], ((int[][]) DC_matrix[DCcode])[nbits][1]);
			// The arguments in bufferIt are code and size.
			if (nbits != 0) {
				bufferIt(outStream, temp2, nbits);
			}

			// The AC portion

			r = 0;

			for (k = 1; k < 64; k++) {
				if ((temp = zigzag[jpegNaturalOrder[k]]) == 0) {
					r++;
				} else {
					while (r > 15) {
						bufferIt(outStream, ((int[][]) AC_matrix[ACcode])[0xF0][0], ((int[][]) AC_matrix[ACcode])[0xF0][1]);
						r -= 16;
					}
					temp2 = temp;
					if (temp < 0) {
						temp = -temp;
						temp2--;
					}
					nbits = 1;
					while ((temp >>= 1) != 0) {
						nbits++;
					}
					i = (r << 4) + nbits;
					bufferIt(outStream, ((int[][]) AC_matrix[ACcode])[i][0], ((int[][]) AC_matrix[ACcode])[i][1]);
					bufferIt(outStream, temp2, nbits);

					r = 0;
				}
			}

			if (r > 0) {
				bufferIt(outStream, ((int[][]) AC_matrix[ACcode])[0][0], ((int[][]) AC_matrix[ACcode])[0][1]);
			}

		}

		// Uses an integer long (32 bits) buffer to store the Huffman encoded bits
		// and sends them to outStream by the byte.

		void bufferIt(DataOutputStream outStream, int code, int size) {
			int PutBuffer = code;
			int PutBits = bufferPutBits;

			PutBuffer &= (1 << size) - 1;
			PutBits += size;
			PutBuffer <<= 24 - PutBits;
			PutBuffer |= bufferPutBuffer;

			while (PutBits >= 8) {
				int c = ((PutBuffer >> 16) & 0xFF);
				try {
					outStream.write(c);
				} catch (IOException e) {
					System.out.println("IO Error: " + e.getMessage());
				}
				if (c == 0xFF) {
					try {
						outStream.write(0);
					} catch (IOException e) {
						System.out.println("IO Error: " + e.getMessage());
					}
				}
				PutBuffer <<= 8;
				PutBits -= 8;
			}
			bufferPutBuffer = PutBuffer;
			bufferPutBits = PutBits;

		}

		void flushBuffer(DataOutputStream outStream) {
			int PutBuffer = bufferPutBuffer;
			int PutBits = bufferPutBits;
			while (PutBits >= 8) {
				int c = ((PutBuffer >> 16) & 0xFF);
				try {
					outStream.write(c);
				} catch (IOException e) {
					System.out.println("IO Error: " + e.getMessage());
				}
				if (c == 0xFF) {
					try {
						outStream.write(0);
					} catch (IOException e) {
						System.out.println("IO Error: " + e.getMessage());
					}
				}
				PutBuffer <<= 8;
				PutBits -= 8;
			}
			if (PutBits > 0) {
				int c = ((PutBuffer >> 16) & 0xFF);
				try {
					outStream.write(c);
				} catch (IOException e) {
					System.out.println("IO Error: " + e.getMessage());
				}
			}
		}

		/*
		 * Initialisation of the Huffman codes for Luminance and Chrominance. This code results in the same tables created in the IJG Jpeg-6a library.
		 */

		public void initHuf() {
			DC_matrix0 = new int[12][2];
			DC_matrix1 = new int[12][2];
			AC_matrix0 = new int[255][2];
			AC_matrix1 = new int[255][2];
			DC_matrix = new Object[2];
			AC_matrix = new Object[2];
			int p, l, i, lastp, si, code;
			int[] huffsize = new int[257];
			int[] huffcode = new int[257];

			/*
			 * init of the DC values for the chrominance [][0] is the code [][1] is the number of bit
			 */

			p = 0;
			for (l = 1; l <= 16; l++) {
				for (i = 1; i <= bitsDCchrominance[l]; i++) {
					huffsize[p++] = l;
				}
			}
			huffsize[p] = 0;
			lastp = p;

			code = 0;
			si = huffsize[0];
			p = 0;
			while (huffsize[p] != 0) {
				while (huffsize[p] == si) {
					huffcode[p++] = code;
					code++;
				}
				code <<= 1;
				si++;
			}

			for (p = 0; p < lastp; p++) {
				DC_matrix1[valDCchrominance[p]][0] = huffcode[p];
				DC_matrix1[valDCchrominance[p]][1] = huffsize[p];
			}

			/*
			 * Init of the AC hufmann code for the chrominance matrix [][][0] is the code & matrix[][][1] is the number of bit needed
			 */

			p = 0;
			for (l = 1; l <= 16; l++) {
				for (i = 1; i <= bitsACchrominance[l]; i++) {
					huffsize[p++] = l;
				}
			}
			huffsize[p] = 0;
			lastp = p;

			code = 0;
			si = huffsize[0];
			p = 0;
			while (huffsize[p] != 0) {
				while (huffsize[p] == si) {
					huffcode[p++] = code;
					code++;
				}
				code <<= 1;
				si++;
			}

			for (p = 0; p < lastp; p++) {
				AC_matrix1[valACchrominance[p]][0] = huffcode[p];
				AC_matrix1[valACchrominance[p]][1] = huffsize[p];
			}

			/*
			 * init of the DC values for the luminance [][0] is the code [][1] is the number of bit
			 */
			p = 0;
			for (l = 1; l <= 16; l++) {
				for (i = 1; i <= bitsDCluminance[l]; i++) {
					huffsize[p++] = l;
				}
			}
			huffsize[p] = 0;
			lastp = p;

			code = 0;
			si = huffsize[0];
			p = 0;
			while (huffsize[p] != 0) {
				while (huffsize[p] == si) {
					huffcode[p++] = code;
					code++;
				}
				code <<= 1;
				si++;
			}

			for (p = 0; p < lastp; p++) {
				DC_matrix0[valDCluminance[p]][0] = huffcode[p];
				DC_matrix0[valDCluminance[p]][1] = huffsize[p];
			}

			/*
			 * Init of the AC hufmann code for luminance matrix [][][0] is the code & matrix[][][1] is the number of bit
			 */

			p = 0;
			for (l = 1; l <= 16; l++) {
				for (i = 1; i <= bitsACluminance[l]; i++) {
					huffsize[p++] = l;
				}
			}
			huffsize[p] = 0;
			lastp = p;

			code = 0;
			si = huffsize[0];
			p = 0;
			while (huffsize[p] != 0) {
				while (huffsize[p] == si) {
					huffcode[p++] = code;
					code++;
				}
				code <<= 1;
				si++;
			}
			for (int q = 0; q < lastp; q++) {
				AC_matrix0[valACluminance[q]][0] = huffcode[q];
				AC_matrix0[valACluminance[q]][1] = huffsize[q];
			}

			DC_matrix[0] = DC_matrix0;
			DC_matrix[1] = DC_matrix1;
			AC_matrix[0] = AC_matrix0;
			AC_matrix[1] = AC_matrix1;
		}

	}

	/*
	 * JpegInfo - Given an image, sets default information about it and divides it into its constituant components, downsizing those that need to be.
	 */

	class JpegInfo {
		String Comment;
		public BufferedImage imageobj;
		public int imageHeight;
		public int imageWidth;
		public int BlockWidth[];
		public int BlockHeight[];

		// the following are set as the default
		public int Precision = 8;
		public int NumberOfComponents = 3;
		public Object Components[];
		public int[] CompID = { 1, 2, 3 };
		public int[] HsampFactor = { 1, 1, 1 };
		public int[] VsampFactor = { 1, 1, 1 };
		public int[] QtableNumber = { 0, 1, 1 };
		public int[] DCtableNumber = { 0, 1, 1 };
		public int[] ACtableNumber = { 0, 1, 1 };
		public boolean[] lastColumnIsDummy = { false, false, false };
		public boolean[] lastRowIsDummy = { false, false, false };
		public int Ss = 0;
		public int Se = 63;
		public int Ah = 0;
		public int Al = 0;
		public int compWidth[], compHeight[];
		public int MaxHsampFactor;
		public int MaxVsampFactor;

		public JpegInfo(BufferedImage image) {
			Components = new Object[NumberOfComponents];
			compWidth = new int[NumberOfComponents];
			compHeight = new int[NumberOfComponents];
			BlockWidth = new int[NumberOfComponents];
			BlockHeight = new int[NumberOfComponents];
			imageobj = image;
			imageWidth = image.getWidth(null);
			imageHeight = image.getHeight(null);
			Comment = "JPEG Encoder Copyright 1998, James R. Weeks and BioElectroMech.  ";
			getYCCArray();
		}

		public void setComment(String comment) {
			Comment.concat(comment);
		}

		public String getComment() {
			return Comment;
		}

		/*
		 * This method creates and fills three arrays, Y, Cb, and Cr using the input image.
		 */

		private void getYCCArray() {
			int values[] = new int[imageWidth * imageHeight];
			int r, g, b, y, x;
			// In order to minimize the chance that grabPixels will throw an exception
			// it may be necessary to grab some pixels every few scanlines and process
			// those before going for more. The time expense may be prohibitive.
			// However, for a situation where memory overhead is a concern, this may be
			// the only choice.

			// PixelGrabber grabber = new PixelGrabber(imageobj.getSource(), 0, 0, imageWidth, imageHeight, values, 0, imageWidth);
			// imageobj.getRGB(values, 0, imageWidth, 0, 0, imageWidth, imageHeight);
			imageobj.getRGB(0, 0, imageWidth, imageHeight, values, 0, imageWidth);

			MaxHsampFactor = 1;
			MaxVsampFactor = 1;
			for (y = 0; y < NumberOfComponents; y++) {
				MaxHsampFactor = Math.max(MaxHsampFactor, HsampFactor[y]);
				MaxVsampFactor = Math.max(MaxVsampFactor, VsampFactor[y]);
			}
			for (y = 0; y < NumberOfComponents; y++) {
				compWidth[y] = (((imageWidth % 8 != 0) ? ((int) Math.ceil((double) imageWidth / 8.0)) * 8 : imageWidth) / MaxHsampFactor) * HsampFactor[y];
				if (compWidth[y] != ((imageWidth / MaxHsampFactor) * HsampFactor[y])) {
					lastColumnIsDummy[y] = true;
				}
				// results in a multiple of 8 for compWidth
				// this will make the rest of the program fail for the unlikely
				// event that someone tries to compress an 16 x 16 pixel image
				// which would of course be worse than pointless
				BlockWidth[y] = (int) Math.ceil((double) compWidth[y] / 8.0);
				compHeight[y] = (((imageHeight % 8 != 0) ? ((int) Math.ceil((double) imageHeight / 8.0)) * 8 : imageHeight) / MaxVsampFactor) * VsampFactor[y];
				if (compHeight[y] != ((imageHeight / MaxVsampFactor) * VsampFactor[y])) {
					lastRowIsDummy[y] = true;
				}
				BlockHeight[y] = (int) Math.ceil((double) compHeight[y] / 8.0);
			}
			/*
			 * try { if(grabber.grabPixels() != true) { try { throw new Exception("Grabber returned false: " + grabber.status()); //throw new
			 * AWTException("Grabber returned false: " + grabber.status()); } catch (Exception e) {}; } } catch (Exception e) {};
			 */
			float Y[][] = new float[compHeight[0]][compWidth[0]];
			float Cr1[][] = new float[compHeight[0]][compWidth[0]];
			float Cb1[][] = new float[compHeight[0]][compWidth[0]];
			int index = 0;
			for (y = 0; y < imageHeight; ++y) {
				for (x = 0; x < imageWidth; ++x) {
					r = ((values[index] >> 16) & 0xff);
					g = ((values[index] >> 8) & 0xff);
					b = (values[index] & 0xff);

					// The following three lines are a more correct color conversion but
					// the current conversion technique is sufficient and results in a higher
					// compression rate.
					// Y[y][x] = 16 + (float)(0.8588*(0.299 * (float)r + 0.587 * (float)g + 0.114 * (float)b ));
					// Cb1[y][x] = 128 + (float)(0.8784*(-0.16874 * (float)r - 0.33126 * (float)g + 0.5 * (float)b));
					// Cr1[y][x] = 128 + (float)(0.8784*(0.5 * (float)r - 0.41869 * (float)g - 0.08131 * (float)b));
					Y[y][x] = (float) ((0.299 * (float) r + 0.587 * (float) g + 0.114 * (float) b));
					Cb1[y][x] = 128 + (float) ((-0.16874 * (float) r - 0.33126 * (float) g + 0.5 * (float) b));
					Cr1[y][x] = 128 + (float) ((0.5 * (float) r - 0.41869 * (float) g - 0.08131 * (float) b));
					index++;
				}
			}

			// Need a way to set the H and V sample factors before allowing downsampling.
			// For now (04/04/98) downsampling must be hard coded.
			// Until a better downsampler is implemented, this will not be done.
			// Downsampling is currently supported. The downsampling method here
			// is a simple box filter.

			Components[0] = Y;
			// Cb2 = DownSample(Cb1, 1);
			Components[1] = Cb1;
			// Cr2 = DownSample(Cr1, 2);
			Components[2] = Cr1;
		}

		float[][] DownSample(float[][] C, int comp) {
			int inrow, incol;
			int outrow, outcol;
			float output[][];
			int bias;
			inrow = 0;
			incol = 0;
			output = new float[compHeight[comp]][compWidth[comp]];
			for (outrow = 0; outrow < compHeight[comp]; outrow++) {
				bias = 1;
				for (outcol = 0; outcol < compWidth[comp]; outcol++) {
					output[outrow][outcol] = (C[inrow][incol++] + C[inrow++][incol--] + C[inrow][incol++] + C[inrow--][incol++] + (float) bias) / (float) 4.0;
					bias ^= 3;
				}
				inrow += 2;
				incol = 0;
			}
			return output;
		}
	}

	/*
	 * LICENSE.TXT ************************************************************** The JpegEncoder and its associated classes are Copyright (c) 1998, James R.
	 * Weeks and BioElectroMech. This software is based in part on the work of the Independent JPEG Group.
	 * 
	 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
	 * 
	 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions, all files included with the source code, and the
	 * following disclaimer.
	 * 
	 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation
	 * and/or other materials provided with the distribution.
	 * 
	 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
	 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
	 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
	 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
	 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	 * **************************************************************************
	 */

	/*
	 * IJGREADME.TXT ************************************************************ The Independent JPEG Group's JPEG software
	 * ==========================================
	 * 
	 * README for release 6a of 7-Feb-96 =================================
	 * 
	 * This distribution contains the sixth public release of the Independent JPEG Group's free JPEG software. You are welcome to redistribute this software and
	 * to use it for any purpose, subject to the conditions under LEGAL ISSUES, below.
	 * 
	 * Serious users of this software (particularly those incorporating it into larger programs) should contact IJG at jpeg-info@uunet.uu.net to be added to our
	 * electronic mailing list. Mailing list members are notified of updates and have a chance to participate in technical discussions, etc.
	 * 
	 * This software is the work of Tom Lane, Philip Gladstone, Luis Ortiz, Jim Boucher, Lee Crocker, Julian Minguillon, George Phillips, Davide Rossi, Ge'
	 * Weijers, and other members of the Independent JPEG Group.
	 * 
	 * IJG is not affiliated with the official ISO JPEG standards committee.
	 * 
	 * 
	 * DOCUMENTATION ROADMAP =====================
	 * 
	 * This file contains the following sections:
	 * 
	 * OVERVIEW General description of JPEG and the IJG software. LEGAL ISSUES Copyright, lack of warranty, terms of distribution. REFERENCES Where to learn
	 * more about JPEG. ARCHIVE LOCATIONS Where to find newer versions of this software. RELATED SOFTWARE Other stuff you should get. FILE FORMAT WARS Software
	 * *not* to get. TO DO Plans for future IJG releases.
	 * 
	 * Other documentation files in the distribution are:
	 * 
	 * User documentation: install.doc How to configure and install the IJG software. usage.doc Usage instructions for cjpeg, djpeg, jpegtran, rdjpgcom, and
	 * wrjpgcom..1 Unix-style man pages for programs (same info as usage.doc). wizard.doc Advanced usage instructions for JPEG wizards only. change.log
	 * Version-to-version change highlights. Programmer and internal documentation: libjpeg.doc How to use the JPEG library in your own programs. example.c
	 * Sample code for calling the JPEG library. structure.doc Overview of the JPEG library's internal structure. filelist.doc Road map of IJG files.
	 * coderules.doc Coding style rules --- please read if you contribute code.
	 * 
	 * Please read at least the files install.doc and usage.doc. Useful information can also be found in the JPEG FAQ (Frequently Asked Questions) article. See
	 * ARCHIVE LOCATIONS below to find out where to obtain the FAQ article.
	 * 
	 * If you want to understand how the JPEG code works, we suggest reading one or more of the REFERENCES, then looking at the documentation files (in roughly
	 * the order listed) before diving into the code.
	 * 
	 * 
	 * OVERVIEW ========
	 * 
	 * This package contains C software to implement JPEG image compression and decompression. JPEG (pronounced "jay-peg") is a standardized compression method
	 * for full-color and gray-scale images. JPEG is intended for compressing "real-world" scenes; line drawings, cartoons and other non-realistic images are
	 * not its strong suit. JPEG is lossy, meaning that the output image is not exactly identical to the input image. Hence you must not use JPEG if you have to
	 * have identical output bits. However, on typical photographic images, very good compression levels can be obtained with no visible change, and remarkably
	 * high compression levels are possible if you can tolerate a low-quality image. For more details, see the references, or just experiment with various
	 * compression settings.
	 * 
	 * This software implements JPEG baseline, extended-sequential, and progressive compression processes. Provision is made for supporting all variants of
	 * these processes, although some uncommon parameter settings aren't implemented yet. For legal reasons, we are not distributing code for the
	 * arithmetic-coding variants of JPEG; see LEGAL ISSUES. We have made no provision for supporting the hierarchical or lossless processes defined in the
	 * standard.
	 * 
	 * We provide a set of library routines for reading and writing JPEG image files, plus two sample applications "cjpeg" and "djpeg", which use the library to
	 * perform conversion between JPEG and some other popular image file formats. The library is intended to be reused in other applications.
	 * 
	 * In order to support file conversion and viewing software, we have included considerable functionality beyond the bare JPEG coding/decoding capability;
	 * for example, the color quantization modules are not strictly part of JPEG decoding, but they are essential for output to colormapped file formats or
	 * colormapped displays. These extra functions can be compiled out of the library if not required for a particular application. We have also included
	 * "jpegtran", a utility for lossless transcoding between different JPEG processes, and "rdjpgcom" and "wrjpgcom", two simple applications for inserting and
	 * extracting textual comments in JFIF files.
	 * 
	 * The emphasis in designing this software has been on achieving portability and flexibility, while also making it fast enough to be useful. In particular,
	 * the software is not intended to be read as a tutorial on JPEG. (See the REFERENCES section for introductory material.) Rather, it is intended to be
	 * reliable, portable, industrial-strength code. We do not claim to have achieved that goal in every aspect of the software, but we strive for it.
	 * 
	 * We welcome the use of this software as a component of commercial products. No royalty is required, but we do ask for an acknowledgement in product
	 * documentation, as described under LEGAL ISSUES.
	 * 
	 * 
	 * LEGAL ISSUES ============
	 * 
	 * In plain English:
	 * 
	 * 1. We don't promise that this software works. (But if you find any bugs, please let us know!) 2. You can use this software for whatever you want. You
	 * don't have to pay us. 3. You may not pretend that you wrote this software. If you use it in a program, you must acknowledge somewhere in your
	 * documentation that you've used the IJG code.
	 * 
	 * In legalese:
	 * 
	 * The authors make NO WARRANTY or representation, either express or implied, with respect to this software, its quality, accuracy, merchantability, or
	 * fitness for a particular purpose. This software is provided "AS IS", and you, its user, assume the entire risk as to its quality and accuracy.
	 * 
	 * This software is copyright (C) 1991-1996, Thomas G. Lane. All Rights Reserved except as specified below.
	 * 
	 * Permission is hereby granted to use, copy, modify, and distribute this software (or portions thereof) for any purpose, without fee, subject to these
	 * conditions: (1) If any part of the source code for this software is distributed, then this README file must be included, with this copyright and
	 * no-warranty notice unaltered; and any additions, deletions, or changes to the original files must be clearly indicated in accompanying documentation. (2)
	 * If only executable code is distributed, then the accompanying documentation must state that "this software is based in part on the work of the
	 * Independent JPEG Group". (3) Permission for use of this software is granted only if the user accepts full responsibility for any undesirable
	 * consequences; the authors accept NO LIABILITY for damages of any kind.
	 * 
	 * These conditions apply to any software derived from or based on the IJG code, not just to the unmodified library. If you use our work, you ought to
	 * acknowledge us.
	 * 
	 * Permission is NOT granted for the use of any IJG author's name or company name in advertising or publicity relating to this software or products derived
	 * from it. This software may be referred to only as "the Independent JPEG Group's software".
	 * 
	 * We specifically permit and encourage the use of this software as the basis of commercial products, provided that all warranty or liability claims are
	 * assumed by the product vendor.
	 * 
	 * 
	 * ansi2knr.c is included in this distribution by permission of L. Peter Deutsch, sole proprietor of its copyright holder, Aladdin Enterprises of Menlo
	 * Park, CA. ansi2knr.c is NOT covered by the above copyright and conditions, but instead by the usual distribution terms of the Free Software Foundation;
	 * principally, that you must include source code if you redistribute it. (See the file ansi2knr.c for full details.) However, since ansi2knr.c is not
	 * needed as part of any program generated from the IJG code, this does not limit you more than the foregoing paragraphs do.
	 * 
	 * The configuration script "configure" was produced with GNU Autoconf. It is copyright by the Free Software Foundation but is freely distributable.
	 * 
	 * It appears that the arithmetic coding option of the JPEG spec is covered by patents owned by IBM, AT&T, and Mitsubishi. Hence arithmetic coding cannot
	 * legally be used without obtaining one or more licenses. For this reason, support for arithmetic coding has been removed from the free JPEG software.
	 * (Since arithmetic coding provides only a marginal gain over the unpatented Huffman mode, it is unlikely that very many implementations will support it.)
	 * So far as we are aware, there are no patent restrictions on the remaining code.
	 * 
	 * WARNING: Unisys has begun to enforce their patent on LZW compression against GIF encoders and decoders. You will need a license from Unisys to use the
	 * included rdgif.c or wrgif.c files in a commercial or shareware application. At this time, Unisys is not enforcing their patent against freeware, so
	 * distribution of this package remains legal. However, we intend to remove GIF support from the IJG package as soon as a suitable replacement format
	 * becomes reasonably popular.
	 * 
	 * We are required to state that "The Graphics Interchange Format(c) is the Copyright property of CompuServe Incorporated. GIF(sm) is a Service Mark
	 * property of CompuServe Incorporated."
	 * 
	 * 
	 * REFERENCES ==========
	 * 
	 * We highly recommend reading one or more of these references before trying to understand the innards of the JPEG software.
	 * 
	 * The best short technical introduction to the JPEG compression algorithm is Wallace, Gregory K. "The JPEG Still Picture Compression Standard",
	 * Communications of the ACM, April 1991 (vol. 34 no. 4), pp. 30-44. (Adjacent articles in that issue discuss MPEG motion picture compression, applications
	 * of JPEG, and related topics.) If you don't have the CACM issue handy, a PostScript file containing a revised version of Wallace's article is available at
	 * ftp.uu.net, graphics/jpeg/wallace.ps.gz. The file (actually a preprint for an article that appeared in IEEE Trans. Consumer Electronics) omits the sample
	 * images that appeared in CACM, but it includes corrections and some added material. Note: the Wallace article is copyright ACM and IEEE, and it may not be
	 * used for commercial purposes.
	 * 
	 * A somewhat less technical, more leisurely introduction to JPEG can be found in "The Data Compression Book" by Mark Nelson, published by M&T Books
	 * (Redwood City, CA), 1991, ISBN 1-55851-216-0. This book provides good explanations and example C code for a multitude of compression methods including
	 * JPEG. It is an excellent source if you are comfortable reading C code but don't know much about data compression in general. The book's JPEG sample code
	 * is far from industrial-strength, but when you are ready to look at a full implementation, you've got one here...
	 * 
	 * The best full description of JPEG is the textbook "JPEG Still Image Data Compression Standard" by William B. Pennebaker and Joan L. Mitchell, published
	 * by Van Nostrand Reinhold, 1993, ISBN 0-442-01272-1. Price US$59.95, 638 pp. The book includes the complete text of the ISO JPEG standards (DIS 10918-1
	 * and draft DIS 10918-2). This is by far the most complete exposition of JPEG in existence, and we highly recommend it.
	 * 
	 * The JPEG standard itself is not available electronically; you must order a paper copy through ISO or ITU. (Unless you feel a need to own a certified
	 * official copy, we recommend buying the Pennebaker and Mitchell book instead; it's much cheaper and includes a great deal of useful explanatory material.)
	 * In the USA, copies of the standard may be ordered from ANSI Sales at (212) 642-4900, or from Global Engineering Documents at (800) 854-7179. (ANSI
	 * doesn't take credit card orders, but Global does.) It's not cheap: as of 1992, ANSI was charging $95 for Part 1 and $47 for Part 2, plus 7%
	 * shipping/handling. The standard is divided into two parts, Part 1 being the actual specification, while Part 2 covers compliance testing methods. Part 1
	 * is titled "Digital Compression and Coding of Continuous-tone Still Images, Part 1: Requirements and guidelines" and has document numbers ISO/IEC IS
	 * 10918-1, ITU-T T.81. Part 2 is titled "Digital Compression and Coding of Continuous-tone Still Images, Part 2: Compliance testing" and has document
	 * numbers ISO/IEC IS 10918-2, ITU-T T.83.
	 * 
	 * Extensions to the original JPEG standard are defined in JPEG Part 3, a new ISO document. Part 3 is undergoing ISO balloting and is expected to be
	 * approved by the end of 1995; it will have document numbers ISO/IEC IS 10918-3, ITU-T T.84. IJG currently does not support any Part 3 extensions.
	 * 
	 * The JPEG standard does not specify all details of an interchangeable file format. For the omitted details we follow the "JFIF" conventions, revision
	 * 1.02. A copy of the JFIF spec is available from: Literature Department C-Cube Microsystems, Inc. 1778 McCarthy Blvd. Milpitas, CA 95035 phone (408)
	 * 944-6300, fax (408) 944-6314 A PostScript version of this document is available at ftp.uu.net, file graphics/jpeg/jfif.ps.gz. It can also be obtained by
	 * e-mail from the C-Cube mail server, netlib@c3.pla.ca.us. Send the message "send jfif_ps from jpeg" to the server to obtain the JFIF document; send the
	 * message "help" if you have trouble.
	 * 
	 * The TIFF 6.0 file format specification can be obtained by FTP from sgi.com (192.48.153.1), file graphics/tiff/TIFF6.ps.Z; or you can order a printed copy
	 * from Aldus Corp. at (206) 628-6593. The JPEG incorporation scheme found in the TIFF 6.0 spec of 3-June-92 has a number of serious problems. IJG does not
	 * recommend use of the TIFF 6.0 design (TIFF Compression tag 6). Instead, we recommend the JPEG design proposed by TIFF Technical Note #2 (Compression tag
	 * 7). Copies of this Note can be obtained from sgi.com or from ftp.uu.net:/graphics/jpeg/. It is expected that the next revision of the TIFF spec will
	 * replace the 6.0 JPEG design with the Note's design. Although IJG's own code does not support TIFF/JPEG, the free libtiff library uses our library to
	 * implement TIFF/JPEG per the Note. libtiff is available from sgi.com:/graphics/tiff/.
	 * 
	 * 
	 * ARCHIVE LOCATIONS =================
	 * 
	 * The "official" archive site for this software is ftp.uu.net (Internet address 192.48.96.9). The most recent released version can always be found there in
	 * directory graphics/jpeg. This particular version will be archived as graphics/jpeg/jpegsrc.v6a.tar.gz. If you are on the Internet, you can retrieve files
	 * from ftp.uu.net by standard anonymous FTP. If you don't have FTP access, UUNET's archives are also available via UUCP; contact help@uunet.uu.net for
	 * information on retrieving files that way.
	 * 
	 * Numerous Internet sites maintain copies of the UUNET files. However, only ftp.uu.net is guaranteed to have the latest official version.
	 * 
	 * You can also obtain this software in DOS-compatible "zip" archive format from the SimTel archives (ftp.coast.net:/SimTel/msdos/graphics/), or on
	 * CompuServe in the Graphics Support forum (GO CIS:GRAPHSUP), library 12 "JPEG Tools". Again, these versions may sometimes lag behind the ftp.uu.net
	 * release.
	 * 
	 * The JPEG FAQ (Frequently Asked Questions) article is a useful source of general information about JPEG. It is updated constantly and therefore is not
	 * included in this distribution. The FAQ is posted every two weeks to Usenet newsgroups comp.graphics.misc, news.answers, and other groups. You can always
	 * obtain the latest version from the news.answers archive at rtfm.mit.edu. By FTP, fetch /pub/usenet/news.answers/jpeg-faq/part1 and .../part2. If you
	 * don't have FTP, send e-mail to mail-server@rtfm.mit.edu with body send usenet/news.answers/jpeg-faq/part1 send usenet/news.answers/jpeg-faq/part2
	 * 
	 * 
	 * RELATED SOFTWARE ================
	 * 
	 * Numerous viewing and image manipulation programs now support JPEG. (Quite a few of them use this library to do so.) The JPEG FAQ described above lists
	 * some of the more popular free and shareware viewers, and tells where to obtain them on Internet.
	 * 
	 * If you are on a Unix machine, we highly recommend Jef Poskanzer's free PBMPLUS image software, which provides many useful operations on PPM-format image
	 * files. In particular, it can convert PPM images to and from a wide range of other formats. You can obtain this package by FTP from ftp.x.org
	 * (contrib/pbmplus*.tar.Z) or ftp.ee.lbl.gov (pbmplus*.tar.Z). There is also a newer update of this package called NETPBM, available from
	 * wuarchive.wustl.edu under directory /graphics/graphics/packages/NetPBM/. Unfortunately PBMPLUS/NETPBM is not nearly as portable as the IJG software is;
	 * you are likely to have difficulty making it work on any non-Unix machine.
	 * 
	 * A different free JPEG implementation, written by the PVRG group at Stanford, is available from havefun.stanford.edu in directory pub/jpeg. This program
	 * is designed for research and experimentation rather than production use; it is slower, harder to use, and less portable than the IJG code, but it is
	 * easier to read and modify. Also, the PVRG code supports lossless JPEG, which we do not.
	 * 
	 * 
	 * FILE FORMAT WARS ================
	 * 
	 * Some JPEG programs produce files that are not compatible with our library. The root of the problem is that the ISO JPEG committee failed to specify a
	 * concrete file format. Some vendors "filled in the blanks" on their own, creating proprietary formats that no one else could read. (For example, none of
	 * the early commercial JPEG implementations for the Macintosh were able to exchange compressed files.)
	 * 
	 * The file format we have adopted is called JFIF (see REFERENCES). This format has been agreed to by a number of major commercial JPEG vendors, and it has
	 * become the de facto standard. JFIF is a minimal or "low end" representation. We recommend the use of TIFF/JPEG (TIFF revision 6.0 as modified by TIFF
	 * Technical Note #2) for "high end" applications that need to record a lot of additional data about an image. TIFF/JPEG is fairly new and not yet widely
	 * supported, unfortunately.
	 * 
	 * The upcoming JPEG Part 3 standard defines a file format called SPIFF. SPIFF is interoperable with JFIF, in the sense that most JFIF decoders should be
	 * able to read the most common variant of SPIFF. SPIFF has some technical advantages over JFIF, but its major claim to fame is simply that it is an
	 * official standard rather than an informal one. At this point it is unclear whether SPIFF will supersede JFIF or whether JFIF will remain the de-facto
	 * standard. IJG intends to support SPIFF once the standard is frozen, but we have not decided whether it should become our default output format or not.
	 * (In any case, our decoder will remain capable of reading JFIF indefinitely.)
	 * 
	 * Various proprietary file formats incorporating JPEG compression also exist. We have little or no sympathy for the existence of these formats. Indeed, one
	 * of the original reasons for developing this free software was to help force convergence on common, open format standards for JPEG files. Don't use a
	 * proprietary file format!
	 * 
	 * 
	 * TO DO =====
	 * 
	 * In future versions, we are considering supporting some of the upcoming JPEG Part 3 extensions --- principally, variable quantization and the SPIFF file
	 * format.
	 * 
	 * Tuning the software for better behavior at low quality/high compression settings is also of interest. The current method for scaling the quantization
	 * tables is known not to be very good at low Q values.
	 * 
	 * As always, speeding things up is high on our priority list.
	 * 
	 * Please send bug reports, offers of help, etc. to jpeg-info@uunet.uu.net.**************************************************************************
	 */
}