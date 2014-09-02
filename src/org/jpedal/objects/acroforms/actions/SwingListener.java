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
 * SwingListener.java
 * ---------------
 */
package org.jpedal.objects.acroforms.actions;

import org.jpedal.external.Options;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.acroforms.creation.FormFactory;
        import org.jpedal.objects.acroforms.rendering.AcroRenderer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import java.awt.event.*;

public class SwingListener extends PDFListener implements MouseListener, KeyListener, FocusListener, MouseMotionListener, ActionListener, ListSelectionListener{
    /*
     * deciphering characteristics from formObject bit 1 is index 0 in []
     * 1 = invisible
     * 2 = hidden - dont display or print
     * 3 = print - print if set, dont if not
     * 4 = nozoom
     * 5= norotate
     * 6= noview
     * 7 = read only (ignored by wiget)
     * 8 = locked
     * 9 = togglenoview
     */

    public SwingListener(FormObject form, AcroRenderer acroRend, ActionHandler formsHandler) {

        super(form, acroRend, formsHandler);
    }

    public void mouseClicked(MouseEvent e) {
    	if(debugMouseActions)
    		System.out.println("SwingListener.mouseClicked()");
    	
    	//<start-adobe><start-thin><start-wrap>
    	//added to check the forms save flag to tell the user how to save the now changed pdf file
    	if(acrorend.getFormFactory().getType() == FormFactory.SWING){
	        org.jpedal.examples.viewer.gui.SwingGUI gui = ((org.jpedal.examples.viewer.gui.SwingGUI) handler.getPDFDecoder().getExternalHandler(Options.SwingContainer));
    	    if(gui!=null)
	    		gui.checkformSavedMessage();
    	}
    	//<end-wrap><end-thin><end-adobe>
    	
        super.mouseClicked(e);
        
        if(e.getSource() instanceof JToggleButton){
	        
	        //moved to mouseClicked as it gives a much better smoothness to the action

        	//store value so if multiple kid values across page with Sync
	        acrorend.getCompData().flagLastUsedValue(e.getSource(),formObject,true);
//	        acrorend.getCompData().syncAllValues();
        }

        //<start-adobe><start-thin>
		handler.updateCordsFromFormComponent(e,true);
		//<end-thin><end-adobe>
    }

    public void mousePressed(MouseEvent e) {
    	if(debugMouseActions)
    		System.out.println("SwingListener.mousePressed()");
        super.mousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
    	if(debugMouseActions)
    		System.out.println("SwingListener.mouseReleased()");
        super.mouseReleased(e);
        
    }

    public void mouseEntered(MouseEvent e) {
    	if(debugMouseActions)
    		System.out.println("SwingListener.mouseEntered()");

        handler.A(e, formObject, ActionHandler.MOUSEENTERED);
        handler.E(e, formObject);

        if (formObject.getCharacteristics()[8]) {//togglenoView
        	acrorend.getCompData().setCompVisible(formObject.getObjectRefAsString(), true);
        }/*else if(command.equals("comboEntry")){
			((JComboBox) e.getSource()).showPopup();
		}*/

    }

    public void mouseExited(MouseEvent e) {
    	if(debugMouseActions)
    		System.out.println("SwingListener.mouseExited()");

        handler.A(e, formObject, ActionHandler.MOUSEEXITED);
        handler.X(e, formObject);

        if (formObject.getCharacteristics()[8]) {//togglenoView
        	acrorend.getCompData().setCompVisible(formObject.getObjectRefAsString(), false);
        }/*else if(command.equals("comboEntry")){
			((JComboBox) e.getSource()).hidePopup();
		}*/

    }

    public void keyTyped(KeyEvent e) { //before key added to data
    	if(debugMouseActions)
    		System.out.println("SwingListener.keyTyped("+e+ ')');

        boolean keyIgnored=false;

        //set length
        int maxLength = formObject.getInt(PdfDictionary.MaxLen);

        if(maxLength!=-1){

            char c=e.getKeyChar();
            
            if(c!=8 && c!=127){

                JTextComponent comp= ((JTextComponent) e.getSource());

                String text=comp.getText();
                
                int length=text.length();
                if(length>=maxLength){
                    e.consume();
                    keyIgnored=true;
                }

                if(length>maxLength)
                comp.setText(text.substring(0,maxLength));

            }
        }

        //if valid process further
        if(!keyIgnored){
        	
        	if(e.getKeyChar()=='\n' && !(e.getSource() instanceof JTextArea))
        		((JComponent)e.getSource()).transferFocus();
    			//acrorend.getCompData().loseFocus();

            int rejectKey=handler.K(e, formObject, ActionHandler.MOUSEPRESSED);

            if(rejectKey==ActionHandler.REJECTKEY)
            e.consume();

            handler.V(e,formObject, ActionHandler.MOUSEPRESSED);

        }
    }

    public void keyPressed(KeyEvent e) {
    	if(debugMouseActions)
    		System.out.println("SwingListener.keyPressed("+e+ ')');
        //ignored at present
    }

    public void keyReleased(KeyEvent e) { //after key added to component value
    	if(debugMouseActions)
    		System.out.println("SwingListener.keyReleased("+e+ ')');

        super.keyReleased(e);
    }

    public void focusGained(FocusEvent e) {
    	if(debugMouseActions)
    		System.out.println("SwingListener.focusGained()");

        super.focusGained(e);
    }

    public void focusLost(FocusEvent e) {
    	if(debugMouseActions)
    		System.out.println("SwingListener.focusLost()");
    	
        super.focusLost(e);
        
        if(!(e.getSource() instanceof JToggleButton)){
        	//store value so if multiple kid values across page with Sync
        	acrorend.getCompData().flagLastUsedValue(e.getComponent(),formObject,true);
//        	acrorend.getCompData().syncAllValues();
        }
    }

	public void mouseDragged(MouseEvent e) {
//		if(debugMouseActions)
//    		System.out.println("SwingListener.mouseDragged()");
		//ignored at present
	}

	public void mouseMoved(MouseEvent e) {
//		if(debugMouseActions)
//    		System.out.println("SwingListener.mouseMoved()");
		//<start-adobe><start-thin>
		handler.updateCordsFromFormComponent(e,false);
		//<end-thin><end-adobe>
	}

	public void actionPerformed(ActionEvent e) {
		if(debugMouseActions)
			System.out.println("SwingListener.actionPerformed()");
		
		//this is called by ulc instead of mouseclicked
		mouseClicked(e);
	}

	public void valueChanged(ListSelectionEvent e) {
		if(debugMouseActions)
			System.out.println("SwingListener.valueChanged()");
		
		//added so the list selection can be updated to the proxy
		mouseClicked(e);
	}
}
