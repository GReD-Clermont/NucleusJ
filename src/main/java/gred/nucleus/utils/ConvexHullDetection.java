package gred.nucleus.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Point;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/*
 * Copyright (c) 2010, Bart Kiers
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */


/**
 * Class which run a 2D convex hull algorithm to a set of voxels
 * <p>
 * Currently uses an implementation of the Graham Scan
 */
public class ConvexHullDetection {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	
	/**
	 * Run graham scan given a list of voxel after converting voxels to points
	 *
	 * @param axesName       current combined axe used
	 * @param lVoxelBoundary voxels of the boundaries
	 *
	 * @return voxels of the convex hull
	 */
	public static List<VoxelRecord> runGrahamScan(String axesName, List<? extends VoxelRecord> lVoxelBoundary) {
		List<Point> points      = new ArrayList<>();
		double      constantAxe = 0;
		
		switch (axesName) {
			case "xy":
				constantAxe = lVoxelBoundary.get(0).k;
				for (VoxelRecord v : lVoxelBoundary) {
					points.add(new Point((int) v.i, (int) v.j));
				}
				break;
			case "xz":
				constantAxe = lVoxelBoundary.get(0).j;
				for (VoxelRecord v : lVoxelBoundary) {
					points.add(new Point((int) v.i, (int) v.k));
				}
				break;
			case "yz":
				constantAxe = lVoxelBoundary.get(0).i;
				for (VoxelRecord v : lVoxelBoundary) {
					points.add(new Point((int) v.j, (int) v.k));
				}
				break;
		}
		
		List<Point>       pointsConvexHull = getConvexHull(points);
		List<VoxelRecord> convexHull       = new ArrayList<>();
		switch (axesName) {
			case "xy":
				for (Point p : pointsConvexHull) {
					VoxelRecord voxel = new VoxelRecord();
					voxel.setLocation(p.x, p.y, constantAxe);
					convexHull.add(voxel);
				}
				break;
			case "xz":
				for (Point p : pointsConvexHull) {
					VoxelRecord voxel = new VoxelRecord();
					voxel.setLocation(p.x, constantAxe, p.y);
					convexHull.add(voxel);
				}
				break;
			case "yz":
				for (Point p : pointsConvexHull) {
					VoxelRecord voxel = new VoxelRecord();
					voxel.setLocation(constantAxe, p.x, p.y);
					convexHull.add(voxel);
				}
				break;
		}
		convexHull.remove(0); // Remove the duplicate voxel (begin/end)
		return convexHull;
	}
	
	
	/**
	 * Returns the convex hull of the points created from {@code xs} and {@code ys}. Note that the first and last
	 * point in the returned {@code List<java.awt.Point>} are the same point.
	 *
	 * @param xs the x coordinates.
	 * @param ys the y coordinates.
	 *
	 * @return the convex hull of the points created from {@code xs} and {@code ys}.
	 *
	 * @throws IllegalArgumentException if {@code xs} and {@code ys} don't have the same size, if all points are
	 *                                  collinear or if there are less than 3 unique points present.
	 */
	public static List<Point> getConvexHull(int[] xs, int[] ys) {
		if (xs.length != ys.length) {
			throw new IllegalArgumentException("xs and ys don't have the same size");
		}
		
		List<Point> points = new ArrayList<>();
		
		for (int i = 0; i < xs.length; i++) {
			points.add(new Point(xs[i], ys[i]));
		}
		
		return getConvexHull(points);
	}
	
	
	/**
	 * Returns the convex hull of the points created from the list {@code points}. Note that the first and last point in
	 * the returned {@code List<java.awt.Point>} are the same point.
	 *
	 * @param points the list of points.
	 *
	 * @return the convex hull of the points created from the list {@code points}.
	 *
	 * @throws IllegalArgumentException if all points are collinear or if there are less than 3 unique points present.
	 */
	public static List<Point> getConvexHull(List<Point> points) {
		List<Point> sorted = new ArrayList<>(getSortedPointSet(points));
		
		if (sorted.size() < 3) {
			throw new IllegalArgumentException("can only create a convex hull of 3 or more unique points");
		}
		
		Stack<Point> stack = new Stack<>();
		stack.push(sorted.get(0));
		stack.push(sorted.get(1));
		
		for (int i = 2; i < sorted.size(); i++) {
			
			Point head   = sorted.get(i);
			Point middle = stack.pop();
			Point tail   = stack.peek();
			
			Turn turn = getTurn(tail, middle, head);
			
			switch (turn) {
				case COUNTER_CLOCKWISE:
					stack.push(middle);
					stack.push(head);
					break;
				case CLOCKWISE:
					i--;
					break;
				case COLLINEAR:
					stack.push(head);
					break;
			}
		}
		
		// close the hull
		stack.push(sorted.get(0));
		
		return new ArrayList<>(stack);
	}
	
	
	/**
	 * Returns the points with the lowest y coordinate. In case more than 1 such point exists, the one with the lowest x
	 * coordinate is returned.
	 *
	 * @param points the list of points to return the lowest point from.
	 *
	 * @return the points with the lowest y coordinate. In case more than 1 such point exists, the one with the lowest x
	 * coordinate is returned.
	 */
	protected static Point getLowestPoint(List<? extends Point> points) {
		
		Point lowest = points.get(0);
		
		for (int i = 1; i < points.size(); i++) {
			
			Point temp = points.get(i);
			
			if (temp.y < lowest.y || temp.y == lowest.y && temp.x < lowest.x) {
				lowest = temp;
			}
		}
		
		return lowest;
	}
	
	
	/**
	 * Returns a sorted set of points from the list {@code points}. The set of points are sorted in increasing order of
	 * the angle they and the lowest point {@code P} make with the x-axis. If tow (or more) points form the same angle
	 * towards {@code P}, the one closest to {@code P} comes first.
	 *
	 * @param points the list of points to sort.
	 *
	 * @return a sorted set of points from the list {@code points}.
	 *
	 * @see ConvexHullDetection#getLowestPoint(java.util.List)
	 */
	protected static Set<Point> getSortedPointSet(List<Point> points) {
		
		Point lowest = getLowestPoint(points);
		
		Set<Point> set = new TreeSet<>(new Comparator<Point>() {
			@Override
			public int compare(Point a, Point b) {
				
				if (a == b || a.equals(b)) {
					return 0;
				}
				
				// use longs to guard against int-underflow
				double thetaA = Math.atan2((long) a.y - lowest.y, (long) a.x - lowest.x);
				double thetaB = Math.atan2((long) b.y - lowest.y, (long) b.x - lowest.x);
				
				if (thetaA < thetaB) {
					return -1;
				} else if (thetaA > thetaB) {
					return 1;
				} else {
					// collinear with the 'lowest' point, let the point closest to it come first
					
					// use longs to guard against int-over/underflow
					double distanceA = Math.sqrt(((long) lowest.x - a.x) * ((long) lowest.x - a.x) +
					                             ((long) lowest.y - a.y) * ((long) lowest.y - a.y));
					double distanceB = Math.sqrt(((long) lowest.x - b.x) * ((long) lowest.x - b.x) +
					                             ((long) lowest.y - b.y) * ((long) lowest.y - b.y));
					
					if (distanceA < distanceB) {
						return -1;
					} else {
						return 1;
					}
				}
			}
		});
		
		set.addAll(points);
		
		return set;
	}
	
	
	/**
	 * Returns the GrahamScan#Turn formed by traversing through the ordered points {@code a}, {@code b} and
	 * {@code c}. More specifically, the cross product {@code C} between the 3 points (vectors) is calculated:
	 *
	 * {@code (b.x-a.x * c.y-a.y) - (b.y-a.y * c.x-a.x)}
	 * <p>
	 * and if {@code C} is less than 0, the turn is CLOCKWISE, if
	 * {@code C} is more than 0, the turn is COUNTER_CLOCKWISE, else
	 * the three points are COLLINEAR.
	 *
	 * @param a the starting point.
	 * @param b the second point.
	 * @param c the end point.
	 *
	 * @return the GrahamScan#Turn formed by traversing through the ordered points {@code a}, {@code b} and
	 * {@code c}.
	 */
	protected static Turn getTurn(Point a, Point b, Point c) {
		
		// use longs to guard against int-over/underflow
		long crossProduct = ((long) b.x - a.x) * ((long) c.y - a.y) -
		                    ((long) b.y - a.y) * ((long) c.x - a.x);
		
		if (crossProduct > 0) {
			return Turn.COUNTER_CLOCKWISE;
		} else if (crossProduct < 0) {
			return Turn.CLOCKWISE;
		} else {
			return Turn.COLLINEAR;
		}
	}
	
	
	/**
	 * An enum denoting a directional-turn between 3 points (vectors).
	 */
	protected enum Turn {CLOCKWISE, COUNTER_CLOCKWISE, COLLINEAR}
	
}