package gred.nucleus.core;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import gred.nucleus.utils.Gradient;
import gred.nucleus.utils.Histogram;
import gred.nucleus.utils.VoxelRecord;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.StackConverter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.type.logic.BitType;
import net.imagej.mesh.Mesh;
import net.imagej.mesh.Meshes;
import net.imagej.mesh.Vertices;
import net.imagej.mesh.Triangles;
import net.imglib2.realtransform.AffineTransform3D;


/**
 * Class computing 3D parameters from raw and his segmented image associated :
 * <p>
 * Volume Flatness Elongation Sphericity Esr SurfaceArea SurfaceAreaCorrected SphericityCorrected MeanIntensity
 * StandardDeviation MinIntensity MaxIntensity OTSUThreshold
 * <p>
 * <p>
 * //TODO reecrire cette classe ya des choses que je fais 5 fois c'est inutil
 *
 * @author Tristan Dubos and Axel Poulet
 */
public class Measure3D {
	ResultsTable _resultsTable;

	ImagePlus[] imageSeg;
	ImagePlus   rawImage;
	ImagePlus _imageSeg;
	
	double xCal;
	double yCal;
	double zCal;
	
	Map<Double, Integer> segmentedNucleusHistogram = new TreeMap<>();
	Map<Double, Integer> backgroundHistogram       = new TreeMap<>();
	private ImagePlus _rawImage;
	TreeMap< Double, Integer> _segmentedNucleusHisto =new TreeMap <Double, Integer>();
	TreeMap< Double, Integer> _backgroundHisto =new TreeMap <Double, Integer>();


	public Measure3D() {
	}
	
	
	public Measure3D(double xCal, double yCal, double zCal) {
		this.xCal = xCal;
		this.yCal = yCal;
		this.zCal = zCal;
		
	}
	
	
	public Measure3D(ImagePlus[] imageSeg, ImagePlus rawImage, double xCal, double yCal, double zCal) {
		this.rawImage = rawImage;
		this.imageSeg = imageSeg;
		this.xCal = xCal;
		this.yCal = yCal;
		this.zCal = zCal;
	}
	public Measure3D(ImagePlus imageSeg, ImagePlus rawImage, double xCal, double ycal, double zCal) {
		this._rawImage = rawImage;
		this._imageSeg = imageSeg;
		this.xCal = xCal;
		this.yCal = ycal;
		this.zCal = zCal;
		this.histogramSegmentedNucleus2();
	}
	
	/**
	 * Scan of image and if the voxel belong to the object of interest, looking, if in his neighborhood there are voxel
	 * value == 0 then it is a boundary voxel. Adding the surface of the face of the voxel frontier, which are in
	 * contact with the background of the image, to the surface total.
	 *
	 * @param label label of the interest object
	 *
	 * @return the surface
	 */
	public double computeSurfaceObject(double label) {
		ImageStack imageStackInput = this.imageSeg[0].getStack();
		double     surfaceArea     = 0, voxelValue, neighborVoxelValue;
		for (int k = 1; k < this.imageSeg[0].getStackSize(); ++k) {
			for (int i = 1; i < this.imageSeg[0].getWidth(); ++i) {
				for (int j = 1; j < this.imageSeg[0].getHeight(); ++j) {
					voxelValue = imageStackInput.getVoxel(i, j, k);
					if (voxelValue == label) {
						for (int kk = k - 1; kk <= k + 1; kk += 2) {
							neighborVoxelValue = imageStackInput.getVoxel(i, j, kk);
							if (voxelValue != neighborVoxelValue) {
								surfaceArea += this.xCal * this.yCal;
							}
						}
						for (int ii = i - 1; ii <= i + 1; ii += 2) {
							neighborVoxelValue = imageStackInput.getVoxel(ii, j, k);
							if (voxelValue != neighborVoxelValue) {
								surfaceArea += this.yCal * this.zCal;
							}
						}
						for (int jj = j - 1; jj <= j + 1; jj += 2) {
							neighborVoxelValue = imageStackInput.getVoxel(i, jj, k);
							if (voxelValue != neighborVoxelValue) {
								surfaceArea += this.xCal * this.zCal;
							}
						}
					}
				}
			}
		}
		return surfaceArea;
	}
	
	
	/**
	 * This Method compute the volume of each segmented objects in imagePlus
	 *
	 * @param imagePlusInput ImagePlus segmented image
	 *
	 * @return double table which contain the volume of each image object
	 */
	public double[] computeVolumeOfAllObjects(ImagePlus imagePlusInput) {
		
		Histogram histogram = new Histogram();
		histogram.run(imagePlusInput);
		double[]             tlabel        = histogram.getLabels();
		double[]             tObjectVolume = new double[tlabel.length];
		Map<Double, Integer> histo         = histogram.getHistogram();
		for (int i = 0; i < tlabel.length; ++i) {
			int nbVoxel = histo.get(tlabel[i]);
			tObjectVolume[i] = nbVoxel * this.xCal * this.yCal * this.zCal;
		}
		return tObjectVolume;
	}
	
	
	/**
	 * Compute the volume of one object with this label
	 *
	 * @param label double label of the object of interest
	 *
	 * @return double: the volume of the label of interest
	 */
	public double computeVolumeObject2(double label) {
		Histogram histogram = new Histogram();
		histogram.run(this.imageSeg[0]);
		Map<Double, Integer> hashMapHistogram = histogram.getHistogram();
		
		return hashMapHistogram.get(label) * this.xCal * this.yCal * this.zCal;
		
	}
	
	
	private double computeVolumeObjectML() {
		double volumeTMP = 0.0;
		for (Map.Entry<Double, Integer> toto : this.segmentedNucleusHistogram.entrySet()) {
			if (toto.getValue() > 0) {
				volumeTMP += toto.getValue();
			}
		}
		return volumeTMP * this.xCal * this.yCal * this.zCal;
	}

