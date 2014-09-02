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
 * SwingMouseListener.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jpedal.Display;
import org.jpedal.PdfDecoder;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.MouseMode;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.gui.SwingGUI;
import org.jpedal.examples.viewer.gui.generic.GUIMouseHandler;
import org.jpedal.external.Options;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.render.DynamicVectorRenderer;

public class SwingMouseListener implements GUIMouseHandler, MouseListener, MouseMotionListener, MouseWheelListener {

	private PdfDecoder decode_pdf;
	private SwingGUI currentGUI;
	private Values commonValues;
	private Commands currentCommands;
	
	SwingMouseSelector selectionFunctions;
	SwingMousePanMode panningFunctions;
	SwingMousePageTurn pageTurnFunctions;
	
	//Custom mouse function
	private static SwingMouseFunctionality customMouseFunctions;

    private boolean scrollPageChanging=false;
	
	/**current cursor position*/
	private int cx,cy;
	
	/**tells user if we enter a link*/
	private String message="";

    /**
     * tracks mouse operation mode currently selected
     */
    private MouseMode mouseMode=new MouseMode();
	
	private AutoScrollThread scrollThread = new AutoScrollThread();
	
	public SwingMouseListener(PdfDecoder decode_pdf, SwingGUI currentGUI,
			Values commonValues,Commands currentCommands) {

		this.decode_pdf=decode_pdf;
		this.currentGUI=currentGUI;
		this.commonValues=commonValues;
		this.currentCommands=currentCommands;
        this.mouseMode=currentCommands.getMouseMode();
		
		selectionFunctions = new SwingMouseSelector(decode_pdf, currentGUI, commonValues, currentCommands);
		panningFunctions = new SwingMousePanMode(decode_pdf);
		pageTurnFunctions = new SwingMousePageTurn(decode_pdf, currentGUI, commonValues, currentCommands);

		if (SwingUtilities.isEventDispatchThread()){
            scrollThread.init();
        }else {
            final Runnable doPaintComponent = new Runnable() {
                public void run() {
                    scrollThread.init();
                }
            };
            SwingUtilities.invokeLater(doPaintComponent);
        }
		
		decode_pdf.addExternalHandler(this, Options.SwingMouseHandler);
	}

	public void setupExtractor() {
		System.out.println("Set up extractor called");
		decode_pdf.addMouseMotionListener(this);
		decode_pdf.addMouseListener(this);

	}

	public void setupMouse() {
		/**
		 * track and display screen co-ordinates and support links
		 */
		decode_pdf.addMouseMotionListener(this);
		decode_pdf.addMouseListener(this);
		decode_pdf.addMouseWheelListener(this);

		//set cursor
		decode_pdf.setDefaultCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

	}

	public void updateRectangle() {
		switch(mouseMode.getMouseMode()){

		case MouseMode.MOUSE_MODE_TEXT_SELECT :
			selectionFunctions.updateRectangle();
			break;

		case MouseMode.MOUSE_MODE_PANNING :
			break;

		}
	}

	public void mouseClicked(MouseEvent e) {
		switch(mouseMode.getMouseMode()){

		case MouseMode.MOUSE_MODE_TEXT_SELECT :
			selectionFunctions.mouseClicked(e);
			break;

		case MouseMode.MOUSE_MODE_PANNING :
			//Does Nothing so ignore
			break;

		}
		
		if (currentCommands.getPages().getBoolean(Display.BoolValue.TURNOVER_ON) && decode_pdf.getDisplayView()==Display.FACING){
			pageTurnFunctions.mouseClicked(e);
		}
		
		if(customMouseFunctions!=null){
			customMouseFunctions.mouseClicked(e);
		}
	}

