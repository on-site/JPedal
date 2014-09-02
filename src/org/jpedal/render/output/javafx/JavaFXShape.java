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
 * JavaFXShape.java
 * ---------------
 */
package org.jpedal.render.output.javafx;

import org.jpedal.color.PdfPaint;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfPageData;
import org.jpedal.render.ShapeFactory;
import org.jpedal.render.output.OutputShape;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

public class JavaFXShape extends OutputShape implements ShapeFactory {

	// JavaFX supports both winding rules, it's default is non-zero
	private int windingRule = PathIterator.WIND_NON_ZERO;
	
	public JavaFXShape(int cmd,int shapeCount,float scaling, Shape currentShape, GraphicsState gs, AffineTransform scalingTransform, Point2D midPoint, Rectangle cropBox, int currentColor, int dpCount, int pageRotation, PdfPageData pageData, int pageNumber, boolean includeClip) {
        
		super(cmd, scaling, currentShape, gs, scalingTransform, midPoint, cropBox, currentColor, dpCount, pageRotation, pageData, pageNumber, includeClip);
        
        this.shapeCount=shapeCount;
        windingRule = currentShape.getPathIterator(scalingTransform).getWindingRule(); // Added winding rule via shape iterator
        generateShapeFromG2Data(gs, scalingTransform, cropBox);
    }
    
    protected void beginShape() {
    	pathCommands.add("\n");
    	pathCommands.add("\tPath path_"+shapeCount+" = new Path();");
    	pathCommands.add("\tObservableList<PathElement> shape_"+shapeCount+" = path_"+shapeCount+".getElements();");
    	pathCommands.add("\taddToGroup.add(path_"+shapeCount+");");//add the shape to the group
    }

    protected void lineTo(double[] coords) {    	
		pathCommands.add("\tshape_"+shapeCount+".add(new LineTo(" + coordsToStringParam(coords, 2) + "));");
    }
    
    protected void bezierCurveTo(double[] coords) {
        pathCommands.add("\tshape_"+shapeCount+".add(new CubicCurveTo("+coordsToStringParam(coords, 6)+"));");
    }

    protected void quadraticCurveTo(double[] coords) {
    	pathCommands.add("\tshape_"+shapeCount+".add(new QuadCurveTo("+coordsToStringParam(coords, 4)+"));");
    	
	}

    protected void moveTo(double[] coords) {
    	pathCommands.add("\tshape_"+shapeCount+".add(new MoveTo("+coordsToStringParam(coords, 2)+"));");
    }
    
    protected void closePath() {
    	pathCommands.add("\tshape_"+shapeCount+".add(new ClosePath());");
    }

    protected void drawCropBox(){
        pathCommands.clear();
        pathCommands.add("\n\t\t/**");
        pathCommands.add("\t* Crop Box properties");
        pathCommands.add("\t*/ ");
        pathCommands.add("\tPath path_"+shapeCount+" = new Path();");
    	pathCommands.add("\tObservableList<PathElement> "+"shape_"+shapeCount+" = path_"+shapeCount+".getElements();");
    	pathCommands.add("\taddToGroup.add(path_"+shapeCount+");");//add the cropBox to the group
    
        double[] coords = {cropBox.x, cropBox.y};
        
        pathCommands.add("\tshape_"+shapeCount+".add(new MoveTo("+convertCoords(coords, 2)+"));");
        coords[0] += cropBox.width;
        pathCommands.add("\tshape_"+shapeCount+".add(new LineTo("+convertCoords(coords, 2)+"));");
        coords[1] += cropBox.height;
        pathCommands.add("\tshape_"+shapeCount+".add(new LineTo("+convertCoords(coords, 2)+"));");
        coords[0] -= cropBox.width;
        pathCommands.add("\tshape_"+shapeCount+".add(new LineTo("+convertCoords(coords, 2)+"));");
    }
    
    protected void applyGraphicsStateToPath(GraphicsState gs){
        int fillType = gs.getFillType();

        if(fillType==GraphicsState.FILL || fillType==GraphicsState.FILLSTROKE) {
            PdfPaint col = gs.getNonstrokeColor();

            if(windingRule == PathIterator.WIND_EVEN_ODD) {
                pathCommands.add("\tpath_"+shapeCount+".setFillRule(FillRule.EVEN_ODD);");
            }

            float fillOpacity=gs.getAlpha(GraphicsState.FILL);
            if(fillOpacity!=1){
                pathCommands.add("\tpath_"+shapeCount+".setOpacity("+ fillOpacity + ");");
            }

            pathCommands.add("\tpath_"+shapeCount+".setStroke(null);"); // The default value is null for all shapes except Line, Polyline, and Path. The default value is Color.BLACK for those shapes.

            pathCommands.add("\tpath_"+shapeCount+".setFill(Color."+ rgbToCSSColor(col.getRGB()) + ");");
            currentColor = col.getRGB();
        }

        if(fillType==GraphicsState.STROKE || fillType==GraphicsState.FILLSTROKE) {
            BasicStroke stroke = (BasicStroke) gs.getStroke();

            //allow for any opacity
            float strokeOpacity=gs.getAlpha(GraphicsState.STROKE);
            if(strokeOpacity!=1){
                pathCommands.add("\tpath_"+shapeCount+".setOpacity("+ strokeOpacity + ");"); // JavaFX fills & strokes cannot have separate opacities.
            }

            if(gs.getOutputLineWidth() != 1) { //attribute double lineWidth; (default 1)
                pathCommands.add("\tpath_"+shapeCount+".setStrokeWidth(" + gs.getOutputLineWidth() + ");");
            }

            if(stroke.getMiterLimit()!=10) {  //attribute double miterLimit; // (default 10)
            	pathCommands.add("\tpath_"+shapeCount+".setStrokeMiterLimit(" + ((double) stroke.getLineWidth()*scaling) + ");");
            }

            pathCommands.add("\tpath_"+shapeCount+".setStrokeLineCap(StrokeLineCap." + determineLineCap(stroke) + ");");
            pathCommands.add("\tpath_"+shapeCount+".setStrokeLineJoin(StrokeLineJoin." + determineLineJoin(stroke) + ");");

            PdfPaint col = gs.getStrokeColor();
            pathCommands.add("\tpath_"+shapeCount+".setStroke(Color."+ rgbToCSSColor(col.getRGB()) + ");");
        }
    }

    /**
     * Extract line cap attribute javaFX implementation
     */
    protected static String determineLineCap(BasicStroke stroke) {//javaFX implementation
        //attribute DOMString lineCap; // "butt", "round", "square" (default "butt")
        String attribute;

        switch(stroke.getEndCap()) {
            case(BasicStroke.CAP_ROUND):
                attribute = "ROUND";
                break;
            case(BasicStroke.CAP_SQUARE):
                attribute = "SQUARE";
                break;
            default:
                attribute = "BUTT";
                break;
        }
        return attribute;
    }
    
    /**
     * Extract line join attribute javaFX implementation
     */
    protected static String determineLineJoin(BasicStroke stroke) {//javaFX implementation
        //attribute DOMString lineJoin; // "round", "bevel", "miter" (default "miter")
        String attribute;
        switch(stroke.getLineJoin()) {
            case(BasicStroke.JOIN_ROUND):
                attribute = "ROUND";
                break;
            case(BasicStroke.JOIN_BEVEL):
                attribute = "BEVEL";
                break;
            default:
                attribute = "MITER";
                break;
        }
        return attribute;
    }

	public void setShapeNumber(int shapeCount) {
		this.shapeCount=shapeCount;
	}

}