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
 * TTGlyph.java
 * ---------------
 */
package org.jpedal.fonts.tt;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashSet;

import org.jpedal.color.PdfPaint;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.io.PathSerializer;
import org.jpedal.objects.GraphicsState;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.repositories.Vector_Double;
import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.utils.repositories.Vector_Object;
import org.jpedal.utils.repositories.Vector_Path;
import org.jpedal.utils.repositories.Vector_Short;

public class TTGlyph implements PdfGlyph, Serializable {


	public static boolean useHinting = false;
	/**/

    public static boolean redecodePage = false;
    
    static {
        String value = System.getProperty("org.jpedal.useTTFontHinting");
        if (value != null) {
            useHinting = value.toLowerCase().equals("true");
        }
    }

    private static HashSet testedFonts = new HashSet();


    private boolean containsBrokenGlyfData=false;

    private boolean ttHintingRequired=false;

	private short minX,minY,maxX,maxY;

    int BPoint1, BPoint2;

    int BP1x=-1,BP2x=-1,BP1y=-1,BP2y=-1;

    private short compMinX, compMinY, compMaxX, compMaxY;

    private int[] scaledX, scaledY;

	private short leftSideBearing;
	//private int advanceWidth;
	
	private Vector_Int xtranslateValues=new Vector_Int(5);
	private Vector_Int ytranslateValues=new Vector_Int(5);
	private Vector_Double xscaleValues=new Vector_Double(5);
	private Vector_Double yscaleValues=new Vector_Double(5);
	private Vector_Double scale01Values=new Vector_Double(5);
	private Vector_Double scale10Values=new Vector_Double(5);

	private int xtranslate,ytranslate;
	
	private double xscale=1,yscale=1,scale01=0,scale10=0;

    private int[] instructions;
    private int currentInstructionDepth=Integer.MAX_VALUE;
	private Vector_Object glyfX=new Vector_Object(5);
	private Vector_Object glyfY=new Vector_Object(5);
	private Vector_Object curves=new Vector_Object(5);
	private Vector_Object contours=new Vector_Object(5);
	private Vector_Int endPtIndices=new Vector_Int(5);

	private int contourCount=0;
	
	private float unitsPerEm=64;
	
	public boolean debug=false;
	
	
	/**paths for the letter, marked as transient so it wont be serialized */
	private transient Vector_Path paths=new Vector_Path(10);
	
	//used to track which glyf for complex glyph
	private int compCount=1;
	
	private boolean isComposite=false;
	
	String glyfName="";
    private double pixelSize;

	//private int idx;
	
	/**
	 * method to set the paths after the object has be deserialized.
	 * 
	 * NOT PART OF API and subject to change (DO NOT USE)
	 * 
	 * @param vp - the Vector_Path to set
	 */
	public void setPaths(Vector_Path vp){
		paths = vp;
	}
	
	/**
	 * method to serialize all the paths in this object.  This method is needed because
	 * GeneralPath does not implement Serializable so we need to serialize it ourself.
	 * The correct usage is to first serialize this object, cached_current_path is marked
	 * as transient so it will not be serilized, this method should then be called, so the
	 * paths are serialized directly after the main object in the same ObjectOutput.
	 * 
	 * NOT PART OF API and subject to change (DO NOT USE)
	 * 
	 * @param os - ObjectOutput to write to
	 * @throws IOException
	 */
	public void writePathsToStream(ObjectOutput os) throws IOException {
		if((paths!=null)){
			
			GeneralPath[] generalPaths=paths.get();
			
			int count=0;
			
			/** find out how many items are in the collection */
			for (int i = 0; i < generalPaths.length; i++) {
				if(generalPaths[i]==null){
					count = i;
					break;
				}
			}
			
			/** write out the number of items are in the collection */
			os.writeObject(count);
			
			/** iterate throught the collection, and write out each path individualy */
			for (int i = 0; i < count; i++) {
				PathIterator pathIterator = generalPaths[i].getPathIterator(new AffineTransform());
				PathSerializer.serializePath(os, pathIterator);
			}
			
		}
	}
	
	public TTGlyph(){}



