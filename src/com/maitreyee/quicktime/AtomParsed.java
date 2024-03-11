package com.maitreyee.quicktime;

public abstract class AtomParsed extends Object {

	protected int height;
	protected int width;

	protected long size;
	protected String type;

	
	public long getSize() {
		return size;
	}
	
	public String getType() {
		return type;
	}

	protected AtomParsed(long size, String type) {
		this.size = size;
		this.type = type;
	}
	
} // ParsedAtom
