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
 * StatusBar.java
 * ---------------
 */
package org.jpedal.io;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.jpedal.utils.LogWriter;

/**
 * encapsulates a status bar to display progess of a page decode and messages 
 * for a GUI client and methods to access it - 
 * See org.examples.jpedal.viewer.Viewer for example of usage
 *  
 */
public class StatusBar
{

	/**amount of detail to show*/
	private static final int debug_level = 0;

	/**current numeric value of Progress bar*/
	private int progress_size = 0;
	
	/**message to display*/
	private String current="";

	/**numeric value Progress bar will count to (lines in data file)*/
	private static final int progress_max_size = 100;

	/**actual status bar*/
	private JProgressBar status = null;

	/**if there is a GUI display*/
	private  boolean showMessages = false;
	
	/**amount done on decode*/
	public float percentageDone=0;
	
	/** master color for statusBar, by default is red*/
	private Color masterColor=null;
	
    /** boolean to show when the status has been reset*/
    private boolean reset=false;
	
	/** initialises statusbar using default colors */
	public StatusBar()
	{
		initialiseStatus("");
	}
	
	/** initialises statusbar using specified color */
	public StatusBar(Color newColor)
	{
		masterColor=newColor;
		initialiseStatus("");
	}
	
	////////////////////////////////////////
	/**
	 * initiate status bar
	 */
	final public void initialiseStatus( String current)
	{
		progress_size = 0;
		status = new JProgressBar();
		if(masterColor!=null)
			status.setForeground(masterColor);
		//show that somethings happerning but not sure how long for
		//status.setIndeterminate(true);
		status.setStringPainted( true );
		status.setMaximum( progress_max_size );
		status.setMinimum( 0 );
		updateStatus( current, 4 );
	}
	////////////////////////////////////////
	/**
	 * update status if client being used
	 * also writes to log (called internally as file decoded)
	 */
	final public void updateStatus( String progress_bar, int debug_level_to_use )
	{

		current=progress_bar;
		
		//update status if in client
		if( showMessages == true )
		{
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){		
					status.setString( current );
					status.setValue( progress_size );
				}
			});
	
		}
		if( debug_level > debug_level_to_use && LogWriter.isOutput())
			LogWriter.writeLog( progress_bar );
	}
	/////////////////////////////////////////////
	/**
	 * return handle on status bar so it can be displayed
	 */
	final public Component getStatusObject()
	{
		return status;
	}

    /**
	 * set progress value (called internally as page decoded)
	 */
	final public void setProgress( int size )
	{
        reset=false;
	    if(status!=null){
		if(size==0)
			progress_size=0;
		if( progress_size < size )
			progress_size = size;
		//if( showMessages == true ){
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
                    status.setValue( progress_size );

				}
			});
		}
	}
	////////////////////////////////////////
	/**
	 * set progress value (called internally as page decoded)
	 */
	final public void setProgress( final String message, int size )
	{
        reset=false;
	    if(status!=null){
		if(size==0)
			progress_size=0;
		if( progress_size < size )
			progress_size = size;
		//if( showMessages == true ){
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
                    status.setString(message);
					status.setValue( progress_size );

				}
			});
		}
	}
	////////////////////////////////////////////////////////////
	/**
	 * reset status bar
	 */
	final public void resetStatus( String current )
	{
        reset = true;
		progress_size = 0;
		updateStatus( current, 4 );
	}

	//////////////////////////
	/**
	 * set client flag to display
	 */
	final public void setClientDisplay()
	{
		showMessages = true;
	}
	
	public void setVisible(boolean visible){
		status.setVisible(visible);
	}
	
	public void setEnabled(boolean enable){
		status.setEnabled(enable);
	}
	
	public boolean isVisible(){
		return status.isVisible();
	}
	
	public boolean isEnabled(){
		return status.isEnabled();
	}

    public boolean isDone() {
        return reset || progress_size >= progress_max_size;
    }
	
}