	public void compute2dParameters ()
	{
		StackConverter stackConverter = new StackConverter( this.imageSeg[0] );
		if (this.imageSeg[0].getType() != ImagePlus.GRAY8)	stackConverter.convertToGray8();
		_resultsTable = computePrameters(this.imageSeg[0],searchSliceWithMaxArea(this.imageSeg[0]));
	}
	private int searchSliceWithMaxArea(ImagePlus imagePlusSegmented)
	{
		Calibration calibration= imagePlusSegmented.getCalibration();
		int indiceMaxArea = -1;
		double xCalibration = calibration.pixelWidth;
		double yCalibration = calibration.pixelHeight;
		ImageStack imageStackSegmented = imagePlusSegmented.getStack();
		double areaMax = 0;
		double area = 0;
		for (int k = 0; k < imagePlusSegmented.getNSlices(); ++k)
		{
			int nbVoxel = 0;
			for (int i = 1; i < imagePlusSegmented.getWidth(); ++i)
			{
				for (int j = 1;  j < imagePlusSegmented.getHeight(); ++j)
					if (imageStackSegmented.getVoxel(i, j, k)>0)
						++nbVoxel ;
			}
			area = xCalibration*yCalibration*nbVoxel;
			if (area > areaMax)
			{
				areaMax = area ;
				indiceMaxArea = k;
			}
		}
		return indiceMaxArea;
	}

	/**
	 * method to compute the parameter
	 *
	 * @param imagePlusSegmented
	 * @param indiceMaxArea
	 * @return a resultTable object which contain the results
	 */
	private ResultsTable computePrameters(ImagePlus imagePlusSegmented, int indiceMaxArea)
	{
		ImagePlus imagePlusTemp = new ImagePlus();
		ImageStack imageStackSegmented = imagePlusSegmented.getStack();
		ImageStack imageStackTemp = new ImageStack(imagePlusSegmented.getWidth(),imagePlusSegmented.getHeight());
		imageStackTemp.addSlice(imageStackSegmented.getProcessor(indiceMaxArea));
		imagePlusTemp.setStack(imageStackTemp);
		Calibration calibrationImagePlusSegmented= imagePlusSegmented.getCalibration();
		Calibration calibration = new Calibration();
		calibration.pixelHeight = calibrationImagePlusSegmented.pixelHeight;
		calibration.pixelWidth =  calibrationImagePlusSegmented.pixelWidth;
		imagePlusTemp.setCalibration(calibration);
		ResultsTable resultTable = new ResultsTable();
		ParticleAnalyzer particleAnalyser = new ParticleAnalyzer
				(ParticleAnalyzer.SHOW_NONE, Measurements.AREA+Measurements.CIRCULARITY, resultTable, 10, Double.MAX_VALUE, 0,1);
		particleAnalyser.analyze(imagePlusTemp);
		return resultTable;

	}
	/**
	 * Aspect ratio The aspect ratio of the particle’s fitted ellipse, i.e., [M ajor Axis]/Minor Axis] . If Fit
	 * Ellipse is selected the Major and Minor axis are displayed. Uses the heading AR.
	 *
	 * @return
	 */
	public double getAspectRatio () {	return _resultsTable.getValue("AR", 0);	}

	/**
	 * Circularity Particles with size circularity values outside the range specified in this field are also
	 * ignored. Circularity = (4π × [Area ] / [P erimeter]2 ) , see Set Measurements. . . ) ranges from 0 (infinitely
	 * elongated polygon) to 1 (perfect circle).
	 *
	 * @return
	 */

