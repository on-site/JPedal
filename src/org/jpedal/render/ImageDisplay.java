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
 * ImageDisplay.java
 * ---------------
 */
package org.jpedal.render;

import org.jpedal.color.PdfPaint;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.GraphicsState;
import org.jpedal.parser.ValueTypes;
import org.jpedal.render.output.OutputDisplay;
import org.jpedal.utils.repositories.Vector_Rectangle;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import org.jpedal.fonts.PdfFont;

public class ImageDisplay extends BaseDisplay implements DynamicVectorRenderer {

    /**
     * used by PDF2HTML to add invisible text
     */
    DynamicVectorRenderer htmlDisplay;

    public ImageDisplay(int pageNumber, boolean addBackground, int defaultSize, ObjectStore newObjectRef) {

		super();

		type = DynamicVectorRenderer.DISPLAY_IMAGE;

		this.pageNumber = pageNumber;
		this.objectStoreRef = newObjectRef;
		this.addBackground = addBackground;

		//setupArrays(defaultSize);
		areas = new Vector_Rectangle(defaultSize);
	}

	// save image in array to draw
	final public int drawImage(int pageNumber, BufferedImage image, GraphicsState gs, boolean alreadyCached, String name, int optionsApplied, int previousUse) {

		//track objects

		int iw = (int) gs.CTM[0][0];
		if (iw < 0) {
			iw = -iw;
		}

		if (iw == 0) {
			iw = (int) gs.CTM[0][1];
		}
		if (iw < 0) {
			iw = -iw;
		}


		int ih = (int) gs.CTM[1][1];
		if (ih < 0) {
			ih = -ih;
		}

		if (ih == 0) {
			ih = (int) gs.CTM[1][0];
		}
		if (ih < 0) {
			ih = -ih;
		}

		areas.addElement(new Rectangle((int) gs.CTM[2][0], (int) gs.CTM[2][1], iw, ih));

		//if(g2!=null)
		renderImage(null, image, gs.getAlpha(GraphicsState.FILL), gs, gs.x, gs.y, optionsApplied);

		return -1;

	}


	/*save clip in array to draw*/
	public void drawClip(GraphicsState currentGraphicsState, Shape defaultClip, boolean canBeCached) {

		Area clip=currentGraphicsState.getClippingShape();

        if(canBeCached && hasClips && lastClip==null&& clip==null){
        
        }else if (!canBeCached || lastClip==null || clip==null || !clip.equals(lastClip)){

    		RenderUtils.renderClip(currentGraphicsState.getClippingShape(), null, defaultClip, g2);

            lastClip=clip;
            
            hasClips=true;
            
       }
	}

	public void drawEmbeddedText(float[][] Trm, int fontSize, PdfGlyph embeddedGlyph,
                                 Object javaGlyph, int type, GraphicsState gs, AffineTransform at, String glyf, PdfFont currentFontData, float glyfWidth) {

        /**
         * add text as visible or invisible
         */
        if(htmlDisplay!=null){
            htmlDisplay.drawEmbeddedText(Trm, fontSize, embeddedGlyph,javaGlyph, type, gs, at, glyf, currentFontData, glyfWidth);

            if(htmlDisplay.getValue(org.jpedal.render.output.OutputDisplay.TextMode)== OutputDisplay.TEXT_VISIBLE_ON_IMAGE){
                return ;
            }
        }

		if (type == TEXT) {

			PdfPaint currentCol = null, fillCol = null;
			int text_fill_type = gs.getTextRenderType();

			//for a fill
			if ((text_fill_type & GraphicsState.FILL) == GraphicsState.FILL) {
				fillCol = gs.getNonstrokeColor();
			}

			//and/or do a stroke
			if ((text_fill_type & GraphicsState.STROKE) == GraphicsState.STROKE) {
				currentCol = gs.getStrokeColor();
			}

			//set the stroke to current value
			Stroke newStroke = gs.getStroke();
			g2.setStroke(newStroke);

			AffineTransform def = g2.getTransform();

			g2.translate(Trm[2][0], Trm[2][1]);

			g2.transform(at);

			renderText(Trm[2][0], Trm[2][1], text_fill_type, (Area) javaGlyph, null, currentCol, fillCol, gs.getAlpha(GraphicsState.STROKE),
                    gs.getAlpha(GraphicsState.FILL));

			g2.setTransform(def);
		} else {

			PdfPaint strokeCol = null, fillCol = null;
			int text_fill_type = gs.getTextRenderType();

			//for a fill
			if ((text_fill_type & GraphicsState.FILL) == GraphicsState.FILL) {
				fillCol = gs.getNonstrokeColor();
			}

			//and/or do a stroke
			if ((text_fill_type & GraphicsState.STROKE) == GraphicsState.STROKE) {
				strokeCol = gs.getStrokeColor();
			}

			//set the stroke to current value
			Stroke newStroke = gs.getStroke(), currentStroke = g2.getStroke();

			//avoid if stroke/fill
			if (text_fill_type == GraphicsState.STROKE) {
				g2.setStroke(newStroke);
			}

			//track objects so we can work out if anything behind

			int fontSize2 = (int) gs.CTM[1][1];
			if (fontSize2 < 0) {
				fontSize2 = -fontSize2;
			}

			if (fontSize2 == 0) {
				fontSize2 = (int) gs.CTM[0][1];
			}
			if (fontSize2 < 0) {
				fontSize2 = -fontSize2;
			}

			areas.addElement(new Rectangle((int) gs.CTM[2][0], (int) gs.CTM[2][1], fontSize2, fontSize2));

			renderEmbeddedText(text_fill_type, embeddedGlyph, type, at, null, strokeCol, fillCol,
                    gs.getAlpha(GraphicsState.STROKE), gs.getAlpha(GraphicsState.FILL), (int) gs.getLineWidth());

			g2.setStroke(currentStroke);

		}
	}

	/*save shape in array to draw*/
	final public void drawShape(Shape currentShape, GraphicsState gs, int cmd) {

		areas.addElement(currentShape.getBounds());

		renderShape(null, gs.getFillType(), gs.getStrokeColor(), gs.getNonstrokeColor(), gs.getStroke(),
                currentShape, gs.getAlpha(GraphicsState.STROKE), gs.getAlpha(GraphicsState.FILL));
	}

	/*add XForm object*/
	final public void drawXForm(DynamicVectorRenderer dvr, GraphicsState gs) {
		renderXForm(dvr, gs.getAlpha(GraphicsState.STROKE));
	}

    /**
     * used by some custom version of DynamicVectorRenderer
     */
    public void writeCustom(int key, Object value) {
        switch(key){
            case ValueTypes.HTMLInvisibleTextHandler:
                this.htmlDisplay= (DynamicVectorRenderer) value;
                break;
        }
    }

}
