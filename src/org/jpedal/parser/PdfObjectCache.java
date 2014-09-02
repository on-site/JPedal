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
 * PdfObjectCache.java
 * ---------------
 */
package org.jpedal.parser;

import org.jpedal.exception.PdfException;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.*;
import org.jpedal.utils.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * caches for data
 */
public class PdfObjectCache {

    public static final int ColorspacesUsed=1;
    public static final int Colorspaces=2;

    //init size of maps
    private static final int initSize=50;

    //int values for all colorspaces
    private final Map colorspacesUsed=new HashMap(initSize);

    public Map colorspacesObjects=new HashMap(initSize);

    /**
     * avoid resaving the same image multiple times
     */
    HashMap imageAlreadySaved=new HashMap();

    /**colors*/
    private Map colorspaces=new HashMap(initSize);

    private Map globalXObjects = new HashMap(initSize),localXObjects=new HashMap(initSize);

    public final Map XObjectColorspaces=new HashMap(initSize);

    public Map patterns=new HashMap(initSize),globalShadings=new HashMap(initSize), localShadings=new HashMap(initSize);

    Map imposedImages = new HashMap(initSize);

    PdfObject groupObj=null;

    PdfObject pageGroupingObj=null;

    /**fonts*/
    public Map unresolvedFonts=new HashMap(initSize);
    public Map directFonts=new HashMap(initSize);
    public Map resolvedFonts=new HashMap(initSize);

    /**GS*/
    Map GraphicsStates=new HashMap(initSize);

    public PdfObjectCache copy() {

        PdfObjectCache copy=new PdfObjectCache();

        copy.localShadings=localShadings;
        copy.unresolvedFonts=unresolvedFonts;
        copy.GraphicsStates= GraphicsStates;
        copy.directFonts= directFonts;
        copy.resolvedFonts= resolvedFonts;
        copy.colorspaces= colorspaces;

        copy.localXObjects= localXObjects;
        copy.globalXObjects= globalXObjects;

        copy.groupObj= groupObj;


        return copy;

    }

    public PdfObjectCache() {}

    public void put(int type, int key, Object value){
        switch(type){
            case ColorspacesUsed:
                colorspacesUsed.put(key,value);
                break;
        }
    }


    public Iterator iterator(int type){

        Iterator returnValue=null;

        switch(type){
            case ColorspacesUsed:
                returnValue=colorspacesUsed.keySet().iterator();
                break;
        }

        return returnValue;
    }

    public Object get(int key, Object value){

        Object returnValue=null;

        switch(key){
            case ColorspacesUsed:
                returnValue=colorspacesUsed.get(value);
                break;

            case Colorspaces:
                returnValue=colorspaces.get(value);
                break;
        }

        return returnValue;
    }

    public void resetFonts() {
        resolvedFonts.clear();
        unresolvedFonts.clear();
        directFonts.clear();
    }

    public PdfObject getXObjects(String localName) {

        PdfObject XObject = (PdfObject) localXObjects.get(localName);
        if (XObject == null)
            XObject = (PdfObject) globalXObjects.get(localName);

        return XObject;
    }

    public void resetXObject(String localName, String ref, byte[] rawData) {

        XObject XObject=new XObject(ref);
        if(rawData[rawData.length-1]=='R')
            XObject.setStatus(PdfObject.UNDECODED_REF);
        else
            XObject.setStatus(PdfObject.UNDECODED_DIRECT);

        XObject.setUnresolvedData(rawData, PdfDictionary.Page);
        if(localXObjects.containsKey(localName)){
            localXObjects.remove(localName);
            //localXObjects.put(localName,XObject);

        }else{
            globalXObjects.remove(localName);
            //globalXObjects.put(localName,XObject);
        }
    }

    public void readResources(PdfObject Resources, boolean resetList, PdfObjectReader currentPdfFile)  throws PdfException{

        currentPdfFile.checkResolved(Resources);

        //decode
        String[] names={"ColorSpace","ExtGState","Font", "Pattern","Shading","XObject"};
        int[] keys={PdfDictionary.ColorSpace, PdfDictionary.ExtGState, PdfDictionary.Font,
                PdfDictionary.Pattern, PdfDictionary.Shading,PdfDictionary.XObject};

        for(int ii=0;ii<names.length;ii++){

            if(keys[ii]==PdfDictionary.Font || keys[ii]==PdfDictionary.XObject)
                readArrayPairs(Resources, resetList,keys[ii],currentPdfFile);
            else
                readArrayPairs(Resources, false,keys[ii],currentPdfFile);
        }
    }