	public double getCirculairty() {   return _resultsTable.getValue("Circ.", 0); }

	
	/**
	 * Compute the volume of one object with this label
	 *
	 * @param imagePlusInput ImagePLus of the segmented image
	 * @param label          double label of the object of interest
	 *
	 * @return double: the volume of the label of interest
	 */
	public double computeVolumeObject(ImagePlus imagePlusInput, double label) {
		Histogram histogram = new Histogram();
		histogram.run(imagePlusInput);
		Map<Double, Integer> hashMapHistogram = histogram.getHistogram();
		return hashMapHistogram.get(label) * this.xCal * this.yCal * this.zCal;
	}
	
	
	/**
	 * compute the equivalent spherical radius
	 *
	 * @param volume double of the volume of the object of interesr
	 *
	 * @return double the equivalent spherical radius
	 */
	public double equivalentSphericalRadius(double volume) {
		double radius = (3 * volume) / (4 * Math.PI);
		radius = Math.pow(radius, 1.0 / 3.0);
		return radius;
	}
	
	
	/**
	 * compute the equivalent spherical radius with ImagePlus in input
	 *
	 * @param imagePlusBinary ImagePlus of the segmented image
	 *
	 * @return double the equivalent spherical radius
	 */
	public double equivalentSphericalRadius(ImagePlus imagePlusBinary) {
		double radius = (3 * computeVolumeObject(imagePlusBinary, 255))
		                / (4 * Math.PI);
		radius = Math.pow(radius, 1.0 / 3.0);
		return radius;
	}
	
	
	/**
	 * Method which compute the sphericity : 36Pi*Volume^2/Surface^3 = 1 if perfect sphere
	 *
	 * @param volume  double volume of the object
	 * @param surface double surface of the object
	 *
	 * @return double sphercity

	public double computeSphericity(double volume, double surface) {
		return ((36 * Math.PI * (volume * volume))
		        / (surface * surface * surface));
	}
	 */

	/**
	 * Compute 3D sphericity of an object using the Wadell formula.
	 *
	 * Formula:
	 * Φ = (π^(1/3) * (6 * V)^(2/3)) / A
	 *
	 * @param volume  Volume of the object (real units, e.g. µm³)
	 * @param surface Surface area of the object (real units, e.g. µm²)
	 * @return Sphericity (dimensionless, 1 for a perfect sphere)
	 */
	public static double computeSphericity(double volume, double surface) {
		if (volume <= 0 || surface <= 0) {
			throw new IllegalArgumentException("Volume and surface area must be positive values.");
		}
		return (Math.cbrt(Math.PI) * Math.pow(6 * volume, 2.0 / 3.0)) / surface;
	}

