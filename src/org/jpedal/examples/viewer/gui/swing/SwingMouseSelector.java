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
 * SwingMouseSelector.java
 * ---------------
 */

package org.jpedal.examples.viewer.gui.swing;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Date;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.jpedal.Display;
import org.jpedal.PdfDecoder;
import org.jpedal.SingleDisplay;
import org.jpedal.examples.viewer.Commands;
import org.jpedal.examples.viewer.Viewer;
import org.jpedal.examples.viewer.Values;
import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.examples.viewer.gui.SwingGUI;
import org.jpedal.exception.PdfException;
import org.jpedal.external.Options;
import org.jpedal.grouping.SearchType;
import org.jpedal.io.Speech;
import org.jpedal.parser.DecoderOptions;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.Messages;

public class SwingMouseSelector implements SwingMouseFunctionality{

	private PdfDecoder decode_pdf;
	private SwingGUI currentGUI;
	private Values commonValues;
	private Commands currentCommands;

	//Experimental multi page highlight flag
	public static boolean activateMultipageHighlight = true;

	//Variables to keep track of multiple clicks
	private int clickCount = 0;
	private long lastTime = -1;

	//Page currently under the mouse
	private int pageMouseIsOver = -1;
	
	//Page currently being highlighted
	private int pageOfHighlight = -1;
	
	//Find current highlighted page
	private boolean startHighlighting = false;
	
	/*
	 * ID of objects found during selection
	 */
	public int id = -1;
	public int lastId =-1;

	//used to track changes when dragging rectangle around
	private int old_m_x2=-1,old_m_y2=-1;

	//Use alt to extract only within exact area
	boolean altIsDown = false;

	private JPopupMenu rightClick = new JPopupMenu();
	private boolean menuCreated = false;

	//Right click options
	JMenuItem copy;
	//======================================
	JMenuItem selectAll, deselectall;
	//======================================
	JMenu extract;
	JMenuItem extractText, extractImage;
	ImageIcon snapshotIcon;
	JMenuItem snapShot;
	//======================================
	JMenuItem find;
	//======================================
	JMenuItem speakHighlighted;

	public SwingMouseSelector(PdfDecoder decode_pdf, SwingGUI currentGUI,
			Values commonValues,Commands currentCommands) {

		this.decode_pdf=decode_pdf;
		this.currentGUI=currentGUI;
		this.commonValues=commonValues;
		this.currentCommands=currentCommands;

		//		decode_pdf.addExternalHandler(this, Options.SwingMouseHandler);

	}

	public void updateRectangle() {
		// TODO Auto-generated method stub

	}


