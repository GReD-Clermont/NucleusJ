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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Point;
import java.lang.invoke.MethodHandles;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class which run a 2D convex hull algorithm to a set of voxels
 * <p>
 * Currently uses an implementation of the Graham Scan
 */
public final class ConvexHullDetection {
	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	

	/** Private constructor to prevent instantiation */
	private ConvexHullDetection() {
		// Private constructor to prevent instantiation
	}
	
	
	/**
	 * Run graham scan given a list of voxel after converting voxels to points
	 *
	 * @param axesName       current combined axe used
	 * @param lVoxelBoundary voxels of the boundaries
	 *
	 * @return voxels of the convex hull
	 */
	public static List<VoxelRecord> runGrahamScan(String axesName, List<? extends VoxelRecord> lVoxelBoundary) {
		List<Point> points      = new ArrayList<>(lVoxelBoundary.size());
		double      constantAxe = 0;
		
		switch (axesName) {
			case "xy":
				constantAxe = lVoxelBoundary.get(0).getK();
				for (VoxelRecord v : lVoxelBoundary) {
					points.add(new Point((int) v.getI(), (int) v.getJ()));
				}
				break;
			case "xz":
				constantAxe = lVoxelBoundary.get(0).getJ();
				for (VoxelRecord v : lVoxelBoundary) {
					points.add(new Point((int) v.getI(), (int) v.getK()));
				}
				break;
			case "yz":
				constantAxe = lVoxelBoundary.get(0).getI();
				for (VoxelRecord v : lVoxelBoundary) {
					points.add(new Point((int) v.getJ(), (int) v.getK()));
				}
				break;
			default:
				// This should never happen
				LOGGER.error("Invalid axes name: {}", axesName);
				throw new IllegalArgumentException("Invalid axes name: " + axesName);
		}
		
		List<Point> pointsConvexHull = getConvexHull(points);
		
		List<VoxelRecord> convexHull = new ArrayList<>(pointsConvexHull.size());
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
			default:
				// This should never happen
				LOGGER.error("Invalid axes name: {}", axesName);
				throw new IllegalArgumentException("Invalid axes name: " + axesName);
		}
		convexHull.remove(0); // Remove the duplicate voxel (begin/end)
		return convexHull;
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
		
		Deque<Point> stack = new ArrayDeque<>(sorted.size());
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
	private static Set<Point> getSortedPointSet(List<? extends Point> points) {
		
		Point lowest = getLowestPoint(points);
		
		Set<Point> set = new TreeSet<>(new Comparator<Point>() {
			@Override
			public int compare(Point a, Point b) {
				if (a == b || a.equals(b)) {
					return 0;
				}
				
				// use longs to guard against int-underflow
				double thetaA = StrictMath.atan2((double) a.y - lowest.y, (double) a.x - lowest.x);
				double thetaB = StrictMath.atan2((double) b.y - lowest.y, (double) b.x - lowest.x);
				
				if (thetaA < thetaB) {
					return -1;
				} else if (thetaA > thetaB) {
					return 1;
				} else {
					// collinear with the 'lowest' point, let the point closest to it come first
					
					// use longs to guard against int-over/underflow
					double distanceA = Math.sqrt(((double) lowest.x - a.x) * ((double) lowest.x - a.x) +
					                             ((double) lowest.y - a.y) * ((double) lowest.y - a.y));
					double distanceB = Math.sqrt(((double) lowest.x - b.x) * ((double) lowest.x - b.x) +
					                             ((double) lowest.y - b.y) * ((double) lowest.y - b.y));
					
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
	 * Returns the GrahamScan#Turn formed by traversing through the ordered points {@code a}, {@code b} and {@code c}.
	 * More specifically, the cross product {@code C} between the 3 points (vectors) is calculated:
	 * <p>
	 * {@code (b.x-a.x * c.y-a.y) - (b.y-a.y * c.x-a.x)}
	 * <p>
	 * and if {@code C} is less than 0, the turn is CLOCKWISE, if {@code C} is more than 0, the turn is
	 * COUNTER_CLOCKWISE, else the three points are COLLINEAR.
	 *
	 * @param a the starting point.
	 * @param b the second point.
	 * @param c the end point.
	 *
	 * @return the GrahamScan#Turn formed by traversing through the ordered points {@code a}, {@code b} and {@code c}.
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