	/**
	 * Method which compute the eigen value of the matrix (differences between the coordinates of all points and the
	 * barycenter. Obtaining a symmetric matrix : xx xy xz xy yy yz xz yz zz Compute the eigen value with the pakage
	 * JAMA
	 *
	 * @param label double label of interest
	 *
	 * @return double table containing the 3 eigen values
	 */
	public double[] computeEigenValue3D(double label) {
		ImageStack  imageStackInput = this.imageSeg[0].getImageStack();
		VoxelRecord barycenter      = computeBarycenter3D(true, this.imageSeg[0], label);
		
		double xx      = 0;
		double xy      = 0;
		double xz      = 0;
		double yy      = 0;
		double yz      = 0;
		double zz      = 0;
		int    counter = 0;
		double voxelValue;
		for (int k = 0; k < this.imageSeg[0].getStackSize(); ++k) {
			double dz = ((this.zCal * (double) k) - barycenter.getK());
			for (int i = 0; i < this.imageSeg[0].getWidth(); ++i) {
				double dx = ((this.xCal * (double) i) - barycenter.getI());
				for (int j = 0; j < this.imageSeg[0].getHeight(); ++j) {
					voxelValue = imageStackInput.getVoxel(i, j, k);
					if (voxelValue == label) {
						double dy = ((this.yCal * (double) j) - barycenter.getJ());
						xx += dx * dx;
						yy += dy * dy;
						zz += dz * dz;
						xy += dx * dy;
						xz += dx * dz;
						yz += dy * dz;
						counter++;
					}
				}
			}
		}
		double[][] tValues = {{xx / counter, xy / counter, xz / counter},
		                      {xy / counter, yy / counter, yz / counter},
		                      {xz / counter, yz / counter, zz / counter}};
		Matrix matrix = new Matrix(tValues);
		
		EigenvalueDecomposition eigenValueDecomposition = matrix.eig();
		return eigenValueDecomposition.getRealEigenvalues();
	}
	
	
	/**
	 * Compute the flatness and the elongation of the object of interest
	 *
	 * @param label double label of interest
	 *
	 * @return double table containing in [0] flatness and in [1] elongation
	 */
	public double[] computeFlatnessAndElongation(double label) {
		double[] shapeParameters = new double[2];
		double[] tEigenValues    = computeEigenValue3D(label);
		shapeParameters[0] = tEigenValues[1] / tEigenValues[0];
		shapeParameters[1] = tEigenValues[2] / tEigenValues[1];
		return shapeParameters;
	}
	
	
	/**
	 * Method which determines object barycenter
	 *
	 * @param unit           if true the coordinates of barycenter are in µm.
	 * @param imagePlusInput ImagePlus of labelled image
	 * @param label          double label of interest
	 *
	 * @return VoxelRecord the barycenter of the object of interest
	 */
	public VoxelRecord computeBarycenter3D(boolean unit,
	                                       ImagePlus imagePlusInput,
	                                       double label) {
		ImageStack  imageStackInput       = imagePlusInput.getImageStack();
		VoxelRecord voxelRecordBarycenter = new VoxelRecord();
		int         count                 = 0;
		int         sx                    = 0;
		int         sy                    = 0;
		int         sz                    = 0;
		double      voxelValue;
		for (int k = 0; k < imagePlusInput.getStackSize(); ++k) {
			for (int i = 0; i < imagePlusInput.getWidth(); ++i) {
				for (int j = 0; j < imagePlusInput.getHeight(); ++j) {
					voxelValue = imageStackInput.getVoxel(i, j, k);
					if (voxelValue == label) {
						sx += i;
						sy += j;
						sz += k;
						++count;
					}
				}
			}
		}
		sx /= count;
		sy /= count;
		sz /= count;
		voxelRecordBarycenter.setLocation(sx, sy, sz);
		if (unit) {
			voxelRecordBarycenter.multiply(this.xCal, this.yCal, this.zCal);
		}
		return voxelRecordBarycenter;
	}
	
	
	/**
	 * Method which compute the barycenter of each objects and return the result in a table of VoxelRecord
	 *
	 * @param imagePlusInput ImagePlus of labelled image
	 * @param unit           if true the coordinates of barycenter are in µm.
	 *
	 * @return table of VoxelRecord for each object of the input image
	 */
	public VoxelRecord[] computeObjectBarycenter(ImagePlus imagePlusInput,
	                                             boolean unit) {
		Histogram histogram = new Histogram();
		histogram.run(imagePlusInput);
		double[]      tLabel       = histogram.getLabels();
		VoxelRecord[] tVoxelRecord = new VoxelRecord[tLabel.length];
		for (int i = 0; i < tLabel.length; ++i) {
			tVoxelRecord[i] = computeBarycenter3D(unit, imagePlusInput, tLabel[i]);
		}
		return tVoxelRecord;
	}
	
	
	/**
	 * Intensity of chromocenters/ intensity of the nucleus
	 *
	 * @param imagePlusInput        ImagePlus raw image
	 * @param imagePlusSegmented    binary ImagePlus
	 * @param imagePlusChromocenter ImagePlus of the chromocemters
	 *
	 * @return double Relative Heterochromatin Fraction compute on the Intensity ratio
	 */
	public double computeIntensityRHF(ImagePlus imagePlusInput
			, ImagePlus imagePlusSegmented, ImagePlus imagePlusChromocenter) {
		double     chromocenterIntensity  = 0;
		double     nucleusIntensity       = 0;
		double     voxelValueChromocenter;
		double     voxelValueInput;
		double     voxelValueSegmented;
		ImageStack imageStackChromocenter = imagePlusChromocenter.getStack();
		ImageStack imageStackSegmented    = imagePlusSegmented.getStack();
		ImageStack imageStackInput        = imagePlusInput.getStack();
		for (int k = 0; k < imagePlusInput.getNSlices(); ++k) {
			for (int i = 0; i < imagePlusInput.getWidth(); ++i) {
				for (int j = 0; j < imagePlusInput.getHeight(); ++j) {
					voxelValueSegmented = imageStackSegmented.getVoxel(i, j, k);
					voxelValueInput = imageStackInput.getVoxel(i, j, k);
					voxelValueChromocenter =
							imageStackChromocenter.getVoxel(i, j, k);
					
					if (voxelValueSegmented > 0) {
						if (voxelValueChromocenter > 0) {
							chromocenterIntensity += voxelValueInput;
						}
						nucleusIntensity += voxelValueInput;
					}
				}
			}
		}
		return chromocenterIntensity / nucleusIntensity;
	}
	
	
	/**
	 * Method which compute the RHF (total chromocenters volume/nucleus volume)
	 *
	 * @param imagePlusSegmented     binary ImagePlus
	 * @param imagePlusChromocenters ImagePLus of the chromocenters
	 *
	 * @return double Relative Heterochromatin Fraction compute on the Volume ratio
	 */
	public double computeVolumeRHF(ImagePlus imagePlusSegmented, ImagePlus imagePlusChromocenters) {
		double   volumeCc            = 0;
		double[] tVolumeChromocenter = computeVolumeOfAllObjects(imagePlusChromocenters);
		for (double v : tVolumeChromocenter) {
			volumeCc += v;
		}
		double[] tVolumeSegmented = computeVolumeOfAllObjects(imagePlusSegmented);
		return volumeCc / tVolumeSegmented[0];
	}
	
	
	/**
	 * Detect the number of object on segmented image.
	 *
	 * @param imagePlusInput Segmented image
	 *
	 * @return int nb of object in the image
	 */
	public int getNumberOfObject(ImagePlus imagePlusInput) {
		Histogram histogram = new Histogram();
		histogram.run(imagePlusInput);
		return histogram.getNbLabels();
	}



	/** Helper: flatten Vertices → double[] */
	private static double[] flatXYZ(final Vertices verts) {
		final double[] out = new double[(int) (verts.size() * 3)];
		for (int i = 0; i < verts.size(); i++) {
			out[i*3    ] = verts.x(i);
			out[i*3 + 1] = verts.y(i);
			out[i*3 + 2] = verts.z(i);
		}
		return out;
	}