    /**
     * Unhinted constructor
     */
    public TTGlyph(String glyfName,boolean debug,Glyf currentGlyf,FontFile2 glyfTable,Hmtx currentHmtx,int idx,
                   float unitsPerEm, String baseFontName){

        //debug=idx==2246;
        this.debug=debug;

        this.glyfName=glyfName;
        //this.idx=idx;
        this.leftSideBearing =currentHmtx.getLeftSideBearing(idx);
        //this.advanceWidth = currentHmtx.getAdvanceWidth(idx);
        this.unitsPerEm=unitsPerEm;

        int p=currentGlyf.getCharString(idx);
        glyfTable.setPointer(p);

        readGlyph(currentGlyf,glyfTable);

        /**create glyphs the first time*/
        for(int i=0;i<this.compCount;i++){

            int[] pX=(int[]) glyfX.elementAt(i);
            int[] pY=(int[]) glyfY.elementAt(i);
            boolean[] onCurve=(boolean[]) curves.elementAt(i);
            boolean[] endOfContour=(boolean[])contours.elementAt(i);
            int endIndex=endPtIndices.elementAt(i);

            if(isComposite){
                xtranslate=xtranslateValues.elementAt(i);
                ytranslate=ytranslateValues.elementAt(i);
                xscale=xscaleValues.elementAt(i);
                yscale=yscaleValues.elementAt(i);
                scale01=scale01Values.elementAt(i);
                scale10=scale10Values.elementAt(i);

                //factor in BPoint where points overlap
                if(BPoint1!=-1 && BPoint2!=-1){
                    if(BP1x==-1 && BP2x==-1 && BP1y==-1 && BP2y==-1){ //first point
                        BP1x=pX[BPoint1];
                        BP1y=pY[BPoint1];

                    }else{ //second and reset

                        BP2x=pX[BPoint2];
                        BP2y=pY[BPoint2];

                        int xx=BP1x-BP2x;
                        int yy=BP1y-BP2y;

                        int count=pX.length;
                        for(int ii=0;ii<count;ii++){
                            pX[ii]=pX[ii]+xx;

                            if(debug)
                                System.out.println(pY[ii]+" "+yy+" BP1y="+BP1y+" BP1y="+BP1y);
                            pY[ii]=pY[ii]+yy;
                        }

                        //reset for next
                        BP1x=-1;
                        BP2x=-1;
                        BP1y=-1;
                        BP2y=-1;
                    }
                }
            }


//		    drawGlyf(pX,pY,onCurve,endOfContour,endIndex,debug);
            createPaths(pX,pY,onCurve,endOfContour,endIndex,debug);
        }

         /**/
        if(debug){
            try{
                System.out.println("debugging"+idx);
                java.awt.image.BufferedImage img=new java.awt.image.BufferedImage(700,700, java.awt.image.BufferedImage.TYPE_INT_ARGB);

                Graphics2D gg2= img.createGraphics();

                for(int jj=0;jj<paths.size()-1;jj++){
                    if(jj==0)
                        gg2.setColor(java.awt.Color.red);
                    else
                        gg2.setColor(java.awt.Color.blue);

                    gg2.fill(paths.elementAt(jj));


                    gg2.draw(paths.elementAt(jj).getBounds());
                }
                //org.jpedal.gui.ShowGUIMessage.showGUIMessage("glyf "+ '/' +paths.size(),img,"glyf "+ '/' +paths.size()+ '/' +compCount);
            }catch(Exception e){
                //tell user and log
                if(LogWriter.isOutput())
                    LogWriter.writeLog("Exception: "+e.getMessage());
            }
        }/***/


        //if(idx==2060)
        //ShowGUIMessage.showGUIMessage("done",img,"glyf done");

    }



	private void readGlyph(Glyf currentGlyf,FontFile2 currentFontFile){
		
		//LogWriter.writeMethod("{readGlyph}", 0);
		
		contourCount=currentFontFile.getNextUint16();

		//read the max/min co-ords
		minX=(short)currentFontFile.getNextUint16();
		minY=(short)currentFontFile.getNextUint16();
		maxX=(short)currentFontFile.getNextUint16();
		maxY=(short)currentFontFile.getNextUint16();
				
		if(minX>maxX || minY>maxY){			
			return;
		}

        if(debug){
            System.out.println("------------------------------------------------------------");
            System.out.println("min="+minX+ ' ' +minY+" max="+maxX+' '+maxY+" contourCount="+contourCount);
        }

        if(contourCount!=65535){
            if(contourCount>0){
			    readSimpleGlyph(currentFontFile);
            }
		}else{

            compMinX=minX;
            compMinY=minY;
            compMaxX=maxX;
            compMaxY=maxY;

            if(debug)
            System.out.println("XXmain="+minX+ ' ' +minY+ ' ' +maxX+ ' ' +maxY);

            readComplexGlyph(currentGlyf,currentFontFile);
		}
		
		
	}

    //Track translations for recursion
    int existingXTranslate =0;
    int existingYTranslate =0;
    int depth = 0;

