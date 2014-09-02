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
 * PdfResources.java
 * ---------------
 */
package org.jpedal.objects;

import org.jpedal.exception.PdfException;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.layers.PdfLayerList;
//<start-adobe>
import org.jpedal.objects.outlines.OutlineData;
//<end-adobe>
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.parser.PdfStreamDecoder;
import org.jpedal.parser.ValueTypes;
import org.jpedal.utils.LogWriter;
import org.w3c.dom.Document;

public class PdfResources {
	
	public static final int AcroFormObj = 1;
	public static final int GlobalResources = 2;
	public static final int StructTreeRootObj = 3;
	public static final int MarkInfoObj = 4;

    PdfLayerList layers;

    /**objects read from root*/
    private PdfObject metadataObj=null, acroFormObj=null, globalResources,PropertiesObj=null, structTreeRootObj=null, OCProperties=null,markInfoObj=null,OutlinesObj=null;

    //<start-adobe>
    /**
     * store outline data extracted from pdf
     */
    private OutlineData outlineData = null;

    //<end-adobe>


    /**
     * initialise OC Content and other items before Page decoded but after Resources read
     * @param current
     * @param currentPdfFile
     */
    public void setupResources(PdfStreamDecoder current, boolean alwaysCheck, PdfObject Resources, int pageNumber,
                               PdfObjectReader currentPdfFile) throws PdfException {

        if (globalResources != null){
            current.readResources(globalResources,true);

            PdfObject propObj=globalResources.getDictionary(PdfDictionary.Properties);
            if(propObj!=null)
                PropertiesObj=propObj;
        }

        /**read the resources for the page*/
        if (Resources != null){
            current.readResources(Resources,true);

            PdfObject propObj=Resources.getDictionary(PdfDictionary.Properties);
            if(propObj!=null)
                PropertiesObj=propObj;
        }

        /**
         * layers
         */
        if(OCProperties!=null && (layers==null || pageNumber!=layers.getOCpageNumber() || alwaysCheck)){

            currentPdfFile.checkResolved(OCProperties);

            if(layers==null)
                layers=new PdfLayerList();

            layers.init(OCProperties, PropertiesObj, currentPdfFile,pageNumber);

        }

        current.setObjectValue(ValueTypes.PdfLayerList,layers);
    }
	
	public PdfObject getPdfObject(int key) {
		
		PdfObject obj=null;
		
		switch(key){

            case AcroFormObj:
                obj=acroFormObj;
                break;

		case GlobalResources:
			obj=globalResources;
			break;

        case MarkInfoObj:
            obj=markInfoObj;
            break;

        case StructTreeRootObj:
            obj=structTreeRootObj;
            break;
		}
		return obj;
	}
	
	public void setPdfObject(int key, PdfObject obj) {
		
		switch(key){
		case GlobalResources:
			globalResources=obj;
			break;
		}
		
	}

	public void flush() {
		globalResources=null;
		
	}

    public void flushObjects() {

        //flush objects held
        metadataObj=null;
        acroFormObj=null;

        markInfoObj=null;
        PropertiesObj=null;
        OCProperties=null;
        structTreeRootObj=null;

        OutlinesObj=null;

        layers=null;

    }

    /**
     * flag to show if PDF document contains an outline
     */
    final public boolean hasOutline() {
        return OutlinesObj != null;
    }

    public void setValues(PdfObject pdfObject, PdfObjectReader currentPdfFile) {

        currentPdfFile.checkResolved(pdfObject);

        metadataObj=pdfObject.getDictionary(PdfDictionary.Metadata);

        acroFormObj=pdfObject.getDictionary(PdfDictionary.AcroForm);
        currentPdfFile.checkResolved(acroFormObj);

        markInfoObj=pdfObject.getDictionary(PdfDictionary.MarkInfo);

        structTreeRootObj=pdfObject.getDictionary(PdfDictionary.StructTreeRoot);

        OCProperties=pdfObject.getDictionary(PdfDictionary.OCProperties);

        OutlinesObj=pdfObject.getDictionary(PdfDictionary.Outlines);

        //<start-adobe>
        //set up outlines
        outlineData = null;
        //<end-adobe>
    }

    //<start-adobe>
    /**
     * provide direct access to outlineData object
     * @return  OutlineData
     */
    public OutlineData getOutlineData() {
        return outlineData;
    }

    public Document getOutlineAsXML(PdfObjectReader currentPdfFile, int pageCount) {
        if (outlineData == null && OutlinesObj != null) {

            try {
                currentPdfFile.checkResolved(OutlinesObj);

                outlineData = new OutlineData(pageCount);
                outlineData.readOutlineFileMetadata(OutlinesObj, currentPdfFile);

            } catch (Exception e) {
                if(LogWriter.isOutput())
                    LogWriter.writeLog("Exception " + e + " accessing outline ");
                outlineData = null;

            }
        }

        if (outlineData != null)
            return outlineData.getList();
        else
            return null;
    }
    //<end-adobe>

    public PdfFileInformation getMetaData(PdfObjectReader currentPdfFile) {
        if (currentPdfFile != null){
            /**Information object holds information from file*/
            return new PdfFileInformation().readPdfFileMetadata(metadataObj, currentPdfFile);
        }else
            return null;
    }

    public boolean isForm() {
        return acroFormObj!=null;
    }

    public PdfLayerList getPdfLayerList() {
        return layers;
    }
}
