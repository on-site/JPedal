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
 * ArrayDecoder.java
 * ---------------
 */
package org.jpedal.io;

import org.jpedal.exception.PdfSecurityException;
import org.jpedal.objects.raw.OCObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.NumberUtils;
import org.jpedal.utils.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * parse PDF array data from PDF
 */
public class ArrayDecoder extends ObjectDecoder{

    //now create array and read values
    private float[] floatValues=null;
    private int[] intValues=null;
    private double[] doubleValues=null;
    private byte[][] mixedValues=null;
    private byte[][] keyValues=null;
    private byte[][] stringValues=null;
    private boolean[] booleanValues=null;
    private Object[] objectValues=null;


    private int i;
    private int endPoint;
    private int type;

    private int keyReached=-1;

    private Object[] objectValuesArray=null;

    public ArrayDecoder(PdfFileReader pdfFileReader, int i, int endPoint, int type) {
        super(pdfFileReader);

        this.i=i;
        this.endPoint=endPoint;
        this.type=type;

    }

    public ArrayDecoder(PdfFileReader pdfFileReader, int i, int endPoint, int type, Object[] objectValuesArray, int keyReached) {
        super(pdfFileReader);

        this.i=i;
        this.endPoint=endPoint;
        this.type=type;
        this.objectValuesArray=objectValuesArray;
        this.keyReached=keyReached;

    }


