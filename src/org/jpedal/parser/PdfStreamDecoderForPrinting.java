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
 * PdfStreamDecoderForPrinting.java
 * ---------------
 */

package org.jpedal.parser;

import org.jpedal.PdfDecoder;
import org.jpedal.external.ColorHandler;
import org.jpedal.external.CustomPrintHintingHandler;
import org.jpedal.external.Options;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.render.SwingDisplay;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class PdfStreamDecoderForPrinting extends PdfStreamDecoder {

    public PdfStreamDecoderForPrinting(PdfObjectReader currentPdfFile, boolean b, PdfLayerList layers) {
        super(currentPdfFile, b, layers);

        isPrinting = true;
    }

    public void print(Graphics2D g2,AffineTransform scaling,int currentPrintPage,
                      Rectangle userAnnot,CustomPrintHintingHandler customPrintHintingHandler, PdfDecoder pdf){

        if(customPrintHintingHandler!=null){
            current.stopG2HintSetting(true);
            customPrintHintingHandler.preprint(g2,pdf);
        }

        current.setPrintPage(currentPrintPage);

        current.setCustomColorHandler((ColorHandler) pdf.getExternalHandler(Options.ColorHandler));

        current.setG2(g2);
        current.paint(null,scaling,userAnnot);
    }

    public void setObjectValue(int key, Object  obj){

        if(key==ValueTypes.ObjectStore){
            objectStoreStreamRef = (ObjectStore)obj;

            current=new SwingDisplay(this.pageNum,objectStoreStreamRef,true);
            current.setHiResImageForDisplayMode(useHiResImageForDisplay);

            if(customImageHandler!=null && current!=null)
                current.setCustomImageHandler(customImageHandler);
        }else{
            super.setObjectValue(key,obj);
        }

    }
}
