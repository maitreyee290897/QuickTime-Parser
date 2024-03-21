package com.maitreyee.quicktime;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.Properties;

/**
 * A factory to provide leaf atom classes by type.
 * Looks up classes from <code>atomfactory.properties</code> props file. Returns generic ParsedLeafAtom if no
 * specific class is available for an atom type.
 */
public class FactoryAtom extends Object {
	
	static FactoryAtom instance;

	/**
	 * private constructor can only be called by the first
	 * getInstance()
	 */
	private FactoryAtom() {

		super();
	}
	
	/**
	 * returns the singleton, creating it if necessary
	 */
	public static FactoryAtom getInstance() {
		if (instance == null) instance = new FactoryAtom();
		return instance;
	}
	
	/**
	 * Returns a ParsedLeafAtom (or subclass) for the given
	 * type. Uses reflection to call the constructor with
	 * the supplied args. Default is basic ParsedLeafAtom but
	 * will give you a more specific atom if you have mapped
	 * the atom type to a class with the <code>atomfactory.properties</code> file.
	 */
	public LeafAtomParsed createAtomFor(long size, String type, RandomAccessFile raf) throws IOException {

		return new LeafAtomParsed(size, type, raf);
	}
}
