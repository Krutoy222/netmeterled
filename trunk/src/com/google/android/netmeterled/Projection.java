package com.google.android.netmeterled;


class Projection {
	final public int mWidth;
	final public int mHeight;
	final public int mOffset;
	final public int maxX;
	final public int maxY;
	final private float mXscale;
	final private float mYscale;

	public Projection(int width, int height, int xoffset,
					int x_range, int y_range) {
		mWidth = width;
		mHeight = height;
		mOffset = xoffset;
		maxX = x_range;
		maxY = y_range;
		mXscale = (float)(width) / x_range;
		mYscale = (float)(height) / y_range;
	}
	public float multXScale(int x) {
		return x * mXscale + 5;
	}
	public float multYScale(int y) {
		return mHeight - (y * mYscale) + mOffset;
	}
}