	final private void readComplexGlyph(Glyf currentGlyf,FontFile2 currentFontFile){

		isComposite=true;

        //Remove elements for the compound as it's only a container
        xtranslateValues.pull();
        ytranslateValues.pull();
        xscaleValues.pull();
        yscaleValues.pull();
        scale01Values.pull();
        scale10Values.pull();


        BPoint1=-1;
        BPoint2=-1;

        //LogWriter.writeMethod("{readComplexGlyph}", 0);

        boolean WE_HAVE_INSTRUCTIONS = false;

        int count=currentGlyf.getGlypfCount();

		while(true){
			int flag=currentFontFile.getNextUint16();
			int glyphIndex=currentFontFile.getNextUint16();

            if(debug)
            System.err.println("Index="+glyphIndex+" flag="+flag+ ' ' +count);

            //allow for bum data
            if(glyphIndex>=count){
                containsBrokenGlyfData=true;
                break;
            }


            //set flag options
			boolean ARG_1AND_2_ARE_WORDS= (flag & 1)==1;
			boolean ARGS_ARE_XY_VALUES= (flag & 2) ==2;
			boolean WE_HAVE_A_SCALE=(flag & 8) ==8;
			boolean WE_HAVE_AN_X_AND_Y_SCALE=(flag & 64) ==64;
			boolean WE_HAVE_A_TWO_BY_TWO=(flag & 128) ==128;
            WE_HAVE_INSTRUCTIONS = WE_HAVE_INSTRUCTIONS || (flag & 256) == 256;



			if (ARG_1AND_2_ARE_WORDS && ARGS_ARE_XY_VALUES){
			    //1st short contains the value of e
			    //2nd short contains the value of f
				xtranslate=currentFontFile.getNextInt16();
				ytranslate=currentFontFile.getNextInt16();
			}else if (!ARG_1AND_2_ARE_WORDS && ARGS_ARE_XY_VALUES){
			    //1st byte contains the value of e
			    //2nd byte contains the value of f
				xtranslate=currentFontFile.getNextint8();
				ytranslate=currentFontFile.getNextint8();
			}else if (ARG_1AND_2_ARE_WORDS && !ARGS_ARE_XY_VALUES){
			    //1st short contains the index of matching point in compound being constructed
			    //2nd short contains index of matching point in component
				BPoint1 =currentFontFile.getNextUint16();
				BPoint2 =currentFontFile.getNextUint16();
				xtranslate=0;
				ytranslate=0;

            }else if (!ARG_1AND_2_ARE_WORDS && !ARGS_ARE_XY_VALUES){
			   // 1st byte containing index of matching point in compound being constructed
			   // 2nd byte containing index of matching point in component
				BPoint1 =currentFontFile.getNextUint8();
				BPoint2 =currentFontFile.getNextUint8();
				xtranslate=0;
				ytranslate=0;

            }

			//set defaults
			xscale=1; //a
			scale01=0; //b
			scale10=0; //c
            yscale=1; //d

			/**workout scaling factors*/
			if((!WE_HAVE_A_SCALE)&&(!WE_HAVE_AN_X_AND_Y_SCALE)&&(!WE_HAVE_A_TWO_BY_TWO)){
				//uses defaults already set
				
			}else if((WE_HAVE_A_SCALE)&&(!WE_HAVE_AN_X_AND_Y_SCALE)&&(!WE_HAVE_A_TWO_BY_TWO)){

				xscale=currentFontFile.getF2Dot14(); //a
				scale01=0; //b
				scale10=0; //c
                yscale=xscale; //d
            }else if((!WE_HAVE_A_SCALE)&&(WE_HAVE_AN_X_AND_Y_SCALE)&&(!WE_HAVE_A_TWO_BY_TWO)){

				xscale=currentFontFile.getF2Dot14(); //a
				scale01=0; //b
				scale10=0; //c
                yscale=currentFontFile.getF2Dot14(); //d

			}else if((!WE_HAVE_A_SCALE)&&(!WE_HAVE_AN_X_AND_Y_SCALE)&&(WE_HAVE_A_TWO_BY_TWO)){

				xscale=currentFontFile.getF2Dot14(); //a
				scale01=currentFontFile.getF2Dot14(); //b
				scale10=currentFontFile.getF2Dot14(); //c
                yscale=currentFontFile.getF2Dot14(); //d
            }

            //store so we can remove later
            int localX = xtranslate;
            int localY = ytranslate;

            //Get total translation
            xtranslate += existingXTranslate;
            ytranslate += existingYTranslate;

			//save values
			xtranslateValues.addElement(xtranslate);
			ytranslateValues.addElement(ytranslate);
			xscaleValues.addElement(xscale);
			yscaleValues.addElement(yscale);
			scale01Values.addElement(scale01);
			scale10Values.addElement(scale10);

			//save location so we can restore
			int pointer=currentFontFile.getPointer();
			
			/**/
			//now read the simple glyphs
			int p=currentGlyf.getCharString(glyphIndex);

            if(p!=-1){
				if(p<0)
					p=-p;
				currentFontFile.setPointer( p);
                existingXTranslate = xtranslate;
                existingYTranslate = ytranslate;
                depth++;
				readGlyph(currentGlyf,currentFontFile);
                depth--;
                existingXTranslate -= localX;
                existingYTranslate -= localY;
			}else{
				System.err.println("Wrong value in complex");
			}
			
			currentFontFile.setPointer(pointer);

			//break out at end
			if((flag & 32) ==0) {

                if (WE_HAVE_INSTRUCTIONS) {
                    int instructionLength = currentFontFile.getNextUint16();
                    int[] instructions = new int[instructionLength];
                    for (int i=0; i< instructionLength; i++)
                        instructions[i] = currentFontFile.getNextUint8();
                    if (depth <= currentInstructionDepth) {
                        this.instructions = instructions;
                        currentInstructionDepth=depth;
                    }
                } else {
                    if (depth <= currentInstructionDepth) {
                        this.instructions = new int[]{};
                        currentInstructionDepth=depth;
                    }
                }

                break;
            }
			
			compCount++;
		}
	}
	
