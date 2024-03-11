
package com.maitreyee.quicktime;

import java.io.IOException;
import java.io.RandomAccessFile;

public class WLOCAtomParsed extends LeafAtomParsed {
	
	int x;
	int y;
	
	public WLOCAtomParsed(long size, String type, RandomAccessFile raf) throws IOException {
		super(size, type, raf);
	}
	
	@Override
	public void init(RandomAccessFile raf) throws IOException {
		// WLOC contains 16-bit x,y values
		byte[] value = new byte[4];
		raf.read(value, 0, value.length);
		x = (value[0] << 8) | value[1];
		y = (value[2] << 8) | value[3];
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	@Override
	public String toString() {
		return super.toString() + " (x,y) == (" + x + "," + y + ")";
		
	}
	
}
