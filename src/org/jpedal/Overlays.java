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
 * Overlays.java
 * ---------------
 */
package org.jpedal;

import org.jpedal.exception.PdfException;
import org.jpedal.render.DynamicVectorRenderer;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Overlays {

    private final Map overlayType = new HashMap();
    private final Map overlayColors = new HashMap();
    private final Map overlayObj = new HashMap();

    private final Map overlayTypeG = new HashMap();
    private final Map overlayColorsG = new HashMap();
    private final Map overlayObjG = new HashMap();

    public void printAdditionalObjectsOverPage(int page, int[] type, Color[] colors, Object[] obj) throws PdfException {


        Integer key = page;

        if (obj == null) { //flush page

            overlayType.remove(key);
            overlayColors.remove(key);
            overlayObj.remove(key);

        } else { //store for printing and add if items already there



            int[] oldType = (int[]) overlayType.get(key);
            if (oldType == null){
                overlayType.put(key, type);

            }else { //merge items

                int oldLength = oldType.length;
                int newLength = type.length;
                int[] combined = new int[oldLength + newLength];

                System.arraycopy(oldType, 0, combined, 0, oldLength);

                System.arraycopy(type, 0, combined, oldLength, newLength);

                overlayType.put(key, combined);
            }


            Color[] oldCol = (Color[]) overlayColors.get(key);
            if (oldCol == null)
                overlayColors.put(key, colors);
            else { //merge items

                int oldLength = oldCol.length;
                int newLength = colors.length;
                Color[] combined = new Color[oldLength + newLength];

                System.arraycopy(oldCol, 0, combined, 0, oldLength);

                System.arraycopy(colors, 0, combined, oldLength, newLength);

                overlayColors.put(key, combined);
            }

            Object[] oldObj = (Object[]) overlayObj.get(key);

            if (oldType == null)
                overlayObj.put(key, obj);
            else { //merge items

                int oldLength = oldObj.length;
                int newLength = obj.length;
                Object[] combined = new Object[oldLength + newLength];

                System.arraycopy(oldObj, 0, combined, 0, oldLength);

                System.arraycopy(obj, 0, combined, oldLength, newLength);

                overlayObj.put(key, combined);
            }
        }
    }

    public void printAdditionalObjectsOverAllPages(int[] type, Color[] colors, Object[] obj) throws PdfException {

        Integer key = -1;

        if (obj == null) { //flush page

            overlayTypeG.remove(key);
            overlayColorsG.remove(key);
            overlayObjG.remove(key);

        } else { //store for printing and add if items already there

            int[] oldType = (int[]) overlayTypeG.get(key);
            if (oldType == null){
                overlayTypeG.put(key, type);

            }else { //merge items

                int oldLength = oldType.length;
                int newLength = type.length;
                int[] combined = new int[oldLength + newLength];

                System.arraycopy(oldType, 0, combined, 0, oldLength);

                System.arraycopy(type, 0, combined, oldLength, newLength);

                overlayTypeG.put(key, combined);
            }


            Color[] oldCol = (Color[]) overlayColorsG.get(key);
            if (oldCol == null)
                overlayColorsG.put(key, colors);
            else { //merge items

                int oldLength = oldCol.length;
                int newLength = colors.length;
                Color[] combined = new Color[oldLength + newLength];

                System.arraycopy(oldCol, 0, combined, 0, oldLength);

                System.arraycopy(colors, 0, combined, oldLength, newLength);

                overlayColorsG.put(key, combined);
            }

            Object[] oldObj = (Object[]) overlayObjG.get(key);

            if (oldType == null)
                overlayObjG.put(key, obj);
            else { //merge items

                int oldLength = oldObj.length;
                int newLength = obj.length;
                Object[] combined = new Object[oldLength + newLength];

                System.arraycopy(oldObj, 0, combined, 0, oldLength);

                System.arraycopy(obj, 0, combined, oldLength, newLength);

                overlayObjG.put(key, combined);
            }
        }
    }
    public void clear() {

        //flush arrays
        overlayType.clear();
        overlayColors.clear();
        overlayObj.clear();

        //flush arrays
        overlayTypeG.clear();
        overlayColorsG.clear();
        overlayObjG.clear();
    }

    public void printOverlays(DynamicVectorRenderer dvr, int page) throws PdfException {

        //store for printing (global first)
        Integer keyG = -1;
        int[] typeG = (int[]) overlayTypeG.get(keyG);
        Color[] colorsG = (Color[]) overlayColorsG.get(keyG);
        Object[] objG = (Object[]) overlayObjG.get(keyG);

        //add to screen display
        dvr.drawAdditionalObjectsOverPage(typeG, colorsG, objG);

        //store for printing
        Integer key = page;

        int[] type = (int[]) overlayType.get(key);
        Color[] colors = (Color[]) overlayColors.get(key);
        Object[] obj = (Object[]) overlayObj.get(key);

        //add to screen display
        dvr.drawAdditionalObjectsOverPage(type, colors, obj);
    }
}