	private void readSimpleGlyph(FontFile2 currentFontFile){
		
		//LogWriter.writeMethod("{readSimpleGlyph}", 0);
		
		int flagCount=1;
		
		short x1  ;
		
		Vector_Int rawFlags=new Vector_Int(50);
		Vector_Int endPts=new Vector_Int(50);
		Vector_Short XX=new Vector_Short(50);
		Vector_Short Y=new Vector_Short(50);
		

		
		//all endpoints
        if(debug){
            System.out.println("endPoints");
            System.out.println("---------");
        }
        
        try{

        	int lastPt=0;
        	for(int i=0;i<contourCount;i++){


        		lastPt=currentFontFile.getNextUint16();

        		if(debug)
        			System.out.println(i+" "+lastPt);

        		endPts.addElement(lastPt);

        	}

        	//allow for corrupted value with not enough entries
        	//ie customers3/ICG3Q03.pdf
        	if(currentFontFile.hasValuesLeft()){


        		/**Don;t comment out !!!!!!!!!
        		 * needs to be read to advance pointer*/
        		int instructionLength=currentFontFile.getNextUint16();


        		int[] instructions=new int[instructionLength];
        		for(int i=0;i<instructionLength;i++)
        			instructions[i]=currentFontFile.getNextUint8();

        		if (depth < currentInstructionDepth)
        			this.instructions = instructions;

        		if(debug){
        			System.out.println("Instructions");
        			System.out.println("------------");
        			System.out.println("count="+instructionLength);
        		}

        		int count = lastPt+ 1;
        		int flag;

        		/**we read the flags (some can repeat)*/
        		for (int i = 0; i < count; i++) {
        			if(currentFontFile.getBytesLeft()<1){
        				return;
        			}
        			flag=currentFontFile.getNextUint8();
        			rawFlags.addElement(flag);  
        			flagCount++;

        			if ((flag & 8) == 8) { //repeating flags        							}
        				int repeatCount = currentFontFile.getNextUint8();
        				for (int r = 1; r <= repeatCount; r++) {
        					rawFlags.addElement(flag);
        					flagCount++;
        				}
        				i += repeatCount;
        			}
        		}

        		/**read the x values and set segment for complex glyph*/
        		for(int i=0;i<count;i++){
        			flag=rawFlags.elementAt(i);

        			//boolean twoByteValue=((flag  & 2)==0);
        			if ((flag & 16) != 0) { //
        				if ((flag & 2) != 0) { //1 byte + value						
        					x1=(short)currentFontFile.getNextUint8();
        					XX.addElement(x1);
        				}else{ //2 byte value - same as previous - ??? same X coord or value
        					XX.addElement((short)0);
        				}

        			} else {
        				if ((flag  & 2) != 0){ //1 byte - value
        					x1=(short)-currentFontFile.getNextUint8();
        					XX.addElement(x1);
        				}else{ //signed 16 bit delta vector        					
        					x1=currentFontFile.getNextSignedInt16();			
        					XX.addElement(x1);	
        				}
        			}
        		}

        		/**read the y values*/
        		for(int i=0;i<count;i++){
        			flag=rawFlags.elementAt(i);
        			if ((flag & 32) != 0) {
        				if ((flag & 4) != 0) {
        					if(currentFontFile.getBytesLeft()<1){
        						return;
        					}					
        					Y.addElement((short)currentFontFile.getNextUint8());
        				} else {
        					Y.addElement((short)0);
        				}
        			} else {
        				if ((flag & 4) != 0) {        					
        					Y.addElement((short)-currentFontFile.getNextUint8());
        				} else {        					
        					short val=currentFontFile.getNextSignedInt16();
        					Y.addElement(val);
        				}
        			}
        		}


        		/**
        		 * calculate the points
        		 */
        		int endPtIndex = 0;
        		int x=0,y=0;

        		int[] flags=rawFlags.get();

        		int[] endPtsOfContours=endPts.get();
        		short[] XPoints=XX.get();
        		short[] YPoints=Y.get();
        		count=XPoints.length;

        		int[] pX=new int[count+2];
        		int[] pY=new int[count+2];
        		boolean[] onCurve=new boolean[count+2];
        		boolean[] endOfContour=new boolean[count+2];

        		int endIndex=0;

        		if(debug){
        			System.out.println("Points");
        			System.out.println("------");
        		}
        		for (int i = 0; i < count; i++) {

        			boolean endPt = endPtsOfContours[endPtIndex] == i;
        			if (endPt){
        				endPtIndex++;
        				endIndex=i+1;
        			}
        			x +=XPoints[i];
        			y +=YPoints[i];

        			pX[i]=x;
        			pY[i]=y;

        			onCurve[i] = i < flagCount && (flags[i] & 1) != 0;

        			endOfContour[i]=endPt;

        			if(debug)
        				System.out.println(i+" "+pX[i]+ ' ' +pY[i]+ " on curve=" +onCurve[i]+" endOfContour[i]="+endOfContour[i]);


        		}

        		for (int i=0; i<pX.length; i++) {
        			int lX = pX[i];
        			int lY = pY[i];

        			//Convert x
        			//pX[i] = convertX(lX,lY);
        			if(!isComposite) {
        				if (!useHinting)
        					pX[i] = (int)(lX/unitsPerEm);
        				else
        					pX[i] = lX;
        			} else {
        				if (!useHinting)
        					pX[i] = (int)((((lX * xscale) + (lY * scale10))+xtranslate)/unitsPerEm);
        				else
        					pX[i] = (int)((((lX * xscale) + (lY * scale10))+xtranslate));
        			}


        			//Convert Y
        			//pY[i] = convertY(lX,lY);
        			if(!isComposite) {
        				if (!useHinting)
        					pY[i] = (int)(lY/unitsPerEm);
        				else
        					pY[i] = lY;
        			} else {
        				if (!useHinting)
        					pY[i] = (int)((((lX * scale01) + (lY * yscale))+ytranslate)/unitsPerEm);
        				else
        					pY[i] = (int)((((lX * scale01) + (lY * yscale))+ytranslate));
        			}
        		}        	

        		//store
        		glyfX.addElement(pX);
        		glyfY.addElement(pY);
        		this.curves.addElement(onCurve);
        		this.contours.addElement(endOfContour);
        		this.endPtIndices.addElement(endIndex);

        		// move back at end
        		/**
		pX[count]=0;
		pY[count]=0;
		onCurve[count]=true;
		endOfContour[count]=true;
		pX[count+1]=advanceWidth;
		pY[count+1]=0;
		onCurve[count+1]=true;
		endOfContour[count+1]=true;

		/**
		debug=false;

		if(idx==92){
		//if(glyfName.equals("y")){
			ii++;
				if(ii>0){
					System.out.println(ii+"=============="+glyfName+"=====================");

					debug=true;

					img=new BufferedImage(1000,1000,BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2=(Graphics2D) img.createGraphics();

         g2.setColor(Color.red);
         render(g2,debug);
         }
         }/**/
        	}
        }
        catch(Exception e){
        	//System.err.println("error occured while reading TTGlyph bytes");
        	return; //there are many files in which the glyph length is not matched with specification
        }
    }