	public void mouseEntered(MouseEvent e) {
		switch(mouseMode.getMouseMode()){

		case MouseMode.MOUSE_MODE_TEXT_SELECT :
			//Text selection does nothing here
			//selectionFunctions.mouseEntered(e);
			break;

		case MouseMode.MOUSE_MODE_PANNING :
			//Does Nothing so ignore
			break;

		}
		
		if (currentCommands.getPages().getBoolean(Display.BoolValue.TURNOVER_ON) && decode_pdf.getDisplayView()==Display.FACING){
			pageTurnFunctions.mouseEntered(e);
		}
		

		if(customMouseFunctions!=null){
			customMouseFunctions.mouseEntered(e);
		}
	}

	public void mouseExited(MouseEvent e) {

        //Ensure mouse coords don't display when mouse not over PDF
        int[] flag = new int[]{SwingGUI.CURSOR, 0};
        currentGUI.setMultibox(flag);

		//If mouse leaves viewer, stop scrolling
        scrollThread.setAutoScroll(false, 0, 0, 0);
        
		switch(mouseMode.getMouseMode()){

		case MouseMode.MOUSE_MODE_TEXT_SELECT :
			selectionFunctions.mouseExited(e);
			break;

		case MouseMode.MOUSE_MODE_PANNING :
			//Does Nothing so ignore
			break;

		}
		
		if (currentCommands.getPages().getBoolean(Display.BoolValue.TURNOVER_ON) && decode_pdf.getDisplayView()==Display.FACING){
			pageTurnFunctions.mouseExited(e);
		}
		

		if(customMouseFunctions!=null){
			customMouseFunctions.mouseExited(e);
		}
	}
	private double middleDragStartX,middleDragStartY,xVelocity,yVelocity;
	private  javax.swing.Timer middleDragTimer;

