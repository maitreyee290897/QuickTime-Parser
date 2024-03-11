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
	
	static Class[] LEAF_ATOM_CONSTRUCTOR_ARGS = { long.class, String.class, RandomAccessFile.class };
	
	protected Properties props;
	
	/**
	 * location of the props file as found with ClassLoader
	 * getResource()... putting it in class' package structure
	 * keeps things tidy (works really well inside a jar too)
	 */
	public static final String PROPS_RESOURCE_NAME = "com/maitreyee/quicktime/atomfactory.properties";
	
	/**
	 * private constructor can only be called by the first
	 * getInstance()
	 */
	private FactoryAtom() {
		super();
		// get props file
		props = new Properties();
		try {
			InputStream propsIn = getClass().getClassLoader().getResourceAsStream(PROPS_RESOURCE_NAME);
			if (propsIn != null) props.load(propsIn);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
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
		String className = props.getProperty(type);
		if (className == null) {
			return new LeafAtomParsed(size, type, raf);
		}
		// now try to instantiate and populate (scary-ass reflection)
		try {
			Class atomClass = Class.forName(className);
			Constructor constructor = atomClass.getDeclaredConstructor(LEAF_ATOM_CONSTRUCTOR_ARGS);
			Object[] args = { new Long(size), type, raf };
			return (LeafAtomParsed) constructor.newInstance(args);
		} catch (Exception e) {
			e.printStackTrace();
			// if anything went wrong, return the simple leaf atom
			return new LeafAtomParsed(size, type, raf);
		}
	}
}