    private void readArrayPairs(PdfObject Resources, boolean resetFontList, int type, PdfObjectReader currentPdfFile) throws PdfException {

        final boolean debugPairs=false;

        if(debugPairs){
            System.out.println("-------------readArrayPairs-----------"+type);
            System.out.println("new="+Resources+ ' '+Resources.getObjectRefAsString());
        }
        String id,value;

        /**
         * new code
         */
        if(Resources!=null){

            PdfObject resObj=Resources.getDictionary(type);

            if(debugPairs)
                System.out.println("new res object="+resObj);

            if(resObj!=null){

                /**
                 * read all the key pairs for Glyphs
                 */
                PdfKeyPairsIterator keyPairs=resObj.getKeyPairsIterator();

                PdfObject obj;

                if(debugPairs){
                    System.out.println("New values");
                    System.out.println("----------");
                }

                while(keyPairs.hasMorePairs()){

                    id=keyPairs.getNextKeyAsString();
                    value=keyPairs.getNextValueAsString();
                    obj=keyPairs.getNextValueAsDictionary();

                    if(debugPairs)
                        System.out.println(id+ ' '+obj+ ' ' +value+ ' ' +Resources.isDataExternal());

                    if(Resources.isDataExternal()){ //check and flag if missing

                        //ObjectDecoder objectDecoder=new ObjectDecoder(currentPdfFile.getObjectReader());

                        if(obj==null && value==null){
                            Resources.setFullyResolved(false);
                            return;
                        }else if(obj==null){

                            PdfObject childObj= ObjectFactory.createObject(type, value, type, -1);

                            childObj.setStatus(PdfObject.UNDECODED_DIRECT);
                            childObj.setUnresolvedData(StringUtils.toBytes(value), type);

//                            if(!objectDecoder.resolveFully(childObj)){
//                                Resources.setFullyResolved(false);
//                                return;
//                            }

                            //cache if setup
                            if(type==PdfDictionary.Font){
                                directFonts.put(id,childObj);
                            }
//                        }else if(!objectDecoder.resolveFully(obj)){
//                            Resources.setFullyResolved(false);
//                            return;
                        }
                    }

                    switch(type){

                        case PdfDictionary.ColorSpace:
                            colorspaces.put(id,obj);
                            break;

                        case PdfDictionary.ExtGState:
                            GraphicsStates.put(id,obj);
                            break;

                        case PdfDictionary.Font:

                            unresolvedFonts.put(id,obj);

                            break;

                        case PdfDictionary.Pattern:
                            patterns.put(id,obj);

                            break;

                        case PdfDictionary.Shading:
                            if(resetFontList)
                                globalShadings.put(id, obj);
                            else
                                localShadings.put(id, obj);

                            break;

                        case PdfDictionary.XObject:
                            if(resetFontList)
                                globalXObjects.put(id, obj);
                            else
                                localXObjects.put(id, obj);

                            break;

                    }

                    keyPairs.nextPair();
                }
            }
        }
    }


    public void reset(PdfObjectCache newCache) {

        //reset copies
        localShadings=new HashMap(initSize);
        resolvedFonts=new HashMap(initSize);
        unresolvedFonts=new HashMap(initSize);
        directFonts=new HashMap(initSize);
        colorspaces=new HashMap(initSize);
        GraphicsStates=new HashMap(initSize);
        localXObjects=new HashMap(initSize);

        Iterator keys=newCache.GraphicsStates.keySet().iterator();
        while(keys.hasNext()){
            Object key=keys.next();
            GraphicsStates.put(key,newCache.GraphicsStates.get(key));
        }

        keys=newCache.colorspaces.keySet().iterator();
        while(keys.hasNext()){
            Object key=keys.next();
            colorspaces.put(key, newCache.colorspaces.get(key));
        }


        keys=newCache.localXObjects.keySet().iterator();
        while(keys.hasNext()){
            Object key=keys.next();
            localXObjects.put(key, newCache.localXObjects.get(key));
        }

        keys=newCache.globalXObjects.keySet().iterator();
        while(keys.hasNext()){
            Object key=keys.next();
            globalXObjects.put(key, newCache.globalXObjects.get(key));
        }

        //allow for no fonts in FormObject when we use any global
        if(unresolvedFonts.isEmpty()){
            //unresolvedFonts=rawFonts;
            keys=newCache.unresolvedFonts.keySet().iterator();
            while(keys.hasNext()){
                Object key=keys.next();
                unresolvedFonts.put(key,newCache.unresolvedFonts.get(key));
            }
        }
    }

    public void restore(PdfObjectCache mainCache) {

        directFonts= mainCache.directFonts;
        unresolvedFonts= mainCache.unresolvedFonts;
        resolvedFonts= mainCache.resolvedFonts;
        GraphicsStates= mainCache.GraphicsStates;
        colorspaces= mainCache.colorspaces;
        localShadings= mainCache.localShadings;
        localXObjects= mainCache.localXObjects;
        globalXObjects= mainCache.globalXObjects;

        groupObj= mainCache.groupObj;

    }

    public int getXObjectCount() {
        return localXObjects.keySet().size()+globalXObjects.keySet().size();
    }

    public Object getImposedKey(String key) {

        return imposedImages.get(key);
    }

    public void setImposedKey(String key,int id) {
        if (imposedImages != null)
            imposedImages.put(key, id);
    }

    /**
     * see if image saved (and flag as true in future)
     * @param objectRefAsString
     * @return
     */
    public boolean testIfImageAlreadySaved(PdfObject XObject) {

        boolean flag=false;

        if(XObject.getGeneralType(-1)!=PdfDictionary.ID){ //never cache these

            String objectRefAsString=XObject.getObjectRefAsString();

            flag=imageAlreadySaved.containsKey(objectRefAsString);

            if(flag==false){
                imageAlreadySaved.put(objectRefAsString,"x");
            }
        }
        return flag;
    }
}
