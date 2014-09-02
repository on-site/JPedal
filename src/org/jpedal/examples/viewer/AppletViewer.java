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
 * AppletViewer.java
 * ---------------
 */
package org.jpedal.examples.viewer;

/**standard Java stuff*/
import javax.swing.*;

import org.jpedal.io.ObjectStore;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * <br>Description: Demo to show JPedal being used 
 * as a GUI viewer in an applet,
 * and to demonstrate some of JPedal's capabilities
 * 
 *   See also http://www.jpedal.org/gplSrc/org/jpedal/examples/viewer/Viewer.java.html
 */
public class AppletViewer extends JApplet{

	private static final long serialVersionUID = 8823940529835337414L;


	Viewer current = new Viewer(Values.RUNNING_APPLET);

    boolean isInitialised=false;
    boolean destroy = false;

	/** main method to run the software */
	public void init()
	{

        if(!isInitialised){

            isInitialised=true;
            
          //<start-wrapper>
            String props = getParameter("propertiesFile");
            if (props != null) {
                current.loadProperties(props);
            }else{
            	//If no file set use default from jar
            	current.loadProperties(Viewer.PREFERENCES_DEFAULT);
            }
            //<end-wrapper>
            
            current.setupViewer();
            
            /**
             * pass in flag and pickup - we could extend to check and set all values
             */
            String mem = getParameter("org.jpedal.memory");
            if (mem!= null && mem.equals("true")) {
            	System.setProperty("org.jpedal.memory", "true");
            }

            if (current.currentGUI.getFrame() instanceof JFrame)
                this.getContentPane().add(((JFrame)current.currentGUI.getFrame()).getContentPane());
            else
                this.getContentPane().add(current.currentGUI.getFrame());

            //<start-wrap>
            /**
            //<end-wrap>
            current.openDefaultFile();
             current.executeCommand(Commands.FACING,null);
            /**/
        }

    }

    public void start(){

        //ensure setup
        init();


        /**

         if (SwingUtilities.isEventDispatchThread()) {

            current.executeCommand(Commands.FACING,null);
        } else {

			final Runnable doPaintComponent = new Runnable() {
				public void run() {
                    current.executeCommand(Commands.FACING,null);
				}
			};
			SwingUtilities.invokeLater(doPaintComponent);
		}
         /**/
	}
	
	public void destroy(){
        destroy = true;

        Viewer.exitOnClose=false;
        current.executeCommand(Commands.EXIT,null);
        
		//ensure cached items removed
		ObjectStore.flushPages();
	}


}
