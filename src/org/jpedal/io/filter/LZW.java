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
 * LZW.java
 * ---------------
 */
package org.jpedal.io.filter;

import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.sun.LZWDecoder;
import org.jpedal.sun.LZWDecoder2;
import org.jpedal.sun.TIFFLZWDecoder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * LZW
 */
public class LZW extends BaseFilter implements PdfFilter {

    // default values
    private int predictor = 1;
    private int EarlyChange = 1;
    private int colors = 1;
    private int bitsPerComponent = 8;
    private int rows;
    private int columns;


    public LZW(PdfObject decodeParms, int width,int height) {

        super(decodeParms);

        rows = height;
        columns = width;

        if(decodeParms!=null){

            int newBitsPerComponent = decodeParms.getInt(PdfDictionary.BitsPerComponent);
            if(newBitsPerComponent!=-1)
                bitsPerComponent=newBitsPerComponent;

            int newColors = decodeParms.getInt(PdfDictionary.Colors);
            if(newColors!=-1)
                colors=newColors;

            int columnsSet = decodeParms.getInt(PdfDictionary.Columns);
            if(columnsSet!=-1)
                columns=columnsSet;

            EarlyChange = decodeParms.getInt(PdfDictionary.EarlyChange);

            predictor = decodeParms.getInt(PdfDictionary.Predictor);

            int rowsSet = decodeParms.getInt(PdfDictionary.Rows);
            if(rowsSet!=-1)
                rows=rowsSet;

        }
    }

    public byte[] decode(byte[] data) throws Exception {

        if (rows * columns == 1) {

            if (data != null) {
                int bitsPerComponent1 = 8;byte[] processed_data = new byte[bitsPerComponent1 * rows
                        * ((columns + 7) >> 3)]; // will be resized if needed
                // 9allow for not a full 8
                // bits

                TIFFLZWDecoder lzw_decode = new TIFFLZWDecoder(columns, predictor, bitsPerComponent1);

                lzw_decode.decode(data, processed_data, rows);

                return applyPredictor(predictor, processed_data, colors, bitsPerComponent1, columns);
            }
        } else { // version for no parameters

            if (data != null) {
                ByteArrayOutputStream processed = new ByteArrayOutputStream();
                LZWDecoder lzw = new LZWDecoder();
                lzw.decode(data, processed, EarlyChange == 1);
                processed.close();
                data = processed.toByteArray();
            }

            data = applyPredictor(predictor, data, colors,  bitsPerComponent, columns);

        }

        return data;
    }

    public void decode(BufferedInputStream bis,BufferedOutputStream streamCache, String cacheName, Map cachedObjects) throws Exception{

        if (rows * columns == 1) {


        } else { // version for no parameters

            /**
             * decompress cached object
             */
            if (bis != null) {

                LZWDecoder2 lzw2 = new LZWDecoder2();
                lzw2.decode(null, streamCache, bis);

            }



            if (predictor != 1 && predictor != 10) {
                streamCache.flush();
                streamCache.close();
                if (cacheName != null)
                    setupCachedObjectForDecoding(cacheName);
            }

            applyPredictor(predictor, null, colors,  bitsPerComponent, columns);

        }
    }
}
