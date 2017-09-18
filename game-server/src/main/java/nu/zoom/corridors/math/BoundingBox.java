/*
 * This file is copyright 2013 by Johan Maasing
 * You have no rights to this file. You may not distribute it in any form or shape.
 */
package nu.zoom.corridors.math;

import static com.google.common.base.Preconditions.*;

/**
 * Axis aligned bounding box.
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class BoundingBox {

	private final Tuple2f min;
	private final Tuple2f max;

	/**
	 * Create the bounding box. Degenerate boxes will throw an exception.
	 *
	 * @param min The min corner, may not be null. Must be less than max.
	 * @param max The max corner, may not be null. Must be greater than min.
	 */
	public BoundingBox(Tuple2f min, Tuple2f max) {
		this.min = checkNotNull(min);
		this.max = checkNotNull(max);
		checkArgument(min.x < max.x, "Min X %s must be smaller than Max X %s", min.x, max.x);
		checkArgument(min.y < max.y, "Min Y must be smaller than Max Y");
	}

	public boolean isInside(Tuple2f point) {
		return ((point.x > this.min.x) && (point.x < this.max.x) && (point.y > this.min.y) && (point.y < this.max.y));
	}

	public Tuple2f getMin() {
		return min;
	}

	public Tuple2f getMax() {
		return max;
	}

	public boolean intersect(BoundingBox worldAABB) {
		final Tuple2f otherMin = worldAABB.getMin();
		final Tuple2f otherMax = worldAABB.getMax();
//		http://gamedev.stackexchange.com/questions/586/what-is-the-fastest-way-to-work-out-2d-bounding-box-intersection
//		!(r2.left > r1.right
//        || r2.right < r1.left
//        || r2.top > r1.bottom
//        || r2.bottom < r1.top);	}
		// Test if they definately do NOT intersect and invert value
		return !(otherMin.x > max.x
				|| otherMax.x < min.x
				|| otherMin.y > max.y
				|| otherMax.y < min.y);
	}
}