    public int readArray(boolean ignoreRecursion, byte[] raw, PdfObject pdfObject, int PDFkeyInt) {

        //roll on
        if(type== PdfDictionary.VALUE_IS_KEY_ARRAY && raw[i]==60){
            //i--;
        }else if(raw[i]!=91 && raw[i]!='<')
            i++;

        //ignore empty
        if(raw[i]=='[' && raw[i+1]==']')
            return i+1;

        Map isRef=new HashMap();

        boolean isHexString=false;

        boolean alwaysRead =(PDFkeyInt==PdfDictionary.Kids || PDFkeyInt==PdfDictionary.Annots);

        final boolean debugArray=debugFastCode;// || type==PdfDictionary.VALUE_IS_OBJECT_ARRAY;

        if(debugArray)
            System.out.println(padding+"Reading array type="+PdfDictionary.showArrayType(type)+" into "+pdfObject+ ' ' +(char)raw[i]+ ' ' +(char)raw[i+1]+ ' ' +(char)raw[i+2]+ ' ' +(char)raw[i+3]+ ' ' +(char)raw[i+4]);

        int currentElement=0, elementCount=0,keyStart;

        //move cursor to start of text
        while(raw[i]==10 || raw[i]==13 || raw[i]==32)
            i++;

        //allow for comment
        if(raw[i]==37)
            skipComment(raw);

        keyStart=i;

        //work out if direct or read ref ( [values] or ref to [values])
        int j2=i;
        byte[] arrayData=raw;

        //may need to add method to PdfObject is others as well as Mask
        boolean isIndirect=raw[i]!=91 && raw[i]!='(' && (PDFkeyInt!=PdfDictionary.Mask && PDFkeyInt!=PdfDictionary.TR &&
                //pdfObject.getObjectType()!=PdfDictionary.ColorSpace &&
                raw[0]!=0); //0 never occurs but we set as flag if called from gotoDest/DefaultActionHandler

        // allow for /Contents null
        if(raw[i]=='n' && raw[i+1]=='u' && raw[i+2]=='l' && raw[i+2]=='l'){
            isIndirect=false;
            elementCount=1;
        }

        //check indirect and not [/DeviceN[/Cyan/Magenta/Yellow/Black]/DeviceCMYK 36 0 R]
        if(isIndirect)
            isIndirect = handleIndirect(endPoint, raw, debugArray);

        if(debugArray && isIndirect)
            System.out.println(padding+"Indirect ref");

        boolean isSingleKey=false,isSingleDirectValue=false; //flag to show points to Single value (ie /FlateDecode)
        boolean isSingleNull=true;
        int endPtr=-1;

        if((raw[i]==47 || raw[i]=='(' || raw[i]=='<' ||
                (raw[i]=='<' && raw[i+1]=='f' && raw[i+2]=='e') && raw[i+3]=='f' && raw[i+4]=='f') &&
                type!=PdfDictionary.VALUE_IS_STRING_ARRAY && PDFkeyInt!=PdfDictionary.TR){ //single value ie /Filter /FlateDecode or (text)

            elementCount=1;
            isSingleKey=true;

            if(debugArray)
                System.out.println(padding+"Direct single value with /");
        }else{

            int endI=-1;//allow for jumping back to single value (ie /Contents 12 0 R )

            if(isIndirect){

                if(debugArray)
                    System.out.println(padding+"------reading data----");

                //allow for indirect to 1 item
                int startI=i;

                if(debugArray)
                    System.out.print(padding+"Indirect object ref=");

                //move cursor to end of ref
                while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62){
                    i++;
                }

                //actual value or first part of ref
                int ref= NumberUtils.parseInt(keyStart, i, raw);

                //move cursor to start of generation
                while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
                    i++;

                // get generation number
                keyStart=i;
                //move cursor to end of reference
                while(raw[i]!=10 && raw[i]!=13 && raw[i]!=32 && raw[i]!=47 && raw[i]!=60 && raw[i]!=62)
                    i++;

                int generation= NumberUtils.parseInt(keyStart, i, raw);

                if(debugFastCode)
                    System.out.print(padding+" ref="+ref+" generation="+generation+ '\n');

                // check R at end of reference and abort if wrong
                //move cursor to start of R
                while(raw[i]==10 || raw[i]==13 || raw[i]==32 || raw[i]==47 || raw[i]==60)
                    i++;

                if(raw[i]!=82) //we are expecting R to end ref
                    throw new RuntimeException(padding+"4. Unexpected value "+(char)raw[i]+" in file - please send to IDRsolutions for analysis");

                if(ignoreRecursion && !alwaysRead){

                    if(debugArray)
                        System.out.println(padding+"Ignore sublevels");
                    return i;
                }

                //read the Dictionary data
                arrayData=objectReader.readObjectAsByteArray(pdfObject, objectReader.isCompressed(ref, generation), ref, generation);

                //allow for data in Linear object not yet loaded
                if(arrayData==null){
                    pdfObject.setFullyResolved(false);

                    if(debugFastCode)
                        System.out.println(padding+"Data not yet loaded");

                    if(LogWriter.isOutput())
                    	LogWriter.writeLog("[Linearized] " + pdfObject.getObjectRefAsString() + " not yet available (14)");

                    return raw.length;
                }

                //lose obj at start and roll onto [
                j2=0;
                while(arrayData[j2]!=91){

                    //allow for % comment
                    if(arrayData[j2]=='%'){
                        while(true){
                            j2++;
                            if(arrayData[j2]==13 || arrayData[j2]==10)
                                break;
                        }
                        while(arrayData[j2]==13 || arrayData[j2]==10)
                            j2++;

                        //roll back as [ may be next char
                        j2--;
                    }

                    //allow for null
                    if(arrayData[j2]=='n' && arrayData[j2+1]=='u' && arrayData[j2+2]=='l' && arrayData[j2+3]=='l')
                        break;

                    if(arrayData[j2]==47){ //allow for value of type  32 0 obj /FlateDecode endob
                        j2--;
                        isSingleDirectValue=true;
                        break;
                    }if ((arrayData[j2]=='<' && arrayData[j2+1]=='<')||
                            ((j2+4<arrayData.length) &&arrayData[j2+3]=='<' && arrayData[j2+4]=='<')){ //also check ahead to pick up [<<
                        endI=i;

                        j2=startI;
                        arrayData=raw;

                        if(debugArray)
                            System.out.println(padding+"Single value, not indirect");

                        break;
                    }

                    j2++;
                }
            }

            if(j2<0) //avoid exception
                j2=0;

            //skip [ and any spaces allow for [[ in recursion
            boolean startFound=false;

            while(arrayData[j2]==10 || arrayData[j2]==13 || arrayData[j2]==32 ||
                    (arrayData[j2]==91 && !startFound)){//(type!=PdfDictionary.VALUE_IS_OBJECT_ARRAY || objectValuesArray==null)))

                if(arrayData[j2]==91)
                    startFound=true;

                j2++;
            }

            //count number of elements
            endPtr=j2;
            boolean charIsSpace,lastCharIsSpace=true,isRecursive ;
            int arrayEnd=arrayData.length;
            if(debugArray)
                System.out.println(padding+"----counting elements----arrayData[endPtr]="+arrayData[endPtr]+" type="+type);

            while(endPtr<arrayEnd && arrayData[endPtr]!=93){

                isRecursive=false;

                //allow for embedded objects
                while(true){

                    if(arrayData[endPtr]=='<' && arrayData[endPtr+1]=='<'){
                        int levels=1;

                        elementCount++;

                        if(debugArray)
                            System.out.println(padding+"Direct value elementCount="+elementCount);

                        while(levels>0){
                            endPtr++;

                            if(arrayData[endPtr]=='<' && arrayData[endPtr+1]=='<'){
                                endPtr++;
                                levels++;
                            }else if(arrayData[endPtr]=='>' && arrayData[endPtr-1]=='>'){
                                endPtr++;
                                levels--;
                            }
                        }

                        if(type==PdfDictionary.VALUE_IS_KEY_ARRAY)
                            endPtr--;

                    }else
                        break;
                }

                //allow for null (not Mixed!)
                if(type!=PdfDictionary.VALUE_IS_MIXED_ARRAY && arrayData[endPtr]=='n' && arrayData[endPtr+1]=='u' &&
                        arrayData[endPtr+2]=='l' && arrayData[endPtr+3]=='l'){

                    //get next legit value and make sure not only value if layer or Order
                    //to handle bum null values in Layers on some files
                    byte nextChar=93;
                    if(PDFkeyInt==PdfDictionary.Layer || PDFkeyInt==PdfDictionary.Order){
                        for(int aa=endPtr+3;aa<arrayData.length;aa++){
                            if(arrayData[aa]==10 || arrayData[aa]==13 || arrayData[aa]==32 || arrayData[aa]==9){
                            }else{
                                nextChar=arrayData[aa];
                                aa=arrayData.length;
                            }
                        }
                    }

                    if(nextChar==93){
                        isSingleNull=true;
                        elementCount=1;
                        break;
                    }else{  //ignore null value
                        isSingleNull=false;
                        //elementCount++;
                        endPtr=endPtr+4;
                        lastCharIsSpace=true;

                        if(debugArray)
                            System.out.println("ignore null");

                        continue;
                    }
                }

                if(isSingleDirectValue && (arrayData[endPtr]==32 || arrayData[endPtr]==13 || arrayData[endPtr]==10))
                    break;

                if(endI!=-1 && endPtr>endI)
                    break;

                if(type==PdfDictionary.VALUE_IS_KEY_ARRAY){
     

                    if(arrayData[endPtr]=='R'  || ((PDFkeyInt==PdfDictionary.TR|| PDFkeyInt==PdfDictionary.Category) && arrayData[endPtr]=='/'  ))
                        elementCount++;

                }else{

                    //handle (string)
                    if(arrayData[endPtr]=='('){
                        elementCount++;

                        if(debugArray)
                            System.out.println(padding+"string");
                        while(true){
                            if(arrayData[endPtr]==')' && !ObjectUtils.isEscaped(arrayData, endPtr))
                                break;

                            endPtr++;

                            lastCharIsSpace=true; //needs to be space for code to work eve if no actual space
                        }
                    }else if(arrayData[endPtr]=='<'){
                        elementCount++;

                        if(debugArray)
                            System.out.println(padding+"direct");
                        while(true){
                            if(arrayData[endPtr]=='>')
                                break;

                            endPtr++;

                            lastCharIsSpace=true; //needs to be space for code to work eve if no actual space
                        }
                    }else if(arrayData[endPtr]==91){ //handle recursion

                        elementCount++;

                        if(debugArray)
                            System.out.println(padding+"recursion");
                        int level=1;

                        while(true){

                            endPtr++;

                            if(endPtr==arrayData.length)
                                break;

                            if(arrayData[endPtr]==93)
                                level--;
                            else if(arrayData[endPtr]==91)
                                level++;

                            if(level==0)
                                break;
                        }

                        isRecursive=true;
                        lastCharIsSpace=true; //needs to be space for code to work eve if no actual space

                    }else{

                        charIsSpace = arrayData[endPtr] == 10 || arrayData[endPtr] == 13 || arrayData[endPtr] == 32 || arrayData[endPtr] == 47;

                        if(lastCharIsSpace && !charIsSpace ){
                            if((type==PdfDictionary.VALUE_IS_MIXED_ARRAY || type==PdfDictionary.VALUE_IS_OBJECT_ARRAY)
                                    && arrayData[endPtr]=='R' && arrayData[endPtr-1]!='/'){ //adjust so returns correct count  /R and  on 12 0 R
                                elementCount--;

                                isRef.put(elementCount - 1,"x");

                                if(debugArray)
                                    System.out.println(padding+"aref "+(char)arrayData[endPtr]);
                            }else
                                elementCount++;

                        }
                        lastCharIsSpace=charIsSpace;
                    }
                }

                //allow for empty array [ ]
                if(!isRecursive && endPtr<arrayEnd && arrayData[endPtr]==93 && type!=PdfDictionary.VALUE_IS_KEY_ARRAY){

                    //get first char
                    int ptr=endPtr-1;
                    while(arrayData[ptr]==13 || arrayData[ptr]==10 || arrayData[ptr]==32)
                        ptr--;

                    if(arrayData[ptr]=='[') //if empty reset
                        elementCount=0;
                    break;
                }

                endPtr++;
            }

            if(debugArray)
                System.out.println(padding+"Number of elements="+elementCount+" rawCount=");

            if(elementCount==0 && debugArray)
                System.out.println(padding+"zero elements found!!!!!!");

        }

