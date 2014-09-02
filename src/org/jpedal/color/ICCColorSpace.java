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
 * ICCColorSpace.java
 * ---------------
 */
package org.jpedal.color;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.jpedal.exception.PdfException;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;


/**
 * handle ICCColorSpace
 */
public class ICCColorSpace
        extends GenericColorSpace {

    //cache values to speed up translation
    private int[] a1,b1,c1;

    private Map cache=new HashMap();

    boolean isCached=false;

    /**
     * reset any defaults if reused
     */
    public void reset(){

        super.reset();

        isConverted=false;

        //set cache to -1 as flag
//		a1=new int[256];
//		b1=new int[256];
//		c1=new int[256];
//
//		for(int i=0;i<256;i++){
//			a1[i]=-1;
//			b1[i]=-1;
//			c1[i]=-1;
//
//		}
//
//		cache.clear();

        isCached=true;
    }


    public ICCColorSpace(PdfObject colorSpace) {

        //set cache to -1 as flag
        a1=new int[256];
        b1=new int[256];
        c1=new int[256];

        for(int i=0;i<256;i++){
            a1[i]=-1;
            b1[i]=-1;
            c1[i]=-1;

        }

        value = ColorSpaces.ICC;
        cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);

        byte[] icc_data=colorSpace.getDecodedStream();

        if (icc_data == null){

            if(LogWriter.isOutput())
                LogWriter.writeLog("Error in ICC data");
        }else {
            try{
                cs = new ICC_ColorSpace(ICC_Profile.getInstance(icc_data));
            }catch(Exception e){
                if(LogWriter.isOutput())
                    LogWriter.writeLog("[PDF] Problem "+e.getMessage()+" with ICC data ");
                failed=true;
            }
        }

        componentCount=cs.getNumComponents();
    }


    /**
     * set color (in terms of rgb)
     */
    final public void setColor(String[] number_values,int items) {

        //if(isCached)
        //	System.out.println("BsetColor "+size);

        float[] colValues=new float[items];

        for(int ii=0;ii<items;ii++)
            colValues[ii]=Float.parseFloat(number_values[ii]);

        setColor(colValues,items);
    }

    /**set color*/
    final public void setColor(float[] operand,int size) {


        //if(isCached)
        //	System.out.println("setColor "+size);

        float[] values=new float[size];
        int[] lookup=new int[size];

        rawValues=new float[size];

        for(int i=0;i<size;i++){
            float val=operand[i];

            rawValues[i]=val;

            values[i]=val;
            if(val>1)
                lookup[i]=(int)(val);
            else
                lookup[i]=(int)(val*255);

        }

        if(size==3 && (a1[lookup[0]]!=-1) &&
                (b1[lookup[1]]!=-1)&&(c1[lookup[2]]!=-1))
        {
            currentColor=new PdfColor(a1[lookup[0]],b1[lookup[1]],c1[lookup[2]]);
            //System.out.println("cached "+operand[0]+" "+operand[1]+" "+operand[2]+" "+this);

        }else if(size==4 && cache.get((lookup[0] << 24) + (lookup[1] << 16) + (lookup[2] << 8) + lookup[3])!=null){

            Object val=cache.get((lookup[0] << 24) + (lookup[1] << 16) + (lookup[2] << 8) + lookup[3]);
            int raw = (Integer) val;
            int r = ((raw >> 16) & 255);
            int g = ((raw >> 8) & 255);
            int b = ((raw) & 255);

            currentColor=new PdfColor(r,g,b);

        }else{

            try{

                values=cs.toRGB(values);

            }catch(Exception ee){
                //file with invalid values appears to work if we just replace
                float[] newValues={values[0],values[0],values[0]};
                values=newValues;
            }
            currentColor=new PdfColor(values[0],values[1],values[2]);

            if(size==3){
                a1[lookup[0]]=(int)(values[0]*255);
                b1[lookup[1]]=(int)(values[1]*255);
                c1[lookup[2]]=(int)(values[2]*255);


            }else if(size==4){ //not used except as flag

                int raw = ((int)(values[0]*255) << 16) +
                        ((int)(values[1]*255) << 8) +
                        (int)(values[2]*255);

                //store values in cache
                cache.put((lookup[0] << 24) + (lookup[1] << 16) + (lookup[2] << 8) + lookup[3], raw);

            }
        }
    }

    /**
     * convert Index to RGB
     */
    public byte[] convertIndexToRGB(byte[] data){

        isConverted=true;

        if(componentCount==4)
            return convert4Index(data);
        else
            return data;

    }

    /**
     * <p>
     * Convert DCT encoded image bytestream to sRGB
     * </p>
     * <p>
     * It uses the internal Java classes and the Adobe icm to convert CMYK and
     * YCbCr-Alpha - the data is still DCT encoded.
     * </p>
     * <p>
     * The Sun class JPEGDecodeParam.java is worth examining because it contains
     * lots of interesting comments
     * </p>
     * <p>
     * I tried just using the new IOImage.read() but on type 3 images, all my
     * clipping code stopped working so I am still using 1.3
     * </p>
     */
    public BufferedImage JPEGToRGBImage(
            byte[] data,int w,int h,float[] decodeArray,int pX,int pY, boolean arrayInverted, PdfObject XObject) {

        if(data.length>9 && data[6] == 'J' && data[7] == 'F' && data[8] == 'I' && data[9] == 'F'){
            return nonRGBJPEGToRGBImage(data,w,h, null,pX,pY);
        }else
            return algorithmicICCToRGB(data,w,h,false,pX,pY,decodeArray);

    }


    /**
     * convert byte[] datastream JPEG to an image in RGB
     * @throws org.jpedal.exception.PdfException
     */
    public BufferedImage  JPEG2000ToRGBImage(byte[] data,int w,int h,float[] decodeArray,
                                             int pX,int pY) throws PdfException {

        byte[] index=this.getIndexedMap();

        if(cs.getNumComponents()==3 || index!=null)
            return super.JPEG2000ToRGBImage(data, w, h, decodeArray, pX, pY);

        BufferedImage image;

        ByteArrayInputStream in;

        Raster ras;

        try {
            in = new ByteArrayInputStream(data);

            ImageReader iir = ImageIO.getImageReadersByFormatName("JPEG2000").next();
            ImageInputStream iin = ImageIO.createImageInputStream(in);


            iir.setInput(iin, true);

            /**
             * indexes are a completely different game
             * CMYK has 4 color components (C,M,Y,K) for each pixel
             * indexed has 1 component pointing to value in lookup table
             * so need a totally different approach
             */


            if(index!=null){

                //make it RGB
                if(!isIndexConverted()){
                    index=convertIndexToRGB(index);
                }

                //get data for image (its an index so just refers to color numbers
                RenderedImage renderimage = iir.readAsRenderedImage(0,iir.getDefaultReadParam());
                ras=renderimage.getData();

                //and build a standard rgb image
                ColorModel cm=new IndexColorModel(8, index.length/3, index, 0, false);
                image = new BufferedImage(cm, ras.createCompatibleWritableRaster(), false, null);

                //downsample to reduce size if huge
                image=cleanupImage(image,pX,pY, image.getType());

            }else{ //non-indexed routine

                //This works except image is wrong so we read and convert
                image=iir.read(0);
                ras=image.getRaster();

                //apply if set
                if(decodeArray!=null){

                    if((decodeArray.length==6 && decodeArray[0]==1f && decodeArray[1]==0f &&
                            decodeArray[2]==1f && decodeArray[3]==0f &&
                            decodeArray[4]==1f && decodeArray[5]==0f )||
                            (decodeArray.length>2 &&
                                    decodeArray[0]==1f && decodeArray[1]==0)){

                        DataBuffer buf=ras.getDataBuffer();

                        int count=buf.getSize();

                        for(int ii=0;ii<count;ii++)
                            buf.setElem(ii,255-buf.getElem(ii));

                    }else if(decodeArray.length==6 &&
                            decodeArray[0]==0f && decodeArray[1]==1f &&
                            decodeArray[2]==0f && decodeArray[3]==1f &&
                            decodeArray[4]==0f && decodeArray[5]==1f){
                    }else if(decodeArray!=null && decodeArray.length>0){
                    }
                }

                ras=cleanupRaster(ras,pX,pY,4);
                w=ras.getWidth();
                h=ras.getHeight();

                //generate the rgb image
                WritableRaster rgbRaster;
                if(image.getType()==13){ //indexed variant
                    rgbRaster = ColorSpaceConvertor.createCompatibleWritableRaaster(image.getColorModel(), w, h);
                    //me does not allow colorconvertop
                    CSToRGB = new ColorConvertOp(cs, image.getColorModel().getColorSpace(), ColorSpaces.hints);
                    image =new BufferedImage(w,h,image.getType());
                }else{

                    //me does not allow colorconvertop
                    if(image.getType()!=10){

                        if(CSToRGB==null)
                            initCMYKColorspace();
                        rgbRaster=ColorSpaceConvertor.createCompatibleWritableRaaster(rgbModel,w, h);

                        CSToRGB = new ColorConvertOp(cs, rgbCS, ColorSpaces.hints);
                        CSToRGB.filter(ras, rgbRaster);

                        image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
                    }else{ //some 4 channel cases will not work so we ignore and use direct image
                        rgbRaster=null;
                    }
                }

                if(rgbRaster!=null){
                    image.setData(rgbRaster);
                }
            }

            iir.dispose();
            iin.close();
            in.close();

            //image=cleanupImage(image,pX,pY);
            //image= ColorSpaceConvertor.convertToRGB(image);

        } catch (Exception ee) {
            if(LogWriter.isOutput())
                LogWriter.writeLog("Problem reading JPEG 2000: " + ee);

            ee.printStackTrace();
            throw new PdfException("Exception "+ee+" with JPEG2000 image - please ensure imageio.jar (see http://www.idrsolutions.com/additional-jars/) on classpath");
        } catch (Error ee2) {
            ee2.printStackTrace();

            if(LogWriter.isOutput())
                LogWriter.writeLog("Problem reading JPEG 2000 with error " + ee2);

            throw new PdfException("Error with JPEG2000 image - please ensure imageio.jar (see http://www.idrsolutions.com/additional-jars/) on classpath");
        }

        return image;
    }


    private BufferedImage algorithmicICCToRGB(
            byte[] data, int w, int h,boolean debug, int pX, int pY, float[] decodeArray) {

        BufferedImage image = null;

        ImageReader iir=null;
        @SuppressWarnings("UnusedAssignment") ImageInputStream iin=null;

        ByteArrayInputStream in = new ByteArrayInputStream(data);

        try{

            //suggestion from Carol
            Iterator iterator = ImageIO.getImageReadersByFormatName("JPEG");

            while (iterator.hasNext())
            {
                Object o = iterator.next();
                iir = (ImageReader) o;
                if (iir.canReadRaster())
                    break;
            }

            ImageIO.setUseCache(false);

            iin = ImageIO.createImageInputStream((in));
            iir.setInput(iin, true);   //new MemoryCacheImageInputStream(in));

            Raster ras=iir.readRaster(0,null);

            //some images need this
            if(iir.getRawImageType(0)==null || alternative==-1)
                return nonRGBJPEGToRGBImage(data,w,h, decodeArray,pX,pY);

            ras=cleanupRaster(ras,pX,pY,componentCount);
            w=ras.getWidth();
            h=ras.getHeight();

            byte[] new_data = new byte[w * h * 3];

            //reuse variable
            data=((DataBufferByte)ras.getDataBuffer()).getData();

            int pixelCount = w * h*3;
            float lastR=0,lastG=0,lastB=0;
            int pixelReached = 0;
            float lastIn1=-1,lastIn2=-1,lastIn3=-1;

            for (int i = 0; i < pixelCount; i = i + 3) {

                float in1 = ((data[i] & 255))/255f;
                float in2 = ((data[1+i] & 255))/255f;
                float in3 = ((data[2+i] & 255))/255f;

                float[] outputValues;
                if((lastIn1==in1)&&(lastIn2==in2)&&(lastIn3==in3)){
                    //use existing values
                }else{//work out new

                    if (debug)
                        System.out.println(in1+" "+in2+ ' ' +in3);

                    float[] inputValues={in1,in2,in3};

                    outputValues=cs.toRGB(inputValues);
                    //outputValues=inputValues;

                    //reset values
                    lastR=(outputValues[0]*255);
                    lastG=(outputValues[1]*255);
                    lastB=(outputValues[2]*255);

                    lastIn1=in1;
                    lastIn2=in2;
                    lastIn3=in3;
                }

                new_data[pixelReached++] =(byte) lastR;
                new_data[pixelReached++] = (byte)lastG;
                new_data[pixelReached++] = (byte)lastB;

            }

            int[] bands = {0,1,2};

            DataBuffer db = new DataBufferByte(new_data, new_data.length);
            image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);

            Raster raster =Raster.createInterleavedRaster(db,w,h,w * 3,3,bands,null);
            image.setData(raster);


        }catch(Exception e){

            if(LogWriter.isOutput())
                LogWriter.writeLog("Problem with color conversion");


        }finally{

            try {
                in.close();
                iir.dispose();
                iin.close();
            } catch (Exception ee) {

                if(LogWriter.isOutput())
                    LogWriter.writeLog("Problem closing  " + ee);
            }
        }
        return image;
    }
}