    //public void render(Graphics2D g2){}
    public void render(int type, Graphics2D g2, float scaling, boolean isFormGlyph){

        AffineTransform restore = g2.getTransform();
        BasicStroke oldStroke = (BasicStroke)g2.getStroke();

        float strokeWidth = oldStroke.getLineWidth();

        if (strokeWidth < 0)
            strokeWidth = -strokeWidth;

        if(useHinting){
            //Scale down glyph
            g2.scale(1 / 100d, 1 / 100d);

            //Widen stroke to compensate for scale
            strokeWidth = strokeWidth * 100;
        }

        g2.setStroke(new BasicStroke(strokeWidth,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND,
                oldStroke.getMiterLimit(),
                oldStroke.getDashArray(),
                oldStroke.getDashPhase()));

        /**drawn the paths*/
        for(int jj=0;jj<paths.size()-1;jj++){

            if((type & GraphicsState.FILL)==GraphicsState.FILL){
                g2.fill(paths.elementAt(jj));
            }else if((type & GraphicsState.STROKE)==GraphicsState.STROKE){
                g2.draw(paths.elementAt(jj));
            }
        }

        if (useHinting) {
            //Restore stroke width and scaling
            g2.setStroke(oldStroke);
            g2.setTransform(restore);
        }
    }

    Area glyphShape=null;
	
	/**return outline of shape*/
	public Area getShape(){/**/
		
		if(glyphShape==null){
		
		    /**drawn the paths*/
		    GeneralPath path=paths.elementAt(0);
		
			for(int jj=1;jj<paths.size()-1;jj++)
				path.append(paths.elementAt(jj),false);			

            if (path == null) {
                return null;
            }

		    glyphShape=new Area(path);
		
		}

		return glyphShape;
		
	}

    //used by Type3 fonts
    public String getGlyphName() {
        return null;
    }

    /**create the actual shape
     * @param pX*/
	public void scaler(int[] pX,int[] pY){

        scaledX = new int[pX.length];
        scaledY = new int[pY.length];

        double scale = (pixelSize / (unitsPerEm*1000)) * 64;

        for (int i=0; i<pX.length; i++) {
            scaledX[i] = (int)((scale * pX[i])+0.5);
            scaledY[i] = (int)((scale * pY[i])+0.5);
        }

        scaledX[pX.length-2] = 0;
        scaledY[pY.length-2] = 0;
        scaledX[pX.length-1] = (int)((scale * leftSideBearing)+0.5);
        scaledY[pY.length-1] = 0;

    }