        if(ignoreRecursion && !alwaysRead)
            return endPtr;

        //setup the correct array to size
        initObjectArray(elementCount);

        /**
         * read all values and convert
         */
        //if(isSingleNull && arrayData[j2]=='n' && arrayData[j2+1]=='u' &&
        if(arrayData[j2]=='n' && arrayData[j2+1]=='u' &&
                arrayData[j2+2]=='l' && arrayData[j2+3]=='l' && isSingleNull &&
                (type!=PdfDictionary.VALUE_IS_OBJECT_ARRAY || elementCount==1)){

            j2=j2+3;

            if(type==PdfDictionary.VALUE_IS_MIXED_ARRAY)
                mixedValues[currentElement]=null;
            else if(type==PdfDictionary.VALUE_IS_KEY_ARRAY)
                keyValues[currentElement]=null;
            else if(type==PdfDictionary.VALUE_IS_STRING_ARRAY)
                stringValues[currentElement]=null;
            else if(type==PdfDictionary.VALUE_IS_OBJECT_ARRAY)
                objectValues[currentElement]=null;

        }else
            j2 = setValue(ignoreRecursion, raw, pdfObject, PDFkeyInt, isRef, isHexString, debugArray, currentElement, elementCount, j2, arrayData, isSingleKey, endPtr);

