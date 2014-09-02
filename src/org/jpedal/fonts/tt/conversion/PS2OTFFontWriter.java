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
 * PS2OTFFontWriter.java
 * ---------------
 */
package org.jpedal.fonts.tt.conversion;

import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.TrueType;
import org.jpedal.fonts.Type1C;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.fonts.glyph.T1Glyphs;
import org.jpedal.fonts.tt.FontFile2;
import org.jpedal.fonts.tt.Hhea;
import org.jpedal.fonts.tt.Hmtx;
import org.jpedal.fonts.tt.TTGlyphs;
import org.jpedal.utils.LogWriter;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PS2OTFFontWriter extends FontWriter{

    byte[] cff;

    byte[] cmap=null;

    PdfFont pdfFont, originalFont;
    PdfJavaGlyphs glyphs;

    int minCharCode;
    int maxCharCode;

    //Glyph Metrics
    private int xAvgCharWidth=0;
    private double xMaxExtent= Double.MIN_VALUE;
    private double minRightSideBearing = Double.MAX_VALUE;
    private double minLeftSideBearing = Double.MAX_VALUE;
    private double advanceWidthMax = Double.MIN_VALUE;
    private double lowestDescender = -1;
    private double highestAscender = 1;
    private float[] fontBBox = new float[4];
    private double emSquareSize = 1000;
    private int[] advanceWidths;
    private int[] lsbs=null;
    private HashMap<String,Integer> widthMap;
    private String[] glyphList=null;

    FontFile2 orginTTTables=null;


    public PS2OTFFontWriter(PdfFont originalFont,byte[] rawFontData, String fileType, HashMap<String,Integer> widths) throws Exception {

        boolean is1C=fileType.equals("cff");

        name=originalFont.getBaseFontName();

        this.widthMap = widths;

        //adjust for TT or Postscript
        String[] tablesUsed=new String[]{"CFF ",
                //                "FFTM",
//                "GDEF",
                "OS/2",
                "cmap",
                "head",
                "hhea",
                "hmtx",
                "maxp",
                "name",
                "post"
        };


        if(fileType.equals("ttf")){

            subType=TTF;

            //read the data into our T1/t1c object so we can then parse
            glyphs=originalFont.getGlyphData();
            pdfFont=new TrueType(rawFontData,glyphs);

            orginTTTables=new FontFile2(rawFontData);

            //order for writing out tables
            //(header if different order) so we also need to translate
//            tablesUsed=new String[]{"head","hhea","maxp",
//                    "OS/2",
//                    "hmtx",
//                    "cmap",
//                    "loca",
//                    "glyf",
//                    "name",
//                    "post"
//            };

            //new woff table order            
            tablesUsed=new String[]{
            		"OS/2",
            		"cmap",            	          
            		"cvt ",
            		"fpgm",
                    "glyf",
                    "head",
                    "hhea",
                    "hmtx",
                    "loca",
                    "maxp",
                    "name",
                    "post",
                    "prep"
                    
            };

            for(int i=0;i<tablesUsed.length;i++){
                IDtoTable.put(tablesUsed[i],i);
            }

        }else{
            /**
             * for the moment we reread with diff paramters to extract other data
             */
            //read the data into our T1/t1c object so we can then parse
            glyphs=new T1Glyphs(false,is1C);

            pdfFont=new Type1C(rawFontData,glyphs,is1C);

        }

        glyphCount=glyphs.getGlyphCount();

        this.originalFont =originalFont;
        this.cff=rawFontData;

        tableList=new ArrayList();

        numTables=tablesUsed.length;

        int floor = 1;
        while (floor*2 <= numTables)
            floor *= 2;

        searchRange = floor * 16;

        entrySelector = 0;
        while (Math.pow(2,entrySelector) < floor)
            entrySelector++;

        rangeShift = (numTables*16) - searchRange;

        tableList.addAll(Arrays.asList(tablesUsed).subList(0, numTables));

        //location of tables
        checksums=new int[tableCount][1];
        tables=new int[tableCount][1];
        tableLength =new int[tableCount][1];

        type=OPENTYPE;


        //@sam - used to hack in font
//        if(name.contains("KTBBOD+HermesFB-Bold")){
////        if(name.contains("ZapfEchos")){
//
//            try{
//                File file=new File("C:/Users/Sam/Downloads/HermesFB-Regular.otf");
////                File file=new File("C:/Users/Sam/Downloads/zapfechosWorks.otf");
//                int len=(int)file.length();
//                byte[] data=new byte[len];
//                FileInputStream fis=new FileInputStream(file);
//                fis.read(data);
//                fis.close();
//                FontFile2 f=new FontFile2(data,false);
//
//
//                for(int l=0;l<numTables;l++){
//
//                    String tag= (String) tableList.get(l);
//
//                    //read table value (including for head which is done differently at end)
//                    int id= getTableID(tag);
//
//                    if(id!=-1){
//                        this.tables[id][currentFontID]=f.getTableStart(id);
//                    }
//                }
//
//            }
//        }
    }

    void readTables() {

        //Fetch advance widths
        int totalWidth=0;
        advanceWidths = new int[glyphCount];
        if (widthMap != null) {
            for (int i=0; i < glyphCount; i++) {
                Integer w;
                if (pdfFont.isCIDFont()) {
                    w = widthMap.get(glyphs.getCharGlyph(i+1));
                } else {
                    w = widthMap.get(glyphs.getIndexForCharString(i+1));
                }

                if (w != null) {
                    advanceWidths[i] = w;
                } else if (pdfFont.is1C()) {
                    int fd = 0;
                    if (((Type1C)pdfFont).getFDSelect() != null) {
                        fd = ((Type1C)pdfFont).getFDSelect()[i];
                    }
                    Integer num = widthMap.get("JPedalDefaultWidth"+fd);
                    if (num != null) {
                        advanceWidths[i] = num;
                    }
                }
                advanceWidthMax = advanceWidthMax > advanceWidths[i] ? advanceWidthMax : advanceWidths[i];
                totalWidth += advanceWidths[i];
            }
        }

        //Store average width
        if(glyphCount>0)
            xAvgCharWidth= (int) ((double)totalWidth/(double)glyphCount);

        //Collect glyph metrics
        double maxX = Double.MIN_VALUE;
        
        int iterationCount = glyphCount;
        if(originalFont.getCIDToGIDMap()!=null){
        	if(originalFont.getCIDToGIDMap().length<iterationCount){
        		iterationCount = originalFont.getCIDToGIDMap().length;
        	}
        }

            for (int i=0; i<iterationCount && i<256; i++) {
                PdfGlyph glyph = glyphs.getEmbeddedGlyph(new org.jpedal.fonts.glyph.T1GlyphFactory(),
                        pdfFont.getMappedChar(i, false),
                        new float[][]{{1,0},{0,1}},
                        i,
                        pdfFont.getGlyphValue(i),
                        pdfFont.getWidth(i),
                        pdfFont.getMappedChar(i, false));

                if (glyph != null && glyph.getShape() != null) {

                    Rectangle2D area = glyph.getShape().getBounds2D();

                    double lsb = area.getMinX();
                    double rsb = advanceWidths[i]-area.getMaxX();
                    double extent = area.getMinX() + area.getWidth();

                    minLeftSideBearing = minLeftSideBearing < lsb ? minLeftSideBearing : lsb;
                    minRightSideBearing = minRightSideBearing < rsb ? minRightSideBearing : rsb;
                    xMaxExtent = xMaxExtent > extent ? xMaxExtent : extent;
                    lowestDescender = lowestDescender < area.getMinY() ? lowestDescender : area.getMinY();
                    highestAscender = highestAscender > area.getMaxY() ? highestAscender : area.getMaxY();
                    maxX = maxX > area.getMaxX() ? maxX : area.getMaxX();
                }
            }

        if (originalFont.is1C()) {
            fontBBox = pdfFont.FontBBox;
        } else {
            fontBBox = originalFont.getFontBounds();
        }

        minLeftSideBearing = minLeftSideBearing < fontBBox[0] ? minLeftSideBearing : fontBBox[0];
        lowestDescender = lowestDescender < fontBBox[1] ? lowestDescender : fontBBox[1];
        maxX = maxX > fontBBox[2] ? maxX : fontBBox[2];
        highestAscender = highestAscender > fontBBox[3] ? highestAscender : fontBBox[3];

        fontBBox = new float[]{(float)minLeftSideBearing, (float)lowestDescender, (float)maxX, (float)highestAscender};
    }

    public byte[] getTableBytes(int tableID){

        byte[] fontData=new byte[0];

        FontTableWriter tableWriter=null;

        switch(tableID){

            case CFF:
                if (pdfFont.is1C()) {
                    //fix bad commands in CFF data
                    fontData = (new CFFFixer(cff)).getBytes();
                } else {
                    //convert type 1 to 1c
                    tableWriter=new CFFWriter(glyphs, name);

                    //Fetch glyph names and metrics
                    CFFWriter cffWriter = (CFFWriter)tableWriter;
                    glyphList = cffWriter.getGlyphList();
                    advanceWidths = cffWriter.getWidths();
                    lsbs = cffWriter.getBearings();
                    fontBBox = cffWriter.getBBox();
                    emSquareSize = cffWriter.getEmSquareSize();

                    highestAscender = fontBBox[3];
                    lowestDescender = fontBBox[1];
                    advanceWidthMax = 0;

                    //Calculate metrics
                    int totalWidth=0;
                    for (int i=0; i<advanceWidths.length; i++) {
                        advanceWidthMax = advanceWidthMax > advanceWidths[i] ? advanceWidthMax : advanceWidths[i];
                        totalWidth += advanceWidths[i];
                        minLeftSideBearing = minLeftSideBearing < lsbs[i] ? minLeftSideBearing : lsbs[i];
                    }

                    if(glyphCount>0)
                        xAvgCharWidth= (int) ((double)totalWidth/(double)glyphCount);


//                    try {
//                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                        BufferedReader reader = new BufferedReader(new FileReader(new File("C:\\Users\\Sam\\Desktop\\FontForgeAmerican.txt")));
//
//                        while (reader.ready()) {
//                            String line = reader.readLine();
//                            String first = line.split("\t")[0];
//                            first = first.replaceAll("[^01]", "");
//                            if (first.length() == 8) {
//                                int mag = 128;
//                                byte val = 0;
//                                for (int i=0; i<8; i++) {
//                                    boolean is1 = first.charAt(i) == '1';
//                                    if (is1)
//                                        val += mag;
//
//                                    mag = mag / 2;
//                                }
//
//                                bos.write(val);
//                                fontData = bos.toByteArray();
//                            }
//                        }
//                    }
                }
                break;

                case HEAD:               
                	if(subType==TTF){
                		 fontData=orginTTTables.getTableBytes(HEAD);
                	}else{
                		tableWriter=new HeadWriter(fontBBox);
                	}
                	
                    break;

                case CMAP :               	
                	if(subType==TTF){
//                		byte [] data = orginTTTables.getTableBytes(CMAP);
//                		if(data.length>0){
                			tableWriter=new CMAPWriter(name,pdfFont,originalFont,glyphs,glyphList);
//                		}
//                		//identify files which does not contain cmap , cidToGidCmap or ToUnicode data
//                		else if(originalFont.getCIDToGIDStream()==null && originalFont.getToUnicode()==null){
//        					tableWriter=new CMAPWriter(name,pdfFont,originalFont,glyphs,glyphList);
//        					break;
//                		}
//                		else{
//                			fontData=data;
//                		}	
    	           	}else{
    	           		// if(name.contains("BKJFHK+HermesFB-Regular")){
    	                tableWriter=new CMAPWriter(name,pdfFont,originalFont,glyphs,glyphList);
    	                 minCharCode=tableWriter.getIntValue(FontTableWriter.MIN_CHAR_CODE);
    	                 maxCharCode=tableWriter.getIntValue(FontTableWriter.MAX_CHAR_CODE);
    	                // }
    	           	}
                    break;

                case GLYF:
                    fontData=orginTTTables.getTableBytes(GLYF);
                    break;

                case HHEA:           	
                	if(subType==TTF){
                        TTGlyphs ttGlyphs =(TTGlyphs)glyphs;
                        Hmtx hmtx = (Hmtx) ttGlyphs.getTable(HMTX);
                        Hhea hhea = (Hhea) ttGlyphs.getTable(FontFile2.HHEA);

                        boolean changed = false;

                        //If advanceWidthMax is 0 recalculate
                        int localAdvanceWidthMax = hhea.getIntValue(Hhea.ADVANCEWIDTHMAX);
                        if(localAdvanceWidthMax==0){
                            for(int z=0;z<glyphs.getGlyphCount();z++){
                                int temp = (int)hmtx.getUnscaledWidth(z);
                                localAdvanceWidthMax = temp > localAdvanceWidthMax ? temp : localAdvanceWidthMax;
                            }

                            //Mark if changed so we can regenerate
                            if (localAdvanceWidthMax != hhea.getIntValue(Hhea.ADVANCEWIDTHMAX)) {
                                changed = true;
                            }
                        }

                        if (changed) {
                            //if advancewidthmax <= 0 calculate value from hmtx unscaled width [see reallybad.pdf case 13246]
                            tableWriter=new HheaWriter(glyphs, hhea.getIntValue(Hhea.XMAXEXTENT),
                                    hhea.getIntValue(Hhea.MINIMUMRIGHTSIDEBEARING),hhea.getIntValue(Hhea.MINIMUMRIGHTSIDEBEARING),
                                    localAdvanceWidthMax,hhea.getIntValue(Hhea.DESCENDER),hhea.getIntValue(Hhea.ASCENDER));
                        }else{
                            fontData = orginTTTables.getTableBytes(HHEA);
                        }
                	}else{
                		tableWriter=new HheaWriter(glyphs, xMaxExtent, minRightSideBearing, minLeftSideBearing, advanceWidthMax, lowestDescender, highestAscender);
                	}
                    break;

                case HMTX:             
                	if(subType==TTF){
                		fontData=orginTTTables.getTableBytes(HMTX);
                	}else{
                		tableWriter=new HmtxWriter(glyphs,advanceWidths, lsbs);
                	}	
                    break;

                case LOCA:
                	if(subType==TTF){
                		fontData=orginTTTables.getTableBytes(LOCA);
                	}
                	else{
                		tableWriter=new LocaWriter(name,pdfFont,originalFont,glyphs,glyphList);
                	}
                	
                    break;

                case OS2 :
                	if(subType==TTF){
                		byte[] data = orginTTTables.getTableBytes(OS2);
                		//check whether OS2 table exists, other wise create new
    					if(data.length>0){
    						fontData=data;
    					}else{    						    						
    						tableWriter=new OS2Writer(originalFont,glyphs, xAvgCharWidth, minCharCode,  maxCharCode, fontBBox, emSquareSize);
    					}
    				}else{
    				    tableWriter=new OS2Writer(originalFont,glyphs, xAvgCharWidth, minCharCode,  maxCharCode, fontBBox, emSquareSize);
    				}
                    break;

                case MAXP :
    				if(subType==TTF){
    				    fontData=orginTTTables.getTableBytes(MAXP);
    				}else{
    				    tableWriter=new MAXPWriter(glyphs);
    				}
                    break;

                case NAME:
                	if(subType==TTF){
                		// fontData = orginTTTables.getTableBytes(NAME);
                		 tableWriter=new NameWriter(pdfFont, glyphs, name);
                	}
                	else{
                		tableWriter=new NameWriter(pdfFont, glyphs, name);
                	}                
                    break;

                case POST:
                	if(subType==TTF){
                		//check whether the POST table exists, other wise create new
                		byte[] data = orginTTTables.getTableBytes(POST);
                		if(data.length>0){
    						fontData=data;
    					}  
                		else{
                			tableWriter=new PostWriter();
                		}
                	}
                	else{
                		tableWriter=new PostWriter();
                	}
                    break;
                    
                    //new additions START    
                case PREP:
                	fontData = orginTTTables.getTableBytes(PREP);
                	//fill with zero values if no bytes found
                	if(fontData.length==0){
                		fontData = new byte[] {FontWriter.setNextUint8(0)};
                	}
                	break;
                	
                case CVT:
                	fontData = orginTTTables.getTableBytes(CVT);
                	//fill with zero values if no bytes found; cvt values always has to be double bytes
                	if(fontData.length==0){
                		fontData = FontWriter.setNextUint16(0);
                	}
                	break;
                	
                case FPGM:
                	fontData = orginTTTables.getTableBytes(FPGM);
                	//fill with zero values if no bytes found
                	if(fontData.length==0){
                		fontData =  new byte[] {FontWriter.setNextUint8(0)};
                	}
                	break;
                	//new additions END
                
                
            default:

//                //@sam - code to hack table from manulaly converted
//                // font (setup fro Zapf)
//                if(name.contains("BKJFHK+HermesFB-Regular")){
////                if(name.contains("apf")){
//
//                        try {
//                File file=new File("C:/Users/Sam/Downloads/BKJFHK+HermesFB-Regular.otf");
////                File file=new File("C:/Users/Sam/Downloads/zapfechosWorks.otf");
//                        int len=(int)file.length();
//                        byte[] data=new byte[len];
//                        FileInputStream fis=new FileInputStream(file);
//                        fis.read(data);
//                        fis.close();
//                        FontFile2 f=new FontFile2(data,false);
//                        f.selectTable(tableID);
//                        fontData=f.getTableBytes(tableID);
//                    }
//                }

                break;
        }

        if(tableWriter!=null){
            try {
                fontData=tableWriter.writeTable();
            } catch (Exception e) {
                //tell user and log
                if(LogWriter.isOutput())
                    LogWriter.writeLog("Exception: "+e.getMessage());
            }
        }


        //Code to save out table
//        if (tableID == CFF && name.contains("NAAAFM"))
//            new BinaryTool(fontData,"C:/Users/sam/desktop/iab/"+name+".iab");


        return fontData;
    }
}
