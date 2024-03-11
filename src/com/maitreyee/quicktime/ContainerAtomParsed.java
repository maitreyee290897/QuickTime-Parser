package com.maitreyee.quicktime;

public class ContainerAtomParsed extends AtomParsed {
	
	protected AtomParsed[] children;
	
	protected ContainerAtomParsed(long size, String type, AtomParsed[] children) {
		super(size, type);
		this.children = children;
	}
	
	public AtomParsed[] getChildren() {
		return children;
	}
	
	@Override
	public String toString() {
		return type + " (" + size + " bytes) - " + children.length + (children.length == 1 ? " child" : " children");
		
	}
	
}