        //put cursor in correct place (already there if ref)
        if(!isIndirect)
            i=j2;

        //set value in PdfObject
        if(type==PdfDictionary.VALUE_IS_FLOAT_ARRAY)
            pdfObject.setFloatArray(PDFkeyInt,floatValues);
        else if(type==PdfDictionary.VALUE_IS_INT_ARRAY)
            pdfObject.setIntArray(PDFkeyInt,intValues);
        else if(type==PdfDictionary.VALUE_IS_BOOLEAN_ARRAY)
            pdfObject.setBooleanArray(PDFkeyInt,booleanValues);
        else if(type==PdfDictionary.VALUE_IS_DOUBLE_ARRAY)
            pdfObject.setDoubleArray(PDFkeyInt,doubleValues);
        else if(type==PdfDictionary.VALUE_IS_MIXED_ARRAY)
            pdfObject.setMixedArray(PDFkeyInt,mixedValues);
        else if(type==PdfDictionary.VALUE_IS_KEY_ARRAY)
            setKeyArrayValue(pdfObject, PDFkeyInt, elementCount);
        else if(type==PdfDictionary.VALUE_IS_STRING_ARRAY)
            pdfObject.setStringArray(PDFkeyInt,stringValues);
        else if(type==PdfDictionary.VALUE_IS_OBJECT_ARRAY)
            setObjectArrayValue(pdfObject, PDFkeyInt, objectValuesArray, keyReached, debugArray);

        if(debugArray)
            showValues();

        //roll back so loop works if no spaces
        if(i<raw.length &&(raw[i]==47 || raw[i]==62 || (raw[i]>='0' && raw[i]<='9')))
            i--;

