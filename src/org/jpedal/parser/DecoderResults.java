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
 * DecoderResults.java
 * ---------------
 */
package org.jpedal.parser;

import org.jpedal.constants.PageInfo;

import java.util.Iterator;

public class DecoderResults {

    private boolean imagesProcessedFully=true,hasNonEmbeddedCIDFonts,hasYCCKimages,pageSuccessful;

    private boolean ttHintingRequired,timeout=false;

    //values on last decodePage
    private Iterator colorSpacesUsed;

    private String nonEmbeddedCIDFonts="";

    /**
     * flag to show embedded fonts present
     */
    private boolean hasEmbeddedFonts = false;

    public boolean getImagesProcessedFully() {
        return imagesProcessedFully;
    }

    public void update(PdfStreamDecoder current, boolean includeAll) {

        colorSpacesUsed= (Iterator) current.getObjectValue(PageInfo.COLORSPACES);


        nonEmbeddedCIDFonts= (String) current.getObjectValue(DecodeStatus.NonEmbeddedCIDFonts);

        hasYCCKimages = current.getBooleanValue(DecodeStatus.YCCKImages);
        pageSuccessful = current.getBooleanValue(DecodeStatus.PageDecodingSuccessful);

        imagesProcessedFully = current.getBooleanValue(DecodeStatus.ImagesProcessed);
        hasNonEmbeddedCIDFonts= current.getBooleanValue(DecodeStatus.NonEmbeddedCIDFonts);

        ttHintingRequired= current.getBooleanValue(DecodeStatus.TTHintingRequired);
        timeout= current.getBooleanValue(DecodeStatus.Timeout);

        if(includeAll){
            hasEmbeddedFonts = current.getBooleanValue(ValueTypes.EmbeddedFonts);
        }

    }

    public void resetTimeout(){
        timeout=false;
    }

    public boolean getPageDecodeStatus(int status) {

        switch(status){
            case DecodeStatus.NonEmbeddedCIDFonts:
                return hasNonEmbeddedCIDFonts;

            case DecodeStatus.ImagesProcessed:
                return imagesProcessedFully;

            case DecodeStatus.PageDecodingSuccessful:
                return pageSuccessful;

            case DecodeStatus.Timeout:
                return timeout;

            case DecodeStatus.YCCKImages:
                return hasYCCKimages;

            case DecodeStatus.TTHintingRequired:
                return ttHintingRequired;

            default:
                throw new RuntimeException("Unknown parameter "+status);

        }
    }

    /**
     * return details on page for type (defined in org.jpedal.constants.PageInfo) or null if no values
     * Unrecognised key will throw a RunTime exception
     *
     * null returned if JPedal not clear on result
     */
    public Iterator getPageInfo(int type) {
        switch(type){

            case PageInfo.COLORSPACES:
                return colorSpacesUsed;

            default:
                return null;
        }
    }

    /**
     * get page statuses
     */
    public String getPageDecodeStatusReport(int status) {

        if(status==(DecodeStatus.NonEmbeddedCIDFonts)){
            return nonEmbeddedCIDFonts;
        }else
            new RuntimeException("Unknown parameter");

        return "";
    }


    /**
     * shows if embedded fonts present on page just decoded
     */
    public boolean hasEmbeddedFonts() {
        return hasEmbeddedFonts;
    }

    public void resetColorSpaces() {
        //disable color list if forms
        colorSpacesUsed=null;
    }
}
