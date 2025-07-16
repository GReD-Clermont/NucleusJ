/*
 * NucleusJ
 * Copyright (C) 2014-2025 iGReD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.igred.nucleus.utils;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.StackConverter;


public class Distance_Map implements PlugInFilter {
	private static final int THRESHOLD = 126;
	
	private ImagePlus image;
	
	
	public int setup(String arg, ImagePlus imp) {
		this.image = imp;
		return DOES_8G;
	}
	
	
	public void run(ImageProcessor ip) {
		apply(image);
	}
	
	
	public static void apply(ImagePlus imagePlus) {
		StackConverter stackConverter = new StackConverter(imagePlus);
		if (imagePlus.getType() != ImagePlus.GRAY8) {
			stackConverter.convertToGray8();
		}
		ImageStack stack    = imagePlus.getStack();
		int        w        = stack.getWidth();
		int        h        = stack.getHeight();
		int        d        = imagePlus.getStackSize();
		int        nThreads = Runtime.getRuntime().availableProcessors();
		
		//Create references to input data
		byte[][] data = new byte[d][];
		for (int k = 0; k < d; k++) {
			data[k] = (byte[]) stack.getPixels(k + 1);
		}
		//Create 32 bit floating point stack for output, s.  Will also use it for g in Transformation 1.
		ImageStack sStack = new ImageStack(w, h);
		float[][]  s      = new float[d][];
		for (int k = 0; k < d; k++) {
			ImageProcessor ipk = new FloatProcessor(w, h);
			sStack.addSlice(null, ipk);
			s[k] = (float[]) ipk.getPixels();
		}
		float[] sk;
		//Transformation 1.  Use s to store g.
		IJ.showStatus("EDT transformation 1/3");
		Step1Thread[] s1t = new Step1Thread[nThreads];
		for (int thread = 0; thread < nThreads; thread++) {
			s1t[thread] = new Step1Thread(thread, nThreads, w, h, d, THRESHOLD, s, data);
			s1t[thread].start();
		}
		try {
			for (int thread = 0; thread < nThreads; thread++) {
				s1t[thread].join();
			}
		} catch (InterruptedException ie) {
			IJ.error("A thread was interrupted in step 1 .");
			Thread.currentThread().interrupt();
		}
		//Transformation 2.  g (in s) -> h (in s)
		IJ.showStatus("EDT transformation 2/3");
		Step2Thread[] s2t = new Step2Thread[nThreads];
		for (int thread = 0; thread < nThreads; thread++) {
			s2t[thread] = new Step2Thread(thread, nThreads, w, h, d, s);
			s2t[thread].start();
		}
		try {
			for (int thread = 0; thread < nThreads; thread++) {
				s2t[thread].join();
			}
		} catch (InterruptedException ie) {
			IJ.error("A thread was interrupted in step 2 .");
			Thread.currentThread().interrupt();
		}
		//Transformation 3. h (in s) -> s
		IJ.showStatus("EDT transformation 3/3");
		Step3Thread[] s3t = new Step3Thread[nThreads];
		for (int thread = 0; thread < nThreads; thread++) {
			s3t[thread] = new Step3Thread(thread, nThreads, w, h, d, s, data);
			s3t[thread].start();
		}
		try {
			for (int thread = 0; thread < nThreads; thread++) {
				s3t[thread].join();
			}
		} catch (InterruptedException ie) {
			IJ.error("A thread was interrupted in step 3 .");
			Thread.currentThread().interrupt();
		}
		//Find the largest distance for scaling
		//Also fill in the background values.
		float distMax = 0;
		int   wh      = w * h;
		float dist;
		for (int k = 0; k < d; k++) {
			sk = s[k];
			for (int ind = 0; ind < wh; ind++) {
				if ((data[k][ind] & 255) < THRESHOLD) {
					sk[ind] = 0;
				} else {
					dist = (float) StrictMath.sqrt(sk[ind]);
					sk[ind] = dist;
					distMax = StrictMath.max(dist, distMax);
				}
			}
		}
		
		IJ.showProgress(1.0);
		String    title  = stripExtension(imagePlus.getTitle());
		ImagePlus impOut = new ImagePlus(title + "EDT", sStack);
		impOut.getProcessor().setMinAndMax(0, distMax);
		imagePlus.setStack(sStack);
	}
	
	
	//Modified from ImageJ code by Wayne Rasband
	static String stripExtension(String name) {
		String strippedName = name;
		if (strippedName != null) {
			int dotIndex = strippedName.lastIndexOf('.');
			if (dotIndex >= 0) {
				strippedName = strippedName.substring(0, dotIndex);
			}
		}
		return strippedName;
	}
	
	
	private static class Step2Thread extends Thread {
		int       thread;
		int       nThreads;
		int       w;
		int       h;
		int       d;
		float[][] s;
		
		
		Step2Thread(int thread, int nThreads, int w, int h, int d, float[][] s) {
			this.thread = thread;
			this.nThreads = nThreads;
			this.w = w;
			this.h = h;
			this.d = d;
			this.s = s;
		}
		
		
		@Override
		public void run() {
			float[] sk;
			int     n = w;
			if (h > n) {
				n = h;
			}
			if (d > n) {
				n = d;
			}
			int     noResult = 3 * (n + 1) * (n + 1);
			int[]   tempInt  = new int[n];
			int[]   tempS    = new int[n];
			boolean nonempty;
			int     test;
			int     min;
			int     delta;
			for (int k = thread; k < d; k += nThreads) {
				IJ.showProgress(k / (1.0 * d));
				sk = s[k];
				for (int i = 0; i < w; i++) {
					nonempty = false;
					for (int j = 0; j < h; j++) {
						tempS[j] = (int) sk[i + w * j];
						if (tempS[j] > 0) {
							nonempty = true;
						}
					}
					if (nonempty) {
						for (int j = 0; j < h; j++) {
							min = noResult;
							delta = j;
							for (int y = 0; y < h; y++) {
								test = tempS[y] + delta * delta;
								delta--;
								if (test < min) {
									min = test;
								}
							}
							tempInt[j] = min;
						}
						for (int j = 0; j < h; j++) {
							sk[i + w * j] = tempInt[j];
						}
					}
				}
			}
		}//run
		
	}//Step2Thread
	
	private static class Step1Thread extends Thread {
		int       thread;
		int       nThreads;
		int       w;
		int       h;
		int       d;
		int       thresh;
		float[][] s;
		byte[][]  data;
		
		
		Step1Thread(int thread, int nThreads, int w, int h, int d, int thresh, float[][] s, byte[][] data) {
			this.thread = thread;
			this.nThreads = nThreads;
			this.w = w;
			this.h = h;
			this.d = d;
			this.thresh = thresh;
			this.data = data;
			this.s = s;
		}
		
		
		@Override
		public void run() {
			float[] sk;
			byte[]  dk;
			int     n = w;
			if (h > n) {
				n = h;
			}
			if (d > n) {
				n = d;
			}
			int       noResult   = 3 * (n + 1) * (n + 1);
			boolean[] background = new boolean[n];
			int       test;
			int       min;
			for (int k = thread; k < d; k += nThreads) {
				IJ.showProgress(k / (1.0 * d));
				sk = s[k];
				dk = data[k];
				for (int j = 0; j < h; j++) {
					for (int i = 0; i < w; i++) {
						background[i] = (dk[i + w * j] & 255) < thresh;
					}
					for (int i = 0; i < w; i++) {
						min = noResult;
						for (int x = i; x < w; x++) {
							if (background[x]) {
								test = i - x;
								test *= test;
								min = test;
								break;
							}
						}
						for (int x = i - 1; x >= 0; x--) {
							if (background[x]) {
								test = i - x;
								test *= test;
								if (test < min) {
									min = test;
								}
								break;
							}
						}
						sk[i + w * j] = min;
					}
				}
			}
		}//run
		
	}//Step1Thread
	
	private static class Step3Thread extends Thread {
		int       thread;
		int       nThreads;
		int       w;
		int       h;
		int       d;
		float[][] s;
		byte[][]  data;
		
		
		Step3Thread(int thread, int nThreads, int w, int h, int d, float[][] s, byte[][] data) {
			this.thread = thread;
			this.nThreads = nThreads;
			this.w = w;
			this.h = h;
			this.d = d;
			this.s = s;
			this.data = data;
		}
		
		
		@Override
		public void run() {
			int     zStart;
			int     zStop;
			int     zBegin;
			int     zEnd;
			int     n = w;
			if (h > n) {
				n = h;
			}
			if (d > n) {
				n = d;
			}
			int     noResult = 3 * (n + 1) * (n + 1);
			int[]   tempInt  = new int[n];
			int[]   tempS    = new int[n];
			boolean nonempty;
			int     test;
			int     min;
			int     delta;
			for (int j = thread; j < h; j += nThreads) {
				IJ.showProgress(j / (1.0 * h));
				for (int i = 0; i < w; i++) {
					nonempty = false;
					for (int k = 0; k < d; k++) {
						tempS[k] = (int) s[k][i + w * j];
						if (tempS[k] > 0) {
							nonempty = true;
						}
					}
					if (nonempty) {
						zStart = 0;
						while (zStart < d - 1 && tempS[zStart] == 0) {
							zStart++;
						}
						if (zStart > 0) {
							zStart--;
						}
						zStop = d - 1;
						while (zStop > 0 && tempS[zStop] == 0) {
							zStop--;
						}
						if (zStop < d - 1) {
							zStop++;
						}
						
						for (int k = 0; k < d; k++) {
							//Limit to the non-background to save time,
							if ((data[k][i + w * j] & 255) >= THRESHOLD) {
								min = noResult;
								zBegin = zStart;
								zEnd = zStop;
								if (zBegin > k) {
									zBegin = k;
								}
								if (zEnd < k) {
									zEnd = k;
								}
								delta = k - zBegin;
								for (int z = zBegin; z <= zEnd; z++) {
									test = tempS[z] + delta * delta;
									delta--;
									if (test < min) {
										min = test;
									}
								}
								tempInt[k] = min;
							}
						}
						for (int k = 0; k < d; k++) {
							s[k][i + w * j] = tempInt[k];
						}
					}
				}
			}
		}//run
		
	}//Step2Thread
	
}