        return i;
    }

    private int setValue(boolean ignoreRecursion, byte[] raw, PdfObject pdfObject, int PDFkeyInt, Map ref, boolean hexString, boolean debugArray, int currentElement, int elementCount, int j2, byte[] arrayData, boolean singleKey, int endPtr) {

        int keyStart;///read values

        while(arrayData[j2]!=93){

            if(endPtr>-1 && j2>=endPtr)
                break;

            //move cursor to start of text
            while(arrayData[j2]==10 || arrayData[j2]==13 || arrayData[j2]==32 || arrayData[j2]==47)
                j2++;

            keyStart=j2;

            if(debugArray)
                System.out.print("j2="+j2+" value="+(char)arrayData[j2]);

            boolean isKey=arrayData[j2-1]=='/';
            boolean isRecursiveValue=false; //flag to show if processed in top part so ignore second part

            //move cursor to end of text
            if(type== PdfDictionary.VALUE_IS_KEY_ARRAY ||
                    ((type==PdfDictionary.VALUE_IS_MIXED_ARRAY || type==PdfDictionary.VALUE_IS_OBJECT_ARRAY) && (ref.containsKey(currentElement)||
                            (PDFkeyInt==PdfDictionary.Order && arrayData[j2]>='0' && arrayData[j2]<='9')||
                            (arrayData[j2]=='<' && arrayData[j2+1]=='<')))){

                if(debugArray)
                    System.out.println("ref currentElement="+currentElement);

                while(arrayData[j2]!='R' && arrayData[j2]!=']'){

                    //allow for embedded object
                    if(arrayData[j2]=='<' && arrayData[j2+1]=='<'){
                        int levels=1;

                        if(debugArray)
                            System.out.println(padding+"Reading Direct value");

                        while(levels>0){
                            j2++;

                            if(arrayData[j2]=='<' && arrayData[j2+1]=='<'){
                                j2++;
                                levels++;
                            }else if(arrayData[j2]=='>' && arrayData[j2+1]=='>'){
                                j2++;
                                levels--;
                            }
                        }
                        break;
                    }

                    if(isKey && PDFkeyInt==PdfDictionary.TR && arrayData[j2+1]==' ')
                        break;

                    j2++;
                }
                j2++;

            }else{

                // handle (string)
                if(arrayData[j2]=='('){

                    keyStart=j2+1;
                    while(true){
                        if(arrayData[j2]==')' && !ObjectUtils.isEscaped(arrayData, j2))
                            break;

                        j2++;
                    }

                    hexString =false;
                
                }else if(arrayData[j2]=='[' && type==PdfDictionary.VALUE_IS_MIXED_ARRAY && PDFkeyInt==PdfDictionary.Names){ // [59 0 R /XYZ null 711 null ]

                    keyStart=j2;
                    while(true){
                        if(arrayData[j2]==']')
                            break;

                        j2++;

                    }

                    //include end bracket
                    j2++;

                }else if(arrayData[j2]=='<'){

                    hexString =true;
                    keyStart=j2+1;
                    while(true){
                        if(arrayData[j2]=='>')
                            break;

                        if(arrayData[j2]=='/')
                            hexString =false;

                        j2++;

                    }

                }else if(arrayData[j2]==91 && type==PdfDictionary.VALUE_IS_OBJECT_ARRAY){

                    //find end
                    int j3=j2+1;
                    int level=1;

                    while(true){

                        j3++;

                        if(j3==arrayData.length)
                            break;

                        if(arrayData[j3]==93)
                            level--;
                        else if(arrayData[j3]==91)
                            level++;

                        if(level==0)
                            break;
                    }
                    j3++;

                    if(debugArray)
                        padding = padding +"   ";

                    ArrayDecoder objDecoder=new ArrayDecoder(objectReader, j2, j3, type, objectValues, currentElement);
                    j2=objDecoder.readArray(ignoreRecursion,  arrayData, pdfObject, PDFkeyInt) ;

                    if(debugArray){
                        int len=padding.length();

                        if(len>3)
                            padding = padding.substring(0,len-3);
                    }

                    if(arrayData[j2]!='[')
                        j2++;

                    isRecursiveValue=true;

                    while(j2<arrayData.length && arrayData[j2]==']')
                        j2++;

                }else if(!isKey && elementCount-currentElement==1 && type==PdfDictionary.VALUE_IS_MIXED_ARRAY){ //if last value just read to end in case 1 0 R

                    while(arrayData[j2]!=93 && arrayData[j2]!=47){

                        if(arrayData[j2]==62 && arrayData[j2+1]==62)
                            break;

                        j2++;
                    }
                }else if(type==PdfDictionary.VALUE_IS_OBJECT_ARRAY && arrayData[j2]=='n' && arrayData[j2+1]=='u' && arrayData[j2+2]=='l' && arrayData[j2+3]=='l'){
                    j2=j2+4;
                    objectValues[currentElement]=null;
                    currentElement++;
                    continue;

                }else{
                    while(arrayData[j2]!=10 && arrayData[j2]!=13 && arrayData[j2]!=32 && arrayData[j2]!=93 && arrayData[j2]!=47){
                        if(arrayData[j2]==62 && arrayData[j2+1]==62)
                            break;

                        j2++;

                        if(j2==arrayData.length)
                            break;
                    }
                }
            }

            //actual value or first part of ref
            if(type==PdfDictionary.VALUE_IS_FLOAT_ARRAY)
                floatValues[currentElement]= NumberUtils.parseFloat(keyStart, j2, arrayData);
            else if(type==PdfDictionary.VALUE_IS_INT_ARRAY)
                intValues[currentElement]= NumberUtils.parseInt(keyStart, j2, arrayData);
            else if(type==PdfDictionary.VALUE_IS_BOOLEAN_ARRAY){
                if(raw[keyStart]=='t' && raw[keyStart+1]=='r' && raw[keyStart+2]=='u' && raw[keyStart+3]=='e')
                    booleanValues[currentElement]=true; //(false id default if not set)
            }else if(type==PdfDictionary.VALUE_IS_DOUBLE_ARRAY)
                doubleValues[currentElement]= NumberUtils.parseFloat(keyStart, j2, arrayData);
            else if(!isRecursiveValue)
                j2 = setObjectArrayValue(pdfObject, PDFkeyInt, hexString, debugArray, currentElement, elementCount, j2, arrayData, singleKey, keyStart);

            currentElement++;

            if(debugArray)
                System.out.println(padding+"roll onto ==================================>"+currentElement+ '/' +elementCount);
            if(currentElement==elementCount)
                break;
        }
        return j2;
    }

    private int setObjectArrayValue(PdfObject pdfObject, int PDFkeyInt, boolean hexString, boolean debugArray, int currentElement, int elementCount, int j2, byte[] arrayData, boolean singleKey, int keyStart) {

        //include / so we can differentiate /9 and 9
        if(keyStart>0 && arrayData[keyStart-1]==47)
            keyStart--;

        //lose any spurious [
        if(keyStart>0 && arrayData[keyStart]=='[' && PDFkeyInt!= PdfDictionary.Names)
            keyStart++;

        //lose any nulls
        if(PDFkeyInt==PdfDictionary.Order || PDFkeyInt==PdfDictionary.Layer){


            while(arrayData[keyStart]=='n' && arrayData[keyStart+1]=='u' && arrayData[keyStart+2]=='l' && arrayData[keyStart+3]=='l' ){
                keyStart=keyStart+4;

                //lose any spurious chars at start
                while(keyStart>=0 && (arrayData[keyStart]==' ' || arrayData[keyStart]==10 || arrayData[keyStart]==13 || arrayData[keyStart]==9))
                    keyStart++;
            }

        }

        //lose any spurious chars at start
        while(keyStart>=0 && (arrayData[keyStart]==' ' || arrayData[keyStart]==10 || arrayData[keyStart]==13 || arrayData[keyStart]==9))
            keyStart++;

        byte[] newValues= ObjectUtils.readEscapedValue(j2, arrayData, keyStart, PDFkeyInt == PdfDictionary.ID);

        if(debugArray)
            System.out.println(padding+"<1.Element -----"+currentElement+ '/' +elementCount+"( j2="+j2+" ) value="+new String(newValues)+ '<');

        if(j2==arrayData.length){
            //ignore
        }else if(arrayData[j2]=='>'){
            j2++;
            //roll past ) and decrypt if needed
        }else if(arrayData[j2]==')'){
            j2++;

            try {
                if(!pdfObject.isInCompressedStream() && decryption!=null)
                    newValues=decryption.decrypt(newValues,pdfObject.getObjectRefAsString(), false,null, false,false);
            } catch (PdfSecurityException e) {
                //tell user and log
                if(LogWriter.isOutput())
                    LogWriter.writeLog("Exception: "+e.getMessage());
            }

            //convert Strings in Order now
            if(PDFkeyInt==PdfDictionary.Order)
                newValues= StringUtils.toBytes(StringUtils.getTextString(newValues, false));
        }

        //update pointer if needed
        if(singleKey)
            i=j2;

        if(type==PdfDictionary.VALUE_IS_MIXED_ARRAY){
            mixedValues[currentElement]=newValues;
        }else if(type==PdfDictionary.VALUE_IS_KEY_ARRAY){
            keyValues[currentElement]= ObjectUtils.convertReturnsToSpaces(newValues);
        }else if(type==PdfDictionary.VALUE_IS_STRING_ARRAY){
            if(hexString){
                //convert to byte values
                String nextValue;
                String str=new String(newValues);
                byte[] IDbytes=new byte[newValues.length/2];
                for(int ii=0;ii<newValues.length;ii=ii+2){

                    if(ii+2>newValues.length)
                        continue;
                    
                    /*String array is a series of byte values.
                    * If the byte values has a \n in the middle we should ignore it.
                    * (customer-June2011/payam.pdf)
                    */
                    if(str.charAt(ii)=='\n'){
                    	ii++;
                    }
                    
                    nextValue=str.substring(ii,ii+2);
                    IDbytes[ii/2]=(byte)Integer.parseInt(nextValue,16);

                }
                newValues=IDbytes;
            }

            stringValues[currentElement]=newValues;

        }else if(type==PdfDictionary.VALUE_IS_OBJECT_ARRAY){
            objectValues[currentElement]=(newValues);

            if(debugArray)
                System.out.println(padding+"objectValues["+currentElement+"]="+ Arrays.toString(objectValues)+ ' ');
        }
        return j2;
    }

    private void initObjectArray(int elementCount) {
        if(type== PdfDictionary.VALUE_IS_FLOAT_ARRAY)
            floatValues=new float[elementCount];
        else if(type==PdfDictionary.VALUE_IS_INT_ARRAY)
            intValues=new int[elementCount];
        else if(type==PdfDictionary.VALUE_IS_BOOLEAN_ARRAY)
            booleanValues=new boolean[elementCount];
        else if(type==PdfDictionary.VALUE_IS_DOUBLE_ARRAY)
            doubleValues=new double[elementCount];
        else if(type==PdfDictionary.VALUE_IS_MIXED_ARRAY)
            mixedValues=new byte[elementCount][];
        else if(type==PdfDictionary.VALUE_IS_KEY_ARRAY)
            keyValues=new byte[elementCount][];
        else if(type==PdfDictionary.VALUE_IS_STRING_ARRAY)
            stringValues=new byte[elementCount][];
        else if(type==PdfDictionary.VALUE_IS_OBJECT_ARRAY)
            objectValues=new Object[elementCount];
    }

    private boolean handleIndirect(int endPoint, byte[] raw, boolean debugArray){

        boolean indirect=true;

        //find next value and make sure not /
        int aa=i, length=raw.length;

        while(raw[aa]!=93 ){
            aa++;

            //allow for ref (ie 7 0 R)
            if(aa>=endPoint || aa>=length)
                break;

            if(raw[aa]=='R' && (raw[aa-1]==32 || raw[aa-1]==10 || raw[aa-1]==13))
                break;
            else if(raw[aa]=='>' && raw[aa-1]=='>'){
                indirect =false;
                if(debugArray )
                    System.out.println(padding+"1. rejected as indirect ref");

                break;
            }else if(raw[aa]==47){
                indirect =false;
                if(debugArray )
                    System.out.println(padding+"2. rejected as indirect ref - starts with /");

                break;
            }
        }
        return indirect;
    }

    private void skipComment(byte[] raw) {
        while(raw[i]!=10 && raw[i]!=13){
            i++;
        }

        //move cursor to start of text
        while(raw[i]==10 || raw[i]==13 || raw[i]==32)
            i++;
    }

    private void setKeyArrayValue(PdfObject pdfObject, int PDFkeyInt, int elementCount) {

        if(type== PdfDictionary.VALUE_IS_KEY_ARRAY && elementCount==1 && PDFkeyInt==PdfDictionary.Annots){//allow for indirect on Annots

            byte[] objData=keyValues[0];

            //allow for null
            if(objData!=null){

                int size=objData.length;
                if(objData[size-1]=='R'){

                    PdfObject obj=new PdfObject(new String(objData));
                    byte[] newData=objectReader.readObjectData(obj);

                    if(newData!=null){

                        int jj=0,newLen=newData.length;
                        boolean hasArray=false;
                        while(jj<newLen){
                            jj++;

                            if(jj==newData.length)
                                break;

                            if(newData[jj]=='['){
                                hasArray=true;
                                break;
                            }else if(newData[jj-1]=='<' && newData[jj]=='<'){
                                hasArray=false;
                                break;
                            }
                        }

                        if(hasArray){
                            ArrayDecoder objDecoder=new ArrayDecoder(objectReader, jj, newLen, PdfDictionary.VALUE_IS_KEY_ARRAY);
                            objDecoder.readArray(false, newData, pdfObject, PDFkeyInt);
                        }else
                            pdfObject.setKeyArray(PDFkeyInt,keyValues);
                    }
                }
            }
        }else
            pdfObject.setKeyArray(PDFkeyInt,keyValues);
    }

    private void setObjectArrayValue(PdfObject pdfObject, int PDFkeyInt, Object[] objectValuesArray, int keyReached, boolean debugArray) {
        //allow for indirect order
        if(PDFkeyInt== PdfDictionary.Order && objectValues!=null && objectValues.length==1 && objectValues[0] instanceof byte[]){

            byte[] objData=(byte[]) objectValues[0];
            int size=objData.length;
            if(objData[size-1]=='R'){

                PdfObject obj=new OCObject(new String(objData));
                byte[] newData=objectReader.readObjectData(obj);

                int jj=0,newLen=newData.length;
                boolean hasArray=false;
                while(jj<newLen){
                    jj++;

                    if(jj==newData.length)
                        break;

                    if(newData[jj]=='['){
                        hasArray=true;
                        break;
                    }
                }

                if(hasArray){
                    ArrayDecoder objDecoder=new ArrayDecoder(objectReader, jj, newLen, PdfDictionary.VALUE_IS_OBJECT_ARRAY);
                    objDecoder.readArray(false, newData, pdfObject, PDFkeyInt);
                }
                objectValues=null;

            }
        }

        if(objectValuesArray!=null){
            objectValuesArray[keyReached]=objectValues;

            if(debugArray)
                System.out.println(padding+"set Object objectValuesArray["+keyReached+"]="+ Arrays.toString(objectValues));

        }else if(objectValues!=null){
            pdfObject.setObjectArray(PDFkeyInt,objectValues);

            if(debugArray)
                System.out.println(padding+PDFkeyInt+" set Object value="+Arrays.toString(objectValues));
        }
    }

    /**
     * used for debugging
     */
    private void showValues() {

        String values="[";

        if(type== PdfDictionary.VALUE_IS_FLOAT_ARRAY){
            for (float floatValue : floatValues){
                values = values + floatValue + ' ';
            }

        }else if(type==PdfDictionary.VALUE_IS_DOUBLE_ARRAY){
            for (double doubleValue : doubleValues){
                values = values + doubleValue + ' ';
            }

        }else if(type==PdfDictionary.VALUE_IS_INT_ARRAY){
            for (int intValue : intValues){
                values = values + intValue + ' ';
            }

        }else if(type==PdfDictionary.VALUE_IS_BOOLEAN_ARRAY){
            for (boolean booleanValue : booleanValues){
                values = values + booleanValue + ' ';
            }

        }else if(type==PdfDictionary.VALUE_IS_MIXED_ARRAY){
            for (byte[] mixedValue : mixedValues){
                if (mixedValue == null)
                    values = values + "null ";
                else
                    values = values + new String(mixedValue) + ' ';
            }

        }else if(type==PdfDictionary.VALUE_IS_KEY_ARRAY){
            for (byte[] keyValue : keyValues) {
                if (keyValue == null)
                    values = values + "null ";
                else
                    values = values + new String(keyValue) + ' ';
            }
        }else if(type==PdfDictionary.VALUE_IS_STRING_ARRAY){
            for (byte[] stringValue : stringValues) {
                if (stringValue == null)
                    values = values + "null ";
                else
                    values = values + new String(stringValue) + ' ';
            }
        }else if(type==PdfDictionary.VALUE_IS_OBJECT_ARRAY){
            values = ObjectUtils.showMixedValuesAsString(objectValues, "");
        }

        values=values+" ]";

        System.out.println(padding+"values="+values);
    }

}
