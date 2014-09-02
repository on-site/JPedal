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
 * SwingMousePanMode.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.swing;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import org.jpedal.PdfDecoder;
import org.jpedal.examples.viewer.gui.SwingGUI;
import org.jpedal.external.Options;

public class SwingMousePanMode implements SwingMouseFunctionality{

	private Point currentPoint;
	private PdfDecoder decode_pdf;
	private Rectangle currentView;
	
	public SwingMousePanMode(PdfDecoder decode_pdf) {
		this.decode_pdf=decode_pdf;
	}

	public void setupMouse() {
		/**
		 * track and display screen co-ordinates and support links
		 */

        //set cursor
        SwingGUI gui = ((SwingGUI)decode_pdf.getExternalHandler(Options.SwingContainer));
        decode_pdf.setCursor(gui.getCursor(SwingGUI.GRAB_CURSOR));
	}
	
	public void mouseClicked(MouseEvent arg0) {
		
	}

	public void mouseEntered(MouseEvent arg0) {

	}
	
	public void mouseExited(MouseEvent arg0) {

	}

	public void mousePressed(MouseEvent arg0) {
        if (arg0.getButton()==MouseEvent.BUTTON1 
        		|| arg0.getButton()==MouseEvent.NOBUTTON) {
            currentPoint = arg0.getPoint();
            currentView = decode_pdf.getVisibleRect();

            //set cursor
            SwingGUI gui = ((SwingGUI)decode_pdf.getExternalHandler(Options.SwingContainer));
            decode_pdf.setCursor(gui.getCursor(SwingGUI.GRABBING_CURSOR));
        }
	}

	public void mouseReleased(MouseEvent arg0) {
        //reset cursor
        SwingGUI gui = ((SwingGUI)decode_pdf.getExternalHandler(Options.SwingContainer));
        decode_pdf.setCursor(gui.getCursor(SwingGUI.GRAB_CURSOR));
	}

	public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            final Point newPoint = e.getPoint();

            int diffX = currentPoint.x-newPoint.x;
            int diffY = currentPoint.y-newPoint.y;


            Rectangle view = currentView;

            view.x +=diffX;

            view.y +=diffY;



            if(!view.contains(decode_pdf.getVisibleRect()))
                decode_pdf.scrollRectToVisible(view);
        }
    }

	public void mouseMoved(MouseEvent e) {
		
	}

}