    /**create the actual shape*/
	public void createPaths(int[] pX,int[] pY,boolean[] onCurve,boolean[] endOfContour,int endIndex, boolean debug){

        //allow for bum data
        if(endOfContour==null)
            return;

        /**
         * scan data and adjust glyfs after first if do not end in contour
         */

        int ptCount=endOfContour.length;

        int start=0, firstPt=-1;
        for(int ii=0;ii<ptCount;ii++){

            if(endOfContour[ii]){

                if(firstPt!=-1 && (!onCurve[start] || !onCurve[ii]) ){ //last point not on curve and we have a first point

                    int diff=firstPt-start,newPos;

                    //make a deep copy of values
                    int pXlength=pX.length;
                    int[] old_pX=new int[pXlength];
                    System.arraycopy(pX,0,old_pX,0,pXlength);

                    int[] old_pY=new int[pXlength];
                    System.arraycopy(pY,0,old_pY,0,pXlength);

                    boolean[] old_onCurve=new boolean[pXlength];
                    System.arraycopy(onCurve,0,old_onCurve,0,pXlength);

                    //rotate values to ensure point at start
                    for(int oldPos=start;oldPos<ii+1;oldPos++){

                        newPos=oldPos+diff;
                        if(newPos>ii)
                            newPos=newPos-(ii-start+1);
                        pX[oldPos]=old_pX[newPos];
                        pY[oldPos]=old_pY[newPos];
                        onCurve[oldPos]=old_onCurve[newPos];

                    }
                }

                //reset values
                start=ii+1;
                firstPt=-1;

            }else if(onCurve[ii] && firstPt==-1){ //track first point
                firstPt=ii;
            }

        }

        boolean isFirstDraw=true;

        GeneralPath current_path =new GeneralPath(GeneralPath.WIND_NON_ZERO);

        int c= pX.length,fc=-1;

        //find first end contour
        for(int jj=0;jj<c;jj++){
            if(endOfContour[jj]){
                fc=jj+1;
                jj=c;
            }
        }

        int x1,y1,x2=0,y2=0,x3=0,y3=0;

        x1=pX[0];
        y1=pY[0];

        if(this.debug)
            System.out.println(pX[0]+" "+pY[0]+" move to x1,y1="+x1+ ' ' +y1);

        current_path.moveTo(x1,y1);

        if(this.debug){
            System.out.println("first contour="+fc+"===================================="+pX[0]+ ' ' +pY[0]);
            //System.out.println("start="+x1+ ' ' +y1+" unitsPerEm="+unitsPerEm);
            //for (int i = 0; i <c-2; i++)
            //System.out.println(i+" "+convertX(pX[i],pY[i])+ ' ' +convertY(pX[i],pY[i])+ ' ' +onCurve[i]+ ' ' +endOfContour[i]+" raw="+pX[i]+ ' ' +pY[i]);

            //System.out.println("Move to "+x1+ ' ' +y1);

        }

        int xs=0,ys=0,lc=0;
        boolean isEnd=false;

        for (int j = 0; j <endIndex; j++) {

            int p=j%fc;
            int p1=(j+1)%fc;
            int p2=(j+2)%fc;
            int pm1=(j-1)%fc;

            /**special cases
             *
             *round up to last point at end
             *First point
             */
            if(j==0)
                pm1=fc-1;
            if(p1<lc)
                p1=p1+lc;
            if(p2<lc)
                p2=p2+lc;

            if(debug)
                System.out.println("points="+lc+ '/' +fc+ ' ' +pm1+ ' ' +p+ ' ' +p1+ ' ' +p2+" j="+j+" endOfContour[j]="+endOfContour[j]);

            //allow for wrap around on contour
            if(endOfContour[j]){
                isEnd=true;

                if(onCurve[fc]){
                    xs=pX[fc];
                    ys=pY[fc];
                }else{
                    xs=pX[j+1];
                    ys=pY[j+1];
                }

                //remember start point
                lc=fc;
                //find next contour
                for(int jj=j+1;jj<c;jj++){
                    if(endOfContour[jj]){
                        fc=jj+1;
                        jj=c;
                    }
                }

                if(debug)
                    System.out.println("End of contour. next="+j+ ' ' +fc+ ' ' +lc);

            }

            if(debug){
                if(j>0)
                    System.out.println("curves="+onCurve[p]+ ' ' +onCurve[p1]+ ' ' +onCurve[p2]+" EndOfContour j-1="+endOfContour[j-1]+" j="+endOfContour[j]+ " j+1=" +endOfContour[j+1]);
                else
                    System.out.println("curves="+onCurve[p]+ ' ' +onCurve[p1]+ ' ' +onCurve[p2]+" EndOfContour j="+endOfContour[j]+" j+1="+endOfContour[j+1]);
            }

            if(lc==fc && onCurve[p]){
                j=c;
                if(debug)
                    System.out.println("last 2 match");
            }else{

                if(debug)
                    System.out.println(fc+" "+pm1+ ' ' +p+ ' ' +p1+ ' ' +p2);

                if(onCurve[p] && onCurve[p1]){ //straight line
                    x3=pX[p1];
                    y3=pY[p1];
                    current_path.lineTo(x3,y3);
                    if(debug)
                        System.out.println(p+" pt,pt "+x3+ ' ' +y3+" (lineTo)");

                    isFirstDraw=false;
                    //curves
                }else if(j<(c-3) &&((fc-lc)>1 || fc==lc)){
                    boolean checkEnd=false;
                    if(onCurve[p] && !onCurve[p1] && onCurve[p2] ){ //2 points + control

                        x1=pX[p];
                        y1=pY[p];
                        x2=pX[p1];
                        y2=pY[p1];
                        x3=pX[p2];
                        y3=pY[p2];
                        j++;
                        checkEnd=true;
                        if(debug)
                            System.out.println(p+" pt,cv,pt "+x1+ ' ' +y1+ ' ' +x2+ ' ' +y2+ ' ' +x3+ ' ' +y3);

                    }else if(onCurve[p] && !onCurve[p1] && !onCurve[p2]){ //1 point + 2 control

                        x1=pX[p];
                        y1=pY[p];
                        x2=pX[p1];
                        y2=pY[p1];
                        x3=midPt(pX[p1], pX[p2]);
                        y3=midPt(pY[p1], pY[p2]);
                        j++;

                        checkEnd=true;

                        if(debug)
                            System.out.println(p+" pt,cv,cv "+x1+ ' ' +y1+ ' ' +x2+ ' ' +y2+ ' ' +x3+ ' ' +y3);

                    }else if(!onCurve[p] && !onCurve[p1] && (!endOfContour[p2] ||fc-p2==1)){ // 2 control + 1 point (final check allows for last point to complete loop

                        x1=midPt(pX[pm1], pX[p]);
                        y1=midPt(pY[pm1], pY[p]);
                        x2=pX[p];
                        y2=pY[p];

                        x3=midPt(pX[p], pX[p1]);
                        y3=midPt(pY[p], pY[p1]);
                        if(debug)
                            System.out.println(p+" cv,cv1 "+x1+ ' ' +y1+ ' ' +x2+ ' ' +y2+ ' ' +x3+ ' ' +y3);


                    }else if(!onCurve[p] && onCurve[p1]){ // 1 control + 2 point

                        x1=midPt(pX[pm1], pX[p]);
                        y1=midPt(pY[pm1], pY[p]);
                        x2=pX[p];
                        y2=pY[p];
                        x3=pX[p1];
                        y3=pY[p1];
                        if(debug)
                            System.out.println(p+" cv,pt "+x1+ ' ' +y1+ ' ' +x2+ ' ' +y2+ ' ' +x3+ ' ' +y3);
                    }

                    if(isFirstDraw){
                        current_path.moveTo(x1,y1);
                        isFirstDraw=false;

                        if(debug)
                            System.out.println("first draw move to "+x1+ ' ' +y1);

                    }

                    if (!(endOfContour[p] && p > 0 && endOfContour[p-1]))
                        current_path.curveTo(x1,y1,x2,y2,x3,y3);

                    if(debug)
                        System.out.println("curveto "+x1+ ' ' +y1+ ' ' +x2+ ' ' +y2+ ' ' +x3+ ' ' +y3);

                    /**if end after curve, roll back so we pick up the end*/
                    if( checkEnd && endOfContour[j]){

                        isEnd=true;

                        xs=pX[fc];
                        ys=pY[fc];
                        //remmeber start point
                        lc=fc;
                        //find next contour
                        for(int jj=j+1;jj<c;jj++){
                            if(endOfContour[jj]){
                                fc=jj+1;
                                jj=c;
                            }
                        }

                        if(debug)
                            System.out.println("Curve");
                    }
                }

                if (endOfContour[p])
                    current_path.closePath();

                if(debug)
                    System.out.println("x2 "+xs+ ' ' +ys+ ' ' +isEnd);


                if(isEnd){
                    current_path.moveTo(xs,ys);
                    isEnd=false;
                    if(debug)
                        System.out.println("Move to "+xs+ ' ' +ys);
                }

                if(debug){
                    try{
                        if(img==null)
                            img=new java.awt.image.BufferedImage(800,800, java.awt.image.BufferedImage.TYPE_INT_ARGB);

                        Graphics2D g2= img.createGraphics();
                        g2.setColor(java.awt.Color.green);
                        g2.draw(current_path);

                        final String key= String.valueOf(p);

                        org.jpedal.gui.ShowGUIMessage.showGUIMessage(key,img,key);

                    }catch(Exception e){
                        //tell user and log
                        if(LogWriter.isOutput())
                            LogWriter.writeLog("Exception: "+e.getMessage());
                    }
                }
            }
        }

        /**
         * store so we can draw glyf as set of paths
         */
        paths.addElement(current_path);

        if(debug)
            System.out.println("Ends at "+x1+ ' ' +y1+" x="+minX+ ',' +maxX+" y="+minY+ ',' +maxY+" glyph x="+compMinX+ ',' +compMaxX+" y="+compMinY+ ',' +compMaxY);

    }

