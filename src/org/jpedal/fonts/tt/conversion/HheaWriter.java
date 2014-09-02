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
 * HheaWriter.java
 * ---------------
 */
package org.jpedal.fonts.tt.conversion;

import org.jpedal.fonts.glyph.PdfJavaGlyphs;
import org.jpedal.fonts.tt.Hhea;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HheaWriter extends Hhea implements FontTableWriter{

    private int glyphCount;
    private double xMaxExtent;
    private double minRightSideBearing;
    private double minLeftSideBearing;
    private double advanceWidthMax;
    private double lowestDescender;
    private double highestAscender;

    public HheaWriter(PdfJavaGlyphs glyphs, double xMaxExtent, double minRightSideBearing, double minLeftSideBearing, double advanceWidthMax, double lowestDescender, double highestAscender) {
        glyphCount = glyphs.getGlyphCount();
        this.xMaxExtent = xMaxExtent;
        this.minRightSideBearing = minRightSideBearing;
        this.minLeftSideBearing = minLeftSideBearing;
        this.advanceWidthMax = advanceWidthMax;
        this.lowestDescender = lowestDescender;
        this.highestAscender = highestAscender;
    }

    public byte[] writeTable() throws IOException {

        ByteArrayOutputStream bos=new ByteArrayOutputStream();

        bos.write(TTFontWriter.setNextUint32(65536));                           //version               65536

        //Designer specified values
        bos.write(TTFontWriter.setFWord((int)highestAscender));                 //ascender
        bos.write(TTFontWriter.setFWord((int)lowestDescender));                 //descender
        bos.write(TTFontWriter.setFWord(0));                                    //lineGap               0

        //Calculated values
        bos.write(TTFontWriter.setUFWord((int)advanceWidthMax));                //advanceWidthMax
        bos.write(TTFontWriter.setFWord((int)minLeftSideBearing));              //minLeftSideBearing
        bos.write(TTFontWriter.setFWord((int)minRightSideBearing));             //minRightSideBearing
        bos.write(TTFontWriter.setFWord((int)xMaxExtent));                      //xMaxExtent

        //Italicise caret?
        bos.write(TTFontWriter.setNextInt16(1));                                //caretSlopeRise        1
        bos.write(TTFontWriter.setNextInt16(0));                                //caretSlopeRun         0
        bos.write(TTFontWriter.setFWord(0));                                    //caretOffset           0

        //reserved values
        for( int i = 0; i < 4; i++ )
            bos.write(TTFontWriter.setNextUint16(0)); //0

        bos.write(TTFontWriter.setNextInt16(0));//metricDataFormat
        bos.write(TTFontWriter.setNextUint16(glyphCount)); //count

        bos.flush();
        bos.close();

        return bos.toByteArray();
    }

    public int getIntValue(int key) {
        return 0;
    }
}
