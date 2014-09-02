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
 * MultiViewListener.java
 * ---------------
 */

package org.jpedal.examples.viewer;

import java.io.File;

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.jpedal.PdfDecoder;
import org.jpedal.examples.viewer.gui.SwingGUI;

public class MultiViewListener implements InternalFrameListener {

	Object pageScaling = null, pageRotation = null;
	private PdfDecoder decode_pdf;
	private SwingGUI currentGUI;
	private Values commonValues;
	private Commands currentCommands;

	public MultiViewListener(PdfDecoder decode_pdf, SwingGUI currentGUI, Values commonValues, Commands currentCommands){
		this.decode_pdf = decode_pdf;
		this.currentGUI = currentGUI;
		this.commonValues = commonValues;
		this.currentCommands = currentCommands;

		// pageScaling = "Window";
		// pageRotation = currentGUI.getRotation();

		// System.out.println("constructor"+ " "+pageScaling+" " +pageRotation);
	}

	public void internalFrameOpened(InternalFrameEvent e) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void internalFrameClosing(InternalFrameEvent e) {
		currentGUI.setBackNavigationButtonsEnabled(false);
		currentGUI.setForwardNavigationButtonsEnabled(false);
		currentGUI.resetPageNav();
	}

	public void internalFrameClosed(InternalFrameEvent e) {

		decode_pdf.flushObjectValues(true);

		decode_pdf.closePdfFile();
	}

	public void internalFrameIconified(InternalFrameEvent e) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void internalFrameDeiconified(InternalFrameEvent e) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}

	/**
	 * switch to active PDF
	 * @param e
	 */
	public void internalFrameActivated(InternalFrameEvent e) {
		
		//System.out.println("activated pdf = "+decode_pdf.getClass().getName() + "@" + Integer.toHexString(decode_pdf.hashCode()));
		// choose selected PDF
		currentGUI.setPdfDecoder(decode_pdf);
		currentCommands.setPdfDecoder(decode_pdf);
		/**
		 * align details in Viewer and variables
		 */
		int page=decode_pdf.getlastPageDecoded();

		commonValues.setPageCount(decode_pdf.getPageCount());
		commonValues.setCurrentPage(page);

		String fileName = decode_pdf.getFileName();
		if(fileName!=null){
			commonValues.setSelectedFile(fileName);
			File file = new File(fileName);
			commonValues.setInputDir(file.getParent());
			commonValues.setFileSize(file.length() >> 10);
		}
		
		// System.err.println("ACTIVATED "+pageScaling+" "+pageRotation+"
		// count="+decode_pdf.getPageCount()/*+"
		// "+localPdf.getDisplayRotation()+" "+localPdf.getDisplayScaling()*/);

		commonValues.setPDF(currentCommands.isPDF());
		commonValues.setMultiTiff(currentGUI.isMultiPageTiff());
		
		//System.err.println("ACTIVATED "+pageScaling+" "+pageRotation+" count="+decode_pdf.getPageCount()/*+" "+localPdf.getDisplayRotation()+" "+localPdf.getDisplayScaling()*/);

		
		if (pageScaling != null)
			currentGUI.setSelectedComboItem(Commands.SCALING, pageScaling.toString());

		if (pageRotation != null)
			currentGUI.setSelectedComboItem(Commands.ROTATION, pageRotation.toString());

// currentGUI.setPage(page);
// //pageCounter2.setText(""+page);

// pageCounter3.setText(""+decode_pdf.getPageCount());

		currentGUI.setPageNumber();

		decode_pdf.updateUI();

		currentGUI.removeSearchWindow(false);
// searchFrame.removeSearchWindow(false);
		
		//Only show navigation buttons required for newly activated frame
		currentGUI.hideRedundentNavButtons();
	}

	public void internalFrameDeactivated(InternalFrameEvent e) {

		// save current settings
		if (pageScaling != null) {
			pageScaling = currentGUI.getSelectedComboItem(Commands.SCALING);
		}
		if (pageRotation != null) {
			pageRotation = currentGUI.getSelectedComboItem(Commands.ROTATION);
		}
		// System.err.println("DEACTIVATED "+pageScaling+" "+pageRotation);
	}

	public void setPageProperties(Object rotation, Object scaling) {
		pageRotation = rotation;
		pageScaling = scaling;
	}
}
