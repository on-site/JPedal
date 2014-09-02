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
 * RotatedTexturePaint.java
 * ---------------
 */
package org.jpedal.color;

import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Matrix;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.io.IOException;

/**
 * draws rotated object held in DynamicVector renderer onto screen to produce rotated pattern fill
 */
public class RotatedTexturePaint implements PdfPaint {

    /**copy of raw tile if rotated*/
    DynamicVectorRenderer patternOnTile =null;

    private float[][] matrix;

    private float xStep,yStep,offsetXOnCanvas,offsetYOnCanvas,tileXoffset,tileYoffset;

    private AffineTransform imageScale;

    boolean cacheToTile,isSideways,isSkewed=false,isUpsideDown=false, isRotated;

    boolean debug = false;

    //used to debug
    BufferedImage img;
    PdfObject patternObj=null;

    /**
     * This just sets up the variables
     * @param patternObj
     * @param cacheToTile - flag to show which version of code used
     * @param patternOnTile - the Java class containing the Java2D instructions to draw the tessalating pattern
     * @param matrix - the image size, rotaion and location
     * @param xStep - the repeating size
     * @param yStep - the repeating size
     * @param offsetXOnCanvas
     * @param offsetYOnCanvas
     * @param imageScale
     */
    public RotatedTexturePaint(boolean isSideways, float[][] rawMatrix, PdfObject patternObj, boolean cacheToTile,
                               DynamicVectorRenderer patternOnTile, float[][] matrix, float xStep, float yStep,
                               float offsetXOnCanvas, float offsetYOnCanvas, AffineTransform imageScale) {

        /**
         * debugging code
         */

    	this.patternObj = patternObj;
        //debug=true;
       // debug=patternObj.getObjectRefAsString().equals("22 0 R");
        //assign values to local copies
        
        this.isSideways=isSideways;

        this.isSkewed=rawMatrix!=null && rawMatrix[0][0]>0 &&  rawMatrix[0][1]<0  && rawMatrix[1][0]>0 &&  rawMatrix[1][1]>0;

        this.cacheToTile = cacheToTile;
        this.patternOnTile = patternOnTile;

        this.matrix = matrix; //contains any rotation and scaling of pattern cell
        this.xStep = xStep; //horizontal width of repeating pattern
        this.yStep = yStep; //vertical width of repeating pattern
        this.offsetXOnCanvas =  offsetXOnCanvas;//x offset so pattern starts from right place
        this.offsetYOnCanvas =offsetYOnCanvas; //y offset so pattern starts from right place

        //potential scaling to apply to pattern cell
        this.imageScale = imageScale;

        if(debug){
            System.out.println("=======PatternObj="+ patternObj.getObjectRefAsString());
            System.out.println("isSideways="+isSideways+" isSkewed="+isSkewed);
            System.out.println("cacheToTile="+cacheToTile);
            System.out.println("patternOnTile="+patternOnTile);
            System.out.println("xStep="+xStep+" yStep="+yStep);
            System.out.println("offsetXOnCanvas="+offsetXOnCanvas+" offsetYOnCanvas="+offsetYOnCanvas);
            System.out.println("imageScale="+imageScale);

            Matrix.show(matrix);

        }

        //factor in any rotation into numbers
        //(ie if the tile is 'turned' we need to draw it in
        //a slightly different position
        if (matrix[0][0] != 0 && matrix[1][1] != 0) {
            tileXoffset = xStep * matrix[0][1];
            tileYoffset = Math.abs(yStep * matrix[1][0]);

            if(debug)
                System.out.println("set tileXoffset="+tileXoffset+" tileYoffset="+tileYoffset);
        }
    }