	public void mousePressed(MouseEvent e) {
		switch(mouseMode.getMouseMode()){

		case MouseMode.MOUSE_MODE_TEXT_SELECT :
			selectionFunctions.mousePressed(e);
			break;

		case MouseMode.MOUSE_MODE_PANNING :
			panningFunctions.mousePressed(e);
			break;

		}
		
		//Start dragging
		if (e.getButton()==MouseEvent.BUTTON2) {
			middleDragStartX = e.getX() - decode_pdf.getVisibleRect().getX();
			middleDragStartY = e.getY() - decode_pdf.getVisibleRect().getY();
			decode_pdf.setCursor(currentGUI.getCursor(SwingGUI.PAN_CURSOR));
			
			//set up timer to refresh display
			if (middleDragTimer == null)
				middleDragTimer = new javax.swing.Timer(100,new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						Rectangle r = decode_pdf.getVisibleRect();
						r.translate((int)xVelocity,(int)yVelocity);
						if (xVelocity<-2) {
							if (yVelocity<-2)
								decode_pdf.setCursor(currentGUI.getCursor(SwingGUI.PAN_CURSORTL));
							else if (yVelocity>2)
								decode_pdf.setCursor(currentGUI.getCursor(SwingGUI.PAN_CURSORBL));
							else
								decode_pdf.setCursor(currentGUI.getCursor(SwingGUI.PAN_CURSORL));
						} else if (xVelocity>2) {
							if (yVelocity<-2)
								decode_pdf.setCursor(currentGUI.getCursor(SwingGUI.PAN_CURSORTR));
							else if (yVelocity>2)
								decode_pdf.setCursor(currentGUI.getCursor(SwingGUI.PAN_CURSORBR));
							else
								decode_pdf.setCursor(currentGUI.getCursor(SwingGUI.PAN_CURSORR));
						} else {
							if (yVelocity<-2)
								decode_pdf.setCursor(currentGUI.getCursor(SwingGUI.PAN_CURSORT));
							else if (yVelocity>2)
								decode_pdf.setCursor(currentGUI.getCursor(SwingGUI.PAN_CURSORB));
							else
								decode_pdf.setCursor(currentGUI.getCursor(SwingGUI.PAN_CURSOR));
						}
						decode_pdf.scrollRectToVisible(r);


					}
				});
			middleDragTimer.start();
		}
		
		if (!SwingUtilities.isMiddleMouseButton(e) && currentCommands.getPages().getBoolean(Display.BoolValue.TURNOVER_ON) && decode_pdf.getDisplayView()==Display.FACING){
			pageTurnFunctions.mousePressed(e);
		}
		

		if(customMouseFunctions!=null){
			customMouseFunctions.mousePressed(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		switch(mouseMode.getMouseMode()){

		case MouseMode.MOUSE_MODE_TEXT_SELECT :
			selectionFunctions.mouseReleased(e);
			break;

		case MouseMode.MOUSE_MODE_PANNING :
			panningFunctions.mouseReleased(e);
			break;

		}
		
		//stop middle click panning
		if (e.getButton() == MouseEvent.BUTTON2) {
			xVelocity = 0;
			yVelocity = 0;
			decode_pdf.setCursor(currentGUI.getCursor(SwingGUI.DEFAULT_CURSOR));
			middleDragTimer.stop();
			decode_pdf.repaint();
		}
		
		if (!SwingUtilities.isMiddleMouseButton(e) && currentCommands.getPages().getBoolean(Display.BoolValue.TURNOVER_ON) && decode_pdf.getDisplayView()==Display.FACING){
			pageTurnFunctions.mouseReleased(e);
		}

		if(customMouseFunctions!=null){
			customMouseFunctions.mouseReleased(e);
		}
	}

	public void mouseDragged(MouseEvent e) {
		scrollAndUpdateCoords(e);
		
		switch(mouseMode.getMouseMode()){

		case MouseMode.MOUSE_MODE_TEXT_SELECT :
			selectionFunctions.mouseDragged(e);
			break;

		case MouseMode.MOUSE_MODE_PANNING :
			panningFunctions.mouseDragged(e);
			break;

		}
		
		if (SwingUtilities.isMiddleMouseButton(e)) {
			//middle drag - update velocity
			xVelocity = ((e.getX() - decode_pdf.getVisibleRect().getX()) - middleDragStartX)/4;
			yVelocity = ((e.getY() - decode_pdf.getVisibleRect().getY()) - middleDragStartY)/4;
		}
		
		if (!SwingUtilities.isMiddleMouseButton(e) && currentCommands.getPages().getBoolean(Display.BoolValue.TURNOVER_ON) && decode_pdf.getDisplayView()==Display.FACING){
			pageTurnFunctions.mouseDragged(e);
		}
		

		if(customMouseFunctions!=null){
			customMouseFunctions.mouseDragged(e);
		}
	}

	public static void setCustomMouseFunctions(SwingMouseFunctionality cmf) {
		customMouseFunctions = cmf;
	}

	public void mouseMoved(MouseEvent e) {
		
		int page = commonValues.getCurrentPage();
		
		Point p = selectionFunctions.getCoordsOnPage(e.getX(), e.getY(), page);
		int x = (int)p.getX();
		int y = (int)p.getY();
		updateCoords(x, y, e.isShiftDown());
		

		/*
		 * Mouse mode specific code
		 */
		switch(mouseMode.getMouseMode()){

		case MouseMode.MOUSE_MODE_TEXT_SELECT :
			int[] values = selectionFunctions.updateXY(e.getX(), e.getY());
			x =values[0];
			y =values[1];
	        if(!currentCommands.extractingAsImage)
			    getObjectUnderneath(x, y);
	        
			selectionFunctions.mouseMoved(e);
			break;

		case MouseMode.MOUSE_MODE_PANNING :
			//Does Nothing so ignore
			break;

		}

		if (currentCommands.getPages().getBoolean(Display.BoolValue.TURNOVER_ON) && decode_pdf.getDisplayView()==Display.FACING){
			pageTurnFunctions.mouseMoved(e);
		}
		

		if(customMouseFunctions!=null){
			customMouseFunctions.mouseMoved(e);
		}
	}

    private void getObjectUnderneath(int x, int y) {
        if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE){
            int type = decode_pdf.getDynamicRenderer().getObjectUnderneath(x, y);
            switch(type){
                case -1 :
                    decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case DynamicVectorRenderer.TEXT :
                    decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                    break;
                case DynamicVectorRenderer.IMAGE :
                    decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    break;
                case DynamicVectorRenderer.TRUETYPE :
                    decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                    break;
                case DynamicVectorRenderer.TYPE1C :
                    decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                    break;
                case DynamicVectorRenderer.TYPE3 :
                    decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                    break;
            }
        }
    }
	
	class AutoScrollThread implements Runnable{

        Thread scroll;
        boolean autoScroll = false;
        int x = 0;
        int y = 0;
        int interval = 0;

        public AutoScrollThread(){
            scroll = new Thread(this);
        }

        public void setAutoScroll(boolean autoScroll, int x, int y, int interval){
            this.autoScroll = autoScroll;
            this.x = currentGUI.AdjustForAlignment(x);
            this.y = y;
            this.interval = interval;
        }

        public void init(){
            scroll.start();
        }

        int usedX,usedY;

        public void run() {

            while (Thread.currentThread().equals(scroll)) {

                //New autoscroll code allow for diagonal scrolling from corner of viewer

                //@kieran - you will see if you move the mouse to right or bottom of page, repaint gets repeatedly called
                //we need to add 2 test to ensure only redrawn if on page (you need to covert x and y back to PDF and
                //check fit in width and height - see code in this class
                //if(autoScroll && usedX!=x && usedY!=y && x>0 && y>0){
                if(autoScroll){
                    final Rectangle visible_test=new Rectangle(x-interval,y-interval,interval*2,interval*2);
                    final Rectangle currentScreen=decode_pdf.getVisibleRect();

                    if(!currentScreen.contains(visible_test)){

                        if (SwingUtilities.isEventDispatchThread()){
                            decode_pdf.scrollRectToVisible(visible_test);
                        }else {
                            final Runnable doPaintComponent = new Runnable() {
                                public void run() {
                                    decode_pdf.scrollRectToVisible(visible_test);
                                }
                            };
                            SwingUtilities.invokeLater(doPaintComponent);
                        }

                        //Check values modified by (interval*2) as visible rect changed by interval
                        if(x-(interval*2)<decode_pdf.getVisibleRect().x)
                            x = x-interval;
                        else if((x+(interval*2))>(decode_pdf.getVisibleRect().x+decode_pdf.getVisibleRect().width))
                            x = x+interval;

                        if(y-(interval*2)<decode_pdf.getVisibleRect().y)
                            y = y-interval;
                        else if((y+(interval*2))>(decode_pdf.getVisibleRect().y+decode_pdf.getVisibleRect().height))
                            y = y+interval;

                        //thrashes box if constantly called

                        //System.out.println("redraw on scroll");
                        //decode_pdf.repaint();
                    }

                    usedX=x;
                    usedY=y;

                }

                //Delay to check for mouse leaving scroll edge)
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

	public void mouseWheelMoved(MouseWheelEvent event) {

		switch(decode_pdf.getDisplayView()){
		case Display.PAGEFLOW:
			break;

		case Display.FACING : 
			if(currentCommands.getPages().getBoolean(Display.BoolValue.TURNOVER_ON))
				pageTurnFunctions.mouseWheelMoved(event);
			break;
		case Display.SINGLE_PAGE : 

			if(currentGUI.getProperties().getValue("allowScrollwheelZoom").toLowerCase().equals("true") && (event.isMetaDown() || event.isControlDown())){
				//zoom
				int scaling = currentGUI.getSelectedComboIndex(Commands.SCALING);
				if(scaling!=-1){
					scaling = (int)decode_pdf.getDPIFactory().removeScaling(decode_pdf.getScaling()*100);
				}else{
					String numberValue = (String)currentGUI.getSelectedComboItem(Commands.SCALING);
					try{
						scaling= (int)Float.parseFloat(numberValue);
					}catch(Exception e){
						scaling=-1;
						//its got characters in it so get first valid number string
						int length=numberValue.length();
						int ii=0;
						while(ii<length){
							char c=numberValue.charAt(ii);
							if(((c>='0')&&(c<='9'))|(c=='.'))
								ii++;
							else
								break;
						}

						if(ii>0)
							numberValue=numberValue.substring(0,ii);

						//try again if we reset above
						if(scaling==-1){
							try{
								scaling = (int)Float.parseFloat(numberValue);
							}catch(Exception e1){scaling=-1;}
						}
					}
				}

				float value = event.getWheelRotation();

				if(scaling!=1 || value<0){
					if(value<0){
						value = 1.25f;
					}else{
						value = 0.8f;
					}
					if(!(scaling+value<0)){
						float currentScaling = (scaling*value);

						//kieran - is this one of yours?
						//
						if(((int)currentScaling)==(scaling))
							currentScaling=scaling+1;
						else
							currentScaling = ((int)currentScaling);

						if(currentScaling<1)
							currentScaling=1;

						if(currentScaling>1000)
							currentScaling=1000;

						//store mouse location
						final Rectangle r = decode_pdf.getVisibleRect();
						final double x = event.getX()/decode_pdf.getBounds().getWidth();
						final double y = event.getY()/decode_pdf.getBounds().getHeight();

						//update scaling
						currentGUI.snapScalingToDefaults(currentScaling);

						//center on mouse location
						Thread t = new Thread() {
							public void run() {
								try {
									decode_pdf.scrollRectToVisible(new Rectangle(
											(int)((x*decode_pdf.getWidth())-(r.getWidth()/2)),
											(int)((y*decode_pdf.getHeight())-(r.getHeight()/2)),
											(int)decode_pdf.getVisibleRect().getWidth(),
											(int)decode_pdf.getVisibleRect().getHeight()));
									decode_pdf.repaint();
								} catch (Exception e) {e.printStackTrace();}
							}
						};
						t.start();
						SwingUtilities.invokeLater(t);
					}
				}
			} else {

//				if(t2!=null)
//					t2.cancel();
//				
//				t2 = new java.util.Timer();
								

				final JScrollBar scroll = currentGUI.getVerticalScrollBar();
				
				//Ensure scrollToPage is set to the correct value as this can change
				//if scaling increases page size to larger than the display area
				scrollToPage = scroll.getValue();
				
				if ((scroll.getValue()>=scroll.getMaximum()-scroll.getHeight() || scroll.getHeight()==0) &&
						event.getUnitsToScroll() > 0 &&
						scrollToPage <= decode_pdf.getPageCount()) {

                    if (scrollPageChanging)
                        return;

					scrollPageChanging = true;

                    //change page
                    //currentCommands.executeCommand(Commands.FORWARDPAGE, null);
					if(scrollToPage<decode_pdf.getPageCount())
						scrollToPage++;
					
//                    TimerTask listener = new PageListener();
//                    t2.schedule(listener, 500);
                    
                    //update scrollbar so at top of page
                    if (SwingUtilities.isEventDispatchThread()) {
                        scroll.setValue(scrollToPage);
                    } else {
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                public void run() {
                                    scroll.setValue(scrollToPage);
                                }
                            });
                        } catch(Exception e) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    scroll.setValue(scrollToPage);
                                }
                            });
                        }
                    }

                    scrollPageChanging = false;

				} else if (scroll.getValue()>=scroll.getMinimum() &&
						event.getUnitsToScroll() < 0 &&
						scrollToPage >= 1) {

                    if (scrollPageChanging)
                        return;

					scrollPageChanging = true;

                    //change page
//                    currentCommands.executeCommand(Commands.BACKPAGE, null);
					if(scrollToPage>=1)
						scrollToPage--;
					
//					TimerTask listener = new PageListener();
//	                t2.schedule(listener, 500);
	                    
                    //update scrollbar so at bottom of page
                    if (SwingUtilities.isEventDispatchThread()) {
                        scroll.setValue(scrollToPage);
                    } else {
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                public void run() {
                                    scroll.setValue(scrollToPage);
                                }
                            });
                        } catch(Exception e) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    scroll.setValue(scrollToPage);
                                }
                            });
                        }
                    }

                    scrollPageChanging = false;

				}
			}
			
			//Don't break here so that continuous modes and single mode can use the following
		default :
			
			//scroll
			Area rect = new Area(decode_pdf.getVisibleRect());
			AffineTransform transform = new AffineTransform();
			transform.translate(0, event.getUnitsToScroll() * decode_pdf.getScrollInterval());
			rect = rect.createTransformedArea(transform);
			decode_pdf.scrollRectToVisible(rect.getBounds());
			
			break;
		}
		//		//Not used in text selection or panning modes
		//		if (decode_pdf.turnoverOn && decode_pdf.getDisplayView()==Display.FACING){
		//			pageTurnFunctions.mouseWheelMoved(event);
		//		}
	}
	