    java.awt.image.BufferedImage img=null;


    static private int midPt(int a, int b) {
		return a + (b - a)/2;
	}
	
//	/**convert to 1000 size*/
//	final private int convertX(int x,int y){
//
//
//        if(!isComposite) {
//			if (!useHinting)
//                return (int)(x/unitsPerEm);
//            else
//                return x;
//        } else {
//            //This code seems to match the spec more closely, but doesn't seem to make any difference...
//            //It's also wildly inefficient, so rewrite it if you're going to turn it on!
////            double absA = xscale;
////            if (absA < 0)
////                absA = -absA;
////            double absB = scale01;
////            if (absB < 0)
////                absB = -absB;
////            double absC = scale10;
////            if (absC < 0)
////                absC = -absC;
////
////            double m = absA;
////            if (absB > m)
////                m = absB;
////            if (absA - absC <= 33d/65536 && absA - absC >= -33d/65536)
////                m = 2*m;
////
////			return (int)((m*(((x * (xscale/m)) + (y * (scale10/m)))+xtranslate))/unitsPerEm);
//            if (!useHinting)
//                return (int)((((x * xscale) + (y * scale10))+xtranslate)/unitsPerEm);
//            else
//                return (int)((((x * xscale) + (y * scale10))+xtranslate));
//        }
//	}
//
//	/**convert to 1000 size*/
//	final private int convertY(int x,int y){
//
//        if(!isComposite) {
//			if (!useHinting)
//                return (int)(y/unitsPerEm);
//            else
//                return y;
//        } else {
//            //This code seems to match the spec more closely, but doesn't seem to make any difference...
//            //It's also wildly inefficient, so rewrite it if you're going to turn it on!
////            double absC = scale10;
////            if (absC < 0)
////                absC = -absC;
////            double absD = yscale;
////            if (absD < 0)
////                absD = -absD;
////
////            double n = absC;
////            if (absD > n)
////                n = absD;
////            if (absC - absD <= 33d/65536 && absC - absD >= -33d/65536)
////                n = 2*n;
////
////            return (int)((n*(((x * (scale01/n)) + (y * (yscale/n)))+ytranslate))/unitsPerEm);//+30;
//            if (!useHinting)
//                return (int)((((x * scale01) + (y * yscale))+ytranslate)/unitsPerEm);
//            else
//                return (int)((((x * scale01) + (y * yscale))+ytranslate));
//        }
//	}