    /**
     * called to fill in the whole defined by db/ub with repeating pattern
     * @param cm
     * @param db
     * @param ub
     * @param xform
     * @param hints
     * @return
     */
    public PaintContext createContext(ColorModel cm, Rectangle db, Rectangle2D ub,
                                      AffineTransform xform, RenderingHints hints) {

        //debug=ub.getBounds().y<250 && ub.getBounds().y>240 && ub.getBounds().x>100 && ub.getBounds().x<140;
        //if(debug)
        //System.out.println(ub.getBounds());

        Graphics2D debugG2=null;
        AffineTransform debugAf2 = null;

        TexturePaint rotatedPaint;

        //create each rotated as single huge panel to fit gap as workaround to java

        //workout required size
        int w=(int)(ub.getWidth());
        int h=(int)(ub.getHeight());

        if(debug)
            System.out.println("area to fill w="+w+" h="+h);

        /**
         * create an image of size to fill gap and
         * create G2 object to draw onto
         */
        BufferedImage wholeImage =new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = wholeImage.createGraphics();
        AffineTransform defaultAf2=g2.getTransform();
        if(debug){
            img=new BufferedImage(1000,1000,BufferedImage.TYPE_INT_RGB);

            debugG2=img.createGraphics();

            debugG2.translate(ub.getX(),ub.getY());
            debugG2.setPaint(Color.GREEN);
            for(int i=-1000;i<1000; i=i+50){
            debugG2.drawLine(-1000, i, 1000, i);
            debugG2.drawLine(i,-1000,i,1000);
            }
            debugG2.setPaint(Color.WHITE);
            debugG2.drawRect(0, 0, (int)ub.getWidth(), (int)ub.getHeight());

            debugAf2=debugG2.getTransform();
        }

        /**
         * work out actual rotated shape values
         * offX, offY are the actual offset needed on the repeating cell
         * to appear in the correct place
         */
        float offX,offY,rotatedWidth,rotatedHeight;

        if(isSkewed){
            rotatedWidth=(xStep *matrix[0][0])+(yStep * matrix[1][0]);
            rotatedHeight=(yStep *matrix[1][1])-(xStep *matrix[0][1]);
        }else{
            rotatedWidth=(xStep *matrix[0][0])-(yStep *matrix[1][0]);

            if(matrix[1][1]>0 && matrix[0][1]>0)
                rotatedHeight=(yStep *matrix[1][1]);
            else
                rotatedHeight=-(yStep *matrix[1][1])-(xStep *matrix[0][1]);
        }

        float shapeW=ub.getBounds().width;
        float shapeH=ub.getBounds().height;

        //number of rows needed to fill pattern
        int numberOfRows =(int)((shapeH/rotatedHeight));

        if(debug)
                System.out.println(numberOfRows+" shapeW="+shapeW+" shapeH="+shapeH+" rotatedWidth="+rotatedWidth+" rotatedHeight="+rotatedHeight);

        /**
         * workout offsets needed for various special cases
         */

        if(numberOfRows >1){ //multiple rows

            offX=(shapeW-(rotatedHeight*(numberOfRows)));
            offY=5-(shapeH-(rotatedWidth* numberOfRows));

            if(debug)
                System.out.println("Multiple Rows OffsetX="+offX+" offsetY="+offY+" numberOfRows"+numberOfRows+" rotatedWidth="+rotatedWidth+" shapeH="+shapeH);

        }else if(rotatedHeight>shapeW){ //one row, wholeImage is wider than gap

            offX=rotatedHeight-shapeW;
            offY=shapeH-rotatedWidth;

            if(debug)
                System.out.println("rotatedHeight>shapeW OffsetX="+offX+" offsetY="+offY);
        }else{ //general case
            offX=(shapeH-rotatedHeight);
            offY=(shapeW-rotatedWidth);

            if(debug)
                System.out.println("general case OffsetX="+offX+" offsetY="+offY);
        }

        /**
         * allow for tile not draw from 0,0 into gap
         * (maybe we actually have a 2x2 tile filing a 6x6 gap which needs to be drawn in
         * 2x1
         */
        //the actual cell dimensions ***** tile w and tile h its dimensions
        //if rotated it is horizontal/vertical surrounding area
        Rectangle actualTileRect= patternOnTile.getOccupiedArea().getBounds();

        int tileW,tileH;
        int patternXOffsetOnTile=0,patternYOffsetOnTile=0; //offset to where we draw the pattern inside the tile
        if(actualTileRect.x<0){
            tileW=actualTileRect.width-actualTileRect.x;
            patternXOffsetOnTile=actualTileRect.x;
        }else
            tileW=actualTileRect.width+actualTileRect.x;

        if(actualTileRect.y<0){
            tileH=actualTileRect.height-actualTileRect.y;
            patternYOffsetOnTile=actualTileRect.y;
        }else
            tileH=actualTileRect.height+actualTileRect.y;

        /**
         * buffer onto Tile
         */
        BufferedImage tileImg=null;

        //turn the tile once
        if(isSideways){

            imageScale=new AffineTransform();
            imageScale.scale(-1, 1);

            imageScale.translate(-(offsetXOnCanvas/(xform.getScaleX())),0);

            if(debug)
                System.out.println("isSideWay imageScale="+imageScale);

        }else if(matrix[0][0] >= 0 && matrix[1][0] >= 0 && matrix[0][1] <= 0 && matrix[1][1] >= 0){ //fix for LHS on shirts file

            imageScale=new AffineTransform();
            imageScale.scale(-1,-1);
            if(!isSkewed)
                imageScale.translate(0,patternOnTile.getOccupiedArea().height-patternOnTile.getOccupiedArea().y);//imgTranslateVarX
            else
                imageScale.translate(0,-(patternOnTile.getOccupiedArea().height-patternOnTile.getOccupiedArea().y));//imgTranslateVarX

            if(debug)
                System.out.println("LHS fit imageScale="+imageScale);
        }else if(matrix[0][0] >= 0 && matrix[1][0] <= 0 && matrix[0][1] >= 0 && matrix[1][1] >= 0){  //@chika

            imageScale=new AffineTransform();
            imageScale.rotate(Math.PI/2);

            //combination of patternOnTile.getOccupiedArea().height values
           imageScale.translate(-74-150,-77-150);
            //imageScale.scale(-1,-1);
//            if(!isSkewed)
//                imageScale.translate(0,patternOnTile.getOccupiedArea().height-patternOnTile.getOccupiedArea().y);//imgTranslateVarX
//            else
//                imageScale.translate(0,-(patternOnTile.getOccupiedArea().height-patternOnTile.getOccupiedArea().y));//imgTranslateVarX

            //imageScale.translate(-(patternOnTile.getOccupiedArea().height-patternOnTile.getOccupiedArea().y),
            //-(patternOnTile.getOccupiedArea().width-patternOnTile.getOccupiedArea().x));
            //if(!isSkewed)
               // imageScale.translate(-patternOnTile.getOccupiedArea().y,-patternOnTile.getOccupiedArea().x);//patternOnTile.getOccupiedArea().height-patternOnTile.getOccupiedArea().y);//imgTranslateVarX
            //else
               // imageScale.translate(0,-(patternOnTile.getOccupiedArea().height-patternOnTile.getOccupiedArea().y));//imgTranslateVarX
        }


        //if image bigger than tile, we just need 1
        if(ub.getBounds().width<patternOnTile.getOccupiedArea().width && ub.getBounds().height<(patternOnTile.getOccupiedArea().height)){
            if(isSideways){
                double contextTranslateX = -g2.getTransform().getTranslateX()-(offsetXOnCanvas-db.width);
                double contextTranslateY =-g2.getTransform().getTranslateY()+(tileYoffset-offsetYOnCanvas);

                g2.translate(contextTranslateX,contextTranslateY);

                if(debug){
                    System.out.println("isSideways translate="+contextTranslateX+ ' ' +contextTranslateY);
                    debugG2.translate(contextTranslateX,contextTranslateY);
                }

            }else{
                //double contextTranslateX =-offX+patternOnTile.getOccupiedArea().width-patternOnTile.getOccupiedArea().x;
                double contextTranslateX=tileW-offX+5;
                double contextTranslateY =-g2.getTransform().getTranslateY()+(tileYoffset-offsetYOnCanvas);

                g2.translate(contextTranslateX,contextTranslateY);

                if(debug){
                    System.out.println("translate="+contextTranslateX+ ' ' +contextTranslateY);
                    debugG2.translate(contextTranslateX,contextTranslateY);
                }

            }

            patternOnTile.setG2(g2);
            patternOnTile.paint(null,imageScale,null);

            if(debug){
                patternOnTile.setG2(debugG2);
                patternOnTile.paint(null,imageScale,null);
            }

        }else{ //if image smaller than tile

            //draw wholeImage onto tile once and then just use tile
            if(cacheToTile){

                if(debug)
                    System.out.println("cached to tile size "+tileW+ ' ' +tileH+ ' ' +this.xStep+ ' ' +yStep);

                tileImg=new BufferedImage(tileW, tileH, BufferedImage.TYPE_INT_ARGB);

                Graphics2D tileG2=tileImg.createGraphics();
                tileG2.translate(-patternXOffsetOnTile,-patternYOffsetOnTile);
                patternOnTile.setG2(tileG2);
                patternOnTile.paint(null,null,null);

            }

            /**
             * allow for specific odd case and move to correct location
             */
            float maxYY=h+yStep+ offsetYOnCanvas;
            if(cacheToTile){
                maxYY=maxYY+(tileImg.getHeight()*2);

                if(debug)
                    System.out.println("adjust maxYY to "+maxYY);

            }
            /**
             * draw repeating pattern onto out tile
             * if tile is smaller than Xstep,Ystep, tesselate to fill
             */
            float startX=0,startY;
            for(float y=0;y<maxYY;y=y + offsetYOnCanvas){ //add all columns

                startY=-tileYoffset - tileYoffset;

                for(float x=-offsetXOnCanvas;x<w+ xStep + offsetXOnCanvas;x=x+ offsetXOnCanvas){ //fill all rows

                    //					if(isUpsideDown)
                    //						g2.translate(x+startX,-(y+startY));
                    //					else
                    //move to correct location to draw pattern
                    g2.translate(offX+x+startX,offY+y+startY);

                    if(debug)
                        debugG2.translate(offX+x+startX,offY+y+startY);

                    if(cacheToTile){ //single wholeImage of tile generated so draw wholeImage at correct location

                        //invert
                        AffineTransform tileAff=new AffineTransform();
                        tileAff.scale(1,-1);
                        tileAff.translate(0,tileImg.getHeight());

                        //draw in location
                        ColorSpaceConvertor.drawImage(g2, tileImg, tileAff, null);

                        if(debug)
                            ColorSpaceConvertor.drawImage(debugG2, tileImg, tileAff, null);

                    }else{ //pass in the wholeImage and get pattern to draw itself onto it
                        
                    	Rectangle tileSize = patternOnTile.getOccupiedArea();

                    	/** Take the top-left and bottom-right coords of current tile location and transform them. */
                    	float currentTileX = offX + x + startX;
                    	float currentTileY = offY + y + startY - offsetYOnCanvas;

                        float[] originalPoint = {currentTileX, currentTileY,
                        		                 currentTileX + tileSize.width, currentTileY + tileSize.height};
                        float[] transformPoint = new float[originalPoint.length]; 
                    	
                        //Translate in accordance with user bound.
                        AffineTransform ubPosition = new AffineTransform();
                        ubPosition.translate(ub.getX(), ub.getY());
                    	ubPosition.transform(originalPoint, 0, transformPoint, 0, 2);
                        

                    	/** Rectangle to check whether current tile needs to be drawn in the user bound.
                    	    Offsets account for gaps caused by the rectangle not being rotated. */
                    	Rectangle2D testArea = new Rectangle2D.Float(transformPoint[0], transformPoint[1], 
                    			                       (float) tileSize.getWidth() + tileXoffset, (float) tileSize.getHeight() + tileYoffset);

                        patternOnTile.setG2(g2);
                        
                        if(ub.intersects(testArea) || (matrix[0][0] >= 0 && matrix[1][0] <= 0 && matrix[0][1] >= 0 && matrix[1][1] >= 0)) {
                    		patternOnTile.paint(null,imageScale,null);
                    		if(debug){
                    		    patternOnTile.setG2(debugG2);
                                patternOnTile.paint(null,imageScale,null);
                            }
                    	}
                    	else if(debug) {
                    		/** Draw orange squares on graphics2D where empty spaces should be. */
                    		debugG2.setTransform(new AffineTransform());
                    		debugG2.setPaint(Color.ORANGE);
                    		debugG2.fill(testArea);
                    	}
                    	
                		if(debug) {
                			/** Draws a reference grid */
                			debugG2.setTransform(new AffineTransform());
                			debugG2.setPaint(Color.WHITE);
                			debugG2.draw(testArea);
                        }
                    }

                    g2.setTransform(defaultAf2);

                    if(debug)
                        debugG2.setTransform(debugAf2);

                    
                    if(isSkewed)
                        startY=startY- tileYoffset;
                    else
                        startY=startY+ tileYoffset;

                }
                startX=startX- tileXoffset;

            }
        }

        //return single wholeImage to fill gap
        Rectangle rect=ub.getBounds();
        rotatedPaint=new TexturePaint(wholeImage,ub.getBounds());

        if(debug){

            //debug direct
            debugG2.setPaint(Color.BLUE);
            debugG2.setTransform(new AffineTransform());
            debugG2.draw(ub);

            //@CHIKA
            String path="/Users/markee/Desktop/Cases/";
            try {
            	ImageIO.write(wholeImage,"PNG",new java.io.File(path+"wholeImage-"+this.patternObj.getObjectRefAsString()+ '-' +rect.x+ '-' +rect.y+".png"));
                ImageIO.write(img,"PNG",new java.io.File(path+"Pattern-"+this.patternObj.getObjectRefAsString()+ '-' +rect.x+ '-' +rect.y+".png"));
            } catch (IOException e) {
                //tell user and log
                if(LogWriter.isOutput())
                    LogWriter.writeLog("Exception: "+e.getMessage());
            }

            //debug image position
            if(tileImg!=null){
                BufferedImage img2=new BufferedImage(1000,1000,BufferedImage.TYPE_INT_RGB);

                Graphics2D debugG3=img2.createGraphics();
                debugG3.setPaint(Color.CYAN);
                debugG3.fill(ub);
                debugG3.setPaint(Color.RED);
                debugG3.draw(ub);
                debugG3.drawImage(wholeImage,rect.x,rect.y,rect.width,rect.height,null);
                try {
                    ImageIO.write(img2,"PNG",new java.io.File(path+"PatternOrig-"+this.patternObj.getObjectRefAsString()
                            + '-' +rect.x+ '-' +rect.y+"-wholeImage.png"));
                } catch (IOException e) {
                    //tell user and log
                    if(LogWriter.isOutput())
                        LogWriter.writeLog("Exception: "+e.getMessage());
                }
            }
        }

        return rotatedPaint.createContext(cm, db, ub, xform, new RenderingHints(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY));
    }

    public void setScaling(double cropX,double cropH,float scaling, float textX, float textY){

    }

    public boolean isPattern() {
        return false;
    }

    public void setPattern(int dummy) {

    }

    public int getRGB() {
        return 0;
    }

    public int getTransparency() {
        return 0;
    }

    public void setRenderingType(int createHtml) {
        //added for HTML conversion
    }

}