//	java.util.Timer t2 = null;
	int scrollToPage = -1;
	
//    class PageListener extends TimerTask {
//
//        public void run() {
//        	if(scrollToPage!=-1){
//        		currentCommands.executeCommand(Commands.GOTO, new Object[]{(""+scrollToPage)});
//        		scrollToPage = -1;
//        	}
//        }
//    }
	
	/**
	 * scroll to visible Rectangle and update Coords box on screen
	 */
	protected void scrollAndUpdateCoords(MouseEvent event) {
        //scroll if user hits side
        int interval=decode_pdf.getScrollInterval();
		Rectangle visible_test=new Rectangle(currentGUI.AdjustForAlignment(event.getX()),event.getY(),interval,interval);
		if((currentGUI.allowScrolling())&&(!decode_pdf.getVisibleRect().contains(visible_test)))
                decode_pdf.scrollRectToVisible(visible_test);

		
		int page = commonValues.getCurrentPage();
		
		Point p = selectionFunctions.getCoordsOnPage(event.getX(), event.getY(), page);
		int x = (int)p.getX();
		int y = (int)p.getY();
		updateCoords(x, y, event.isShiftDown());
    }
	
	/**update current page co-ordinates on screen
	 */
	public void updateCoords(/*MouseEvent event*/int x, int y, boolean isShiftDown){

		cx=x;
		cy=y;

		if(decode_pdf.getDisplayView()!=Display.SINGLE_PAGE){
			
			if(SwingMouseSelector.activateMultipageHighlight){
				
				//@kieran remove when facing is working correctly
				if(decode_pdf.getDisplayView()==Display.FACING){
					cx=0;
					cy=0;
				}
			}else{
				cx=0;
				cy=0;
			}
		}


		if((Values.isProcessing())|(commonValues.getSelectedFile()==null))
			currentGUI.setCoordText("  X: "+ " Y: " + ' ' + ' '); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		else
			currentGUI.setCoordText("  X: " + cx + " Y: " + cy+ ' ' + ' ' +message); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	}
	
	 public int[] getCursorLocation() {
	        return new int[]{cx,cy};
	    }
	
	 public void checkLinks(boolean mouseClicked, PdfObjectReader pdfObjectReader){
		 //int[] pos = updateXY(event.getX(), event.getY());
		 pageTurnFunctions.checkLinks(mouseClicked, pdfObjectReader, cx, cy);
	 }

	 public void updateCordsFromFormComponent(MouseEvent e) {
		 JComponent component = (JComponent) e.getSource();

		 int x = component.getX() + e.getX();
		 int y = component.getY() + e.getY();
		 Point p = selectionFunctions.getCoordsOnPage(x, y, commonValues.getCurrentPage());
		 x = (int)p.getX();
		 y = (int)p.getY();
			
		 updateCoords(x, y, e.isShiftDown());
	 }
	 
	 public boolean getPageTurnAnimating() {
		 return pageTurnFunctions.getPageTurnAnimating();
	 }
	 
	 public void setPageTurnAnimating(boolean a) {
		 pageTurnFunctions.setPageTurnAnimating(a);
	 }
}