	/**
	 * Mouse Button Listener
	 */
	public void mouseClicked(MouseEvent event) {

		if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE || activateMultipageHighlight){
			long currentTime = new Date().getTime();

			if(lastTime+500 < currentTime)
				clickCount=0;

			lastTime = currentTime;

			if(event.getButton()==MouseEvent.BUTTON1
					 || event.getButton()==MouseEvent.NOBUTTON){
				//Single mode actions
				if(clickCount!=4)
					clickCount++;

				//highlight image on page if over
				//int[] c = smh.getCursorLocation();
				float scaling=currentGUI.getScaling();
				int inset= GUI.getPDFDisplayInset();
				int mouseX = (int)((currentGUI.AdjustForAlignment(event.getX())-inset)/scaling);
				int mouseY = (int)(decode_pdf.getPdfPageData().getCropBoxHeight(commonValues.getCurrentPage())-((event.getY()-inset)/scaling));
				
				Point mousePoint = getCoordsOnPage(event.getX(), event.getY(), commonValues.getCurrentPage());
				mouseX = (int)mousePoint.getX();
				mouseY = (int)mousePoint.getY();
				
				if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE)
					id = decode_pdf.getDynamicRenderer().isInsideImage(mouseX,mouseY);
				else
					id = -1;
				
				if(lastId!=id && id!=-1){
					Rectangle imageArea = decode_pdf.getDynamicRenderer().getArea(id);


					if(imageArea!=null){
						int h= imageArea.height;
						int w= imageArea.width;
						
						int x= imageArea.x;
						int y= imageArea.y;
						decode_pdf.getDynamicRenderer().setneedsHorizontalInvert(false);
						decode_pdf.getDynamicRenderer().setneedsVerticalInvert(false);
						//						Check for negative values
						if(w<0){
							decode_pdf.getDynamicRenderer().setneedsHorizontalInvert(true);
							w =-w;
							x =x-w;
						}
						if(h<0){
							decode_pdf.getDynamicRenderer().setneedsVerticalInvert(true);
							h =-h;
							y =y-h;
						}

						if(currentGUI.currentCommands.isImageExtractionAllowed()){
                            currentCommands.pages.setHighlightedImage(new int[]{x,y,w,h});
						}

					}
					lastId = id;
				}else{
					if(currentGUI.currentCommands.isImageExtractionAllowed()){
                        currentCommands.pages.setHighlightedImage(null);
					}
					lastId = -1;
				}

				if(id==-1){
					if(clickCount>1){
						switch(clickCount){
						case 1 : //single click adds caret to page
							/**
							 * Does nothing yet. IF above prevents this case from ever happening
							 * Add Caret code here and add shift click code for selection.
							 * Also remember to comment out "if(clickCount>1)" from around this switch to activate
							 */
							break;
						case 2 : //double click selects line
							Rectangle[] lines = decode_pdf.getTextLines().getLineAreas(pageMouseIsOver);
							Rectangle point = new Rectangle(mouseX,mouseY,1,1);

							if(lines!=null) { //Null is page has no lines
								for(int i=0; i!=lines.length; i++){
									if(lines[i].intersects(point)){
										decode_pdf.updateCursorBoxOnScreen(lines[i], DecoderOptions.highlightColor);
										decode_pdf.getTextLines().addHighlights(new Rectangle[]{lines[i]}, false, pageMouseIsOver);
										//decode_pdf.setMouseHighlightArea(lines[i]);
									}
								}
							}
							break;
						case 3 : //triple click selects paragraph
							Rectangle para = decode_pdf.getTextLines().setFoundParagraph(mouseX,mouseY, pageMouseIsOver);
							if(para!=null){
								decode_pdf.updateCursorBoxOnScreen(para,DecoderOptions.highlightColor);
								//decode_pdf.repaint();
								//decode_pdf.setMouseHighlightArea(para);
							}
							break;
						case 4 : //quad click selects page
							currentGUI.currentCommands.executeCommand(Commands.SELECTALL, null);
							break;
						}
					}
				}
			}else if(event.getButton()==MouseEvent.BUTTON2){

			}else if(event.getButton()==MouseEvent.BUTTON3){

			}
		}		
	}

	public void mousePressed(MouseEvent event) {

		if(decode_pdf.getDisplayView()== Display.SINGLE_PAGE || activateMultipageHighlight){
			if(event.getButton()==MouseEvent.BUTTON1
					 || event.getButton()==MouseEvent.NOBUTTON){
				/** remove any outline and reset variables used to track change */

				decode_pdf.updateCursorBoxOnScreen(null, null); //remove box
                currentCommands.pages.setHighlightedImage(null);// remove image highlight
				decode_pdf.getTextLines().clearHighlights();

				//Remove focus from form is if anywhere on pdf panel is clicked / mouse dragged
				decode_pdf.grabFocus();
				
				//int[] values = updateXY(event.getX(), event.getY());
				Point values = getCoordsOnPage(event.getX(), event.getY(), commonValues.getCurrentPage());
				commonValues.m_x1=(int)values.getX();
				commonValues.m_y1=(int)values.getY();

			}
		}		
	}

	public void mouseReleased(MouseEvent event) {
		if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE || activateMultipageHighlight){
			if(event.getButton()==MouseEvent.BUTTON1
					 || event.getButton()==MouseEvent.NOBUTTON){
				
				//If we have been highlighting, stop now and reset all flags
				if(startHighlighting){
					startHighlighting = false;
					//pageOfHighlight = -1;
				}
				
				repaintArea(new Rectangle(commonValues.m_x1 - currentGUI.cropX, commonValues.m_y2 + currentGUI.cropY, commonValues.m_x2 - commonValues.m_x1 + currentGUI.cropX,
                        (commonValues.m_y1 - commonValues.m_y2) + currentGUI.cropY), currentGUI.mediaH);//redraw
				decode_pdf.repaint();

				if(currentCommands.extractingAsImage){

					/** remove any outline and reset variables used to track change */
					decode_pdf.updateCursorBoxOnScreen(null, null); //remove box
					decode_pdf.getTextLines().clearHighlights(); //remove highlighted text
                    currentCommands.pages.setHighlightedImage(null);// remove image highlight

					decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

					currentGUI.currentCommands.extractSelectedScreenAsImage();
                    currentCommands.extractingAsImage=false;
                    PdfDecoder.showMouseBox = false;

				}
				
				//Ensure this is reset to -1 regardless
				pageOfHighlight = -1;
				
			} else if(event.getButton()==MouseEvent.BUTTON3){
				if(currentGUI.getProperties().getValue("allowRightClick").toLowerCase().equals("true")){
					if (!menuCreated)
						createRightClickMenu();

					if(currentCommands.pages.getHighlightedImage()==null)
						extractImage.setEnabled(false);
					else
						extractImage.setEnabled(true);

					if(decode_pdf.getTextLines().getHighlightedAreas(commonValues.getCurrentPage())==null){
						extractText.setEnabled(false);
						find.setEnabled(false);
						speakHighlighted.setEnabled(false);
						copy.setEnabled(false);
					}else{
						extractText.setEnabled(true);
						find.setEnabled(true);
						speakHighlighted.setEnabled(true);
						copy.setEnabled(true);
					}

					//<start-wrap>
					if(decode_pdf!=null && decode_pdf.isOpen())
						rightClick.show(decode_pdf, event.getX(), event.getY());
					//<end-wrap>
				}
			}
		}		
	}


	/**
	 * Mouse Motion Listener
	 */
	public void mouseEntered(MouseEvent arg0) {

	}

	public void mouseExited(MouseEvent arg0) {

	}

	public void mouseDragged(MouseEvent event) {
		
		if(event.getButton()==MouseEvent.BUTTON1
				 || event.getButton()==MouseEvent.NOBUTTON){
			
			altIsDown = event.isAltDown();
			if(!startHighlighting)
				startHighlighting = true;

			Point values = getCoordsOnPage(event.getX(), event.getY(), commonValues.getCurrentPage());

			if(pageMouseIsOver==pageOfHighlight){
				commonValues.m_x2=(int)values.getX();
				commonValues.m_y2=(int)values.getY();
			}

			if(commonValues.isPDF())
				generateNewCursorBox();

		}

	}

	public void mouseMoved(MouseEvent event) {

		//Update cursor for this position
//		int[] values = updateXY(event.getX(), event.getY());
//		int x =values[0];
//		int y =values[1];
//		decode_pdf.getObjectUnderneath(x, y);


	}


	/**
	 * get raw co-ords and convert to correct scaled units
	 * @return int[] of size 2, [0]=new x value, [1] = new y value
	 */
	protected int[] updateXY(int originalX, int originalY) {

		float scaling=currentGUI.getScaling();
		int inset= GUI.getPDFDisplayInset();
		int rotation=currentGUI.getRotation();

		//get co-ordinates of top point of outine rectangle
		int x=(int)(((currentGUI.AdjustForAlignment(originalX))-inset)/scaling);
		int y=(int)((originalY-inset)/scaling);

		//undo any viewport scaling
		if(commonValues.maxViewY!=0){ // will not be zero if viewport in play
			x=(int)(((x-(commonValues.dx*scaling))/commonValues.viewportScale));
			y=(int)((currentGUI.mediaH-((currentGUI.mediaH-(y/scaling)-commonValues.dy)/commonValues.viewportScale))*scaling);
		}

		int[] ret=new int[2];
		if(rotation==90){	        
			ret[1] = x+currentGUI.cropY;
			ret[0] =y+currentGUI.cropX;
		}else if((rotation==180)){
			ret[0]=currentGUI.mediaW- (x+currentGUI.mediaW-currentGUI.cropW-currentGUI.cropX);
			ret[1] =y+currentGUI.cropY;
		}else if((rotation==270)){
			ret[1] =currentGUI.mediaH- (x+currentGUI.mediaH-currentGUI.cropH-currentGUI.cropY);
			ret[0]=currentGUI.mediaW-(y+currentGUI.mediaW-currentGUI.cropW-currentGUI.cropX);
		}else{
			ret[0] = x+currentGUI.cropX;
			ret[1] =currentGUI.mediaH-(y+currentGUI.mediaH-currentGUI.cropH-currentGUI.cropY);    
		}
		return ret;
	}


	/**
	 * Create right click menu if does not exist
	 */
	private void createRightClickMenu(){

		copy = new JMenuItem(Messages.getMessage("PdfRightClick.copy"));
		selectAll = new JMenuItem(Messages.getMessage("PdfRightClick.selectAll"));
		deselectall = new JMenuItem(Messages.getMessage("PdfRightClick.deselectAll"));
		extract = new JMenu(Messages.getMessage("PdfRightClick.extract"));
		extractText = new JMenuItem(Messages.getMessage("PdfRightClick.extractText"));
		extractImage = new JMenuItem(Messages.getMessage("PdfRightClick.extractImage"));
		snapshotIcon = new ImageIcon(getClass().getResource("/org/jpedal/examples/viewer/res/snapshot_menu.gif"));
		snapShot = new JMenuItem(Messages.getMessage("PdfRightClick.snapshot"), snapshotIcon);
		find = new JMenuItem(Messages.getMessage("PdfRightClick.find"));
		speakHighlighted = new JMenuItem("Speak Highlighted text");

		rightClick.add(copy);
		copy.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE)
					currentGUI.currentCommands.executeCommand(Commands.COPY, null);
				else{
					if(Viewer.showMessages)
						JOptionPane.showMessageDialog(currentGUI.getFrame(),"Copy is only avalible in single page display mode");
				}
			}
		});

		rightClick.addSeparator();


		rightClick.add(selectAll);
		selectAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				currentGUI.currentCommands.executeCommand(Commands.SELECTALL, null);
			}
		});

		rightClick.add(deselectall);
		deselectall.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				currentGUI.currentCommands.executeCommand(Commands.DESELECTALL, null);
			}
		});

		rightClick.addSeparator();

		rightClick.add(extract);

		extract.add(extractText);
		extractText.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE)
					currentGUI.currentCommands.extractSelectedText();
				else{
					if(Viewer.showMessages)
						JOptionPane.showMessageDialog(currentGUI.getFrame(),"Text Extraction is only avalible in single page display mode");
				}
			}
		});

		extract.add(extractImage);
		extractImage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(currentCommands.pages.getHighlightedImage()==null){
					if(Viewer.showMessages)
						JOptionPane.showMessageDialog(decode_pdf, "No image has been selected for extraction.", "No image selected", JOptionPane.ERROR_MESSAGE);
				}else{
					if(decode_pdf.getDisplayView()==1){
						JFileChooser jf = new JFileChooser();
						FileFilter ff1 = new FileFilter(){
							public boolean accept(File f){
								return f.isDirectory() || f.getName().toLowerCase().endsWith(".jpg") || f.getName().toLowerCase().endsWith(".jpeg");
							}
							public String getDescription(){
								return "JPG (*.jpg)" ;
							}
						};
						FileFilter ff2 = new FileFilter(){
							public boolean accept(File f){
								return f.isDirectory() || f.getName().toLowerCase().endsWith(".png");
							}
							public String getDescription(){
								return "PNG (*.png)" ;
							}
						};
						FileFilter ff3 = new FileFilter(){
							public boolean accept(File f){
								return f.isDirectory() || f.getName().toLowerCase().endsWith(".tif") || f.getName().toLowerCase().endsWith(".tiff");
							}
							public String getDescription(){
								return "TIF (*.tiff)" ;
							}
						};
						jf.addChoosableFileFilter(ff3);
						jf.addChoosableFileFilter(ff2);
						jf.addChoosableFileFilter(ff1);
						jf.showSaveDialog(null);

						File f = jf.getSelectedFile();
						boolean failed = false;
						if(f!=null){
							String filename = f.getAbsolutePath();
							String type = jf.getFileFilter().getDescription().substring(0,3).toLowerCase();

							//Check to see if user has entered extension if so ignore filter
							if(filename.indexOf('.')!=-1){
								String testExt = filename.substring(filename.indexOf('.')+1).toLowerCase();
								if(testExt.equals("jpg") || testExt.equals("jpeg"))
									type = "jpg";
								else
									if(testExt.equals("png"))
										type = "png";
									else //*.tiff files using JAI require *.TIFF
										if(testExt.equals("tif") || testExt.equals("tiff"))
											type = "tiff";
										else{
											//Unsupported file format
											if(Viewer.showMessages)
												JOptionPane.showMessageDialog(null, "Sorry, we can not currently save images to ."+testExt+" files.");
											failed = true;
										}
							}

							//JAI requires *.tiff instead of *.tif
							if(type.equals("tif"))
								type = "tiff";

							//Image saved in All files filter, default to .png
							if(type.equals("all"))
								type = "png";

							//If no extension at end of name, added one
							if(!filename.toLowerCase().endsWith('.' +type))
								filename = filename+ '.' +(type);

							//If valid extension was choosen
							if(!failed)
								decode_pdf.getDynamicRenderer().saveImage(id, filename, type);
						}
					}
				}
			}
		});

		extract.add(snapShot);
		snapShot.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				currentGUI.currentCommands.executeCommand(Commands.SNAPSHOT, null);
			}
		});

		rightClick.addSeparator();

		rightClick.add(find);
		find.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {

				/**ensure co-ords in right order*/
				Rectangle coords= decode_pdf.getCursorBoxOnScreen();
				if(coords==null){
					if(Viewer.showMessages)
						JOptionPane.showMessageDialog(decode_pdf, "There is no text selected.\nPlease highlight the text you wish to search.", "No Text selected", JOptionPane.ERROR_MESSAGE);
					return;
				}

				String textToFind=currentGUI.showInputDialog(Messages.getMessage("PdfViewerMessage.GetUserInput"));

				//if cancel return to menu.
				if(textToFind==null || textToFind.length()<1){
					return;
				}


				int t_x1=coords.x;
				int t_x2=coords.x+coords.width;
				int t_y1=coords.y;
				int t_y2=coords.y+coords.height;

				if(t_y1<t_y2){
					int temp = t_y2;
					t_y2=t_y1;
					t_y1=temp;
				}

				if(t_x1>t_x2){
					int temp = t_x2;
					t_x2=t_x1;
					t_x1=temp;
				}

				if(t_x1<currentGUI.cropX)
					t_x1 = currentGUI.cropX;
				if(t_x1>currentGUI.mediaW-currentGUI.cropX)
					t_x1 = currentGUI.mediaW-currentGUI.cropX;

				if(t_x2<currentGUI.cropX)
					t_x2 = currentGUI.cropX;
				if(t_x2>currentGUI.mediaW-currentGUI.cropX)
					t_x2 = currentGUI.mediaW-currentGUI.cropX;

				if(t_y1<currentGUI.cropY)
					t_y1 = currentGUI.cropY;
				if(t_y1>currentGUI.mediaH-currentGUI.cropY)
					t_y1 = currentGUI.mediaH-currentGUI.cropY;

				if(t_y2<currentGUI.cropY)
					t_y2 = currentGUI.cropY;
				if(t_y2>currentGUI.mediaH-currentGUI.cropY)
					t_y2 = currentGUI.mediaH-currentGUI.cropY;

				//<start-demo>
				/**<end-demo>
                 if(Viewer.showMessages)
                 JOptionPane.showMessageDialog(currentGUI.getFrame(),Messages.getMessage("PdfViewerMessage.FindDemo"));
                 textToFind=null;
                 /**/

				int searchType = SearchType.DEFAULT;

				int caseSensitiveOption=currentGUI.showConfirmDialog(Messages.getMessage("PdfViewercase.message"),
						null,	JOptionPane.YES_NO_OPTION);

				if(caseSensitiveOption==JOptionPane.YES_OPTION)
					searchType |= SearchType.CASE_SENSITIVE;

				int findAllOption=currentGUI.showConfirmDialog(Messages.getMessage("PdfViewerfindAll.message"),
						null,	JOptionPane.YES_NO_OPTION);

				if(findAllOption==JOptionPane.NO_OPTION)
					searchType |= SearchType.FIND_FIRST_OCCURANCE_ONLY;

				int hyphenOption=currentGUI.showConfirmDialog(Messages.getMessage("PdfViewerfindHyphen.message"),
						null,	JOptionPane.YES_NO_OPTION);

				if(hyphenOption==JOptionPane.YES_OPTION)
					searchType |= SearchType.MUTLI_LINE_RESULTS;

				if(textToFind!=null){
					try {
						float[] co_ords;

//						if((searchType & SearchType.MUTLI_LINE_RESULTS)==SearchType.MUTLI_LINE_RESULTS)
//							co_ords = decode_pdf.getGroupingObject().findTextInRectangleAcrossLines(t_x1,t_y1,t_x2,t_y2,commonValues.getCurrentPage(),textToFind,searchType);
//						else
//							co_ords = decode_pdf.getGroupingObject().findTextInRectangle(t_x1,t_y1,t_x2,t_y2,commonValues.getCurrentPage(),textToFind,searchType);

						co_ords = decode_pdf.getGroupingObject().findText(new Rectangle(t_x1,t_y1,t_x2-t_x1,t_y2-t_y1),commonValues.getCurrentPage(),new String[]{textToFind},searchType);

						if(co_ords!=null){
							if(co_ords.length<3)
								currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.Found")+ ' ' +co_ords[0]+ ',' +co_ords[1]);
							else{
								StringBuilder displayCoords = new StringBuilder();
								String coordsMessage = Messages.getMessage("PdfViewerMessage.FoundAt");
								for(int i=0;i<co_ords.length;i=i+5){
									displayCoords.append(coordsMessage).append(' ');
									displayCoords.append(co_ords[i]);
									displayCoords.append(',');
									displayCoords.append(co_ords[i+1]);

									//										//Other two coords of text
									//										displayCoords.append(',');
									//										displayCoords.append(co_ords[i+2]);
									//										displayCoords.append(',');
									//										displayCoords.append(co_ords[i+3]);

									displayCoords.append('\n');
									if(co_ords[i+4]==-101){
										coordsMessage = Messages.getMessage("PdfViewerMessage.FoundAtHyphen");
									}else{
										coordsMessage = Messages.getMessage("PdfViewerMessage.FoundAt");
									}

								}
								currentGUI.showMessageDialog(displayCoords.toString());
							}
						}else
							currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NotFound"));

					} catch (PdfException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}
			}

		});



		menuCreated = true;
		decode_pdf.add(rightClick);
	}

	/**
	 * generate new  cursorBox and highlight extractable text,
	 * if hardware acceleration off and extraction on<br>
	 * and update current cursor box displayed on screen
	 */
	protected void generateNewCursorBox() {

		//redraw rectangle of dragged box onscreen if it has changed significantly
		if ((old_m_x2!=-1)|(old_m_y2!=-1)|(Math.abs(commonValues.m_x2-old_m_x2)>5)|(Math.abs(commonValues.m_y2-old_m_y2)>5)) {	

			//allow for user to go up
			int top_x = commonValues.m_x1;
			if (commonValues.m_x1 > commonValues.m_x2)
				top_x = commonValues.m_x2;
			int top_y = commonValues.m_y1;
			if (commonValues.m_y1 > commonValues.m_y2)
				top_y = commonValues.m_y2;
			int w = Math.abs(commonValues.m_x2 - commonValues.m_x1);
			int h = Math.abs(commonValues.m_y2 - commonValues.m_y1);

			//add an outline rectangle  to the display
			Rectangle currentRectangle=new Rectangle (top_x,top_y,w,h);
			
			//tell JPedal to highlight text in this area (you can add other areas to array)
			decode_pdf.updateCursorBoxOnScreen(currentRectangle,DecoderOptions.highlightColor);
			if(!currentCommands.extractingAsImage){
				int type = decode_pdf.getDynamicRenderer().getObjectUnderneath(commonValues.m_x1, commonValues.m_y1);

				if((altIsDown &&
						(type!=DynamicVectorRenderer.TEXT && type!=DynamicVectorRenderer.TRUETYPE &&
								type!=DynamicVectorRenderer.TYPE1C && type!=DynamicVectorRenderer.TYPE3))){

					//Highlight all within the rectangle
						decode_pdf.getTextLines().addHighlights(new Rectangle[]{currentRectangle}, true, pageOfHighlight);
				}else{ //Find start and end locations and highlight all object in order in between
					Rectangle r = new Rectangle(commonValues.m_x1, commonValues.m_y1,commonValues.m_x2 - commonValues.m_x1, commonValues.m_y2-commonValues.m_y1);

					decode_pdf.getTextLines().addHighlights(new Rectangle[]{r}, false, pageOfHighlight);

				}
			}
			//reset tracking
			old_m_x2=commonValues.m_x2;
			old_m_y2=commonValues.m_y2;

		}
		
		((SingleDisplay)decode_pdf.getExternalHandler(Options.Display)).refreshDisplay();
		decode_pdf.repaint();
	}

	private Point getPageCoordsInSingleDisplayMode(int x, int y, int page){
		//<start-adobe>
		if (currentGUI.useNewLayout) {
			
			int[] flag = new int[2];
			
			flag[0] = SwingGUI.CURSOR;
			flag[1]=0;
			
			int pageWidth,pageHeight;
			if (currentGUI.getRotation()%180==90) {
				pageWidth = decode_pdf.getPdfPageData().getScaledCropBoxHeight(page);
				pageHeight = decode_pdf.getPdfPageData().getScaledCropBoxWidth(page);
			} else {
				pageWidth = decode_pdf.getPdfPageData().getScaledCropBoxWidth(page);
				pageHeight = decode_pdf.getPdfPageData().getScaledCropBoxHeight(page);
			}

			Rectangle pageArea = new Rectangle(
					(decode_pdf.getWidth()/2) - (pageWidth/2),
					decode_pdf.getInsetH(),
					pageWidth, 
					pageHeight);

			if (pageArea.contains(x,y))
				//set displayed
				flag[1] = 1;
			else
				//set not displayed
				flag[1] = 0;

			//Set highlighting page
			if(pageOfHighlight==-1 && startHighlighting){
				pageOfHighlight = page;
			}
			
			//Keep track of page the mouse is over at all times
			pageMouseIsOver = page;
			
			currentGUI.setMultibox(flag);
			
		}
		
		//<end-adobe>
		
		float scaling=currentGUI.getScaling();
		int inset= GUI.getPDFDisplayInset();
		int rotation=currentGUI.getRotation();

		
		//Apply inset to values
		int ex=currentGUI.AdjustForAlignment(x)-inset;
		int ey=y-inset;

		//undo any viewport scaling
		if(commonValues.maxViewY!=0){ // will not be zero if viewport in play
			ex=(int)(((ex-(commonValues.dx*scaling))/commonValues.viewportScale));
			ey=(int)((currentGUI.mediaH-((currentGUI.mediaH-(ey/scaling)-commonValues.dy)/commonValues.viewportScale))*scaling);
		}
		
		//Apply page scale to value
		x=(int)((ex)/scaling);
		y=(int)((ey/scaling));
		
		//Apply rotation to values
		if(rotation==90){
			int tmp=(x+currentGUI.cropY);
			x = (y+currentGUI.cropX);
			y =tmp;	
		}else if((rotation==180)){
			x =(currentGUI.cropW+currentGUI.cropX)-x;
			y =(y+currentGUI.cropY);
		}else if((rotation==270)){
			int tmp=(currentGUI.cropH+currentGUI.cropY)-x;
			x =(currentGUI.cropW+currentGUI.cropX)-y;
			y =tmp;
		}else{
			x = (x+currentGUI.cropX);
			if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE)
				y =(currentGUI.cropH+currentGUI.cropY)-y;
			else
				y =(currentGUI.cropY)+y;
		}
		
		return new Point(x, y);
	}

	private Point getPageCoordsInContinuousDisplayMode(int x, int y, int page){

        Display pages=(SingleDisplay) decode_pdf.getExternalHandler(Options.Display);

        //<start-adobe>
		if (currentGUI.useNewLayout) {
			int[] flag = new int[2];

			flag[0] = SwingGUI.CURSOR;
			flag[1]=0;

			//In continuous pages are centred so we need make
			int xAdjustment = (decode_pdf.getWidth()/2) - (decode_pdf.getPdfPageData().getScaledCropBoxWidth(page)/2);
			if(xAdjustment<0)
				xAdjustment = 0;
			else{
				//This adjustment is the correct position.
				//Offset removed to that when used later we get either offset unaltered or correct position
				xAdjustment = xAdjustment-pages.getXCordForPage(page);
			}
			Rectangle pageArea = new Rectangle(pages.getXCordForPage(page)+xAdjustment,
					pages.getYCordForPage(page),
					decode_pdf.getPdfPageData().getScaledCropBoxWidth(page),
					decode_pdf.getPdfPageData().getScaledCropBoxHeight(page));
			if(pageArea.contains(x,y)){
				//set displayed
				flag[1] = 1;
			}



			if(flag[1]==0){
				if(y<pageArea.y && page>1){
					while(flag[1]==0 && page>1){
						page--;
						pageArea = new Rectangle(pages.getXCordForPage(page)+xAdjustment,
								pages.getYCordForPage(page),
								decode_pdf.getPdfPageData().getScaledCropBoxWidth(page),
								decode_pdf.getPdfPageData().getScaledCropBoxHeight(page));
						if(pageArea.contains(x,y)){
							//set displayed
							flag[1] = 1;
						}
					}
				}else{
					if(y>pageArea.getMaxY() && page<commonValues.getPageCount()){
						while(flag[1]==0 && page<commonValues.getPageCount()){
							page++;
							pageArea = new Rectangle(pages.getXCordForPage(page)+xAdjustment,
                                    pages.getYCordForPage(page),
									decode_pdf.getPdfPageData().getScaledCropBoxWidth(page),
									decode_pdf.getPdfPageData().getScaledCropBoxHeight(page));
							if(pageArea.contains(x,y)){
								//set displayed
								flag[1] = 1;
							}
						}
					}
				}
			}
			
			//Set highlighting page
			if(pageOfHighlight==-1 && startHighlighting){
				pageOfHighlight = page;
			}
			
			//Keep track of page mouse is over at all times
			pageMouseIsOver = page;
			
			//Tidy coords for multipage views
			y= ((pages.getYCordForPage(page)+decode_pdf.getPdfPageData().getScaledCropBoxHeight(page))+decode_pdf.getInsetH())-y;
			
			currentGUI.setMultibox(flag);
			
//			if(flag[1]==1 && (findPageToHighlight && commonValues.getCurrentHighlightedPage()==-1)){
//				commonValues.setCurrentHighlightedPage(page);
//			}
//			else{
//				commonValues.setCurrentHighlightedPage(-1);
//			}
		}
		
		//<end-adobe>
		

		float scaling=currentGUI.getScaling();
		int inset= GUI.getPDFDisplayInset();
		int rotation=currentGUI.getRotation();

		
		//Apply inset to values
		int ex=currentGUI.AdjustForAlignment(x)-inset;
		int ey=y-inset;

		//undo any viewport scaling
		if(commonValues.maxViewY!=0){ // will not be zero if viewport in play
			ex=(int)(((ex-(commonValues.dx*scaling))/commonValues.viewportScale));
			ey=(int)((currentGUI.mediaH-((currentGUI.mediaH-(ey/scaling)-commonValues.dy)/commonValues.viewportScale))*scaling);
		}
		
		//Apply page scale to value
		x=(int)((ex)/scaling);
		y=(int)((ey/scaling));
		
		//Apply rotation to values
		if(rotation==90){
			int tmp=(x+currentGUI.cropY);
			x = (y+currentGUI.cropX);
			y =tmp;	
		}else if((rotation==180)){
			x =(currentGUI.cropW+currentGUI.cropX)-x;
			y =(y+currentGUI.cropY);
		}else if((rotation==270)){
			int tmp=(currentGUI.cropH+currentGUI.cropY)-x;
			x =(currentGUI.cropW+currentGUI.cropX)-y;
			y =tmp;
		}else{
			x = (x+currentGUI.cropX);
			if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE)
				y =(currentGUI.cropH+currentGUI.cropY)-y;
			else
				y =(currentGUI.cropY)+y;
		}
		
		return new Point(x, y);
	}

	private Point getPageCoordsInContinuousFacingDisplayMode(int x, int y, int page){
		//<start-adobe>

        Display pages=(SingleDisplay) decode_pdf.getExternalHandler(Options.Display);

        if (currentGUI.useNewLayout) {
			int[] flag = new int[2];

			flag[0] = SwingGUI.CURSOR;
			flag[1]=0;
			
			//Check if we are in the region of the left or right pages
			if(page != 1 && x>(decode_pdf.getWidth()/2) && page<commonValues.getPageCount()){// && x>pageArea.x){
				page++;
			}

			//Set the adjustment for page position
			int xAdjustment = (decode_pdf.getWidth()/2) - (decode_pdf.getPdfPageData().getScaledCropBoxWidth(page))-(decode_pdf.getInsetW());

			//Unsure if this is needed. Still checking
			if(xAdjustment<0){
				System.err.println("x adjustment is less than 0");
				xAdjustment = 0;
			}
			
			//Check to see if pagearea contains the mouse
			Rectangle pageArea = new Rectangle(pages.getXCordForPage(page)+xAdjustment,
                    pages.getYCordForPage(page),
					decode_pdf.getPdfPageData().getScaledCropBoxWidth(page),
					decode_pdf.getPdfPageData().getScaledCropBoxHeight(page));
			if(pageArea.contains(x,y)){
				//set displayed
				flag[1] = 1;
			}

			
			//If neither of the two current pages contain the mouse start checking the other pages
			//Could be improved to minimise on the loops and calls to decode_pdf.getPageOffsets(page)
			if(flag[1]==0){
				if(y<pageArea.y && page>1){
					while(flag[1]==0 && page>1){
						page--;
						xAdjustment = (decode_pdf.getWidth()/2) - (decode_pdf.getPdfPageData().getScaledCropBoxWidth(page))-(decode_pdf.getInsetW());
						if(xAdjustment<0)
							xAdjustment = 0;
						pageArea = new Rectangle(pages.getXCordForPage(page)+xAdjustment,
                                pages.getYCordForPage(page),
								decode_pdf.getPdfPageData().getScaledCropBoxWidth(page),
								decode_pdf.getPdfPageData().getScaledCropBoxHeight(page));
						if(pageArea.contains(x,y)){
							//set displayed
							flag[1] = 1;
						}

					}
				}else{
					if(y>pageArea.getMaxY() && page<commonValues.getPageCount()){
						while(flag[1]==0 && page<commonValues.getPageCount()){
							page++;
							xAdjustment = (decode_pdf.getWidth()/2) - (decode_pdf.getPdfPageData().getScaledCropBoxWidth(page))-(decode_pdf.getInsetW());
							if(xAdjustment<0)
								xAdjustment = 0;
							pageArea = new Rectangle(pages.getXCordForPage(page)+xAdjustment,
                                    pages.getYCordForPage(page),
									decode_pdf.getPdfPageData().getScaledCropBoxWidth(page),
									decode_pdf.getPdfPageData().getScaledCropBoxHeight(page));
							if(pageArea.contains(x,y)){
								//set displayed
								flag[1] = 1;
							}

						}
					}
				}
			}
			
			//Set highlighting page
			if(pageOfHighlight==-1 && startHighlighting){
				pageOfHighlight = page;
			}
			
			//Keep track of page mouse is over at all times
			pageMouseIsOver = page;
			
			//Tidy coords for multipage views
			y= (((pages.getYCordForPage(page)+decode_pdf.getPdfPageData().getScaledCropBoxHeight(page))+decode_pdf.getInsetH()))-y;

			x = x - ((pages.getXCordForPage(page))-decode_pdf.getInsetW());
			
			currentGUI.setMultibox(flag);
			
		}
		//<end-adobe>
		

		float scaling=currentGUI.getScaling();
		int inset= GUI.getPDFDisplayInset();
		int rotation=currentGUI.getRotation();

		
		//Apply inset to values
		int ex=currentGUI.AdjustForAlignment(x)-inset;
		int ey=y-inset;

		//undo any viewport scaling
		if(commonValues.maxViewY!=0){ // will not be zero if viewport in play
			ex=(int)(((ex-(commonValues.dx*scaling))/commonValues.viewportScale));
			ey=(int)((currentGUI.mediaH-((currentGUI.mediaH-(ey/scaling)-commonValues.dy)/commonValues.viewportScale))*scaling);
		}
		
		//Apply page scale to value
		x=(int)((ex)/scaling);
		y=(int)((ey/scaling));
		
		//Apply rotation to values
		if(rotation==90){
			int tmp=(x+currentGUI.cropY);
			x = (y+currentGUI.cropX);
			y =tmp;	
		}else if((rotation==180)){
			x =(currentGUI.cropW+currentGUI.cropX)-x;
			y =(y+currentGUI.cropY);
		}else if((rotation==270)){
			int tmp=(currentGUI.cropH+currentGUI.cropY)-x;
			x =(currentGUI.cropW+currentGUI.cropX)-y;
			y =tmp;
		}else{
			x = (x+currentGUI.cropX);
			if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE)
				y =(currentGUI.cropH+currentGUI.cropY)-y;
			else
				y =(currentGUI.cropY)+y;
		}
		
		return new Point(x, y);
	}

	private Point getPageCoordsInFacingDisplayMode(int x, int y, int page){
		//<start-adobe>
		if (currentGUI.useNewLayout) {
			
			/**
			 * TO BE IMPLEMENTED
			 */
			int[] flag = new int[2];

			flag[0] = SwingGUI.CURSOR;
			flag[1]=0;
			
			
			flag[1] = 0;
			
			currentGUI.setMultibox(flag);
			
		}
		//<end-adobe>
		
		float scaling=currentGUI.getScaling();
		int inset= GUI.getPDFDisplayInset();
		int rotation=currentGUI.getRotation();

		
		//Apply inset to values
		int ex=currentGUI.AdjustForAlignment(x)-inset;
		int ey=y-inset;

		//undo any viewport scaling
		if(commonValues.maxViewY!=0){ // will not be zero if viewport in play
			ex=(int)(((ex-(commonValues.dx*scaling))/commonValues.viewportScale));
			ey=(int)((currentGUI.mediaH-((currentGUI.mediaH-(ey/scaling)-commonValues.dy)/commonValues.viewportScale))*scaling);
		}
		
		//Apply page scale to value
		x=(int)((ex)/scaling);
		y=(int)((ey/scaling));
		
		//Apply rotation to values
		if(rotation==90){
			int tmp=(x+currentGUI.cropY);
			x = (y+currentGUI.cropX);
			y =tmp;	
		}else if((rotation==180)){
			x =(currentGUI.cropW+currentGUI.cropX)-x;
			y =(y+currentGUI.cropY);
		}else if((rotation==270)){
			int tmp=(currentGUI.cropH+currentGUI.cropY)-x;
			x =(currentGUI.cropW+currentGUI.cropX)-y;
			y =tmp;
		}else{
			x = (x+currentGUI.cropX);
			if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE)
				y =(currentGUI.cropH+currentGUI.cropY)-y;
			else
				y =(currentGUI.cropY)+y;
		}
		
		return new Point(x, y);
	}
		
	/**
	 * Find and updates coords for the current page
	 * @param x :: The x coordinate of the cursors location in display area coordinates
	 * @param y :: The y coordinate of the cursors location in display area coordinates
	 * @param page :: The page we are currently on
	 * @return Point object of the cursor location in page coordinates
	 */
	public Point getCoordsOnPage(int x, int y, int page){
		
		//Update cursor position if over page      

		Point pagePosition = null;
		switch(decode_pdf.getDisplayView()){
		case Display.SINGLE_PAGE:
			pagePosition = getPageCoordsInSingleDisplayMode(x, y, page);
			x = pagePosition.x;
			y = pagePosition.y;
			break;
		case Display.CONTINUOUS:
			pagePosition = getPageCoordsInContinuousDisplayMode(x, y, page);
			x = pagePosition.x;
			y = pagePosition.y;
			break;

		case Display.FACING: 
			pagePosition = getPageCoordsInFacingDisplayMode(x, y, page);
			x = pagePosition.x;
			y = pagePosition.y;

			break;

		case Display.CONTINUOUS_FACING:
			pagePosition = getPageCoordsInContinuousFacingDisplayMode(x, y, page);
			x = pagePosition.x;
			y = pagePosition.y;

			break;
		default : break;
		}

		return new Point(x, y);
	}

    /**requests repaint of an area*/
    public void repaintArea(Rectangle screenBox,int maxY){

        int strip=10;

        float scaling=decode_pdf.getScaling();

        int x = (int)(screenBox.x*scaling)-strip;
        int y = (int)((maxY-screenBox.y-screenBox.height)*scaling)-strip;
        int width = (int)((screenBox.x+screenBox.width)*scaling)+strip+strip;
        int height = (int)((screenBox.y+screenBox.height)*scaling)+strip+strip;

        /**repaint manager*/
        RepaintManager currentManager=RepaintManager.currentManager(decode_pdf);

        currentManager.addDirtyRegion(decode_pdf,x,y,width,height);

    }
}