	/* (non-Javadoc)
	 * @see org.jpedal.fonts.PdfGlyph#getWidth()
	 */
	public float getmaxWidth() {
		
		return 0;
	}

	/* (non-Javadoc)
     * @see org.jpedal.fonts.PdfGlyph#getmaxHeight()
     */
    public int getmaxHeight() {
        return 0;
    }

	/* (non-Javadoc)
	 * @see org.jpedal.fonts.PdfGlyph#setT3Colors(java.awt.Color, java.awt.Color)
	 */
	public void setT3Colors(PdfPaint strokeColor, PdfPaint nonstrokeColor, boolean lockColours) {

	}

	/* (non-Javadoc)
	 * @see org.jpedal.fonts.PdfGlyph#ignoreColors()
	 */
	public boolean ignoreColors() {
		return false;
	}
	
	/*
	 * Makes debugging easier
	 */
	public int getID() {
		return id;
	}

	int id=0;
	
	/* 
	 * Makes debugging easier
	 */
	public void setID(int id) {
		this.id=id;
	}


	public void flushArea() {
		glyphShape=null;
	}

	public void setWidth(float width) {
		
	}

    public int getFontBB(int type) {

        if(isComposite){
        if(type== PdfGlyph.FontBB_X)
            return compMinX;
        else if(type== PdfGlyph.FontBB_Y)
            return compMinY;
        else if(type== PdfGlyph.FontBB_WIDTH)
            return compMaxX;
        else if(type== PdfGlyph.FontBB_HEIGHT)
            return compMaxY;
        else
            return 0;
        }else{
            if(type== PdfGlyph.FontBB_X)
            return minX;
        else if(type== PdfGlyph.FontBB_Y)
            return minY;
        else if(type== PdfGlyph.FontBB_WIDTH)
            return maxX;
        else if(type== PdfGlyph.FontBB_HEIGHT)
            return maxY;
        else
            return 0;
        }
    }

    public void setStrokedOnly(boolean b) {
        //not used here
    }

    //use by TT to handle broken TT fonts
    public boolean containsBrokenData() {
        return this.containsBrokenGlyfData;  
    }

}
