/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/java-pdf-library-support/
 *
 * (C) Copyright 1997-2013, IDRsolutions and Contributors.
 *
 * 	This file is part of JPedal
 *
     This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


 *
 * ---------------
 * CFFWriter.java
 * ---------------
 */
package org.jpedal.fonts.tt.conversion;

import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.Type1;
import org.jpedal.fonts.Type1C;
import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.utils.LogWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CFFWriter extends Type1 implements FontTableWriter{


    final private static boolean debugTopDictOffsets = false;

    private String name;
    private byte[][] subrs;
    final private String[] glyphNames;
    private byte[][] charstrings;
    private int[] charstringXDisplacement, charstringYDisplacement;
    private byte[] header, nameIndex, topDictIndex, globalSubrIndex, encodings, charsets, charStringsIndex, privateDict, localSubrIndex, stringIndex;
    final private ArrayList<String> strings = new ArrayList<String>();
    private int[] widthX, widthY, lsbX, lsbY;
    private int defaultWidthX, nominalWidthX;
    private ArrayList<CharstringElement> currentCharString;
    private int currentCharStringID;
    private float[] bbox = new float[4];

    //Values for processing flex sections
    private boolean inFlex=false;
    private CharstringElement currentFlexCommand;
    private boolean firstArgsAdded=false;

    //Values for dealing with incorrect em square
    private double emSquareSize=1000;
    private double scale = 1;
    private boolean inSeac=false;

    public CFFWriter(PdfJavaGlyphs glyphs, String name) {

        this.glyphs = glyphs;
        this.name = name;

        //Fetch charstrings and subrs
        Map charStringSegments = glyphs.getCharStrings();

        //Count subrs and chars
        Object[] keys = charStringSegments.keySet().toArray();
        Arrays.sort(keys);
        int maxSubrNum=0, maxSubrLen=0, charCount=0;
        for (int i=0; i<charStringSegments.size(); i++) {
            String key = (String) keys[i];
            if (key.startsWith("subrs")) {
                int num = Integer.parseInt(key.replaceAll("[^0-9]",""));
                if (num > maxSubrNum) {
                    maxSubrNum = num;
                }
                int len = ((byte[])charStringSegments.get(key)).length;
                if (len > maxSubrLen) {
                    maxSubrLen = len;
                }
            } else {
                charCount++;
            }
        }

        //Move to array
        subrs = new byte[maxSubrNum+1][];
        glyphNames = new String[charCount];
        charstrings = new byte[charCount][];
        charstringXDisplacement = new int[charCount];
        charstringYDisplacement = new int[charCount];
        charCount=0;
        for (int i=0; i<charStringSegments.size(); i++) {
            String key = (String) keys[i];
            Object obj = charStringSegments.get(key);
            byte[] cs = ((byte[])obj);
            if (key.startsWith("subrs")) {
                int num = Integer.parseInt(key.replaceAll("[^0-9]",""));
                subrs[num] = cs;
            } else {
                glyphNames[charCount] = key;
                charstrings[charCount] = cs;
                charCount++;
            }
        }

        convertCharstrings();

    }

    /**
     * Convert the charstrings from type 1 to type 2.
     */
    private void convertCharstrings() {

        /**
         * Convert instructions
         */
        try {

            widthX = new int[charstrings.length];
            widthY = new int[charstrings.length];
            lsbX = new int[charstrings.length];
            lsbY = new int[charstrings.length];

            //Perform initial conversion
            byte[][] newCharstrings = new byte[charstrings.length][];
            for (int charstringID=0; charstringID<charstrings.length; charstringID++) {
                newCharstrings[charstringID] = convertCharstring(charstrings[charstringID], charstringID);
            }

            //Check em square size and reconvert while scaling if necessary
            if (bbox[2] - bbox[0] > 1100) {

                //Calculate em size and scaling to apply
                emSquareSize = (bbox[2]-bbox[0]);
                scale = 1d/(emSquareSize/1000d);

                //Reset displacements & bbox
                charstringXDisplacement = new int[charstringXDisplacement.length];
                charstringYDisplacement = new int[charstringYDisplacement.length];
                bbox = new float[4];

                //Re-convert charstrings now scale is set
                for (int charstringID=0; charstringID<charstrings.length; charstringID++) {
                    newCharstrings[charstringID] = convertCharstring(charstrings[charstringID], charstringID);
                }
                charstrings = newCharstrings;

            } else {
                charstrings = newCharstrings;
            }

        } catch (Exception e) {
            //tell user and log
            if(LogWriter.isOutput())
                LogWriter.writeLog("Exception: "+e.getMessage());
        }

        /**
         * Calculate values for defaultWidthX and nominalWidthX & add widths to start of charstrings
         */

        //Find counts for each value
        HashMap<Integer, Integer> valueCount = new HashMap<Integer, Integer>();
        for (int i=0; i<charstrings.length; i++) {
            Integer count = valueCount.get(Integer.valueOf(widthX[i]));
            if (count == null) {
                count = 1;
            } else {
                count = count+1;
            }
            valueCount.put(widthX[i], count);
        }

        //Find most common value to use as defaultWidthX
        Object[] values = valueCount.keySet().toArray();
        int maxCount=0;
        defaultWidthX=0;
        for (Object value : values) {
            int count = valueCount.get(value);
            if (count > maxCount) {
                maxCount = count;
                defaultWidthX = (Integer)value;
            }
        }

        //Find average for nominalWidthX
        int total=0;
        int count=0;
        for (Object value : values) {
            if ((Integer)value != defaultWidthX) {
                count++;
                total += (Integer)value;
            }
        }
        if (count != 0) {
            nominalWidthX = total / count;
        } else {
            nominalWidthX = 0;
        }

        //Blank default widths and update other widths
        for (int i=0; i<widthX.length; i++) {
            if (widthX[i] == defaultWidthX) {
                widthX[i] = Integer.MIN_VALUE;
            } else {
                widthX[i] = widthX[i] - nominalWidthX;
            }
        }

        //Append widths to start of charstrings (but not if it's 0 as this signifies default)
        for (int i=0; i<widthX.length; i++) {
            if (widthX[i] != Integer.MIN_VALUE) {
                byte[] width = FontWriter.setCharstringType2Number(widthX[i]);
                byte[] newCharstring = new byte[width.length+charstrings[i].length];
                System.arraycopy(width, 0, newCharstring, 0, width.length);
                System.arraycopy(charstrings[i], 0, newCharstring, width.length, charstrings[i].length);
                charstrings[i] = newCharstring;
            }
        }

        //Check all charstrings end in endchar and append if not
        for (int i=0; i<charstrings.length; i++) {
            if (charstrings[i][charstrings[i].length-1] != 14) {
                byte[] newCharstring = new byte[charstrings[i].length+1];
                System.arraycopy(charstrings[i], 0, newCharstring, 0, charstrings[i].length);
                newCharstring[newCharstring.length-1] = 14;
                charstrings[i] = newCharstring;
            }
        }

    }

    /**
     * Convert a charstring from type 1 to type 2.
     * @param charstring The charstring to convert
     * @param charstringID The number of the charstring to convert
     * @return The converted charstring
     */
    private byte[] convertCharstring(byte[] charstring, int charstringID) {

        int[] cs = new int[charstring.length];
        for (int i=0; i<charstring.length; i++) {
            cs[i] = charstring[i];
            if (cs[i] < 0) {
                cs[i] += 256;
            }
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        currentCharString = new ArrayList<CharstringElement>();
        currentCharStringID = charstringID;


        //Convert to CharstringElements
        CharstringElement element;
        for (int i=0; i < cs.length; i+=element.getLength()) {
            element = new CharstringElement(cs, i);
        }

        //Rescale commands if necessary
        if (emSquareSize != 1000 && !inSeac) {
            for (CharstringElement e : currentCharString) {
                e.scale();
            }
            widthX[charstringID] = (int)(scale*widthX[charstringID]);
            widthY[charstringID] = (int)(scale*widthY[charstringID]);
            lsbX[charstringID] = (int)(scale*lsbX[charstringID]);
            lsbY[charstringID] = (int)(scale*lsbY[charstringID]);
        }

        //Calculate and store displacement and bbox
        for (CharstringElement e : currentCharString) {
            int[] d = e.getDisplacement();
            charstringXDisplacement[charstringID] += d[0];
            charstringYDisplacement[charstringID] += d[1];
            bbox[0] = charstringXDisplacement[charstringID] < bbox[0] ? charstringXDisplacement[charstringID] : bbox[0];
            bbox[1] = charstringYDisplacement[charstringID] < bbox[1] ? charstringYDisplacement[charstringID] : bbox[1];
            bbox[2] = charstringXDisplacement[charstringID] > bbox[2] ? charstringXDisplacement[charstringID] : bbox[2];
            bbox[3] = charstringYDisplacement[charstringID] > bbox[3] ? charstringYDisplacement[charstringID] : bbox[3];
        }

        //Print for debug
//                System.out.println("Charstring "+charstringID);
//                for (CharstringElement currentElement : currentCharString) {
//                    byte[] e = currentElement.getType2Bytes();
//                    for (byte b : e) {
//                        String bin = Integer.toBinaryString(b);
//                        int addZeros = 8 - bin.length();
//                        for (int k=0; k<addZeros; k++)
//                            System.out.print("0");
//                        if (addZeros < 0)
//                            bin = bin.substring(-addZeros);
//                        int val = b;
//                        if (val < 0)
//                            val += 256;
//                        System.out.println(bin+"\t"+val);
//                    }
//                    System.out.println(currentElement);
//                }
//                    System.out.println();
//                System.out.println();

        //Convert to type 2
        try {
            for (CharstringElement currentElement : currentCharString) {
                bos.write(currentElement.getType2Bytes());
            }
        } catch(IOException e) {
            //tell user and log
            if(LogWriter.isOutput())
                LogWriter.writeLog("Exception: "+e.getMessage());
        }

        return bos.toByteArray();
    }


    /**
     * Returns a string ID for a given string. It first checks in the standard strings, and if it isn't there places it
     * in the array of custom strings.
     * @param text String to fetch ID for
     * @return String ID
     */
    public int getSIDForString(String text) {
        for (int i=0; i<Type1C.type1CStdStrings.length; i++) {
            if (text.equals(Type1C.type1CStdStrings[i])) {
                return i;
            }
        }

        for (int i=0; i<strings.size(); i++) {
            if (text.equals(strings.get(i))) {
                return 391+i;
            }
        }

        strings.add(text);
        return 390+strings.size();
    }


    /**
     * Retrieve the final whole table.
     * @return the new CFF table
     * @throws IOException
     */
    public byte[] writeTable() throws IOException {

        ByteArrayOutputStream bos=new ByteArrayOutputStream();

        //Set as empty array for top Dict generator
        topDictIndex = globalSubrIndex = stringIndex = encodings = charsets = charStringsIndex = privateDict = localSubrIndex = new byte[]{};

        /**
         * Generate values
         */
        header = new byte[]{TTFontWriter.setNextUint8(1),                   //major
                TTFontWriter.setNextUint8(0),                               //minor
                TTFontWriter.setNextUint8(4),                               //headerSize
                TTFontWriter.setNextUint8(2)};                              //offSize
        nameIndex = createIndex(new byte[][]{name.getBytes()});

        if (debugTopDictOffsets) {
            System.out.println("Generating first top dict...");
        }

        topDictIndex = createIndex(new byte[][]{createTopDict()});
        globalSubrIndex = createIndex(new byte[][]{});
        //Global Subr INDEX                                             -Probably don't need
        encodings = createEncodings();
        charsets = createCharsets();
        //FDSelect                                              //CIDFonts only
        charStringsIndex = createIndex(charstrings);            //per-font                                            //Might need to reorder, although .notdef does seem to be first
        //Font DICT INDEX                                       //per-font, CIDFonts only
        privateDict = createPrivateDict();                      //per-font
//        localSubrIndex = createIndex(subrs);                    //per-font or per-Private DICT for CIDFonts - Subr's are currently inlined
        //Copyright and Trademark Notices
        stringIndex = createIndex(createStrings());             //Generate last as strings are added as required by other sections


        //Regenerate private dict until length is stable
        byte[] lastPrivateDict;
        do {
            lastPrivateDict = new byte[privateDict.length];
            System.arraycopy(privateDict, 0, lastPrivateDict, 0, privateDict.length);
            privateDict = createPrivateDict();
        } while (!Arrays.equals(privateDict, lastPrivateDict));


        //Regenerate top dict index until length is stable
        byte[] lastTopDictIndex;
        do {
            lastTopDictIndex = new byte[topDictIndex.length];
            System.arraycopy(topDictIndex, 0, lastTopDictIndex, 0, topDictIndex.length);
            if (debugTopDictOffsets) {
                System.out.println("Current length is "+lastTopDictIndex.length+". Testing against new...");
            }
            topDictIndex = createIndex(new byte[][]{createTopDict()});
        } while (!Arrays.equals(lastTopDictIndex, topDictIndex));

        if (debugTopDictOffsets) {
            System.out.println("Length matches, offsets are now correct.");
        }

        /**
         * Write out
         */
        bos.write(header);
        bos.write(nameIndex);
        bos.write(topDictIndex);
        bos.write(stringIndex);
        bos.write(globalSubrIndex);
        bos.write(encodings);
        bos.write(charsets);
        bos.write(charStringsIndex);
        bos.write(privateDict);
//        bos.write(localSubrIndex);   //Subr's are currently inlined so this is not needed

        return bos.toByteArray();
    }


    /**
     * Create an index as a byte array
     * @param data Array of byte array data chunks
     * @return Byte array of index
     * @throws IOException if cannot write data
     */
    private static byte[] createIndex(byte[][] data) throws IOException {

        int count = data.length;

        //Check for empty index
        if (count == 0) {
            return new byte[]{0,0};
        }

        //Generate offsets
        int[] offsets = new int[count+1];
        offsets[0] = 1;
        for (int i=1; i<count+1; i++) {
            byte[] cs = data[i-1];
            if (cs != null) {
                offsets[i] = offsets[i-1] + cs.length;
            } else {
                offsets[i] = offsets[i-1];
            }
        }
        //Generate offSize
        int offSize = getOffsizeForMaxVal(offsets[count]);

        int len = 3+(offSize*offsets.length)+offsets[count];
        ByteArrayOutputStream bos = new ByteArrayOutputStream(len);

        //Write out
        bos.write(TTFontWriter.setNextUint16(count));                       //count
        bos.write(TTFontWriter.setNextUint8(offSize));                      //offSize
        for (int offset : offsets) {
            bos.write(FontWriter.setUintAsBytes(offset, offSize));          //offsets
        }
        for (byte[] item : data) {
            if (item != null) {
                bos.write(item);                                             //data
            }
        }

        return bos.toByteArray();
    }

    /**
     * Create the Top Dict.
     * @return a byte array representing the Top DICT
     * @throws IOException is ByteArrayOutputStream breaks
     */
    private byte[] createTopDict() throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        //Version                       0
        bos.write(FontWriter.set1cNumber(getSIDForString("1")));
        bos.write((byte)0);

        //Notice                        1
        if (copyright != null) {
            bos.write(FontWriter.set1cNumber(getSIDForString(copyright)));
            bos.write((byte)1);
        }

        //FontBBox                      5
        bos.write(FontWriter.set1cNumber((int)bbox[0]));
        bos.write(FontWriter.set1cNumber((int)bbox[1]));
        bos.write(FontWriter.set1cNumber((int)bbox[2]));
        bos.write(FontWriter.set1cNumber((int)bbox[3]));
        bos.write((byte)5);

//        //FontMatrix
//        //Commented out as mac doesn't support FontMatrix :(
//        //Reduce font size if em square is incorrect
//        if (emSquareSize != 1000) {
//            bos.write(FontWriter.set1cRealNumber(1d/emSquareSize));
//            bos.write(FontWriter.set1cNumber(0));
//            bos.write(FontWriter.set1cNumber(0));
//            bos.write(FontWriter.set1cRealNumber(1d/emSquareSize));
//            bos.write(FontWriter.set1cNumber(0));
//            bos.write(FontWriter.set1cNumber(0));
//            bos.write(new byte[]{12,7});
//        }

        //encoding                      16
        int loc = header.length+nameIndex.length+topDictIndex.length+stringIndex.length+globalSubrIndex.length;
        if (encodings.length != 0) {
            bos.write(FontWriter.set1cNumber(loc));
            if (debugTopDictOffsets) {
                System.out.println("Encoding offset: "+loc);
            }
            bos.write((byte)16);
        }

        //charset                       15
        loc += encodings.length;
        bos.write(FontWriter.set1cNumber(loc));
        if (debugTopDictOffsets) {
            System.out.println("Charset offset: "+loc);
        }
        bos.write((byte)15);

        //charstrings                   17
        loc += charsets.length;
        bos.write(FontWriter.set1cNumber(loc));
        if (debugTopDictOffsets) {
            System.out.println("Charstrings offset: "+loc);
        }
        bos.write((byte)17);

        //private                       18
        loc += charStringsIndex.length;
        bos.write(FontWriter.set1cNumber(privateDict.length));
        bos.write(FontWriter.set1cNumber(loc));
        if (debugTopDictOffsets) {
            System.out.println("Private offset: "+loc);
        }
        bos.write((byte)18);


        return bos.toByteArray();
    }

    /**
     * Create the Strings ready to place into an index
     * @return The strings as an array of byte arrays
     */
    private byte[][] createStrings() {
        byte[][] result = new byte[strings.size()][];

        for (int i=0; i<strings.size(); i++) {
            result[i] = strings.get(i).getBytes();
        }

        return result;
    }

    /**
     * Create charsets table.
     * @return byte array representing the Charsets
     */
    private byte[] createCharsets() {

        //Make sure .notdef removed
        String[] names=null;
        for (int i=0; i<glyphNames.length; i++) {
            if (".notdef".equals(glyphNames[i])) {
                names = new String[glyphNames.length-1];
                System.arraycopy(glyphNames, 0, names, 0, i);
                System.arraycopy(glyphNames, i+1, names, i, names.length-i);
            }
        }

        if (names == null) {
            names = glyphNames;
        }

        //Create array for result
        byte[] result = new byte[(names.length*2)+1];

        //Leave first byte blank for format 0, then fill rest of array with 2-byte SIDs
        for (int i=0; i<names.length; i++) {
            byte[] sid = FontWriter.setUintAsBytes(getSIDForString(names[i]), 2);

            result[1+(i*2)] = sid[0];
            result[2+(i*2)] = sid[1];
        }

        return result;
    }

    /**
     * Create Encodings table
     * @return byte array representing the Encodings
     */
    private static byte[] createEncodings() {
        return new byte[0];
    }

    /**
     * Create the Private dictionary
     * @return byte array representing the Private dict
     * @throws IOException if ByteOutputStream breaks
     */
    private byte[] createPrivateDict() throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

//        //Subrs                        19     Subr's are currently inlined so this isn't needed
//        bos.write(FontWriter.set1cNumber(privateDict.length));
//        bos.write((byte)19);

        //defaultWidthX                 20
        bos.write(FontWriter.set1cNumber(defaultWidthX));
        bos.write((byte)20);

        //nominalWidthX                 21
        bos.write(FontWriter.set1cNumber(nominalWidthX));
        bos.write((byte)21);

//        bos.write(FontWriter.set1cNumber(-24));
//        bos.write(FontWriter.set1cNumber(24));
//        bos.write(FontWriter.set1cNumber(670));
//        bos.write(FontWriter.set1cNumber(17));
//        bos.write(FontWriter.set1cNumber(-189));
//        bos.write(FontWriter.set1cNumber(13));
//        bos.write((byte)6);			//BlueValues
//
//        bos.write(FontWriter.set1cNumber(326));
//        bos.write(FontWriter.set1cNumber(5));
//        bos.write(FontWriter.set1cNumber(-512));
//        bos.write(FontWriter.set1cNumber(2));
//        bos.write((byte)7);			//OtherBlues
//
//        bos.write(FontWriter.set1cNumber(1));
//        bos.write(new byte[]{12,10});   //BlueShift
//
//        bos.write(FontWriter.set1cNumber(63));
//        bos.write((byte)10);			//StdHW
//
//        bos.write(FontWriter.set1cNumber(11));
//        bos.write(FontWriter.set1cNumber(44));
//        bos.write(FontWriter.set1cNumber(2));
//		bos.write(FontWriter.set1cNumber(2));
//		bos.write(FontWriter.set1cNumber(4));
//		bos.write(FontWriter.set1cNumber(5));
//		bos.write(FontWriter.set1cNumber(42));
//		bos.write(FontWriter.set1cNumber(9));
//		bos.write(FontWriter.set1cNumber(18));
//		bos.write(FontWriter.set1cNumber(14));
//    	bos.write(FontWriter.set1cNumber(123));
//		bos.write(FontWriter.set1cNumber(4));
//        bos.write(new byte[]{12,12});  		//StemSnapH
//
//        bos.write(FontWriter.set1cNumber(74));
//        bos.write((byte)11);			//StdVW
//
//		bos.write(FontWriter.set1cNumber(70));
//		bos.write(FontWriter.set1cNumber(4));
//		bos.write(FontWriter.set1cNumber(6));
//		bos.write(FontWriter.set1cNumber(12));
//        bos.write(new byte[]{12,13});		//StemSnapV

        return bos.toByteArray();
    }

    /**
     * Calculate the offSize required to encode a value
     * @param i Max value to encode
     * @return Number of bytes required
     */
    private static byte getOffsizeForMaxVal(int i) {
        byte result = 1;
        while (i > 256) {
            result++;
            i = i / 256;
        }

        return result;
    }

    public int getIntValue(int i) {
        return -1;
    }

    /**
     * Return a list of the names of the glyphs in this font.
     * @return List of glyph names
     */
    public String[] getGlyphList() {
        return glyphNames;
    }

    /**
     * Return the widths of all of the glyphs of this font.
     * @return List of glyph widths
     */
    public int[] getWidths() {
        int[] widths = new int[widthX.length];

        for (int i=0; i<widthX.length; i++) {
            if (widthX[i] == Integer.MIN_VALUE) {
                widths[i] = defaultWidthX;
            } else {
                widths[i] = widthX[i] + nominalWidthX;
            }
        }

        return widths;
    }

    /**
     * Return the left side bearings of all of the glyphs of this font.
     * @return List of left side bearings.
     */
    public int[] getBearings() {
        return lsbX;
    }

    /**
     * Return a bounding box calculated from the outlines.
     * @return the calculated bbox
     */
    public float[] getBBox() {
        return bbox;
    }

    /**
     * Return the size of the em square calculated from the outlines.
     * @return the calculated em square size
     */
    public double getEmSquareSize() {
        return emSquareSize;
    }

    private class CharstringElement {
        private boolean isCommand=true;
        private String commandName;
        private int numberValue;
        private int length=1;
        private ArrayList<CharstringElement> args = new ArrayList<CharstringElement>();
        final private boolean isResult;
        private CharstringElement parent;

        /**
         * Constructor used for generating an integer parameter.
         * @param number The number this element should represent
         */
        public CharstringElement(int number) {
            isResult = false;
            isCommand = false;
            numberValue = number;
        }

        /**
         * Constructor used for generating placeholder result elements.
         * @param parent The element this is a result of
         */
        public CharstringElement(CharstringElement parent) {
            isResult = true;
            isCommand = false;
            this.parent = parent;
            currentCharString.add(this);
        }

        /**
         * Normal constructor used when converting from Type 1 stream.
         * @param charstring byte array to copy from
         * @param pos starting position for this element
         */
        public CharstringElement(int[] charstring, int pos) {
            isResult = false;
            currentCharString.add(this);

            int b = charstring[pos];

            if (b >= 32 && b <= 246) {                                      //Single byte number

                numberValue = b - 139;
                isCommand = false;

            } else if((b >= 247 && b <= 250) || (b >= 251 && b <= 254)) {   //Two byte number

                if (b < 251) {
                    numberValue = ((b - 247) * 256) + charstring[pos+1] + 108;
                } else {
                    numberValue = -((b - 251) * 256) - charstring[pos+1] - 108;
                }

                isCommand = false;
                length = 2;

            } else {

                boolean mergePrevious=false;

                switch (b) {
                    case 1:             //hstem
                        commandName = "hstem";
                        claimArguments(2, true, true);
                        break;
                    case 3:             //vstem
                        commandName = "vstem";
                        claimArguments(2, true, true);
                        break;
                    case 4:             //vmoveto
                        commandName = "vmoveto";
                        claimArguments(1, true, true);

                        //If in a flex section channel arg and 0 into current flex command
                        if (inFlex) {
                            //If second pair found add to the first and store back
                            if (currentFlexCommand.args.size()==2 && !firstArgsAdded) {
                                int arg0 = currentFlexCommand.args.get(0).numberValue;
                                int arg1 = args.get(0).numberValue + currentFlexCommand.args.get(1).numberValue;
                                currentFlexCommand.args.clear();
                                currentFlexCommand.args.add(new CharstringElement(arg0));
                                currentFlexCommand.args.add(new CharstringElement(arg1));
                                firstArgsAdded = true;
                            } else {
                                currentFlexCommand.args.add(new CharstringElement(0));
                                currentFlexCommand.args.add(args.get(0));
                            }
                            commandName = "";
                        }
                        break;
                    case 5:             //rlineto
                        commandName = "rlineto";
                        claimArguments(2, true, true);
                        mergePrevious=true;
                        break;
                    case 6:             //hlineto
                        commandName = "hlineto";
                        claimArguments(1, true, true);
                        break;
                    case 7:             //vlineto
                        commandName = "vlineto";
                        claimArguments(1, true, true);
                        break;
                    case 8:             //rrcurveto
                        commandName = "rrcurveto";
                        claimArguments(6, true, true);
                        mergePrevious=true;
                        break;
                    case 9:             //closepath
                        commandName = "closepath";
                        claimArguments(0, false, true);
                        break;
                    case 10:            //callsubr
                        commandName = "callsubr";
                        claimArguments(1, false, false);

                        int subrNumber = args.get(0).numberValue;

                        //Handle starting a section of flex code
                        if (!inFlex && subrNumber == 1) {
                            //Repurpose this as flex command and set flag for processing following commands
                            args.clear();
                            commandName = "flex";
                            currentFlexCommand = this;
                            inFlex = true;
                        }

                        //Handle subr calls during flex sections
                        if (inFlex && subrNumber >= 0 && subrNumber <= 2) {

                            //Handle endind flex section
                            if (subrNumber == 0) {
                                claimArguments(3, false, false);
                                if (args.size() >= 4) {
                                    currentFlexCommand.args.add(args.get(3));
                                } else {
                                    currentFlexCommand.args.add(new CharstringElement(0));
                                }
                                inFlex = false;
                                firstArgsAdded = false;
                            }

                        //Handle other cases
                        } else {

                            byte[] rawSubr = subrs[subrNumber];

                            //Deal with top byte being negative
                            int[] subr = new int[rawSubr.length];
                            for (int i=0; i<rawSubr.length; i++) {
                                subr[i] = rawSubr[i];
                                if (subr[i] < 0) {
                                    subr[i] += 256;
                                }
                            }

                            //Convert to CharstringElements
                            CharstringElement element;
                            for (int i=0; i < subr.length; i+=element.length) {
                                element = new CharstringElement(subr, i);
                            }
                        }
                        break;
                    case 11:            //return
                        commandName = "return";
                        break;
                    case 12:            //2 byte command
                        length = 2;
                        switch(charstring[pos+1]) {
                            case 0:     //dotsection
                                commandName = "dotsection";
                                claimArguments(0, false, true);
                                break;
                            case 1:     //vstem3
                                commandName = "vstem3";
                                claimArguments(6, true, true);
                                break;
                            case 2:     //hstem3
                                commandName = "hstem3";
                                claimArguments(6, true, true);
                                break;
                            case 6:     //seac
                                commandName = "seac";
                                claimArguments(5, true, true);
                                break;
                            case 7:     //sbw
                                commandName = "sbw";
                                claimArguments(4, true, true);
                                lsbX[currentCharStringID] = args.get(0).evaluate();
                                lsbY[currentCharStringID] = args.get(1).evaluate();
                                widthX[currentCharStringID] = args.get(2).evaluate();
                                widthY[currentCharStringID] = args.get(3).evaluate();

                                //repurpose as rmoveto
                                if (lsbX[currentCharStringID] != 0) {
                                    commandName = "rmoveto";
                                    args.clear();
                                    args.add(new CharstringElement(lsbX[currentCharStringID]));
                                    args.add(new CharstringElement(lsbY[currentCharStringID]));
                                }
                                break;
                            case 12:    //div
                                commandName = "div";
                                claimArguments(2, false, false);
                                new CharstringElement(this);
                                break;
                            case 16:    //callothersubr
                                commandName = "callothersubr";
                                claimArguments(2, false, false);
                                if (args.size() > 1) {
                                    int count = args.get(1).numberValue;
                                    boolean foundEnough = claimArguments(count, false, false);

                                    if (!foundEnough) {
                                        currentCharString.remove(this);
                                        return;
                                    }

                                    //Place arguments back on stack
                                    for (int i=0; i<count; i++) {
                                        new CharstringElement(args.get((1+count)-i).numberValue);
                                    }
                                }
                                break;
                            case 17:    //pop
                                commandName = "pop";
                                new CharstringElement(this);
                                break;
                            case 33:    //setcurrentpoint
                                commandName = "setcurrentpoint";
                                claimArguments(2, true, true);
                                break;
                            default:
                        }
                        break;
                    case 13:            //hsbw
                        commandName = "hsbw";
                        claimArguments(2, true, true);
                        lsbX[currentCharStringID] = args.get(0).evaluate();
                        widthX[currentCharStringID] = args.get(1).evaluate();

                        //repurpose as rmoveto
                        if (lsbX[currentCharStringID] != 0) {
                            commandName = "rmoveto";
                            args.set(1, new CharstringElement(0));
                        }
                        break;
                    case 14:            //endchar
                        commandName = "endchar";
                        claimArguments(0, false, true);
                        break;
                    case 21:            //rmoveto
                        commandName = "rmoveto";
                        claimArguments(2, true, true);

                        //If in a flex section channel args into current flex command
                        if (inFlex) {
                            //If second pair found add to the first and store back
                            if (currentFlexCommand.args.size()==2 && !firstArgsAdded) {
                                int arg0 = args.get(0).numberValue + currentFlexCommand.args.get(0).numberValue;
                                int arg1 = args.get(1).numberValue + currentFlexCommand.args.get(1).numberValue;
                                currentFlexCommand.args.clear();
                                currentFlexCommand.args.add(new CharstringElement(arg0));
                                currentFlexCommand.args.add(new CharstringElement(arg1));
                                firstArgsAdded = true;
                            } else {
                                currentFlexCommand.args.add(args.get(0));
                                currentFlexCommand.args.add(args.get(1));
                            }
                            commandName = "";
                        }
                        break;
                    case 22:            //hmoveto
                        commandName = "hmoveto";
                        claimArguments(1, true, true);

                        //If in a flex section channel arg and 0 into current flex command
                        if (inFlex) {
                            //If second pair found add to the first and store back
                            if (currentFlexCommand.args.size()==2 && !firstArgsAdded) {
                                int arg0 = args.get(0).numberValue + currentFlexCommand.args.get(0).numberValue;
                                int arg1 = currentFlexCommand.args.get(1).numberValue;
                                currentFlexCommand.args.clear();
                                currentFlexCommand.args.add(new CharstringElement(arg0));
                                currentFlexCommand.args.add(new CharstringElement(arg1));
                                firstArgsAdded = true;
                            } else {
                                currentFlexCommand.args.add(args.get(0));
                                currentFlexCommand.args.add(new CharstringElement(0));
                            }
                            commandName = "";
                        }
                        break;
                    case 30:            //vhcurveto
                        commandName = "vhcurveto";
                        claimArguments(4, true, true);
                        break;
                    case 31:            //hvcurveto
                        commandName = "hvcurveto";
                        claimArguments(4, true, true);
                        break;
                    case 255:           //5 byte number
                        length = 5;
                        isCommand = false;
                        numberValue = (charstring[pos+4] & 0xFF) +
                                ((charstring[pos+3] & 0xFF) << 8) +
                                ((charstring[pos+2] & 0xFF) << 16) +
                                ((charstring[pos+1] & 0xFF) << 24);

                        break;
                    default:
                }

                if (mergePrevious) {
                    CharstringElement previous = currentCharString.get(currentCharString.indexOf(this)-1);
                    if (commandName.equals(previous.commandName) && previous.args.size()<=(39-args.size())) {
                        currentCharString.remove(previous);
                        for (CharstringElement e : args) {
                            previous.args.add(e);
                        }
                        args = previous.args;
                    }
                }
            }
        }

        /**
         * Evaluate the numerical value of this element. This is used for hsbw and sbw where the value is being funneled
         * into a data structure rather than remaining in the converted charstring.
         * @return The numerical value of the element.
         */
        private int evaluate() {
            if (isResult) {
                return parent.evaluate();
            } else if (isCommand) {
                if ("div".equals(commandName)) {
                    return (args.get(1).evaluate() / args.get(0).evaluate());
                } else {
                }
            }

            return numberValue;
        }

        /**
         * @return The number of bytes used in the original stream for just this element (not it's arguments).
         */
        public int getLength() {
            return length;
        }


        /**
         * Get the displacement created by this CharstringElement.
         * @return An int array pair of values for the horizontal and vertical displacement values.
         */
        public int[] getDisplacement() {

            if (!isCommand) {
                return new int[]{0, 0};
            }

            if ("hstem".equals(commandName)) {
            } else if ("vstem".equals(commandName)) {
            } else if ("vmoveto".equals(commandName)) {
                return new int[]{0, args.get(0).evaluate()};
            } else if ("rlineto".equals(commandName)) {
                int dx = 0;
                int dy = 0;
                for (int i=0; i<args.size()/2; i++) {
                    dx += args.get(i*2).evaluate();
                    dy += args.get(1+(i*2)).evaluate();
                }
                return new int[]{dx, dy};
            } else if ("hlineto".equals(commandName)) {
                return new int[]{args.get(0).evaluate(), 0};
            } else if ("vlineto".equals(commandName)) {
                return new int[]{0, args.get(0).evaluate()};
            } else if ("rrcurveto".equals(commandName)) {
                int dx = 0;
                int dy = 0;
                for (int i=0; i<args.size()/2; i++) {
                    dx += args.get(i*2).evaluate();
                    dy += args.get(1+(i*2)).evaluate();
                }
                return new int[]{dx, dy};
            } else if ("closepath".equals(commandName)) {
            } else if ("callsubr".equals(commandName)) {
            } else if ("return".equals(commandName)) {
            } else if ("dotsection".equals(commandName)) {
            } else if ("vstem3".equals(commandName)) {
            } else if ("hstem3".equals(commandName)) {
            } else if ("seac".equals(commandName)) {
                //Hopefully won't have to implement this...
            } else if ("sbw".equals(commandName)) {
            } else if ("div".equals(commandName)) {
            } else if ("callothersubr".equals(commandName)) {
            } else if ("pop".equals(commandName)) {
            } else if ("setcurrentpoint".equals(commandName)) {
            } else if ("hsbw".equals(commandName)) {
            } else if ("endchar".equals(commandName)) {
            } else if ("rmoveto".equals(commandName)) {
                return new int[]{args.get(0).evaluate(), args.get(1).evaluate()};
            } else if ("hmoveto".equals(commandName)) {
                return new int[]{args.get(0).evaluate(), 0};
            } else if ("vhcurveto".equals(commandName)) {
                return new int[]{args.get(1).evaluate()+args.get(3).evaluate(), args.get(0).evaluate()+args.get(2).evaluate()};
            } else if ("hvcurveto".equals(commandName)) {
                return new int[]{args.get(0).evaluate()+args.get(1).evaluate(), args.get(2).evaluate()+args.get(3).evaluate()};
            } else if ("flex".equals(commandName)) {
                int dx = 0;
                int dy = 0;
                for (int i=0; i<6; i++) {
                    dx += args.get(i*2).evaluate();
                    dy += args.get(1+(i*2)).evaluate();
                }
                return new int[]{dx, dy};
            } else if (commandName.length()==0) {
                return new int[]{0,0};
            } else {
            }

            return new int[]{0,0};
        }

        /**
         * Scale this element according to a precalculated scale value. Works recursively.
         */
        public void scale() {

            //If result, ignore
            if (isResult) {
                return;
            }

            //If number, scale it
            if (!isCommand) {
                numberValue = (int)(numberValue*scale);
                return;
            }

            //Check how to handle args if command
            boolean scaleAll=false;
            if ("hstem".equals(commandName)) {
                scaleAll = true;
            } else if ("vstem".equals(commandName)) {
                scaleAll = true;
            } else if ("vmoveto".equals(commandName)) {
                scaleAll = true;
            } else if ("rlineto".equals(commandName)) {
                scaleAll = true;
            } else if ("hlineto".equals(commandName)) {
                scaleAll = true;
            } else if ("vlineto".equals(commandName)) {
                scaleAll = true;
            } else if ("rrcurveto".equals(commandName)) {
                scaleAll = true;
            } else if ("closepath".equals(commandName)) {
            } else if ("callsubr".equals(commandName)) {
            } else if ("return".equals(commandName)) {
            } else if ("dotsection".equals(commandName)) {
            } else if ("vstem3".equals(commandName)) {
                scaleAll = true;
            } else if ("hstem3".equals(commandName)) {
                scaleAll = true;
            } else if ("seac".equals(commandName)) {
                for (int i=0; i<3; i++) {
                    args.get(i).scale();
                }
            } else if ("sbw".equals(commandName)) {
            } else if ("div".equals(commandName)) {
                scaleAll = true;
            } else if ("callothersubr".equals(commandName)) {
            } else if ("pop".equals(commandName)) {
            } else if ("setcurrentpoint".equals(commandName)) {
                scaleAll = true;
            } else if ("hsbw".equals(commandName)) {
            } else if ("endchar".equals(commandName)) {
            } else if ("rmoveto".equals(commandName)) {
                scaleAll = true;
            } else if ("hmoveto".equals(commandName)) {
                scaleAll = true;
            } else if ("vhcurveto".equals(commandName)) {
                scaleAll = true;
            } else if ("hvcurveto".equals(commandName)) {
                scaleAll = true;
            } else if ("flex".equals(commandName)) {
                scaleAll = true;
            } else if (commandName.length()==0) {
            } else {
            }

            if (scaleAll) {
                for (CharstringElement e : args) {
                    e.scale();
                }
            }
        }

        /**
         * Return the type 2 bytes required to match the effect of the instruction and it's arguments.
         * @return the type 2 bytes required to match the effect of the instruction and it's arguments
         */
        public byte[] getType2Bytes() {

            if (!isCommand) {

                if (isResult) {
                    return new byte[]{};
                }

                return FontWriter.setCharstringType2Number(numberValue);
            }

            boolean noChange=false;
            byte[] commandNumber=new byte[]{};

            if ("hstem".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{1};
            } else if ("vstem".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{3};
            } else if ("vmoveto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{4};
            } else if ("rlineto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{5};
            } else if ("hlineto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{6};
            } else if ("vlineto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{7};
            } else if ("rrcurveto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{8};
            } else if ("closepath".equals(commandName)) {
                //Remove moveto automatically closes paths in Type 2
                return new byte[]{};
            } else if ("callsubr".equals(commandName)) {
                return new byte[]{};

            } else if ("return".equals(commandName)) {
//                    noChange=true;
//                    commandNumber=new byte[]{11};
                //Unsupported othersubrs
                return new byte[]{};
            } else if ("dotsection".equals(commandName)) {
                //Deprecated - remove
                return new byte[]{};
            } else if ("vstem3".equals(commandName)) {

            } else if ("hstem3".equals(commandName)) {

            } else if ("seac".equals(commandName)) {    //Create accented character by merging specified charstrings

                //Get args
//                int asb = args.get(0).numberValue;
                int adx = args.get(1).numberValue;
                int ady = args.get(2).numberValue;
                int bchar = args.get(3).numberValue;
                int achar = args.get(4).numberValue;

                //Look up character code for specified location in standard encoding
                int aCharUnicode = (int)StandardFonts.getEncodedChar(StandardFonts.STD, achar).charAt(0);
                int bCharUnicode = (int)StandardFonts.getEncodedChar(StandardFonts.STD, bchar).charAt(0);
                int accentIndex = -1;
                int baseIndex = -1;

                //Run through glyph names comparing character codes to those for the accent and base to find glyph indices
                for (int i=0; i<glyphNames.length; i++) {
                    int adobePos = StandardFonts.getAdobeMap(glyphNames[i]);
                    if (adobePos >= 0 && adobePos < 512) {

                        if (adobePos == aCharUnicode) {
                            accentIndex = i;
                        }
                        if (adobePos == bCharUnicode) {
                            baseIndex = i;
                        }
                    }
                }

                //Check both glyphs found
                if (accentIndex == -1 || baseIndex == -1) {
                    return new byte[]{};
                }

                //Merge glyphs
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    int charstringStore = currentCharStringID;

                    //Fetch base charstring, convert, and remove endchar command
                    charstringXDisplacement[baseIndex] = 0;
                    charstringYDisplacement[baseIndex] = 0;
                    inSeac = true;
                    byte[] rawBaseCharstring = convertCharstring(charstrings[baseIndex], baseIndex);
                    inSeac = false;
                    currentCharStringID = charstringStore;
                    byte[] baseCharstring = new byte[rawBaseCharstring.length-1];
                    System.arraycopy(rawBaseCharstring, 0, baseCharstring, 0, baseCharstring.length);
                    bos.write(baseCharstring);

                    //Move to the origin plus the offset
                    bos.write(FontWriter.setCharstringType2Number(-(charstringXDisplacement[baseIndex]) + adx));
                    bos.write(FontWriter.setCharstringType2Number(-(charstringYDisplacement[baseIndex]) + ady));
                    bos.write((byte)21);

                    //Fetch accent charstring and convert
                    charstringXDisplacement[accentIndex] = 0;
                    charstringYDisplacement[accentIndex] = 0;
                    byte[] accentCharstring = convertCharstring(charstrings[accentIndex], accentIndex);
                    currentCharStringID = charstringStore;
                    bos.write(accentCharstring);

                    return bos.toByteArray();
                } catch (IOException e) {
                    //tell user and log
                    if(LogWriter.isOutput())
                        LogWriter.writeLog("Exception: "+e.getMessage());
                }

            } else if ("sbw".equals(commandName)) {
                //Might need to moveto arg coordinates?
                return new byte[]{};

            } else if ("div".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{12,12};

            } else if ("callothersubr".equals(commandName)) {

            } else if ("pop".equals(commandName)) {

            } else if ("setcurrentpoint".equals(commandName)) {

            } else if ("hsbw".equals(commandName)) {
                //Might need to moveto arg coordinates?
                return new byte[]{};

            } else if ("endchar".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{14};
            } else if ("rmoveto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{21};
            } else if ("hmoveto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{22};
            } else if ("vhcurveto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{30};
            } else if ("hvcurveto".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{31};
            } else if ("flex".equals(commandName)) {
                noChange=true;
                commandNumber=new byte[]{12,35};
            } else if (commandName.length()==0) {
                return new byte[]{};
            } else {
            }

            if (noChange) {
                //No change - return args and command
                ByteArrayOutputStream bos = getStreamWithArgs();
                try {
                    bos.write(commandNumber);
                } catch (IOException e) {
                    //tell user and log
                    if(LogWriter.isOutput())
                        LogWriter.writeLog("Exception: "+e.getMessage());
                }
                return bos.toByteArray();
            }


            return new byte[]{};
        }

        private ByteArrayOutputStream getStreamWithArgs() {
            ByteArrayOutputStream result = new ByteArrayOutputStream();

            try {
                for (CharstringElement arg : args) {
                    result.write(arg.getType2Bytes());
                }
            } catch(IOException e) {
                //tell user and log
                if(LogWriter.isOutput())
                    LogWriter.writeLog("Exception: "+e.getMessage());
            }

            return result;
        }

        /**
         * Return a representation of the element as a string.
         * @return Element as string
         */
        public String toString() {
            if (isCommand) {
                return commandName + args.toString();
            }

            if (isResult) {
                return "result of "+parent;
            }

            return String.valueOf(numberValue);
        }

        private void printStack() {
            System.out.println("Stack bottom");
            for (CharstringElement e : currentCharString) {
                if (!e.isCommand) {
                    System.out.println(e);
                }
            }
            System.out.println("Stack top");
        }

        /**
         * Removes arguments from the stack (in other words, numbers and results from the instruction stream) and places
         * them in this element's argument list.
         * @param count The number of arguments to take
         * @param takeFromBottom Where to take the arguments from
         * @param clearStack Whether to clear the stack after
         * @return whether enough arguments were found
         */
        private boolean claimArguments(int count, boolean takeFromBottom, boolean clearStack) {

            if (count > 0) {
                int currentIndex = currentCharString.indexOf(this);
                if (currentIndex == -1) {
                    throw new RuntimeException("Not in list!");
                }

                int argsFound = 0;
                boolean failed = false;
                while (argsFound < count && !failed) {

                    boolean found = false;
                    if (takeFromBottom) {
                        int pos=0;
                        while (!found && pos <= currentIndex) {
                            CharstringElement e = currentCharString.get(pos);
                            if (!e.isCommand) {
                                argsFound++;
                                args.add(e);
                                currentCharString.remove(e);
                                found = true;
                            }
                            pos++;
                        }
                    } else {
                        int pos = currentIndex;
                        while (!found && pos >= 0) {
                            CharstringElement e = currentCharString.get(pos);
                            if (!e.isCommand) {
                                argsFound++;
                                args.add(e);
                                currentCharString.remove(e);
                                found = true;
                                currentIndex--;
                            }
                            pos--;
                        }
                    }
                    if (!found) {
                        failed = true;
                    }
                }

                if (argsFound < count) {
//                    System.out.println("Not enough arguments! ("+argsFound+" of "+count+") "+ (currentCharStringID > charstrings.length ? "subr "+(currentCharStringID-charstrings.length) : "charstring "+currentCharStringID));
//                    throw new RuntimeException("Not enough arguments!");
                    return false;
                }
            }

            if (clearStack) {
                for (int i=0; i<currentCharString.size(); i++) {
                    CharstringElement e = currentCharString.get(i);
                    if (!e.isCommand) {
                        currentCharString.remove(e);
                    }
                }
            }

            return true;

        }
    }


}