	/** Returns {surface, volume} in calibrated units (µm² / µm³, etc.). */
	public double[] mcSurfaceAndVolume() {

		/* 1 — wrap the existing mask ImageStack as an ImgLib2 BitType image */
		RandomAccessibleInterval<BitType> mask =
				ImagePlusAdapter.wrap(this.imageSeg[0]);   // your field *is* the mask

		/* 2 — Marching Cubes (iso-level 0.5 for {0,1} data) */
		Mesh mesh = Meshes.marchingCubes(mask, 0.5);

		/* 3 — copy vertices to a flat array and scale to physical units */
		double[] v = flatXYZ(mesh.vertices());
		for (int i = 0; i < v.length; i += 3) {
			v[i]     *= xCal;   // x-coord
			v[i + 1] *= yCal;   // y-coord
			v[i + 2] *= zCal;   // z-coord
		}

		/* 4 — integrate triangles for surface & volume */
		double surface = 0, volume = 0;
		Triangles tris = mesh.triangles();

		for (int t = 0; t < tris.size(); t++) {
			int ia = (int) tris.vertex0(t) * 3;
			int ib = (int) tris.vertex1(t) * 3;
			int ic = (int) (tris.vertex2(t) * 3);

			double ax = v[ia], ay = v[ia+1], az = v[ia+2];
			double bx = v[ib], by = v[ib+1], bz = v[ib+2];
			double cx = v[ic], cy = v[ic+1], cz = v[ic+2];

			/* surface patch = ½‖(B−A)×(C−A)‖ */
			double abx = bx - ax, aby = by - ay, abz = bz - az;
			double acx = cx - ax, acy = cy - ay, acz = cz - az;
			double nx  = aby*acz - abz*acy;
			double ny  = abz*acx - abx*acz;
			double nz  = abx*acy - aby*acx;
			surface += 0.5 * Math.sqrt(nx*nx + ny*ny + nz*nz);

			/* volume patch = ⅙ A·(B×C) */
			volume  += (ax*(by*cz - bz*cy)
					- ay*(bx*cz - bz*cx)
					+ az*(bx*cy - by*cx)) / 6.0;
		}

		return new double[] { surface, Math.abs(volume) };
	}


