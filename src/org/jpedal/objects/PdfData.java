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
 * PdfData.java
 * ---------------
 */
package org.jpedal.objects;

import org.jpedal.color.GenericColorSpace;
import org.jpedal.utils.Fonts;


/**
 * <p>
 * holds text data for extraction & manipulation
 * </p>
 * <p>
 * Pdf routines create 'raw' text data
 * </p>
 * <p>
 * grouping routines will attempt to intelligently stitch together and leave as
 * 'processed data' in this class
 * </p>
 * <p>
 * <b>NOTE ONLY methods (NOT public variables) are part of API </b>
 * </p>
 *
 */
public class PdfData
{

	/**identify type as text*/
	public static final int TEXT = 0;

	/**identify type as image*/
	public static final int IMAGE = 1;

	/**test orientation*/
	public static final int HORIZONTAL_LEFT_TO_RIGHT = 0;

	public static final int HORIZONTAL_RIGHT_TO_LEFT = 1;

	public static final int VERTICAL_TOP_TO_BOTTOM = 2;

	public static final int VERTICAL_BOTTOM_TO_TOP = 3;

	private int pointer=0;

	/**flag to show x co-ord has been embedded in content*/
	private boolean widthIsEmbedded=false;

	/**local store for max and widthheight of page*/
	public float maxY=0,maxX=0;

    /**used to hide our encoding values in fragments of data so we can strip out*/
    public static final String marker = (String.valueOf(((char) (0))));
    public static final String hiddenMarker = (String.valueOf(((char) (65534))));

    /**initial array size*/
    protected int max=2000;

    /**hold the raw content*/
    public String[] contents=new String[max];

    /**hold flag on raw content orientation*/
    public int[] f_writingMode=new int[max];

    /**hold raw content*/
    public int[] text_length=new int[max];

    /**hold raw content*/
    public int[] move_command=new int[max];

    /**hold raw content*/
    public float[] f_character_spacing=new float[max];

    /**hold raw content*/
    public int[] f_end_font_size=new int[max];

    /**hold raw content*/
    public float[] space_width=new float[max];

    /**hold raw content*/
    public float[] f_x1=new float[max];

    /**hold color content*/
    public String[] colorTag=new String[max];

    /**hold raw content*/
    public float[] f_x2=new float[max];

    /**hold raw content*/
    public float[] f_y1=new float[max];

    /**hold raw content*/
    public float[] f_y2=new float[max];

    boolean isColorExtracted;

    /** create empty object to hold content*/
	public PdfData(){}

	/**
	 * get number of raw objects on page
	 */
	final public int getRawTextElementCount()
	{
		return pointer;
	}

	/**
	 * clear store of objects once written out
	 * to reclaim memory. If flag set, sets data to
	 * state after page decoded before grouping for reparse
	 */
	final public void flushTextList( boolean reinit )
	{

		if(!reinit){

			pointer=0;

			max=2000;

			contents=new String[max];
			f_writingMode=new int[max];
			text_length=new int[max];
			move_command=new int[max];
			f_character_spacing=new float[max];
			f_end_font_size=new int[max];
			space_width=new float[max];
			f_x1=new float[max];
			f_x2=new float[max];
			f_y1=new float[max];
			f_y2=new float[max];

			colorTag=new String[max];
		}
	}


	/**
	 * store line of raw text for later processing
	 */
	final public void addRawTextElement( float character_spacing,int writingMode,
			String font_as_string,  float current_space, int fontSize,
			float x1, float y1, float x2, float y2, int move_type,
			StringBuffer processed_line, int current_text_length, String currentColorTag, boolean isXMLExtraction){

		if(processed_line.length()>0){

			//add tokens
			if(isXMLExtraction){
				processed_line.insert( 0, font_as_string );
				processed_line.append( Fonts.fe );
			}

			//add color token
			if(isColorExtracted){
				processed_line.insert( 0, currentColorTag );
				processed_line.append( GenericColorSpace.ce );
			}

			f_writingMode[pointer]=writingMode;
			text_length[pointer]=current_text_length;
			move_command[pointer]=move_type;
			f_character_spacing[pointer]=character_spacing;
			f_x1[pointer]=x1;
			colorTag[pointer]=currentColorTag;
			f_x2[pointer]=x2;
			f_y1[pointer]=y1;
			f_y2[pointer]=y2;
			contents[pointer]=processed_line.toString();
			f_end_font_size[pointer]=fontSize;
			space_width[pointer]=current_space*1000;

            pointer++;

			//resize pointers
			if(pointer==max)
				resizeArrays(0);
		}
	}

	/**
	 * resize arrays to add newItems to end (-1 makes it grow)
	 */
	private void resizeArrays(int newItems) {

		float[] temp_f;
		int[] temp_i;
		String[] temp_s;

		if(newItems<0){
			max=-newItems;
			pointer=max;
		}else if(newItems==0){
			if(max<5000)
				max=max*5;
			else if(max<10000)
				max=max*2;
			else
				max=max+1000;
		}else{
			max=contents.length+newItems-1;
			pointer=contents.length;
		}

		temp_s=contents;
		contents = new String[max];
		System.arraycopy( temp_s, 0, contents, 0, pointer );

		temp_i=f_writingMode;
		f_writingMode=new int[max];
		f_writingMode = new int[max];
		System.arraycopy( temp_i, 0, f_writingMode, 0, pointer );

		temp_s=colorTag;
		colorTag = new String[max];
		System.arraycopy( temp_s, 0, colorTag, 0, pointer );

		temp_i=text_length;
		text_length = new int[max];
		System.arraycopy( temp_i, 0, text_length, 0, pointer );

		temp_i=move_command;
		move_command = new int[max];
		System.arraycopy( temp_i, 0, move_command, 0, pointer );

		temp_f=f_character_spacing;
		f_character_spacing = new float[max];
		System.arraycopy( temp_f, 0, f_character_spacing, 0, pointer );

		temp_i=f_end_font_size;
		f_end_font_size = new int[max];
		System.arraycopy( temp_i, 0, f_end_font_size, 0, pointer );

		temp_f=space_width;
		space_width = new float[max];
		System.arraycopy( temp_f, 0, space_width, 0, pointer );

		temp_f=f_x1;
		f_x1 = new float[max];
		System.arraycopy( temp_f, 0, f_x1, 0, pointer );

		temp_f=f_x2;
		f_x2 = new float[max];
		System.arraycopy( temp_f, 0, f_x2, 0, pointer );

		temp_f=f_y1;
		f_y1 = new float[max];
		System.arraycopy( temp_f, 0, f_y1, 0, pointer );

		temp_f=f_y2;
		f_y2 = new float[max];
		System.arraycopy( temp_f, 0, f_y2, 0, pointer );
	}

	/**
	 * set flag to show width in text
	 */
	public void widthIsEmbedded() {

		widthIsEmbedded=true;

	}

	/**
	 * show if width in text
	 */
	public boolean IsEmbedded() {

		return widthIsEmbedded;

	}

    /**
     * set colour extraction
     */
    public void enableTextColorDataExtraction() {
        isColorExtracted=true;
    }

    /**
     * flag to show if color extracted in xml
     */
    public boolean isColorExtracted() {
        return isColorExtracted;
    }
	
	public void dispose(){

        contents=null;

        f_writingMode=null;

        text_length=null;

        move_command=null;

        f_character_spacing=null;

        f_end_font_size=null;

        space_width=null;

        f_x1=null;

        colorTag=null;

        f_x2=null;

        f_y1=null;

        f_y2=null;

	}
}
