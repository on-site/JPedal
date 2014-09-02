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
 * CMAPWriter.java
 * ---------------
 */
package org.jpedal.fonts.tt.conversion;

import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.fonts.tt.*;
import org.jpedal.utils.Sorts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class
        CMAPWriter extends CMAP implements FontTableWriter{

    //set in code for use in OS2 table
    int minCharCode=65536;
    int maxCharCode=0;

    String fontName;
    private PdfFont originalFont;

    public CMAPWriter(FontFile2 currentFontFile, int startPointer, Glyf currentGlyf) {
        super(currentFontFile, startPointer, currentGlyf);

    }
    
    /**
     *Creates format 0 subtable for true type fonts [only]
     *@param PdfJavaGlyphs
     *@return length of format 0 table [hardcoded to 262] 
     */
    private int createFormat0MapForTT(PdfJavaGlyphs glyphs){
    	TTGlyphs ttGlyphs = (TTGlyphs)glyphs;    	
    	CMAP currentCmap = (CMAP) ttGlyphs.getTable(FontFile2.CMAP);
    	Map glyphMap = null;
    	if(currentCmap==null){
    		glyphMap = new HashMap<Integer,Integer>();
    		if(originalFont.getToUnicode()==null){
    			int gCount = glyphs.getGlyphCount();
    			for(int z=0;z<gCount;z++){
    				glyphMap.put(z,z);
    			}    		
    		}
    		else{
    			for(int z=0;z<65536;z++){
    				if(originalFont.getUnicodeMapping(z)!=null){
    					String str = originalFont.getUnicodeMapping(z);
    					int adjValue = getAdjustedUniValue(str);
    					if(adjValue>=0){
    						glyphMap.put(adjValue,z);
    					}
    				}
    			}
    		}
    	}
    	else{
    		glyphMap = currentCmap.buildCharStringTable();
    	}    	

    	for(int z=0;z<256;z++){    		
    		glyphIndexToChar[1][z] = glyphMap.get(z)!=null?(Integer)glyphMap.get(z):0;
    	}    	
    	return 262;
    }

    //this method should be combined to createFormat4Map subroutine in future
    //@see createFormat4Map
    private int createFormat4MapForTT(PdfJavaGlyphs glyphs){
    	TTGlyphs ttGlyphs = (TTGlyphs)glyphs;    	
    	CMAP currentCmap = (CMAP) ttGlyphs.getTable(FontFile2.CMAP);
    	Map glyphMap = null;
    	if(currentCmap==null){
    		glyphMap = new HashMap<Integer,Integer>();
    		if(originalFont.getToUnicode()==null){
    			int gCount = glyphs.getGlyphCount();
    			for(int z=0;z<gCount;z++){
    				glyphMap.put(z,z);
    			}    		
    		}
    		else{    			
    			for(int z=0;z<65536;z++){
    				if(originalFont.getUnicodeMapping(z)!=null){
    					String str = originalFont.getUnicodeMapping(z);
    					int adjValue = getAdjustedUniValue(str);
    					if(adjValue>=0){
    						glyphMap.put(adjValue,z);
    					}
    					//System.out.println(z+" "+originalFont.getUnicodeMapping(z)+" "+adjValue);    	
    				}
    			}


                //If there's no space character, find one
                if (glyphMap.get(0x20) == null) {
                    Object[] keys = ttGlyphs.getCharStrings().keySet().toArray();
                    Hmtx hmtx = (Hmtx)ttGlyphs.getTable(FontFile2.HMTX);
                    int currentGlyph = 0;

                    //Ensure glyph is not already assigned and has a nonzero width
                    for (Object key : keys) {
                        while (currentGlyph < originalFont.getGlyphData().getGlyphCount() &&
                                hmtx.getUnscaledWidth(currentGlyph) == 0) {
                            currentGlyph++;
                        }

                        if (currentGlyph == (Integer)key) {
                            currentGlyph++;
                        }
                    }

                    //Place the glyph at 0x20 (unicode space)
                    glyphMap.put(0x20, currentGlyph);
                }
    		}
    	}else{
    		glyphMap = currentCmap.buildCharStringTable();
    	}    	

    	int segCount = 0;			

    	HashMap<Integer,Integer> uniToRawMap = new HashMap<Integer,Integer>();
    	HashMap<Integer,Integer> uniToCharCodeMap = new HashMap<Integer,Integer>();
    	HashMap<Integer,Integer> pointerCodeMap = new HashMap<Integer,Integer>();//holds the byte array and relative pointers to access it in id range offset
    	ArrayList<Integer> pointerList = new ArrayList<Integer>();//to calculate the bytearray to be inserted after id range offset

    	for (Object key : glyphMap.keySet()) {
    		int k = (Integer) key;
    		int v = (Integer) glyphMap.get(k);
    		int uniValue;
    		if(currentCmap == null || currentCmap.hasFormat4()){
    			uniValue = k;
    		}else{
    			uniValue = (originalFont.getUnicodeMapping(k)!=null)?originalFont.getUnicodeMapping(k).charAt(0):k;  
        	}    		
    		uniToRawMap.put(uniValue, k);
    		uniToCharCodeMap.put(uniValue,v);
    		// System.out.println(k+" == "+uniValue+" == >"+v);
    	}

    	Object[] uniToRawMapKeys = uniToRawMap.keySet().toArray();
    	Arrays.sort(uniToRawMapKeys); 

    	segCount = uniToRawMap.size()+1;

    	endCode = new int[segCount];
    	endCode[segCount-1] = 0xffff;
    	for(int z=0;z<segCount-1;z++){
    		endCode[z] = (Integer)uniToRawMapKeys[z];
    	}
    	startCode = new int[segCount];
    	startCode[segCount-1] = 0xffff;
    	for(int z=0;z<segCount-1;z++){
    		startCode[z] = (Integer)uniToRawMapKeys[z];
    	}
    	idDelta = new int[segCount];
    	idDelta[segCount-1] = 1;
    	for(int z=0;z<segCount-1;z++){ 
    		idDelta[z] = uniToCharCodeMap.get(startCode[z]) - startCode[z];	
    		if(idDelta[z]>=0){//according to spec id delta should be zero if startcode is less than character code
    			idDelta[z]=0;
    			pointerCodeMap.put(z,2*(segCount-(z))+(2*pointerList.size()));
    			pointerList.add(uniToCharCodeMap.get(startCode[z]));
    		}
    	}  
    	idRangeOffset = new int[segCount];
    	idRangeOffset[segCount-1] = 0;
    	for(int z=0;z<segCount-1;z++){
    		idRangeOffset[z] = pointerCodeMap.get(z)!=null ? pointerCodeMap.get(z):0;
    	}

    	int segX2  = segCount * 2;
    	int searchRange = (int) (2 * (Math.pow(2,Math.floor(Math.log(segCount)/Math.log(2)))));
    	int entrySelector = (int) (Math.log(searchRange/2)/Math.log(2));
    	int rangeShift = 2 * segCount - searchRange;

    	CMAPreserved = new int[]{0,0,0};
    	CMAPsegCount = new int[]{segX2,0,segX2};
    	CMAPsearchRange = new int[]{searchRange, 0, searchRange};
    	CMAPentrySelector = new int[]{entrySelector,0,entrySelector};
    	CMAPrangeShift = new int[]{rangeShift,0,rangeShift};

    	if(pointerList.size()>0){
    		glyphIdArray = new int[pointerList.size()];
    		for(int z=0;z<glyphIdArray.length;z++){
    			glyphIdArray[z] = pointerList.get(z);
    		}
    	}

    	return 16 + (segCount * 8)+(pointerList.size()*2) ;

    }

    private int createFormat4Map(PdfJavaGlyphs glyphs, boolean is1C, String[] glyphList) {

        //create array and fill with dummy values
        int[] unicodeToGlyph = new int[65536];
        for (int i=0; i<unicodeToGlyph.length;i++) {
            unicodeToGlyph[i]=Integer.MAX_VALUE;
        }

	/**
	 * get mapped values to rebuild table using data we extracted from fonts
	 */
        if(originalFont.getFontType()==StandardFonts.TRUETYPE){ //TTF 

            int count=glyphList.length;
            int ptr;
            for(int ii=0;ii<count;ii++){
                if(glyphList[ii]!=null){
                    ptr=Integer.parseInt(glyphList[ii]);
		    if(ptr>0){
			unicodeToGlyph[ii]=ptr;
		    }
		}
            }
        }else{ //PS version
            getNonTTGlyphData(glyphs, is1C, glyphList, unicodeToGlyph);
        }

        //Find ranges of unicode characters with valid glyphs
        int[] rangeStart = new int[40000];
        int[] rangeEnd = new int[40000];
        int segCount = 0;
        boolean inRange = false;
        for (int i=0; i<65536; i++) {
            if (inRange && unicodeToGlyph[i] == Integer.MAX_VALUE) {
                inRange = false;
                rangeEnd[segCount] = i-1;
                segCount++;
            } else if (!inRange && unicodeToGlyph[i] != Integer.MAX_VALUE) {
                inRange = true;
                rangeStart[segCount] = i;
            }
        }

        //Handle unicode replacement character
        if (unicodeToGlyph[0xFFFD] == Integer.MAX_VALUE) {
            rangeStart[segCount] = 0xFFFD;
            rangeEnd[segCount] = 0xFFFD;
            unicodeToGlyph[0xFFFD] = 1;
            segCount++;
        }

        //Deal with end character
        if (unicodeToGlyph[0xFFFF] == Integer.MAX_VALUE) {
            rangeStart[segCount] = 0xFFFF;
            rangeEnd[segCount] = 0xFFFF;
            unicodeToGlyph[0xFFFF] = 1;
            segCount++;
        }

        //Initialise values
        int segX2 = segCount*2;
        CMAPsegCount = new int[]{segX2,0,segX2};

        int searchRange = 1;
        while (searchRange*2 <= segCount)
            searchRange *= 2;
        searchRange *= 2;
        CMAPsearchRange = new int[]{searchRange, 0, searchRange};

        int entrySelector = 0;
        int working = searchRange/2;
        while (working > 1) {
            working /= 2;
            entrySelector++;
        }
        CMAPentrySelector = new int[]{entrySelector,0,entrySelector};

        int rangeShift = segX2 - searchRange;
        CMAPrangeShift = new int[]{rangeShift,0,rangeShift};

        endCode = rangeEnd;

        CMAPreserved = new int[]{0,0,0};

        startCode = rangeStart;

        idRangeOffset = new int[segCount];

        //Check ranges are sequential in key and value and flag if not
        int glyphIdArrayLength=0;
        for (int i=0; i<segCount; i++) {
            int diff = unicodeToGlyph[rangeStart[i]] - rangeStart[i];
            for (int j=rangeStart[i]+1; j <= rangeEnd[i]; j++) {
                if (unicodeToGlyph[j] - j != diff) {
                    idRangeOffset[i] = -1;
                    glyphIdArrayLength += (rangeEnd[i]+1) - rangeStart[i];
                    break;
                }
            }
        }

        //Deal with non-zero idRangeOffset values! (flagged as -1)
        //WARNING! This is currently untested so results may vary!
        glyphIdArray = new int[glyphIdArrayLength];

        //Calculate deltas
        idDelta = new int[segCount];

        //Go through segments for deltas or offsets
        int addressPointer = 16 + (segCount * 8);
        int arrayPointer = 0;
        for (int i=0; i<idRangeOffset.length; i++){
            if (idRangeOffset[i] == 0) {
                //Use a direct offset from the unicode values
                idDelta[i] = unicodeToGlyph[rangeStart[i]] - rangeStart[i] - 1;
            } else {
                //Use glyphIdArray
                idRangeOffset[i] = addressPointer - (16 + (segCount * 6) + (i * 2));

                for (int j=rangeStart[i]; j<=rangeEnd[i]; j++) {
                    glyphIdArray[arrayPointer] = unicodeToGlyph[j]-1;

                    addressPointer+=2;
                    arrayPointer++;
                }
            }
        }

        //Return table length
        return 16 + (segCount * 8) + (glyphIdArrayLength * 2);
    }

    private void getNonTTGlyphData(PdfJavaGlyphs glyphs, boolean is1C, String[] glyphList, int[] unicodeToGlyph) {
        //Get glyphs present and put in unicode array
        int cid=0;
        for (int i=0; i<glyphs.getGlyphCount()+1; i++) {
            String val=null;
            if (originalFont != null && originalFont.getGlyphData().isIdentity() && originalFont.hasToUnicode() && i>1) {
                val = originalFont.getUnicodeMapping(cid);
                while (val == null && cid < 0xd800) {
                    cid++;
                    val = originalFont.getUnicodeMapping(cid);
                }
                if (val != null) {
                    unicodeToGlyph[val.charAt(0)] = i;
                }

                cid++;
            } else {
                if (val==null && is1C) {
                    val = glyphs.getIndexForCharString(i);
                } else if (val==null && i < glyphList.length) {
                    val = glyphList[i];
                }

                if (val != null) {
                    int uc = StandardFonts.getIDForGlyphName(fontName, val);

                    if (uc >= 0 && uc < unicodeToGlyph.length) {
                        if (is1C)
                            unicodeToGlyph[uc] = i;
                        else
                            unicodeToGlyph[uc] = i+1;
                    }
                }
            }
        }
    }

    /**
     * used to turn Ps into OTF
     */
    public CMAPWriter(String fontName, PdfFont currentFontData, PdfFont originalFont, PdfJavaGlyphs glyphs, String[] glyphList) {

        this.fontName = fontName;

        this.originalFont = originalFont;

        /**make a list of glyfs from TT font*/
        //commented out because data manipulation is handled differently in TT fonts
        //@see createFormat4MapForTT in CMAPWriter and
        //@see buildCharStringTable() in CMAP
//        if(glyphList==null && originalFont.getFontType()==StandardFonts.TRUETYPE){
//            Map charStringsFoundInTTFont=originalFont.getGlyphData().getCharStrings();
//
//            if(charStringsFoundInTTFont!=null){
//                int size=charStringsFoundInTTFont.size();
//                glyphList=new String[size];
//                Iterator i=charStringsFoundInTTFont.keySet().iterator();
//                int ptr=0;
//                while(i.hasNext()){
//                    glyphList[ptr]=i.next().toString();
//                    ptr++;
//                }
//            }
//        }

        /**
         * initialise the 3 tables we will need for out fonts in browser
         */
        numberSubtables=3;
        CMAPformats=new int[]{4,0,4};
        glyphIndexToChar=new int[3][256];
        platformID=new int[]{0,1,3};
        platformSpecificID=new int[]{3,0,1};
        CMAPlang=new int[]{0,0,0};
        
        
        //Initialise format 4 fields
        int format4Length = 0 ;
        
        if(originalFont.getFontType() == StandardFonts.TRUETYPE){        	
        	format4Length = createFormat4MapForTT(glyphs);
        	createFormat0MapForTT(glyphs);
        }else{ // for cff font handling
        	format4Length = createFormat4Map(glyphs, currentFontData.is1C(), glyphList);        
        	int enc=StandardFonts.MAC;
            StandardFonts.checkLoaded(enc);
          
            //Get glyphs present and put in mac encoding array
            for (int i=0; i<glyphs.getGlyphCount()+1; i++) {
                String val=null;
                if (currentFontData.is1C())
                    val = glyphs.getIndexForCharString(i);
                else if (i < glyphList.length)
                    val = glyphList[i];

                if (val != null) {
                    int id = StandardFonts.lookupCharacterIndex(val, StandardFonts.MAC);

                    int glyph = i;
                    if (currentFontData.is1C()) {
                        glyph -= 1;
                        if (i == 1)
                            glyph = 1;
                    }
                    if (id >= 0 && id < 256)
                        glyphIndexToChar[1][id] = glyph;
                }
            }        	
        }
                
        CMAPlength=new int[]{format4Length,262,format4Length};
        CMAPsubtables=new int[]{28,28+(format4Length*2),28+format4Length};
        

    }


    public byte[] writeTable() throws IOException {

        ByteArrayOutputStream bos=new ByteArrayOutputStream();

        final boolean debug=false;

        if(debug)
                System.out.println("write CMAP "+this);

        //LogWriter.writeMethod("{readCMAPTable}", 0);

        //read 'cmap' table
        {

            int numberSubtables=this.numberSubtables;

            ArrayList tables=new ArrayList();
            for(int i=0;i<numberSubtables;i++){
                tables.add(i);
            }

            bos.write(TTFontWriter.setNextUint16(id));
            bos.write(TTFontWriter.setNextUint16(numberSubtables));

            for(int j=0;j<numberSubtables;j++){

                int i=(Integer)tables.get(j);
                boolean isDuplicate=i<0;
                if(i<0)
                    i=-i;

                if(isDuplicate){
                    bos.write(TTFontWriter.setNextUint16(0));
                    bos.write(TTFontWriter.setNextUint16(3));
                }else{
                    bos.write(TTFontWriter.setNextUint16(platformID[i]));
                    bos.write(TTFontWriter.setNextUint16(platformSpecificID[i]));
                }

                bos.write(TTFontWriter.setNextUint32(CMAPsubtables[i]));

                if(debug)
                    System.out.println("platformID[i]="+platformID[i]+" platformSpecificID[i]="+platformSpecificID[i]+" CMAPsubtables[i]="+CMAPsubtables[i]);
            }

            //work our correct order for subtables
            int[] offset=new int[numberSubtables];
            int[] order=new int[numberSubtables];
            for(int j=0;j<numberSubtables;j++){
                int i=(Integer)tables.get(j);
                if(i<0)
                    i=-i;

                offset[i]=CMAPsubtables[i];
                order[j]=i;
            }
            order= Sorts.quicksort(offset, order);

            //now write back each subtable
            for(int j=0;j<numberSubtables;j++){
                int i=order[j];

                //any padding
                while(bos.size()<CMAPsubtables[i]){
                    bos.write((byte) 0);
                }
                //}

                //assume 16 bit format to start
                bos.write(TTFontWriter.setNextUint16(CMAPformats[i]));

                //length
                bos.write(TTFontWriter.setNextUint16(CMAPlength[i]));

                //lang
                bos.write(TTFontWriter.setNextUint16(CMAPlang[i]));

                //actual data
                if(CMAPformats[i]==0 && CMAPlength[i]==262){

                   for(int glyphNum=0;glyphNum<256;glyphNum++){
                       bos.write(TTFontWriter.setNextUint8(glyphIndexToChar[i][glyphNum]));


                   }


                }else if(CMAPformats[i]==4){

                    //@sam -works for TT.
                    //to make it work for OTF we need to setup the values for all
                    //the variables in the  OTF COnstructor
                    //public CMAPWriter(PdfFont currentFontData,PdfJavaGlyphs glyphs)


                    //segcount
                    int segCount=CMAPsegCount[i]/2;
                    bos.write(TTFontWriter.setNextUint16(CMAPsegCount[i]));

                    bos.write(TTFontWriter.setNextUint16(CMAPsearchRange[i]));
                    bos.write(TTFontWriter.setNextUint16(CMAPentrySelector[i]));
                    bos.write(TTFontWriter.setNextUint16(CMAPrangeShift[i]));

                    for (int jj = 0; jj < segCount; jj++)
                         bos.write(TTFontWriter.setNextUint16(endCode[jj]));

                    bos.write(TTFontWriter.setNextUint16(CMAPreserved[i]));

                    for (int jj = 0; jj < segCount; jj++)
                        bos.write(TTFontWriter.setNextUint16(startCode[jj]));

                    for (int jj = 0; jj < segCount; jj++)
                        bos.write(TTFontWriter.setNextUint16(idDelta[jj]));

                    for (int jj = 0; jj < segCount; jj++)
                        bos.write(TTFontWriter.setNextUint16(idRangeOffset[jj]));
                    
                    if(glyphIdArray != null){
                    	for (int aGlyphIdArray : glyphIdArray) bos.write(TTFontWriter.setNextUint16(aGlyphIdArray));
                    }
                    
//                }else if(CMAPformats[j]==6){
//                    int firstCode=currentFontFile.getNextUint16();
//                    int entryCount=currentFontFile.getNextUint16();
//
//                    f6glyphIdArray = new int[firstCode+entryCount];
//                    for(int jj=0;jj<entryCount;jj++)
//                        f6glyphIdArray[jj+firstCode]=currentFontFile.getNextUint16();
//
//                }else{
//                    //System.out.println("Unsupported Format "+CMAPformats[j]);
//                    //reset to avoid setting
//                    CMAPformats[j]=-1;
//
                }
            }
        }

        /**validate format zero encoding*/
        //if(formatFour!=-1)
        //validateMacEncoding(formatZero,formatFour);

        bos.flush();
        bos.close();

        return bos.toByteArray();
    }

    public int getIntValue(int key) {

        int value=0;

        switch(key){
            case MIN_CHAR_CODE:
            value=minCharCode;
                break;

            case MAX_CHAR_CODE:
            value=maxCharCode;
                break;
        }

        return value;
    }
    
    /**
     *return the adjusted unicode value based on String
     *which also adjusts some composite glyph values 
     */
    private int getAdjustedUniValue(String str){
    	if(str.length()==1){
    		return str.charAt(0);
    	}
    	else{
    		if(str.equals("ff")){
    			return 64256;
    		}
    		else if(str.equals("fi")){
    			return 64257;
    		}
    		else if(str.equals("fl")){
    			return 64258;
    		}
    		else if(str.equals("ffi")){
    			return 64259;
    		}
    		else if(str.equals("ffl")){
    			return 64260;
    		}
    		else{
    			return -1;
    		}
    	}
    }

    public int getMaxCharCode() {
        return maxCharCode;
    }
}