	/**
	 * Method to compute surface of the segmented object using gradient information.
	 *
	 * @return
	 */
	public double computeComplexSurface() {
		Gradient           gradient            = new Gradient(this.rawImage);
		List<Double>[][][] tableUnitary        = gradient.getUnitNormals();
		ImageStack         imageStackSegmented = this.imageSeg[0].getStack();
		double             surfaceArea         = 0, voxelValue, neighborVoxelValue;
		VoxelRecord        voxelRecordIn       = new VoxelRecord();
		VoxelRecord        voxelRecordOut      = new VoxelRecord();
		
		for (int k = 2; k < this.imageSeg[0].getNSlices() - 2; ++k) {
			for (int i = 2; i < this.imageSeg[0].getWidth() - 2; ++i) {
				for (int j = 2; j < this.imageSeg[0].getHeight() - 2; ++j) {
					voxelValue = imageStackSegmented.getVoxel(i, j, k);
					if (voxelValue > 0) {
						for (int kk = k - 1; kk <= k + 1; kk += 2) {
							neighborVoxelValue =
									imageStackSegmented.getVoxel(i, j, kk);
							if (voxelValue != neighborVoxelValue) {
								voxelRecordIn.setLocation(
										i,
										j,
										k);
								voxelRecordOut.setLocation(
										i,
										j,
										kk);
								surfaceArea += computeSurfelContribution(
										tableUnitary[i][j][k],
										tableUnitary[i][j][kk],
										voxelRecordIn,
										voxelRecordOut,
										((this.xCal) * (this.yCal)));
							}
						}
						for (int ii = i - 1; ii <= i + 1; ii += 2) {
							neighborVoxelValue = imageStackSegmented.getVoxel(
									ii, j, k);
							if (voxelValue != neighborVoxelValue) {
								voxelRecordIn.setLocation(
										i,
										j,
										k);
								voxelRecordOut.setLocation(
										ii,
										j,
										k);
								surfaceArea += computeSurfelContribution(
										tableUnitary[i][j][k],
										tableUnitary[ii][j][k],
										voxelRecordIn, voxelRecordOut,
										((this.yCal) * (this.zCal)));
							}
						}
						for (int jj = j - 1; jj <= j + 1; jj += 2) {
							neighborVoxelValue = imageStackSegmented.getVoxel(
									i, jj, k);
							if (voxelValue != neighborVoxelValue) {
								voxelRecordIn.setLocation(
										i,
										j,
										k);
								voxelRecordOut.setLocation(
										i,
										jj,
										k);
								surfaceArea += computeSurfelContribution(
										tableUnitary[i][j][k],
										tableUnitary[i][jj][k],
										voxelRecordIn,
										voxelRecordOut,
										((this.xCal) * (this.zCal)));
							}
						}
					}
				}
			}
		}
		return surfaceArea;
	}
	/**
	 * Method to compute surface of the segmented object using gradient information.
	 *
	 * @param imagePlusSegmented segmented image
	 * @param gradient           gradient computed from raw images
	 *
	 * @return
	 */
	public double computeComplexSurface(ImagePlus imagePlusSegmented, Gradient gradient) {
		List<Double>[][][] tableUnitary        = gradient.getUnitNormals();
		ImageStack         imageStackSegmented = imagePlusSegmented.getStack();
		double             surfaceArea         = 0, voxelValue, neighborVoxelValue;
		VoxelRecord        voxelRecordIn       = new VoxelRecord();
		VoxelRecord        voxelRecordOut      = new VoxelRecord();
		Calibration        calibration         = imagePlusSegmented.getCalibration();
		double             xCalibration        = calibration.pixelWidth;
		double             yCalibration        = calibration.pixelHeight;
		double             zCalibration        = calibration.pixelDepth;
		for (int k = 2; k < imagePlusSegmented.getNSlices() - 2; ++k) {
			for (int i = 2; i < imagePlusSegmented.getWidth() - 2; ++i) {
				for (int j = 2; j < imagePlusSegmented.getHeight() - 2; ++j) {
					voxelValue = imageStackSegmented.getVoxel(i, j, k);
					if (voxelValue > 0) {
						for (int kk = k - 1; kk <= k + 1; kk += 2) {
							neighborVoxelValue = imageStackSegmented.getVoxel(i, j, kk);
							if (voxelValue != neighborVoxelValue) {
								voxelRecordIn.setLocation(i, j, k);
								voxelRecordOut.setLocation(i, j, kk);
								surfaceArea += computeSurfelContribution(tableUnitary[i][j][k],
								                                         tableUnitary[i][j][kk],
								                                         voxelRecordIn,
								                                         voxelRecordOut,
								                                         ((xCalibration) * (yCalibration)));
							}
						}
						for (int ii = i - 1; ii <= i + 1; ii += 2) {
							neighborVoxelValue = imageStackSegmented.getVoxel(ii, j, k);
							if (voxelValue != neighborVoxelValue) {
								voxelRecordIn.setLocation(i, j, k);
								voxelRecordOut.setLocation(ii, j, k);
								surfaceArea += computeSurfelContribution(tableUnitary[i][j][k],
								                                         tableUnitary[ii][j][k],
								                                         voxelRecordIn,
								                                         voxelRecordOut,
								                                         ((yCalibration) * (zCalibration)));
							}
						}
						for (int jj = j - 1; jj <= j + 1; jj += 2) {
							neighborVoxelValue = imageStackSegmented.getVoxel(i, jj, k);
							if (voxelValue != neighborVoxelValue) {
								voxelRecordIn.setLocation(i, j, k);
								voxelRecordOut.setLocation(i, jj, k);
								surfaceArea += computeSurfelContribution(tableUnitary[i][j][k],
								                                         tableUnitary[i][jj][k],
								                                         voxelRecordIn,
								                                         voxelRecordOut,
								                                         ((xCalibration) * (zCalibration)));
							}
						}
					}
				}
			}
		}
		return surfaceArea;
	}
	
	
	/**
	 * Compute surface contribution of each voxels from gradients.
	 *
	 * @param listUnitaryIn
	 * @param listUnitaryOut
	 * @param voxelRecordIn
	 * @param voxelRecordOut
	 * @param as
	 *
	 * @return
	 */
	private double computeSurfelContribution(List<Double> listUnitaryIn,
	                                         List<Double> listUnitaryOut,
	                                         VoxelRecord voxelRecordIn,
	                                         VoxelRecord voxelRecordOut,
	                                         double as) {
		double dx = voxelRecordIn.i - voxelRecordOut.i;
		double dy = voxelRecordIn.j - voxelRecordOut.j;
		double dz = voxelRecordIn.k - voxelRecordOut.k;
		double nx = (listUnitaryIn.get(0) + listUnitaryOut.get(0)) / 2;
		double ny = (listUnitaryIn.get(1) + listUnitaryOut.get(1)) / 2;
		double nz = (listUnitaryIn.get(2) + listUnitaryOut.get(2)) / 2;
		return Math.abs((dx * nx + dy * ny + dz * nz) * as);
	}
	
	
	/**
	 * Compute an Hashmap describing the segmented object (from raw data). Key = Voxels intensity value = Number of
	 * voxels
	 * <p>
	 * If voxels ==255 in seg image add Hashmap (Voxels intensity ,+1)
	 */
	private void histogramSegmentedNucleus() {
		ImageStack imageStackRaw = this.rawImage.getStack();
		ImageStack imageStackSeg = this.imageSeg[0].getStack();
		Histogram  histogram     = new Histogram();
		histogram.run(this.rawImage);
		for (int k = 0; k < this.rawImage.getStackSize(); ++k) {
			for (int i = 0; i < this.rawImage.getWidth(); ++i) {
				for (int j = 0; j < this.rawImage.getHeight(); ++j) {
					double voxelValue = imageStackSeg.getVoxel(i, j, k);
					if (voxelValue == 255) {
						if (!this.segmentedNucleusHistogram.containsKey(imageStackRaw.getVoxel(i, j, k))) {
							this.segmentedNucleusHistogram.put(imageStackRaw.getVoxel(i, j, k), 1);
						} else {
							this.segmentedNucleusHistogram.put(imageStackRaw.getVoxel(i, j, k),
							                                   this.segmentedNucleusHistogram.get(imageStackRaw.getVoxel(
									                                   i,
									                                   j,
									                                   k)) +
							                                   1);
						}
					} else {
						if (!this.backgroundHistogram.containsKey(imageStackRaw.getVoxel(i, j, k))) {
							this.backgroundHistogram.put(imageStackRaw.getVoxel(i, j, k), 1);
						} else {
							this.backgroundHistogram.put(imageStackRaw.getVoxel(i, j, k),
							                             this.backgroundHistogram.get(imageStackRaw.getVoxel(i,
							                                                                                 j,
							                                                                                 k)) + 1);
						}
					}
				}
			}
		}
	}
	private void histogramSegmentedNucleus2() {
		
		ImageStack imageStackRaw = this._rawImage.getStack();
		ImageStack imageStackSeg = this._imageSeg.getStack();
		Histogram histogram = new Histogram();
		histogram.run(this._rawImage);
		for(int k = 0; k < this._rawImage.getStackSize(); ++k) {
			for (int i = 0; i < this._rawImage.getWidth(); ++i) {
				for (int j = 0; j < this._rawImage.getHeight(); ++j) {
					double voxelValue = imageStackSeg.getVoxel(i, j, k);
					if (voxelValue ==255) {
						if(!this._segmentedNucleusHisto.containsKey(
								imageStackRaw.getVoxel(i, j, k)) ){
							this._segmentedNucleusHisto.put(
									imageStackRaw.getVoxel(i, j, k),  1);
						}
						else{
							this._segmentedNucleusHisto.put(
									imageStackRaw.getVoxel(i, j, k),
									this._segmentedNucleusHisto.get(
											imageStackRaw.getVoxel(i, j, k)) + 1);
						}
					}
					else{
						if(!this._backgroundHisto.containsKey(
								imageStackRaw.getVoxel(i, j, k)) ){
							this._backgroundHisto.put(
									imageStackRaw.getVoxel(i, j, k),  1);
						}
						else{
							this._backgroundHisto.put(
									imageStackRaw.getVoxel(i, j, k),
									this._backgroundHisto.get(
											imageStackRaw.getVoxel(i, j, k)) + 1);
						}
					}
				}
			}
		}
		
	}
	
	
	/**
	 * Compute the mean intensity of the segmented object by comparing voxels intensity in the raw image and
	 * white/segmented voxels the segmented image.
	 *
	 * @return mean intensity of segmented object
	 */
	private double meanIntensity() {
		int    numberOfVoxel = 0;
		double mean          = 0;
		for (Map.Entry<Double, Integer> histogram : this.segmentedNucleusHistogram.entrySet()) {
			numberOfVoxel += histogram.getValue();
			mean += histogram.getKey() * histogram.getValue();
		}
		return mean / numberOfVoxel;
	}
	
	
	/**
	 * Compute mean intensity of background
	 *
	 * @return mean intensity of background
	 */
	private double meanIntensityBackground() {
		double     meanIntensity = 0;
		int        voxelCounted  = 0;
		ImageStack imageStackRaw = this.rawImage.getStack();
		ImageStack imageStackSeg = this.imageSeg[0].getStack();
		for (int k = 0; k < this.rawImage.getStackSize(); ++k) {
			for (int i = 0; i < this.rawImage.getWidth(); ++i) {
				for (int j = 0; j < this.rawImage.getHeight(); ++j) {
					if (imageStackSeg.getVoxel(i, j, k) == 0) {
						meanIntensity += imageStackRaw.getVoxel(i, j, k);
						voxelCounted++;
					}
				}
			}
		}
		meanIntensity /= voxelCounted;
		return meanIntensity;
	}
	
	
	/**
	 * Compute the standard deviation of the mean intensity
	 *
	 * @return the standard deviation of the mean intensity of segmented object
	 *
	 * @see Measure3D#meanIntensity()
	 */
	private double standardDeviationIntensity(Double mean) {
		int    numberOfVoxel = 0;
		double std           = 0;
		for (Map.Entry<Double, Integer> histogram : this.segmentedNucleusHistogram.entrySet()) {
			numberOfVoxel += histogram.getValue();
			std = Math.abs((histogram.getKey() * histogram.getValue()) - (histogram.getValue() * mean));
		}
		return std / (numberOfVoxel - 1);
		
		
	}
	
	
	/**
	 * Find the maximum intensity voxel of segmented object
	 *
	 * @return the maximum intensity voxel of segmented object
	 */
	private double maxIntensity() {
		double maxIntensity = 0;
		for (Map.Entry<Double, Integer> entry : this.segmentedNucleusHistogram.entrySet()) {
			if (maxIntensity == 0 || entry.getKey().compareTo(maxIntensity) > 0) {
				maxIntensity = entry.getKey();
			}
		}
		return maxIntensity;
		
	}
	
	
	/**
	 * Find the minimum intensity voxel of segmented object
	 *
	 * @return the minimum intensity voxel of segmented object
	 */
	private double minIntensity() {
		Iterator<Map.Entry<Double, Integer>> iterator     = segmentedNucleusHistogram.entrySet().iterator();
		int                                  count        = 0;
		double                               minIntensity = 0;
		while (iterator.hasNext() && count == 0) {
			Map.Entry<Double, Integer> pair = iterator.next();
			count = pair.getValue();
			minIntensity = pair.getKey();
		}
		return minIntensity;
	}
	
	
	/**
	 * Compute the median intensity value of raw image voxel
	 *
	 * @return median intensity value of raw image voxel
	 */
	public double medianComputingImage() {
		double    voxelMedianValue = 0;
		Histogram histogram        = new Histogram();
		histogram.run(this.rawImage);
		Map<Double, Integer> nucleusHistogram = histogram.getHistogram();
		
		int     size      = rawImage.getHeight() * rawImage.getWidth() * rawImage.getNSlices();
		int     increment = 0;
		boolean even      = false;
		for (Entry<Double, Integer> entry : nucleusHistogram.entrySet()) {
			increment += entry.getValue();
			if (size == 2 * increment) {
				voxelMedianValue = entry.getKey();
				even = true;
			} else if (size < 2 * increment) {
				voxelMedianValue += entry.getKey();
				if (even) voxelMedianValue /= 2;
				break;
			}
		}
		return voxelMedianValue;
	}
	
	
	private double medianIntensityNucleus() {
		double voxelMedianValue = 0;
		int    nbNucleusVoxels  = 0;
		for (int f : this.segmentedNucleusHistogram.values()) {
			nbNucleusVoxels += f;
		}
		int     increment = 0;
		boolean even      = false;
		for (Map.Entry<Double, Integer> entry : segmentedNucleusHistogram.entrySet()) {
			increment += entry.getValue();
			if (nbNucleusVoxels == 2 * increment) {
				voxelMedianValue = entry.getKey();
				even = true;
			} else if (nbNucleusVoxels < 2 * increment) {
				voxelMedianValue += entry.getKey();
				if (even) voxelMedianValue /= 2;
				break;
			}
		}
		return voxelMedianValue;
	}
	

