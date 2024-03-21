package com.maitreyee.quicktime;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;

public class AtomTreeParser extends Object {
	/**
	 * QuickTime container atoms solely consist of other atoms.
	 * Therefore, when encountering one, we recursively explore
	 * its contents to retrieve its children.
	 */
	public static final String[] ATOM_CONTAINER_TYPE_STRINGS = { "moov", "trak", "udta", "tref", "imap", "mdia", "minf", "stbl", "edts", "mdra", "rmra", "imag", "vnrp", "dinf" };
	/**
	 * A set is utilized for rapid retrieval of potential atom containers,
	 * encompassing all elements from ATOM_CONTAINER_TYPE_STRINGS
	 */
	public static final HashSet ATOM_CONTAINER_TYPES = new HashSet();
	static {
		for (int i = 0; i < ATOM_CONTAINER_TYPE_STRINGS.length; i++) {
			ATOM_CONTAINER_TYPES.add(ATOM_CONTAINER_TYPE_STRINGS[i]);
		} // for
	}
	
	static byte[] atomSizeBuf = new byte[4];
	static byte[] atomTypeBuf = new byte[4];
	static byte[] maitreyee = new byte[32];
	static byte[] extendedAtomSizeBuf = new byte[8];
	
	/**
	 * Retrieve the top-level atoms, thereby parsing
	 * the entire structure from the provided file.
	 */
	public static AtomParsed[] parseGivenAtoms(File f) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(f, "r");
		AtomParsed[] atoms = parseGivenAtoms(raf, 0, raf.length());
		raf.close();
		return atoms;
	}
	
	protected static AtomParsed[] parseGivenAtoms(RandomAccessFile raf, long firstOffset, long stopAtOffsetValue) throws IOException {
		

		//The variable "off" represents the atom's offset into the file.
		// It gets reset for the next sibling at the bottom of the loop,
		// after the size of the preceding sibling is read.

		long offsetValue = firstOffset;
		ArrayList parsedAtomList = new ArrayList();
		// while (raf.getFilePointer() <= stopAt) {
		while (offsetValue < stopAtOffsetValue) {
			raf.seek(offsetValue);

			//The process involves:
			// The first 32 bits represent the atom size.
			// Utilize BigInteger to convert bytes into a long data type,
			// rather than a signed integer.
			int bytesRead = raf.read(atomSizeBuf, 0, atomSizeBuf.length);
			if (bytesRead < atomSizeBuf.length) throw new IOException("couldn't read atom length");
			BigInteger atomSizeBI = new BigInteger(atomSizeBuf);
			//avoid integer overflow so used long
			long atomSize = atomSizeBI.longValue();
			
			// this is kind of a hack to handle the udta problem
			// (see below) when the parent didn't have children,
			// meaning we've read 4 bytes of 0 and the atom is
			// already over
			if (raf.getFilePointer() == stopAtOffsetValue) break;
			
			// 2. next, the atom type
			bytesRead = raf.read(atomTypeBuf, 0, atomTypeBuf.length);
			if (bytesRead != atomTypeBuf.length) throw new IOException("Couldn't read atom type");
			String atomType = new String(atomTypeBuf);

			// 3. if atomSize was 1, then there are 64 bits of extended size
			if (atomSize == 1) {
				bytesRead = raf.read(extendedAtomSizeBuf, 0, extendedAtomSizeBuf.length);
				if (bytesRead != extendedAtomSizeBuf.length) throw new IOException("Couldn't read extended atom size");
				BigInteger extendedSizeBI = new BigInteger(extendedAtomSizeBuf);
				atomSize = extendedSizeBI.longValue();
			}
			
			// if this atom size is negative, or extends past end
			// of file, it's extremely suspicious (ie, we're not
			// really in a quicktime file)
			if ((atomSize < 0) || ((offsetValue + atomSize) > raf.length())) throw new IOException("atom has invalid size: " + atomSize);
			
			// 4. if a container atom, then parse the children
			AtomParsed atomParsed = null;
			if (ATOM_CONTAINER_TYPES.contains(atomType)) {
				// children run from current point to the end of the atom
				AtomParsed[] children = parseGivenAtoms(raf, raf.getFilePointer(), offsetValue + atomSize);
				atomParsed = new ContainerAtomParsed(atomSize, atomType, children);
			} else {
				if (atomType.equals("stsd")) {

					parseSTSDAtom(raf, offsetValue + 8, atomSize - 8);
				}
				atomParsed = FactoryAtom.getInstance().createAtomFor(atomSize, atomType, raf);
			}
			
			// add atom to the list
			parsedAtomList.add(atomParsed);
			
			// now set offset to next atom (or end-of-file
			// in special case (atomSize = 0 means atom goes
			// to EOF)
			if (atomSize == 0)
				offsetValue = raf.length();
			else
				offsetValue += atomSize;

			if (atomType.equals("udta")) offsetValue += 4;
			
		} // while not at stopAt
		
		// convert the array list into an array
		AtomParsed[] atomArray = new AtomParsed[parsedAtomList.size()];
		parsedAtomList.toArray(atomArray);
		return atomArray;
	} // parseAtoms

	private static void parseSTSDAtom(RandomAccessFile fis, long startPos, long size) throws IOException {

		byte[] sizeBytes = new byte[4];
		fis.read(sizeBytes);
		int descriptionSize = ((sizeBytes[0] & 0xFF) << 24) | ((sizeBytes[1] & 0xFF) << 16) | ((sizeBytes[2] & 0xFF) << 8) | (sizeBytes[3] & 0xFF);


		fis.skipBytes(4+4+6+4+4+4+4+4+2);

		byte[] widthDimension = new byte[2];
		fis.read(widthDimension);
		int width = ((widthDimension[0] & 0xFF) << 8) | (widthDimension[1] & 0xFF);

		byte[] heightBytes = new byte[2];
		fis.read(heightBytes);
		int height = ((heightBytes[0] & 0xFF) << 8) | (heightBytes[1] & 0xFF);

		if(height == 0) {

			System.out.println("Audio: " + width + " Hz");
		} else {

			System.out.println("Video Dimensions: " + width + " x " + height);
		}
		System.out.println("\n");
		fis.seek(startPos);

	}


	private static void parseSTSDAtom2(RandomAccessFile fis, long startPos, long size) throws IOException {

		byte[] sizeBytes = new byte[4];
		fis.read(sizeBytes);
		int descriptionSize = ((sizeBytes[0] & 0xFF) << 24) | ((sizeBytes[1] & 0xFF) << 16) | ((sizeBytes[2] & 0xFF) << 8) | (sizeBytes[3] & 0xFF);


		fis.skipBytes(4+4+6+4+4+4+4+4+2);

		byte[] widthDimension = new byte[2];
		fis.read(widthDimension);
		int width = ((widthDimension[0] & 0xFF) << 8) | (widthDimension[1] & 0xFF);
//		System.out.println("width: " + width);

//		byte[] widthBytes = new byte[2];
//		fis.read(widthBytes);
//		int width = ((widthBytes[0] & 0xFF) << 8) | (widthBytes[1] & 0xFF);


		byte[] heightBytes = new byte[2];
		fis.read(heightBytes);
		int height = ((heightBytes[0] & 0xFF) << 8) | (heightBytes[1] & 0xFF);


		System.out.println("Rate: " + width + "hz");

//		fis.seek(startPos);

	}

	/**
	 * debug and parse the atom chosen by the user
	 */
	public static void main(String[] args) {
		JFileChooser sampleFile = new JFileChooser();
		int response = sampleFile.showOpenDialog(null);
		if (response == JFileChooser.CANCEL_OPTION) return;
		File f = sampleFile.getSelectedFile();
		try {
			AtomParsed[] atomStructure = parseGivenAtoms(f);
			System.out.println("The Atom tree is as follows:  \n");
			displayAtomTree(atomStructure, "");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			// awt will be hanging around because of the filechooser
			// so let's go away now
			System.exit(0);
		}
	} // main
	
	/**
	 * A helper function for the main program, it recursively prints atoms along
	 * with their children, adding additional indentation for
	 * each subsequent generation.
	 */
	protected static final void displayAtomTree(AtomParsed[] atomHeirarchy, String indent) {
		for (int i = 0; i < atomHeirarchy.length; i++) {
			System.out.print(indent);
			AtomParsed atom = atomHeirarchy[i];
			System.out.println(atom.toString());

			if (atom instanceof ContainerAtomParsed) {
				AtomParsed[] children = ((ContainerAtomParsed) atom).getChildren();
				displayAtomTree(children, indent + "  ");
			}
		}
	}
	
}