	private double medianIntensityBackground() {
		double voxelMedianValue   = 0;
		int    nbBackgroundVoxels = 0;
		for (int f : this.backgroundHistogram.values()) {
			nbBackgroundVoxels += f;
		}
		int     increment = 0;
		boolean even      = false;
		for (Map.Entry<Double, Integer> entry : this.backgroundHistogram.entrySet()) {
			increment += entry.getValue();
			if (nbBackgroundVoxels == 2 * increment) {
				voxelMedianValue = entry.getKey();
				even = true;
			} else if (nbBackgroundVoxels < 2 * increment) {
				voxelMedianValue += entry.getKey();
				if (even) voxelMedianValue /= 2;
				break;
			}
		}
		return voxelMedianValue;
	}
	
	
	/**
	 * list of parameters compute in this method returned in tabulated format
	 *
	 * @return list of parameters compute in this method returned in tabulated format
	 */
	public String nucleusParameter3D() {
		String results;
		histogramSegmentedNucleus();
		// double volume = computeVolumeObject2(255);
		
		double   volume         = computeVolumeObjectML();
		double   surfaceArea    = computeSurfaceObject(255);
		double   surfaceAreaNew = computeComplexSurface();
		double[] tEigenValues   = computeEigenValue3D(255);
		compute2dParameters();
		double[] sv = mcSurfaceAndVolume();
		//System.out.printf("Surface = %.3f   Volume = %.3f%n", sv[0], sv[1]);

		results = this.rawImage.getTitle() + ","
		          //  + computeVolumeObject2(255) + "\t"
		          + sv[1] + ","
		          + computeFlatnessAndElongation(255)[0] + ","
		          + computeFlatnessAndElongation(255)[1] +","
		          + equivalentSphericalRadius(sv[1]) + ","
		          + sv[0] + ","
		          + computeSphericity(sv[1], sv[0]) + ","
		          + meanIntensity() + ","
		          + meanIntensityBackground() + ","
		          + standardDeviationIntensity(meanIntensity()) + ","
		          + minIntensity() + ","
		          + maxIntensity() + ","
		          + medianComputingImage() +","
		          + medianIntensityNucleus() +","
		          + medianIntensityBackground() + ","
		          + this.rawImage.getHeight() * this.rawImage.getWidth() * this.rawImage.getNSlices()+","
		          + computeEigenValue3D(255)[0] + ","
		          + computeEigenValue3D(255)[1] + ","
		          + computeEigenValue3D(255)[2] + ","
				  + getAspectRatio()  + ","
		 		  + getCirculairty()

					;
		return results;
	}
	
}